# New Relic Platform Oracle Plugin - Java


----

## Requirements

The requirements for running this plugin are:

- A New Relic account. Sign up for a free account [here](http://newrelic.com)
- Java Runtime (JRE) environment Version 1.6 or later
- A server running Oracle
- Network access to New Relic

----

## Installation

This plugin can be installed as follows:

### Install Manually (Non-standard)

#### Step 1 - Downloading and Extracting the Plugin

The latest version of the plugin can be downloaded [here](https://github.com/truemark/newrelic-oracle-plugin).
Add the plugin.jar file to location of your choice.

#### Step 2 - Configuring the Plugin

Check out the [configuration information](#configuration-information) section for details on configuring your plugin.

#### Step 3 - Running the Plugin

To run the plugin, execute the following command from a terminal or command window (assuming Java is installed and on your path):

```
	java -Xmx128m -jar plugin.jar
```
----

## Configuration Information

### Configuration Files

Configuration files needs to be setup in order to get the plugin running.
- newrelic.json - Add your New Relic licence key to this file.

`Example:`

```
{
  "license_key": "YOUR_LICENSE_KEY_HERE"
  "log_level": "debug",
  "log_file_path": "/var/logs/newrelic"
}
```

- metric.json - Add array of metric details to file, this will contails the details on what data you want to report.

`Example:`

```
[
  {
	"id": "METRIC_CATEGORY_NAME",
    "sql": "SQL_TO_FETCH_DATA_TO_REPORT",
    "unit": "UNIT_FOR_DATA",
    "enabled": 'TRUE - "REPORT METRIC /FALSE - IGNORE METRIC"
  }
]
```

- plugin.json - Add agent details in this file:

`Example:`

```
    {
      "agents": [
        {
          "name" : "Name_For_Agent",
          "host" : "Host_Address",
          "user" : "USERNAME",
          "password" : "PASSWORD"
        }
      ]
    }
```

