package org.geotools.gml2.bindings;

import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class GMLMultiPointTypeBindingTest extends AbstractGMLBindingTest {
	
	ElementInstance mp,point1,point2;
	GeometryFactory gf;
	
	protected void setUp() throws Exception {
		super.setUp();
	
		point1 = createElement(GML.NAMESPACE,"myPoint",GML.POINTMEMBERTYPE,null);
		point2 = createElement(GML.NAMESPACE,"myPoint",GML.POINTMEMBERTYPE,null);
		mp = createElement(GML.NAMESPACE,"myMultiPoint",GML.MULTIPOINTTYPE,null);
		
		container = new DefaultPicoContainer();
		container.registerComponentImplementation(GeometryFactory.class);
		container.registerComponentImplementation(GMLGeometryCollectionTypeBinding.class);
		container.registerComponentImplementation(GMLMultiPointTypeBinding.class);
	}
	
	public void test() throws Exception {
		Node node = createNode(mp,
			new ElementInstance[]{point1,point2}, new Object[]{
				new GeometryFactory().createPoint(new Coordinate(0,0)),
				new GeometryFactory().createPoint(new Coordinate(1,1))
			},null,null	
		);
		
		GMLGeometryCollectionTypeBinding s1 = 
			(GMLGeometryCollectionTypeBinding)container.getComponentInstanceOfType(GMLGeometryCollectionTypeBinding.class);
		GMLMultiPointTypeBinding s2 =
			(GMLMultiPointTypeBinding)container.getComponentInstanceOfType(GMLMultiPointTypeBinding.class);
		
		MultiPoint mpoint = (MultiPoint)s2.parse(mp,node,s1.parse(mp,node,null));
		
		assertNotNull(mpoint);
		assertEquals(mpoint.getNumGeometries(),2);
		
		assertEquals(((Point)mpoint.getGeometryN(0)).getX(),0d,0d);
		assertEquals(((Point)mpoint.getGeometryN(0)).getY(),0d,0d);
		assertEquals(((Point)mpoint.getGeometryN(1)).getX(),1d,0d);
		assertEquals(((Point)mpoint.getGeometryN(1)).getY(),1d,0d);
	}
	
}