package org.geotools.data.jdbc;

import java.sql.Types;

import junit.framework.TestCase;

import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;

/**
 * Test case for {@link JDBCTypeBuilder}.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class JDBCTypeBuilderTest extends TestCase {

	JDBCTypeBuilder typeBuilder;
	
	protected void setUp() throws Exception {
		typeBuilder = new JDBCTypeBuilder( new SimpleTypeFactoryImpl() );
	}
	
	public void test() {
		typeBuilder.setName("testType");
		typeBuilder.setNamespaceURI( "http://geotools.org/test" );
		typeBuilder.attribute( "int", Types.INTEGER );
		typeBuilder.attribute( "double", Types.DOUBLE );
		typeBuilder.attribute( "string", Types.VARCHAR );
		
		FeatureType featureType = typeBuilder.buildFeatureType();
		assertNotNull( featureType );
		assertNotNull( featureType.getAttributeType( "int" ) );
		assertEquals( Integer.class, featureType.getAttributeType( "int" ).getBinding() );
		assertNotNull( featureType.getAttributeType( "double" ) );
		assertEquals( Double.class, featureType.getAttributeType( "double" ).getBinding() );
		assertNotNull( featureType.getAttributeType( "string" ) );
		assertEquals( String.class, featureType.getAttributeType( "string" ).getBinding() );
	}
	
}
