/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.property;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;

/**
 * Sample DataStore implementation, please see formal tutorial included
 * with users docs.
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class PropertyDataStore extends AbstractDataStore {
    protected File directory;
    public PropertyDataStore(File dir) {
        if( !dir.isDirectory()){
            throw new IllegalArgumentException( dir +" is not a directory");
        }
        directory = dir;
    }
    public String[] getTypeNames() {
        FilenameFilter f;
        String list[] = directory.list( new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return name.endsWith(".properties");
            }
        });
        for( int i=0; i<list.length;i++){
            list[i] = list[i].substring(0, list[i].lastIndexOf('.'));
        }
        return list;
    }
    // START SNIPPET: getSchema
    public FeatureType getSchema(String typeName) throws IOException {
        String typeSpec = property( typeName, "_");
        try {
            String namespace = directory.getName();
            return DataUtilities.createType( namespace+"."+typeName,typeSpec );
        } catch (SchemaException e) {
            e.printStackTrace();
            throw new DataSourceException( typeName+" schema not available", e);
        }
    }
    // END SNIPPET: getSchema

    private String property( String typeName, String key ) throws IOException {
        File file = new File( directory, typeName+".properties");
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        try {        
            for( String line = reader.readLine(); line != null; line = reader.readLine()){
                if( line.startsWith( key+"=" )){
                    return line.substring( key.length()+1 );
                }
            }
        }
        finally {
            reader.close();            
        }        
        return null;        
    }
    protected FeatureReader getFeatureReader(String typeName) throws IOException {
        return new PropertyFeatureReader( directory, typeName );        
    }
    protected FeatureWriter getFeatureWriter(String typeName) throws IOException {
        return new PropertyFeatureWriter( this, typeName );
    }
    public void createSchema(FeatureType featureType) throws IOException {
        String typeName = featureType.getTypeName();
        File file = new File( directory, typeName+".properties");
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        writer.write("_=");
        writer.write( DataUtilities.spec( featureType ) );
        writer.close();
    }
    //
    // Access to Optimiations
    //
    public FeatureSource getFeatureSource(final String typeName) throws IOException {
        return new PropertyFeatureSource( this, typeName );
    }        
}
