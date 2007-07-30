package org.geotools.geometry.iso.primitive;

import org.geotools.geometry.GeometryBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import junit.framework.TestCase;

public class PrimitiveGeometryBuilderTest extends TestCase {

	CoordinateReferenceSystem crs_WGS84;
	GeometryBuilder builder;
	
	public void setUp() {
		crs_WGS84 = DefaultGeographicCRS.WGS84;
		builder = new GeometryBuilder(crs_WGS84); 
	}
	
	public void testBuildPoint() {
		
		// test positionfactory
		PositionFactory posFactory = builder.getPositionFactory();
		DirectPosition position = posFactory.createDirectPosition(new double[] { 48.44, -123.37 });
		System.out.println(position);
		
		// test primitivefactory
		PrimitiveFactory primitiveFactory = builder.getPrimitiveFactory();
		System.out.println(primitiveFactory);
		System.out.println(primitiveFactory.getCoordinateReferenceSystem());
		primitiveFactory.createPoint(new double[] { 48.44, -123.37 });
	}
}
