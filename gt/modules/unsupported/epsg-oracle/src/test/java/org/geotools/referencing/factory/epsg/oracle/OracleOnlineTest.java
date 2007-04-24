package org.geotools.referencing.factory.epsg.oracle;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.geotools.factory.JNDI;
import org.geotools.test.OnlineTestCase;

public class OracleOnlineTest extends OnlineTestCase {
    DataSource datasource;
    
    protected String getFixtureId() {
        return "local";
    }

    protected void connect() throws Exception {
        Context context = JNDI.getInitialContext( null );
        BasicDataSourceFactory factory = new BasicDataSourceFactory();
        datasource = factory.createDataSource( fixture );
    }   

    protected void disconnect() throws Exception {
        datasource = null;
    }
}
