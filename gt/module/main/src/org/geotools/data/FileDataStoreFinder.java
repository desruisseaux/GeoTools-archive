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
package org.geotools.data;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.factory.FactoryFinder;


/**
 * <p>
 * Most of this code was copied from DataStoreFinder.  See the Documentation
 * there for details.
 * </p>
 * 
 * <p>
 * This searches for DataStores which support a singular file  parsed in a
 * particular file format.
 * </p>
 *
 * @author dzwiers
 *
 * @see DataStoreFinder
 */
public class FileDataStoreFinder {
    /** The logger for the filter module. */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.data");

    private FileDataStoreFinder() {
    }

    /**
     * Checks each available datasource implementation in turn and returns the
     * first one which claims to support the resource identified by the params
     * object.
     *
     * @param url A Map object which contains a defenition of the resource to
     *        connect to. for file based resources the property 'url' should
     *        be set within this Map.
     *
     * @return The first datasource which claims to process the required
     *         resource, returns null if none can be found.
     *
     * @throws IOException If a suitable loader can be found, but it can not be
     *         attached to the specified resource without errors.
     */
    public static DataStore getDataStore(URL url) throws IOException {
        Iterator ps = getAvailableDataStores();

        while (ps.hasNext()) {
            FileDataStoreFactorySpi fac = (FileDataStoreFactorySpi) ps.next();

            try {
                if (fac.canProcess(url)) {
                    return fac.createDataStore(url);
                }
            } catch (Throwable t) {
                /**
                 * The logger for the filter module.
                 */
                LOGGER.log(Level.WARNING,
                    "Could not aquire " + fac.getDescription() + ":" + t, t);

                // Protect against DataStores that don't carefully
                // code canProcess
                continue;
            }
        }

        return null;
    }

    /**
     * Returns an iterator of FileDataStoreFactorySpi to allow for the easy
     * creation of a FileDataStore
     *
     * @return
     *
     * @see FileDataStoreFactorySpi
     * @see FileDataStore
     */
    public static Iterator getAvailableDataStores() {
        Set availableDS = new HashSet();
        Iterator it = FactoryFinder.factories(FileDataStoreFactorySpi.class);

        while (it.hasNext()) {
            FileDataStoreFactorySpi dsFactory = (FileDataStoreFactorySpi) it
                .next();

            if (dsFactory.isAvailable()) {
                availableDS.add(dsFactory);
            }
        }

        return availableDS.iterator();
    }
}
