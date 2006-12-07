package org.geotools.xml.impl.jxpath;

import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.geotools.feature.FeatureType;

/**
 * Iterates over a single attribute of a feature type.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SingleFeatureTypeAttributeIterator implements NodeIterator {

	/**
	 * The feature type node pointer
	 */
	FeatureTypePointer pointer;
	/**
	 * The feature type
	 */
	FeatureType featureType;
	/**
	 * indedx of property
	 */
	int index;
	
	/**
	 * Creates the iteartor.
	 * 
	 * @param pointer The pointer to the feature.
	 * @param index The index of the property to iterate over
	 */
	public SingleFeatureTypeAttributeIterator( FeatureTypePointer pointer, int index ) {
		this.pointer = pointer;
		this.index = index;
		featureType = (FeatureType) pointer.getImmediateNode();
	}
	
	/**
	 * Always return 1, only a single property.
	 */
	public int getPosition() {
		return 1;
	}

	/**
	 * Return true if position == 1.
	 */
	public boolean setPosition(int position) {
		return position < 2;
	}

	/**
	 * Return a pointer to the property at the set index.
	 */
	public NodePointer getNodePointer() {
		return new FeatureTypeAttributePointer( pointer, index );
	}

}