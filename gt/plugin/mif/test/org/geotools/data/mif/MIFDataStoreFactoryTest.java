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

import junit.framework.TestCase;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi.Param;
import java.io.IOException;
import java.util.HashMap;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 */
public class MIFDataStoreFactoryTest extends TestCase {
    private MIFDataStoreFactory dsFactory = null;

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
        super.tearDown();
        dsFactory = null;
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetDisplayName() {
        assertEquals("MIFDataStore", dsFactory.getDisplayName());
    }

    /**
     * DOCUMENT ME!
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
     * DOCUMENT ME!
     */
    public void testGetDescription() {
        assertEquals("MapInfo MIF/MID format datastore",
            dsFactory.getDescription());
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcess() {
        assertEquals(true,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    MIFTestUtils.getDataPath())));
        assertEquals(false,
            dsFactory.canProcess(MIFTestUtils.getParams("miffooobar",
                    MIFTestUtils.getDataPath())));
        assertEquals(false,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    MIFTestUtils.getDataPath() + "/some/non/existent/path/")));
    }

    /**
     * DOCUMENT ME!
     */
    public void testIsAvailable() {
        assertEquals(true, dsFactory.isAvailable());
    }

    /**
     * DOCUMENT ME!
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
        // TODO Hints still missing
    }
}
