package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Envelope;

public class GMLBoundingShapeTypeBindingTest extends AbstractGMLBindingTest {

	ElementInstance boundingShape;
	ElementInstance box;
	ElementInstance nil;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		boundingShape = createElement(GML.NAMESPACE,"myBoundingShape",GML.BOUNDINGSHAPETYPE,null);
		box = createElement(GML.NAMESPACE,"Box",GML.BOXTYPE,null);
		nil = createElement(GML.NAMESPACE,"null",GML.NULLTYPE,null);
	}
	
	public void testWithBox() throws Exception {
		Envelope e = new Envelope();
		e.expandToInclude(1,2);
		
		Node node = createNode(
			boundingShape, new ElementInstance[]{box}, new Object[]{e},
			null,null
		);
		
		GMLBoundingShapeTypeBinding s = 
			(GMLBoundingShapeTypeBinding)getBinding(GML.BOUNDINGSHAPETYPE);
		
		
		
		Envelope e1 = (Envelope) s.parse(boundingShape,node,null);
		assertNotNull(e1);
		
		assertEquals(e1,e);
	}
	
	public void testWithNull() throws Exception {
		Node node = createNode(
			boundingShape, new ElementInstance[]{nil}, new Object[]{"unknown"},
			null,null
		);
		
		GMLBoundingShapeTypeBinding s = 
			(GMLBoundingShapeTypeBinding)getBinding(GML.BOUNDINGSHAPETYPE);
		
		
		
		Envelope e1 = (Envelope) s.parse(boundingShape,node,null);
		assertNotNull(e1);
		assertTrue(e1.isNull());
	}
}
