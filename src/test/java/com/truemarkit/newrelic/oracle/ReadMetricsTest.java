package com.truemarkit.newrelic.oracle;

import com.truemarkit.newrelic.oracle.model.Metric;
import com.truemarkit.newrelic.oracle.model.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Unit test to read the metrics file.
 *
 * @author Erik R. Jensen
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Metrics.class)
public class ReadMetricsTest {

	@Autowired
	private Metrics loadedMetrics;

	@Test
	public void readMetrics() throws Exception {
		List<Metric> metrics = loadedMetrics.getMetrics();
		log.info("Found [" + metrics.size() + "] metrics");
	}

}
