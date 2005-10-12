/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.postgis.referencing;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCTransactionState;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.postgis.PostgisConnectionFactory;
import org.geotools.data.postgis.PostgisTests;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import junit.framework.TestCase;

/**
 * Test PostgisAuthorityFactory
 * 
 * @author jeichar
 * @since 0.6.0
 */
public class PostgisAuthorityFactoryOnlineTest extends TestCase {
    
    
    private String TABLE_NAME="SPATIAL_REF_SYS";
    private String WKT_COLUMN="SRTEXT";
    private String SRID_COLUMN="SRID";
    
    public int getSRIDs(ConnectionPool pool) throws Exception{
        Connection dbConnection = null;

        try {
            String sqlStatement = "SELECT "+SRID_COLUMN+" FROM "+TABLE_NAME;
            dbConnection = pool.getConnection();

            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                int srid = result.getInt("srid");
                JDBCUtils.close(statement);

                return srid;
            } else {
                String mesg = "No srid column row: "+ TABLE_NAME;
                throw new DataSourceException(mesg);
            }
        } catch (SQLException sqle) {
            String message = sqle.getMessage();

            throw new DataSourceException(message, sqle);
        }finally {
            JDBCUtils.close(dbConnection, Transaction.AUTO_COMMIT, null);
        }
    }
    
    public void testCreateCRS() throws Exception{
    	PostgisTests.Fixture fx = PostgisTests.newFixture();
      PostgisConnectionFactory f = 
    	  new PostgisConnectionFactory(fx.host,fx.port.intValue(),fx.database);
      ConnectionPool pool=f.getConnectionPool(fx.user,fx.password);
      PostgisAuthorityFactory factory=new PostgisAuthorityFactory(pool);
      CoordinateReferenceSystem crs=factory.createCRS(getSRIDs(pool));
      assertNotNull(crs);
  }

}
