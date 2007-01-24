/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
 */
package org.geotools.referencing.factory.epsg;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

// Geotools dependencies
import org.geotools.referencing.factory.URN_EPSG_Test;
import org.geotools.referencing.factory.OrderedAxisAuthorityFactoryTest;


/**
 * Performs all tests for the <code>org.geotools.referencing.factory.epsg</code> packages.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AllTests extends TestCase {
    /** No need to construct this class. */
    private AllTests() {
    }

    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        TestRunner.run(suite());
    }

    /**
     * Returns all suites.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("org.geotools.referencing.factory.epsg");
        suite.addTest(DefaultFactoryTest             .suite());
        suite.addTest(OperationFactoryTest           .suite());
        suite.addTest(OrderedAxisAuthorityFactoryTest.suite());
        suite.addTest(URN_EPSG_Test                  .suite());
        return suite;
    }
}
