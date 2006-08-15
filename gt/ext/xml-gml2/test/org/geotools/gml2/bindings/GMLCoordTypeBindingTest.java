package org.geotools.gml2.bindings;

import java.math.BigDecimal;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.geotools.xs.bindings.XS;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;


public class GMLCoordTypeBindingTest extends AbstractGMLBindingTest {
	
	ElementInstance x,y,z;
	ElementInstance coordinate;
	MutablePicoContainer container;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		x = createElement(GML.NAMESPACE, "X", XS.DECIMAL, "12.34");
		y = createElement(GML.NAMESPACE, "Y", XS.DECIMAL, "56.78");
		z = createElement(GML.NAMESPACE, "Z", XS.DECIMAL, "910.11");
		coordinate = 
			createElement(GML.NAMESPACE, "myCoordinate", GML.COORDTYPE, null);
		
		container = new DefaultPicoContainer();
		container.registerComponentInstance(CoordinateArraySequenceFactory.instance());
		container.registerComponentImplementation(GMLCoordTypeBinding.class);
		
	}
	public void testParse1D() throws Exception {
		Node node = createNode(
			coordinate, new ElementInstance[]{x}, new Object[]{new BigDecimal(12.34)},
			null, null
		);
		
		GMLCoordTypeBinding strategy = 
			(GMLCoordTypeBinding) container.getComponentInstanceOfType(GMLCoordTypeBinding.class);
		
		CoordinateSequence c = (CoordinateSequence) strategy.parse(coordinate,node,null);
		assertNotNull(c);
		assertEquals(c.getOrdinate(0,CoordinateSequence.X), 12.34, 0d);
	}
	
	public void testParse2D() throws Exception {
		Node node = createNode(
			coordinate, new ElementInstance[]{x,y}, 
			new Object[]{new BigDecimal(12.34),new BigDecimal(56.78)},
			null, null
		);
		
		
		GMLCoordTypeBinding strategy = 
			(GMLCoordTypeBinding) container.getComponentInstanceOfType(GMLCoordTypeBinding.class);
		
		CoordinateSequence c = (CoordinateSequence) strategy.parse(coordinate,node,null);
		assertNotNull(c);
		assertEquals(c.getOrdinate(0,CoordinateSequence.X), 12.34, 0d);
		assertEquals(c.getOrdinate(0,CoordinateSequence.Y), 56.78, 0d);
	}
	
	public void testParse3D() throws Exception {
		Node node = createNode(
			coordinate, new ElementInstance[]{x,y,z}, 
			new Object[]{new BigDecimal(12.34),new BigDecimal(56.78),new BigDecimal(910.11)},
			null, null	
		);
		GMLCoordTypeBinding strategy = 
			(GMLCoordTypeBinding) container.getComponentInstanceOfType(GMLCoordTypeBinding.class);
		
		CoordinateSequence c = (CoordinateSequence) strategy.parse(coordinate,node,null);
		assertNotNull(c);
		assertEquals(c.getOrdinate(0,CoordinateSequence.X), 12.34, 0d);
		assertEquals(c.getOrdinate(0,CoordinateSequence.Y), 56.78, 0d);
		assertEquals(c.getOrdinate(0,CoordinateSequence.Z), 910.11, 0d);
	}
}