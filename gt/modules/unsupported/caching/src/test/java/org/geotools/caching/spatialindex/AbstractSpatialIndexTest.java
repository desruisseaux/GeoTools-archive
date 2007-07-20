package org.geotools.caching.spatialindex;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 * @author crousson
 *
 */
public abstract class AbstractSpatialIndexTest extends TestCase {
    protected AbstractSpatialIndex index;
    int setSize = 1000;
    protected ArrayList regions = new ArrayList(setSize);
    protected Random generator = new Random();
    protected Region universe = new Region(new double[] { 0, 0 },
            new double[] { 1, 1 });
    protected double meansize = 0.01;

    protected void setUp() {
        index = createIndex();

        double width = universe.getHigh(0) - universe.getLow(0);
        double height = universe.getHigh(1) - universe.getLow(1);

        for (int i = 0; i < setSize; i++) {
            double centerx = (meansize) +
                (generator.nextDouble() * (width - (2 * meansize)));
            double centery = (meansize) +
                (generator.nextDouble() * (height - (2 * meansize)));
            double h = generator.nextDouble() * meansize * 2;
            double w = generator.nextDouble() * meansize * 2;
            Region reg = new Region(new double[] {
                        centerx - (w / 2), centery - (h / 2)
                    }, new double[] { centerx + (w / 2), centery + (h / 2) });
            regions.add(reg);
        }

        for (int i = 0; i < setSize; i++) {
            Region r = (Region) regions.get(i);
            index.insertData("Object: " + i, r, i);
        }
    }

    public void testInsertion() {
        Statistics stats = index.getStatistics();
        assertEquals(setSize, stats.getNumberOfData());
    }

    public void testDeletion() {
        int dels = setSize;

        for (int i = 0; i < dels; i++) {
            assertTrue(index.deleteData((Region) regions.get(i), i));
        }

        assertEquals(setSize - dels, index.getStatistics().getNumberOfData());
    }

    public void testIntersectionQuery() {
        YieldingVisitor v = new YieldingVisitor();
        Region query = new Region(new double[] { 0, 0 }, new double[] { 1, 1 });
        index.intersectionQuery(query, v);
        assertEquals(setSize, v.harvest.size());
        assertEquals(index.getStatistics().getNumberOfNodes(), v.visited_nodes);

        Set comp_result = noIndexQuery(regions, query,
                AbstractSpatialIndex.IntersectionQuery);
        assertEquals(comp_result, v.harvest);
        v = new YieldingVisitor();
        query = new Region(new double[] { .25, .25 }, new double[] { .75, .75 });
        index.intersectionQuery(query, v);
        assertTrue(v.harvest.size() < setSize);
        comp_result = noIndexQuery(regions, query,
                AbstractSpatialIndex.IntersectionQuery);
        assertEquals(comp_result, v.harvest);
    }

    public void testContainmentQuery() {
        YieldingVisitor v = new YieldingVisitor();
        Region query = new Region(new double[] { 0, 0 }, new double[] { 1, 1 });
        index.containmentQuery(query, v);
        assertEquals(setSize, v.harvest.size());
        assertEquals(index.getStatistics().getNumberOfNodes(), v.visited_nodes);

        Set comp_result = noIndexQuery(regions, query,
                AbstractSpatialIndex.ContainmentQuery);
        assertEquals(comp_result, v.harvest);
        v = new YieldingVisitor();
        query = new Region(new double[] { .25, .25 }, new double[] { .75, .75 });
        index.containmentQuery(query, v);
        assertTrue(v.harvest.size() < setSize);
        comp_result = noIndexQuery(regions, query,
                AbstractSpatialIndex.ContainmentQuery);

        //assertEquals(comp_result, v.harvest) ; // FIXME: store items in multiple tiles, if they intersect with
    }

    public void testPointQuery() {
        YieldingVisitor v = new YieldingVisitor();
        Point query = new Point(new double[] {
                    generator.nextDouble(), generator.nextDouble()
                });
        index.intersectionQuery(query, v);

        Set comp_result = noIndexQuery(regions, query,
                AbstractSpatialIndex.IntersectionQuery);
        assertEquals(comp_result, v.harvest);
    }

    public void testQueryStrategy() {
    }

    public void testFlush() {
        index.flush();

        YieldingVisitor v = new YieldingVisitor();
        Region query = new Region(new double[] { 0, 0 }, new double[] { 1, 1 });
        index.containmentQuery(query, v);
        assertEquals(0, v.harvest.size());
        assertEquals(index.getStatistics().getNumberOfNodes(), v.visited_nodes);
    }

    HashSet noIndexQuery(ArrayList searchset, Shape query, int type) {
        HashSet harvest = new HashSet();

        for (int i = 0; i < setSize; i++) {
            Region r = (Region) regions.get(i);

            if (((type == AbstractSpatialIndex.IntersectionQuery) &&
                    (query.intersects(r))) ||
                    ((type == AbstractSpatialIndex.ContainmentQuery) &&
                    (query.contains(r)))) {
                harvest.add("Object: " + i);
            }
        }

        return harvest;
    }

    protected abstract AbstractSpatialIndex createIndex();

    class YieldingVisitor implements Visitor {
        HashSet harvest = new HashSet(20);
        int visited_nodes = 0;

        public void visitData(Data d) {
            harvest.add(d.getData());
        }

        public void visitNode(Node n) {
            visited_nodes++;
        }
    }
}
