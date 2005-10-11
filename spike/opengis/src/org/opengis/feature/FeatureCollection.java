package org.opengis.feature;

import java.util.Collection;
import java.util.Iterator;

import org.geotools.filter.Filter;
import org.opengis.feature.type.FeatureCollectionType;

/**
 * Represents a FeatureCollection (explicitly a Collection<Feature>.
 * <p>
 * Note the "attributes" available in a FeatureCollection are to be
 * considered a derrived quality based on the contents (or members) of the
 * collection. An "empty" FeatureCollection should not exist.
 * </p>
 * @author Jody Garnett
 */
public interface FeatureCollection extends Feature, Collection<Feature> {
	/**
	 * Restricted to return a FeatureCollectionType.
	 */
    public FeatureCollectionType getType();

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
    public Iterator<Feature> features();
    
    /**
     * You are required to close iterators after use.
     * <p>
     * Many FeatureCollections are backed by IO resources that need
     * to be returned to the opperating system after use.
     * </p>
     */
    public void close( Iterator<Feature> iterator );
    
    /**
     * FeatureCollection "view" indicated by provided filter.
     * <p>
     * The contents of the returned FeatureCollection are determined by
     * applying the provider Fitler to the entire contents of this FeatureCollection.
     * The result is "live" aqnd modifications will be shared.
     * <p>
     * This method is used cut down on the number of filter based methods
     * required for a useful FeatureCollection construct. The FeatureCollections
     * returned really should be considered as a temporary "view" used to
     * control the range of a removeAll, or modify opperation.
     * <p>
     * Example Use:
     * <pre><code>
     * collection.subCollection( filter ).removeAll();
     * </code></pre>
     * The above recommended use is agreement with the Collections API precident of
     * List.subList( start, end ). 
     * @param filter
     * @return FeatureCollection identified as subset.
     */
    public FeatureCollection subCollection( Filter filter );
}
