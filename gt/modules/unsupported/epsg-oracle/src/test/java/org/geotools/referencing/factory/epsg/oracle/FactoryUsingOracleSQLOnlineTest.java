package org.geotools.referencing.factory.epsg.oracle;

import java.sql.Connection;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.factory.JNDI;
import org.geotools.referencing.CRS;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.factory.epsg.FactoryOnOracleSQL;
import org.geotools.referencing.factory.epsg.FactoryUsingOracleSQL;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.GeodeticDatum;

/**
 * This class tests the Factory<b>Using</b>OracleSQL - ie the thing that does work!
 * <p>
 * No cache or buffer was harmed in the making of these tests.
 *  
 * @author Jody
 */
public class FactoryUsingOracleSQLOnlineTest extends OracleOnlineTestCase {

    public void testDatumCreation() throws Exception {
        Connection connection = datasource.getConnection();
        try{
            Hints hints = new Hints(Hints.EPSG_DATA_SOURCE, "jdbc/EPSG");
        
            FactoryUsingOracleSQL oracle = new FactoryUsingOracleSQL(hints, connection);
    
            GeodeticDatum datum = oracle.createGeodeticDatum("6326");
            assertNotNull(datum);
        }
        finally {
            connection.close();
        }
    }
    
    public void testCRSCreation() throws Exception {
        Connection connection = datasource.getConnection();
        try{
            Hints hints = new Hints(Hints.EPSG_DATA_SOURCE, "jdbc/EPSG");
            FactoryUsingOracleSQL oracle = new FactoryUsingOracleSQL(hints, connection );
            
            CoordinateReferenceSystem crs = oracle.createCoordinateReferenceSystem("4326");
            assertNotNull(crs);
        }
        finally {
            connection.close();
        }
    }

}
