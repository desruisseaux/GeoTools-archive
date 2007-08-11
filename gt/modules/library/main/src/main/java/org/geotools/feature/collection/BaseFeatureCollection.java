package org.geotools.feature.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureCollectionImpl;
import org.geotools.feature.simple.SimpleFeatureCollectionTypeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Base feature collection for geotools feature collection implementations as 
 * the transisition to the geoapi feature model is made.
 * <p>
 * The primary role of this base collection is to implement the feature aspect 
 * of the feature collection api, leaving the collection aspect to subclasses. 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 * @since 2.5
 */
public abstract class BaseFeatureCollection extends SimpleFeatureCollectionImpl
		implements FeatureCollection {

	/**
	 * logger
	 */
	protected static Logger LOGGER = Logger.getLogger("org.geotools.feature");
	/**
	 * listeners
	 */
	protected List listeners = new ArrayList();
	
	/**
	 * Constructs the collection with an id.
	 * 
	 * @param id The identifier of the feature collection.
	 */
    protected BaseFeatureCollection( String id ){
    	this(id,(FeatureType)null);
    }
    
    /**
	 * Constructs the collection with an id and a single member type.
	 * 
	 * @param id The identifier of the feature collection.
	 * @param memberType The type of the members of the collection.
	 */
    protected BaseFeatureCollection( String id, FeatureType memberType ){
    	super( new BaseFeatureCollectionType(memberType), id );
    }
    
    /**
     * Constructs the collection with an id and pre-existing type.
     * 
     * @param id The identifier of the feature collection.
     * @param type The type of the feature collection.
     */
    protected BaseFeatureCollection( String id, SimpleFeatureCollectionType type ) {
    	super( type, id );
    }

    //
    // FeatureCollection
    //
    final public void addListener(CollectionListener listener) throws NullPointerException {
    	listeners.add(listener);
    }
    
    final public void removeListener(CollectionListener listener) throws NullPointerException {
    	listeners.remove(listener);
    }
    
    public FeatureType getSchema() {
    	return (FeatureType) getMemberType();
    }
    
    //
    // Feature
    //
	public Object getAttribute(int index) {
		return getValue(index);
	}

	public Object getAttribute(String xPath) {
		return getValue(xPath);
	}

	public Object[] getAttributes(Object[] attributes) {
		Object[] retArray;

        List values = getValues();
        if (attributes == null) {
            //retArray = new Object[attributes.length];
        	retArray = new Object[values.size()];
        } else {
            retArray = attributes;
        }

        //System.arraycopy(attributes, 0, retArray, 0, attributes.length);
        //return retArray;
        return values.toArray( attributes );
	}

	@Override
	public SimpleFeatureCollectionType getType() {
	    return (SimpleFeatureCollectionType) super.getType();
	}
	public BaseFeatureCollectionType getFeatureType() {
		return (BaseFeatureCollectionType) getType();
	}

	public Geometry getPrimaryGeometry() {
		return (Geometry) getDefaultGeometryValue();
	}

	public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		setValue(position,val);
	}

	public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
		setValue(xPath,attribute);
	}

	public void setPrimaryGeometry(Geometry geometry) throws IllegalAttributeException {
		setDefaultGeometryValue(geometry);
	}
    
	/**
	 * Subclasses need to override this.
	 */
	public ReferencedEnvelope getBounds() {
		throw new UnsupportedOperationException("subclasses should override");
	}
}
