/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.referencing;

import java.io.*;
import java.util.*;
import javax.units.SI;
import junit.framework.*;
import java.awt.geom.AffineTransform;

import org.geotools.parameter.*;
import org.geotools.referencing.*;
import org.geotools.referencing.cs.*;
import org.geotools.referencing.crs.*;
import org.geotools.referencing.wkt.*;
import org.geotools.referencing.datum.*;
import org.geotools.referencing.operation.*;
import org.geotools.referencing.operation.transform.*;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.referencing.operation.MathTransform;


/**
 * Test the creation of {@link Info} and its subclasses. Some basic features and
 * simple <cite>Well Know Text</cite> (WKT) formatting are also tested.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BasicTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        org.geotools.util.MonolineFormatter.initGeotools();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(BasicTest.class);
    }

    /**
     * Construct a test case.
     */
    public BasicTest(String testName) {
        super(testName);
    }

    /**
     * Tests {@link Identifier} attributes. Useful for making sure that the
     * hash code enumerated in the switch statement in the constructor have
     * the correct value.
     */
    public void testIdentifier() {
        Map properties = new HashMap();
        properties.put("code",          "This is a code");
        properties.put("codeSpace",     "This is a code space");
        properties.put("version",       "This is a version");
        properties.put("dummy",         "Doesn't matter");
        properties.put("remarks",       "There is remarks");
        properties.put("remarks_fr",    "Voici des remarques");
        properties.put("remarks_fr_CA", "Pareil");

        Identifier identifier = new Identifier(properties);
        assertEquals("code",          "This is a code",        identifier.getCode());
        assertEquals("codeSpace",     "This is a code space",  identifier.getCodeSpace());
        assertEquals("version",       "This is a version",     identifier.getVersion());
        assertEquals("remarks",       "There is remarks",      identifier.getRemarks(Locale.ENGLISH));
        assertEquals("remarks_fr",    "Voici des remarques",   identifier.getRemarks(Locale.FRENCH));
        assertEquals("remarks_fr_CA", "Pareil",                identifier.getRemarks(Locale.CANADA_FRENCH));
        assertEquals("remarks_fr_BE", "Voici des remarques",   identifier.getRemarks(new Locale("fr", "BE")));
    }

    /**
     * Test {@link ReferenceSystem}.
     */
    public void testReferenceSystem() {
        Map properties = new HashMap();
        properties.put("name",          "This is a name");
        properties.put("name_fr",       "Voici un nom");
        properties.put("scope",         "This is a scope");
        properties.put("scope_fr",      "Valide dans ce domaine");
        properties.put("remarks",       "There is remarks");
        properties.put("remarks_fr",    "Voici des remarques");

        ReferenceSystem reference = new ReferenceSystem(properties);
        assertEquals("name",          "This is a name",         reference.getName   (null));
        assertEquals("name_fr",       "Voici un nom",           reference.getName   (Locale.FRENCH));
        assertEquals("scope",         "This is a scope",        reference.getScope  (null));
        assertEquals("scope_fr",      "Valide dans ce domaine", reference.getScope  (Locale.FRENCH));
        assertEquals("remarks",       "There is remarks",       reference.getRemarks(null));
        assertEquals("remarks_fr",    "Voici des remarques",    reference.getRemarks(Locale.FRENCH));
    }

    /**
     * Tests {@link CoordinateSystemAxis} constants.
     */
    public void testAxis() {
        // Test Well Know Text
        assertEquals("x",         "AXIS[\"x\", EAST]",         CoordinateSystemAxis.X        .toWKT(0));
        assertEquals("y",         "AXIS[\"y\", NORTH]",        CoordinateSystemAxis.Y        .toWKT(0));
        assertEquals("z",         "AXIS[\"z\", UP]",           CoordinateSystemAxis.Z        .toWKT(0));
        assertEquals("Longitude", "AXIS[\"Longitude\", EAST]", CoordinateSystemAxis.LONGITUDE.toWKT(0));
        assertEquals("Latitude",  "AXIS[\"Latitude\", NORTH]", CoordinateSystemAxis.LATITUDE .toWKT(0));
        assertEquals("Altitude",  "AXIS[\"Altitude\", UP]",    CoordinateSystemAxis.ALTITUDE .toWKT(0));
        assertEquals("Time",      "AXIS[\"Time\", FUTURE]",    CoordinateSystemAxis.TIME     .toWKT(0));

        assertEquals("Longitude", "AXIS[\"Geodetic longitude\", EAST]",  CoordinateSystemAxis.GEODETIC_LONGITUDE .toWKT(0));
        assertEquals("Longitude", "AXIS[\"Spherical longitude\", EAST]", CoordinateSystemAxis.SPHERICAL_LONGITUDE.toWKT(0));
        assertEquals("Latitude",  "AXIS[\"Geodetic latitude\", NORTH]",  CoordinateSystemAxis.GEODETIC_LATITUDE  .toWKT(0));
        assertEquals("Latitude",  "AXIS[\"Spherical latitude\", NORTH]", CoordinateSystemAxis.SPHERICAL_LATITUDE .toWKT(0));

        // Test localization
        assertEquals("English", "Time",  CoordinateSystemAxis.TIME.getName(Locale.ENGLISH));
        assertEquals("French",  "Temps", CoordinateSystemAxis.TIME.getName(Locale.FRENCH ));

        // Test 
        assertFalse("X",         CoordinateSystemAxis.X        .equals(CoordinateSystemAxis.GEOCENTRIC_X,        false));
        assertFalse("Longitude", CoordinateSystemAxis.LONGITUDE.equals(CoordinateSystemAxis.GEODETIC_LONGITUDE,  false));
        assertFalse("Longitude", CoordinateSystemAxis.LONGITUDE.equals(CoordinateSystemAxis.SPHERICAL_LONGITUDE, false));
    }

    /**
     * Tests {@link CoordinateSystem}.
     */
    public void testCoordinateSystems() {
        // Test dimensions
        assertEquals("Cartesian 2D",   2, CartesianCS  .PROJECTED  .getDimension());
        assertEquals("Cartesian 3D",   3, CartesianCS  .GEOCENTRIC .getDimension());
        assertEquals("Ellipsoidal 2D", 2, EllipsoidalCS.GEODETIC_2D.getDimension());
        assertEquals("Ellipsoidal 3D", 3, EllipsoidalCS.GEODETIC_3D.getDimension());
        assertEquals("Vertical",       1, VerticalCS   .DEPTH      .getDimension());
        assertEquals("Temporal",       1, TemporalCS   .DAYS       .getDimension());
    }

    /**
     * Test {@link Datum} and well-know text formatting.
     */
    public void testDatum() {
        // WGS84 components and equalities
        assertEquals("Ellipsoid",     Ellipsoid    .WGS84,     GeodeticDatum.WGS84.getEllipsoid());
        assertEquals("PrimeMeridian", PrimeMeridian.GREENWICH, GeodeticDatum.WGS84.getPrimeMeridian());
        assertFalse ("VerticalDatum", VerticalDatum.GEOIDAL.equals(VerticalDatum.ELLIPSOIDAL));
        assertEquals("Geoidal",       VerticalDatumType.GEOIDAL,     VerticalDatum.GEOIDAL    .getVerticalDatumType());
        assertEquals("Ellipsoidal",   VerticalDatumType.ELLIPSOIDAL, VerticalDatum.ELLIPSOIDAL.getVerticalDatumType());

        // Test WKT
        assertEquals("Ellipsoid",     "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]",  Ellipsoid.WGS84          .toWKT(0));
        assertEquals("PrimeMeridian", "PRIMEM[\"Greenwich\", 0.0]",                     PrimeMeridian.GREENWICH  .toWKT(0));
        assertEquals("VerticalDatum", "VERT_DATUM[\"Geoidal\", 2005]",                  VerticalDatum.GEOIDAL    .toWKT(0));
        assertEquals("VerticalDatum", "VERT_DATUM[\"Ellipsoidal\", 2002]",              VerticalDatum.ELLIPSOIDAL.toWKT(0));
        assertEquals("GeodeticDatum", "DATUM[\"WGS84\", "+
                                      "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]]", GeodeticDatum.WGS84      .toWKT(0));

        // Test properties
        Map properties = new HashMap();
        properties.put("name",          "This is a name");
        properties.put("name_fr",       "Voici un nom");
        properties.put("scope",         "This is a scope");
        properties.put("scope_fr",      "Valide dans ce domaine");
        properties.put("remarks",       "There is remarks");
        properties.put("remarks_fr",    "Voici des remarques");

        GeodeticDatum datum = new GeodeticDatum(properties,
                                                Ellipsoid.createEllipsoid("Test", 1000, 1000, SI.METER),
                                                new PrimeMeridian("Test", 12));
        assertEquals("name",          "This is a name",         datum.getName   (null));
        assertEquals("name_fr",       "Voici un nom",           datum.getName   (Locale.FRENCH));
        assertEquals("scope",         "This is a scope",        datum.getScope  (null));
        assertEquals("scope_fr",      "Valide dans ce domaine", datum.getScope  (Locale.FRENCH));
        assertEquals("remarks",       "There is remarks",       datum.getRemarks(null));
        assertEquals("remarks_fr",    "Voici des remarques",    datum.getRemarks(Locale.FRENCH));
    }

    /**
     * Test {@link ParameterValue}.
     */
    public void testParameter() {
        assertEquals(   "intValue", 14,  new ParameterValue("Test", 14).intValue());
        assertEquals("doubleValue", 27,  new ParameterValue("Test", 27).doubleValue(), 0);
        assertEquals("doubleValue", 300, new ParameterValue("Test",  3, SI.METER).doubleValue(SI.CENTI(SI.METER)), 0);
    }

    /**
     * Tests {@link CoordinateReferenceSystem}.
     */
    public void testCoordinateReferenceSystems() {
        // Test dimensions
        assertEquals("WGS84 2D", 2, GeographicCRS.WGS84   .getCoordinateSystem().getDimension());
        assertEquals("WGS84 3D", 3, GeographicCRS.WGS84_3D.getCoordinateSystem().getDimension());

        // Test WKT
        assertEquals("WGS84", "GEOGCS[\"WGS84\", " +
                                "DATUM[\"WGS84\", "+
                                "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], "+
                                "PRIMEM[\"Greenwich\", 0.0], "+
                                "UNIT[\"degree\", 0.017453292519943295], "+
                                "AXIS[\"Geodetic longitude\", EAST], "+
                                "AXIS[\"Geodetic latitude\", NORTH]]",
                     GeographicCRS.WGS84.toWKT(0));
    }

    /**
     * Test WKT formatting of transforms backed by matrix.
     */
    public void testMatrix() {
        final Formatter  formatter = new Formatter(null);
        final GeneralMatrix matrix = new GeneralMatrix(4);
        matrix.setElement(0,2,  4);
        matrix.setElement(1,0, -2);
        matrix.setElement(2,3,  7);
        MathTransform transform = ProjectiveTransform.create(matrix);
        assertFalse(transform instanceof AffineTransform);
        formatter.append(transform);
        assertEquals(formatter.toString(), "PARAM_MT[\"Affine\", "          +
                                           "PARAMETER[\"num_row\", 4], "    +
                                           "PARAMETER[\"num_col\", 4], "    +
                                           "PARAMETER[\"elt_0_2\", 4.0], "  +
                                           "PARAMETER[\"elt_1_0\", -2.0], " +
                                           "PARAMETER[\"elt_2_3\", 7.0]]");
        matrix.setSize(3,3);
        transform = ProjectiveTransform.create(matrix);
        assertTrue(transform instanceof AffineTransform);
        formatter.clear();
        formatter.append(transform);
        assertEquals(formatter.toString(), "PARAM_MT[\"Affine\", "          +
                                           "PARAMETER[\"num_row\", 3], "    +
                                           "PARAMETER[\"num_col\", 3], "    +
                                           "PARAMETER[\"elt_0_2\", 4.0], "  +
                                           "PARAMETER[\"elt_1_0\", -2.0]]");
    }

    /**
     * Test serialization of various objects.
     */
    public void testSerialization() throws IOException, ClassNotFoundException {
        serialize(GeodeticDatum.WGS84);
        serialize(PrimeMeridian.GREENWICH);
        serialize(CartesianCS.PROJECTED);
        serialize(CartesianCS.GEOCENTRIC);
        serialize(EllipsoidalCS.GEODETIC_2D);
        serialize(EllipsoidalCS.GEODETIC_3D);
    }

    /**
     * Test the serialization of the given object.
     */
    private static void serialize(final Object object) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream out  = new ByteArrayOutputStream();
        final ObjectOutputStream    outs = new ObjectOutputStream(out);
        outs.writeObject(object);
        outs.close();

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        final Object test = in.readObject();
        in.close();

        assertEquals("Serialization", object, test);
    }
}
