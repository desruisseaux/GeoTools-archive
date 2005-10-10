package org.geotools.feature;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.feature.*");
		//$JUnit-BEGIN$
		suite.addTestSuite(TypesTest.class);
		suite.addTestSuite(DescriptorsTest.class);
		suite.addTestSuite(RestrictionsTest.class);
		suite.addTestSuite(XPathTest.class);
		suite.addTest(org.geotools.feature.schema.AllTests.suite());
		suite.addTest(org.geotools.feature.type.AllTests.suite());
		suite.addTest(org.geotools.feature.simple.AllTests.suite());
		suite.addTest(org.geotools.feature.impl.AllTests.suite());
		//$JUnit-END$
		return suite;
	}

}
