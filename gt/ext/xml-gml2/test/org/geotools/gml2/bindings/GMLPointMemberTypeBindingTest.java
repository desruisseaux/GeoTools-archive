package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GMLPointMemberTypeBindingTest extends AbstractGMLBindingTest {
	ElementInstance association;
	ElementInstance geometry;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		association = createElement(GML.NAMESPACE,"myAssociation",GML.POINTMEMBERTYPE,null);
		geometry = createElement(GML.NAMESPACE,"myGeometry",GML.POINTTYPE,null);
	}
	
	public void testWithGeometry() throws Exception {
		Node node = createNode(
			association, new ElementInstance[]{geometry},
			new Object[]{new GeometryFactory().createPoint(new Coordinate(0,0))},
			null,null
		);
		GMLGeometryAssociationTypeBinding s1 = 
			(GMLGeometryAssociationTypeBinding)getBinding(GML.GEOMETRYASSOCIATIONTYPE);
		Geometry g = (Geometry) s1.parse(association,node,null);
		
		GMLPointMemberTypeBinding s2 = (GMLPointMemberTypeBinding)getBinding(GML.POINTMEMBERTYPE);
		g = (Geometry) s2.parse(association,node,g);
		
		assertNotNull(g);
		assertTrue(g instanceof Point);
	}
}
