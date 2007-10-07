package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;

import com.vividsolutions.jts.geom.Geometry;

public class JDBCDataStoreTest extends JDBCTestSupport {

    protected void setUp() throws Exception {
        super.setUp();
        
        try {
            run( "DROP TABLE \"geotools\".\"ft2\";" );
        }
        catch( Exception e ) {}
    }
    
	public void testGetNames() throws IOException {
		String[] typeNames = dataStore.getTypeNames();
		assertEquals( 1, typeNames.length );
	}
	
	public void testGetSchema() throws Exception {
		SimpleFeatureType ft1 = dataStore.getSchema( "ft1" ); 
		assertNotNull( ft1 );
		
		assertNotNull( ft1.getAttribute("geometry") );
		assertNotNull( ft1.getAttribute("intProperty") );
		assertNotNull( ft1.getAttribute("doubleProperty") );
		assertNotNull( ft1.getAttribute("stringProperty") );
		
		assertEquals( Geometry.class, ft1.getAttribute("geometry").getType().getBinding() );
		assertEquals( Integer.class, ft1.getAttribute("intProperty").getType().getBinding()  );
		assertEquals( Double.class, ft1.getAttribute("doubleProperty").getType().getBinding()  );
		assertEquals( String.class, ft1.getAttribute("stringProperty").getType().getBinding()  );
		
	}
	
	public void testCreateSchema() throws Exception {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder( );
		builder.setName( "ft2" );
		builder.setNamespaceURI( dataStore.getNamespaceURI() );
		builder.add( "geometry", Geometry.class );
		builder.add( "intProperty", Integer.class );
		builder.add( "dateProperty", Date.class );
		
		SimpleFeatureType featureType = builder.buildFeatureType(); 
		dataStore.createSchema( featureType );
		
		SimpleFeatureType ft2 = dataStore.getSchema( "ft2" );
		assertEquals( ft2, featureType );
		
		Connection cx = dataStore.createConnection();
		Statement st = cx.createStatement();
          try {
              st.executeQuery( "SELECT * from \"geotools\".\"ft2\";" );   
          }
          catch( SQLException e ) {
              fail( "table ft2 does not exist");
          }
          
		st.close();
	}
	
	public void testGetFeatureSource() throws Exception {
		FeatureSource featureSource = dataStore.getFeatureSource( "ft1" );
		assertNotNull( featureSource );
	}
	
	public void testGetFeatureReader() throws Exception {
	    DefaultQuery query = new DefaultQuery("ft1");
	    FeatureReader reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
	    
	    for ( int i = 0; i < 3; i++ ) {
	        assertTrue( reader.hasNext() );
	        SimpleFeature feature = reader.next();
	        assertNotNull( feature );
	        assertEquals( 4, feature.getAttributeCount() );
	    }
	    assertFalse( reader.hasNext() );
	    reader.close();
	    
	    query.setPropertyNames(new String[]{"intProperty"});
	    reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
	    for ( int i = 0; i < 3; i++ ) {
            assertTrue( reader.hasNext() );
            SimpleFeature feature = reader.next();
            assertEquals( 1, feature.getAttributeCount() );
        }
	    assertFalse( reader.hasNext() );
	    reader.close();
	    
	    FilterFactory ff = dataStore.getFilterFactory();
	    Filter f = ff.equals( ff.property("intProperty"), ff.literal(1) );
	    query.setFilter( f );
	    
	    reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
        for ( int i = 0; i < 1; i++ ) {
            assertTrue( reader.hasNext() );
            SimpleFeature feature = reader.next();
        }
        assertFalse( reader.hasNext() );
        reader.close();
    }
	
	public void testGetFeatureWriter() throws IOException {
	    FeatureWriter writer = dataStore.getFeatureWriter("ft1", Transaction.AUTO_COMMIT);
	    while ( writer.hasNext() ) {
	        SimpleFeature feature = writer.next();
	        feature.setAttribute( "intProperty", new Integer( 100 ) );
	    }
	    
	    writer.close();
	    
	    DefaultQuery query = new DefaultQuery("ft1");
        FeatureReader reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
        assertTrue( reader.hasNext() );
        while( reader.hasNext() ) {
            SimpleFeature feature = reader.next();
            assertEquals( new Integer( 100 ), feature.getAttribute("intProperty") );
        }
        reader.close();
	}
	
	public void testGetFeatureWriterWithFilter() throws IOException {
	    FilterFactory ff = dataStore.getFilterFactory();
	    
	    Filter f = ff.equals( ff.property( "intProperty" ), ff.literal( 100 ) );
	    FeatureCollection features = dataStore.getFeatureSource("ft1").getFeatures( f );
	    assertEquals( 0, features.size() );
	    
	    f = ff.equals( ff.property( "intProperty" ), ff.literal( 1 ) );
	    
	    FeatureWriter writer = dataStore.getFeatureWriter("ft1", f, Transaction.AUTO_COMMIT);
        while ( writer.hasNext() ) {
            SimpleFeature feature = writer.next();
            feature.setAttribute( "intProperty", new Integer( 100 ) );
        }
        writer.close();
        
        f = ff.equals( ff.property( "intProperty" ), ff.literal( 100 ) );
        features = dataStore.getFeatureSource("ft1").getFeatures( f );
        assertEquals( 1, features.size() );
        
	}
	
	public void testGetFeatureWriterAppend() throws IOException {
	    FeatureWriter writer = dataStore.getFeatureWriterAppend("ft1", Transaction.AUTO_COMMIT);
        for ( int i = 3; i < 6; i++ ) { 
            SimpleFeature feature = writer.next();
            feature.setAttribute( "intProperty", new Integer( i ) );
        }
        writer.close();
        
        FeatureCollection features = dataStore.getFeatureSource("ft1").getFeatures();
        assertEquals( 6, features.size() );
	}
}
