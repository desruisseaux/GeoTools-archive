package org.geotools.caching.spatialindex.grid;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.grid.GridData;


public class GridNodeTest extends TestCase {
    GridNode node;
    Region mbr;
    String data = "Sample data";
    String data2 = "Sample data 2";

    public static Test suite() {
        return new TestSuite(GridNodeTest.class);
    }

    public void setUp() {
        mbr = new Region(new double[] { 0, 1 }, new double[] { 2, 3 });
        node = new GridNode(0, null, mbr);
    }

    public void testConstructor() {
        assertEquals(mbr, node.mbr);
        assertEquals(new Region(mbr), node.mbr);

        GridNode child = new GridNode(1, node, mbr);
        assertEquals(0, child.getLevel());
        assertEquals(node, child.parent);
        assertEquals(mbr, node.mbr);
        assertEquals(new Region(mbr), node.mbr);
    }

    void populate() {
        for (int i = 0; i < 10; i++) {
            node.insertData(8, new GridData(8, mbr, data));
            node.insertData(16, new GridData(16, mbr, data2));
        }
    }

    public void testInsert() {
        populate();
        assertEquals(20, node.num_data);
        assertEquals(8, node.data_ids[0]);
        assertEquals(data2, node.data[1].getData());
        assertEquals(8, node.data_ids[18]);
        assertEquals(data2, node.data[19].getData());
    }

    public void testDelete() {
        populate();
        node.deleteData(14);
        node.deleteData(15);
        assertEquals(18, node.num_data);
        assertEquals(8, node.data_ids[0]);
        assertEquals(data2, node.data[1].getData());
        assertEquals(16, node.data_ids[14]);
        assertEquals(data, node.data[15].getData());
        assertEquals(null, node.data[19]);

        try {
            node.deleteData(18);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }
}
