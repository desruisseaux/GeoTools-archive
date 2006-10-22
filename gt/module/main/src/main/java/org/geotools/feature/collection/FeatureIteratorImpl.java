/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.feature.collection;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureCollection;

/**
 * A convenience class for dealing with FeatureCollection Iterators. DOES NOT
 * implement Iterator.
 * <p>
 * We are sorry but this does not implement Iteartor<Feature>, although it should
 * be a drop in replacement when Geotools is able to upgrade to Java 5.
 * </p>
 * @author Ian Schneider
 * @source $URL$
 */
public class FeatureIteratorImpl implements FeatureIterator {
    /** The iterator from the FeatureCollection to return features from. */
     java.util.Iterator iterator;
     FeatureCollection collection;
     
    /**
     * Create a new FeatureIterator using the Iterator from the given
     * FeatureCollection.
     *
     * @param collection The FeatureCollection to perform the iteration on.
     */
    public FeatureIteratorImpl(FeatureCollection collection) {
        this.collection = collection;
        this.iterator = collection.iterator();
    }

    /**
     * Does another Feature exist in this Iteration.
     * <p>
     * Iterator defin: Returns true if the iteration has more elements. (In other words, returns true if next would return an element rather than throwing an exception.)
     * </p>
     * @return true if more Features exist, false otherwise.
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Get the next Feature in this iteration.
     *
     * @return The next Feature
     *
     * @throws java.util.NoSuchElementException If no more Features exist.
     */
    public Feature next() throws java.util.NoSuchElementException {
        return (Feature) iterator.next();
    }
    /**
     * Required so FeatureCollection classes can implement close( FeatureIterator ).
     */
    public void close(){
        if( iterator != null ){
            collection.close( iterator );
            iterator = null;
            collection = null;
        }
    }
}
