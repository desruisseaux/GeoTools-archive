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

import org.geotools.data.DataStoreFactorySpi.Param;
import java.io.IOException;
import java.util.Map;


/**
 * Exercise DB2DataStoreFactory.
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2DataStoreFactoryTest extends DB2TestCase {
    DB2DataStoreFactory factory = new DB2DataStoreFactory();

    public void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsAvailable() {
        assertTrue("isAvailable didn't return true", factory.isAvailable());
    }

    public void testCreateDataStore() {
        // Should succeed
        try {
            DB2DataStore dataStore = (DB2DataStore) factory.createDataStore(allParams);
        } catch (IOException e) {
            fail("createDataStore failed:" + e);
        }

        // Should fail
        try {
            Map params = copyParams(allParams);
            params.put("dbtype", "nodb");

            DB2DataStore dataStore = (DB2DataStore) factory.createDataStore(params);
            fail("createDataStore succeeded with invalid dbtype parameter");
        } catch (IOException e) {
            // We should come here as a result
        }
    }

    public void testCreateNewDataStore() {
        // Should fail
        try {
            DB2DataStore dataStore = (DB2DataStore) factory.createNewDataStore(allParams);
            fail("createNewDataStore didn't fail");
        } catch (UnsupportedOperationException e) {
            // We should come here as a result
        }
    }

    public void testGetDescription() {
        assertEquals("DB2 Data Store", factory.getDescription());
    }

    public void testGetDisplayName() {
        assertEquals("DB2", factory.getDisplayName());
    }

    public void testGetParametersInfo() {
        Param[] params = factory.getParametersInfo();
        int i = 0;

        try {
            for (i = 0; i < params.length; i++) {
                params[0].lookUp(allParams);
            }
        } catch (IOException e) {
            // should never get here
            fail("lookUp failed on " + params[i]);
        }

        // test for missing parameter
        Map paramMap = copyParams(allParams);
        paramMap.remove("dbtype");

        try {
            for (i = 0; i < params.length; i++) {
                params[0].lookUp(paramMap);
            }

            fail("Didn't fail on missing parameter dbtype");
        } catch (IOException e) {
            // should get here in successful case
        }
    }

    public void testCanProcess() {
        Map params;

        // Make sure it succeeds for all good parameters
        params = copyParams(allParams);
        assertTrue("all parameters valid - should have succeeded",
            factory.canProcess(params));

        // Should fail if "database" parameter is missing
        params = copyParams(allParams);
        params.remove("database");
        assertFalse("database parameter is required", factory.canProcess(params));

        // Should fail if "dbtype" parameter is not "db2"
        params = copyParams(allParams);
        params.put("dbtype", "nodb");
        assertFalse("dbtype parameter is required", factory.canProcess(params));

        // Should succeed if "dbname" parameter is not lowercase
        params = copyParams(allParams);
        params.put("dbtype", "DB2");
        assertTrue("should succeed with uppercase DB2 as dbtype",
            factory.canProcess(params));

        // Should fail if "host" parameter is missing
        params = copyParams(allParams);
        params.remove("host");
        assertFalse("host parameter is required", factory.canProcess(params));

        // Should fail if "port" parameter is missing
        params = copyParams(allParams);
        params.remove("port");
        assertFalse("port parameter is required", factory.canProcess(params));

        // Should succeed if "user" parameter is missing - not sure if it should be mandatory
        params = copyParams(allParams);
        params.remove("user");
        assertTrue("user parameter should be optional",
            factory.canProcess(params));

        // Should succeed if "passwd" parameter is missing - not sure if it should be mandatory
        params = copyParams(allParams);
        params.remove("passwd");
        assertTrue("passwd parameter should be optional",
            factory.canProcess(params));

        // Should fail if "user" parameter is missing - not sure if it should be mandatory
        params = copyParams(allParams);
        params.remove("tabschema");
        assertTrue("tabschema parameter should be optional",
            factory.canProcess(params));
    }
}
