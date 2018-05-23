package com.truemarkit.newrelic.oracle.model;

import lombok.Data;

/**
 * Holds plugin api settings.
 *
 * @author Dilip S Sisodia
 */
@Data
public class PluginApi {
  private String apiUrl;
  private String pluginsApiUrl;
  private String componentsApiUrl;
  private String metricDataApiUrl;
  private Boolean reportTablespaceDaysToFull;
}
