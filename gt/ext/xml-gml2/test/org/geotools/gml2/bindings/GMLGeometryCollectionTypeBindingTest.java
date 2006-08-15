package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GMLGeometryCollectionTypeBindingTest extends AbstractGMLBindingTest {

	ElementInstance gcol,point1,point2,line1,ring1,poly1;
	GeometryFactory gf;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		point1 = createElement(GML.NAMESPACE,"myPoint",GML.POINTMEMBERTYPE,null);
		point2 = createElement(GML.NAMESPACE,"myPoint",GML.POINTMEMBERTYPE,null);
		line1 = createElement(GML.NAMESPACE,"myLine",GML.LINESTRINGMEMBERTYPE,null);
		ring1 = createElement(GML.NAMESPACE,"myLine",GML.LINEARRINGMEMBERTYPE,null);
		poly1 = createElement(GML.NAMESPACE,"myPoly",GML.POLYGONMEMBERTYPE,null);
		gcol = createElement(GML.NAMESPACE,"myColl",GML.GEOMETRYCOLLECTIONTYPE,null);
		gf = new GeometryFactory();
		
		container = new DefaultPicoContainer();
		container.registerComponentImplementation(GeometryFactory.class);
		container.registerComponentImplementation(GMLGeometryCollectionTypeBinding.class);
	}
	
	public void testHomogeneous() throws Exception {
		Node node = createNode(gcol,
			new ElementInstance[]{point1,point2}, new Object[]{
				gf.createPoint(new Coordinate(0,0)),
				gf.createPoint(new Coordinate(1,1))
			},null,null	
		);
		
		GMLGeometryCollectionTypeBinding s = 
			(GMLGeometryCollectionTypeBinding)container.getComponentInstanceOfType(GMLGeometryCollectionTypeBinding.class);
		
		GeometryCollection gc = (GeometryCollection)s.parse(gcol,node,null);
		assertNotNull(gc);
		assertEquals(gc.getNumGeometries(),2);
		assertTrue(gc.getGeometryN(0) instanceof Point);
		assertTrue(gc.getGeometryN(1) instanceof Point);
		assertEquals(((Point)gc.getGeometryN(0)).getX(),0d,0d);
		assertEquals(((Point)gc.getGeometryN(0)).getY(),0d,0d);
		assertEquals(((Point)gc.getGeometryN(1)).getX(),1d,0d);
		assertEquals(((Point)gc.getGeometryN(1)).getY(),1d,0d);
	}
	
	public void testHeterogeneous() throws Exception {
		
			Node node = createNode(gcol,
				new ElementInstance[]{point1,point2,line1,ring1,poly1}, new Object[]{
					gf.createPoint(new Coordinate(0,0)),
					gf.createPoint(new Coordinate(1,1)),
					gf.createLineString(new Coordinate[]{new Coordinate(0,0),new Coordinate(1,1)}),
					gf.createLinearRing(new Coordinate[]{new Coordinate(0,0),new Coordinate(1,1),new Coordinate(2,2),new Coordinate(0,0)}),
					gf.createPolygon(gf.createLinearRing(new Coordinate[]{new Coordinate(0,0),new Coordinate(1,1),new Coordinate(2,2),new Coordinate(0,0)}),null)
				},null,null	
			);
			
			GMLGeometryCollectionTypeBinding s = 
				(GMLGeometryCollectionTypeBinding)container.getComponentInstanceOfType(GMLGeometryCollectionTypeBinding.class);
			
			GeometryCollection gc = (GeometryCollection)s.parse(gcol,node,null);
			assertNotNull(gc);
			assertEquals(gc.getNumGeometries(),5);
			assertTrue(gc.getGeometryN(0) instanceof Point);
			assertTrue(gc.getGeometryN(1) instanceof Point);
			assertTrue(gc.getGeometryN(2) instanceof LineString);
			assertTrue(gc.getGeometryN(3) instanceof LinearRing);
			assertTrue(gc.getGeometryN(4) instanceof Polygon);
	}
}
