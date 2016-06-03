package com.netradius.oraclePlugin;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.Config;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * @author Dilip S Sisodia
 */
@Slf4j
public class OracleAgentFactory extends AgentFactory {

	@Override
	public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
		String name = (String) properties.get("name");
		String host = (String) properties.get("host");
		String user = (String) properties.get("user");
		String password = (String) properties.get("password");
		String metrics = (String) properties.get("metrics");
		if (name == null || host == null || user == null || password == null) {
			throw new ConfigurationException("'name', 'host', 'user' and 'password' cannot be null.");
		}

		return new OracleAgent(name, host, user, password, processMetricCategories(metrics),readCategoryConfiguration());

	}

	public Map<String, Object> readCategoryConfiguration() throws ConfigurationException {
		Map<String, Object> metricCategories = new HashMap<String, Object>();
		try {
			JSONArray json = readJSONFile("metric.json");
			for (int i = 0; i < json.size(); i++) {
				JSONObject obj = (JSONObject) json.get(i);
				String category = (String) obj.get("id");
				metricCategories.put(category, obj);
			}
		} catch (ConfigurationException e) {
			throw new ConfigurationException("'metric_categories' could not be found in the 'plugin.json' configuration file");
		}
		return metricCategories;
	}

	Set<String> processMetricCategories(String metrics) {
		String[] categories = metrics.toLowerCase().split(",");
		Set<String> set = new HashSet<String>(Arrays.asList(categories));
		set.remove(""); // in case of trailing comma or two consecutive commas
		return set;
	}


}
