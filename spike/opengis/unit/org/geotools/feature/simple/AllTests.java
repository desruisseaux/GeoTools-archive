package org.geotools.feature.simple;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.feature.simple");
		//$JUnit-BEGIN$
		suite.addTest(SimpleFeatureTypeImplTest.suite());
		suite.addTest(SimpleFeatureImplTest.suite());
		suite.addTestSuite(SimpleDescriptorImplTest.class);
		//$JUnit-END$
		return suite;
	}

}
