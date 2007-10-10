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
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * The state of an entry in a datastore, maintained on a per-transaction basis.
 * <p>
 * State is maintained on a per transaction basis (see {@link ContentEntry}}. 
 * State maintained includes cached values such as:
 * <ul>
 *   <li>feature type ({@link #getFeatureType()}
 *   <li>number of features ({@link #getCount()}
 *   <li>spatial extent ({@link #getBounds()}.
 * </ul>
 * Other types of state depend on the data format. For instance, a jdbc database
 * backed format would probably want to store a database connection as state.
 *</p>
 * <p>
 * This class is a "data object" and is not thread safe. It is up to clients of 
 * this class to ensure that values are set in a thread-safe / synchronized 
 * manner. For example:
 * <pre>
 *   <code>
 *   ContentState state = ...;
 *   
 *   //get the count
 *   int count = state.getCount();
 *   if ( count == -1 ) {
 *     synchronized ( state ) {
 *       count = calculateCount();
 *       state.setCount( count );
 *     }
 *   }
 *   </code>
 * </pre>
 * </p>
 * <p>
 * This class may be extended. Subclasses may extend (not override) the 
 * following methods:
 * <ul>
 * <li>{@link #flush()}
 * <li>{@link #close()}
 * </ul>
 * Subclasses should also override {@link #copy()}.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public class ContentState {
	
	/**
	 * cached feature type
	 */
	protected SimpleFeatureType featureType;
	/**
	 * cached number of features
	 */
	protected int count = -1;
	/**
	 * cached bounds of features
	 */
	protected ReferencedEnvelope bounds;
    /**
     * entry maintaining the state
     */
    protected ContentEntry entry;
    /**
     * observers
     */
    protected List<FeatureListener> listeners;
    
    /**
     * Creates a new state.
     *
     * @param entry The entry for the state.
     */
    public ContentState(ContentEntry entry) {
        this.entry = entry;
        this.listeners = new ArrayList<FeatureListener>(2);
    }
    
    /**
     * Creates a new state from a previous one.
     * <p>
     * All state from the specified <tt>state</tt> is copied. Therefore subclasses
     * extending this constructor should clone all mutable objects.
     * </p>
     *
     * @param state The existing state.
     */
    protected ContentState(ContentState state) {
		this( state.getEntry() );
		
        featureType = state.featureType;
        count = state.count;
        bounds = state.bounds == null ? 
                null : new ReferencedEnvelope( state.bounds );
	}

    /**
     * The entry which maintains the state.
     */
    public ContentEntry getEntry() {
        return entry;
    }
    
    /**
     * The cached feature type.
     */
	public final SimpleFeatureType getFeatureType(){
    	return featureType;
    }
    
	/**
     * Sets the cached feature type.
     */
    public final void setFeatureType( SimpleFeatureType featureType ){
    	this.featureType = featureType;
    }
    
    /**
     * The cached number of features.
     * 
     */
    public final int getCount(){
    	return count;
    }
    
    /**
     * Sets the cached number of features.
     */
    public final void setCount(int count){
    	this.count = count;
    }
    
    /**
     * The cached spatial extent.
     */
    public final ReferencedEnvelope getBounds(){
    	return bounds;
    }
    
    /**
     * Sets the cached spatial extent.
     */
    public final void setBounds( ReferencedEnvelope bounds ){
    	this.bounds = bounds;
    }
    
    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    public final void addListener(FeatureListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public final void removeListener(FeatureListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clears cached state.
     * <p>
     * This method does not affect any non-cached state. This method may be 
     * extended by subclasses, but not overiden.
     * </p>
     */
    public void flush() {
        featureType = null;
        count = -1;
        bounds = null;
    }
    
    /**
     * Clears all state.
     * <p>
     * Any resources that the state holds onto (like a database connection) should 
     * be closed or disposes when this method is called. This method may be 
     * extended by subclasses, but not overiden.
     * </p>
     */
    public void close() {
        featureType = null;
        if( listeners != null ){
            listeners.clear();
            listeners = null;
        }
    }
    
    /**
     * Copies the state.
     * <p>
     * Subclasses shold override this method. Any mutable state objects should 
     * be cloned.
     * </p>
     *
     * @return A copy of the state.
     */
    public ContentState copy() {
        return new ContentState( this );
    }
}
