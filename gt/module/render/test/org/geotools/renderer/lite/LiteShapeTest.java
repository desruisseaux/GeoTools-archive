/*
 * RenderStyleTest.java
 *
 * Created on 27 May 2002, 15:40
 */

package org.geotools.renderer.lite;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * 
 * @author jamesm,iant
 * @source $URL$
 */
public class LiteShapeTest extends TestCase {
	private java.net.URL base = getClass().getResource("testData/");

	public LiteShapeTest(java.lang.String testName) {
		super(testName);

	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(LiteShapeTest.class);
		return suite;
	}

	public void testLineShape() throws TransformException, FactoryException {
		GeometryFactory geomFac = new GeometryFactory();
		LineString lineString = makeSampleLineString(geomFac, 0, 0);
		LiteShape2 lineShape = new LiteShape2(lineString, ProjectiveTransform
				.create(new AffineTransform()), new Decimator(
				ProjectiveTransform.create(new AffineTransform())), false);

		assertFalse(lineShape.contains(0, 0));
		assertTrue(lineShape.contains(60, 60));
		assertFalse(lineShape.contains(50, 50, 10, 10));
		assertTrue(lineShape.contains(new java.awt.Point(60, 60)));
		assertFalse(lineShape.contains(new java.awt.geom.Rectangle2D.Float(50,
				50, 10, 10)));
		assertTrue(lineShape.getBounds2D().equals(
				new Rectangle2D.Double(50, 50, 80, 250)));
		assertTrue(lineShape.getBounds().equals(
				new java.awt.Rectangle(50, 50, 80, 250)));
		assertTrue(lineShape.intersects(0, 0, 100, 100));
		assertTrue(lineShape.intersects(new Rectangle2D.Double(0, 0, 100, 100)));
		assertFalse(lineShape.intersects(55, 55, 3, 100));
		assertFalse(lineShape
				.intersects(new Rectangle2D.Double(55, 55, 3, 100)));
	}

	public void testPolygonShape() throws TransformException, FactoryException {
		GeometryFactory geomFac = new GeometryFactory();
		Polygon polygon = makeSamplePolygon(geomFac, 0, 0);
		LiteShape2 lineShape = new LiteShape2(polygon, ProjectiveTransform
				.create(new AffineTransform()), new Decimator(
				ProjectiveTransform.create(new AffineTransform())), false);

		assertFalse(lineShape.contains(0, 0));
		assertTrue(lineShape.contains(100, 100));
		assertFalse(lineShape.contains(50, 50, 10, 10));
		assertTrue(lineShape.contains(100, 100, 10, 10));
		assertTrue(lineShape.contains(new java.awt.Point(70, 90)));
		assertFalse(lineShape.contains(new java.awt.geom.Rectangle2D.Float(50,
				50, 10, 10)));
		assertTrue(lineShape.getBounds2D().equals(
				new Rectangle2D.Double(60, 70, 70, 50)));
		assertTrue(lineShape.getBounds().equals(
				new java.awt.Rectangle(60, 70, 70, 50)));
		assertTrue(lineShape.intersects(0, 0, 100, 100));
		assertTrue(lineShape.intersects(new Rectangle2D.Double(0, 0, 100, 100)));
		assertFalse(lineShape.intersects(55, 55, 3, 100));
		assertFalse(lineShape
				.intersects(new Rectangle2D.Double(55, 55, 3, 100)));
	}

	private LineString makeSampleLineString(final GeometryFactory geomFac,
			double xoff, double yoff) {
		Coordinate[] linestringCoordinates = new Coordinate[8];
		linestringCoordinates[0] = new Coordinate(50.0d + xoff, 50.0d + yoff);
		linestringCoordinates[1] = new Coordinate(60.0d + xoff, 50.0d + yoff);
		linestringCoordinates[2] = new Coordinate(60.0d + xoff, 60.0d + yoff);
		linestringCoordinates[3] = new Coordinate(70.0d + xoff, 60.0d + yoff);
		linestringCoordinates[4] = new Coordinate(70.0d + xoff, 70.0d + yoff);
		linestringCoordinates[5] = new Coordinate(80.0d + xoff, 70.0d + yoff);
		linestringCoordinates[6] = new Coordinate(80.0d + xoff, 80.0d + yoff);
		linestringCoordinates[7] = new Coordinate(130.0d + xoff, 300.0d + yoff);
		LineString line = geomFac.createLineString(linestringCoordinates);

		return line;
	}

	private com.vividsolutions.jts.geom.Polygon makeSamplePolygon(
			final GeometryFactory geomFac, double xoff, double yoff) {
		Coordinate[] polygonCoordinates = new Coordinate[10];
		polygonCoordinates[0] = new Coordinate(70 + xoff, 70 + yoff);
		polygonCoordinates[1] = new Coordinate(60 + xoff, 90 + yoff);
		polygonCoordinates[2] = new Coordinate(60 + xoff, 110 + yoff);
		polygonCoordinates[3] = new Coordinate(70 + xoff, 120 + yoff);
		polygonCoordinates[4] = new Coordinate(90 + xoff, 110 + yoff);
		polygonCoordinates[5] = new Coordinate(110 + xoff, 120 + yoff);
		polygonCoordinates[6] = new Coordinate(130 + xoff, 110 + yoff);
		polygonCoordinates[7] = new Coordinate(130 + xoff, 90 + yoff);
		polygonCoordinates[8] = new Coordinate(110 + xoff, 70 + yoff);
		polygonCoordinates[9] = new Coordinate(70 + xoff, 70 + yoff);
		try {
			LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
			com.vividsolutions.jts.geom.Polygon polyg = geomFac.createPolygon(
					ring, null);
			return polyg;
		} catch (TopologyException te) {
			fail("Error creating sample polygon for testing " + te);
		}
		return null;
	}
}
