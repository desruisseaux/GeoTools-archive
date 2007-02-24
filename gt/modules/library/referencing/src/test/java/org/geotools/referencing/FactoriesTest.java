/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Projection;
import org.opengis.util.GenericName;
import org.opengis.util.ScopedName;

// Geotools dependencies
import org.geotools.referencing.factory.DatumAliases;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.GeotoolsFactory;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.operation.projection.MapProjection;
import org.geotools.resources.Arguments;


/**
 * Tests the creation of {@link CoordinateReferenceSystem} objects and dependencies through
 * factories (not authority factories).
 *
 * @source $URL$
 * @version $Id$
 */
public final class FactoriesTest extends TestCase {
    /**
     * The output stream. Will be overwriten by the {@link #main}
     * if the test is run from the command line.
     */
    private static PrintWriter out = new PrintWriter(new StringWriter());

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(FactoriesTest.class);
    }

    /**
     * Creates a new instance of <code>FactoriesTest</code>
     */
    public FactoriesTest(final String name) {
        super(name);
    }

    /**
     * Convenience method creating a map with only the "name" property.
     * This is the only mandatory property for object creation.
     */
    private static Map name(final String name) {
        return Collections.singletonMap("name", name);
    }

    /**
     * Tests the creation of new coordinate reference systems.
     *
     * @throws FactoryException if a coordinate reference system can't be created.
     */
    public void testCreation() throws FactoryException {
        out.println();
        out.println("Testing CRS creations");
        out.println("---------------------");
        out.println();
        out.println("create Coodinate Reference System....1: ");
        final         DatumFactory datumFactory = FactoryFinder.getDatumFactory        (null);
        final            CSFactory    csFactory = FactoryFinder.getCSFactory           (null);
        final           CRSFactory   crsFactory = FactoryFinder.getCRSFactory          (null);
        final MathTransformFactory    mtFactory = FactoryFinder.getMathTransformFactory(null);

        final Ellipsoid airy1830;
        final Unit meters = SI.METER;
        airy1830 = datumFactory.createEllipsoid(name("Airy1830"), 6377563.396, 6356256.910, meters);
        out.println();
        out.println("create Coodinate Reference System....2: ");
        out.println(airy1830.toWKT());

        final PrimeMeridian greenwich;
        final Unit degrees = NonSI.DEGREE_ANGLE;
        greenwich = datumFactory.createPrimeMeridian(name("Greenwich"), 0, degrees);
        out.println();
        out.println("create Coodinate Reference System....3: ");
        out.println(greenwich.toWKT());

        // NOTE: we could use the following pre-defined constant instead:
        //       DefaultPrimeMeridian.GREENWICH;
        final GeodeticDatum datum;
        datum = datumFactory.createGeodeticDatum(name("Airy1830"), airy1830, greenwich);
        out.println();
        out.println("create Coodinate Reference System....4: ");
        out.println(datum.toWKT());

        // NOTE: we could use the following pre-defined constant instead:
        //       DefaultEllipsoidalCS.GEODETIC_2D;
        final EllipsoidalCS ellCS;
        ellCS = csFactory.createEllipsoidalCS(name("Ellipsoidal"),
                csFactory.createCoordinateSystemAxis(name("Longitude"), "long", AxisDirection.EAST,  degrees),
                csFactory.createCoordinateSystemAxis(name("Latitude"),  "lat",  AxisDirection.NORTH, degrees));
        out.println();
        out.println("create Coodinate Reference System....5: ");
        out.println(ellCS); // No WKT for coordinate systems

        final GeographicCRS geogCRS;
        geogCRS = crsFactory.createGeographicCRS(name("Airy1830"), datum, ellCS);
        out.println();
        out.println("create Coodinate Reference System....6: ");
        out.println(geogCRS.toWKT());

        final MathTransform p;
        final ParameterValueGroup param = mtFactory.getDefaultParameters("Transverse_Mercator");
        param.parameter("semi_major")        .setValue(airy1830.getSemiMajorAxis());
        param.parameter("semi_minor")        .setValue(airy1830.getSemiMinorAxis());
        param.parameter("central_meridian")  .setValue(     49);
        param.parameter("latitude_of_origin").setValue(     -2);
        param.parameter("false_easting")     .setValue( 400000);
        param.parameter("false_northing")    .setValue(-100000);
        out.println();
        out.println("create Coodinate System....7: ");
        out.println(param);

        // NOTE: we could use the following pre-defined constant instead:
        //       DefaultCartesianCS.PROJECTED;
        final CartesianCS cartCS;
        cartCS = csFactory.createCartesianCS(name("Cartesian"),
                 csFactory.createCoordinateSystemAxis(name("Easting"),  "x", AxisDirection.EAST,  meters),
                 csFactory.createCoordinateSystemAxis(name("Northing"), "y", AxisDirection.NORTH, meters));
        out.println();
        out.println("create Coodinate Reference System....8: ");
        out.println(cartCS); // No WKT for coordinate systems
            
        final ProjectedCRS projCRS;
        projCRS = new FactoryGroup(datumFactory, csFactory, crsFactory, mtFactory).
             createProjectedCRS(name("Great_Britian_National_Grid"), geogCRS, null, param, cartCS);
        out.println();
        out.println("create Coodinate System....9: ");
        out.println(projCRS.toWKT());
    }

    /**
     * Tests all map projection creation.
     */
    public void testMapProjections() throws FactoryException {
        out.println();
        out.println("Testing classification names");
        out.println("----------------------------");
        final MathTransformFactory mtFactory = FactoryFinder.getMathTransformFactory(null);
        final Collection methods = mtFactory.getAvailableMethods(Projection.class);
        for (final Iterator it=methods.iterator(); it.hasNext();) {
            final OperationMethod    method = (OperationMethod) it.next();
            final String     classification = method.getName().getCode();
            final ParameterValueGroup param = mtFactory.getDefaultParameters(classification);
            try {
                param.parameter("semi_major").setValue(6377563.396);
                param.parameter("semi_minor").setValue(6356256.909237285);
            } catch (IllegalArgumentException e) {
                // Above parameters do not exists. Ignore.
            }
            final MathTransform mt;
            try {
                mt = mtFactory.createParameterizedTransform(param);
            } catch (FactoryException e) {
                // Probably not a map projection. This test is mostly about projection, so ignore.
                continue;
            } catch (UnsupportedOperationException e) {
                continue;
            }
            if (mt instanceof MapProjection) {
                /*
                 * Tests map projection properties. Some tests are ommitted for south-oriented
                 * map projections, since they are implemented as a concatenation of their North-
                 * oriented variants with an affine transform.
                 */
                out.println(classification);
                final boolean skip =
                        classification.equalsIgnoreCase("Transverse Mercator (South Orientated)") ||
                        classification.equalsIgnoreCase("Equidistant_Cylindrical");
                if (!skip) {
                    assertEquals(classification, ((MapProjection) mt).getParameterDescriptors().getName().getCode());
                }
                final ProjectedCRS projCRS =
                        new DefaultProjectedCRS("Test", method,
                            DefaultGeographicCRS.WGS84, mt, DefaultCartesianCS.PROJECTED);
                final Conversion conversion = projCRS.getConversionFromBase();
                assertSame(mt, conversion.getMathTransform());
                final OperationMethod projMethod = conversion.getMethod();
                assertEquals(classification, projMethod.getName().getCode());
            }
        }
    }

    /**
     * Tests datum aliases. Note: ellipsoid and prime meridian are dummy values just
     * (not conform to the usage in real world) just for testing purpose.
     */
    public void testDatumAliases() throws FactoryException {
        final String           name0 = "Nouvelle Triangulation Francaise (Paris)";
        final String           name1 = "Nouvelle_Triangulation_Francaise_Paris";
        final String           name2 = "NTF (Paris meridian)";
        final Ellipsoid    ellipsoid = DefaultEllipsoid.WGS84;
        final PrimeMeridian meridian = DefaultPrimeMeridian.GREENWICH;
        DatumFactory         factory = new GeotoolsFactory();
        final Map         properties = Collections.singletonMap("name", name1);
        GeodeticDatum datum = factory.createGeodeticDatum(properties, ellipsoid, meridian);
        assertTrue(datum.getAlias().isEmpty());

        for (int i=0; i<3; i++) {
            switch (i) {
                case  0: factory = new DatumAliases(factory);           break;
                case  1: factory = FactoryFinder.getDatumFactory(null); break;
                case  2: ((DatumAliases) factory).freeUnused();         break;
                default: throw new AssertionError(); // Should not occurs.
            }
            final String pass = "Pass #"+i;
            datum = factory.createGeodeticDatum(properties, ellipsoid, meridian);
            final GenericName[] aliases = (GenericName[]) datum.getAlias().toArray(new GenericName[0]);
            assertEquals(pass, 3, aliases.length);
            assertEquals(pass, name0, aliases[0].asLocalName().toString());
            assertEquals(pass, name1, aliases[1].asLocalName().toString());
            assertEquals(pass, name2, aliases[2].asLocalName().toString());
            assertTrue  (pass, aliases[0] instanceof ScopedName);
            assertTrue  (pass, aliases[1] instanceof ScopedName);
            assertTrue  (pass, aliases[2] instanceof ScopedName);
        }

        datum = factory.createGeodeticDatum(Collections.singletonMap("name", "Tokyo"), ellipsoid, meridian);
        Collection/*<GenericName>*/ aliases = datum.getAlias();
        assertEquals(4, aliases.size());

        ((DatumAliases) factory).freeUnused();
        datum = factory.createGeodeticDatum(Collections.singletonMap("name", "_toKyo  _"), ellipsoid, meridian);
        assertEquals(4, datum.getAlias().size());
        assertTrue(aliases.equals(datum.getAlias()));

        datum = factory.createGeodeticDatum(Collections.singletonMap("name", "D_Tokyo"), ellipsoid, meridian);
        assertEquals(4, datum.getAlias().size());

        datum = factory.createGeodeticDatum(Collections.singletonMap("name", "Luxembourg 1930"), ellipsoid, meridian);
        assertEquals(3, datum.getAlias().size());

        datum = factory.createGeodeticDatum(Collections.singletonMap("name", "Dummy"), ellipsoid, meridian);
        assertTrue("Non existing datum should have no alias.", datum.getAlias().isEmpty());

        datum = factory.createGeodeticDatum(Collections.singletonMap("name", "WGS 84"), ellipsoid, meridian);
        assertTrue (AbstractIdentifiedObject.nameMatches(datum, "WGS 84"));
        assertTrue (AbstractIdentifiedObject.nameMatches(datum, "WGS_1984"));
        assertTrue (AbstractIdentifiedObject.nameMatches(datum, "World Geodetic System 1984"));
        assertFalse(AbstractIdentifiedObject.nameMatches(datum, "WGS 72"));
    }

    /**
     * Run the test from the command line.
     * Options: {@code -verbose}.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        if (arguments.getFlag("-verbose")) try {
            out = arguments.out;
            final FactoriesTest test = new FactoriesTest(null);
            test.testCreation();
            test.testMapProjections();
        } catch (FactoryException exception) {
            exception.printStackTrace(arguments.err);
        } else {
            junit.textui.TestRunner.run(suite());
        }
    }
}
