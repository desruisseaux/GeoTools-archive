/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005. All rights reserved.
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
package org.geotools.data.db2;

import org.geotools.data.jdbc.JDBCDataStoreConfig;
import java.io.IOException;


/**
 * Exercise DB2DataStore.
 *
 * @author David Adler - IBM Corporation
 */
public class DB2DataStoreTest extends DB2TestCase {
    /**
     * Setup gets a database connection that will be live for the duration of
     * all tests.
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();
        pool = getLocalConnectionPool();
    }

    /**
     * Closes the database connection before we terminate.
     *
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        //		pool.close();
        super.tearDown();
    }

    public void testNewDBDataStore() throws Exception {
        JDBCDataStoreConfig config = new JDBCDataStoreConfig(tabSchema,
                tabSchema, 100000);

        try {
            DB2DataStore dataStore = new DB2DataStore(null, config, getDbURL());
            fail("new DB2DataStore should have failed for null connection pool");
        } catch (IOException e) {
        }

        DB2DataStore dataStore = null;

        try {
            dataStore = new DB2DataStore(pool, config, getDbURL());
        } catch (IOException e) {
            fail("new DB2DataStore shouldn't have failed");
        }

        assertEquals(getDbURL(), dataStore.getDbURL());
    }

    public void testEntity() throws Exception {
        DB2DataStore dataStore = getDataStore();
        String[] typeNames = dataStore.getTypeNames();
        int foundCount = 0;

        for (int i = 0; i < typeNames.length; i++) {
            if (typeNames[i].equals("Places")) {
                foundCount++;
            }

            if (typeNames[i].equals("Roads")) {
                foundCount++;
            }
        }

        assertTrue(foundCount == 2);
    }
}
