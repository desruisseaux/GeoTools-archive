package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class GMLMultiLineStringPropertyTypeBindingTest extends AbstractGMLBindingTest {
	ElementInstance association;
	ElementInstance geometry;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		association = createElement(GML.NAMESPACE,"myMultiLineStringProperty",GML.MULTILINESTRINGPROPERTYTYPE,null);
		geometry = createElement(GML.NAMESPACE,"myMultiLineString",GML.MULTILINESTRINGTYPE,null);
		
		container = new DefaultPicoContainer();
		container.registerComponentImplementation(GeometryFactory.class);
		container.registerComponentImplementation(GMLGeometryAssociationTypeBinding.class);
		container.registerComponentImplementation(GMLMultiLineStringPropertyTypeBinding.class);
	}
	
	public void testWithGeometry() throws Exception {
		LineString p1 = new GeometryFactory().createLineString(new Coordinate[]{new Coordinate(0,0),new Coordinate(1,1)});
		LineString p2 = new GeometryFactory().createLineString(new Coordinate[]{new Coordinate(2,2),new Coordinate(3,3)});
		
		Node node = createNode(
			association, new ElementInstance[]{geometry},
			new Object[]{new GeometryFactory().createMultiLineString(new LineString[]{p1,p2})},
			null,null
		);
		
		GMLGeometryAssociationTypeBinding s = 
			(GMLGeometryAssociationTypeBinding)container.getComponentInstanceOfType(GMLGeometryAssociationTypeBinding.class);
		
		GMLMultiLineStringPropertyTypeBinding s1 = (GMLMultiLineStringPropertyTypeBinding) container.getComponentInstanceOfType(GMLMultiLineStringPropertyTypeBinding.class);
		
		MultiLineString p = (MultiLineString) s1.parse(association,node,s.parse(association,node,null));
		assertNotNull(p);
	}
}
