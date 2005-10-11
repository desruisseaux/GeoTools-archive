package org.geotools.feature.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.filter.Filter;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This is is a sample FeatureCollection implementation.
 * <p>
 * If you are a user - yes this FeatureCollection can be used to hold content in memory.
 * <p>
 * <p>
 * Note/Rant to data providers:
 * <p>
 * If you are a data provider (implementing a DataStore?) please don't use this class, you
 * should be doing your own thing.
 * </p>
 * As an example of doing your own thing, JDBC DataStore should be making a JDBCFeatureCollection
 * that just maintains a Filter defining the contents. Until such time as content is accessed for
 * the first time; at which point the collection can be relized by fetching a ResultSet.
 * If possible (for a paged result set) this may be cached for subsequent access.
 * <p>
 * Note that in a good implementation FeatureCollection will form a "chain" that is grounded by a
 * FeatureStore that holds the Transaction etc...  It is up to the implementor to decide what
 * to do when an FeatureCollection in the chain actually relizes content? Collections downstream
 * should be able to work off the FeatureCollection that is already relized.  Note additional API
 * may be used, as the FeatureCollection directly creates subCollections that act views on the origional
 * content.
 * </p>
 * If this is too complicated (aka you don't want to break out data mining techniques) please consider
 * working with the following - A three tiered approach with different assumptions at
 * each level:
 * <ol>
 * <li>Level 1 - All
 *   <br>Example:<code>FeatureStore.getFeatures()</code>
 *    <ul>
 *    <li>represents all the content, assume this cannot fit into memory.
 *    <li>don't cache unless high latency w/ modification notification or timestamp available (aka WFS)
 *    <li>use metadata for aggregate function results if available (bounds, count)
 *    </ul>
 * <li>Level 2 - Collection
 *   <br>Example:<code>FeatureStore.getFeatures().getSubCollection( Filter )</code>
 *   <br>Example:<code>FeatureStore.getFeatures( Filter )</code>
 *    <ul>
 *    <li>- represents the results of a query, may cache
 *    <li>- consider cache result of aggregate functions
 *    <li>- consider cache data (database resultset, local hsql cache, whatever)
 *    <li>- consider cache in memory (for small count) 
 *    </ul>
 * <li>Level 3 - Transient
 *   <br>Example:<code>FeatureStore.getFeatures().getSubCollection( Filter ).getSubCollection( Filter )</code>
 *   <br>Example:<code>FeatureCollection.getSubCollection( Filter )</code>
 *   <ul>
 *    <li>temporary collection (used to hold a Filter for subsequent opperation and cut down on API)
 *        <br><b>Example:</b><code>collection.getSubCollection( Filter ).remove()</code>
 *    <li>don't cache result, see above use
 *    <li>if getSubCollection( Filter) then they are breaking out assumption, take appropriate action.
 *    <br><b>Appropriate Action:</b?construct a Level 2 collection, and wrap it (aka switch over to delegation),
 *        and provide the client code with another Level 3
 *    <li>
 *    </ul>
 * </ul>
 * The above breakdown would be a good breakdown of abstract classes for implementors to work against. However
 * even if this is provided, there is no excuse not to do the right thing for your datasource. And for a data
 * source the right thing is never to burn memory.
 * </p>
 * 
 * @author Jody Garnett
 */
public class FeatureCollectionImpl extends ArrayList<Feature> implements FeatureCollection {
	FeatureImpl delegate;
	
    public FeatureCollectionImpl( String id, FeatureCollectionType type ){
    	delegate = new FeatureImpl( id, type );
    }
    
	public FeatureCollectionType getType() {
		return (FeatureCollectionType) delegate.getType();
	}

	public Iterator<Feature> features() {
		return iterator();
	}

	public void close(Iterator<Feature> iterator) {
		// nop
	}

	public String getID() {
		return delegate.getID();
	}

	/**
	 * Hard pressed to know what to return here ...
	 * <p>
	 * Return CRS used for data/bounds:
	 * <ol>
	 * <li>based on default Geometry, or GeometryType
	 * <li>based on member feature type
	 * </ol>
	 * </p>
	 * Given the custom of any and all attribute information in a 
	 * feature collection being derrived from the members the
	 * above order makes sense.
	 * </p>
	 */
	public CoordinateReferenceSystem getCRS() {
		CoordinateReferenceSystem crs = delegate.getCRS();
		if( crs != null) return crs;
		
		return null; // should process getType().getMemberDescriptor() 
	}

	public GeometryAttribute getDefaultGeometry() {
		return delegate.getDefaultGeometry();
	}

	public List<Attribute> get() {
		return delegate.get();
	}

	public void set(List<Attribute> attributes) {
		delegate.set( attributes );	
	}

	public List<AttributeType> types() {
		return delegate.types();
	}

	public List<Object> values() {
		return delegate.values();
	}

	public Object get(AttributeType type) {
		return delegate.get( type );
	}

	public void set(Object newValue) {
		delegate.set( newValue );
	}

	public FeatureCollection subCollection(Filter filter) {
		return new FeatureCollection(){
			
		};
	}
}
