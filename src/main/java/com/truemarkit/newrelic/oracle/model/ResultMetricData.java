package com.truemarkit.newrelic.oracle.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Dilip S Sisodia
 */
@Data
@Accessors(chain = true)
public class ResultMetricData {
  String key;
  String unit;
  Float value;
}
