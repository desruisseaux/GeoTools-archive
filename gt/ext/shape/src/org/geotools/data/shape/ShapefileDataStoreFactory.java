/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
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
package org.geotools.data.shape;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Collections;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;


/**
 * Implementation of the DataStore service provider interface for Shapefiles.
 *
 * @author Chris Holmes, TOPP
 * @version $Id$
 */
public class ShapefileDataStoreFactory
    implements org.geotools.data.FileDataStoreFactorySpi {
    
    private static final Param URLP = new Param("url", URL.class,
                                                "url to a .shp file");
    private static final Param MEMORY_MAPPED =
        new Param("memory mapped buffer", Boolean.class,
                  "enable/disable the use of memory-mapped io", false);

    private static final Param CREATE_SPATIAL_INDEX =
        new Param("create spatial index", Boolean.class,
                  "enable/disable the automatic creation of spatial index", 
                  false);
    
    /**
     * Takes a list of params which describes how to access a restore and
     * determins if it can be read by the Shapefile Datastore.
     *
     * @param params A set of params describing the location of a restore.
     *        Files should be pointed to by a 'url' param.
     *
     * @return true iff params contains a url param which points to a file
     *         ending in shp
     */
    public boolean canProcess(Map params) {
        boolean accept = false;
        if (params.containsKey(URLP.key)) {
            try {
                URL url = (URL) URLP.lookUp(params);
                accept = canProcess(url);
            } catch (IOException ioe) {
                // yes, I am eating this
            }
        }
        return accept;
    }

    /**
     * Returns an instance of a ShapeFileDataStore iff the restore pointed to
     * by params actualy is a Shapefile.
     *
     * @param params A param list with information on the location of a
     *        restore.  For shapefiles this should contain a 'url' param which
     *        points to a file which ends in shp.
     *
     * @return DataStore A ShapefileDatastore
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException Thrown if the datastore which is created
     *         cannot be attached to the restore specified in params.
     */
    public DataStore createDataStore(Map params) throws IOException {
        DataStore ds = null;

        URL url = null;
        try {
            url = (URL) URLP.lookUp(params);
            Boolean mm = (Boolean) MEMORY_MAPPED.lookUp(params);
            if (mm == null)
                mm = Boolean.TRUE;

            Boolean idx = (Boolean) CREATE_SPATIAL_INDEX.lookUp(params);
            if (idx == null)
                idx = Boolean.TRUE;
            ds = new ShapefileDataStore(url,
                                        mm.booleanValue(), 
                                        idx.booleanValue());
        } catch (MalformedURLException mue) {
            throw new DataSourceException("Unable to attatch datastore to "
                + url, mue);
        } 
 
        return ds;
    }

    /**
     * Not implemented yet.
     *
     * @param params
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     * @throws UnsupportedOperationException
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getDisplayName() {
        return "Shapefile";
    }
    /**
     * Describes the type of data the datastore returned by this factory works
     * with.
     *
     * @return String a human readable description of the type of restore
     *         supported by this datastore.
     */
    public String getDescription() {
        return "ESRI(tm) Shapefiles (*.shp)";
    }

//    public DataSourceMetadataEnity createMetadata( Map params )
//            throws IOException {
//        
//        URL url = (URL) URLP.lookUp(params);
//        Boolean mm = (Boolean) MEMORY_MAPPED.lookUp(params);
//        Boolean idx = (Boolean) CREATE_SPATIAL_INDEX.lookUp(params);
//        
//        String server;
//        String name;
//        if( url.getProtocol().equals("file")){
//            server = "localhost";
//            name = url.getPath();
//        }
//        else {
//            server = url.getHost()+":"+url.getPort();
//            name = url.getFile();
//        }
//        return new DataSourceMetadataEnity( server, name, "Shapefile access for "+url );
//    }
    /**
     * Test to see if this datastore is available, if it has all the
     * appropriate libraries to construct a datastore.  This datastore just
     * returns true for now.
     *
     * @return <tt>true</tt> if and only if this factory is available to create
     *         DataStores.
     *
     * @task REVISIT: I'm just adding this method to compile, maintainer should
     *       revisit to check for any libraries that may be necessary for
     *       datastore creations.
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * Describe parameters.
     *
     * @return
     *
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { URLP, MEMORY_MAPPED, CREATE_SPATIAL_INDEX };
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#getFileExtensions()
     */
    public String[] getFileExtensions() {
        return new String[] {".shp",};
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#canProcess(java.net.URL)
     */
    public boolean canProcess(URL f) {
        return f.getFile().toUpperCase().endsWith("SHP");
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#createDataStore(java.net.URL)
     */
    public DataStore createDataStore(URL url)  throws IOException{
        DataStore ds = null;
        try {
            ds = new ShapefileDataStore(url,Boolean.TRUE.booleanValue());
        } catch (MalformedURLException mue) {
            throw new DataSourceException("Unable to attatch datastore to "
                + url, mue);
        } 
 
        return ds;
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#getTypeName(java.net.URL)
     */
    public String getTypeName(URL url) throws IOException {
        DataStore ds = createDataStore(url);
        String[] names = ds.getTypeNames(); // should be exactly one
        return ((names == null || names.length==0)?null:names[0]);
    }

    /**
     * Returns the implementation hints. The default implementation returns en empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
