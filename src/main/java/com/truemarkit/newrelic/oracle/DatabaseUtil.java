package com.truemarkit.newrelic.oracle;

import com.netradius.commons.lang.StringHelper;
import com.newrelic.metrics.publish.util.Logger;
import com.truemarkit.newrelic.oracle.model.ResultMetricData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Contaoins database utility methods.
 *
 * @author Dilip S Sisodia
 * @author Erik R. Jensen
 */
public class DatabaseUtil {

	private static final Logger log = Logger.getLogger(DatabaseUtil.class);

	@Nonnull
	public static String getJdbcUrl(@Nonnull String host, @Nonnull String port,
									@Nullable String sid, @Nullable String serviceName) {
		return StringHelper.isEmpty(sid)
				? "jdbc:oracle:thin:@//" + host.trim() + ":" + port.trim() + "/" + serviceName.trim()
				: "jdbc:oracle:thin:@" + host.trim() + ":" + port.trim() + ":" + sid.trim();
	}

	public static HikariDataSource getHikariDataSource(String name, String host, String port, String sid,
													   String serviceName, String username, String password) {
		HikariDataSource dataSource = null;
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(getJdbcUrl(host, port, sid, serviceName));
		config.setUsername(username);
		config.setPassword(password);
		config.setReadOnly(true);
		config.setMinimumIdle(1);
		config.setMaximumPoolSize(3);
		config.setPoolName(name);
		config.setDriverClassName("oracle.jdbc.OracleDriver");
		config.setInitializationFailFast(true);
		config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(15L));
		config.setConnectionTestQuery("SELECT 1 FROM DUAL");
		try {
			dataSource = new HikariDataSource(config);
		} catch (HikariPool.PoolInitializationException ex) {
			log.error("Database connection error for component: " + name);
			log.error("Error Initializing database pool. Error connecting to database: " + host + ": " +
					(StringHelper.isEmpty(sid) ? serviceName : sid) + ex.getMessage());
		} catch (Exception ex) {
			log.error("Database connection error for component: " + name);
			log.error("Error connecting to database: " + host + ": " +
					(StringHelper.isEmpty(sid) ? serviceName : sid) + ex.getMessage());
		}
		return dataSource;
	}

	@Nonnull
	public static List<ResultMetricData> getQueryResult(@Nonnull Connection conn,
														@Nonnull String query, @Nonnull String category, int descColumnCount, @Nonnull String unit) {
		List<ResultMetricData> returnMetrics = new ArrayList<>();

		try (PreparedStatement statement = conn.prepareStatement(query);
			 ResultSet rs = statement.executeQuery()) {
			statement.setQueryTimeout(10);
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next()) {
				for (int i = 1; i <= metaData.getColumnCount(); i++) { // use column names as the "key"
					String value = rs.getString(i);
					String columnName = metaData.getColumnName(i).toLowerCase();
					String key = category.toLowerCase();
					String localUnit = unit;
					if (StringHelper.isEmpty(localUnit)) {
						localUnit = rs.getString(1);
					}

					for (int j = 1; j <= descColumnCount; j++) {
						key = key + "/" + rs.getString(j).toLowerCase();
					}

					if (i > descColumnCount) {
						if (!StringHelper.isEmpty(columnName)) {
							key = key + "/" + columnName.toLowerCase();
						}
						ResultMetricData data = new ResultMetricData();

						if (value == null) {
							data.setKey(key);
							data.setValue(-1.0f);
							data.setUnit(localUnit);
						} else {
							data.setKey(key);
							data.setValue(translateStringToNumber(value));
							data.setUnit(localUnit);
						}
						returnMetrics.add(data);
					}
				}
			}

		} catch (SQLException e) {
			log.error("Error executing query: " + e.getMessage(), e);
		}
		return returnMetrics;
	}

	@Nonnull
	public static boolean getDatabaseStatus(@Nonnull Connection conn) {
		String status = "";
		String dbStatus = "";
		String instanceName = "";
		String query = "SELECT INSTANCE_NAME, STATUS, DATABASE_STATUS FROM V$INSTANCE";

		try (PreparedStatement statement = conn.prepareStatement(query);
			 ResultSet rs = statement.executeQuery()) {
			statement.setQueryTimeout(10);
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next()) {
				status = rs.getString("STATUS");
				dbStatus = rs.getString("DATABASE_STATUS");
				instanceName = rs.getString("INSTANCE_NAME");
			}
		} catch (SQLException e) {
			log.error("Error executing query: " + e.getMessage(), e);
			return false;
		}

		if (StringHelper.isEmpty(status) || StringHelper.isEmpty(dbStatus) || StringHelper.isEmpty(instanceName)) {
			return false;
		} else {
			if (status.equals("OPEN") && dbStatus.equals("ACTIVE")) {
				return true;
			} else {
				return false;
			}
		}
	}

	private static float translateStringToNumber(@Nonnull String val) {
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
