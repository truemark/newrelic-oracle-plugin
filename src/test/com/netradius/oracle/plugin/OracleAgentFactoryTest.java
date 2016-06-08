package com.netradius.oracle.plugin;

import com.netradius.oracle.plugin.model.Metric;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
  * @author Dilip S Sisodia
 */
public class OracleAgentFactoryTest {
	@Test
	public void testOracleAgentFactory() {
		OracleAgentFactory factory = new OracleAgentFactory();
		Map<String, Object> metric = new HashMap<>();

		metric.put("host", "localhost");
		metric.put("name", "local-oracle");
		metric.put("user", "scott");
		metric.put("password", "tiger");
		Metric testMetric = new Metric();
		testMetric.setId("test");
		testMetric.setEnabled(true);
		testMetric.setUnit("test");
		try {
			factory.createConfiguredAgent(metric);
		} catch (ConfigurationException ex) {
			log.error("Can not initialize Oracle AgentFactory" + ex.getMessage());
		}
	}

	@Test
	public void testReadMetrics() {
		OracleAgentFactory factory = new OracleAgentFactory();
		Map<String, Object> metrics = factory.readMetrics();
		assertTrue(metrics.size() > 0);
	}


}
