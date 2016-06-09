package com.truemarkit.newrelic.oracle;


import com.truemarkit.newrelic.oracle.model.Metric;
import com.newrelic.agent.deps.org.slf4j.LoggerFactory;
import com.newrelic.metrics.publish.Agent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
		Map<String, Float> results = gatherMetrics(connection); // Gather defined metrics
		reportMetrics(results); // Report Metrics to New Relic
	}

	@Override
	public String getAgentName() {
		return this.name;
	}

	private Map<String, Float> gatherMetrics(Connection c) {
		Map<String, Float> results = new HashMap<>(); // Create an empty set of results
		Map<String, Object> categories = metricCategories; // Get current Metric Categories

		Iterator<String> iter = categories.keySet().iterator();
		if (iter.hasNext()) {
			String category = iter.next();
			Metric attributes = (Metric) categories.get(category);
			results.putAll(oracleDB.getQueryResult(c, attributes.getSql(), attributes.getId()));
		}
		return results;
	}

	public void reportMetrics(Map<String, Float> results) {
		int count = 0;
		log.info("Reporting ", results.size(), " metrics. ");
		log.info(results.toString());

		Iterator<String> iter = results.keySet().iterator();
		while (iter.hasNext()) { // Iterate over current metrics
			String key = iter.next().toLowerCase();
			Float val = results.get(key);
			log.debug(key + " " + val.toString());
			count++;
			reportMetric(key, "BYTES", val);
		}
		log.debug("Reported to New Relic " + count + " metrics.");
	}
}