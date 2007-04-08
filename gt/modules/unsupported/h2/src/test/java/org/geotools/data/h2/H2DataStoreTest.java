package org.geotools.data.h2;

import java.sql.Types;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class H2DataStoreTest extends H2TestSupport {

	public void testGetTypeNames() throws Exception {
		String[] typeNames = dataStore.getTypeNames();
		
		assertEquals( 1, typeNames.length );
	}
	
	public void testGetSchema() throws Exception {
		FeatureType ft1 = dataStore.getSchema( "featureType1" );
		assertNotNull( ft1 );
		
		assertEquals( "featureType1", ft1.getTypeName() );
		assertEquals( 4, ft1.getAttributeCount() );
		
		assertEquals( Geometry.class, ft1.getAttributeType( "geometry" ).getType() );
		assertEquals( Integer.class, ft1.getAttributeType( "intProperty" ).getType() );
		assertEquals( Double.class, ft1.getAttributeType( "doubleProperty" ).getType() );
		assertEquals( String.class, ft1.getAttributeType( "stringProperty" ).getType() );
	}

	public void testCreateSchema() throws Exception {
		
		assertEquals( 1, dataStore.getTypeNames().length );
		
		//create a new feature type
		H2TypeBuilder builder = 
			new H2TypeBuilder( dataStore.getTypeFactory() );
		builder.setName( "featureType2" );
		builder.setNamespaceURI( dataStore.getNamespaceURI() );
		
		builder.attribute( "dateProperty", Types.DATE );
		builder.attribute( "boolProperty", Types.BOOLEAN );
		
		FeatureType ft2 = builder.feature();
		dataStore.createSchema( ft2 );
		
		assertEquals( 2,  dataStore.getTypeNames().length );
		
	}
	
	public void testGetFeatureReaderAll() throws Exception {
		DefaultQuery query = new DefaultQuery( "featureType1" );
		FeatureReader reader = 
			dataStore.getFeatureReader( query, Transaction.AUTO_COMMIT ); 
		
		assertNotNull( reader );
		
		for ( int i = 1; i <= 3; i++) {
			assertTrue( reader.hasNext() );
			Feature feature = reader.next();
			
			assertEquals( "" + i, feature.getID() );
			
			assertEquals( i, ((Point)(feature.getDefaultGeometry())).getX(), 0 );
			assertEquals( i, ((Point)(feature.getDefaultGeometry())).getY(), 0 );
			
			assertEquals( i, ((Point)(feature.getAttribute("geometry"))).getX(), 0 );
			assertEquals( i, ((Point)(feature.getAttribute("geometry"))).getY(), 0 );	
			
			assertEquals( i, ((Integer)feature.getAttribute("intProperty")).intValue());
			assertEquals( i + (i/10d), ((Double)feature.getAttribute("doubleProperty")).doubleValue(), 0.1 );
		
			String stringProperty = ( i == 1 ) ? "one" : ( i == 2 ) ? "two" : "three"; 
			assertEquals( stringProperty, (String)feature.getAttribute("stringProperty") );
		}
		
		assertFalse( reader.hasNext() );
		reader.close();
		
	}
}
