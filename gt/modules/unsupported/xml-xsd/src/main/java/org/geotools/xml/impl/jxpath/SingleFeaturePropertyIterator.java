package org.geotools.xml.impl.jxpath;

import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.geotools.feature.Feature;

/**
 * Iterates over a single property of a feature.
 * <p>
 * Will "iterate" over fid if index is set to -1.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SingleFeaturePropertyIterator implements NodeIterator {

	/**
	 * The feature node pointer
	 */
	FeaturePointer pointer;
	/**
	 * The feature.
	 */
	Feature feature;
	/**
	 * indedx of property
	 */
	int index;
	
	/**
	 * Creates the iteartor.
	 * 
	 * @param pointer The pointer to the feature.
	 * @param index The index of the property to iterate over, or -1 for the feature id.
	 */
	public SingleFeaturePropertyIterator( FeaturePointer pointer, int index ) {
		this.pointer = pointer;
		this.index = index;
		feature = (Feature) pointer.getImmediateNode();
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
		return new FeaturePropertyPointer( pointer, index );
	}

}
