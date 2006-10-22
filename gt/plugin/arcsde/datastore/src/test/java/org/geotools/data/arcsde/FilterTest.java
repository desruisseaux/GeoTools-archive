/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.arcsde;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;


/**
 * This test case does not still use the ArcSDEDataStore testing data supplied
 * with in {@code test-data/import}, so it is excluded in project.xml.
 *
 * @author cdillard
 * @source $URL$
 * @version $Id$
 */
public class FilterTest extends TestCase {
    private static final Comparator FEATURE_COMPARATOR = new Comparator() {
            public int compare(Object o1, Object o2) {
                Feature f1 = (Feature) o1;
                Feature f2 = (Feature) o2;

                return f1.getID().compareTo(f2.getID());
            }
        };

    private DataStore dataStore;

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void setUp() throws Exception {
        super.setUp();

        HashMap storeParams = new HashMap();
        storeParams.put("dbtype", "arcsde");
        storeParams.put("server", "cdillardpc");
        storeParams.put("port", "5151");
        storeParams.put("instance", "sde");
        storeParams.put("user", "sde");
        storeParams.put("password", "sde");
        this.dataStore = DataStoreFinder.getDataStore(storeParams);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     *
     * @param fr DOCUMENT ME!
     * @param c DOCUMENT ME!
     *
     * @throws NoSuchElementException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    private static void collectResults(FeatureReader fr, Collection c)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        while (fr.hasNext())
            c.add(fr.next());
    }

