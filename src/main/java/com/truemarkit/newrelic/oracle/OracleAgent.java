package com.truemarkit.newrelic.oracle;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.util.Logger;
import com.truemarkit.newrelic.oracle.model.Metric;
import com.truemarkit.newrelic.oracle.model.ResultMetricData;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static com.truemarkit.newrelic.oracle.DatabaseUtil.getDatabaseStatus;
import static com.truemarkit.newrelic.oracle.DatabaseUtil.getHikariDataSource;
import static com.truemarkit.newrelic.oracle.DatabaseUtil.getQueryResult;

/**
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

  private HikariDataSource dataSource;

  private final String name;
  private final String host;
  private final String port;
  private final String sid;
  private final String serviceName;
  private final String username;
  private final String password;

  private List<Metric> metricCategories = new ArrayList<>();

  public OracleAgent(String name, String host, String port, String sid,
                     String serviceName, String username, String password, List<Metric> metricCategories) {
    super(GUID, version);
    this.name = name;
    this.host = host;
    this.port = port;
    this.sid = sid;
    this.serviceName = serviceName;
    this.username = username;
    this.password = password;

    this.dataSource = getHikariDataSource(name, host, port, sid, serviceName, username, password);

    // TODO I don't get this
    ObjectMapper objectMapper = new ObjectMapper();
    this.metricCategories = objectMapper.convertValue(metricCategories,
        new TypeReference<List<Metric>>() {
        });
  }

  @Override
  public String getAgentName() {
    return this.name;
  }

  @Override
  public void pollCycle() {
    reportMetrics(gatherMetrics());
  }

  private List<ResultMetricData> gatherMetrics() {
    List<ResultMetricData> resultMetrics = new ArrayList<>();
    if (this.dataSource == null) {
      this.dataSource = getHikariDataSource(this.name, this.host, this.port, this.sid, this.serviceName,
          this.username, this.password);
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

      for (Metric metric : this.metricCategories) {
        if (metric.isEnabled()) {
          resultMetrics.addAll(
              getQueryResult(conn,
                  metric.getSql(),
                  metric.getId(),
                  metric.getDescriptionColumnCount(),
                  metric.getUnit(),
                  metric.getDatabaseTable()));
        }
      }
    } catch (Exception e) {
      log.error("Error getting data for component: " + this.name);
      log.error("Error gathering metrics: " + e.getMessage());
    }

    return resultMetrics;
  }

  public void reportMetrics(List<ResultMetricData> results) {
    int count = 0;
    if(results != null) {
      for (ResultMetricData data : results) {
        try {
          if (data != null) {
            if (data.getValue() == null) {
              log.error("Can not report null value for key: " + data.getKey());
            } else {
              reportMetric(data.getKey(), data.getUnit(), data.getValue());
              log.debug("key: ", data.getKey(),
                  " value: ", data.getValue(),
                  " unit: ", data.getUnit());
              count++;
            }
          }
        } catch (Exception ex) {
          if (ex.getCause() != null) {
            log.error("Error reporting metrics: " + ex.getMessage());
          }
        }
      }
    }
    log.debug("Reported [" + count + "] metrics");
  }
}