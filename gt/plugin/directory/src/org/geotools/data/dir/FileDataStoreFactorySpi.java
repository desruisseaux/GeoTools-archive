package org.geotools.data.dir;

import java.io.IOException;
import java.net.URL;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public interface FileDataStoreFactorySpi extends DataStoreFactorySpi {
    public String[] getFileExtensions();
    public boolean canProcess(URL f);
    public DataStore createDataStore(URL url) throws IOException ;
    public String getTypeName(URL url) throws IOException ;
}
