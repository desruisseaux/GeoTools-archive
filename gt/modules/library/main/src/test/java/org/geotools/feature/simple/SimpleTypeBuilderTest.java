package org.geotools.feature.simple;

import java.util.Collections;
import java.util.List;

import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Name;
import org.geotools.feature.type.SchemaImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.geotools.feature.type.TypeName;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Schema;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import junit.framework.TestCase;

public class SimpleTypeBuilderTest extends TestCase {

	static final String URI = "gopher://localhost/test";
	
	SimpleTypeBuilder builder;
	
	protected void setUp() throws Exception {
		Schema schema = new SchemaImpl( "test" );
		
		TypeFactoryImpl typeFactory = new TypeFactoryImpl();
		AttributeType pointType = 
			typeFactory.createGeometryType( new TypeName( "test", "pointType" ), Point.class, null, false, false, Collections.EMPTY_SET, null, null);		
		schema.put( new Name( "test", "pointType" ), pointType );
		
		AttributeType intType = 
			typeFactory.createAttributeType( new TypeName( "test", "intType" ), Integer.class, false, false, Collections.EMPTY_SET, null, null);
		schema.put( new Name( "test", "intType" ), intType );
		
		builder = new SimpleTypeBuilder( new SimpleTypeFactoryImpl() );
		builder.setBindings(schema);
	}
	
	public void testSanity() {
		builder.setName( "testName" );
		builder.setNamespaceURI( "testNamespaceURI" );
		builder.add( "point", Point.class, null );
		builder.add( "integer", Integer.class );
		
		SimpleFeatureType type = builder.buildFeatureType();
		assertNotNull( type );
		
		assertEquals( 2, type.getAttributeCount() );
		
		AttributeType t = type.getType( "point" );
		assertNotNull( t );
		assertEquals( Point.class, t.getBinding() );
		
		t = type.getType( "integer" );
		assertNotNull( t );
		assertEquals( Integer.class, t.getBinding() );
		
		t = type.getDefaultGeometryType();
		assertNotNull( t );
		assertEquals( Point.class, t.getBinding() );
	}
	
	public void testCRS() {
		builder.setName( "testName" );
		builder.setNamespaceURI( "testNamespaceURI" );
		
		builder.setCRS(DefaultGeographicCRS.WGS84);
		builder.add( "point", Point.class );
		builder.add( "point2", Point.class, DefaultGeographicCRS.WGS84 );
		builder.setDefaultGeometry("point");
		SimpleFeatureType type = builder.buildFeatureType();
		assertEquals( DefaultGeographicCRS.WGS84, type.getCRS() );
		
		assertNull( type.getDefaultGeometryType().getCRS() );
		assertEquals( DefaultGeographicCRS.WGS84, ((GeometryType)type.getType("point2")).getCRS());
	}
}
