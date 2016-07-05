package com.truemarkit.newrelic.oracle;

import com.truemarkit.newrelic.oracle.model.Metric;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

/**
 * Unit test to read the metrics file.
 *
 * @author Erik R. Jensen
 */
@Slf4j
public class ReadMetricsTest {

	@Test
	public void readMetrics() throws Exception {
		List<Metric> metrics = new OracleAgentFactory().readMetrics();
		log.info("Found [" + metrics.size() + "] metrics");
	}

}
