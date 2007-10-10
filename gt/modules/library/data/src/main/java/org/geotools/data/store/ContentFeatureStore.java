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
package org.geotools.data.store;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

/**
 * Abstract implementation of FeatureStore.
 * <p>
 * List its base class {@link ContentFeatureSource}, this feature store works off 
 * of operations provided by {@link FeatureCollection}.
 * </p>
 * <p>
 * The {@link #addFeatures(FeatureCollection)} method is used to add features to
 * the feature store. The method should return the "persistent" feature id's 
 * which are generated after the feature has been added to persistent storage.
 * Often the persistent fid is different from the fid specified by the actual 
 * feature being inserted. For this reason {@link SimpleFeature#getUserData()} is
 * used to report back persistent fids. It is up to the implementor of the 
 * feature collection to report this value back after a feature has been inserted.
 * As an example, consider an implementation of {@link FeatureCollection#add(Object)}.
 * <pre>
 *  boolean add( Object o ) {
 *    SimpleFeature feature = (SimpleFeature) o;
 *    
 *    //1.add the feature to storage
 *    ...
 *    
 *    //2. derive the persistent fid
 *    String fid = ...;
 *    
 *    //3. set the user data
 *    feature.getUserData().put( "fid", fid );
 *  
 *  }
 * </pre>
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public abstract class ContentFeatureStore extends ContentFeatureSource implements FeatureStore {
    public ContentFeatureStore(ContentEntry entry) {
        super(entry);
    }
    
    /**
     * Adds a feature collection to the feature store.
     * <p>
     * This method delegates to the {@link FeatureCollection#add(Object)}
     * method of the feature collection created by {@link #all(ContentState)}.
     * </p>
     * <p>
     * <b>Note:</b>Persistent feature id's are reported back using 
     * {@link Feature#getUserData()} under the "fid" key. 
     * </p>
     */
    public final Set<String> addFeatures(FeatureCollection collection)
        throws IOException {
        
        //grab all the features
        FeatureCollection all = all(entry.getState(transaction));
        
        //gather up id's
        Set<String> ids = new TreeSet<String>();
        
        for ( Iterator<SimpleFeature> i = collection.iterator(); i.hasNext(); ) {
            SimpleFeature feature = i.next();
            if ( all.add( feature ) ) {
                String fid = (String) feature.getUserData().get( "fid" );
                if ( fid != null ) {
                    ids.add( fid );
                }
                else {
                    ids.add( feature.getID() );
                }
            }
            
        }
        
        return ids;
    }

    /**
     * Sets the features of the feature source.
     * <p>
     * This method delegates to the {@link FeatureCollection#clear()} and 
     * {@link FeatureCollection#add(Object)} methods of the feature collection
     * created by {@link #all(ContentState)}.
     * </p>
     * <p>
     * This method operates by first clearing the contents of the feature 
     * collection, then adding features produced by <tt>reader</tt>.
     * </p>
     */
    public final void setFeatures(FeatureReader reader) throws IOException {
        FeatureCollection features = all( getState() );
        features.clear();
        
        while( reader.hasNext() ) {
            features.add( reader.next() );
        }
    }
    
    /**
     * Modifies or updates the features of a feature store which match the 
     * specified filter.
     * <p>
     * This method delegates to the {@link FeatureCollection#update(AttributeDescriptor[], Object[] value)}
     * method of the feature collection created by {@link #filtered(ContentState, Filter)}.
     * </p>
     * <p>
     * The <tt>filter</tt> must not be <code>null</code>, in this case this method
     * will throw an {@link IllegalArgumentException}.
     * </p>
     */
    public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
        throws IOException {
        if ( filter == null ) {
            String msg = "Must specify a filter, must not be null.";
            throw new IllegalArgumentException( msg );
        }
        
        //TODO: implement and make final when datastore api is changed.
        //FeatureCollection features = filtered(getState(), filter);
        //features.update(type, value);
    }

    /**
     * Calls through to {@link #modifyFeatures(AttributeDescriptor[], Object[], Filter)}.
     */
    public final void modifyFeatures(AttributeDescriptor type, Object value, Filter filter)
        throws IOException {
        
        modifyFeatures( new AttributeDescriptor[]{ type }, new Object[]{ value }, filter );
    }

    /**
     * Removes the features from the feature store which match the specified 
     * filter.
     * <p>
     * This method delegates to the {@link FeatureCollection#clear()} method of
     * the feature collection created by {@link #filtered(ContentState, Filter)}.
     * </p>
     * <p>
     * The <tt>filter</tt> must not be <code>null</code>, in this case this method
     * will throw an {@link IllegalArgumentException}.
     * </p>
     */
    public final void removeFeatures(Filter filter) throws IOException {
        if ( filter == null ) {
            String msg = "Must specify a filter, must not be null.";
            throw new IllegalArgumentException( msg );
        }
        
        filtered( getState(), filter ).clear();
    }
}
