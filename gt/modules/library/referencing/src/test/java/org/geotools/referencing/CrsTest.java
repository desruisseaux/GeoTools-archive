/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing;

import java.util.Set;
import java.awt.geom.Rectangle2D;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;


/**
 * Tests the {@link CRS} utilities methods.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CrsTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(CrsTest.class);
    }

    /**
     * Constructs a test case.
     */
    public CrsTest(String testName) {
        super(testName);
    }

    /**
     * Tests the {@link CRS#getSupportedAuthorities} method.
     */
    public void testSupportedAuthorities() {
        final Set<String> withoutAlias = CRS.getSupportedAuthorities(false);
        assertTrue (withoutAlias.contains("CRS"));
        assertTrue (withoutAlias.contains("AUTO2"));
        assertTrue (withoutAlias.contains("urn:ogc:def"));
        assertTrue (withoutAlias.contains("http://www.opengis.net"));
        assertFalse(withoutAlias.contains("AUTO"));
        assertFalse(withoutAlias.contains("urn:x-ogc:def"));

        final Set<String> withAlias = CRS.getSupportedAuthorities(true);
        assertTrue (withAlias.containsAll(withoutAlias));
        assertFalse(withoutAlias.containsAll(withAlias));
        assertTrue (withAlias.contains("AUTO"));
        assertTrue (withAlias.contains("urn:x-ogc:def"));
    }

    /**
     * Tests simple decode.
     */
    public void testDecode() throws FactoryException {
        assertSame(DefaultGeographicCRS.WGS84, CRS.decode("WGS84(DD)"));
    }

    /**
     * Tests an ESRI code.
     */
    public void XtestESRICode() throws Exception {
        String wkt = "PROJCS[\"Albers_Conic_Equal_Area\",\n"                  +
                     "  GEOGCS[\"GCS_North_American_1983\",\n"                +
                     "    DATUM[\"D_North_American_1983\",\n"                 +
                     "    SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],\n" +
                     "    PRIMEM[\"Greenwich\",0.0],\n"                       +
                     "    UNIT[\"Degree\",0.0174532925199433]],\n"            +
                     "  PROJECTION[\"Equidistant_Conic\"],\n"                 +
                     "  PARAMETER[\"False_Easting\",0.0],\n"                  +
                     "  PARAMETER[\"False_Northing\",0.0],\n"                 +
                     "  PARAMETER[\"Central_Meridian\",-96.0],\n"             +
                     "  PARAMETER[\"Standard_Parallel_1\",33.0],\n"           +
                     "  PARAMETER[\"Standard_Parallel_2\",45.0],\n"           +
                     "  PARAMETER[\"Latitude_Of_Origin\",39.0],\n"            +
                     "  UNIT[\"Meter\",1.0]]";
        CoordinateReferenceSystem crs = CRS.parseWKT(wkt);
        final CoordinateReferenceSystem WGS84  = DefaultGeographicCRS.WGS84;
        final MathTransform crsTransform = CRS.findMathTransform(WGS84, crs, true);
        assertFalse(crsTransform.isIdentity());
    }

    /**
     * Tests the transformations of an envelope.
     */
    public void testEnvelopeTransformation() throws FactoryException, TransformException {
        String wkt = "PROJCS[\"NAD_1983_UTM_Zone_10N\",\n"                  +
                     "  GEOGCS[\"GCS_North_American_1983\",\n"              +
                     "    DATUM[\"D_North_American_1983\",\n"               +
                     "    SPHEROID[\"GRS_1980\",6378137,298.257222101]],\n" +
                     "    PRIMEM[\"Greenwich\",0],\n"                       +
                     "    UNIT[\"Degree\",0.017453292519943295]],\n"        +
                     "  PROJECTION[\"Transverse_Mercator\"],\n"             +
                     "  PARAMETER[\"False_Easting\",500000],\n"             +
                     "  PARAMETER[\"False_Northing\",0],\n"                 +
                     "  PARAMETER[\"Central_Meridian\",-123],\n"            +
                     "  PARAMETER[\"Scale_Factor\",0.9996],\n"              +
                     "  PARAMETER[\"Latitude_Of_Origin\",0],\n"             +
                     "  UNIT[\"Meter\",1]]";
        final CoordinateReferenceSystem mapCRS = CRS.parseWKT(wkt);
        final CoordinateReferenceSystem WGS84  = DefaultGeographicCRS.WGS84;
        final MathTransform crsTransform = CRS.findMathTransform(WGS84, mapCRS, true);
        assertFalse(crsTransform.isIdentity());

        final GeneralEnvelope firstEnvelope, transformedEnvelope, oldEnvelope;
        firstEnvelope = new GeneralEnvelope(new double[] {-124, 42}, new double[] {-122, 43});
        firstEnvelope.setCoordinateReferenceSystem(WGS84);

        transformedEnvelope = CRS.transform(crsTransform, firstEnvelope);
        transformedEnvelope.setCoordinateReferenceSystem(mapCRS);

        oldEnvelope = CRS.transform(crsTransform.inverse(), transformedEnvelope);
        oldEnvelope.setCoordinateReferenceSystem(WGS84);

        assertTrue(oldEnvelope.contains(firstEnvelope, true));
        assertTrue(oldEnvelope.equals  (firstEnvelope, 0.02, true));
    }

    /**
     * Tests the transformations of a rectangle using a coordinate operation.
     * With assertions enabled, this also test the transformation of an envelope.
     */
    public void testTransformationOverPole() throws FactoryException, TransformException {
        String wkt = "PROJCS[\"WGS 84 / Antarctic Polar Stereographic\",\n"     +
                     "  GEOGCS[\"WGS 84\",\n"                                   +
                     "    DATUM[\"World Geodetic System 1984\",\n"              +
                     "      SPHEROID[\"WGS 84\", 6378137.0, 298.257223563]],\n" +
                     "    PRIMEM[\"Greenwich\", 0.0],\n"                        +
                     "    UNIT[\"degree\", 0.017453292519943295]],\n"           +
                     "  PROJECTION[\"Polar Stereographic (variant B)\"],\n"     +
                     "  PARAMETER[\"standard_parallel_1\", -71.0],\n"           +
                     "  UNIT[\"m\", 1.0]]";
        final CoordinateReferenceSystem mapCRS = CRS.parseWKT(wkt);
        final CoordinateReferenceSystem WGS84  = DefaultGeographicCRS.WGS84;
        final CoordinateOperation operation =
                CRS.getCoordinateOperationFactory(false).createOperation(mapCRS, WGS84);
        final MathTransform transform = operation.getMathTransform();
        assertTrue(transform instanceof MathTransform2D);
        /*
         * The rectangle to test, which contains the South pole.
         */
        Rectangle2D envelope = XRectangle2D.createFromExtremums(
                -3943612.4042124213, -4078471.954436003,
                 3729092.5890516187,  4033483.085688618);
        /*
         * This is what we get without special handling of singularity point.
         * Note that is doesn't include the South pole as we would expect.
         */
        Rectangle2D expected = XRectangle2D.createFromExtremums(
                -178.49352310409273, -88.99136583196398,
                 137.56220967463082, -40.905775004205864);
        /*
         * Tests what we actually get.
         */
        Rectangle2D actual = CRS.transform((MathTransform2D) transform, envelope, null);
        assertTrue(XRectangle2D.equalsEpsilon(expected, actual));
        /*
         * Using the transform(CoordinateOperation, ...) method,
         * the singularity at South pole is taken in account.
         */
        expected = XRectangle2D.createFromExtremums(-180, -90, 180, -40.905775004205864);
        actual = CRS.transform(operation, envelope, actual);
        assertTrue(XRectangle2D.equalsEpsilon(expected, actual));
        /*
         * The rectangle to test, which contains the South pole, but this time the south
         * pole is almost in a corner of the rectangle
         */
        envelope = XRectangle2D.createFromExtremums(-4000000, -4000000, 300000, 30000);
        expected = XRectangle2D.createFromExtremums(-180, -90, 180, -41.03163170198091);
        actual = CRS.transform(operation, envelope, actual);
        assertTrue(XRectangle2D.equalsEpsilon(expected, actual));
    }
}
