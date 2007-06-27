package org.geotools.geometry.iso.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.geometry.iso.PositionFactoryImpl;
import org.geotools.geometry.iso.PrecisionModel;
import org.geotools.geometry.iso.coordinate.GeometryFactoryImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.geotools.referencing.CRS;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.geometry.coordinate.LineString;
import org.opengis.geometry.coordinate.Position;
import org.opengis.geometry.primitive.Curve;
import org.opengis.geometry.primitive.CurveSegment;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import junit.framework.TestCase;

public class TransformTest extends TestCase {
	
	private CoordinateReferenceSystem crs1;
	private CoordinateReferenceSystem crs2;
	private CoordinateReferenceSystem crs3;
	
	public void setUp() throws Exception {
		this.crs1 = CRS.decode( "EPSG:4326");
		this.crs2 = CRS.decode( "EPSG:32201");
		this.crs3 = CRS.decode( "EPSG:3005");
	}

	public void _testPoint() throws Exception {

		PositionFactory positionFactory = new PositionFactoryImpl(crs1, new PrecisionModel());
		PrimitiveFactory primitiveFactory = new PrimitiveFactoryImpl(crs1, positionFactory);
		
		Point point1 = primitiveFactory.createPoint( new double[]{1.5,1.5} );
		Point point2 = (Point) point1.transform(crs2);
		Point point3 = (Point) point1.transform(crs3);
		
		System.out.println(point1);
		System.out.println(point2);
		System.out.println(point3);
	}
	
	public void _testCurve() throws Exception {

		PositionFactory positionFactory = new PositionFactoryImpl(crs1, new PrecisionModel());
		PrimitiveFactory primitiveFactory = new PrimitiveFactoryImpl(crs1, positionFactory);
		GeometryFactory geometryFactory = new GeometryFactoryImpl(crs1, positionFactory);
		
		List<Position> points = new ArrayList<Position>();
		points.add(primitiveFactory.createPoint( new double[]{1.5,1.5} ));
		points.add(primitiveFactory.createPoint( new double[]{2,2} ));
		points.add(primitiveFactory.createPoint( new double[]{2.5,2.5} ));
		points.add(primitiveFactory.createPoint( new double[]{3,3} ));
        LineString lineString = geometryFactory.createLineString(points);
        List curveSegmentList = Collections.singletonList(lineString);
        
        Curve curve1 = primitiveFactory.createCurve(curveSegmentList);
        Curve curve2 = (Curve) curve1.transform(crs2);
        Curve curve3 = (Curve) curve1.transform(crs3);
		
		System.out.println(curve1);
		System.out.println(curve2);
		System.out.println(curve3);
	}
}
