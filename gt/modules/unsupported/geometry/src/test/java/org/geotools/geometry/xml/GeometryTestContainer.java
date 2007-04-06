package org.geotools.geometry.xml;


import java.util.List;
import java.util.ArrayList;

/**
 * the {@code GeometryTest} class is a container that holds a {@code List} of
 * {@code GeometryTestCase}s and provides a way to execute them all.
 * @author <a href="mailto:joel@lggi.com">Joel Skelton</a>
 */
public class GeometryTestContainer {
    private List<GeometryTestCase> testCases;

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


}
