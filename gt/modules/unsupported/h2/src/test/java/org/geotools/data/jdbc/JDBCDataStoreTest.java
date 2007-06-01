package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;

import com.vividsolutions.jts.geom.Geometry;

public class JDBCDataStoreTest extends JDBCTestSupport {

	public void testGetNames() throws IOException {
		String[] typeNames = dataStore.getTypeNames();
		assertEquals( 1, typeNames.length );
	}
	
	public void testGetSchema() throws Exception {
		FeatureType ft1 = dataStore.getSchema( "ft1" ); 
		assertNotNull( ft1 );
		
		assertNotNull( ft1.getAttributeType("geometry") );
		assertNotNull( ft1.getAttributeType("intProperty") );
		assertNotNull( ft1.getAttributeType("doubleProperty") );
		assertNotNull( ft1.getAttributeType("stringProperty") );
		
		assertEquals( Geometry.class, ft1.getAttributeType("geometry").getType() );
		assertEquals( Integer.class, ft1.getAttributeType("intProperty").getType()  );
		assertEquals( Double.class, ft1.getAttributeType("doubleProperty").getType()  );
		assertEquals( String.class, ft1.getAttributeType("stringProperty").getType()  );
		
	}
	
	public void testCreateSchema() throws Exception {
		SimpleTypeBuilder builder = 
			new SimpleTypeBuilder( new SimpleTypeFactoryImpl() );
		builder.setName( "ft2" );
		builder.setNamespaceURI( dataStore.getNamespaceURI() );
		builder.attribute( "geometry", Geometry.class );
		builder.attribute( "intProperty", Integer.class );
		builder.attribute( "dateProperty", Date.class );
		
		FeatureType featureType = builder.feature(); 
		dataStore.createSchema( featureType );
		
		FeatureType ft2 = dataStore.getSchema( "ft2" );
		assertFalse( ft2 == featureType );
		
		assertEquals( ft2, featureType );
		
		JDBCUtils.statement(dataStore, new JDBCRunnable() {
			public Object run(Statement st) throws IOException, SQLException {
				try {
					st.executeQuery( "SELECT * from \"geotools\".\"ft2\";" );	
				}
				catch( SQLException e ) {
					fail( "table ft2 does not exist");
				}
				
				return null;
			}
		});
	}
	
	public void testGetFeatureSource() throws Exception {
		FeatureSource featureSource = dataStore.getFeatureSource( "ft1" );
		assertNotNull( featureSource );
	}
	
	
}
