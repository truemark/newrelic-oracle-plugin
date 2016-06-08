package com.netradius.oracle.plugin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netradius.oracle.plugin.model.Metric;
import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.Config;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
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

		return new OracleAgent(name, host, user, password, readMetrics());
	}

	public Map<String, Object> readMetrics() {
		Map<String, Object> metricCategories = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
//			ClassLoader loader = ClassLoader.getSystemClassLoader();
			List<Metric> metrics = objectMapper.readValue(new File("config/metric.json"),
					new TypeReference<List<Metric>>() {});
			for (Metric metric: metrics) {
				if(metric.isEnabled()){
					metricCategories.put(metric.getId(), metric);
				}
			}
		} catch (Exception ex) {
			log.error("Can not read metrics: " + ex.getMessage());
		}
		return metricCategories;
	}

}
