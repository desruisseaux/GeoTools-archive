/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Test support for test cases which require an "online" resource, such as an
 * external server or database.
 * <p>
 * Online tests work off of a "fixture". A fixture is a properties file which
 * defines connection parameters for some remote service. Each online test case
 * must define the id of the fixture is uses with {@link #getFixtureId()}.
 * </p>
 * <p>
 * Fixtures are stored under the users home directory, under the ".geotools"
 * directory. In the event that a fixture does not exist, the test case is
 * aborted.
 * </p>
 * <p>
 * Online tests connect to remote / online resources. Test cases should do all
 * connection / disconnection in the {@link #connect} and {@link #disconnect()}
 * methods.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public abstract class OnlineTestCase extends TestCase {

    /**
     * The test fixture, null if the fixture is not available.
     */
    protected Properties fixture;

    /**
     * Loads the test fixture for the test case.
     * <p>
     * The fixture is obtained via {@link #getFixtureId()}.
     * </p>
     */
    protected final void setUp() throws Exception {
        // load the fixture
        File base = new File(System.getProperty("user.home") + File.separator
                + ".geotools");
        File fixtureFile = new File(base, getFixtureId().replace('.',
                File.separatorChar).concat(".properties"));

        if (fixtureFile.exists()) {
            InputStream input = new BufferedInputStream(new FileInputStream(
                    fixtureFile));
            try {
                fixture = new Properties();
                fixture.load(input);
            } finally {
                input.close();
            }

            // call the setUp template method
            try {
                connect();
            } catch (Throwable t) {
                // abort the test
                fixture = null;
            }

        }
    }

    /**
     * Tear down method for test, calls through to {@link #disconnect()} if the
     * test is active.
     */
    protected final void tearDown() throws Exception {
        if (fixture != null) {
            try {
                disconnect();
            } catch (Throwable t) {
                // do nothing
            }
        }
    }

    /**
     * Connection method, called from {@link #setUp()}.
     * <p>
     * Subclasses should do all initialization / connection here. In the event
     * of a connection not being available, this method should throw an
     * exception to abort the test case.
     * </p>
     * 
     * @throws Exception
     */
    protected void connect() throws Exception {
    }

    /**
     * Disconnection method, called from {@link #tearDown()}.
     * <p>
     * Subclasses should do all cleanup here.
     * </p>
     * 
     * @throws Exception
     */
    protected void disconnect() throws Exception {
    }

    /**
     * Override which checks if the fixture is available. If not the test is not
     * executed.
     */
    protected void runTest() throws Throwable {
        // if the fixture was loaded, run
        if (fixture != null) {
            super.runTest();
        }

        // otherwise do nothing
    }

    /**
     * The fixture id for the test case.
     * <p>
     * This name is hierachical, similar to a java package name. Example:
     * "postgis.demo_bc"
     * </p>
     * 
     * @return The fixture id.
     */
    protected abstract String getFixtureId();

}
