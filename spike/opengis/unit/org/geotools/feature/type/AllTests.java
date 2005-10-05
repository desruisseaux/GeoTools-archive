package org.geotools.feature.type;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.feature.type");
		//$JUnit-BEGIN$
		suite.addTestSuite(BusinessDriverExamplesTest.class);
		suite.addTest(AttributeTypeImplTest.suite());
		suite.addTestSuite(TypeFactoryImplTest.class);
		//$JUnit-END$
		return suite;
	}

}
