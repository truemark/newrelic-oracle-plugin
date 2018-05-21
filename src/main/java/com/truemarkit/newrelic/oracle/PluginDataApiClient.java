package com.truemarkit.newrelic.oracle;

import com.truemarkit.newrelic.oracle.http.QueryStringBuilder;
import com.truemarkit.newrelic.oracle.http.RestClient;
import com.truemarkit.newrelic.oracle.http.UrlConnectionRestClient;
import com.truemarkit.newrelic.oracle.model.Component;
import com.truemarkit.newrelic.oracle.model.ListView;
import com.truemarkit.newrelic.oracle.model.MetricDataRoot;
import com.truemarkit.newrelic.oracle.model.Plugin;
import com.truemarkit.newrelic.oracle.model.PluginMetric;
import com.truemarkit.newrelic.oracle.model.ResultMetricData;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Client for plugin data requests.
 *
 * @author Dilip S Sisodia
 */
@Slf4j
public class PluginDataApiClient implements Serializable {

  private static final long serialVersionUID = 2559029395731051238L;
  private static final String NEWRELIC_API = "https://api.newrelic.com/v2";
  private static final String PLUGINS_API = "https://api.newrelic.com/v2/plugins.json";
  private static final String COMPONENTS_API = "https://api.newrelic.com/v2/components.json";
  private static final String METRIC_DATA_API =
      "https://api.newrelic.com/v2/components/<COMPONENT_ID>/metrics/data.json";

  private RestClient restClient;

  PluginDataApiClient(String pluginDataApiKey) {
    this.restClient = new UrlConnectionRestClient(pluginDataApiKey);
  }

  private String getPluginsApi() {
    StringBuilder sb = new StringBuilder();
    sb.append(NEWRELIC_API)
        .append("/plugins.json?");
    QueryStringBuilder builder = new QueryStringBuilder();
    builder.add("filter[guid]", "com.truemarkit.newrelic.oracle");
    sb.append(builder.toQueryString());
    return sb.toString();
  }

  private String getComponentsApi(String pluginId, String componentName) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEWRELIC_API)
        .append("/components.json?");
    QueryStringBuilder builder = new QueryStringBuilder();
    builder.add("filter[name]", componentName);
    builder.add("filter[plugin_id]", pluginId);
    sb.append(builder.toQueryString());
    return sb.toString();
  }

  private String getMetricDataApi(String componentId, List<ResultMetricData> metrics) {
    StringBuilder sb = new StringBuilder();
    sb.append(NEWRELIC_API)
        .append("/components/")
        .append(componentId)
        .append("/metrics/data.json");
    QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.DATE, -7);
    queryStringBuilder.add("from", calendar.getTime().toInstant());
    queryStringBuilder.add("period", "3600");
    sb.append("?")
        .append(queryStringBuilder.toQueryString());
    queryStringBuilder = new QueryStringBuilder();
    for (ResultMetricData metricData : metrics) {
      queryStringBuilder.add(
          "names[]",
          "Component/" + metricData.getKey() + "[" + metricData.getUnit() + "]");
      sb.append("&" + queryStringBuilder.toQueryString());
    }
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  String getPluginId() throws IOException {
    ListView<Plugin> plugins = this.restClient.get(getPluginsApi(), ListView.class, Plugin.class);
    Plugin oraclePlugin = plugins.getItems().stream().filter(
        plugin -> plugin.getPublisher().equals("TrueMark")).findFirst().orElse(new Plugin());
    return oraclePlugin.getId();
  }

  @SuppressWarnings("unchecked")
  String getComponentId(String pluginId, String componentName) throws IOException {
    ListView<Component> components = this.restClient.get(
        getComponentsApi(pluginId, componentName),
        ListView.class,
        Component.class);
    Component component = components.getItems().get(0);
    return component == null ? null : component.getId();
  }

  public <T, S> T get(String path, Class<T> clazz, Class<S> parameterClass) throws IOException {
    return restClient.get(path, clazz, parameterClass);
  }

  List<PluginMetric> getMetricData(
      String componentId,
      List<ResultMetricData> tablespaceMetrics) throws IOException {
    MetricDataRoot metricDataRoot = this.restClient.get(
        getMetricDataApi(componentId, tablespaceMetrics),
        MetricDataRoot.class);
    return metricDataRoot.getMetricData().getMetrics();
  }

}
