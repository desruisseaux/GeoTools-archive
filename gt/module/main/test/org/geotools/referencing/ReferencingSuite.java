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
package org.geotools.referencing;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

// Test units
import org.geotools.referencing.*;


/**
 * Performs all tests for the <code>org.geotools.referencing</code> packages.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ReferencingSuite extends TestCase {
    /**
     * Construct a suite.
     */
    public ReferencingSuite(final String name) {
        super(name);
    }        

    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Returns all suites.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("All referencing tests");
//      suite.addTest(CodeListTest  .suite());
        suite.addTest(BasicTest     .suite());
        suite.addTest(MatrixTest    .suite());
        suite.addTest(ParameterTest .suite());
        return suite;
    }
}
