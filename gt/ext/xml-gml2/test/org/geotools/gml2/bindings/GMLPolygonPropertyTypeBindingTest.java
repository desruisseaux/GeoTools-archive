package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class GMLPolygonPropertyTypeBindingTest extends AbstractGMLBindingTest {
	ElementInstance association;
	ElementInstance geometry;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		association = createElement(GML.NAMESPACE,"myPolygonProperty",GML.POLYGONPROPERTYTYPE,null);
		geometry = createElement(GML.NAMESPACE,"myPolygon",GML.POLYGONTYPE,null);
	}
	
	public void testWithGeometry() throws Exception {
		Node node = createNode(
			association, new ElementInstance[]{geometry},
			new Object[]{new GeometryFactory().createPolygon(new GeometryFactory().createLinearRing(new Coordinate[]{new Coordinate(0,0),new Coordinate(1,1),new Coordinate(2,2),new Coordinate(0,0),}),null)},
			null,null
		);
		GMLGeometryAssociationTypeBinding s = 
			(GMLGeometryAssociationTypeBinding)getBinding(GML.GEOMETRYASSOCIATIONTYPE);
		GMLPolygonPropertyTypeBinding s1 = (GMLPolygonPropertyTypeBinding) getBinding(GML.POLYGONPROPERTYTYPE);
		Polygon p = (Polygon) s1.parse(association,node,s.parse(association,node,null));
		assertNotNull(p);
	}
}
