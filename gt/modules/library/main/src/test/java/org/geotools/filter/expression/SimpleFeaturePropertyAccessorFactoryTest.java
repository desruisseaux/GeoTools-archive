package org.geotools.filter.expression;

import java.util.Map;

import org.geotools.feature.Feature;

import junit.framework.TestCase;

public class SimpleFeaturePropertyAccessorFactoryTest extends TestCase {

	SimpleFeaturePropertyAccessorFactory factory;
	
	protected void setUp() throws Exception {
		factory = new SimpleFeaturePropertyAccessorFactory();
	}
	
	public void test() {
		assertNotNull( factory.createPropertyAccessor( Feature.class, "xpath", null, null ) );
		assertNull( factory.createPropertyAccessor( Map.class , "xpath", null, null ) );
	}
	
	
}
