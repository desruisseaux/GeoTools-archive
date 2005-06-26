/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.util.logging.Level;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;

// Geotools dependencies
import org.geotools.referencing.operation.AuthorityBackedFactory;
import org.geotools.referencing.FactoryFinder;
import org.geotools.util.MonolineFormatter;
import org.geotools.resources.Arguments;


/**
 * Tests the usage of {@link CoordinateOperationFactory} with the help of the
 * EPSG database. Any EPSG plugin should fit. However, this test live in the
 * {@code plugin/epsg-hsql} module since the HSQL plugin is the only one which
 * is garantee to work on any machine running Maven.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class OperationFactoryTest extends TestCase {
    /**
     * Run the suite from the command line. If {@code "-log"} flag is specified on the
     * command-line, then the logger will be set to {@link Level#CONFIG}. This is usefull
     * for tracking down which data source is actually used.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        final boolean log = arguments.getFlag("-log");
        arguments.getRemainingArguments(0);
        MonolineFormatter.initGeotools(log ? Level.CONFIG : null);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(OperationFactoryTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public OperationFactoryTest(final String name) {
        super(name);
    }

    /**
     * Returns the first identifier for the specified object.
     */
    private static String getIdentifier(final IdentifiedObject object) {
        return ((Identifier) object.getIdentifiers().iterator().next()).getCode();
    }

    /**
     * Tests the creation of an operation from EPSG:4230 to EPSG:4326. They are the same
     * CRS than the one tested in {@link DefaultDataSourceTest#testTransformations}.
     */
    public void testCreate() throws FactoryException {
        final CRSAuthorityFactory       crsFactory;
        final CoordinateOperationFactory opFactory;
        final CoordinateReferenceSystem  sourceCRS;
        final CoordinateReferenceSystem  targetCRS;
        final CoordinateOperation        operation;

        crsFactory = FactoryFinder.getCRSAuthorityFactory("EPSG", null);
        opFactory  = FactoryFinder.getCoordinateOperationFactory(null);
        sourceCRS  = crsFactory.createCoordinateReferenceSystem("4230");
        targetCRS  = crsFactory.createCoordinateReferenceSystem("4326");
        operation  = opFactory.createOperation(sourceCRS, targetCRS);

        assertSame(sourceCRS, operation.getSourceCRS());
        assertSame(targetCRS, operation.getTargetCRS());
        assertTrue("EPSG authority factory not found.", opFactory instanceof AuthorityBackedFactory);
        assertEquals("1612", getIdentifier(operation)); // See comment in DefaultDataSourceTest.
    }
}
