package org.geotools.geometry.xml;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.opengis.geometry.Geometry;

/**
 * This class represents the part of the JTS test XML file
 * that is wrapped with the "case" tags. It contains two
 * geometry objects and then one or more tests to apply
 * to those geometries
 * @author <a href="mailto:joel@lggi.com">Joel Skelton</a>
 */
public class GeometryTestCase {
    private static final Logger LOG = Logger.getLogger("org.geotools.geometry");
    private List<GeometryTestOperation> operationList;
    private Geometry geomA;
    private Geometry geomB;
    private String description;

    /**
     * Constructor
     */
    public GeometryTestCase() {
        this.operationList = new ArrayList<GeometryTestOperation>();
        this.geomA = null;
        this.geomB = null;
        description = "No description";        
    }

    /**
     * Sets the geometry specified by the A tag
     * @param a
     */
    public void setGeometryA(Geometry a) {
        geomA = a;
    }

    /**
     * Sets the geometry specified by the b tag
     * @param b
     */
    public void setGeometryB(Geometry b) {
        geomB = b;
    }

    /**
     * Adds in a test operation that will be run on the given
     * A and B geometries.
     * @param op
     */
    public void addTestOperation(GeometryTestOperation op) {
        operationList.add(op);
    }

    /**
     * Sets the description text string for this test case. The
     * description is used for logging results.
     * @param desc
     */
    public void setDescription(String desc) {
        description = desc;
    }

    /**
     * Run any test operations stored for this test case
     * @return result
     */
    public boolean runTestCases() {
        boolean result = true;
        LOG.info("Running test:" + description);
        for (GeometryTestOperation op : operationList) {
            LOG.info("Running test case:" + op);
            if (!op.run(geomA, geomB)) {
                LOG.severe(op.toString() + " failed");
                result = false;
            }
        }
        return result;
    }
}
