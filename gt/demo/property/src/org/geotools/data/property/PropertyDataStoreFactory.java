package org.geotools.data.property;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;


public class PropertyDataStoreFactory implements DataStoreFactorySpi {
    
    public DataStore createDataStore(Map params) throws IOException {
        Object dirParam = params.get("directory");
	File dir = null;
	if(dir instanceof File){
	        dir = (File) dirParam;
	}else{
		dir = new File(String.valueOf(dirParam));
	}
        if( dir.exists() && dir.isDirectory() ){
            return new PropertyDataStore( dir );
        }
        else {
            throw new IOException("Directory is required");
        }
    }
    public DataStore createNewDataStore(Map params) throws IOException {
        File dir = (File) params.get("directory");
        if (dir.exists()) {
            throw new IOException(dir + " already exists");
        }

        boolean created;
        
        created = dir.mkdir();

        if (!created) {
            throw new IOException("Could not create the directory" + dir);
        }
        return new PropertyDataStore(dir);
    }
    public String getDisplayName() {
        return "Properties";
    }
    public String getDescription() {
        return "Allows access to Java Property files containing Feature information";
    }
//    public DataSourceMetadataEnity createMetadata( Map params )
//            throws IOException {
//        if( !canProcess( params )){
//            throw new IOException( "Provided params cannot be used to connect");
//        }
//        File dir = (File) DIRECTORY.lookUp( params );
//        return new DataSourceMetadataEnity( dir, "Property file access for " + dir );        
//    }    
    Param DIRECTORY = new Param("directory", File.class,
            "Directory containting property files", true);
    public Param[] getParametersInfo() {
        return new Param[] { DIRECTORY, };
    }
    
	/**
	 * Test to see if this datastore is available, if it has all the
	 * appropriate libraries to construct a datastore.  This datastore just
	 * returns true for now.  This method is used for gui apps, so as to
	 * not advertise data store capabilities they don't actually have.
	 *
	 * @return <tt>true</tt> if and only if this factory is available to create
	 *         DataStores.
	 *
	 * @task REVISIT: I'm just adding this method to compile, maintainer should
	 *       revisit to check for any libraries that may be necessary for
	 *       datastore creations. ch.
	 */
	public boolean isAvailable() {
		return true;
	}
    
    public boolean canProcess(Map params) {
        try {
            File file = (File) DIRECTORY.lookUp( params );
            return file.exists() && file.isDirectory();
        }
        catch ( Exception erp ){
            return false;
        }        
    }
}
