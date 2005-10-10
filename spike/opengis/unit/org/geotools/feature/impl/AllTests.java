package org.geotools.feature.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.feature.impl");
		//$JUnit-BEGIN$
		suite.addTestSuite(AttributeImplTest.class);
		suite.addTestSuite(ComplexAttributeImplTest.class);
		suite.addTestSuite(FeatureImplTest.class);
		suite.addTestSuite(AttributeFactoryImplTest.class);
		//$JUnit-END$
		return suite;
	}

}
