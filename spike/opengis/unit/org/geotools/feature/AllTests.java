package org.geotools.feature;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.feature.*");
		//$JUnit-BEGIN$
		suite.addTest(org.geotools.feature.schema.AllTests.suite());
		suite.addTest(org.geotools.feature.type.AllTests.suite());
		//$JUnit-END$
		return suite;
	}

}
