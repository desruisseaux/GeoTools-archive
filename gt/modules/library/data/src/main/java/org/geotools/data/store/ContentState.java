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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.FeatureListener;
import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.opengis.feature.simple.SimpleTypeFactory;

/**
 * Everything you need to implement your FeatureCollections (captures content
 * that changes for each Transaction; such as count or bounds).
 * <p>
 * Please be careful when caching content; a Feature created with one feature
 * may be different then one created with another factory.
 * </p>
 * <p>
 * Some thoughts on how to cache content:
 * <ul>
 * <li>key FeatureTypeFactory value: FeatureType
 * <li>key FeatureFactory value: FeatureCollection (representing all content)
 * <li>key "bounds" value: ReferenceEnvelope of all content
 * <li>key "count" value: number of features in total
 * </ul>
 * Note: please be careful in overriding clone in order to prevent
 * any values from being shared between transactions.
 * <p>
 * @author Jody Garnett, Refractions Research Inc.
 */
public abstract class ContentState {

    /** Cache to be duplicated on each transaction */
    private Map cache = new HashMap();

    /** Internal list for broadcasting events */
    private List listeners = new ArrayList(2);

    /** Content entry recording content definition. */
    final protected ContentEntry entry;
    
    /**
     * Protected constructor, subclass must be sure to include entry.
     * 
     * @param entry
     */
    protected ContentState( ContentEntry entry ){
        this.entry = entry;        
    }
    
    /** Add an entry to the cache */
    final public void put( Object key, Object value ){
        cache.put( key, value );
    }
    /** Retrive a value from the cache (or null) */
    final public Object get( Object key ){
        return cache.get( key );
    }
    /** Empty cache (often in response to a great change) */
    final public void flush(){
        cache.clear();
    }
    /**
     * Access to content entry (containing content definition )
     */
    public ContentEntry getEntry(){
        return entry;
    }
    /**
     * Used to clean up after use.
     */
    public void close(){
        cache.clear();
        cache = null;
        listeners.clear();
        listeners = null;
    }
    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    public void addListener(FeatureListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(FeatureListener listener) {
        listeners.remove(listener);
    }

    /** Grabs a copy (duplicated map & empty listener list) */
    protected Object clone() throws CloneNotSupportedException {
        ContentState clone = (ContentState) super.clone();
        clone.cache = new HashMap( cache );
        clone.listeners = new ArrayList(2);
        return clone;
    }

    public ContentState copy() {
        try {
            ContentState copy = (ContentState) clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("ContentState always can be copied");
        }
    }        
    
    /**
     * Accessor for the feature type of the content entry.
     * <p>
     * Use {@link #featureType(SimpleTypeFactory)} to create a feature type 
     * with a particular factory.
     * </p>
     * 
     */
    final public FeatureType featureType() throws IOException {
		return featureType( getEntry().getDataStore().getTypeFactory() );
	}
	
	/**
     * Builds and caches the featureType based on current state.
     * 
     * @param factory The factory used to build the feature type.
     */
    final public FeatureType featureType( SimpleTypeFactory factory  ) throws IOException {
    	FeatureType featureType = (FeatureType) get( factory.getClass() );
    	if ( featureType == null ) {
    		//build the feature type
    		featureType = buildFeatureType( factory );
    		
    		//cache it
    		put( factory.getClass(), featureType );
    	}
    	
    	return featureType;
    }
    
    /**
     * Creates a feature type for the entry.
     * <p>
     * An implementation of this method should create a new instance of 
     * {@link SimpleTypeBuilder}, injecting it with the supplied factory.
     * </p>
     * @param factory The factory used to create the feature type.
     */
    abstract protected FeatureType buildFeatureType( SimpleTypeFactory factory )
    	throws IOException;
}