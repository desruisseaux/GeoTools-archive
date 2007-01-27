/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeConnection;


/**
 * Tests de functionality of a pool of ArcSDE connection objects over a live
 * ArcSDE database
 *
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL$
 * @version $Id$
 */
public class ArcSDEConnectionPoolTest extends TestCase {
    /** DOCUMENT ME! */
    private static Logger LOGGER = Logger.getLogger("org.geotools.data.sde");

    /** DOCUMENT ME! */
    private Map connectionParameters;

    /** DOCUMENT ME! */
    private ConnectionConfig connectionConfig = null;

    /** DOCUMENT ME! */
    private ArcSDEConnectionPool pool = null;

    /**
     * Creates a new ArcSDEConnectionPoolTest object.
     *
     * @param name DOCUMENT ME!
     */
    public ArcSDEConnectionPoolTest(String name) {
        super(name);
    }

    /**
     * loads {@code test-data/testparams.properties} to get connection parameters and
     * sets up a ConnectionConfig with them for tests to set up ArcSDEConnectionPool's
     * as requiered
     *
     * @throws Exception DOCUMENT ME!
     * @throws IllegalStateException DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();

        Properties conProps = new Properties();
        String propsFile = "testparams.properties";
        URL conParamsSource = org.geotools.test.TestData.url(this, propsFile);
        LOGGER.fine("loading connection parameters from "
            + conParamsSource.toExternalForm());

        InputStream in = conParamsSource.openStream();

        if (in == null) {
            throw new IllegalStateException("cannot find test params: "
                + conParamsSource.toExternalForm());
        }

        conProps.load(in);
        in.close();
        connectionParameters = conProps;

        //test that mandatory connection parameters are set
        try {
            connectionConfig = new ConnectionConfig(conProps);
        } catch (Exception ex) {
            throw new IllegalStateException(
                "No valid connection parameters found in "
                + conParamsSource.toExternalForm() + ": " + ex.getMessage());
        }
    }

    /**
     * closes the connection pool if it's still open
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        connectionConfig = null;

        if (pool != null) {
            pool.close();
        }

        pool = null;
        super.tearDown();
    }

    /**
     * Sets up a new ArcSDEConnectionPool with the connection parameters stored
     * in <code>connParams</code> and throws an exception if something goes
     * wrong
     *
     * @param connParams a set of connection parameters from where the new
     *        ArcSDEConnectionPool will connect to the SDE database and create
     *        connections
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException if the set of connection parameters are
     *         not propperly set
     * @throws NullPointerException if <code>connParams</code> is null
     * @throws DataSourceException if the pool can't create the connections
     *         with the passed arguments (i.e. can't connect to SDE database)
     */
    private ArcSDEConnectionPool createPool(Map connParams)
        throws IllegalArgumentException, NullPointerException, 
            DataSourceException {
        this.connectionConfig = new ConnectionConfig(connParams);
        LOGGER.fine("creating a new ArcSDEConnectionPool with "
            + connectionConfig);

        if (this.pool != null) {
            LOGGER.fine("pool already created, closing it");
            this.pool.close();
        }

        this.pool = new ArcSDEConnectionPool(connectionConfig);
        LOGGER.fine("pool created");

        return this.pool;
    }

