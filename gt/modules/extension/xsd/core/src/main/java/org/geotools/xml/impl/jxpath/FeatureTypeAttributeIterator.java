package org.geotools.xml.impl.jxpath;

import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.geotools.feature.FeatureType;

/**
 * A special iteartor for iteating over the attributes of a feature type.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FeatureTypeAttributeIterator implements NodeIterator {

	/**
	 * The feature type node pointer
	 */
	FeatureTypePointer pointer;
	/**
	 * The feature type
	 */
	FeatureType featureType;
	/**
	 * current position
	 */
	int position;
	
	public FeatureTypeAttributeIterator( FeatureTypePointer pointer ) {
		this.pointer = pointer;
		featureType = (FeatureType) pointer.getImmediateNode();
		position = 1;
	}
	
	public int getPosition() {
		return position;
	}

	public boolean setPosition(int position) {
		this.position = position;
		return position <= featureType.getAttributeCount();
	}

	public NodePointer getNodePointer() {
		return new FeatureTypeAttributePointer( pointer, position - 1);
	}

	
}
