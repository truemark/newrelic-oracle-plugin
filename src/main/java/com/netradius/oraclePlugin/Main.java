package com.netradius.oraclePlugin;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Dilip S Sisodia
 */
@Slf4j
public class Main {
	public static void main(String[] args) {
		try {
			Runner runner = new Runner();
			runner.add(new OracleAgentFactory());
			runner.setupAndRun();
		} catch (ConfigurationException e) {
			log.error("ERROR: can not start/run OracleAgentFactory:  " + e.getMessage());
			System.exit(-1);
		}
	}
}
