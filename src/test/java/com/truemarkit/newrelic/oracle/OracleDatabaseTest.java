package com.truemarkit.newrelic.oracle;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.util.Map;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertNotNull;

/**
  * @author Dilip S Sisodia
 */
@Slf4j
public class OracleDatabaseTest {
	@Test
	public void testGetConnection() {
		DatabaseUtil db = new DatabaseUtil();
		Connection con = DatabaseUtil.getConnection("localhost", "1521", "xe", "scott", "tiger");
		assertNotNull(con);
	}

	@Test
	public void testBadHost() {
		Connection con = DatabaseUtil.getConnection("localhost", "1521", "xe", "scott", "tiger");
		assertNull(con);
	}

	@Test
	public void testRunSql() {
		DatabaseUtil db = new DatabaseUtil();
		Connection con = DatabaseUtil.getConnection("localhost", "1521", "xe", "scott", "tiger");
		Map<String, Float> results = db.getQueryResult(con, "select files.tablespace_name, maxbytes as \"BYTES SIZE\", bytes - free_bytes as \"BYTES USED\", maxbytes - (bytes - free_bytes) as \"BYTES AVAIL\", round(maxbytes / 1048576, 2) as \"MB SIZE\", round((bytes - free_bytes) / 1048576, 2) as \"MB USED\", round((maxbytes - (bytes - free_bytes)) / 1048576, 2) as \"MB AVAIL\", round(((bytes - free_bytes) / maxbytes) * 100, 2) as \"% USED\", round((100 - ((bytes - free_bytes) / maxbytes) * 100), 2) as \"% FREE\" from (select tablespace_name, sum(bytes) as bytes, sum(maxbytes) as maxbytes from (select tablespace_name, bytes, case autoextensible when 'YES' then maxbytes else bytes end as maxbytes from dba_data_files) group by tablespace_name) files, (select tablespace_name, sum(bytes) as free_bytes from dba_free_space group by tablespace_name) free where files.tablespace_name = free.tablespace_name order by free.tablespace_name", "Test");
		log.debug(results.toString());
	}
}
