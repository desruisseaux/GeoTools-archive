package org.geotools.data.gml;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

/**
 * <p> 
 * This creates GML DataStores based for the directory provided. By 
 * convention the name of the file x.gml represents the data type x.
 * </p>
 * @author dzwiers
 */
public class GMLDataStoreFactory implements DataStoreFactorySpi{

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(Map params) throws IOException {
        if(canProcess(params)){
            File dir = (File) params.get("directory");
            return new GMLDataStore(dir);
        }
        return null;
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        if(!canProcess(params))
            return null;
        File dir = (File) params.get("directory");
        if (dir.exists()) {
            throw new IOException(dir + " already exists");
        }
        boolean created;       
        created = dir.mkdir();
        if (!created) {
            throw new IOException("Could not create the directory" + dir);
        }
        return new GMLDataStore(dir);
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDescription()
     */
    public String getDescription() {
        return "Read only data store for validating gml 2.x data";
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        Param directory = new Param("directory", File.class,
            "Directory containing gml files", true);

        return new Param[] { directory, };
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
     */
    public boolean canProcess(Map params) {
        return (params != null) && params.containsKey("directory")
        && params.get("directory") instanceof File 
        && ((File)params.get("directory")).isDirectory();
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

}
