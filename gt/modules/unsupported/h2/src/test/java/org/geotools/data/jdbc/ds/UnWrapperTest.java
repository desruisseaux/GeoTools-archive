package org.geotools.data.jdbc.ds;



import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import junit.framework.TestCase;

public class UnWrapperTest extends TestCase {

   public void testDBCPUnwrapper() throws SQLException, IOException {
       BasicDataSource ds = new BasicDataSource();
       ds.setDriverClassName("org.h2.Driver");
       ds.setUrl("jdbc:h2:mem:test_mem");
       ds.setAccessToUnderlyingConnectionAllowed(true);
       
       Connection conn = ds.getConnection();
       UnWrapper uw = DataSourceFinder.getUnWrapper(conn);
       assertNotNull(uw);
       assertTrue(uw.canUnwrap(conn));
       Connection unwrapped = uw.unwrap(conn);
       assertNotNull(unwrapped);
       assertTrue(unwrapped instanceof org.h2.jdbc.JdbcConnection);
   }
}
