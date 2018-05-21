package com.truemarkit.newrelic.oracle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Holds data for new relic plugin.
 *
 * @author Dilip S Sisodia
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Plugin {
  private String id;
  private String name;
  private String guid;
  private String publisher;
}
