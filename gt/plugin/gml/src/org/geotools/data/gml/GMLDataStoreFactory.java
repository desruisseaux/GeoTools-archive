package org.geotools.data.gml;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataSourceMetadataEnity;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

/**
 * <p> 
 * This creates GML DataStores based for the directory provided. By 
 * convention the name of the file x.gml represents the data type x.
 * </p>
 * @author dzwiers
 */
public class GMLDataStoreFactory implements DataStoreFactorySpi {

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(Map params) throws IOException {
        File file = (File) DIRECTORY.lookUp( params );
        if( file.exists() && file.isDirectory() ){        
            return new GMLDataStore( file );
        }
        else {
            throw new IOException( "Provided file was not a directory");
        }        
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        File file = (File) DIRECTORY.lookUp( params );
        if (file.exists()) {
            throw new IOException(file + " already exists");
        }
        boolean created;       
        created = file.mkdir();
        if (!created) {
            throw new IOException("Could not create the directory" + file);
        }
        return new GMLDataStore(file);
    }

    public String getDisplayName() {
        return "GML";
    }
    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDescription()
     */
    public String getDescription() {
        return "Read only data store for validating gml 2.x data";
    }
    public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
        File file = (File) DIRECTORY.lookUp( params );
        String parent = file.getParent();
        String name = file.getName();
        return new DataSourceMetadataEnity( parent, name, "Access to GML files in "+name );
    }
    public static final Param DIRECTORY = new Param("directory", File.class,
            "Directory containing gml files", true);
    /**
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { DIRECTORY, };
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
