package org.geotools.feature.type;

import javax.xml.namespace.QName;

import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;

import junit.framework.TestCase;

public class GeometryTypeImplTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.geotools.feature.type.GeometryTypeImpl.getCRS()'
	 */
	public void testGetCRS() {
		TypeFactory tf = new TypeFactoryImpl();
		GeometryType<?> type;
		
		type =(GeometryType)tf.createType("test", LineString.class);
		assertNull(type.getCRS());
		
		CoordinateReferenceSystem crs = new CoordinateReferenceSystem(){};
		type = tf.createGeometryType(new QName("test"), LineString.class, false, crs);
		assertSame(crs, type.getCRS());
	}

}
