
package org.geotools.data.dir;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.factory.FactoryFinder;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
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
     * @param params A Map object which contains a defenition of the resource
     *        to connect to. for file based resources the property 'url'
     *        should be set within this Map.
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
            	/** The logger for the filter module. */
                LOGGER.log( Level.WARNING, "Could not aquire "+fac.getDescription()+":"+t, t );            	
                // Protect against DataStores that don't carefully
                // code canProcess
                
                continue;
            }
        }

        return null;
    }
    
    public static Iterator getAvailableDataStores() {
        Set availableDS = new HashSet();
        Iterator it = FactoryFinder.factories(FileDataStoreFactorySpi.class);
        while (it.hasNext()) {
            FileDataStoreFactorySpi dsFactory = (FileDataStoreFactorySpi) it.next();
            if (dsFactory.isAvailable()) {
                availableDS.add(dsFactory);
            }
        }
        return availableDS.iterator();
    }
}
