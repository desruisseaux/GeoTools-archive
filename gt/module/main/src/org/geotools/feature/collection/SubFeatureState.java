package org.geotools.feature.collection;

import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This is *not* a Feature - it is a Delegate used by FeatureCollection
 * implementations as "mix-in", provides implementation of featureCollection
 * events, featureType, and attribute access backed by an origional
 * FeatureCollection.
 * <p>
 * To use cut&paste the following code exactly:<pre>
 * <code>
 * TBA
 * </code>
 * </p>
 * <p>
 * On the bright side this means we can "fix" all the SubFeatureCollection
 * implementationsin one fell-swoop.
 * </p>
 * 
 * @author Jody Garnett, Refractions Reserach, Inc.
 * @since GeoTools 2.2
 * @source $URL$
 */
public class SubFeatureState extends FeatureState {
	final FeatureCollection collection;
    
    final CollectionListener listener = new CollectionListener(){        
        public void collectionChanged( CollectionEvent tce ) {
            bounds = null;
        }        
    };
    
	public SubFeatureState( FeatureCollection collection, FeatureCollection sub ){
        super( sub );
		this.collection = collection; 
        collection.addListener( listener );
	}
    
    public void close(){
        if( listener != null ){
            collection.removeListener( listener );
        }        
        bounds = null;
    }

	//
	// FeatureCollection Event Support
	//

    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    public void addListener(CollectionListener listener) {
        collection.addListener( listener );
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(CollectionListener listener) {
        collection.removeListener( listener );
    }
    protected void fireChange( Feature[] features, int type ) {
        // 
    }
	//
	// Feature Methods
    //
    public FeatureType getFeatureType() {
        return collection.getFeatureType();
    }
    public FeatureType getChildFeatureType() {
        return collection.getSchema();
    }
    public String getId(){
        return collection.getID();
    }
    
}
