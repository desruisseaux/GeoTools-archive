
package org.geotools.data.dir;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataSourceMetadataEnity;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFactorySpi.Param;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DirectoryDataStoreFactory implements DataStoreFactorySpi {

    public static final Param DIRECTORY = new Param("directory", File.class,
            "Directory containing gml files", true);
    public static final Param CREATE_SUFFIX_ORDER = new Param("suffix_list", String[].class,
            "space delimited list of prefixes to attempt and create new files for.", true);
    
    /**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(Map params) throws IOException {
        if(!canProcess(params))
            throw new IOException("Invalid parameters for DirectoryDataStore");
        File f = (File)DIRECTORY.lookUp(params);
        String[] strs = (String[])CREATE_SUFFIX_ORDER.lookUp(params);
        if(strs==null)
            throw new IOException("Invalid parameter "+CREATE_SUFFIX_ORDER.key+" : is null");
        if(!f.isDirectory())
            throw new IOException("Invalid parameter "+DIRECTORY.key+" : is not a valid directory");
        return new DirectoryDataStore(f,strs);
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createMetadata(java.util.Map)
     */
    public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
        File url = (File) DIRECTORY.lookUp( params );
        String parent = url.getParent();
        String name = url.getName();
        return new DataSourceMetadataEnity( parent, name, "Access to Directory "+url.toString());
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        if(!canProcess(params))
            throw new IOException("Invalid parameters for DirectoryDataStore");
        File f = (File)DIRECTORY.lookUp(params);
        if(f.exists())
            throw new IOException("Invalid parameter "+DIRECTORY.key+" : directory already exists");

        if(!f.isDirectory())
            throw new IOException("Invalid parameter "+DIRECTORY.key+" : is not a valid directory");

        if(!f.createNewFile())
            throw new IOException("Invalid parameter "+DIRECTORY.key+" : cannot create directory");
        String[] strs = (String[])CREATE_SUFFIX_ORDER.lookUp(params);
        if(strs==null)
            throw new IOException("Invalid parameter "+CREATE_SUFFIX_ORDER.key+" : is null");
        return new DirectoryDataStore(f,strs);
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDisplayName()
     */
    public String getDisplayName() {
        return "Directory DataStore";
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getDescription()
     */
    public String getDescription() {
        return "Propagates to multiple file types representing a directory";
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { DIRECTORY, CREATE_SUFFIX_ORDER};
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
     */
    public boolean canProcess(Map params) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

}
