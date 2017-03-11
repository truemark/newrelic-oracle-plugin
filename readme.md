# New Relic Platform Oracle Plugin - Java
This plugin for oracle collects the key performance data for your oracle instance and reports the key metrics to
NewRelic dashboard, set up the alerts and get notified immediately on critical conditions.
## Requirements

The requirements for running this plugin are:

- A New Relic account. Sign up for a free account [here](http://newrelic.com)
- Java Runtime (JRE) environment Version 1.8 or later
- A server running Oracle
- Network access to New Relic

## Installation
#### NPI Compatible Installation:
This plugin can be installed with the New Relic Platform Installer (NPI). See the NPI install steps below.

1) Install New Relic Plateform Installer(NPI)
Please install the new relic plateform installer using command similar to:
```
LICENSE_KEY=YOUR_LICENSE_KEY_HERE bash -c "$(curl -sSL https://download.newrelic.com/npi/release/install-npi-linux-debian-x86.sh)"
```
Please select the appropriate version of npi tool based on your operating system from [here](https://docs.newrelic.com/docs/plugins/plugins-new-relic/installing-plugins/installing-npi-compatible-plugin#npi-os-version)

2) Above command should install the npi tool to your system, output of above command should display the install location.
the default install location is /Users/USERNAME/newrelic-npi, where USERNAME is your system username.

3) Now navigate to directory(newrelic-npi) where npi tool is installed, you will find the config directory there, which
 will contain the configuration information for your npi installation.
```
cd /Users/USERNAME/newrelic-npi
```
 
4) create the manifest.json file inside config directory using below command:
```
vim config/manifest.json
```
and copy paste below content and save the file.
```
[{

 "guid": "com.truemarkit.newrelic.oracle",
 "download_url": "https://github.com/truemark/newrelic-oracle-plugin/raw/master/dist/plugin-dist.tar.gz",
 "publisher": "TrueMark",
 "version": "1.1.0",
 "installer_compatible": true,
 "implementation": "Java"
}]
```
5) Now run below command to install the plugin:
```
    ./npi install com.truemarkit.newrelic.oracle --untrusted

```

Please follow the commandline to complete the configuration(check configuration section) and installation of plugin,
select yes to set as background process if you want to run the plugin as a service.

6) Now start the plugin using below command:
```
./npi start com.truemarkit.newrelic.oracle
```

#### Install Manually (Non-standard)

##### Step 1 - Downloading and Extracting the Plugin

The latest version of the plugin can be downloaded [here](https://github.com/truemark/newrelic-oracle-plugin).
Add the plugin.jar file to location of your choice.

##### Step 2 - Configuring the Plugin

Check out the [configuration information](#configuration-information) section for details on configuring your plugin.

##### Step 3 - Running the Plugin

To run the plugin, execute the following command from a terminal or command window (assuming Java is installed and on your path):

```
    java -Xmx128m -jar plugin.jar
```

## Updating Plugin
Plugin can be updated to a newer version, when available, as below:
 
1) You will receive a notification on New Relic dashboard when a newer version of plugin is available.

2) You can check details about plugin version [here](https://rpm.newrelic.com/accounts/1370610/plugins/directory/518)

3) Now navigate to directory(newrelic-npi) where npi tool is installed, you will find the config directory there, which
will contain the configuration information for your npi installation.
```
cd path_to_newrelic_install_directory
 
Example: cd /Users/USERNAME/newrelic-npi
```

4) Open the plugin manifest file (manifest.json) inside config directory
```
vim config/manifest.json
```

5) Update the version value for plugin to new version
```
[{

 "guid": "com.truemarkit.newrelic.oracle",
 "download_url": "https://github.com/truemark/newrelic-oracle-plugin/raw/master/dist/plugin-dist.tar.gz",
 "publisher": "TrueMark",
 "version": "1.1.0",  <== UPDATE NEW VERSION HERE
 "installer_compatible": true,
 "implementation": "Java"
}]
```

6) Back up your configuration files to some other location
```
cp plugins/com.truemarkit.newrelic.oracle/plugin/config/newrelic.json BACKUP_LOCATION
cp plugins/com.truemarkit.newrelic.oracle/plugin/config/plugin.json BACKUP_LOCATION
```

7) Now remove the service if plugin is setup as a background service.
```
./npi remove-service com.truemarkit.newrelic.oracle
```

8) Remove old plugin installation
```
./npi remove com.truemarkit.newrelic.oracle
```

9) Reinstall plugin
```
./npi install com.truemarkit.newrelic.oracle --untrusted
```

10) Stop the plugin service
```
./npi stop com.truemarkit.newrelic.oracle
```

11) Update your configuration files from backup location
```
cp BACKUP_LOCATION/newrelic.json plugins/com.truemarkit.newrelic.oracle/plugin/config/ 
cp BACKUP_LOCATION/plugin.json plugins/com.truemarkit.newrelic.oracle/plugin/config/ 
```

12) Re-start the plugin
```
./npi start com.truemarkit.newrelic.oracle
```

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

- metrics.yml - This file will be required if you want to use some custom sql to report data to New Relic dashboard otherwise default metrics defined in the plugin will be used.
  
  Add array of metric details to file, this will contails the details on what data you want to report.

`Example:`

```
  - id: "METRIC_CATEGORY_NAME"
    sql: "SQL_TO_FETCH_DATA_TO_REPORT"
    unit: "UNIT_FOR_DATA"
    enabled: TRUE - REPORT METRIC /FALSE - IGNORE METRIC
    descriptionColumnCount: "integer value to add the sql column content to be part of metric name"
```

- plugin.json - Add agent details in this file:

`Example:`

```
    {
      "agents": [
        {
          "name": "Name_For_Agent",
          "host": "Host_Address",
          "port": "Port for database connection",
          "service_name": "service name or sid, either one is required",
          "sid": "sid or service name, either one is required",
          "username": "db username",
          "password": "db user password"
        }
      ]
    }
```