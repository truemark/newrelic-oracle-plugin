package com.truemarkit.newrelic.oracle;

import com.netradius.commons.lang.StringHelper;
import com.newrelic.metrics.publish.util.Logger;
import com.truemarkit.newrelic.oracle.model.ResultMetricData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@Nonnull
	public static List<ResultMetricData> getQueryResult(@Nonnull Connection conn,
			@Nonnull String query, @Nonnull String category, int descColumnCount, @Nonnull String unit) {
		Map<String, Float> results = new HashMap<>();
		List<ResultMetricData> returnMetrics = new ArrayList<>();

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
						if(!StringHelper.isEmpty(columnName)) {
							key = key + "/" + columnName.toLowerCase();
						}
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

		} catch (SQLException e) {
			log.error("Error executing query: " + e.getMessage(), e);
		}
		// TODO I don't get this
		Map<String, Map<String, Float>> finalResult = new HashMap<>();
		finalResult.put(unit, results);
//		return results;
		return returnMetrics;
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
