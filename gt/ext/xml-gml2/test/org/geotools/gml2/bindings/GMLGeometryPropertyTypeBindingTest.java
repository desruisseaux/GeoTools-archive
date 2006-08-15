package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GMLGeometryPropertyTypeBindingTest extends AbstractGMLBindingTest {
	ElementInstance association;
	ElementInstance geometry;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		association = createElement(GML.NAMESPACE,"myGeometryProperty",GML.GEOMETRYPROPERTYTYPE,null);
		geometry = createElement(GML.NAMESPACE,"myPoint",GML.POINTTYPE,null);
	}
	
	public void testWithGeometry() throws Exception {
		Node node = createNode(
			association, new ElementInstance[]{geometry},
			new Object[]{new GeometryFactory().createPoint(new Coordinate(0,0))},
			null,null
		);
		GMLGeometryAssociationTypeBinding s = 
			(GMLGeometryAssociationTypeBinding)getBinding(GML.GEOMETRYASSOCIATIONTYPE);
		GMLGeometryPropertyTypeBinding s1 = (GMLGeometryPropertyTypeBinding) getBinding(GML.GEOMETRYPROPERTYTYPE);
		Geometry p = (Geometry) s1.parse(association,node,s.parse(association,node,null));
		assertNotNull(p);
	}
}