    /**
     * DOCUMENT ME!
     *
     * @param c1 DOCUMENT ME!
     * @param c2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private boolean compareFeatureLists(Collection c1, Collection c2) {
        System.err.println("Collection 1 size: " + c1.size());
        System.err.println("Collection 2 size: " + c2.size());

        if (c1.size() != c2.size()) {
            System.err.println(
                "Returned feature collections have different sizes.");

            return false;
        }

        ArrayList al1 = new ArrayList(c1);
        ArrayList al2 = new ArrayList(c2);
        Collections.sort(al1, FEATURE_COMPARATOR);
        Collections.sort(al2, FEATURE_COMPARATOR);

        int n = c1.size();

        for (int i = 0; i < n; i++) {
            Feature f1 = (Feature) al1.get(i);
            Feature f2 = (Feature) al2.get(i);

            if (!f1.equals(f2)) {
                System.err.println("Mismatch at sorted record " + i + ":");
                System.err.println(f1);
                System.err.println(f2);

                return false;
            }
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param x1 DOCUMENT ME!
     * @param y1 DOCUMENT ME!
     * @param x2 DOCUMENT ME!
     * @param y2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static LineString buildSegment(double x1, double y1, double x2,
        double y2) {
        Coordinate[] coordArray = new Coordinate[] {
                new Coordinate(x1, y1), new Coordinate(x2, y2)
            };
        GeometryFactory gf = new GeometryFactory();
        LineString result = gf.createLineString(coordArray);

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param minx DOCUMENT ME!
     * @param miny DOCUMENT ME!
     * @param maxx DOCUMENT ME!
     * @param maxy DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static Polygon buildPolygon(double minx, double miny, double maxx,
        double maxy) {
        Coordinate[] coordArray = new Coordinate[] {
                new Coordinate(minx, miny), new Coordinate(minx, maxy),
                new Coordinate(maxx, maxy), new Coordinate(maxx, miny),
                new Coordinate(minx, miny)
            };
        GeometryFactory gf = new GeometryFactory();
        Polygon p = gf.createPolygon(gf.createLinearRing(coordArray),
                new LinearRing[0]);

        return p;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testDisjointFilter() throws Exception {
        FeatureType ft = this.dataStore.getSchema("SDE.SDE.JAKARTA");

        // Build the filter
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFilter filter = ff.createGeometryFilter(FilterType.GEOMETRY_DISJOINT);
        filter.addLeftGeometry(ff.createAttributeExpression(ft, "SHAPE"));

        double minx = 106.62;
        double maxx = 106.727;
        double miny = -6.24;
        double maxy = -6.16;
        Polygon p = buildPolygon(minx, miny, maxx, maxy);
        filter.addRightGeometry(ff.createLiteralExpression(p));

        runTestWithFilter(ft, filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testContainsFilter() throws Exception {
        FeatureType ft = this.dataStore.getSchema("SDE.SDE.JAKARTA");

        // Build the filter
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFilter filter = ff.createGeometryFilter(FilterType.GEOMETRY_CONTAINS);
        filter.addLeftGeometry(ff.createAttributeExpression(ft, "SHAPE"));

        double minx = 106.6666;
        double maxx = 106.6677;
        double miny = -6.1676;
        double maxy = -6.1672;
        Polygon p = buildPolygon(minx, miny, maxx, maxy);
        filter.addRightGeometry(ff.createLiteralExpression(p));

        runTestWithFilter(ft, filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testBBoxFilter() throws Exception {
        FeatureType ft = this.dataStore.getSchema("SDE.SDE.JAKARTA");

        // Build the filter
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFilter filter = ff.createGeometryFilter(FilterType.GEOMETRY_BBOX);
        filter.addLeftGeometry(ff.createAttributeExpression(ft, "SHAPE"));

        double minx = 106.6337;
        double maxx = 106.6381;
        double miny = -6.1794;
        double maxy = -6.1727;
        Polygon p = buildPolygon(minx, miny, maxx, maxy);
        filter.addRightGeometry(ff.createLiteralExpression(p));

        runTestWithFilter(ft, filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testIntersectsFilter() throws Exception {
        FeatureType ft = this.dataStore.getSchema("SDE.SDE.JAKARTA");

        // Build the filter
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFilter filter = ff.createGeometryFilter(FilterType.GEOMETRY_INTERSECTS);
        filter.addLeftGeometry(ff.createAttributeExpression(ft, "SHAPE"));

        double minx = 106.6337;
        double maxx = 106.6381;
        double miny = -6.1794;
        double maxy = -6.1727;
        Polygon p = buildPolygon(minx, miny, maxx, maxy);
        filter.addRightGeometry(ff.createLiteralExpression(p));

        runTestWithFilter(ft, filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testOverlapsFilter() throws Exception {
        FeatureType ft = this.dataStore.getSchema("SDE.SDE.JAKARTA");

        // Build the filter
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFilter filter = ff.createGeometryFilter(FilterType.GEOMETRY_OVERLAPS);
        filter.addLeftGeometry(ff.createAttributeExpression(ft, "SHAPE"));

        double minx = 106.6337;
        double maxx = 106.6381;
        double miny = -6.1794;
        double maxy = -6.1727;
        Polygon p = buildPolygon(minx, miny, maxx, maxy);
        filter.addRightGeometry(ff.createLiteralExpression(p));

        runTestWithFilter(ft, filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testWithinFilter() throws Exception {
        FeatureType ft = this.dataStore.getSchema("SDE.SDE.JAKARTA");

        // Build the filter
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFilter filter = ff.createGeometryFilter(FilterType.GEOMETRY_WITHIN);
        filter.addLeftGeometry(ff.createAttributeExpression(ft, "SHAPE"));

        double minx = 106.6337;
        double maxx = 106.6381;
        double miny = -6.1794;
        double maxy = -6.1727;
        Polygon p = buildPolygon(minx, miny, maxx, maxy);
        filter.addRightGeometry(ff.createLiteralExpression(p));

        runTestWithFilter(ft, filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testCrossesFilter() throws Exception {
        FeatureType ft = this.dataStore.getSchema("SDE.SDE.JAKARTA");

        // Build the filter
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFilter filter = ff.createGeometryFilter(FilterType.GEOMETRY_CROSSES);
        filter.addLeftGeometry(ff.createAttributeExpression(ft, "SHAPE"));

        double minx = 106.6337;
        double maxx = 106.6381;
        double miny = -6.1794;
        double maxy = -6.1727;
        LineString ls = buildSegment(minx, miny, maxx, maxy);
        filter.addRightGeometry(ff.createLiteralExpression(ls));

        runTestWithFilter(ft, filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testEqualFilter() throws Exception {
        FeatureType ft = this.dataStore.getSchema("SDE.SDE.JAKARTA");

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();

        // Get a geometry for equality comparison
        Filter fidFilter = ff.createFidFilter("SDE.SDE.JAKARTA.101");
        FeatureReader fr = this.dataStore.getFeatureReader(new DefaultQuery(
                    "SDE.SDE.JAKARTA", fidFilter), Transaction.AUTO_COMMIT);
        Feature feature = fr.next();
        fr.close();

        Geometry g = (Geometry) feature.getAttribute("SHAPE");

        // Build the filter
        GeometryFilter filter = ff.createGeometryFilter(FilterType.GEOMETRY_EQUALS);
        filter.addLeftGeometry(ff.createAttributeExpression(ft, "SHAPE"));
        filter.addRightGeometry(ff.createLiteralExpression(g));

        runTestWithFilter(ft, filter);
    }

    /**
     * DOCUMENT ME!
     *
     * @param ft DOCUMENT ME!
     * @param filter DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void runTestWithFilter(FeatureType ft, Filter filter)
        throws Exception {
        System.err.println("****************");
        System.err.println("**");
        System.err.println("** TESTING FILTER: " + filter);
        System.err.println("**");
        System.err.println("****************");

        // First, read using the slow, built-in mechanisms
        DefaultQuery allJakarta = new DefaultQuery("SDE.SDE.JAKARTA");
        System.err.println("Performing slow read...");

        long startTime = System.currentTimeMillis();
        FeatureReader fr = this.dataStore.getFeatureReader(allJakarta,
                Transaction.AUTO_COMMIT);
        FilteringFeatureReader ffr = new FilteringFeatureReader(fr, filter);
        ArrayList slowResults = new ArrayList();
        collectResults(ffr, slowResults);
        ffr.close();

        long endTime = System.currentTimeMillis();
        System.err.println("Slow read took " + (endTime - startTime)
            + " milliseconds.");

        // Now read using DataStore's mechanisms.
        System.err.println("Performing fast read...");
        startTime = System.currentTimeMillis();

        DefaultQuery filteredJakarta = new DefaultQuery("SDE.SDE.JAKARTA",
                filter);
        fr = this.dataStore.getFeatureReader(filteredJakarta, Transaction.AUTO_COMMIT);

        ArrayList fastResults = new ArrayList();
        collectResults(fr, fastResults);
        fr.close();
        endTime = System.currentTimeMillis();
        System.err.println("Fast read took " + (endTime - startTime)
            + " milliseconds.");

        boolean result = compareFeatureLists(slowResults, fastResults);

        if (result) {
            System.err.println("Results were identical.");
        } else {
            throw new Exception("Results were different.");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(String[] args) throws Exception {
        FilterTest ft = new FilterTest();
        ft.setUp();
        ft.testBBoxFilter();
        ft.testContainsFilter();
        ft.testCrossesFilter();
        ft.testDisjointFilter();
        ft.testEqualFilter();
        ft.testIntersectsFilter();
        ft.testOverlapsFilter();
        ft.testWithinFilter();
        ft.tearDown();
    }
}
