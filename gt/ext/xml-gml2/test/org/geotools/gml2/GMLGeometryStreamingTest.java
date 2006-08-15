package org.geotools.gml2;

import java.io.InputStream;
import java.util.LinkedList;

import junit.framework.TestCase;

import org.geotools.gml2.bindings.GMLBindingConfiguration;
import org.geotools.gml2.bindings.GMLSchemaLocationResolver;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.geotools.xml.StreamingParser;
import org.geotools.xs.bindings.XSBindingConfiguration;
import org.picocontainer.MutablePicoContainer;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

public class GMLGeometryStreamingTest extends TestCase {
	
	StreamingParser parser;
	
	protected void setUp() throws Exception {	
		Configuration configuration = new Configuration() {

        	public void configureBindings(MutablePicoContainer container) {
        		container.registerComponentImplementation(LinkedList.class);
        		new XSBindingConfiguration().configure(container);
        		new GMLBindingConfiguration().configure(container);
        	}
        	
        	public void configureContext(MutablePicoContainer container) {
        		container.registerComponentImplementation(GMLSchemaLocationResolver.class);
        		container.registerComponentInstance(CoordinateArraySequenceFactory.instance());
        		container.registerComponentImplementation(GeometryFactory.class);
        	}
        };
        
        parser = new StreamingParser(
        		configuration,getClass().getResourceAsStream("geometry.xml"), "/child::*"
		);
    }

	public void test() throws Exception {
		Object o = parser.parse();
		assertNotNull(o);
		assertTrue(o instanceof Point);
		
		o = parser.parse();
		assertNotNull(o);
		assertTrue(o instanceof LineString);
		
		o = parser.parse();
		assertNotNull(o);
		assertTrue(o instanceof Polygon);
		
		o = parser.parse();
		assertNull(o);
	}
}
