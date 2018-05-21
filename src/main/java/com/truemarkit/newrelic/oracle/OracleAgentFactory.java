package com.truemarkit.newrelic.oracle;

import com.netradius.commons.lang.StringHelper;
import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.Config;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;
import com.truemarkit.newrelic.oracle.model.Metric;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Agent factory for creating OracleAgent.
 *
 * @author Dilip S Sisodia
 * @author Erik R. Jensen
 */
public class OracleAgentFactory extends AgentFactory {

  private static final Logger log = Logger.getLogger(OracleAgentFactory.class);

  @Override
  public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
    String name = (String) properties.get("name");
    if (StringHelper.isEmpty(name)) {
      throw new ConfigurationException("name may not be empty");
    }
    String host = (String) properties.get("host");
    if (StringHelper.isEmpty(host)) {
      throw new ConfigurationException("host may not be empty");
    }
    String port = (String) properties.get("port");
    if (StringHelper.isEmpty(port)) {
      throw new ConfigurationException("port may not be empty");
    }
    String serviceName = (String) properties.get("service_name");
    String sid = (String) properties.get("sid");
    if (StringHelper.isEmpty(serviceName) && StringHelper.isEmpty(sid)) {
      throw new ConfigurationException("service_name or sid must have a value");
    }
    String username = (String) properties.get("username");
    if (StringHelper.isEmpty(username)) {
      throw new ConfigurationException("username may not be empty");
    }
    String password = (String) properties.get("password");
    if (StringHelper.isEmpty(password)) {
      throw new ConfigurationException("password may not be empty");
    }
    Map<String, String> apiConfigMap = new HashMap<>();
    try {
      apiConfigMap = readPluginDataApiConfiguration("plugin.json");
    } catch (Exception ex) {
      throw new ConfigurationException("Error reading api configuration.");
    }
    return new OracleAgent(name, host, port, sid, serviceName, username, password, readMetrics(),
        apiConfigMap);
  }

  @SuppressWarnings("unchecked")
  public List<Metric> readMetrics() throws ConfigurationException {
    Yaml y = new Yaml();
    try (InputStream in = Main.class.getClass().getResourceAsStream("/metrics.yml")) {
      Iterator<Object> lstObj = y.loadAll(in).iterator();
      if (lstObj.hasNext()) {
        List<Metric> metrics = (List<Metric>) lstObj.next();
        log.debug("Found [" + metrics.size() + "] metrics");
        return metrics;
      }
    } catch (Exception e) {
      throw new ConfigurationException("Failed to read metrics: " + e.getMessage(), e);
    }
    throw new ConfigurationException("No metrics found");
  }

  public Map<String, String> readPluginDataApiConfiguration(String configFileName)
      throws IOException, ConfigurationException {
    Map<String, String> apiConfiguration = new HashMap<>();
    try {
      String path = Config.getConfigDirectory() + File.separatorChar + configFileName;
      File file = new File(path);

      if (!file.exists()) {
        log.error("Cannot find config file " + path);
        throw new ConfigurationException("Cannot find config file " + path);
      }
      FileReader reader = new FileReader(file);
      try {

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        apiConfiguration.putAll((Map<String, String>) jsonObject.get("plugin"));
      } catch (IOException | ParseException ex) {
        log.error("Can not read or parse api configuration: " + ex.getMessage());
        throw new ConfigurationException("Can not read or parse api configuration: "
            + ex.getMessage());
      } finally {
        if (reader != null) {
          reader.close();
        }
      }

    } catch (ConfigurationException e) {
      throw new ConfigurationException("'metric_categories' could not be found in the "
          + "'plugin.json' configuration file");
    }
    return apiConfiguration;
  }
}
