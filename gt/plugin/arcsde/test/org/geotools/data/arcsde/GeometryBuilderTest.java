/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeShape;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: GeometryBuilderTest.java,v 1.1 2004/03/11 00:36:41 groldan Exp $
 */
public class GeometryBuilderTest extends TestCase {
    /** DOCUMENT ME! */
    static Logger LOGGER = Logger.getLogger(GeometryBuilderTest.class.getPackage()
                                                                     .getName());

    /** DOCUMENT ME! */
    private GeometryBuilder geometryBuilder = null;

    /** DOCUMENT ME! */
    private WKTReader wktReader;

    /**
     * Creates a new GeometryBuilderTest object.
     *
     * @param name DOCUMENT ME!
     */
    public GeometryBuilderTest(String name) {
        super(name);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.wktReader = new WKTReader();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        this.geometryBuilder = null;
        this.wktReader = null;
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetDefaultValues() {
        testGetDefaultValue(Point.class);
        testGetDefaultValue(MultiPoint.class);
        testGetDefaultValue(LineString.class);
        testGetDefaultValue(MultiLineString.class);
        testGetDefaultValue(Polygon.class);
        testGetDefaultValue(MultiPolygon.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testPointBuilder() throws Exception {
        testBuildJTSGeometries(Point.class, "pointtest.wkt");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testMultiPointBuilder() throws Exception {
        testBuildJTSGeometries(MultiPoint.class, "multipointtest.wkt");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testLineStringBuilder() throws Exception {
        testBuildJTSGeometries(LineString.class, "linestringtest.wkt");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testMultiLineStringBuilder() throws Exception {
        testBuildJTSGeometries(MultiLineString.class, "multilinestringtest.wkt");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testPolygonBuilder() throws Exception {
        testBuildJTSGeometries(Polygon.class, "polygontest.wkt");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testMultiPolygonBuilder() throws Exception {
        testBuildJTSGeometries(MultiPolygon.class, "multipolygontest.wkt");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testConstructShapePoint() throws Exception {
        Geometry[] testPoints = null;

        testPoints = loadTestData("pointtest.wkt");

        testBuildSeShapes(testPoints);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testConstructShapeMultiPoint() throws Exception {
        Geometry[] testMultiPoints = null;

        testMultiPoints = loadTestData("multipointtest.wkt");

        testBuildSeShapes(testMultiPoints);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testConstructShapeLineString() throws Exception {
        Geometry[] testLineStrings = null;

        testLineStrings = loadTestData("linestringtest.wkt");

        testBuildSeShapes(testLineStrings);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testConstructShapeMultiLineString() throws Exception {
        Geometry[] testMultiLineStrings = null;

        testMultiLineStrings = loadTestData("multilinestringtest.wkt");

        testBuildSeShapes(testMultiLineStrings);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testConstructShapePolygon() throws Exception {
        Geometry[] testPolygons = null;

        testPolygons = loadTestData("polygontest.wkt");

        testBuildSeShapes(testPolygons);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testConstructShapeMultiPolygon() throws Exception {
        Geometry[] testMultiPolygons = null;

        testMultiPolygons = loadTestData("multipolygontest.wkt");

        testBuildSeShapes(testMultiPolygons);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testConstructShapeEmpty() throws Exception {
        Geometry[] testEmptys = new Geometry[6];
        testEmptys[0] = GeometryBuilder.builderFor(Point.class).getEmpty();
        testEmptys[1] = GeometryBuilder.builderFor(MultiPoint.class).getEmpty();
        testEmptys[2] = GeometryBuilder.builderFor(LineString.class).getEmpty();
        testEmptys[3] = GeometryBuilder.builderFor(MultiLineString.class)
                                       .getEmpty();
        testEmptys[4] = GeometryBuilder.builderFor(Polygon.class).getEmpty();
        testEmptys[5] = GeometryBuilder.builderFor(MultiPolygon.class).getEmpty();
        testBuildSeShapes(testEmptys);
    }

    /**
     * tests each geometry in <code>geometries</code> using
     * <code>testConstructShape(Geometry)</code>
     *
     * @param geometries DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private static void testBuildSeShapes(Geometry[] geometries)
        throws Exception {
        for (int i = 0; i < geometries.length; i++) {
            testConstructShape(geometries[i]);
        }
    }

    /**
     * tests the building of SeShape objects from JTS Geometries. To do that,
     * recieves a Geometry object, then creates a GeometryBuilder for it's
     * geometry type and ask it to construct an equivalent SeShape. With this
     * SeShape, checks that it's number of points is equal to the number of
     * points in <code>geometry</code>, and then creates an equivalent
     * Geometry object, wich in turn is checked for equality against
     * <code>geometry</code>.
     *
     * @param geometry DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private static void testConstructShape(Geometry geometry)
        throws Exception {
        LOGGER.finer("testConstructShape: testing " + geometry);

        Class geometryClass = geometry.getClass();
        GeometryBuilder builder = GeometryBuilder.builderFor(geometryClass);

        SeCoordinateReference cr = TestData.getGenericCoordRef();
        if (LOGGER.isLoggable(Level.FINE)) {
            System.err.println("\n\n******************\n" + cr.getXYEnvelope());
        }
        Geometry equivalentGeometry = null;

        SeShape equivalentShape = builder.constructShape(geometry, cr);
        int expectedNumOfPoints = geometry.getNumPoints();

        assertEquals(geometry + " - " + equivalentShape, expectedNumOfPoints,
            equivalentShape.getNumOfPoints());
        LOGGER.fine("geometry and SeShape contains the same number of points: "
            + equivalentShape.getNumOfPoints());

        LOGGER.finer("generating an SeShape's equivalent Geometry");
        equivalentGeometry = builder.construct(equivalentShape);

        LOGGER.fine("now testing both geometries for equivalence: " + geometry
            + " -- " + equivalentGeometry);

        assertEquals(geometry.getDimension(), equivalentGeometry.getDimension());
        LOGGER.fine("dimension test passed");

        assertEquals(geometry.getGeometryType(),
            equivalentGeometry.getGeometryType());
        LOGGER.fine("geometry type test passed");

        assertEquals(geometry + " - " + equivalentGeometry,
            geometry.getNumPoints(), equivalentGeometry.getNumPoints());
        LOGGER.fine("numPoints test passed");

        LOGGER.fine(geometry.getEnvelopeInternal() + " == "
            + equivalentGeometry.getEnvelopeInternal());

        /*
           assertEquals(geometry.getEnvelopeInternal(),
               equivalentGeometry.getEnvelopeInternal());
         */
        assertEquals(geometry.getArea(), equivalentGeometry.getArea(), 0.1);
        LOGGER.fine("area test passed");
    }

    /**
     * Tests that the geometry builder for the geometry class given by
     * <code>geometryClass</code> correctly constcucts JTS geometries fom
     * ArcSDE Java API's <code>SeShape</code>.
     * 
     * <p>
     * To do so, first parses the WKT geometries from the properties file
     * pointed by <code>"test-data/" + testDataSource</code>, then creates
     * their corresponding <code>SeShape</code> objects and finally used
     * GeometryBuilder to build the JTS geometries back, which are tested for
     * equality against the original ones.
     * </p>
     *
     * @param geometryClass a JTS geometry class
     * @param testDataResource the resource name under "test-data/" which
     *        contains the geometries to load in WKT.
     *
     * @throws Exception for any problem that could arise
     */
    private void testBuildJTSGeometries(final Class geometryClass,
        final String testDataResource) throws Exception {
        LOGGER.fine("---- testBuildGeometries: testing " + testDataResource
            + " ----");

        this.geometryBuilder = GeometryBuilder.builderFor(geometryClass);
        LOGGER.fine("created " + this.geometryBuilder.getClass().getName());

        Geometry[] expectedGeometries = loadTestData(testDataResource);
        Geometry createdGeometry;
        Geometry expectedGeometry;
        double[][][] sdeCoords;

        //create a sde CRS with a huge value range and 5 digits of presission
        SeCoordinateReference seCRS = TestData.getGenericCoordRef();

        for (int i = 0; i < expectedGeometries.length; i++) {
            expectedGeometry = expectedGeometries[i];
            sdeCoords = geometryToSdeCoords(expectedGeometry, seCRS);

            //geometryBuilder.newGeometry is a protected method
            //and should not be called directly. We use it here
            //just for testing purposes. Instead, geometryBuilder.construct(SeShape)
            //must be used
            createdGeometry = this.geometryBuilder.newGeometry(sdeCoords);
            assertEquals(expectedGeometry.getClass(), createdGeometry.getClass());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param jtsGeom DOCUMENT ME!
     * @param seCRS DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    private double[][][] geometryToSdeCoords(final Geometry jtsGeom,
        final SeCoordinateReference seCRS) throws SeException, IOException {
        int numParts;
        double[][][] sdeCoords;
        GeometryCollection gcol = null;

        if (jtsGeom instanceof GeometryCollection) {
            gcol = (GeometryCollection) jtsGeom;
        } else {
            Geometry[] geoms = { jtsGeom };
            gcol = new GeometryFactory().createGeometryCollection(geoms);
        }

        List allPoints = new ArrayList();
        numParts = gcol.getNumGeometries();

        int[] partOffsets = new int[numParts];
        Geometry geom;

        for (int currGeom = 0; currGeom < numParts; currGeom++) {
            partOffsets[currGeom] = allPoints.size();
            geom = gcol.getGeometryN(currGeom);

            Coordinate[] coords = geom.getCoordinates();

            for (int i = 0; i < coords.length; i++) {
                Coordinate c = coords[i];
                SDEPoint p = new SDEPoint(c.x, c.y);
                allPoints.add(p);
            }
        }

        SDEPoint[] points = new SDEPoint[allPoints.size()];
        allPoints.toArray(points);

        SeShape shape = new SeShape(seCRS);

        try {
            if (jtsGeom instanceof Point || gcol instanceof MultiPoint) {
                shape.generatePoint(points.length, points);
            } else if (jtsGeom instanceof LineString
                    || jtsGeom instanceof MultiLineString) {
                shape.generateLine(points.length, numParts, partOffsets, points);
            } else {
                shape.generatePolygon(points.length, numParts, partOffsets,
                    points);
            }
        } catch (SeException e) {
            LOGGER.warning(e.getSeError().getErrDesc());
            throw new DataSourceException(e.getSeError().getErrDesc() + ": "
                + jtsGeom, e);
        }

        sdeCoords = shape.getAllCoords();

        return sdeCoords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param coords DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private double[] toSdeCoords(Coordinate[] coords) {
        int nCoords = coords.length;
        double[] sdeCoords = new double[2 * nCoords];
        Coordinate c;

        for (int i = 0, j = 1; i < nCoords; i++, j += 2) {
            c = coords[i];
            sdeCoords[j - 1] = c.x;
            sdeCoords[j] = c.y;
        }

        return sdeCoords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param resource DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private Geometry[] loadTestData(final String resource)
        throws Exception {
        List testGeoms = new LinkedList();
        Geometry g;
        String line = null;

        try {
            LOGGER.fine("loading test data test-data/" + resource);

            InputStream in = org.geotools.resources.TestData.openStream(this, resource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("#") || "".equals(line)) {
                    continue;
                }

                g = this.wktReader.read(line);
                LOGGER.fine("loaded test geometry: " + g.toText());
                testGeoms.add(g);
            }
        } catch (ParseException ex) {
            LOGGER.severe("cant create a test geometry: " + ex.getMessage());
            throw ex;
        } catch (IOException ex) {
            LOGGER.severe("cant load test data " + resource + ": "
                + ex.getMessage());
            throw ex;
        }

        return (Geometry[]) testGeoms.toArray(new Geometry[0]);
    }

    /**
     * given a geometry class, tests that GeometryBuilder.defaultValueFor that
     * class returns an empty geometry of the same geometry class
     *
     * @param geometryClass DOCUMENT ME!
     */
    private void testGetDefaultValue(Class geometryClass) {
        Geometry geom = GeometryBuilder.defaultValueFor(geometryClass);
        assertNotNull(geom);
        assertTrue(geom.isEmpty());
        assertTrue(geometryClass.isAssignableFrom(geom.getClass()));
    }
}
