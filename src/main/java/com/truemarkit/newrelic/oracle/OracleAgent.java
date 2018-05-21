package com.truemarkit.newrelic.oracle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.util.Logger;
import com.truemarkit.newrelic.oracle.model.Metric;
import com.truemarkit.newrelic.oracle.model.PluginMetric;
import com.truemarkit.newrelic.oracle.model.ResultMetricData;
import com.truemarkit.newrelic.oracle.model.Timeslice;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.truemarkit.newrelic.oracle.DatabaseUtil.getDatabaseStatus;
import static com.truemarkit.newrelic.oracle.DatabaseUtil.getHikariDataSource;
import static com.truemarkit.newrelic.oracle.DatabaseUtil.getQueryResult;

/**
 * Agent for collecting and reporting data to new relic.
 *
 * @author Dilip S Sisodia
 * @author Erik R. jensen
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OracleAgent extends Agent {

  private static final Logger log = Logger.getLogger(OracleAgent.class);

  // This is used for testing
  // private static final String GUID = "com.truemarkit.newrelic.oracletest";
  // This is used for production
  private static final String GUID = "com.truemarkit.newrelic.oracle";
  private static final String version = "1.1.1";
  private final String name;
  private final String host;
  private final String port;
  private final String sid;
  private final String serviceName;
  private final String username;
  private final String password;
  List<ResultMetricData> lastMinuteMetrics = new ArrayList<>();
  private HikariDataSource dataSource;
  private List<Metric> metricCategories = new ArrayList<>();
  private final Map<String, String> apiConfiguration;
  private final PluginDataApiClient pluginDataApiClient;
  private String pluginId;
  private String componentId;

  public OracleAgent(String name,
                     String host,
                     String port,
                     String sid,
                     String serviceName,
                     String username,
                     String password,
                     List<Metric> metricCategories,
                     Map<String, String> apiConfiguration) {
    super(GUID, version);
    this.name = name;
    this.host = host;
    this.port = port;
    this.sid = sid;
    this.serviceName = serviceName;
    this.username = username;
    this.password = password;

    this.dataSource = getHikariDataSource(name, host, port, sid, serviceName, username, password);
    this.apiConfiguration = apiConfiguration;
    // TODO I don't get this
    ObjectMapper objectMapper = new ObjectMapper();
    this.metricCategories = objectMapper.convertValue(metricCategories,
        new TypeReference<List<Metric>>() {
        });
    this.pluginDataApiClient = new PluginDataApiClient(
        this.apiConfiguration.get("pluginDataApiKey"));
    try {
      this.pluginId = pluginDataApiClient.getPluginId();
      this.componentId = pluginDataApiClient.getComponentId(this.pluginId, this.name);
    } catch (IOException ex) {
      log.error("Error fetching tablespace used history data from new relic. " + ex.getMessage());
    }
  }

  @Override
  public String getAgentName() {
    return this.name;
  }

  @Override
  public void pollCycle() {
    this.lastMinuteMetrics = gatherMetrics(); // Gather defined metrics
    reportMetrics(this.lastMinuteMetrics);
  }

  private List<ResultMetricData> gatherMetrics() {
    List<Metric> categories = metricCategories; // Get current Metric Categories
    List<ResultMetricData> resultMetrics = new ArrayList<>();

    if (this.dataSource == null) {
      this.dataSource = getHikariDataSource(this.name, this.host, this.port, this.sid,
          this.serviceName, this.username, this.password);
    }
    try (Connection conn = this.dataSource.getConnection()) {
      if (getDatabaseStatus(conn)) {
        resultMetrics.add(new ResultMetricData()
            .setKey("database-down")
            .setValue(0f)
            .setUnit("down"));
      } else {
        resultMetrics.add(new ResultMetricData()
            .setKey("database-down")
            .setValue(1f)
            .setUnit("down"));
      }
    } catch (Exception e) {
      resultMetrics.add(new ResultMetricData()
          .setKey("database-down")
          .setValue(1f)
          .setUnit("down"));
      log.error("Error getting data for component: " + this.name);
      log.error("Error gathering metrics: " + e.getMessage());
    }

    try (Connection conn = this.dataSource.getConnection()) {
      for (Metric metric : categories) {
        if (metric.isEnabled()) {
          resultMetrics.addAll(getQueryResult(conn, metric.getSql(), metric.getId(),
              metric.getDescriptionColumnCount(), metric.getUnit()));
        }
      }
    } catch (Exception e) {
      log.error("Error getting data for component: " + this.name);
      log.error("Error gathering metrics: " + e.getMessage());
    }

    if (Boolean.getBoolean(this.apiConfiguration.get("pluginDataApiKey"))) {
      resultMetrics.addAll(getDaysToFullMetrics(resultMetrics));
    }
    return resultMetrics;
  }

  private void reportMetrics(List<ResultMetricData> results) {
    int count = 0;
    for (ResultMetricData data : results) {
      try {
        if (data != null) {
          if (data.getValue() == null) {
            log.error("Can not report null value for key: " + data.getKey());
          } else {
            reportMetric(data.getKey(), data.getUnit(), data.getValue());
            log.debug("key: ", data.getKey(), data.getValue(), data.getUnit());
            count++;
          }
        }
      } catch (Exception ex) {
        log.error("Error reporting metrics: " + ex.getMessage());
      }
    }
    log.debug("Reported [" + count + "] metrics");
  }

  private int calculateRateOfChange(
      PluginMetric metricData,
      ResultMetricData availableTablespace) {
    List<Timeslice> timeslices = metricData.getTimeslices();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Map<String, List<Timeslice>> groupedTimeslices =
        timeslices.stream().collect(
            Collectors.groupingBy(t -> dateFormat.format(t.getFrom())));
    Map<String, Timeslice> dateTs = new TreeMap<>();
    groupedTimeslices.forEach((d, t) -> {
      Optional<Timeslice> maxTimeslice = t.stream().max(
          Comparator.comparing(Timeslice::getAverageValue));
      maxTimeslice.ifPresent(timeslice -> {
        dateTs.putIfAbsent(d, timeslice);
      });
    });

    AtomicLong lastValue = new AtomicLong(0);
    AtomicInteger index = new AtomicInteger(0);

    dateTs.forEach((d, ts) -> {
      long value = ts.getAverageValue() - lastValue.getAndSet(ts.getAverageValue());
      ts.setValue(value <= 0 ? 0 : value);
      if (index.getAndIncrement() == 0) {
        ts.setValue(0);
      }
      dateTs.put(d, ts);
    });

    float avgRateOfChangePerDay = (dateTs.values().stream().mapToLong(Timeslice::getValue)
        .sum()) / (dateTs.size() - 1);
    Double daysToFull = avgRateOfChangePerDay <= 0
        ? 1000
        : Math.ceil(availableTablespace.getValue() / avgRateOfChangePerDay);
    return daysToFull.intValue();
  }

  private List<ResultMetricData> getDaysToFullMetrics(List<ResultMetricData> resultMetrics) {
    Pattern patternUsed = Pattern.compile("tablespace/(.*)/used/bytes");

    List<ResultMetricData> tablespaceUsedMetrics = resultMetrics.stream()
        .filter(resultMetricData -> {
          return patternUsed.matcher(resultMetricData.getKey()).matches();
        }).collect(Collectors.toList());

    List<ResultMetricData> daysToFullMetrics = new ArrayList<>();
    try {
      List<PluginMetric> pluginMetrics =
          pluginDataApiClient.getMetricData(this.componentId, tablespaceUsedMetrics);
      pluginMetrics.forEach(pluginMetric -> {
        Pattern pattern = Pattern.compile("Component/(.*)/used/bytes[BYTES]");
        Matcher matcher = patternUsed.matcher(pluginMetric.getName());
        String tablespaceName = "";
        if (matcher.find()) {
          tablespaceName = matcher.group(1);
        }

        String sizeKeyName = "tablespace/" + tablespaceName + "/avail/bytes";
        ResultMetricData availTablespace = resultMetrics.stream().filter(at -> {
          return at.getKey().equals(sizeKeyName);
        }).findFirst().get();

        int daysToFull = calculateRateOfChange(pluginMetric, availTablespace);
        ResultMetricData data = new ResultMetricData();
        data.setKey("tablespace/" + tablespaceName + "/days to full");
        data.setValue((float) (daysToFull > 1000 ? 1000 : daysToFull));
        data.setUnit("days");
        daysToFullMetrics.add(data);
      });

    } catch (IOException ex) {
      log.error("Error fetching historical metric data for tablespace used. " + ex.getMessage());
    }
    return daysToFullMetrics;
  }
}
