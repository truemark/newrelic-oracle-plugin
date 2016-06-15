package com.truemarkit.newrelic.oracle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.truemarkit.newrelic.oracle.model.Metric;
import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
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
		String port = (String) properties.get("port");
		String serviceName = (String) properties.get("service_name");
		String user = (String) properties.get("user");
		String password = (String) properties.get("password");
		String metrics = (String) properties.get("metrics");
		if (name == null || host == null || user == null || password == null || port == null || serviceName == null) {
			throw new ConfigurationException("'name', 'host', 'user' and 'password' cannot be null.");
		}

		return new OracleAgent(name, host, port, serviceName, user, password, readMetrics());
	}

	public List<Metric> readMetrics() {
		List<Metric> metrics = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			metrics = objectMapper.readValue(new File("config/metric.json"),
					new TypeReference<List<Metric>>() {});
		} catch (Exception ex) {
			log.error("Can not read metrics: " + ex.getMessage());
		}
		return metrics;
	}

}
