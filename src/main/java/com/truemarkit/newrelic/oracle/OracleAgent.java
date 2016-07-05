package com.truemarkit.newrelic.oracle;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newrelic.agent.deps.org.slf4j.Logger;
import com.newrelic.agent.deps.org.slf4j.LoggerFactory;
import com.newrelic.metrics.publish.Agent;
import com.truemarkit.newrelic.oracle.model.Metric;
import com.truemarkit.newrelic.oracle.model.ResultMetricData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static com.truemarkit.newrelic.oracle.DatabaseUtil.*;

/**
 * @author Dilip S Sisodia
 * @author Erik R. jensen
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OracleAgent extends Agent {

	private static final Logger log = LoggerFactory.getLogger(OracleAgent.class);

	// This is used for testing
//	private static final String GUID = "com.truemarkit.newrelic.oracletest";
	// This is used for production
	private static final String GUID = "com.truemarkit.newrelic.oracle";
	private static final String version = "1.1.0";

	private HikariDataSource dataSource;

	private final String name;

	private List<Metric> metricCategories = new ArrayList<>();

	public OracleAgent(String name, String host, String port, String sid,
			String serviceName, String username, String password, List<Metric> metricCategories) {
		super(GUID, version);
		this.name = name;

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(getJdbcUrl(host, port, sid, serviceName));
		config.setUsername(username);
		config.setPassword(password);
		config.setReadOnly(true);
		config.setMinimumIdle(1);
		config.setMaximumPoolSize(2);
		config.setPoolName(name);
		config.setDriverClassName("oracle.jdbc.OracleDriver");
		config.setInitializationFailFast(true);
		config.setConnectionTestQuery("SELECT 1 FROM DUAL");
		dataSource = new HikariDataSource(config);

		// TODO I don't get this
		ObjectMapper objectMapper = new ObjectMapper();
		this.metricCategories = objectMapper.convertValue(metricCategories, new TypeReference<List<Metric>>() {});
	}

	@Override
	public String getAgentName() {
		return this.name;
	}

	@Override
	public void pollCycle() {
		List<ResultMetricData> results = gatherMetrics(); // Gather defined metrics
		reportMetrics(results); // Report Metrics to New Relic
	}

	private List<ResultMetricData> gatherMetrics() {
		List<Metric> categories = metricCategories; // Get current Metric Categories
		List<ResultMetricData> resultMetrics = new ArrayList<>();
		try (Connection conn = dataSource.getConnection()) {
			for (Metric metric : categories) {
				if (metric.isEnabled()) {
					resultMetrics.addAll(getQueryResult(conn, metric.getSql(), metric.getId(),
							metric.getDescriptionColumnCount(), metric.getUnit()));
				}
			}
		} catch (Exception e) {
			log.error("Error gathering metrics: " + e.getMessage(), e);
		}
		return resultMetrics;
	}

	public void reportMetrics(List<ResultMetricData> results) {
		int count = 0;
		for (ResultMetricData data :results) {
			if(data.getValue() == null) {
				log.error("Can not report null value for key: " + data.getKey());
			} else {
				reportMetric(data.getKey(),data.getUnit(), data.getValue());
				count++;
			}
		}
		log.debug("Reported [" + count + "] metrics");
	}
}