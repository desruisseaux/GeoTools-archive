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

// J2SE dependencies and extensions
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.units.SI;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.crs.GeographicCRS;
import org.geotools.referencing.cs.CartesianCS;
import org.geotools.referencing.cs.CoordinateSystem;
import org.geotools.referencing.cs.CoordinateSystemAxis;
import org.geotools.referencing.cs.EllipsoidalCS;
import org.geotools.referencing.cs.TimeCS;
import org.geotools.referencing.cs.VerticalCS;
import org.geotools.referencing.datum.Datum;
import org.geotools.referencing.datum.Ellipsoid;
import org.geotools.referencing.datum.GeodeticDatum;
import org.geotools.referencing.datum.PrimeMeridian;
import org.geotools.referencing.datum.VerticalDatum;
import org.geotools.util.SimpleInternationalString;


/**
 * Tests the creation of {@link IdentifiedObject} and its subclasses. Some basic features and
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
        final Map properties = new HashMap();
        assertNull(properties.put("code",          "This is a code"));
        assertNull(properties.put("authority",     "This is an authority"));
        assertNull(properties.put("version",       "This is a version"));
        assertNull(properties.put("dummy",         "Doesn't matter"));
        assertNull(properties.put("remarks",       "There is remarks"));
        assertNull(properties.put("remarks_fr",    "Voici des remarques"));
        assertNull(properties.put("remarks_fr_CA", "Pareil"));

        Identifier identifier = new Identifier(properties);
        assertEquals("code",          "This is a code",        identifier.getCode());
        assertEquals("authority",     "This is an authority",  identifier.getAuthority().getTitle().toString());
        assertEquals("version",       "This is a version",     identifier.getVersion());
        assertEquals("remarks",       "There is remarks",      identifier.getRemarks().toString(Locale.ENGLISH));
        assertEquals("remarks_fr",    "Voici des remarques",   identifier.getRemarks().toString(Locale.FRENCH));
        assertEquals("remarks_fr_CA", "Pareil",                identifier.getRemarks().toString(Locale.CANADA_FRENCH));
        assertEquals("remarks_fr_BE", "Voici des remarques",   identifier.getRemarks().toString(new Locale("fr", "BE")));

        if (false) {
            // Disabled in order to avoid logging a warning (it disturb the JUnit output)
            properties.put("remarks", new SimpleInternationalString("Overrides remarks"));
            identifier = new Identifier(properties);
            assertEquals("remarks", "Overrides remarks", identifier.getRemarks().toString(Locale.ENGLISH));
        }

        assertNotNull(properties.remove("authority"));
        assertNull   (properties.put("AutHOrITY", new CitationImpl("An other authority")));
        identifier = new Identifier(properties);
        assertEquals("authority", "An other authority", identifier.getAuthority().getTitle().toString(Locale.ENGLISH));

        assertNotNull(properties.remove("AutHOrITY"));
        assertNull   (properties.put("authority", Locale.CANADA));
        try {
            identifier = new Identifier(properties);
            fail();
        } catch (InvalidParameterValueException exception) {
            // This is the expected exception
        }
    }

    /**
     * Test {@link IdentifiedObject}.
     */
    public void testIdentifiedObject() {
        final Map properties = new HashMap();
        assertNull(properties.put("name",             "This is a name"));
        assertNull(properties.put("remarks",          "There is remarks"));
        assertNull(properties.put("remarks_fr",       "Voici des remarques"));
        assertNull(properties.put("dummy",            "Doesn't matter"));
        assertNull(properties.put("dummy_fr",         "Rien d'intéressant"));
        assertNull(properties.put("local",            "A custom localized string"));
        assertNull(properties.put("local_fr",         "Une chaîne personalisée"));
        assertNull(properties.put("anchorPoint",      "Anchor point"));
        assertNull(properties.put("realizationEpoch", "Realization epoch"));
        assertNull(properties.put("validArea",        "Valid area"));

        final Map remaining = new HashMap();
        final IdentifiedObject reference = new IdentifiedObject(properties, remaining, new String[] {"local"});
        assertEquals("name",       "This is a name",         reference.getName().getCode());
        assertEquals("remarks",    "There is remarks",       reference.getRemarks().toString(null));
        assertEquals("remarks_fr", "Voici des remarques",    reference.getRemarks().toString(Locale.FRENCH));

        // Check extra properties
        assertEquals("Size:",    6,                    remaining.size());
        assertEquals("dummy",    "Doesn't matter",     remaining.get("dummy"));
        assertEquals("dummy_fr", "Rien d'intéressant", remaining.get("dummy_fr"));
        assertEquals("local",    "A custom localized string", ((InternationalString) remaining.get("local")).toString(null));
        assertEquals("local_fr", "Une chaîne personalisée",   ((InternationalString) remaining.get("local")).toString(Locale.FRENCH));
        assertFalse ("local_fr", remaining.containsKey("local_fr"));

        // Check the case of some special property keys
        assertEquals("anchorPoint",      "Anchor point",      remaining.get("anchorPoint"));
        assertEquals("realizationEpoch", "Realization epoch", remaining.get("realizationEpoch"));
        assertEquals("validArea",        "Valid area",        remaining.get("validArea"));
    }

    /**
     * Test {@link ReferenceSystem}.
     */
    public void testReferenceSystem() {
        final Map properties = new HashMap();
        assertNull(properties.put("name",       "This is a name"));
        assertNull(properties.put("scope",      "This is a scope"));
        assertNull(properties.put("scope_fr",   "Valide dans ce domaine"));
        assertNull(properties.put("remarks",    "There is remarks"));
        assertNull(properties.put("remarks_fr", "Voici des remarques"));

        final ReferenceSystem reference = new ReferenceSystem(properties);
        assertEquals("name",          "This is a name",         reference.getName()   .getCode());
        assertEquals("scope",         "This is a scope",        reference.getScope()  .toString(null));
        assertEquals("scope_fr",      "Valide dans ce domaine", reference.getScope()  .toString(Locale.FRENCH));
        assertEquals("remarks",       "There is remarks",       reference.getRemarks().toString(null));
        assertEquals("remarks_fr",    "Voici des remarques",    reference.getRemarks().toString(Locale.FRENCH));
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
        assertEquals("English", "Time",  CoordinateSystemAxis.TIME.getAlias()[0].toInternationalString().toString(Locale.ENGLISH));
        assertEquals("French",  "Temps", CoordinateSystemAxis.TIME.getAlias()[0].toInternationalString().toString(Locale.FRENCH ));

        // Test geocentric
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
        assertEquals("Temporal",       1, TimeCS       .DAYS       .getDimension());
    }

    /**
     * Test {@link Datum} and well-know text formatting.
     */
    public void testDatum() {
        // WGS84 components and equalities
        assertEquals("Ellipsoid",     Ellipsoid    .WGS84,     GeodeticDatum.WGS84.getEllipsoid());
        assertEquals("PrimeMeridian", PrimeMeridian.GREENWICH, GeodeticDatum.WGS84.getPrimeMeridian());
        assertFalse ("VerticalDatum", VerticalDatum.GEOIDAL .equals( VerticalDatum.ELLIPSOIDAL));
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
        final Map properties = new HashMap();
        properties.put("name",          "This is a name");
        properties.put("scope",         "This is a scope");
        properties.put("scope_fr",      "Valide dans ce domaine");
        properties.put("remarks",       "There is remarks");
        properties.put("remarks_fr",    "Voici des remarques");

        GeodeticDatum datum = new GeodeticDatum(properties,
                                                Ellipsoid.createEllipsoid("Test", 1000, 1000, SI.METER),
                                                new PrimeMeridian("Test", 12));

        assertEquals("name",          "This is a name",         datum.getName   ().getCode());
        assertEquals("scope",         "This is a scope",        datum.getScope  ().toString(null));
        assertEquals("scope_fr",      "Valide dans ce domaine", datum.getScope  ().toString(Locale.FRENCH));
        assertEquals("remarks",       "There is remarks",       datum.getRemarks().toString(null));
        assertEquals("remarks_fr",    "Voici des remarques",    datum.getRemarks().toString(Locale.FRENCH));
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
     * Test serialization of various objects.
     */
    public void testSerialization() throws IOException, ClassNotFoundException {
        serialize(CoordinateSystemAxis.X);
        serialize(CoordinateSystemAxis.GEOCENTRIC_X);
        serialize(CoordinateSystemAxis.GEODETIC_LONGITUDE);
        serialize(CartesianCS.PROJECTED);
        serialize(CartesianCS.GEOCENTRIC);
        serialize(EllipsoidalCS.GEODETIC_2D);
        serialize(EllipsoidalCS.GEODETIC_3D);
        serialize(GeodeticDatum.WGS84);
        serialize(PrimeMeridian.GREENWICH);
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
        assertEquals("Serialization", object.hashCode(), test.hashCode());
    }
}
