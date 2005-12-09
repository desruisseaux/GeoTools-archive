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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.data.FeatureResults;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.Filter;


/**
 * Represents a collection of features.
 * <p>
 * Implementations (and client code) should adhere to the rules set forth
 * by java.util.Collection. That is, some methods are
 * optional to implement, and may throw an UnsupportedOperationException.
 * </p>
 * <p>
 * FeatureCollection house rules:
 * <ul>
 * <li>FeatureCollection.close( iterator ) must be called (see example below)
 * <li>Features are not specifically ordered within the FeatureCollection (see FeatureList)
 * <li>Two instances cannot exist with the same Feature ID (Feature contract)
 * <li>(unsure) the same Instance can be in the collection more then once  
 * </ul>
 * In programmer speak a FeatureCollection is a "Bag" with an index based ID.
 * </p>
 * <p>
 * <h3>Life Cycle of Iterator</h3>
 * <p>
 * We have also adopted an additional constraint on the use of iterator.
 * You must call FeatureCollection.close( iterator ) to allow FeatureCollection
 * to clean up any operating system resources used to acces information.
 * </p>
 * <p>
 * Example (safe) use:<pre><code>
 * Iterator iterator = collection.iterator();
 * try {
 *     for( Iterator i=collection.iterator(); i.hasNext();){
 *          Feature feature = (Feature) i.hasNext();
 *          System.out.println( feature.getID() );
 *     }
 * }
 * finally {
 *     collection.close( iterator );
 * }
 * </code></pre>
 * </p>
 * <p>
 * Handy Tip: Although many resource backed collections will choose
 * to release resources at when the iterator has reached the end of its contents
 * this is not something you should rely on.
 * </p>
 * <h2>Notes for FeatureCollection Implementors</h2>
 * <p>
 * Many users will be treating this as a straight forward Collection,
 * there code will break often enough due to latency - try and close
 * up resources for them when you can detect that an Iterator is not
 * useful anymore.
 * </p> 
 * <p>
 * Collections are used in two fashions, basically as you see them,
 * and also as "range" for common opperations. You can see this with
 * List.subCollection( Filter ). Existing RnD effort is
 * going towards supporting this kind of use at the FeatureCollection
 * level.
 * </p>
 * 
 * @see java.util.Collection, org.geotools.Feature
 * @author Ian Turton, CCG
 * @author Rob Hranac, VFNY
 * @author Ian Schneider, USDA-ARS
 * @author Jody Garnett, Refractions Research, Inc.
 * @version $Id: FeatureCollection.java,v 1.12 2003/07/30 21:31:41 jmacgill Exp $
 */
public interface FeatureCollection extends Collection, FeatureResults, Feature {
    
    /**
     * Obtain a FeatureIterator of the Features within this collection.
     * <p>
     * The implementation of Collection must adhere to the rules of
     * fail-fast concurrent modification. In addition (to allow for
     * resource backed collections, the <code>close( Iterator )</code>
     * method must be called.
     * <p>
     * 
     * This is almost equivalent to:
     * <ul>
     * <li>a Type-Safe call to: 
     * <code>getAttribute(getFeatureType().getAttributeType(0).getName()).iterator();</code>.
     * <li>A Java 5:<code>Iterator&lt;Feature&gt;</code>
     * </ul>
     * </p>
     * Example (safe) use:<pre><code>
     * FeatureIterator iterator=collection.features();
     * try {
     *     while( iterator.hasNext()  ){
     *          Feature feature = iterator.next();
     *          System.out.println( feature.getID() );
     *     }
     * }
     * finally {
     *     collection.close( iterator );
     * }
     * </code></pre>
     * </p>
     * 
     * <p>
     * GML Note: The contents of this iterator are considered to be defined by
     * <b>featureMember</b> tags (and/or the single allowed <b>FeatureMembers</b> tag).
     * Please see getFeatureType for more details.
     * </p>
     * 
     * @return A FeatureIterator.
     */    
    FeatureIterator features();    
    
    /**
     * Returns an iterator over the contents of this collection.
     * <p>
     * Collection is not guarneteed to be ordered in any manner.
     * </p>
     * <p>
     * The implementation of Collection must adhere to the rules of
     * fail-fast concurrent modification. In addition (to allow for
     * resource backed collections, the <code>close( Iterator )</code>
     * method must be called.
     * <p>
     * </p>
     * Example (safe) use:<pre><code>
     * Iterator iterator = collection.iterator();
     * try {
     *     while( iterator.hasNext();){
     *          Feature feature = (Feature) iterator.hasNext();
     *          System.out.println( feature.getID() );
     *     }
     * }
     * finally {
     *     collection.close( iterator );
     * }
     * </code></pre>
     * </p>
     * <h3>GML Notes</h3>
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
     * @see features()
     */
    public Iterator iterator();
    
    /**
     * Clean up any resources assocaited with this iterator in a manner similar to JDO collections.
     * <p>
     * You must be sure to allow null values, this is because in a try/finally
     * block client code may not be sure if they have actualy succeed in
     * assign a value to an iterator they wish to ensure is closed.
     * By permiting null as an api we prevent a null check in lots of finally
     * statements.
     * </p>
     * <p>
     * Note: Because of FeatureReader using an interator internally,
     * there is only one implementation of this method that makes
     * any sense:<pre><code>
     * <b>public void</b> close( FeatureIterator iterator) {
     *     <b>if</b>( iterator != null ) iterator.close();
     * }
     * </code></pre>
     * </p>
     */
    public void close( FeatureIterator close );
    
