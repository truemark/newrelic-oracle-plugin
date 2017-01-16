package com.truemarkit.newrelic.oracle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Dilip S Sisodia
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "metrics", locations = {"classpath:/metrics.yml"})
public class Metrics {
	private List<Metric> metrics;
}
