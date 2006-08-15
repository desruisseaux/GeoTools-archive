package org.geotools.filter;

import java.util.LinkedList;

import junit.framework.TestCase;

import org.geotools.filter.v1_0.OGCSchemaLocationResolver;
import org.geotools.gml2.bindings.GMLSchemaLocationResolver;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.geotools.xs.bindings.XSBindingConfiguration;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.picocontainer.MutablePicoContainer;

public class OGCFilterTest extends TestCase {
	
	Parser parser;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		Configuration configuration = new Configuration() {

        	public void configureBindings(MutablePicoContainer container) {
        		container.registerComponentImplementation(LinkedList.class);
        		
        		new XSBindingConfiguration().configure(container);
        		new org.geotools.filter.v1_0.OGCBindingConfiguration().configure(container);
        	
        	}
        	
        	public void configureContext(MutablePicoContainer container) {
        		container.registerComponentImplementation(GMLSchemaLocationResolver.class);
        		container.registerComponentImplementation(OGCSchemaLocationResolver.class);
        		container.registerComponentImplementation(FilterFactoryImpl.class);
        		
        	}
        };
        
        parser = new Parser(configuration,getClass().getResourceAsStream("test1.xml"));
	}
	
	public void testRun() throws Exception {
		Object thing = parser.parse();
		assertNotNull(thing);
		assertTrue(thing instanceof PropertyIsEqualTo);
		
		PropertyIsEqualTo equal = (PropertyIsEqualTo) thing;
		assertTrue( equal.getExpression1() instanceof PropertyName );
		assertTrue( equal.getExpression2() instanceof Literal );
		
		PropertyName name = (PropertyName) equal.getExpression1();
		assertEquals( "testString", name.getPropertyName() );
		
		Literal literal = (Literal) equal.getExpression2();
		assertEquals( "2", literal.toString() );
	}

}
