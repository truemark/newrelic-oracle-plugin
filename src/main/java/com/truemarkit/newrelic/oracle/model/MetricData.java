package com.truemarkit.newrelic.oracle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * Holds metric data for a time range.
 *
 * @author Dilip S Sisodia
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricData {
  private Date from;
  private Date to;
  private List<PluginMetric> metrics;
}
