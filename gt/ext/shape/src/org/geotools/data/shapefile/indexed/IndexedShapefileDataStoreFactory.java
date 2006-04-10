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
package org.geotools.data.shapefile.indexed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Implementation of the DataStore service provider interface for Shapefiles.
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 */
public class IndexedShapefileDataStoreFactory
    implements org.geotools.data.FileDataStoreFactorySpi {
    public static final Byte TREE_NONE = new Byte(IndexedShapefileDataStore.TREE_NONE);
    public static final Byte TREE_GRX = new Byte(IndexedShapefileDataStore.TREE_GRX);
    public static final Byte TREE_QIX = new Byte(IndexedShapefileDataStore.TREE_QIX);
    public static final Param NAMESPACEP = new Param("namespace", URI.class,
            "uri to a the namespace", false); //not required
    public static final Param URLP = new Param("shapefile url", URL.class,
            "url to a .shp file");
    public static final Param MEMORY_MAPPED = new Param("memory mapped buffer",
            Boolean.class, "enable/disable the use of memory-mapped io", false);
    public static final Param CREATE_SPATIAL_INDEX = new Param("create spatial index",
            Boolean.class,
            "enable/disable the automatic creation of spatial index", false);
    public static final Param SPATIAL_INDEX_TYPE = new Param("index type",
            Byte.class,
            "select type of index to use or none: Valid values are: TREE_GRX (RTREE), TREE_QIX (QUADTREE), TREE_NONE (no indexing), ",
            false) {
    	
    	public Object parse(String text) throws Throwable {
    		Object value = null;
    		try {
    			value = super.parse(text);
    		}
    		catch(Throwable t) {}
    		
    		if (value == null && text != null) {
    			text = text.trim();
    			if ("TREE_GRX".equalsIgnoreCase(text) || "RTREE".equalsIgnoreCase(text))
    				return TREE_GRX;
    			if ("TREE_QIX".equalsIgnoreCase(text) || "QUADTREE".equalsIgnoreCase(text)) 
    				return TREE_QIX;
    			if ("TREE_NONE".equalsIgnoreCase(text) || "NO INDEX".equalsIgnoreCase(text))
    				return TREE_NONE;
    		}
    		
    		return value;
    	}
    };
    private final static Map HINTS = new HashMap();

    static {
        HINTS.put(CREATE_SPATIAL_INDEX, Boolean.valueOf(true));
        HINTS.put(MEMORY_MAPPED, Boolean.valueOf(true));
        HINTS.put(SPATIAL_INDEX_TYPE, TREE_GRX);
    }

    private Map liveStores = new HashMap();

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
                accept = false;
            }
        }

        if (params.containsKey(SPATIAL_INDEX_TYPE.key)) {
            Byte type;

            try {
                type = (Byte) SPATIAL_INDEX_TYPE.lookUp(params);
                accept = accept
                    && (type.equals(TREE_GRX) || type.equals(TREE_QIX)
                    || type.equals(TREE_NONE));
            } catch (Exception e) {
                accept = false;
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
     */
    public DataStore createDataStore(Map params) throws IOException {
        DataStore ds = null;

        if (!liveStores.containsKey(params)) {
            ds = createNewDataStore(params);
        } else {
            ds = (DataStore) liveStores.get(params);
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
     * @throws DataSourceException
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        URL url = null;
        DataStore ds = null;

        try {
            url = (URL) URLP.lookUp(params);

            Boolean mm = (Boolean) MEMORY_MAPPED.lookUp(params);

            if (mm == null) {
                mm = Boolean.FALSE;
            }

            Boolean idx = (Boolean) CREATE_SPATIAL_INDEX.lookUp(params);

            if (idx == null) {
                idx = Boolean.TRUE;
            }

            URI namespace = (URI) NAMESPACEP.lookUp(params);
            Byte type = (Byte) SPATIAL_INDEX_TYPE.lookUp(params);

            if (type == null) {
                type = new Byte(IndexedShapefileDataStore.TREE_QIX);
            }

            ds = new IndexedShapefileDataStore(url, namespace,
                    mm.booleanValue(), idx.booleanValue(), type.byteValue());
            liveStores.put(params, ds);
        } catch (MalformedURLException mue) {
            throw new DataSourceException("Unable to attatch datastore to "
                + url, mue);
        }

        return ds;
    }

    public String getDisplayName() {
        return "Shapefile (Indexed)";
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
        try {
            ShapefileDataStore.class.getName();
            Geometry.class.getName();
        } catch (Exception e) {
            return false;
        }

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
        return new Param[] {
            URLP, NAMESPACEP, MEMORY_MAPPED, CREATE_SPATIAL_INDEX,
            SPATIAL_INDEX_TYPE
        };
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#getFileExtensions()
     */
    public String[] getFileExtensions() {
        return new String[] { ".shp", };
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
    public DataStore createDataStore(URL url) throws IOException {
        Map params = new HashMap();
        params.put(URLP.key, url);

        return createDataStore(params);
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#createDataStore(java.net.URL)
     */
    public DataStore createDataStore(URL url, boolean memorymapped)
        throws IOException {
        Map params = new HashMap();
        params.put(URLP.key, url);
        params.put(MEMORY_MAPPED.key, new Boolean(memorymapped));

        return createDataStore(params);
    }

    /**
     * @see org.geotools.data.dir.FileDataStoreFactorySpi#getTypeName(java.net.URL)
     */
    public String getTypeName(URL url) throws IOException {
        DataStore ds = createDataStore(url);
        String[] names = ds.getTypeNames(); // should be exactly one

        return (((names == null) || (names.length == 0)) ? null : names[0]);
    }

    /**
     * Returns the implementation hints. The default implementation returns en
     * empty map.
     *
     * @return DOCUMENT ME!
     */
    public Map getImplementationHints() {
        return HINTS;
    }
}
