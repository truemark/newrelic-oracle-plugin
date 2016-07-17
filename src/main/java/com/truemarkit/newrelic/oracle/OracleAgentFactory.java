package com.truemarkit.newrelic.oracle;

import com.netradius.commons.lang.StringHelper;
import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.truemarkit.newrelic.oracle.model.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Dilip S Sisodia
 * @author Erik R. Jensen
 */
@Component
public class OracleAgentFactory extends AgentFactory {

	private static final Logger log = LoggerFactory.getLogger(OracleAgentFactory.class);

	@Autowired
	private Metrics metrics;

	@Override
	public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
		String name = (String) properties.get("name");
		if (StringHelper.isEmpty(name)) {
			throw new ConfigurationException("name may not be empty");
		}
		String host = (String) properties.get("host");
		if (StringHelper.isEmpty(host)) {
			throw new ConfigurationException("host may not be empty");
		}
		String port = (String) properties.get("port");
		if (StringHelper.isEmpty(port)) {
			throw new ConfigurationException("port may not be empty");
		}
		String serviceName = (String) properties.get("service_name");
		String sid = (String) properties.get("sid");
		if (StringHelper.isEmpty(serviceName) && StringHelper.isEmpty(sid)) {
			throw new ConfigurationException("service_name or sid must have a value");
		}
		String username = (String) properties.get("username");
		if (StringHelper.isEmpty(username)) {
			throw new ConfigurationException("username may not be empty");
		}
		String password = (String) properties.get("password");
		if (StringHelper.isEmpty(password)) {
			throw new ConfigurationException("password may not be empty");
		}
		return new OracleAgent(name, host, port, sid, serviceName, username, password, metrics.getMetrics());
	}
}
