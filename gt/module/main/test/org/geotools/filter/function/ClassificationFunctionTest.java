package org.geotools.filter.function;

import org.geotools.feature.Feature;

import junit.framework.TestCase;

public class ClassificationFunctionTest extends TestCase {

    public ClassificationFunctionTest(String testName) {
        super(testName);
    }

    public void testMode() {
    	ClassificationFunction classifier = new ClassificationFunction() {
			public String getName() {
				return null;
			}

			public Object getValue(Feature feature) {
				return null;
			}
    	};
    	// this is an invalid combination of MODE parameters, but it
		// demonstrates the proper operation of passing multiple flags.
    	classifier.setMode(ClassificationFunction.MODE_NaN_ISOLATE | ClassificationFunction.MODE_NaN_KEEP);
    	assertTrue(classifier.isModeSet(ClassificationFunction.MODE_NaN_ISOLATE));
    	assertTrue(classifier.isModeSet(ClassificationFunction.MODE_NaN_KEEP));
    	assertFalse(classifier.isModeSet(ClassificationFunction.MODE_NaN_IGNORE));
    }
}
