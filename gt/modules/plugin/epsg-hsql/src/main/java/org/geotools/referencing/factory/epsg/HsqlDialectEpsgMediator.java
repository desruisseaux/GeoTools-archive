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
 * HsqlDialectEpsgFactory.
 * 
 * @author Cory Horner (Refractions Research)
 */
public class HsqlDialectEpsgMediator extends AbstractEpsgMediator {

    public HsqlDialectEpsgMediator(int priority, Hints hints, DataSource datasource) {
        super(priority, hints, datasource);
    }

    /**
     * Reinitialize an instance to be returned by the pool.
     */
    protected void activateWorker(Object obj) throws Exception {
        //HsqlDialectEpsgFactory factory = (HsqlDialectEpsgFactory) obj;
        //factory.connect();
    }

    /**
     * Destroys an instance no longer needed by the pool.
     */
    protected void destroyWorker(Object obj) throws Exception {
        HsqlDialectEpsgFactory factory = (HsqlDialectEpsgFactory) obj;
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
        Hints hints = new Hints(Hints.BUFFER_POLICY, "none");     
        HsqlDialectEpsgFactory factory = new HsqlDialectEpsgFactory(hints, connection);
        return factory;
    }

    /**
     * Uninitialize an instance to be returned to the pool.
     */
    protected void passivateWorker(Object obj) throws Exception {
        //HsqlDialectEpsgFactory factory = (HsqlDialectEpsgFactory) obj;
        //factory.disconnect();
    }

    /**
     * Ensures that the instance is safe to be returned by the pool.
     */
    protected boolean validateWorker(Object obj) {
        //TODO: ensure that the worker is no longer in use
        return true;
    }

}
