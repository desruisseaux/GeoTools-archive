/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data;

import java.util.EventObject;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Represents all events triggered by DataStore instances (typically change events).
 *
 * <p>
 * The "Source" for FeatureEvents is taken to be a <code>FeatureSource</code>,
 * rather than <code>DataStore</code>. The is due to FeatureSource<SimpleFeatureType, SimpleFeature> having a
 * hold of Transaction information.
 * </p>
 *
 * <p>
 * DataStore implementations will actually keep the list listeners sorted
 * by TypeName, and can report FeatureWriter modifications as required
 * (by filtering the Listener list by typeName and Transaction).
 * </p>
 *
 * <p>
 * The Transaction.commit() operation will also need to provide notification, this
 * shows up as a CHANGE event; with a bit more detail being available in the subclass
 * BatchFeatureEvent.
 * </p>
 * 
 * @since GeoTools 2.0
 * @source $URL$
 */
public class FeatureEvent extends EventObject {
    private static final long serialVersionUID = 3154238322369916485L;

    /**
     * FeatureWriter event type denoting the adding features.
     *
     * <p>
     * This EventType is used when FeatureWriter.write() is called when
     * <code>FeatureWriter.hasNext()</code> has previously returned
     * <code>false</code>. This action represents a newly create Feature being
     * passed to the DataStore.
     * </p>
     *
     * <p>
     * The FeatureWriter making the modification will need to check that
     * <code>typeName</code> it is modifing matches the
     * <code>FeatureSource.getSchema().getTypeName()</code> before sending
     * notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * If the FeatureWriter is opperating against a Transaction it will need
     * ensure that to check the FeatureSource.getTransaction() for a match
     * before sending notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * FeatureEvent.getBounds() should reflect the the Bounding Box of the
     * newly created Features.
     * </p>
     * @deprecated Please use FeatureEvent.getType() == Type.ADDED
     */
    public static final int FEATURES_ADDED = 1;

    /**
     * Event type constant denoting that features in the collection has been
     * modified.
     *
     * <p>
     * This EventType is used when a FeatureWriter.write() is called when
     * <code>FeatureWriter.hasNext()</code> returns <code>true</code> and the
     * current Feature has been changed. This EventType is also used when a
     * Transaction <code>commit()</code> or <code>rolledback</code> is called.
     * </p>
     *
     * <p>
     * The FeatureWriter making the modification will need to check that
     * <code>typeName</code> it is modifing matches the
     * <code>FeatureSource.getSchema().getTypeName()</code> before sending
     * notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * If the FeatureWriter is opperating against a Transaction it will need
     * ensure that to check the FeatureSource.getTransaction() for a match
     * before sending notification to any listeners on the FeatureSource. All
     * FeatureSources of the same typename will need to be informed of a
     * <code>commit</code>, except ones in the same Transaction,  and only
     * FeatureSources in the same Transaction will need to be informed of a
     * rollback.
     * </p>
     *
     * <p>
     * FeatureEvent.getBounds() should reflect the the BoundingBox of the
     * FeatureWriter modified Features. This may not be possible during a
     * <code>commit()</code> or <code>rollback()</code> opperation.
     * </p>
     * @deprecated Please use FeatureEvent.getType() == Type.CHANGED 
     */
    public static final int FEATURES_CHANGED = 0;

    /**
     * Event type constant denoting the removal of a feature.
     *
     * <p>
     * This EventType is used when FeatureWriter.remove() is called. This
     * action represents a Feature being removed from the DataStore.
     * </p>
     *
     * <p>
     * The FeatureWriter making the modification will need to check that
     * <code>typeName</code> it is modifing matches the
     * <code>FeatureSource.getSchema().getTypeName()</code> before sending
     * notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * If the FeatureWriter is opperating against a Transaction it will need
     * ensure that to check the FeatureSource.getTransaction() for a match
     * before sending notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * FeatureEvent.getBounds() should reflect the the Bounding Box of the
     * removed Features.
     * </p>
     * @deprecated Please use FeatureEvent.getType() == Type.REMOVED 
     */
    public static final int FEATURES_REMOVED = -1;

