package org.geotools.xml.impl.jxpath;

import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.geotools.feature.Feature;

public class FeaturePropertyIterator implements NodeIterator {

	/**
	 * The feature node pointer
	 */
	FeaturePointer pointer;
	/**
	 * The feature.
	 */
	Feature feature;
	/**
	 * current position
	 */
	int position;
	
	public FeaturePropertyIterator( FeaturePointer pointer ) {
		this.pointer = pointer;
		feature = (Feature) pointer.getImmediateNode();
		position = 1;
	}
	
	public int getPosition() {
		return position;
	}

	public boolean setPosition(int position) {
		this.position = position;
		return position <= feature.getNumberOfAttributes();
	}

	public NodePointer getNodePointer() {
		return new FeaturePropertyPointer( pointer, position - 1);
	}

}
