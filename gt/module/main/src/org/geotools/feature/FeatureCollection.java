/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.feature;

import java.util.Iterator;

import org.geotools.data.FeatureResults;

import com.vividsolutions.jts.geom.Envelope;


/** Represents a collection of features. Implementations and client code should
 * adhere to the rules set forth by java.util.Collection. That is, some methods are
 * optional to implement, and may throw an UnsupportedOperationException.
 * @see java.util.Collection
 * @author Ian Turton, CCG
 * @author Rob Hranac, VFNY
 * @author Ian Schneider, USDA-ARS
 * @version $Id: FeatureCollection.java,v 1.12 2003/07/30 21:31:41 jmacgill Exp $
 */
public interface FeatureCollection extends java.util.Collection, Feature, FeatureResults {
    /**
     * All iterators must be closed using the {@linkplain FeatureCollection#close(Iterator)} method.
     * @see java.lang.Iterable#iterator()
     */
    Iterator iterator();
    
    /** 
     * Obtain a FeatureIterator of the Feature Objects contained within this
     * collection. The implementation of Collection must adhere to the rules of
     * fail-fast concurrent modification.
     * 
     * This is almost equivalent to a Type-Safe call to 
     * getAttribute(getFeatureType().getAttributeType(0).getName()).iterator();
     * 
     * @return A FeatureIterator.
     */    
    FeatureIterator features();

    /** Adds a listener for collection events.
     * @param listener The listener to add
     * @throws NullPointerException If the listener is null.
     */
    void addListener(CollectionListener listener) throws NullPointerException;

    /** Removes a listener for collection events.
     * @param listener The listener to remove
     * @throws NullPointerException If the listener is null.
     */
    void removeListener(CollectionListener listener) throws NullPointerException;

    /**
     * Gets a reference to the schema for this feature.
     * 
     * Generally this schema will have one AttributeType (the FeatureType of the children) with multiplicity *.
     *
     * @return A reference to this feature's schema.
     */
    FeatureType getFeatureType();
    
    /**
     * Close the iterator if a connection is open that requires closing.  All iterators must be
     * closed.  
     * 
     * @param iterator
     */
    void close( Iterator iterator);   
    
    /**
     * Close the iterator if a connection is open that requires closing.  All iterators must be
     * closed.  
     * 
     * @param iterator
     */
    void close( FeatureIterator iterator);
    
}
