package org.opengis.feature;

import java.util.Iterator;

import org.opengis.feature.type.FeatureCollectionType;

public interface FeatureCollection extends Feature {
    FeatureCollectionType getType();

    /**
	 * Access contents of this collection.
	 * <p>
	 * Note XPath: the contents of a GML collection are represented by either
	 * featureMember or featureMembers. When interpretting an XPath expression,
	 * you should consider this function to visit both elements for you.
	 */
    Iterator<Feature> features();

    /** You are required to close iterators after use */
    void close( Iterator<Feature> iterator );
}

