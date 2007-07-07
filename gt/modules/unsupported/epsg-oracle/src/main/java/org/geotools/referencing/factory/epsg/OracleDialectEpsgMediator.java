/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2007, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.referencing.factory.epsg;

import java.sql.Connection;

import javax.sql.DataSource;

import org.geotools.factory.Hints;
import org.geotools.referencing.factory.AbstractEpsgMediator;

/**
 * Mediator which delegates the creation of referencing objects to the
 * OracleDialectEpsgFactory.
 * 
 * @author Cory Horner (Refractions Research)
 */
public class OracleDialectEpsgMediator extends AbstractEpsgMediator {

    Hints hints;
    
    public OracleDialectEpsgMediator(int priority, Hints hints, DataSource datasource) {
        super( hints, datasource);
        this.hints = hints;
    }

    /**
     * Reinitialize an instance to be returned by the pool.
     */
    protected void activateWorker(Object obj) throws Exception {
        OracleDialectEpsgFactory factory = (OracleDialectEpsgFactory) obj;
        factory.connect();
    }

    /**
     * Destroys an instance no longer needed by the pool.
     */
    protected void destroyWorker(Object obj) throws Exception {
        OracleDialectEpsgFactory factory = (OracleDialectEpsgFactory) obj;
        factory.disconnect();
        factory.dispose();
        factory = null;
    }

    /**
     * Creates an instance that can be returned by the pool.
     */
    protected Object makeWorker() throws Exception {
        //DataSource datasource = HsqlEpsgDatabase.createDataSource();
        //Connection connection = datasource.getConnection();
        Connection connection = getConnection();
        //Hints hints = new Hints(Hints.BUFFER_POLICY, "none");     
        OracleDialectEpsgFactory factory = new OracleDialectEpsgFactory(hints, connection);
        return factory;
    }

    /**
     * Uninitialize an instance to be returned to the pool.
     */
    protected void passivateWorker(Object obj) throws Exception {
        // Each implementation has the choice of closing connections when they
        // are returned to the worker pool, or when the objects are destroyed.
        // In this implementation, we have chosen to keep connections open
        // during their idle time and close the connection when the worker is
        // evicted. If we wanted to change this, we would move the disconnect
        // statement to this method. Alternatively, we could also keep track
        // of the idle time, and configure the pool to validate idle workers
        // frequently. We would then do a check in the validateWorker method
        // to close the connection when a connection close threshold is hit,
        // prior to worker destruction.
    }

    /**
     * Ensures that the instance is safe to be returned by the pool.
     */
    protected boolean validateWorker(Object obj) {
        //TODO: ensure that the worker is no longer in use
        return true;
    }

}
