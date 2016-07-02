package com.truemarkit.newrelic.oracle;

import com.newrelic.agent.deps.org.slf4j.Logger;
import com.newrelic.agent.deps.org.slf4j.LoggerFactory;
import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

/**
 * Main entry point for the plugin.
 *
 * @author Dilip S Sisodia
 * @author Erik R. Jensen
 */
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		try {
			Runner runner = new Runner();
			runner.add(new OracleAgentFactory());
			runner.setupAndRun();
		} catch (ConfigurationException e) {
			log.error("Error starting plugin: " + e.getMessage(), e);
			System.exit(1);
		}
	}
}
