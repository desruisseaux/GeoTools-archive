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
import org.opengis.util.GenericName;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.referencing.crs.AbstractCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.AbstractCS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.cs.DefaultTimeCS;
import org.geotools.referencing.cs.DefaultVerticalCS;
import org.geotools.referencing.datum.AbstractDatum;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.datum.DefaultVerticalDatum;
import org.geotools.util.SimpleInternationalString;


/**
 * Tests the creation of {@link IdentifiedObject} and its subclasses. Some basic features and
 * simple <cite>Well Know Text</cite> (WKT) formatting are also tested.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BasicTest extends TestCase {
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
        return new TestSuite(BasicTest.class);
    }

    /**
     * Construct a test case.
     */
    public BasicTest(String testName) {
        super(testName);
    }

    /**
     * Tests {@link NamedIdentifier} attributes. Useful for making sure that the
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

        NamedIdentifier identifier = new NamedIdentifier(properties);
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
            identifier = new NamedIdentifier(properties);
            assertEquals("remarks", "Overrides remarks", identifier.getRemarks().toString(Locale.ENGLISH));
        }

        assertNotNull(properties.remove("authority"));
        assertNull   (properties.put("AutHOrITY", new CitationImpl("An other authority")));
        identifier = new NamedIdentifier(properties);
        assertEquals("authority", "An other authority", identifier.getAuthority().getTitle().toString(Locale.ENGLISH));

        assertNotNull(properties.remove("AutHOrITY"));
        assertNull   (properties.put("authority", Locale.CANADA));
        try {
            identifier = new NamedIdentifier(properties);
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
        final AbstractIdentifiedObject reference = new AbstractIdentifiedObject(
                properties, remaining, new String[] {"local"});
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
     * Test {@link AbstractReferenceSystem}.
     */
    public void testReferenceSystem() {
        final Map properties = new HashMap();
        assertNull(properties.put("name",       "This is a name"));
        assertNull(properties.put("scope",      "This is a scope"));
        assertNull(properties.put("scope_fr",   "Valide dans ce domaine"));
        assertNull(properties.put("remarks",    "There is remarks"));
        assertNull(properties.put("remarks_fr", "Voici des remarques"));

        final AbstractReferenceSystem reference = new AbstractReferenceSystem(properties);
        assertEquals("name",          "This is a name",         reference.getName()   .getCode());
        assertEquals("scope",         "This is a scope",        reference.getScope()  .toString(null));
        assertEquals("scope_fr",      "Valide dans ce domaine", reference.getScope()  .toString(Locale.FRENCH));
        assertEquals("remarks",       "There is remarks",       reference.getRemarks().toString(null));
        assertEquals("remarks_fr",    "Voici des remarques",    reference.getRemarks().toString(Locale.FRENCH));
    }

    /**
     * Tests {@link DefaultCoordinateSystemAxis} constants.
     */
    public void testAxis() {
        // Test Well Know Text
        assertEquals("x",         "AXIS[\"x\", EAST]",         DefaultCoordinateSystemAxis.X        .toWKT(0));
        assertEquals("y",         "AXIS[\"y\", NORTH]",        DefaultCoordinateSystemAxis.Y        .toWKT(0));
        assertEquals("z",         "AXIS[\"z\", UP]",           DefaultCoordinateSystemAxis.Z        .toWKT(0));
        assertEquals("Longitude", "AXIS[\"Longitude\", EAST]", DefaultCoordinateSystemAxis.LONGITUDE.toWKT(0));
        assertEquals("Latitude",  "AXIS[\"Latitude\", NORTH]", DefaultCoordinateSystemAxis.LATITUDE .toWKT(0));
        assertEquals("Altitude",  "AXIS[\"Altitude\", UP]",    DefaultCoordinateSystemAxis.ALTITUDE .toWKT(0));
        assertEquals("Time",      "AXIS[\"Time\", FUTURE]",    DefaultCoordinateSystemAxis.TIME     .toWKT(0));

        assertEquals("Longitude", "AXIS[\"Geodetic longitude\", EAST]",  DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE .toWKT(0));
        assertEquals("Longitude", "AXIS[\"Spherical longitude\", EAST]", DefaultCoordinateSystemAxis.SPHERICAL_LONGITUDE.toWKT(0));
        assertEquals("Latitude",  "AXIS[\"Geodetic latitude\", NORTH]",  DefaultCoordinateSystemAxis.GEODETIC_LATITUDE  .toWKT(0));
        assertEquals("Latitude",  "AXIS[\"Spherical latitude\", NORTH]", DefaultCoordinateSystemAxis.SPHERICAL_LATITUDE .toWKT(0));

        // Test localization
        assertEquals("English", "Time",  ((GenericName) DefaultCoordinateSystemAxis.TIME.getAlias().iterator().next()).toInternationalString().toString(Locale.ENGLISH));
        assertEquals("French",  "Temps", ((GenericName) DefaultCoordinateSystemAxis.TIME.getAlias().iterator().next()).toInternationalString().toString(Locale.FRENCH ));
        // TODO: remove cast and use static import once we are allowed to compile for J2SE 1.5.
        //       It will make the line much shorter!!

        // Test geocentric
        assertFalse("X",         DefaultCoordinateSystemAxis.X        .equals(DefaultCoordinateSystemAxis.GEOCENTRIC_X,        false));
        assertFalse("Longitude", DefaultCoordinateSystemAxis.LONGITUDE.equals(DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE,  false));
        assertFalse("Longitude", DefaultCoordinateSystemAxis.LONGITUDE.equals(DefaultCoordinateSystemAxis.SPHERICAL_LONGITUDE, false));
    }

    /**
     * Tests {@link AbstractCS}.
     */
    public void testCoordinateSystems() {
        // Test dimensions
        assertEquals("Cartesian 2D",   2, DefaultCartesianCS  .PROJECTED  .getDimension());
        assertEquals("Cartesian 3D",   3, DefaultCartesianCS  .GEOCENTRIC .getDimension());
        assertEquals("Ellipsoidal 2D", 2, DefaultEllipsoidalCS.GEODETIC_2D.getDimension());
        assertEquals("Ellipsoidal 3D", 3, DefaultEllipsoidalCS.GEODETIC_3D.getDimension());
        assertEquals("Vertical",       1, DefaultVerticalCS   .DEPTH      .getDimension());
        assertEquals("Temporal",       1, DefaultTimeCS       .DAYS       .getDimension());
    }

    /**
     * Test {@link AbstractDatum} and well-know text formatting.
     */
    public void testDatum() {
        // WGS84 components and equalities
        assertEquals("Ellipsoid",     DefaultEllipsoid.WGS84,         DefaultGeodeticDatum.WGS84.getEllipsoid());
        assertEquals("PrimeMeridian", DefaultPrimeMeridian.GREENWICH, DefaultGeodeticDatum.WGS84.getPrimeMeridian());
        assertFalse ("VerticalDatum", DefaultVerticalDatum.GEOIDAL.equals(DefaultVerticalDatum.ELLIPSOIDAL));
        assertEquals("Geoidal",       VerticalDatumType.GEOIDAL,     DefaultVerticalDatum.GEOIDAL    .getVerticalDatumType());
        assertEquals("Ellipsoidal",   VerticalDatumType.ELLIPSOIDAL, DefaultVerticalDatum.ELLIPSOIDAL.getVerticalDatumType());

        // Test WKT
        assertEquals("Ellipsoid",     "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]",  DefaultEllipsoid.WGS84          .toWKT(0));
        assertEquals("PrimeMeridian", "PRIMEM[\"Greenwich\", 0.0]",                     DefaultPrimeMeridian.GREENWICH  .toWKT(0));
        assertEquals("VerticalDatum", "VERT_DATUM[\"Geoidal\", 2005]",                  DefaultVerticalDatum.GEOIDAL    .toWKT(0));
        assertEquals("VerticalDatum", "VERT_DATUM[\"Ellipsoidal\", 2002]",              DefaultVerticalDatum.ELLIPSOIDAL.toWKT(0));
        assertEquals("GeodeticDatum", "DATUM[\"WGS84\", "+
                                      "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]]", DefaultGeodeticDatum.WGS84      .toWKT(0));

        // Test properties
        final Map properties = new HashMap();
        properties.put("name",          "This is a name");
        properties.put("scope",         "This is a scope");
        properties.put("scope_fr",      "Valide dans ce domaine");
        properties.put("remarks",       "There is remarks");
        properties.put("remarks_fr",    "Voici des remarques");

        DefaultGeodeticDatum datum = new DefaultGeodeticDatum(properties,
                DefaultEllipsoid.createEllipsoid("Test", 1000, 1000, SI.METER),
                new DefaultPrimeMeridian("Test", 12));

        assertEquals("name",          "This is a name",         datum.getName   ().getCode());
        assertEquals("scope",         "This is a scope",        datum.getScope  ().toString(null));
        assertEquals("scope_fr",      "Valide dans ce domaine", datum.getScope  ().toString(Locale.FRENCH));
        assertEquals("remarks",       "There is remarks",       datum.getRemarks().toString(null));
        assertEquals("remarks_fr",    "Voici des remarques",    datum.getRemarks().toString(Locale.FRENCH));
    }

    /**
     * Tests {@link AbstractCRS}.
     */
    public void testCoordinateReferenceSystems() {
        // Test dimensions
        assertEquals("WGS84 2D", 2, DefaultGeographicCRS.WGS84   .getCoordinateSystem().getDimension());
        assertEquals("WGS84 3D", 3, DefaultGeographicCRS.WGS84_3D.getCoordinateSystem().getDimension());

        // Test WKT
        assertEquals("WGS84", "GEOGCS[\"WGS84\", " +
                                "DATUM[\"WGS84\", "+
                                "SPHEROID[\"WGS84\", 6378137.0, 298.257223563]], "+
                                "PRIMEM[\"Greenwich\", 0.0], "+
                                "UNIT[\"degree\", 0.017453292519943295], "+
                                "AXIS[\"Geodetic longitude\", EAST], "+
                                "AXIS[\"Geodetic latitude\", NORTH]]",
                     DefaultGeographicCRS.WGS84.toWKT(0));
    }

    /**
     * Test serialization of various objects.
     */
    public void testSerialization() throws IOException, ClassNotFoundException {
        serialize(DefaultCoordinateSystemAxis.X);
        serialize(DefaultCoordinateSystemAxis.GEOCENTRIC_X);
        serialize(DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE);
        serialize(DefaultCartesianCS.PROJECTED);
        serialize(DefaultCartesianCS.GEOCENTRIC);
        serialize(DefaultEllipsoidalCS.GEODETIC_2D);
        serialize(DefaultEllipsoidalCS.GEODETIC_3D);
        serialize(DefaultGeodeticDatum.WGS84);
        serialize(DefaultPrimeMeridian.GREENWICH);
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
