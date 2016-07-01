package com.truemarkit.newrelic.oracle;

import com.netradius.commons.lang.StringHelper;
import com.truemarkit.newrelic.oracle.model.ResultMetricData;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dilip S Sisodia
 */
@Slf4j
public class DatabaseUtil {

	private static Logger log = LoggerFactory.getLogger(DatabaseUtil.class);
	private static Connection connection;
	private static final String driver = "oracle.jdbc.driver.OracleDriver";

	public static Connection getConnection(String host, String port, String sid,
			String serviceName, String user, String password) {
		try {
			if(connection == null) {
				Class.forName(driver);
				String databaseUrl = getHostUrl(host, port, sid, serviceName);
				connection = DriverManager.getConnection(databaseUrl, user, password);
				if(connection == null) {
					log.error("Error getting connection to database url: " + databaseUrl);
				}
			}
		} catch (ClassNotFoundException ex) {
			log.error("Error loading database driver: " + ex.getMessage());
		} catch (SQLException ex) {
			log.error("Error getting database connection: " + ex.getMessage());
		}
		return connection;
	}

	private static String getHostUrl(String host, String port, String sid, String serviceName) {
		return StringHelper.isEmpty(sid)
				? "jdbc:oracle:thin:@//" + host.trim() + ":" + port.trim() + "/" + serviceName.trim()
				: "jdbc:oracle:thin:@" + host.trim() + ":" + port.trim() + ":" + sid.trim();
	}

	public List<ResultMetricData> getQueryResult(Connection conn, String query, String category, int descColumnCount, String unit) {
		Map<String, Float> results = new HashMap<>();
		List<ResultMetricData> returnMetrics = new ArrayList<>();

		if(conn == null) {
			log.error("Invalid connection");
			return null;
		}

		try(PreparedStatement statement = conn.prepareStatement(query);
		    ResultSet rs = statement.executeQuery()) {
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next()) {
				for (int i = 1; i <= metaData.getColumnCount(); i++) { // use column names as the "key"
					String value = rs.getString(i);
					String columnName = metaData.getColumnName(i).toLowerCase();
					String key = category.toLowerCase();

					for(int j = 1; j <= descColumnCount; j++) {
						key = key + "/" + rs.getString(j).toLowerCase();
					}

					if(i > descColumnCount) {
						key = key + "/" + columnName.toLowerCase();
						ResultMetricData data = new ResultMetricData();

						if (value == null) {
							results.put(key, -1.0f);
							data.setKey(key);
							data.setValue(-1.0f);
							data.setUnit(unit);
						} else {
							results.put(key, translateStringToNumber(value));
							data.setKey(key);
							data.setValue(translateStringToNumber(value));
							data.setUnit(unit);
						}
						returnMetrics.add(data);
					}
				}
			}

		} catch (SQLException ex) {
			log.error("Error executing query: " + ex.getMessage());
		}
		Map<String, Map<String, Float>> finalResult = new HashMap<>();
		finalResult.put(unit, results);
//		return results;
		return returnMetrics;
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
