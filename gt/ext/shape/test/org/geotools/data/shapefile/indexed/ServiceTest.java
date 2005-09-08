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
package org.geotools.data.shapefile.indexed;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import java.util.HashMap;
import java.util.Iterator;


/**
 * DOCUMENT ME!
 *
 * @author ian
 */
public class ServiceTest extends TestCaseSupport {
    final String TEST_FILE = "statepop.shp";

    public ServiceTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite(ServiceTest.class));
    }

    /**
     * Make sure that the loading mechanism is working properly.
     */
    public void testIsAvailable() {
        Iterator list = DataStoreFinder.getAvailableDataStores();
        boolean found = false;

        while (list.hasNext()) {
            DataStoreFactorySpi fac = (DataStoreFactorySpi) list.next();

            if (fac instanceof IndexedShapefileDataStoreFactory) {
                found = true;
                assertNotNull(fac.getDescription());

                break;
            }
        }

        assertTrue("ShapefileDataSourceFactory not registered", found);
    }

    /**
     * Ensure that we can create a DataStore using url OR string url.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testShapefileDataStore() throws Exception {
        HashMap params = new HashMap();
        params.put("url", getTestResource(TEST_FILE));

        DataStore ds = DataStoreFinder.getDataStore(params);
        assertNotNull(ds);
        params.put("url", getTestResource(TEST_FILE).toString());
        assertNotNull(ds);
    }

    public void testBadURL() {
        HashMap params = new HashMap();
        params.put("url", "aaa://bbb.ccc");

        try {
            IndexedShapefileDataStoreFactory f = new IndexedShapefileDataStoreFactory();
            f.createDataStore(params);
            fail("did not throw error");
        } catch (java.io.IOException ioe) {
            // this is actually good
        }
    }
}
