/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
 *    Created on June 21, 2002, 12:30 PM
 */
package org.geotools.data.jdbc;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Runs the filter tests.
 *
 * @author James Macgill<br>
 * @author Chris Holmes
 *
 * @task REVISIT: Is there still need for this with maven?  It runs everything
 *       that ends with Test.
 * @source $URL: http://gtsvn.refractions.net/geotools/trunk/gt/modules/library/jdbc/src/test/java/org/geotools/filter/SQLFilterSuite.java $
 */
public class SQLFilterSuite extends TestCase {
    public SQLFilterSuite(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("All filter tests");

        suite.addTestSuite(FilterToSQLTest.class);
        
        return suite;
    }
}
