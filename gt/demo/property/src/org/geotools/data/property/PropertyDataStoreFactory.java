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
package org.geotools.data.property;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @author jgarnett
 * @version $Id$
 */
public class PropertyDataStoreFactory implements DataStoreFactorySpi {
    //    public DataSourceMetadataEnity createMetadata( Map params )
    //            throws IOException {
    //        if( !canProcess( params )){
    //            throw new IOException( "Provided params cannot be used to connect");
    //        }
    //        File dir = (File) DIRECTORY.lookUp( params );
    //        return new DataSourceMetadataEnity( dir, "Property file access for " + dir );        
    //    }    

    /** DOCUMENT ME!  */
    private static final Param DIRECTORY = new Param("directory", File.class,
            "Directory containting property files", true);

    /**
     * DOCUMENT ME!
     *
     * @param params DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public DataStore createDataStore(Map params) throws IOException {
    	File dir = new File(String.valueOf(params.get(DIRECTORY.key)));
        if (dir.exists() && dir.isDirectory()) {
            return new PropertyDataStore(dir);
        } else {
            throw new IOException("Directory is required");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param params DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public DataStore createNewDataStore(Map params) throws IOException {
    	File dir = new File(String.valueOf(params.get(DIRECTORY.key)));

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

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDisplayName() {
        return "Properties";
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDescription() {
        return "Allows access to Java Property files containing Feature information";
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Param[] getParametersInfo() {
        return new Param[] { DIRECTORY, };
    }

    /**
     * Test to see if this datastore is available, if it has all the
     * appropriate libraries to construct a datastore.  This datastore just
     * returns true for now.  This method is used for gui apps, so as to not
     * advertise data store capabilities they don't actually have.
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

    /**
     * DOCUMENT ME!
     *
     * @param params DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean canProcess(Map params) {
        try {
        	File file = new File(String.valueOf(params.get(DIRECTORY.key)));
            return file.exists() && file.isDirectory();
        } catch (Exception erp) {
        	erp.printStackTrace();
            return false;
        }
    }
    
    /**
     */
    public Map getImplementationHints(){
        return java.util.Collections.EMPTY_MAP;
    }
}
