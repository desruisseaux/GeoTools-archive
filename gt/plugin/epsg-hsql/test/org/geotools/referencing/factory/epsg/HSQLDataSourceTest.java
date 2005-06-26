/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
import java.sql.SQLException;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.util.MonolineFormatter;
import org.geotools.resources.Arguments;



/**
 * Tests transformations from CRS and/or operations created from the EPSG factory, using HSQL
 * plugin. This class performs exactly the same tests than {@link DefaultDataSourceTest}, but
 * on the HSQL plugin instead of the default (MS-Access if available) one.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Vadim Semenov
 */
public class HSQLDataSourceTest extends DefaultDataSourceTest {
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
        return new TestSuite(HSQLDataSourceTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public HSQLDataSourceTest(final String name) {
        super(name);
    }

    /**
     * Sets up the authority factory. This setup process does not rely on FactoryFinder.
     * We rely want to test the HSQL implementation, not an arbitrary implementation.
     */
    protected void setUp() throws SQLException {
        // Do not call super.setUp()
        factory = new DefaultFactory();
        factory.setDataSource(new HSQLDataSource());
    }
}
