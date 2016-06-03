package com.netradius.oraclePlugin;


import com.newrelic.metrics.publish.Agent;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Dilip S Sisodia
 */
@Slf4j
public class OracleAgent extends Agent {

	private static final String GUID = "com.netradius.oracle";
	private static final String version = "1.0.0";
	private final String name;
	private final String host;
	private final String user;
	private final String password;
	private Map<String, Object> metricCategories = new HashMap<String, Object>();

	private DatabaseUtil oracleDB;
	private Connection connection;

	public OracleAgent(String name, String host, String user, String password, Set<String> metrics, Map<String, Object> metricCategories) {
		super(GUID, version);

		this.name = name; // Set local attributes for new class object
		this.host = host;
		this.user = user;
		this.password = password;
		this.metricCategories = this.metricCategories;

		oracleDB = new DatabaseUtil();
		connection = DatabaseUtil.getConnection(host, user, password);
	}

	@Override
	public void pollCycle() {

//		Connection c = m.getConnection(host, user, passwd, properties); // Get a database connection (which should be cached)
		if (connection == null) {
			connection = DatabaseUtil.getConnection(host, user, password);
		}

		Map<String, Float> results = gatherMetrics(connection); // Gather defined metrics
		reportMetrics(results); // Report Metrics to New Relic
	}

	@Override
	public String getComponentHumanLabel() {
		return name;
	}

	private Map<String, Float> gatherMetrics(Connection c) {
		Map<String, Float> results = new HashMap<>(); // Create an empty set of results
		Map<String, Object> categories = metricCategories; // Get current Metric Categories

		Iterator<String> iter = categories.keySet().iterator();
		while (iter.hasNext()) {
			String category = iter.next();
			@SuppressWarnings("unchecked")
			Map<String, String> attributes = (Map<String, String>) categories.get(category);
			results.putAll(oracleDB.getQueryResult(c, attributes.get("SQL"), category));
		}
		return results;
	}

	public void reportMetrics(Map<String, Float> results) {
		int count = 0;
		log.info("Collected ", results.size(), " MySQL metrics. ");
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