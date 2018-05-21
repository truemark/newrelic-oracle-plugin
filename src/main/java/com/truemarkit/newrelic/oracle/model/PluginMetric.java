package com.truemarkit.newrelic.oracle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Holds data for metric.
 *
 * @author Dilip S Sisodia
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginMetric {
  private String name;
  private List<Timeslice> timeslices;
}