    /**
     * tests that a connection to a live ArcSDE database can be established
     * with the parameters defined int testparams.properties, and a
     * ArcSDEConnectionPool can be properly setted up
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testConnect() throws IOException {
        LOGGER.fine("testing connection to the sde database");

        ConnectionPoolFactory pf = ConnectionPoolFactory.getInstance();
        ConnectionConfig congfig = null;

        congfig = new ConnectionConfig(connectionParameters);

        try {
            ArcSDEConnectionPool pool = pf.createPool(congfig);
            LOGGER.fine("connection succeed " + pool.getPoolSize()
                + " connections ready");
        } catch (DataSourceException ex) {
            throw ex;
        } finally {
            pf.clear(); //close and remove all pools
        }
    }
    /**
     * Checks that after creation the pool has the specified initial number of
     * connections.
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    public void testInitialCount()
        throws DataSourceException, UnavailableConnectionException {
        int MIN_CONNECTIONS = 2;
        int MAX_CONNECTIONS = 6;

        //override pool.minConnections and pool.maxConnections from
        //the configured parameters to test the connections' pool
        //availability
        Map params = new HashMap(this.connectionParameters);
        params.put(ConnectionConfig.MIN_CONNECTIONS_PARAM,
            new Integer(MIN_CONNECTIONS));
        params.put(ConnectionConfig.MAX_CONNECTIONS_PARAM,
            new Integer(MAX_CONNECTIONS));

        createPool(params);

        //check that after creation, the pool contains the minimun number
        //of connections specified
        assertEquals("after creation, the pool must contain the minimun number of connections specified",
            MIN_CONNECTIONS, this.pool.getPoolSize());
    }

    /**
     * Tests that the pool creation fails if a wrong set of parameters is
     * passed (i.e. maxConnections is lower than minConnections)
     *
     * @throws DataSourceException
     * @throws UnavailableConnectionException
     */
    public void testChecksLimits()
        throws DataSourceException, UnavailableConnectionException {
        int MIN_CONNECTIONS = 2;

        //override pool.minConnections and pool.maxConnections from
        //the configured parameters to test the connections' pool
        //availability
        Map params = new HashMap(this.connectionParameters);
        params.put(ConnectionConfig.MIN_CONNECTIONS_PARAM,
            new Integer(MIN_CONNECTIONS));
        params.put(ConnectionConfig.MAX_CONNECTIONS_PARAM, new Integer(1));

        //this MUST fail, since maxConnections is lower than minConnections
        try {
            LOGGER.fine(
                "testing parameters' sanity check at pool creation time");
            createPool(params);
            fail(
                "the connection pool creation must have failed since a wrong set of arguments was passed");
        } catch (IllegalArgumentException ex) {
            //it's ok, it is what's expected
            LOGGER.fine("pramams assertion passed");
        }
    }


    /**
     * tests that no more than pool.maxConnections connections can be created,
     * and once one connection is freed, it is ready to be used again.
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    public void testMaxConnections()
        throws DataSourceException, UnavailableConnectionException {
        final int MIN_CONNECTIONS = 2;
        final int MAX_CONNECTIONS = 2;

        Map params = new HashMap(this.connectionParameters);
        params.put(ConnectionConfig.MIN_CONNECTIONS_PARAM,
            new Integer(MIN_CONNECTIONS));
        params.put(ConnectionConfig.MAX_CONNECTIONS_PARAM,
            new Integer(MAX_CONNECTIONS));
        
        createPool(params);

        PooledConnection []conns = new PooledConnection[MAX_CONNECTIONS];
        //try to get the maximun number of connections specified, and do not
        //release anyone
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            conns[i] = pool.getConnection();
        }

        //now that the max number of connections is reached, the pool
        //should throw an UnavailableConnectionException
        try {
            this.pool.getConnection();
            fail(
                "since the max number of connections was reached, the pool should have throwed an UnavailableConnectionException");
        } catch (UnavailableConnectionException ex) {
            LOGGER.fine(
                "maximun number of connections reached, got an UnavailableConnectionException, it's OK");
        }

        //now, free one and check the same conection is returned on the
        //next call to getConnection()
        PooledConnection expected = conns[0];
        expected.close();

        PooledConnection conn = this.pool.getConnection();
        assertEquals(expected, conn);
    }

    /**
     * a null database name should not be an impediment to create the pool
     *
     * @throws DataSourceException
     */
    public void testCreateWithNullDBName() throws DataSourceException {
        Map params = new HashMap(this.connectionParameters);
        params.remove(ConnectionConfig.INSTANCE_NAME_PARAM);
        createPool(params);
    }

    /**
     * an empty database name should not be an impediment to create the pool
     *
     * @throws DataSourceException
     */
    public void testCreateWithEmptyDBName() throws DataSourceException {
        Map params = new HashMap(this.connectionParameters);
        params.put(ConnectionConfig.INSTANCE_NAME_PARAM, "");
        createPool(params);
    }
}
