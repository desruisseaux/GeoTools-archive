/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/**
 * Implementation of DataStore based around feature collections.
 * <p>
 * This implementation of DataStore is based around four feature collections
 * implementation that must be provided by the subclass.
 * <ul>
 * <li>FeatureCollection <b>All</b>: represents all content</li>
 * <li>FeatureCollection <b>Sub</b>: filtered content</li>
 * <li>FeatureList <b>Sorted</b>: sorted content (a FeatureList)</li>
 * <li>FeatureCollection <b>Readonly</b> filted content for reading only </li>
 * </ul>
 * These feature collections are all <b>fully capable</b>; that is able to do
 * everything from sorting to reprojection and the various retyping operations.
 * </p>
 * <p>
 * A subclass has the following responsibility / operatunities:
 * <ul>
 * <li>Provide an implementation of "Content" teaching the
 *     ContentDataStore 
 * <li>Additional methods as needs or interest dictate
 * </ul>
 * This class is locked down to the maximum extent possible; learning from our
 * earlier experience.
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class ContentDataStore implements DataStore {
     /**
      * Map<TypeName,ContentEntry> one for each kind of content served up.
      */
     final Map entries;
     
     final protected Content content;

     public ContentDataStore( Content content ){
         this.content = content;
         this.entries = new HashMap();
     }

    public void createSchema(FeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("Not yet");
    }

    /** Used to strongly type typeName as soon as possible */
    final private TypeName name( String typeName ){
        for( Iterator i = entries.keySet().iterator(); i.hasNext(); ){
            TypeName name = (TypeName) i.next();
            if( name.getLocalPart().equals( typeName )) return name;
        }
        return null;
    }
    
    final private FeatureCollection query( Query query, Transaction transaction ) throws IOException{
//        TypeName typeName = name( query.getTypeName() );
//        ContentEntry entry = (ContentEntry) entries.get( typeName );
//        ContentState state = entry.getState( transaction );
        
          FeatureSource source = getFeatureSource( query.getTypeName() );
          if( source instanceof FeatureStore){
              ((FeatureStore)source).setTransaction( transaction );
          }
          FeatureCollection collection = source.getFeatures( query.getFilter() );

          if( query.getCoordinateSystemReproject() != null ){
              // collection = collection.reproject( query.getCoordinateSystemReproject() );
          }
          if( query.getCoordinateSystem() != null ){
              // collection = collection.toCRS( query.getCoordinateSystem() );
          }
          if( query.getMaxFeatures() != Integer.MAX_VALUE ){
              collection = (FeatureCollection)
                  collection.sort( SortBy.NATURAL_ORDER ).subList( 0, query.getMaxFeatures() );
          }
          if( query.getNamespace() != null ){
              //collection = collection.toNamespace( query.getNamespace() );
          }
          if( query.getPropertyNames() != Query.ALL_NAMES ){
              //collection = collection.reType( query.getPropertyNames() );
          }
          return collection;        
    }

    public FeatureSource getFeatureSource(String typeName) throws IOException {
        return null;
    }

    public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction) throws IOException {
        return null;
    }

    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction) throws IOException {
        return null;
    }

    public LockingManager getLockingManager() {
        return null;
    }

    public FeatureType getSchema(String typeName) throws IOException {
        return null;
    }

    public String[] getTypeNames() throws IOException {
        return null;
    }

    public FeatureSource getView(Query query) throws IOException, SchemaException {
        return null;
    }

    public void updateSchema(String typeName, FeatureType featureType) throws IOException {        
    }

    //
    // low level operations
    //
    public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
        FeatureCollection collection = query( query, transaction );
        
        return null; // new DelegateFeatureReader( collection.getSchema(), collection.features() );
    }

    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction) throws IOException {
        //FeatureCollection collection = query( query, transaction );

        return null; // new DelegateFeatureWriter( collection.getSchema(), collection.features() );
    }

}