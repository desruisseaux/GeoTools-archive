package org.geotools.feature.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.data.collection.ResourceCollection;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.type.FeatureAttributeType;
import org.geotools.xml.gml.GMLSchema;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * This is *not* a Feature - it is a Delegate used by FeatureCollection
 * implementations as "mix-in", provides implementation of featureCollection
 * events, featureType, and attribute access.
 * <p>
 * To use cut&paste the following code exactly:<pre>
 * <code>
 * 
 * </code>
 * </p>
 * <p>
 * On the bright side this means we can "fix" all the FeatureCollection implementations
 * in one fell-swoop.
 * </p>
 * 
 * @author Jody Garnett, Refractions Reserach, Inc.
 * @since GeoTools 2.2
 */
public class FeatureDelegate {
	final ResourceCollection collection;
	final FeatureType featureType;
	final FeatureType schema;	
	
    /** Internal listener storage list */
    private List listeners = new ArrayList(2);

    /** Internal envelope of bounds. */
    private Envelope bounds = null;
    
	/**
 	 * Construct a fake FeatureType of this FeatureCollection.
  	 * <p>
	 * Unless a FeatureType was provided during consturction (or this method is
	 * overriden) a FeatureType will be generated based on getSchmea according
	 * to the following assumptions:
	 * <ul>
	 * <li>FeatureType is gml:AbstractFeatureCollectionType
	 * <li>first attribute is getSchema.typeName
	 * <li>the attribute FeatureType the same as returned by getSchema()
	 * </ul>
	 * </p>
	 * 
	 */
	private static FeatureType featureType( FeatureType schema ){
		List ats = new LinkedList();
        ats.add(new FeatureAttributeType(schema.getTypeName(), schema,false));
        return new DefaultFeatureType("AbstractFeatureColletionType",GMLSchema.NAMESPACE,ats,new LinkedList(),null);        
	}

	public FeatureDelegate( ResourceCollection collection, FeatureType schema ){
		this( collection, featureType( schema ), schema );				
	}
	public FeatureDelegate( ResourceCollection collection, FeatureType featureType, FeatureType schema ){
		this.collection = collection;
		this.featureType = featureType;
		this.schema = schema;
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
    	if( !(collection instanceof FeatureCollection )) return;    	
        listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(CollectionListener listener) {
    	if( !(collection instanceof FeatureCollection )) return;    	
        listeners.remove(listener);
    }
    
    /**
     * To let listeners know that something has changed.
     */
    protected void fireChange(Feature[] features, int type) {
    	if( !(collection instanceof FeatureCollection )) return;
    	
        bounds = null; // must recalculate bounds

        CollectionEvent cEvent = new CollectionEvent( (FeatureCollection) collection, features, type);
        
        for (int i = 0, ii = listeners.size(); i < ii; i++) {
            ((CollectionListener) listeners.get(i)).collectionChanged(cEvent);
        }
    }
        
    protected void fireChange(Feature feature, int type) {
        fireChange(new Feature[] {feature}, type);
    }
    
    protected void fireChange(Collection coll, int type) {
        Feature[] features = new Feature[coll.size()];
        features = (Feature[]) coll.toArray(features);
        fireChange(features, type);
    }
	//
	// Feature Methods
    //    
    /**
     * Gets the bounding box for the features in this feature collection.
     * 
     * @return the envelope of the geometries contained by this feature
     *         collection.
     */
    public Envelope getBounds() {
        if (bounds == null) {
            bounds = new Envelope();
            Iterator i = collection.iterator();
            try {            	
	            while(i.hasNext()) {
	                Envelope geomBounds = ((Feature) i.next()).getBounds();                
	                if ( ! geomBounds.isNull() ) {
	                    bounds.expandToInclude(geomBounds);
	                }
	            }
            }
            finally {
            	collection.close( i );
            }
        }
        return bounds;
    }

    public synchronized FeatureType getFeatureType() {
        return featureType;
    }
    
    public Object[] getAttributes( Object[] attributes ) {
        List list = (List) getAttribute( 0 );
        return list.toArray( attributes );
    }
    
    /**
     * Not really interested yet .. 
     */
    public Object getAttribute( String xPath ) {
        if(xPath.indexOf(featureType.getTypeName())>-1)
            if(xPath.endsWith("]")){
                return getAttribute(0);
            } else {
                return getAttribute(0);
            }
        return null;
    }
    
    public Object getAttribute( int index ) {
        if(index == 0){
        	Iterator i = collection.iterator();
        	List list = new ArrayList();        	
            try {                
                while( i.hasNext() ){
                    Feature feature = (Feature) i.next();                    
                    list.add( feature );
                }
                return list;
            } catch (NoSuchElementException e) {
                return Collections.EMPTY_LIST; // could not find contents
            }
            finally {
                collection.close( i );
            }
        }
        return null;
    }
    
    public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		if(position == 0 && val instanceof Collection){
            Collection newStuff = (Collection) val;
			if( !isFeatures( newStuff )) {
				throw new IllegalAttributeException("Content must be features");
			}            
            collection.clear(); // clean out previous contents!
            Iterator i = newStuff.iterator();
            try {
	            while( i.hasNext() ){
	                Feature feature = (Feature) i.next();                
	                if( collection instanceof FeatureCollection ){
	                	feature.setParent( (FeatureCollection) collection );
	                }
	                collection.add( feature );
	            }
            }
            finally {
                collection.close( i );
            }
			//fireChange(nw,0);
		}
	}
       
	public int getNumberOfAttributes() {
		return featureType.getAttributeCount();		
	}

    public void setAttribute( String xPath, Object attribute ) throws IllegalAttributeException {
        if(xPath.indexOf(featureType.getTypeName())>-1){
            if(xPath.endsWith("]")){
                // TODO get index and grab it
            } else {
                setAttribute(0,attribute);
            }
        }
    }

    public Geometry getDefaultGeometry() {
        return null;
    }

    public void setDefaultGeometry( Geometry geometry ) throws IllegalAttributeException {
        throw new IllegalAttributeException( "DefaultGeometry not supported" );
    }
    
    //
    // Utility Methods
    //
    /** 
     * Get the set of fids for the provided collection.
     * <p>
     * By doing a quick pass through the collection we can  do
     * comparisons based on Feature ID (rather then collection
     * membership).
     * </p>
     * <p>
     * A subclass that tracks its FID information may wish to override
     * this method.
     * </p>
     */
    public static Set fids( Collection stuff ){
        if( stuff instanceof DefaultFeatureCollection ){
            DefaultFeatureCollection features = (DefaultFeatureCollection) stuff;
            return features.fids();
        }
        
        Iterator iterator = stuff.iterator();
        Set fids = new HashSet();
        try {
            while( iterator.hasNext() ){
                Feature feature = (Feature) iterator.next();
                fids.add( feature.getID() );
            }
        }
        finally {
            if( stuff instanceof ResourceCollection){
                ((ResourceCollection) stuff).close( iterator );
            }
        }
        return fids;
    }
    
    /** Test if collection is all features! */
    public static boolean isFeatures( Collection stuff ){
        if( stuff instanceof FeatureCollection ) return true;
        
        Iterator i = stuff.iterator();
        try {
	        while( i.hasNext() ){
	            if(!(i.next() instanceof Feature))
	                return false;
	        }
        }
        finally {
            if( stuff instanceof ResourceCollection){
                ((ResourceCollection) stuff).close( i );
            }
        }
        return true;
    }    
    
}