/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.resources.Arguments;
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
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;


/**
 * Test the creation of {@link CoordinateReferenceSystem} objects.
 *
 * @version $Id$
 */
public class CreationTest extends TestCase {
    /**
     * The output stream. Will be overwriten by the {@link #main}
     * if the test is run from the command line.
     */
    private static PrintWriter out = new PrintWriter(new StringWriter());

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(CreationTest.class);
    }

    /**
     * Creates a new instance of <code>CreationTest</code>
     */
    public CreationTest(final String name) {
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
     * Test the creation of new coordinate reference systems.
     *
     * @throws FactoryException if a coordinate reference system can't be created.
     */
    public void testCreation() throws FactoryException {
        out.println();
        out.println("create Coodinate Reference System....1: ");
        final         DatumFactory datumFactory = FactoryFinder.getDatumFactory();
        final            CSFactory    csFactory = FactoryFinder.getCSFactory();
        final           CRSFactory   crsFactory = FactoryFinder.getCRSFactory();
        final MathTransformFactory    mtFactory = FactoryFinder.getMathTransformFactory();

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
        //       org.geotools.referencing.datum.PrimeMeridian.GREENWICH;
        final GeodeticDatum datum;
        datum = datumFactory.createGeodeticDatum(name("Airy1830"), airy1830, greenwich);
        out.println();
        out.println("create Coodinate Reference System....4: ");
        out.println(datum.toWKT());

        // NOTE: we could use the following pre-defined constant instead:
        //       org.geotools.referencing.cs.EllipsoidalCS.GEODETIC_2D;
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
        p = mtFactory.createParameterizedTransform(param);
        out.println();
        out.println("create Coodinate System....7: ");
        out.println(p.toWKT());

        // NOTE: we could use the following pre-defined constant instead:
        //       org.geotools.referencing.cs.CartesianCS.PROJECTED;
        final CartesianCS cartCS;
        cartCS = csFactory.createCartesianCS(name("Cartesian"),
                 csFactory.createCoordinateSystemAxis(name("Easting"),  "x", AxisDirection.EAST,  meters),
                 csFactory.createCoordinateSystemAxis(name("Northing"), "y", AxisDirection.NORTH, meters));
        out.println();
        out.println("create Coodinate Reference System....8: ");
        out.println(cartCS); // No WKT for coordinate systems
            
        final ProjectedCRS projCRS;
        projCRS = crsFactory.createProjectedCRS(name("Great_Britian_National_Grid"), geogCRS, p, cartCS);
        out.println();
        out.println("create Coodinate System....9: ");
        out.println(projCRS.toWKT());
    }

    /**
     * Run the test from the command line.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        if (arguments.getFlag("-verbose")) try {
            out = arguments.out;
            final CreationTest test = new CreationTest(null);
            test.testCreation();
        } catch (FactoryException exception) {
            exception.printStackTrace(arguments.err);
        } else {
            junit.textui.TestRunner.run(suite());
        }
    }
}
