package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class GMLBoxTypeBindingTest extends AbstractGMLBindingTest {

	ElementInstance box,coord1,coord2,coord3,coords;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		box = createElement(GML.NAMESPACE,"myBox",GML.BOXTYPE,null);
		coord1 = createElement(GML.NAMESPACE,"coord",GML.COORDTYPE,null);
		coord2 = createElement(GML.NAMESPACE,"coord",GML.COORDTYPE,null);
		coord3 = createElement(GML.NAMESPACE,"coord",GML.COORDTYPE,null);
		coords = createElement(GML.NAMESPACE,"coordinates",GML.COORDINATESTYPE,null);
		
	}
	
	public void testTwoCoord() throws Exception {
		Node node = createNode(
			box, new ElementInstance[]{coord1,coord2}, 
			new Object[]{
				createCoordinateSequence(new Coordinate(1,2)),
				createCoordinateSequence(new Coordinate(3,4))
			}, null,null
		);
		
		GMLBoxTypeBinding s = (GMLBoxTypeBinding)getBinding(GML.BOXTYPE);
		Envelope e = (Envelope)s.parse(box,node,null);
		assertNotNull(e);
		assertEquals(e.getMinX(),1d,0d);
		assertEquals(e.getMinY(),2d,0d);
		assertEquals(e.getMaxX(),3d,0d);
		assertEquals(e.getMaxY(),4d,0d);
	}
	
	public void testSingleCoord() throws Exception {
		Node node = createNode(
			box, new ElementInstance[]{coord1}, 
			new Object[]{
				createCoordinateSequence(new Coordinate(1,2))
			}, null,null
		);
		
		GMLBoxTypeBinding s = (GMLBoxTypeBinding)getBinding(GML.BOXTYPE);
		
		try {
			Envelope e = (Envelope)s.parse(box,node,null);
			fail("< 2 coordinate envelope should have thrown exception");
		} 
		catch (Exception e) {
			//ok
		}
	}
	
	public void testMultiCoord() throws Exception {
		Node node = createNode(
			box, new ElementInstance[]{coord1,coord2,coord3}, 
			new Object[]{
				createCoordinateSequence(new Coordinate(1,2)),
				createCoordinateSequence(new Coordinate(3,4)),
				createCoordinateSequence(new Coordinate(5,6))
			}, null,null
		);
		
		GMLBoxTypeBinding s = (GMLBoxTypeBinding)getBinding(GML.BOXTYPE);
		
		try {
			Envelope e = (Envelope)s.parse(box,node,null);
			fail("> 2 coordinate envelope should have thrown exception");
		} 
		catch (Exception e) {
			//ok
		}
	}
	
	public void testTwoCoordinates() throws Exception {
		Node node = createNode(
			box, new ElementInstance[]{coords}, 
			new Object[]{
				createCoordinateSequence(
					new Coordinate[]{new Coordinate(1,2),new Coordinate(3,4)}
				)
			}, null,null
		);
		
		GMLBoxTypeBinding s = (GMLBoxTypeBinding)getBinding(GML.BOXTYPE);
		Envelope e = (Envelope)s.parse(box,node,null);
		assertNotNull(e);
		assertEquals(e.getMinX(),1d,0d);
		assertEquals(e.getMinY(),2d,0d);
		assertEquals(e.getMaxX(),3d,0d);
		assertEquals(e.getMaxY(),4d,0d);
	}
	
	public void testSingleCoordinates() throws Exception {
		Node node = createNode(
			box, new ElementInstance[]{coords}, 
			new Object[]{
				createCoordinateSequence(new Coordinate(1,2))
			}, null,null
		);
		
		GMLBoxTypeBinding s = (GMLBoxTypeBinding)getBinding(GML.BOXTYPE);
		
		try {
			Envelope e = (Envelope)s.parse(box,node,null);
			fail("< 2 coordinate envelope should have thrown exception");
		} 
		catch (Exception e) {
			//ok
		}
	}
	
	public void testMultiCoordinates() throws Exception {
		Node node = createNode(
			box, new ElementInstance[]{coords}, 
			new Object[]{
				createCoordinateSequence(
					new Coordinate[]{new Coordinate(1,2),new Coordinate(3,4),new Coordinate(5,6)}
				)
				
			}, null,null
		);
		
		GMLBoxTypeBinding s = (GMLBoxTypeBinding)getBinding(GML.BOXTYPE);
		
		try {
			Envelope e = (Envelope)s.parse(box,node,null);
			fail("> 2 coordinate envelope should have thrown exception");
		} 
		catch (Exception e) {
			//ok
		}
	}
}
