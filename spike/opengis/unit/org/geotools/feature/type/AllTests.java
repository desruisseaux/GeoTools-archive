package org.geotools.feature.type;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.feature.type");
		//$JUnit-BEGIN$
		suite.addTest(AttributeTypeImplTest.suite());
		suite.addTestSuite(GeometryTypeImplTest.class);
		suite.addTestSuite(ComplexTypeImplTest.class);
		suite.addTestSuite(FeatureCollectionTypeImplTest.class);
		suite.addTestSuite(TypeFactoryImplTest.class);
		suite.addTestSuite(BusinessDriverExamplesTest.class);
		//$JUnit-END$
		return suite;
	}

}
