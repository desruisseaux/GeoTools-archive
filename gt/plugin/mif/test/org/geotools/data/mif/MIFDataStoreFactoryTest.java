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
package org.geotools.data.mif;

import java.io.IOException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataStoreFactorySpi.Param;


/**
 * TestCase class for MIFDataStoreFactory
 *
 * @author Luca S. Percich, AMA-MI
 */
public class MIFDataStoreFactoryTest extends TestCase {
    private MIFDataStoreFactory dsFactory = null;

    /**
     */
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(new TestSuite(MIFDataStoreFactoryTest.class));
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        dsFactory = new MIFDataStoreFactory();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        dsFactory = null;
        super.tearDown();
    }

    /**
     */
    public void testGetDisplayName() {
        assertEquals("MIFDataStore", dsFactory.getDisplayName());
    }

    /**
     * Creates a MIFDataStore using DataStoreFinder
     */
    public void testDataStoreFinder() {
        DataStore ds = null;

        try {
            ds = DataStoreFinder.getDataStore(MIFTestUtils.getParams("mif",
                        MIFTestUtils.getDataPath()));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertNotNull(ds);
        assertEquals(true, ds.getClass() == MIFDataStore.class);
    }

    /**
     */
    public void testCreateDataStore() {
        DataStore ds = null;

        try {
            ds = dsFactory.createDataStore(MIFTestUtils.getParams("mif",
                        MIFTestUtils.getDataPath()));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertNotNull(ds);
        assertEquals(true, ds.getClass() == MIFDataStore.class);
    }

    /**
     */
    public void testGetDescription() {
        assertEquals("MapInfo MIF/MID format datastore",
            dsFactory.getDescription());
    }

    /**
     * Test the canProcess() method with different sets of (possibly wrong)
     * parameters
     */
    public void testCanProcessPath() {
        String dataPath = MIFTestUtils.getDataPath();

        // Opens the test-data directory
        assertEquals(true,
            dsFactory.canProcess(MIFTestUtils.getParams("mif", dataPath)));
    }

    /**
     */
    public void testCanProcessWrongDBType() {
        String dataPath = MIFTestUtils.getDataPath();

        // fails because dbtype != "mif"
        assertEquals(false,
            dsFactory.canProcess(MIFTestUtils.getParams("miffooobar", dataPath)));
    }

    /**
     */
    public void testCanProcessMIF() {
        String dataPath = MIFTestUtils.getDataPath();

        // Opens a single mif file; works with or without extension, and regardless the extension's case.
        assertEquals(true,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    dataPath + "grafo")));
        assertEquals(true,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    dataPath + "grafo.MIF")));
        assertEquals(true,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    dataPath + "grafo.mif")));
    }

    /**
     */
    public void testCanProcessWrongPath() {
        String dataPath = MIFTestUtils.getDataPath();

        // Fails because an extension other than ".mif" was specified
        assertEquals(false,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    dataPath + "grafo.zip")));

        // fails because the path is non-existent
        assertEquals(false,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    dataPath + "some/non/existent/path/")));
    }

    /**
     */
    public void testIsAvailable() {
        assertEquals(true, dsFactory.isAvailable());
    }

    /**
     */
    public void testGetParametersInfo() {
        Param[] pars = dsFactory.getParametersInfo();
        assertNotNull(pars);
        assertEquals(pars[2].key, MIFDataStore.PARAM_FIELDCASE);
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetImplementationHints() {
        assertNotNull(dsFactory.getImplementationHints());
    }
}
