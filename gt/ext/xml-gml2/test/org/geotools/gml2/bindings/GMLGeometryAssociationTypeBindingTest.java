package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GMLGeometryAssociationTypeBindingTest extends AbstractGMLBindingTest {

	ElementInstance association;
	ElementInstance geometry;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		association = createElement(GML.NAMESPACE,"myAssociation",GML.GEOMETRYASSOCIATIONTYPE,null);
		geometry = createElement(GML.NAMESPACE,"myGeometry",GML.ABSTRACTGEOMETRYTYPE,null);
	}
	
	public void testWithGeometry() throws Exception {
		Node node = createNode(
			association, new ElementInstance[]{geometry},
			new Object[]{new GeometryFactory().createPoint(new Coordinate(0,0))},
			null,null
		);
		GMLGeometryAssociationTypeBinding s = 
			(GMLGeometryAssociationTypeBinding)getBinding(GML.GEOMETRYASSOCIATIONTYPE);
		Geometry g = (Geometry) s.parse(association,node,null);
		assertNotNull(g);
		assertTrue(g instanceof Point);
	}
	
	public void testWithoutGeometry() throws Exception {
		Node node = createNode(association, null,null, null,null);
		GMLGeometryAssociationTypeBinding s = 
			(GMLGeometryAssociationTypeBinding)getBinding(GML.GEOMETRYASSOCIATIONTYPE);
	
		try {
			s.parse(association,node,null);
			fail("Empty geometry association should throw exception");
		} 
		catch (Exception e) {}
	}
}
