package org.geotools.feature.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.collection.ResourceCollection;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.type.FeatureAttributeType;
import org.geotools.xml.gml.GMLSchema;

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
 * @source $URL$
 */
public class BaseFeatureState extends FeatureState {
    //final ResourceCollection collection;
	final FeatureType featureType;
	final FeatureType schema;	
	String id;
    
    /** Internal listener storage list */
    private List listeners = new ArrayList(2);

    
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
	public static FeatureType featureType( FeatureType schema ){
		List ats = new LinkedList();
        ats.add(new FeatureAttributeType(schema.getTypeName(), schema,false));
        return new DefaultFeatureType("AbstractFeatureColletionType",GMLSchema.NAMESPACE,ats,new LinkedList(),null);        
	}

	public BaseFeatureState( ResourceCollection collection, FeatureType schema ){
		this( collection, featureType( schema ), schema );				
	}
	public BaseFeatureState( ResourceCollection collection, FeatureType featureType, FeatureType schema ){
        super( collection );
		//this.collection = collection;
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
    	listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(CollectionListener listener) {
    	listeners.remove(listener);
    }
    
    /**
     * To let listeners know that something has changed.
     */
    protected void fireChange(Feature[] features, int type) {
    	bounds = null; // must recalculate bounds

        CollectionEvent cEvent = new CollectionEvent( (FeatureCollection) data, features, type);
        
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
    public FeatureType getFeatureType() {
        return featureType;
    }
    public FeatureType getChildFeatureType() {
        return schema;
    }    
    public String getId() {
        return id;
    }
    public void setId( String id ){
        this.id = id;
    }    
}
