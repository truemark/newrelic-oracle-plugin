package com.truemarkit.newrelic.oracle;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Dilip S Sisodia
 */
@Slf4j
public class DatabaseUtil {

	private static Connection connection;
	private static final String driver = "oracle.jdbc.driver.OracleDriver";

	public static Connection getConnection(String host, String port, String serviceName, String user, String password) {
		try {
			if(connection == null || !connection.isValid(30000)) {

				Class.forName(driver);
				connection = DriverManager.getConnection(getHostUrl(host, port, serviceName) , user, password);
			}
		} catch (ClassNotFoundException ex) {
			log.error("Error loading database driver: " + ex.getMessage());
		} catch (SQLException ex) {
			log.error("Error getting database connection: " + ex.getMessage());
		}
		return connection;
	}

	private static String getHostUrl(String host, String port, String serviceName) {
		return "jdbc:oacle:thin:@" + host.trim() + ":" + port.trim() + ":" + serviceName.trim();
	}

	public Map<String, Float> getQueryResult(Connection conn, String query, String category) {
		ResultSet rs;
		Map<String, Float> results = new HashMap<>();

		if(conn == null) {
			log.error("Invalid connection");
			return null;
		}
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			rs = statement.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next()) {
				for (int i = 1; i <= metaData.getColumnCount(); i++) { // use column names as the "key"
					String value = rs.getString(i);
					String columnName = metaData.getColumnName(i).toLowerCase();
					String key = category.toLowerCase() + "/" + columnName.toLowerCase();
					if (value == null) {
						results.put(key, -1.0f);
					} else {
						results.put(key, translateStringToNumber(value));
					}
				}
			}

		} catch (SQLException ex) {
			log.error("Error executing query: " + ex.getMessage());
		}
		return results;
	}

	private static Float translateStringToNumber(String val) {
		try {
			if (val.contains(" ")) {
				val = val.replaceAll(" ", "");
			}
			return Float.parseFloat(val);
		} catch (Exception e) {
			log.error("Unable to parse int/float number from value ", val);
		}
		return 0.0f;
	}


}
