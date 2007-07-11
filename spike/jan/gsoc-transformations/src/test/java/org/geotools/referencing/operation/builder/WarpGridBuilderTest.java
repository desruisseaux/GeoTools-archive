/**
 *
 */
package org.geotools.referencing.operation.builder;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultEngineeringCRS;


/**
 * @author jezekjan
 *
 */
public class WarpGridBuilderTest extends TestCase {
    private double tolerance = 0.02; //cm
    private CoordinateReferenceSystem crs = DefaultEngineeringCRS.GENERIC_2D;

    /**
     * Run the suite from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     *
     * @return DOCUMENT ME!
     */
    public static Test suite() {
        return new TestSuite(WarpGridBuilderTest.class);
    }

    /**
     * Generates Mapped positions inside specified envelope.
     * @param env Envelope
     * @param number Number of points to be generated
     * @param deltas aproximatly the delats betwean source and target point.
     * @return
     */
    private List /*<MappedPositions>*/ generateMappedPositions(Envelope env, int number,
        double deltas, CoordinateReferenceSystem crs) {
        List /*<MappedPositions>*/ vectors = new ArrayList();
        double minx = env.getLowerCorner().getCoordinates()[0];
        double miny = env.getLowerCorner().getCoordinates()[1];

        double maxx = env.getUpperCorner().getCoordinates()[0];
        double maxy = env.getUpperCorner().getCoordinates()[1];

        final Random random = new Random(8578348921369L);

        for (int i = 0; i < number; i++) {
            double x = minx + (random.nextDouble() * (maxx - minx));
            double y = miny + (random.nextDouble() * (maxy - miny));
            vectors.add(new MappedPosition(new DirectPosition2D(crs, x, y),
                    new DirectPosition2D(crs,
                        (x + (random.nextDouble() * deltas)) - (random.nextDouble() * deltas),
                        (y + (random.nextDouble() * deltas)) - (random.nextDouble() * deltas))));
        }

        return vectors;
    }

    public void testTPSWarpGridBuilder() {
        try {
            // Envelope 20*20 km 
            Envelope env = new Envelope2D(crs, 0, 0, 20000, 20000);

            // Generates 15 MappedPositions of aproximatly 2 m diferences
            List mp = generateMappedPositions(env, 15, 2, crs);

            TPSGridBuilder builder = new TPSGridBuilder(mp, 500, 500, env);

            // Uncoment to show the grid
            //(new GridCoverageFactory()).create("",builder.getDxGrid(),env).show();
            assertBuilder(builder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testIDWarpGridBuilder() {
        try {
            // Envelope 20*20 km 
            Envelope env = new Envelope2D(crs, 0, 0, 20000, 20000);

            // Generates 15 MappedPositions of aproximatly 2 m diferences
            List mp = generateMappedPositions(env, 15, 2, crs);

            TPSGridBuilder builder = new TPSGridBuilder(mp, 500, 500, env);

            // Uncoment to show the grid
            //(new GridCoverageFactory()).create("",builder.getDxGrid(),env).show();
            assertBuilder(builder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assertBuilder(MathTransformBuilder builder) {
        List mp = builder.getMappedPositions();

        try {
            for (int i = 0; i < mp.size(); i++) {
                Assert.assertEquals(0,
                    ((MappedPosition) mp.get(i)).getError(builder.getMathTransform(), null),
                    tolerance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