    /**
     * Clean up after any resources assocaited with this itterator in a manner similar to JDO collections.
     * </p>
     * Example (safe) use:<pre><code>
     * Iterator iterator = collection.iterator();
     * try {
     *     for( Iterator i=collection.iterator(); i.hasNext();){
     *          Feature feature = (Feature) i.hasNext();
     *          System.out.println( feature.getID() );
     *     }
     * }
     * finally {
     *     collection.close( iterator );
     * }
     * </code></pre>
     * </p>
     * @param close
     */
    public void close( Iterator close );
    /**
     * Adds a listener for collection events.
     * <p>
     * When this collection is backed by live data the event notification
     * will follow the guidelines outlined by FeatureListner.
     * </p>
     *  
     * @param listener The listener to add
     * @throws NullPointerException If the listener is null.
     */
    void addListener(CollectionListener listener) throws NullPointerException;

    /**
     * Removes a listener for collection events.
     * 
     * @param listener The listener to remove
     * @throws NullPointerException If the listener is null.
     */
    void removeListener(CollectionListener listener) throws NullPointerException;

    /**
     * Gets a reference to the type of this feature collection.
     * <p>
     * There are several limitations on the use of FeatureType with respect to a FeatureCollection.
     * </p>
     * <p>
     * GML 3.x: all FeatureCollections decend from gml:AbstractFeatureCollectionType:
     * <ul>
     * <li>featureMember 0..* allows _Feature or xlink:ref
     * <li>featureMembers 0..1 contents treated as _Feature
     * </ul>
     * The contents defined in this manner is returned the collection
     * iterator() method.
     * </p>
     * <p>
     * GML 3.x: gml:AbstractFeatureCollectionType decends from gml:BoundedFeatureType:
     * <ul>
     * <li>metaDataProperty 0..*
     * <li>description 0..1
     * <li>name 0..*
     * <li>boundedBy 1..1 (required) 
     * <li>location 0..1
     * </ul>
     * The value of the boundedBy attribute should be derived from the contents
     * of the collection.
     * </p>
     * <h3>Implementation Notes</h3>
     * <p>
     * There is a difference between getFeatureType() and getSchema(), getSchema is named
     * for historical reasons and reprensets the LCD FeatureType that best represents the
     * contents of this collection.
     * <ul>
     * <li>The degenerate case returns the "_Feature" FeatureType, where the
     * onlything known is that the contents are Features.
     * <li>For a collection backed by a shapefiles (or database tables) the
     *     FeatureType returned by getSchema() will complete describe each and every child in the collection.
     * <li>For mixed content FeatureCollections you will need to check the FeatureType
     *     of each Feature as it is retrived from the collection
     * </ul>
     * </p>
     * 
     * @return A reference to this collections type
     */
    FeatureType getFeatureType();
    
    /**
     * The schema for the child features of this collection.
     * <p>
     * There is a difference between getFeatureType() and getSchema()represents the LCD
     * FeatureType that best represents the contents of this collection.
     * <ul>
     * <li>The degenerate case returns the "_Feature" FeatureType, where the
     * onlything known is that the contents are Features.
     * <li>For a collection backed by a shapefiles (or database tables) the FeatureType returned by getSchema() will
     * complete describe each and every child in the collection.
     * <li>For mixed content FeatureCollections you will need to check the FeatureType of each Feature as it
     * is retrived from the collection
     * </ul>
     * </p>
     * <p>
     * The method getSchema() is named for compatability with the geotools 2.0 API. In the
     * Geotools 2.2 time frame we should be able to replace this method with a careful check
     * of getFeatureType() and its attributes. 
     *  </p>
     * @return FeatureType describing the "common" schema to all child features of this collection 
     */
    FeatureType getSchema();
        
    /**
     * Will visit the contents of the feature collection.
     * <p>
     * Note: When performing aggregate calculations please consider using
     * the Filter/Expression/Function API as it may be optimized.
     * </p>
     * @param visitor
     * @throws IOException 
     */
    void accepts( FeatureVisitor visitor ) throws IOException;
    
    /**
     * FeatureCollection "view" indicated by provided filter.
     * <p>
     * The contents of the returned FeatureCollection are determined by
     * applying the provider Fitler to the entire contents of this
     * FeatureCollection. The result is "live" and modifications will
     * be shared.
     * <p>
     * This method is used cut down on the number of filter based methods
     * required for a useful FeatureCollection construct. The FeatureCollections
     * returned really should be considered as a temporary "view" used to
     * control the range of a removeAll, or modify opperation.
     * <p>
     * Example Use:
     * <pre><code>
     * collection.subCollection( filter ).clear();
     * </code></pre>
     * The above recommended use is agreement with the Collections API precident of
     * List.subList( start, end ). 
     * <p>
     * The results of subCollection:
     * <ul>
     * <li>are to be considered unordered
     * <li>may be an ordered FeatureList if requested when sortBy is indicated
     * </ul>
     * </p>
     * @see FeatureList
     * @param filter
     * @return FeatureCollection identified as subset.
     */
    public FeatureCollection subCollection( Filter filter );    
}