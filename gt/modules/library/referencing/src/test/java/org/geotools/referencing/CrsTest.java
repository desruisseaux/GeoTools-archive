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

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.wkt.Parser;


/**
 * Tests the {@link CRS} utilities methods.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CrsTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
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
        assertTrue(oldEnvelope.equals  (firstEnvelope, 0.02));
    }
}
