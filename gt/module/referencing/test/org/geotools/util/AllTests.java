/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.util;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Performs all tests for the <code>org.geotools.util</code> packages. This
 * also includes tests in some dependencies, like <code>org.geotools.resources</code>
 * package.
 *
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
        org.geotools.util.MonolineFormatter.initGeotools();
        TestRunner.run(suite());
    }

    /**
     * Returns all suites.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("org.geotools.util");
        suite.addTest(org.geotools.resources.AllTests.suite());
        suite.addTest(MonolineFormatterTest          .suite());
        suite.addTest(InternationalStringTest        .suite());
        suite.addTest(RangeSetTest                   .suite());
        suite.addTest(DisjointSetTest                .suite());
        suite.addTest(WeakHashSetTest                .suite());
        suite.addTest(WeakValueHashMapTest           .suite());
        return suite;
    }
}
