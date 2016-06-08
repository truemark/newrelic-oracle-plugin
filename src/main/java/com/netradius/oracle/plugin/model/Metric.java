package com.netradius.oracle.plugin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
  * @author Dilip S Sisodia
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metric {
	private String id;
	private String sql;
	private String unit;
	private boolean enabled;
}
