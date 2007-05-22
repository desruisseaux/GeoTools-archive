package org.geotools.referencing.factory.epsg.oracle;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

import org.geotools.factory.JNDI;
import org.geotools.referencing.CRS;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.factory.epsg.FactoryOnOracleSQL;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.GeodeticDatum;

public class EPSGOracleOnlineTest extends OracleOnlineTestCase {

    public void testWSG84() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        assertNotNull(crs);
    }
    public void testTestTEST() throws Exception {
        Context context = JNDI.getInitialContext(null);
        DataSource source = (DataSource) context.lookup("jdbc/EPSG");
        assertNotNull(source);
        assertSame(source, this.datasource);
    }
    /**
     * It is a little hard to test this thing, the DefaultAuthorityFactory holds a field "buffered"
     * that is an AbstractAuthorityFactory which in turn is an FactoryUsing
     * 
     * @throws Exception
     */
    public void testNakedAuthorityFactory() throws Exception {
        FactoryOnOracleSQL oracle = new FactoryOnOracleSQL();

        
        CoordinateReferenceSystem crs = oracle.createCoordinateReferenceSystem("4326");
        assertNotNull(crs);
    }
    
    public void testDatumCreation() throws Exception {
        FactoryOnOracleSQL oracle = new FactoryOnOracleSQL();
                
        GeodeticDatum datum = oracle.createGeodeticDatum("6326");
        assertNotNull( datum );
    }
}
