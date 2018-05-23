package com.truemarkit.newrelic.oracle;

import com.netradius.commons.lang.StringHelper;
import com.newrelic.metrics.publish.util.Logger;
import com.truemarkit.newrelic.oracle.model.ResultMetricData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Contains database utility methods.
 *
 * @author Dilip S Sisodia
 * @author Erik R. Jensen
 */
public class DatabaseUtil {

  private static final Logger log = Logger.getLogger(DatabaseUtil.class);

  @Nonnull
  static String getJdbcUrl(
      @Nonnull String host,
      @Nonnull String port,
      @Nullable String sid,
      @Nullable String serviceName) {
    return StringHelper.isEmpty(sid)
        ? "jdbc:oracle:thin:@//" + host.trim() + ":" + port.trim() + "/" + serviceName.trim()
        : "jdbc:oracle:thin:@" + host.trim() + ":" + port.trim() + ":" + sid.trim();
  }

  static HikariDataSource getHikariDataSource(
      String name,
      String host,
      String port,
      String sid,
      String serviceName,
      String username,
      String password) {
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
      log.error("Error Initializing database pool. Error connecting to database: "
          + host + ": " + (StringHelper.isEmpty(sid) ? serviceName : sid) + ex.getMessage());
    } catch (Exception ex) {
      log.error("Database connection error for component: " + name);
      log.error("Error connecting to database: " + host + ": "
          + (StringHelper.isEmpty(sid) ? serviceName : sid) + ex.getMessage());
    }
    return dataSource;
  }

  @Nonnull
  static List<ResultMetricData> getQueryResult(
      @Nonnull Connection conn,
      @Nonnull String query,
      @Nonnull String category,
      int descColumnCount,
      @Nonnull String unit) {
    List<ResultMetricData> returnMetrics = new ArrayList<>();

    try (PreparedStatement statement = conn.prepareStatement(query);
         ResultSet rs = statement.executeQuery()) {
      statement.setQueryTimeout(10);
      ResultSetMetaData metaData = rs.getMetaData();
      while (rs.next()) {
        for (int i = 1; i <= metaData.getColumnCount(); i++) { // use column names as the "key"
          String value = rs.getString(i);
          String columnName = metaData.getColumnName(i).toLowerCase();
          StringBuilder keyBuilder = new StringBuilder();
          keyBuilder.append(category.toLowerCase());
          String localUnit = unit;
          if (StringHelper.isEmpty(localUnit)) {
            localUnit = rs.getString(1);
          }

          for (int j = 1; j <= descColumnCount; j++) {
            keyBuilder
                .append("/")
                .append(rs.getString(j).toLowerCase());
          }

          if (i > descColumnCount) {
            if (!StringHelper.isEmpty(columnName)) {
              keyBuilder
                  .append("/")
                  .append(columnName.toLowerCase());
            }
            ResultMetricData data = new ResultMetricData();

            if (value == null) {
              data.setKey(keyBuilder.toString());
              data.setValue(-1.0f);
              data.setUnit(localUnit);
            } else {
              data.setKey(keyBuilder.toString());
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

  static boolean getDatabaseStatus(@Nonnull Connection conn) {
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

    if (StringHelper.isEmpty(status)
        || StringHelper.isEmpty(dbStatus)
        || StringHelper.isEmpty(instanceName)) {
      return false;
    } else {
      return status.equals("OPEN") && dbStatus.equals("ACTIVE");
    }
  }

  public static List<String> getAllTablespaces(@Nonnull Connection conn) {
    String query = "select tablespace_name from dba_data_files";
    List<String> tablespaces = new ArrayList<>();
    try (PreparedStatement statement = conn.prepareStatement(query);
         ResultSet rs = statement.executeQuery()) {
      statement.setQueryTimeout(10);
      ResultSetMetaData metaData = rs.getMetaData();
      while (rs.next()) {
        tablespaces.add(rs.getString("TABLESPACE_NAME"));
      }
    } catch (SQLException e) {
      log.error("Error executing query: " + e.getMessage(), e);
    }
    return tablespaces;
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
