package org.opengis.feature;

import java.util.Iterator;

import org.opengis.feature.type.FeatureCollectionType;

/**
 * Represents a FeatureCollection.
 * <p>
 * Note the "attributes" available in a FeatureCollection are to be
 * considered a derrived quality based on the contents (or members) of the
 * collection.
 * </p>
 * @author Jody Garnett
 */
public interface FeatureCollection extends Feature {
	/**
	 * Restricted to return a FeatureCollectionType.
	 */
    FeatureCollectionType getType();

    /**
	 * Access contents of this collection.
	 * <p>
	 * Note XPath: the contents of a GML collection are represented by either
	 * featureMember or featureMembers. When interpretting an XPath expression,
	 * you should consider this function to visit both elements for you.
	 * </p>
	 * <p>
	 * XPath Mapping:
	 * <ul>
	 * <li>Preferred:<code>featureMember/*</code>
	 * <li>Legal:<code>featureMembers</code>
	 * </ul>
	 * @return Iterator over the contents of this feature collection
	 */
    Iterator<Feature> features();

    /**
     * You are required to close iterators after use.
     * <p>
     * Many FeatureCollections are backed by IO resources that need
     * to be returned to the opperating system after use.
     * </p>
     */
    void close( Iterator<Feature> iterator );
}

