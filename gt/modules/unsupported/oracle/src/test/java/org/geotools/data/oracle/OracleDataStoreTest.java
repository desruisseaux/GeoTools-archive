/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *    
 */
package org.geotools.data.oracle;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.fidmapper.MaxIncFIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.geometry.jts.ReferencedEnvelope;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author geoghegs
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * @source $URL$
 */
public class OracleDataStoreTest extends TestCase {
    private ConnectionPool cPool;
    private FilterFactory filterFactory = FilterFactoryFinder.createFilterFactory();
    private Properties properties;
    private GeometryFactory jtsFactory = new GeometryFactory();
    private String schemaName;
    private OracleDataStore dstore;
    private Connection conn;
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("remote.properties"));
        schemaName = properties.getProperty("schema");
        OracleConnectionFactory fact = new OracleConnectionFactory(properties.getProperty("host"), 
                properties.getProperty("port"), properties.getProperty("instance"));
        fact.setLogin(properties.getProperty("user"), properties.getProperty("passwd"));        
        try {
        	cPool = fact.getConnectionPool();                    
            conn = cPool.getConnection();
        } catch( Throwable t ){
        	System.out.println("Warning: could not connect, configure "+getClass().getResource("test.properties"));
        	return;
        }
        System.out.println(conn.getTypeMap());
        reset();
        dstore = new OracleDataStore(cPool, properties.getProperty("schema"), new HashMap());
        dstore.setFIDMapper("ORA_TEST_POINTS", new TypedFIDMapper(new MaxIncFIDMapper("ORA_TEST_POINTS", "ID", Types.INTEGER), "ORA_TEST_POINTS"));
        dstore.setFIDMapper("ORA_TEST_LINES", new TypedFIDMapper(new MaxIncFIDMapper("ORA_TEST_LINES", "ID", Types.INTEGER), "ORA_TEST_LINES"));
        dstore.setFIDMapper("RA_TEST_POLYGONS", new TypedFIDMapper(new MaxIncFIDMapper("ORA_TEST_POLYGONS", "ID", Types.INTEGER), "ORA_TEST_POLYGONS"));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {    	
    	//reset();
        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();
        manager.closeAll();    	
    }
    static boolean first  = false;
    
    protected void reset() throws Exception {
    	if( conn == null ) return;
    	Statement st = conn.createStatement();
    	
    	if( !first ){
    		try {
    			st.execute("DROP TABLE ora_test_points");
    			st.executeUpdate("DELETE FROM user_sdo_geom_metadata WHERE TABLE_NAME='ORA_TEST_POINTS'");    	    	    			
    		}
    		catch( SQLException noPrevRun){}
    		first = true;
    	}
    	try {
			if( st.executeQuery("SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME = 'ORA_TEST_POINTS'").next()){
				try {
		    		st.executeUpdate("DELETE FROM ORA_TEST_POINTS");
		    	}
		    	catch (SQLException fine){
		    		// must be the first time?
		    		fine.printStackTrace();
		    	}    					
			}
			else {
				st.execute( "CREATE TABLE ORA_TEST_POINTS ("+
						    "       NAME VARCHAR2(15),"+
						    "       INTVAL NUMBER(12,0),"+"" +
						    "       ID NUMBER(3,0) PRIMARY KEY,"+
						    "       SHAPE MDSYS.SDO_GEOMETRY )");
		    	st.execute("INSERT INTO user_sdo_geom_metadata (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID )"+
					    "VALUES('ORA_TEST_POINTS','SHAPE',"+
					        "MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X',0,-20,0.5),MDSYS.SDO_DIM_ELEMENT('Y',0,10,0.5)),"+
					    "NULL)");
//		    	 st.execute("DROP INDEX GEOTOOLS.ORA_TEST_POINTS_SHAPE_IDX");		    	
		    	st.execute("CREATE INDEX GEOTOOLS.ORA_TEST_POINTS_SHAPE_IDX "+
		    				"ON GEOTOOLS.ORA_TEST_POINTS (SHAPE) INDEXTYPE IS " +
		    				"MDSYS.SPATIAL_INDEX PARAMETERS (' SDO_INDX_DIMS=2 LAYER_GTYPE=\"COLLECTION\"') ");		    			    	
			}
    	}
    	catch (SQLException fine){
    		fine.printStackTrace();
    	}  	
    	//                   + (20,30)
    	//         
    	//  +(20,10)         +(10,10)   + (20,10)   +(30,10)
    	//        
    	st.execute("INSERT INTO ORA_TEST_POINTS VALUES ('point 1',10,1," +
                "MDSYS.SDO_GEOMETRY(2001,NULL," +
                "MDSYS.SDO_POINT_TYPE(10.0, 10.0, NULL),"+
                "NULL,NULL))");
    	st.execute("INSERT INTO ORA_TEST_POINTS VALUES ('point 2',20,2," +
                        "MDSYS.SDO_GEOMETRY(2001,NULL," +
                        "MDSYS.SDO_POINT_TYPE(20.0, 10.0, NULL),"+
                        "NULL,NULL))");
    	st.execute("INSERT INTO ORA_TEST_POINTS VALUES ('point 3',30,3," +
                        "MDSYS.SDO_GEOMETRY(2001,NULL," +
                        "SDO_POINT_TYPE(20.0, 30.0, NULL),"+
                        "NULL,NULL))");
    	st.execute("INSERT INTO ORA_TEST_POINTS VALUES ('point 4',40,4," +
                        "MDSYS.SDO_GEOMETRY(2001,NULL," +
                        "SDO_POINT_TYPE(30.0, 10.0, NULL),"+
                        "NULL,NULL))");
    	st.execute("INSERT INTO ORA_TEST_POINTS VALUES ('point 5',50,5," +
                        "MDSYS.SDO_GEOMETRY(2001,NULL," +
                        "SDO_POINT_TYPE(-20.0, 10.0, NULL),"+
                        "NULL,NULL))");

    	
    }

    public void testGetFeatureTypes() throws IOException {
    	if( conn == null ) return;
    	
        String[] fts = dstore.getTypeNames();
        List list = Arrays.asList( fts );
        
        System.out.println( list );
        assertTrue( list.contains("ORA_TEST_POINTS"));        
    }

    public void testGetSchema() throws Exception {
    	if( conn == null ) return;    	
            FeatureType ft = dstore.getSchema("ORA_TEST_POINTS");
            assertNotNull(ft);
            System.out.println(ft);
    }

    public void testGetFeatureReader() throws Exception {
    	if( conn == null ) return;    	
            FeatureType ft = dstore.getSchema("ORA_TEST_POINTS");
            Query q = new DefaultQuery( "ORA_TEST_POINTS" );
            FeatureReader fr = dstore.getFeatureReader( q, Transaction.AUTO_COMMIT);
            assertEquals( ft, fr.getFeatureType() );
            int count = 0;

            while (fr.hasNext()) {
                fr.next();
                count++;
            }
            assertEquals(5, count);

            fr.close();
    }

    public void testGetFeatureWriter() throws Exception {
    	if( conn == null ) return;    	
        FeatureWriter writer = dstore.getFeatureWriter("ORA_TEST_POINTS", Filter.INCLUDE, Transaction.AUTO_COMMIT);
        assertNotNull(writer);

        Feature feature = writer.next();
        System.out.println(feature);
        feature.setAttribute(0, "Changed Feature");
        System.out.println(feature);
        writer.write();
        writer.close();

        Query q = new DefaultQuery( "ORA_TEST_POINTS" );
        FeatureReader reader = dstore.getFeatureReader( q, Transaction.AUTO_COMMIT);
        Feature readF = reader.next();
        
        assertEquals("Changed Feature", feature.getAttribute(0));
        assertEquals(feature.getID(), readF.getID());
        assertEquals(feature.getAttribute(0), readF.getAttribute((0)));
        assertEquals(feature.getAttribute(1), readF.getAttribute((1)));
        // assertTrue(feature.getAttribute(2).equals(readF.getAttribute((2))));  JTS doesnt override equals.  POS
        
        reader.close();
    }
    
    public void testMaxFeatures() throws Exception {
    	if( conn == null ) return;    	
        DefaultQuery query = new DefaultQuery();
        query.setTypeName("ORA_TEST_POINTS");
        query.setMaxFeatures(3);
        FeatureSource fs = dstore.getFeatureSource("ORA_TEST_POINTS");        
        FeatureCollection fr = fs.getFeatures(query);
        assertEquals(3, fr.size());
    }

    public void testLikeGetFeatures() throws Exception {
    	if( conn == null ) return;    	
        LikeFilter likeFilter = filterFactory.createLikeFilter();
        Expression pattern = filterFactory.createLiteralExpression("*");
        Expression attr = filterFactory.createAttributeExpression(null, "NAME");
        likeFilter.setPattern(pattern, "*", "?", "\\"); // '*' --> '%' 
        likeFilter.setValue(attr);
        
        FeatureSource fs = dstore.getFeatureSource("ORA_TEST_POINTS");
        FeatureCollection fr = fs.getFeatures(likeFilter);
        assertEquals(5, fr.size());
        
        pattern = filterFactory.createLiteralExpression("*5");
        likeFilter.setPattern(pattern, "*", "?", "\\");
        fr = fs.getFeatures(likeFilter);
        assertEquals(1, fr.size());
    }
    
    public void testAttributeFilter() throws Exception {
    	if( conn == null ) return;    	
        CompareFilter attributeEquality = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression attribute = filterFactory.createAttributeExpression(dstore.getSchema("ORA_TEST_POINTS"), "NAME");
        Expression literal = filterFactory.createLiteralExpression("point 1");
        attributeEquality.addLeftValue(attribute);
        attributeEquality.addRightValue(literal);
        
        FeatureSource fs = dstore.getFeatureSource("ORA_TEST_POINTS");
        FeatureCollection fr = fs.getFeatures(attributeEquality);
        assertEquals(1, fr.size());
        
        FeatureCollection fc = fr;
        assertEquals(1, fc.size());
        Feature f = fc.features().next();
        assertEquals("point 1", f.getAttribute("NAME"));        
    }
    
    public void testBBoxFilter() throws Exception {
    	if( conn == null ) return;    	
        GeometryFilter filter = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        Expression right = filterFactory.createBBoxExpression(new Envelope(-180, 180, -90, 90));
        Expression left = filterFactory.createAttributeExpression(dstore.getSchema("ORA_TEST_POINTS"), "SHAPE");
        filter.addLeftGeometry(left);
        filter.addRightGeometry(right);
        
        FeatureSource fs = dstore.getFeatureSource("ORA_TEST_POINTS");
        FeatureCollection fr = fs.getFeatures(filter);        
        assertEquals(5, fr.size()); // we pass this!
        
    	//                   + (20,30)
    	//                            +----------------------+
    	//  +(20,10)         +(10,10) | + (20,10)   +(30,10) |
    	//                            +----------------------+
        right = filterFactory.createBBoxExpression(new Envelope(15, 35, 0, 15));        
        filter.addRightGeometry(right);
        fr = fs.getFeatures(filter);
        assertEquals(2, fr.size()); // we have 4!
    }
    
    public void testPointGeometryConversion() throws Exception {
    	if( conn == null ) return;    	
        CompareFilter filter = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression left = filterFactory.createAttributeExpression(dstore.getSchema("ORA_TEST_POINTS"), "NAME");
        Expression right = filterFactory.createLiteralExpression("point 1");
        filter.addLeftValue(left);
        filter.addRightValue(right);
        
        
        FeatureSource fs = dstore.getFeatureSource("ORA_TEST_POINTS");
        FeatureCollection fr = fs.getFeatures(filter);        
        assertEquals(1, fr.size());
        
        Feature feature = (Feature) fr.iterator().next();
        Geometry geom = feature.getDefaultGeometry();
        assertEquals(Point.class.getName(), geom.getClass().getName());
        Point point = (Point) geom;
        assertEquals(10.0, point.getX(), 0.001);
        assertEquals(10.0, point.getY(), 0.001);
    }
    
    public void testAddFeatures() throws Exception {
    	if( conn == null ) return;    	
        Map fidGen = new HashMap();
        fidGen.put("ORA_TEST_POINTS", JDBCDataStoreConfig.FID_GEN_MANUAL_INC);
        JDBCDataStoreConfig config = JDBCDataStoreConfig.createWithSchemaNameAndFIDGenMap(schemaName, fidGen);
        
        String name = "add_name";
        BigDecimal intval = new BigDecimal(70);
        Point point = jtsFactory.createPoint(new Coordinate(-15.0, -25));
        Feature feature = dstore.getSchema("ORA_TEST_POINTS").create(new Object[] { name, intval, point });
        FeatureStore fs = (FeatureStore) dstore.getFeatureSource("ORA_TEST_POINTS");
        fs.addFeatures(DataUtilities.collection(feature));

        // Select is directly from the DB
        Connection conn = cPool.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM ORA_TEST_POINTS WHERE NAME = 'add_name'");

        if (rs.next()) {
            assertEquals(rs.getString("NAME"), name);
            assertEquals(70, rs.getInt("INTVAL"));            
        } else {
            fail("Feature was not added correctly");
        }
    }
    
    public void testRemoveFeatures() throws Exception {
    	if( conn == null ) return;    	
        FeatureStore fs = (FeatureStore) dstore.getFeatureSource("ORA_TEST_POINTS");
        fs.removeFeatures(Filter.INCLUDE);
        FeatureCollection fr = fs.getFeatures();
        assertEquals(0, fr.size());
    }
    
    public void testPropertySelect() throws Exception {
    	if( conn == null ) return;    	
        DefaultQuery q = new DefaultQuery("ORA_TEST_POINTS",Filter.INCLUDE);
        q.setPropertyNames(new String[]{"NAME"});
        FeatureReader fr = dstore.getFeatureReader(q, Transaction.AUTO_COMMIT);
        Feature f = fr.next();
        FeatureType ft = f.getFeatureType();
        assertEquals(1, ft.getAttributeCount());
        assertEquals("NAME", ft.getAttributeType(0).getName());        
    }
    public void testBounds(){
    	if( conn == null ) return;    	    	
    	Envelope extent = dstore.getEnvelope("ORA_TEST_POINTS");
    	assertNotNull( extent );
    	assertTrue( extent instanceof ReferencedEnvelope );
    	ReferencedEnvelope envelope = (ReferencedEnvelope) extent;
    	assertFalse( envelope.isNull() );
    }
}
