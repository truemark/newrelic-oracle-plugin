package com.truemarkit.newrelic.oracle;

import org.junit.Test;

import static com.truemarkit.newrelic.oracle.DatabaseUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for DatabaseUtil.
 *
 * @author Erik R. Jensen
 */
public class DatabaseUtilTest {

	@Test
	public void testSid() {
		assertThat("jdbc:oracle:thin:@localhost:1521:xe", is(equalTo(getJdbcUrl("localhost", "1521", "xe", null))));
		assertThat("jdbc:oracle:thin:@localhost:1521:xe", is(equalTo(getJdbcUrl("localhost", "1521", "xe", ""))));
	}

	@Test
	public void testServiceName() {
		assertThat("jdbc:oracle:thin:@//localhost:1521/xe", is(equalTo(getJdbcUrl("localhost", "1521", null, "xe"))));
		assertThat("jdbc:oracle:thin:@//localhost:1521/xe", is(equalTo(getJdbcUrl("localhost", "1521", "", "xe"))));
	}

}
