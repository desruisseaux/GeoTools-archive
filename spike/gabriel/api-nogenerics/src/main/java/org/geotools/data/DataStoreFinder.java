/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;

/**
 * Borrowed from main module to keep the spike self contained.
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/main/src/main/java/org/geotools/data/DataStoreFinder.java $
 */
public final class DataStoreFinder {
    /** The logger for the filter module. */
    protected static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.data");

    /**
     * The service registry for this manager. Will be initialized only when
     * first needed.
     */
    private static FactoryRegistry registry;

    // Singleton pattern
    private DataStoreFinder() {
    }

    /**
     * @param params
     * @return
     * @throws IOException
     */
    public static DataStore getDataStore(Map params) throws IOException {
        FeatureData repository;
        repository = getDataRepository(params);
        if (repository instanceof DataStore) {
            return (DataStore) repository;
        }
        return null;
    }

    /**
     * Checks each available datasource implementation in turn and returns the
     * first one which claims to support the resource identified by the params
     * object.
     * 
     * @param params
     *            A Map object which contains a defenition of the resource to
     *            connect to. for file based resources the property 'url' should
     *            be set within this Map.
     * 
     * @return The first datasource which claims to process the required
     *         resource, returns null if none can be found.
     * 
     * @throws IOException
     *             If a suitable loader can be found, but it can not be attached
     *             to the specified resource without errors.
     */
    public static synchronized FeatureData getDataRepository(Map params)
            throws IOException {
        Iterator ps = getServiceRegistry().getServiceProviders(DataStoreFactorySpi.class, null,
                null);
        FeatureDataFactory fac;

        IOException canProcessButNotAvailable = null;
        while (ps.hasNext()) {
            fac = (FeatureDataFactory) ps.next();
            boolean canProcess = false;
            try {
                canProcess = fac.canProcess(params);
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Problem asking " + fac.getDisplayName()
                        + " if it can process request:" + t, t);
                // Protect against DataStores that don't carefully code
                // canProcess
                continue;
            }
            if (canProcess) {
                boolean isAvailable = false;
                try {
                    isAvailable = fac.isAvailable();
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Difficulity checking if " + fac.getDisplayName()
                            + " is available:" + t, t);
                    // Protect against DataStores that don't carefully code
                    // isAvailable
                    continue;
                }
                if (isAvailable) {
                    try {
                        return fac.createDataStore(params);
                    } catch (IOException couldNotConnect) {
                        canProcessButNotAvailable = couldNotConnect;
                        LOGGER.log(Level.WARNING, fac.getDisplayName()
                                + " should be used, but could not connect", couldNotConnect);
                    }
                } else {
                    canProcessButNotAvailable = new IOException(
                            fac.getDisplayName()
                                    + " should be used, but is not availble. Have you installed the required drivers or jar files?");
                    LOGGER.log(Level.WARNING, fac.getDisplayName()
                            + " should be used, but is not availble", canProcessButNotAvailable);
                }
            }
        }
        if (canProcessButNotAvailable != null) {
            throw canProcessButNotAvailable;
        }
        return null;
    }

    /**
     * Finds all implemtaions of DataStoreFactory which have registered using
     * the services mechanism, regardless weather it has the appropriate
     * libraries on the classpath.
     * 
     * @return An iterator over all discovered datastores which have registered
     *         factories
     */
    public static synchronized Iterator getAllDataStores() {
        Set availableDS = new HashSet(5);
        return getServiceRegistry().getServiceProviders(DataStoreFactorySpi.class, null, null);
    }

    /**
     * Finds all implemtaions of DataStoreFactory which have registered using
     * the services mechanism, and that have the appropriate libraries on the
     * classpath.
     * 
     * @return An iterator over all discovered datastores which have registered
     *         factories, and whose available method returns true.
     */
    public static synchronized Iterator getAvailableRepositories() {
        Set availableDS = new HashSet(5);
        Iterator it = getServiceRegistry().getServiceProviders(FeatureData.class, null,
                null);
        FeatureDataFactory dsFactory;
        while (it.hasNext()) {
            dsFactory = (FeatureDataFactory) it.next();

            if (dsFactory.isAvailable()) {
                availableDS.add(dsFactory);
            }
        }

        return availableDS.iterator();
    }

    /**
     * Finds all implemtaions of DataStoreFactory which have registered using
     * the services mechanism, and that have the appropriate libraries on the
     * classpath.
     * 
     * @return An iterator over all discovered datastores which have registered
     *         factories, and whose available method returns true.
     */
    public static synchronized Iterator getAvailableDataStores() {
        Set availableDS = new HashSet(5);
        Iterator it = getServiceRegistry().getServiceProviders(DataStoreFactorySpi.class, null,
                null);
        DataStoreFactorySpi dsFactory;
        while (it.hasNext()) {
            dsFactory = (DataStoreFactorySpi) it.next();

            if (dsFactory.isAvailable()) {
                availableDS.add(dsFactory);
            }
        }

        return availableDS.iterator();
    }

    /**
     * Returns the service registry. The registry will be created the first time
     * this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(DataStoreFinder.class);
        if (registry == null) {
            registry = new FactoryCreator(Arrays
                    .asList(new Class<?>[] { DataStoreFactorySpi.class }));
        }
        return registry;
    }

    /**
     * Scans for factory plug-ins on the application class path. This method is
     * needed because the application class path can theoretically change, or
     * additional plug-ins may become available. Rather than re-scanning the
     * classpath on every invocation of the API, the class path is scanned
     * automatically only on the first invocation. Clients can call this method
     * to prompt a re-scan. Thus this method need only be invoked by
     * sophisticated applications which dynamically make new plug-ins available
     * at runtime.
     */
    public static synchronized void scanForPlugins() {

        getServiceRegistry().scanForPlugins();

    }
}
