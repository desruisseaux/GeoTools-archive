package org.geotools.data.h2;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.data.jdbc.JDBCConformanceTestSupport;
import org.geotools.data.jdbc.JDBCDataStore;

/**
 * JDBC conformance test for h2.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class H2ConformanceTest extends JDBCConformanceTestSupport {

	protected DataSource createDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		
		dataSource.setUrl("jdbc:h2:geotools");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setPoolPreparedStatements(false);
		
		return dataSource;
	}

	protected JDBCDataStore createDataStore() {
		return new H2DataStore();
	}

}
