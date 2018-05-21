package com.truemarkit.newrelic.oracle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;

/**
 * Holds timeslice value for metric.
 *
 * @author Dilip S Sisodia
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Timeslice {
  private Date from;
  private Date to;
  private long averageValue;
  private long value;

  @JsonProperty("values")
  private void setValues(Map<String, Long> values) {
    this.averageValue = (Long) values.get("average_value");
  }
}
