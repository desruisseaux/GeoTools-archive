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

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.FeatureListener;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Envelope;


/**
 * State for a content entry.
 * <p>
 * This class maintains a cache of certain aspects of a feature type which are
 * subject to the state of a dataset modified in a transaction. Examples of 
 * such content:
 * <ul>
 * <li>
 * <li>Type FeatureType (per FeatureTypeFactory )
 * <li>key: "bounds"; value: Envelope of dataset
 * <li>key: "count"; value: Number of features in dataset
 * </ul>
 * </p>
 * 
 * @author Jody Garnett, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public class ContentState {
	
	/**
	 * cached feature type
	 */
	protected FeatureType memberType;
	/**
	 * cached feature collection type
	 */
	protected FeatureType collectionType;
	/**
	 * cached number of features
	 */
	protected int count = -1;
	/**
	 * cached bounds of features
	 */
	protected Envelope bounds;

    /**
     * entry maintaining the state
     */
    protected ContentEntry entry;

    /**
     * observers
     */
    private List listeners = new ArrayList(2);
    
    /**
     * Creates a new state.
     *
     * @param entry The entry for the state.
     */
    public ContentState(ContentEntry entry) {
        this.entry = entry;
        this.listeners = new ArrayList(2);
    }
    
    protected ContentState(ContentState state) {
		this( state.getEntry() );
		
        memberType = state.memberType;
        collectionType = state.collectionType;
        count = state.count;
        bounds = state.bounds == null ? null : new Envelope( state.bounds );
	}

	public FeatureType getMemberType(){
    	return memberType;
    }
    
    public void setMemberType( FeatureType memberType ){
    	this.memberType = memberType;    	
    }
    
    public FeatureType getCollectionType(){
    	return collectionType; 
    }
    
    public void setCollectionType( FeatureType featureType ){
    	collectionType = featureType;    	
    }
    
    public int getCount(){
    	return count;
    }
    
    public void setCount(int count){
    	this.count = count;
    }
    
    public Envelope getBounds(){
    	return bounds;
    }
    
    public void setBounds( Envelope bounds ){
    	this.bounds = bounds;
    }
    
    /**
     * Flushes the cache.
     */
    public void flush() {
        memberType = null;
        collectionType = null;
        count = -1;
        bounds = null;
    }

    /**
     * Access to content entry (containing content definition )
     */
    public ContentEntry getEntry() {
        return entry;
    }

    /**
     * Cleans up the state object by clearing cache and listeners.
     */
    public void close() {
    	memberType = null;
    	collectionType = null;
    	if( listeners != null ){
    		listeners.clear();
    		listeners = null;
    	}
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

    /**
     * Copies the state.
     *
     * @return A copy of the state.
     */
    public ContentState copy() {
        return new ContentState( this );
    }
}
