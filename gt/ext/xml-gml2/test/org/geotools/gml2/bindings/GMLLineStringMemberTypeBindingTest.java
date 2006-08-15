package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class GMLLineStringMemberTypeBindingTest extends AbstractGMLBindingTest {
	ElementInstance association;
	ElementInstance geometry;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		association = createElement(GML.NAMESPACE,"myAssociation",GML.LINESTRINGMEMBERTYPE,null);
		geometry = createElement(GML.NAMESPACE,"myGeometry",GML.LINESTRINGTYPE,null);
	}
	
	public void testWithGeometry() throws Exception {
		Node node = createNode(
			association, new ElementInstance[]{geometry},
			new Object[]{new GeometryFactory().createLineString(new Coordinate[]{new Coordinate(0,0),new Coordinate(1,1)})},
			null,null
		);
		GMLGeometryAssociationTypeBinding s1 = 
			(GMLGeometryAssociationTypeBinding)getBinding(GML.GEOMETRYASSOCIATIONTYPE);
		Geometry g = (Geometry) s1.parse(association,node,null);
		
		GMLLineStringMemberTypeBinding s2 = (GMLLineStringMemberTypeBinding)getBinding(GML.LINESTRINGMEMBERTYPE);
		g = (Geometry) s2.parse(association,node,g);
		
		assertNotNull(g);
		assertTrue(g instanceof LineString);
	}
}
