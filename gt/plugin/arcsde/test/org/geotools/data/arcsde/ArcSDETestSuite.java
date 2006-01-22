/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.arcsde;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * geotools2 ArcSDE test suite
 *
 * @author Gabriel Rold?n
 * @source $URL$
 * @version $Id$
 */
public class ArcSDETestSuite extends TestCase {
    /**
     * Creates a new SdeTestSuite object.
     *
     * @param s suite's name
     */
    public ArcSDETestSuite(String s) {
        super(s);
    }

    /**
     * adds and returns all arcsde datasource relates tests
     *
     * @return test suite for sde datasource
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(ArcSDEJavaApiTest.class);
        suite.addTestSuite(GeometryBuilderTest.class);
        suite.addTestSuite(ArcSDEQueryTest.class);
        suite.addTestSuite(ArcSDEConnectionPoolTest.class);
        suite.addTestSuite(ArcSDEDataStoreTest.class);
        suite.addTestSuite(ArcSDEFeatureStoreTest.class);

        return suite;
    }
}
