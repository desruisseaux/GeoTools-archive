package org.geotools.geometry.xml;


import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.geotools.geometry.iso.PrecisionModel;

import junit.framework.TestSuite;

/**
 * the {@code GeometryTest} class is a container that holds a {@code List} of
 * {@code GeometryTestCase}s and provides a way to execute them all.
 * @author <a href="mailto:joel@lggi.com">Joel Skelton</a>
 */
public class GeometryTestContainer {
    private List<GeometryTestCase> testCases;
    private PrecisionModel precisionModel;
    
    /**
     * Constructor
     */
    public GeometryTestContainer() {
        testCases = new ArrayList<GeometryTestCase>();
    }

    /**
     * Adds a constructed test case into the list of available tests
     * @param testCase
     */
    public void addTestCase(GeometryTestCase testCase) {        
        testCases.add(testCase);
    }

    /**
     * Runs all tests currently contained. Returns true if all tests pass, false otherwise
     * @return true if all tests pass, false otherwise
     */
    public boolean runAllTestCases() {
        for (GeometryTestCase testCase : testCases) {
            if (!testCase.runTestCases()) {
                return false;
            }
        }
        return true;
    }
    
    public void addToTestSuite(String name, TestSuite suite, Properties excludes) {
        for (GeometryTestCase testCase : testCases) {
            //only add the test case if its description is NOT in the excludes list
            if (!GeometryConformanceTest.isExcluded(excludes, testCase.getDescription())) {
                testCase.setName(name);
                //check for overrides on operations
                testCase = GeometryConformanceTest.overrideOps(testCase, excludes);
                suite.addTest(testCase);
            }
        }
    }

	protected PrecisionModel getPrecisionModel() {
		return precisionModel;
	}

	protected void setPrecisionModel(PrecisionModel precisionModel) {
		this.precisionModel = precisionModel;
	}

}
