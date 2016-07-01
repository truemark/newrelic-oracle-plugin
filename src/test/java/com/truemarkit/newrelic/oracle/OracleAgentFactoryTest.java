package com.truemarkit.newrelic.oracle;

import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.truemarkit.newrelic.oracle.model.Metric;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
  * @author Dilip S Sisodia
 */
@Slf4j
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
	public void testReadMetrics() throws Exception {
		OracleAgentFactory factory = new OracleAgentFactory();
		List<Metric> metrics = factory.readMetrics();
		assertTrue(metrics.size() > 0);
	}


}
