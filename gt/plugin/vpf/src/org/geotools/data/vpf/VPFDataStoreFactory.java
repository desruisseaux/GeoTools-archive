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
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.vpf.ifc.FileConstants;
import org.geotools.feature.SchemaException;


/**
 * Class VPFDataSourceFactory.java is responsible for constructing appropriate
 * VPFDataStore (actually VPFLibrary) objects.
 * VPFDataStoreFactory
 * - factory for VPFLibrary
 *    - factory for VPFCoverage
 *        - factory for VPFFeatureClass
 *            - implements FeatureType by delegation to contained DefaultFeatureType
 *            - contains VPFFiles
 *            - retrieves VPFColumns from VPFFiles for use in constructing DefaultFeatureType
 *            - contains joins (column pairs)
 *        - factory for VPFFeatureType
 *            - implements FeatureType by delegation to contained VPFFeatureClass
 * 
 * VPFFile
 * - contains VPFInputStream
 * - factory for VPFColumn
 *    - implements AttributeType by delegation to contained DefaultFeatureType
 * <p>
 * Created: Fri Mar 28 15:54:32 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @author <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @version 2.0.0
 */
public class VPFDataStoreFactory implements DataStoreFactorySpi {
    /**
     * Default Constructor
     *
     */
    public VPFDataStoreFactory() {
    }
    /*
     *  (non-Javadoc)
     * @see org.geotools.data.DataStoreFactorySpi#getDisplayName()
     */
    public String getDisplayName() {
        return "Vector Product Format Library";
    }
    /*
     *  (non-Javadoc)
     * @see org.geotools.data.DataStoreFactorySpi#getDescription()
     */
    public String getDescription() {
        return "Vector Product Format Library data store implementation.";
    }
    /*
     *  (non-Javadoc)
     * @see org.geotools.data.DataStoreFactorySpi#canProcess(java.util.Map)
     */
    public boolean canProcess(Map params ) {
        boolean result = false;
        try {
            File file = (File) DIR.lookUp( params );
            File lhtFile;
            if( (file.exists() && file.isDirectory()) ){
                lhtFile = new File(file, FileConstants.LIBRARY_HEADER_TABLE);
                if(lhtFile.exists()){
                    result = true;
                }
            }
        } catch (IOException exc) {
        }
        return result;
    }
    /*
     *  (non-Javadoc)
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(Map params) throws IOException {
        return create(params);
    }
    /*
     *  (non-Javadoc)
     * @see org.geotools.data.DataStoreFactorySpi#createMetadata(java.util.Map)
     */
//    public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
//        if( !canProcess( params )){
//            throw new IOException( "Provided params cannot be used to connect");
//        }
//        File dir = (File) DIR.lookUp( params );
//        String parent = dir.getParent();
//        String name = dir.getName();        
//        return new DataSourceMetadataEnity( parent, name, "VPF data source access for " + dir );
//    }
    /**
     * Creates a data store.
     * @param params A <code>Map</code> of parameters which must be verified and 
     * @return
     * @throws IOException
     */
    private DataStore create(Map params) throws IOException {
        DataStore result = null;
        File file = (File) DIR.lookUp( params );
        File lhtFile;
        if( (file.exists() && file.isDirectory()) ){
            lhtFile = new File(file, "lht");
            if(lhtFile.exists()){
                try {
                    result = new VPFLibrary( file );
                } catch (SchemaException exc) {
                    throw new IOException("There was a problem making one of the feature classes as a FeatureType.");
                }
            }
        }
        return result;
    }
    /*
     * 
     *  (non-Javadoc)
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(Map params) throws java.io.IOException {
        return create(params);
    }
    /**
     * A parameter which is the directory containing the LHT file
     */
    public static final Param DIR = new Param( "dir", File.class, "Directory containing lht file", true );
    /*
     *  (non-Javadoc)
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { DIR,  };
    }
    /*
     *  (non-Javadoc)
     * @see org.geotools.data.DataStoreFactorySpi#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }
}