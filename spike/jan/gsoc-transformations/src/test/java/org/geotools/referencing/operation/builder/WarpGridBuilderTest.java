/**
 *
 */
package org.geotools.referencing.operation.builder;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;


/**
 * @author jezekjan
 *
 */
public class WarpGridBuilderTest extends TestCase {
    private double tolerance = 0.05; //cm
    private CoordinateReferenceSystem crs = DefaultEngineeringCRS.GENERIC_2D;
    private boolean show = true;

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
     * @return
     */
    public static Test suite() {
        return new TestSuite(WarpGridBuilderTest.class);
    }

    /**
     * Generates Mapped positions inside specified envelope.
     * @param env Envelope
     * @param number Number of points to be generated
     * @param deltas approximately the deltas between source and target point.
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

    /**
     * Test of TPDWarpGridBuilder
     *
     */
    public void testIDWWarpGridBuilder() {
        try {
            // Envelope 20*20 km 
            Envelope env = new Envelope2D(crs, 0, 0, 20000, 20000);

            // Generates 15 MappedPositions of approximately 2 m differences
            List mp = generateMappedPositions(env, 15, 2, crs);

            WarpGridBuilder builder = new IDWGridBuilder(mp, 100, 100, env);

            if (show == true) {
                (new GridCoverageFactory()).create("IDW - dx", builder.getDxGrid(), env).show();
                (new GridCoverageFactory()).create("IDW - dy", builder.getDyGrid(), env).show();
            }

            assertBuilder(builder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test of IDWDWarpGridBuilder
     *
     */
    public void testTPSWarpGridBuilder() {
        try {
            // Envelope 20*20 km 
            Envelope env = new Envelope2D(crs, 0, 0, 6000, 6000);

            // Generates 15 MappedPositions of approximately 2 m differences
            List mp = generateMappedPositions(env, 15, 5, crs);

            GeneralMatrix M = new GeneralMatrix(3, 3);
            double[] m0 = { 1000, 0, 0 };
            double[] m1 = { 0, 1000, 0 };
            double[] m2 = { 0, 0, 1 };
            M.setRow(0, m0);
            M.setRow(1, m1);
            M.setRow(2, m2);

            WarpGridBuilder builder = new TPSGridBuilder(mp, 20, 20, env,
                    ProjectiveTransform.create(M));

            if (show == true) {
                (new GridCoverageFactory()).create("TPS - dx", builder.getDxGrid(), env).show();
                (new GridCoverageFactory()).create("TPS - dy", builder.getDyGrid(), env).show();
            }

            assertBuilder(builder);
            assertInverse(builder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests that transformed source point fits to target point (considering tolerance).
     * @param builder
     */
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

    private void assertInverse(MathTransformBuilder builder) {
        try {
            List mp = builder.getMappedPositions();

            for (int i = 0; i < mp.size(); i++) {
                MappedPosition p = (MappedPosition) mp.get(i);

                MappedPosition inversMp = new MappedPosition(p.getTarget(), p.getSource());
                //inversMp.add(new MappedPosition(p.getTarget(),p.getSource()));
             //   System.out.println(inversMp.getError(builder.getMathTransform().inverse(), null));
                Assert.assertEquals(0, inversMp.getError(builder.getMathTransform().inverse(), null),
                    tolerance);
            }
        } catch (NoninvertibleTransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FactoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //builder.getMathTransform().inverse();
    }
}
