package com.truemarkit.newrelic.oracle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Mapped to root element for metric data response from new relic api.
 *
 * @author Dilip S Sisodia
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricDataRoot {

  @JsonProperty("metric_data")
  private MetricData metricData;
}
