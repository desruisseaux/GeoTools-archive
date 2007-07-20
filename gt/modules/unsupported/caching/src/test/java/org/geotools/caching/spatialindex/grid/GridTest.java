package org.geotools.caching.spatialindex.grid;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.caching.spatialindex.AbstractSpatialIndex;
import org.geotools.caching.spatialindex.AbstractSpatialIndexTest;
import org.geotools.caching.spatialindex.Region;


public class GridTest extends AbstractSpatialIndexTest {
    Grid index;

    public static Test suite() {
        return new TestSuite(GridTest.class);
    }

    protected AbstractSpatialIndex createIndex() {
        index = new Grid(new Region(universe), 100);

        return index;
    }

    /*public void testInsertion() {
            super.testInsertion();
            System.out.println("Root insertions = " + index.root_insertions) ;
    }*/
}
