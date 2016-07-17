package com.truemarkit.newrelic.oracle;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Main entry point for the plugin.
 *
 * @author Dilip S Sisodia
 * @author Erik R. Jensen
 */
@Component
public class Main implements CommandLineRunner {

	private static final Logger log = Logger.getLogger(Main.class);
	private OracleAgentFactory oracleAgentFactory;

	@Autowired
	private Main(OracleAgentFactory oracleAgentFactory) {
		this.oracleAgentFactory = oracleAgentFactory;
	}

	public void main(String[] args) {
		try {
			Runner runner = new Runner();
			runner.add(oracleAgentFactory);
			runner.setupAndRun();
		} catch (ConfigurationException e) {
			log.error("Error starting plugin: " + e.getMessage(), e);
			System.exit(1);
		}
	}

	@Override
	public void run(String... strings) throws Exception {
		main(strings);
	}
}
