package org.geotools.renderer.lite;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.renderer.lite");
		//$JUnit-BEGIN$
		suite.addTest(LiteShapeTest.suite());
		suite.addTestSuite(GridCoverageRendererTest.class);
		suite.addTest(Rendering2DTest.suite());
		suite.addTestSuite(BoundsExtractorTest.class);
		suite.addTestSuite(LabelingTest.class);
		//$JUnit-END$
		return suite;
	}

}
