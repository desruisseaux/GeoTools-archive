package org.geotools.data;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.factory.FactoryFinder;

public class DataAccessFinder {
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.data");

    /**
     * Finds all implemtaions of DataStoreFactory which have registered using
     * the services mechanism, and that have the appropriate libraries on the
     * classpath.
     *
     * @return An iterator over all discovered datastores which have registered
     *         factories, and whose available method returns true.
     */
    public static Iterator getAvailableDataStores() {
        Set availableDS = new HashSet();
        Iterator it = FactoryFinder.factories(DataAccessFactory.class);

        while (it.hasNext()) {
            DataAccessFactory dsFactory = (DataAccessFactory) it.next();

            if (dsFactory.isAvailable()) {
                availableDS.add(dsFactory);
            }
        }

        return availableDS.iterator();
    }
    
    public static DataAccess createAccess(Object bean) throws IOException {
        Iterator ps = getAvailableDataStores();

        while (ps.hasNext()) {
            DataAccessFactory fac = (DataAccessFactory) ps.next();

            try {
                if (fac.canAccess(bean)) {
                    return fac.createAccess(bean);
                }
            } catch (Throwable t) {
                LOGGER.log( Level.WARNING, "Could not acquire "+fac.getName()+":"+t, t );                
                continue;
            }
        }
        return null;
    }
    
    public static DataAccess createAccess(Map properties) throws IOException {
        Iterator ps = getAvailableDataStores();

        while (ps.hasNext()) {
            DataAccessFactory fac = (DataAccessFactory) ps.next();
            Object bean = fac.createAccessBean();
            {
                // apply as many properties as will fit
            }
            try {
                if (fac.canAccess(bean)) {
                    return fac.createAccess(bean);
                }
            } catch (Throwable t) {
                LOGGER.log( Level.WARNING, "Could not acquire "+fac.getName()+":"+t, t );                
                continue;
            }
        }
        return null;
    }
}
