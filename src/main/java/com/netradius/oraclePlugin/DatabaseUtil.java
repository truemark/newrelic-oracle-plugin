package com.netradius.oraclePlugin;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dilip S Sisodia
 */
@Slf4j
public class DatabaseUtil {

	private static Connection connection;
	private static final String driver = "oracle.jdbc.driver.OracleDriver";

	public static Connection getConnection(String host, String user, String password) {
		try {
			if(connection == null || !connection.isValid(30000)) {
				Class.forName(driver);
				connection = DriverManager.getConnection(host, user, password);
			}
		} catch (ClassNotFoundException ex) {
			log.error("Error loading database driver: " + ex.getMessage());
		} catch (SQLException ex) {
			log.error("Error getting database connection: " + ex.getMessage());
		}
		return connection;
	}

	public Map<String, Float> getQueryResult(Connection conn, String query, String category) {
		ResultSet rs = null;
		Map<String, Float> results = new HashMap<String, Float>();

		try {
			PreparedStatement statement = conn.prepareStatement(query);
			rs = statement.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			if (rs.next()) {
				for (int i = 1; i <= metaData.getColumnCount(); i++) { // use column names as the "key"
					String value = rs.getString(i);
					String columnName = metaData.getColumnName(i).toLowerCase();
					String key = category + "/" + columnName;
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

	public static Float translateStringToNumber(String val) {
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