    public enum Type {
        /**
         * Features have been added.
         *
         * <p>
         * This EventType is used when FeatureWriter.close() is called when
         * <p>
         * The use of FeatureStore addFeatures( FeatureCollection<SimpleFeatureType, SimpleFeature> ) is recommended,
         * and will issue a single FeatureEvent with getType() ADDED.
         * <p> 
         * For a more hands on experience FeatureWriter can be used to create
         * new features can be added by calling
         * <code>next()</code> and <code>write()</code>
         * when <code>hasNext()</code> has previously returned
         * <code>false</code>.
         * This action represents a newly create Feature being
         * passed to the DataStore.
         * <p>
         * If you are working with a Transaction you will only receive events
         * for modifications that occur on that Transaction. If not you will need
         * to wait for the event like everyone else.</p>
         *
         * <p>
         * FeatureEvent.getBounds() should reflect the the Bounding Box of the
         * newly created Features.
         * </p>
         */
        ADDED( FEATURES_ADDED ),
        CHANGED( FEATURES_CHANGED ),
        REMOVED( FEATURES_REMOVED );
        
        final int type;
        
        Type( int type ){
            this.type = type;
        }
        static Type fromValue( int value ){
        	switch( value ){
        	case FEATURES_ADDED: return ADDED;
        	case FEATURES_CHANGED: return CHANGED;
        	case FEATURES_REMOVED: return REMOVED;
        	}   
        	return CHANGED;    	
        }
    }

    /** Indicates one of Type.ADDED, Type.REMOVED, Type.CHANGED */
    private Type type;
    
    /**
     * Indicates the bounds in which the modification occurred.
     *
     * <p>
     * This value is allowed to by <code>null</code> if this information is not
     * known.
     * </p>
     */
    private Envelope bounds;

    /**
     * Constructs a new FeatureEvent.
     *
     * @param FeatureSource<SimpleFeatureType, SimpleFeature> The DataStore that fired the event
     * @param eventType One of FEATURE_CHANGED, FEATURE_REMOVED or
     *        FEATURE_ADDED
     * @param bounds The area modified by this change
     */
    public FeatureEvent(FeatureSource<? extends FeatureType, ? extends Feature> featureSource,
            Type type, Envelope bounds) {
        super(featureSource);
        this.type = type;
        this.bounds = bounds;
    }
    
    /**
     * Constructs a new FeatureEvent.
     *
     * @param FeatureSource<SimpleFeatureType, SimpleFeature> The DataStore that fired the event
     * @param eventType One of FEATURE_CHANGED, FEATURE_REMOVED or
     *        FEATURE_ADDED
     * @param bounds The area modified by this change
     * @deprecated Please use FeatureEvent( FeatureSource, Type, Envelope )
     */
    public FeatureEvent(FeatureSource<? extends FeatureType, ? extends Feature> featureSource,
            int eventType, Envelope bounds) {
        super(featureSource);        
    	switch( eventType ){
    	case FEATURES_ADDED:
    		type = Type.ADDED;
    	    break;
    	case FEATURES_CHANGED:
    		type = Type.CHANGED;
    		break;
    	case FEATURES_REMOVED: 
    		type = Type.REMOVED;
    		break;
    	default:
    		type = Type.CHANGED;    			
    	}   
        this.bounds = bounds;
    }

    /**
     * Provides access to the FeatureSource<SimpleFeatureType, SimpleFeature> which fired the event.
     *
     * @return The FeatureSource<SimpleFeatureType, SimpleFeature> which was the event's source.
     */
    @SuppressWarnings("unchecked")
    public FeatureSource<? extends FeatureType, ? extends Feature> getFeatureSource() {
        return (FeatureSource<? extends FeatureType, ? extends Feature>) source;
    }

    /**
     * Provides information on the type of change that has occured. Possible
     * types are: add, remove, change
     *
     * @return an int which must be one of FEATURES_ADDED, FEATURES_REMOVED,
     *         FEATURES_CHANGED
     */
    public int getEventType() {
        return type.type;
    }
    
    /**
     * Provides information on the type of change that has occured. Possible
     * types are: add, remove, change
     *
     * @return Type
     */
    public Type getType() {
        return type;
    }

    /**
     * Provides access to the area modified (if known).
     *
     * @return A bounding box of the modifications or <code>null</code> if
     *         unknown.
     */
    public Envelope getBounds() {
        return bounds;
    }
}
