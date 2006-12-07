package org.geotools.filter.expression;

import java.util.Map;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import junit.framework.TestCase;

public class SimpleFeaturePropertyAccessorFactoryTest extends TestCase {

	SimpleFeaturePropertyAccessorFactory factory;
	
	protected void setUp() throws Exception {
		factory = new SimpleFeaturePropertyAccessorFactory();
	}
	
	public void test() {
		
		//make sure features are supported
		assertNotNull( factory.createPropertyAccessor( Feature.class, "xpath", null, null ) );
		assertNotNull( factory.createPropertyAccessor( FeatureType.class, "xpath", null, null ) );
		assertNull( factory.createPropertyAccessor( Map.class , "xpath", null, null ) );
		
		//make sure only simple xpath
		assertNull( factory.createPropertyAccessor( Feature.class, "@xpath", null, null )  );
		assertNull( factory.createPropertyAccessor( FeatureType.class, "@xpath", null, null )  );
		
		assertNull( factory.createPropertyAccessor( Feature.class, "/xpath", null, null ) );
		assertNull( factory.createPropertyAccessor( FeatureType.class, "/xpath", null, null ) );
		
		assertNull( factory.createPropertyAccessor( Feature.class, "*[0]", null, null ) );
		assertNull( factory.createPropertyAccessor( FeatureType.class, "*[0]", null, null ) );
	}
	
	
	
}
