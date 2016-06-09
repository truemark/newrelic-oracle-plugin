package com.truemarkit.newrelic.oracle;


import com.truemarkit.newrelic.oracle.model.Metric;
import com.newrelic.agent.deps.org.slf4j.LoggerFactory;
import com.newrelic.metrics.publish.Agent;
import com.truemarkit.newrelic.oracle.model.ResultMetricData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Dilip S Sisodia
 */
@Slf4j
@Data
public class OracleAgent extends Agent {

	private static com.newrelic.agent.deps.org.slf4j.Logger log = LoggerFactory.getLogger(OracleAgent.class);

	private static final String GUID = "com.truemarkit.newrelic.oracletest";
	private static final String version = "1.0.0";
	private final String name;
	private final String host;
	private final String port;
	private final String serviceName;
	private final String user;
	private final String password;

	private Map<String, Object> metricCategories = new HashMap<>();

	private DatabaseUtil oracleDB;
	private Connection connection;

	public OracleAgent(String name, String host, String port, String serviceName, String user, String password, Map<String, Object> metricCategories) {
		super(GUID, version);

		this.name = name;
		this.host = host;
		this.port = port;
		this.serviceName = serviceName;
		this.user = user;
		this.password = password;
		this.metricCategories = metricCategories;

		oracleDB = new DatabaseUtil();
		connection = DatabaseUtil.getConnection(host, port, serviceName, user, password);
	}

	@Override
	public void pollCycle() {

		if (connection == null) {
			connection = DatabaseUtil.getConnection(host, port, serviceName, user, password);
		}
		List<ResultMetricData> results = gatherMetrics(connection); // Gather defined metrics
		reportMetrics(results); // Report Metrics to New Relic
	}

	@Override
	public String getAgentName() {
		return this.name;
	}

	private List<ResultMetricData> gatherMetrics(Connection c) {
		Map<String, Object> categories = metricCategories; // Get current Metric Categories
		List<ResultMetricData> resultMetrics = new ArrayList<>();

		Iterator<String> iter = categories.keySet().iterator();
		while (iter.hasNext()) {
			String category = iter.next();
			Metric attributes = (Metric) categories.get(category);
			try {
				if(c == null) {
					c = DatabaseUtil.getConnection(host, port, serviceName, user, password);
				}
				resultMetrics.addAll(oracleDB.getQueryResult(c, attributes.getSql(), attributes.getId(), attributes.getDescriptionColumnCount(), attributes.getUnit()));
			} catch (Exception ex) {
				log.error("Database connection is invalid: " + ex.getMessage());
			}
		}
//		return results;
		return resultMetrics;
	}

	public void reportMetrics(List<ResultMetricData> results) {
		int count = 0;
		log.info("Reporting ", results.size(), " metrics. ");
		log.info(results.toString());

		for (ResultMetricData data :results) {
			if(data.getValue() == null) {
				log.error("Can not report null value for key: " + data.getKey());
			} else {
				reportMetric(data.getKey(),data.getUnit(), data.getValue());
				count++;
			}
		}
		log.debug("Reported to New Relic " + count + " metrics.");
	}
}