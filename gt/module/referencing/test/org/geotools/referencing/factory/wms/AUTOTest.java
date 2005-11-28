/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.wms;

// J2SE dependencies
import java.util.logging.Level;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.util.MonolineFormatter;


/**
 * Tests {@link WebAuthorityFactory}.
 * 
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class AUTOTest extends TestCase {
    /**
     * The factory to test.
     */
    private CRSAuthorityFactory factory;

    /**
     * Run the suite from the command line.
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
        return new TestSuite(AUTOTest.class);
    }

    /**
     * Initializes the factory to test.
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = new AutoCRSFactory();
    }

    /**
     * UDIG requires this to work.
     */
    public void test42001() throws FactoryException {
        CoordinateReferenceSystem utm = factory.createCoordinateReferenceSystem("AUTO:42001,0.0,0.0");
        assertNotNull("auto-utm", utm);
        assertSame(utm, factory.createObject("AUTO:42001,0.0,0.0"));
    }
}
