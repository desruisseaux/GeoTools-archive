package org.geotools.feature.schema;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.feature.schema");
		//$JUnit-BEGIN$
		suite.addTestSuite(DescriptorFactoryImplTest.class);
		//$JUnit-END$
		return suite;
	}

}
