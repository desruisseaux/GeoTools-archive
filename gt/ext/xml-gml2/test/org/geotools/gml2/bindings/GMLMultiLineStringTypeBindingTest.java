package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;

public class GMLMultiLineStringTypeBindingTest extends AbstractGMLBindingTest {

	ElementInstance ml,line1,line2;
	GeometryFactory gf;
	
	protected void setUp() throws Exception {
		super.setUp();
	
		line1 = createElement(GML.NAMESPACE,"myLine",GML.LINESTRINGMEMBERTYPE,null);
		line2 = createElement(GML.NAMESPACE,"myLine",GML.LINESTRINGMEMBERTYPE,null);
		ml = createElement(GML.NAMESPACE,"myMultiLine",GML.MULTILINESTRINGTYPE,null);
		
		container = new DefaultPicoContainer();
		container.registerComponentImplementation(GeometryFactory.class);
		container.registerComponentImplementation(GMLGeometryCollectionTypeBinding.class);
		container.registerComponentImplementation(GMLMultiLineStringTypeBinding.class);
	}
	
	public void test() throws Exception {
		Node node = createNode(ml,
			new ElementInstance[]{line1,line2}, new Object[]{
				new GeometryFactory().createLineString(new Coordinate[]{new Coordinate(0,0),new Coordinate(1,1)}),
				new GeometryFactory().createLineString(new Coordinate[]{new Coordinate(2,2),new Coordinate(3,3)})
			},null,null	
		);
		
		GMLGeometryCollectionTypeBinding s1 = 
			(GMLGeometryCollectionTypeBinding)container.getComponentInstanceOfType(GMLGeometryCollectionTypeBinding.class);
		GMLMultiLineStringTypeBinding s2 =
			(GMLMultiLineStringTypeBinding)container.getComponentInstanceOfType(GMLMultiLineStringTypeBinding.class);
		
		MultiLineString mline = (MultiLineString)s2.parse(ml,node,s1.parse(ml,node,null));
		
		assertNotNull(mline);
		assertEquals(mline.getNumGeometries(),2);
		
	}
}
