package org.geotools.data.store;

import java.util.Iterator;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

/**
 * Iterator wrapper which re-types features on the fly based on a target 
 * feature type.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ReTypingIterator implements Iterator {

	/**
	 * The delegate iterator
	 */
	Iterator delegate;
	/**
	 * The target feature type
	 */
	FeatureType target;
	/**
	 * The matching types from target 
	 */
	AttributeType[] types;
	
	public ReTypingIterator( Iterator delegate, FeatureType source, FeatureType target ) {
		this.delegate = delegate;
		this.target = target;
		types = typeAttributes( source, target );
	}
	
	public Iterator getDelegate() {
		return delegate;
	}

	public void remove() {
		delegate.remove();
	}

	public boolean hasNext() {
		return delegate.hasNext();
	}

	public Object next() {
		Feature next = (Feature) delegate.next();
        String id = next.getID();

        Object[] attributes = new Object[types.length];
        String xpath;

        try {
			for (int i = 0; i < types.length; i++) {
			    xpath = types[i].getName();
			    attributes[i] = types[i].duplicate(next.getAttribute(xpath));
			}

			return target.create(attributes, id);
		} 
        catch (IllegalAttributeException e) {
        	throw new RuntimeException( e );
		}
	}
	
	 /**
     * Supplies mapping from origional to target FeatureType.
     * 
     * <p>
     * Will also ensure that origional can cover target
     * </p>
     *
     * @param target Desired FeatureType
     * @param origional Origional FeatureType
     *
     * @return Mapping from originoal to target FeatureType
     *
     * @throws IllegalArgumentException if unable to provide a mapping
     */
    protected AttributeType[] typeAttributes(FeatureType original,
        FeatureType target) {
        if (target.equals(original)) {
            throw new IllegalArgumentException(
                "FeatureReader allready produces contents with the correct schema");
        }

        if (target.getAttributeCount() > original.getAttributeCount()) {
            throw new IllegalArgumentException(
                "Unable to retype FeatureReader (origional does not cover requested type)");
        }

        String xpath;
        AttributeType[] types = new AttributeType[target.getAttributeCount()];

        for (int i = 0; i < target.getAttributeCount(); i++) {
            AttributeType attrib = target.getAttributeType(i);
            xpath = attrib.getName();
            types[i] = attrib;

            if (!attrib.equals(original.getAttributeType(xpath))) {
                throw new IllegalArgumentException(
                    "Unable to retype FeatureReader (origional does not cover "
                    + xpath + ")");
            }
        }

        return types;
    }

}
