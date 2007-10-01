package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.FeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class JDBCDataStoreTest extends JDBCTestSupport {

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
		assertFalse( ft2 == featureType );
		
		assertEquals( ft2, featureType );
		
		Connection cx = dataStore.connection();
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
	
}
