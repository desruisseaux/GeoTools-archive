/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.vpf;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Map;

import org.geotools.data.DataSourceMetadataEnity;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;


/**
 * Class VPFDataSourceFactory.java is responsible for
 * 
 * <p>
 * Created: Fri Mar 28 15:54:32 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @version 2.0.0
 */
public class VPFDataStoreFactory implements DataStoreFactorySpi {
    public VPFDataStoreFactory() {
    }
    public String getDisplayName() {
        return "Vector Product Format";
    }
    public String getDescription() {
        return "Vector Product Format data store implementation.";
    }

    public boolean canProcess(Map params ) {
        if (!params.containsKey("dir")) {
            return false;
        }
        try {
            File file = (File) DIR.lookUp( params );

            //System.out.println( "Filen eksisterer...: " + file.exists() );
            //System.out.println( "Filen er et dir....: " + file.isDirectory());
            //System.out.println( "Svaret blir........: " + (file.exists() && file.isDirectory()) );
            return (file.exists() && file.isDirectory());
        } catch (Exception e) {
            return false;
        }
    }

    public DataStore createDataStore(Map params) throws IOException {
        return create(params);
    }
    public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
        if( !canProcess( params )){
            throw new IOException( "Provided params cannot be used to connect");
        }
        File dir = (File) DIR.lookUp( params );
        String parent = dir.getParent();
        String name = dir.getName();        
        return new DataSourceMetadataEnity( parent, name, "VPF" );
    }

    /**
     * Create a VPFDataStore.
     * <p>
     * Shouldn't you check that the dht file exists?
     * </p>
     */
    private DataStore create(Map params) throws IOException {
        // This will lookup up dir in the Map
        // it will make sure it is a File, and upcovert
        // a text representation using reflection if needed.       
        File file = (File) DIR.lookUp( params );
        
        // What it cannot do is check if it is a directory,
        // and contains a dht file.
        if( (file.exists() && file.isDirectory()) ){            
            return new VPFDataStore( file );
        }
        else {
            throw  new IOException("Provided file must be a directory");            
        }
    }

    public DataStore createNewDataStore(Map params) throws java.io.IOException {
        return create(params);
    }
    
    public static final Param DIR = new Param( "dir", File.class, "Directory containing dht file", true );    
    public Param[] getParametersInfo() {
        return new Param[] { DIR,  };
    }

    public boolean isAvailable() {
        return true;
    }
}