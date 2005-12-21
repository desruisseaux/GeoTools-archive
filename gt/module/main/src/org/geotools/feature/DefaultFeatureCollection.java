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

// J2SE interfaces
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.feature.collection.FeatureState;
import org.geotools.feature.collection.FeatureIteratorImpl;
import org.geotools.feature.collection.SubFeatureCollection;
import org.geotools.feature.type.FeatureAttributeType;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.Filter;
import org.geotools.filter.SortBy;
import org.geotools.filter.SortBy2;
import org.geotools.util.NullProgressListener;
import org.geotools.util.ProgressListener;
import org.geotools.xml.gml.GMLSchema;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * A basic implementation of FeatureCollection which use a {@link TreeMap} for
 * its internal storage.
 * <p>
 * This should be considered a MemoryFeatureCollection.
 * </p>
 * 
 * @author Ian Schneider
 * @version $Id: DefaultFeatureCollection.java,v 1.6 2003/12/04 23:17:23 aaime Exp $
 */
public class DefaultFeatureCollection implements FeatureCollection {
    
    /**
     * Contents of collection, referenced by FeatureID.
     * <p>
     * This use will result in collections that are sorted by FID, in keeping
     * with shapefile etc...
     * </p>
     */
    private SortedMap contents = new TreeMap();
    
    /** Internal listener storage list */
    private List listeners = new ArrayList(2);

    /** Internal envelope of bounds. */
    private Envelope bounds = null;
        
    private String id; /// fid

    /**
     * This constructor is protected to discourage direct usage...
     * <p>
     * Opportunistic reuse is encouraged, but only for the purposes
     * of testing or other specialized uses. Normal creation should
     * occur through <code>org.geotools.core.FeatureCollections.newCollection()</code>
     * allowing applications to customize any generated collections.
     * </p>
     * @param id may be null ... feature id
     * @param FeatureType optional, may be null
     */
    protected DefaultFeatureCollection(String id, FeatureType featureType) {
    	this.id = id;
    	if(featureType == null){
    		List ats = new LinkedList();
    		ats.add(new FeatureAttributeType("_Feature",new DefaultFeatureType("AbstractFeatureType",GMLSchema.NAMESPACE,new LinkedList(),new LinkedList(),null),false));
    		featureType = new DefaultFeatureType("AbstractFeatureCollectionType",GMLSchema.NAMESPACE,ats,new LinkedList(),null);
    	}
    	this.featureType = featureType;
        this.childType = null; // no children yet
    }
    private FeatureType featureType;
    private FeatureType childType;

    public FeatureType getSchema() {
        if( childType == null ) {
            // no children guess Features are okay then
            new DefaultFeatureType("AbstractFeatureType",GMLSchema.NAMESPACE,new LinkedList(),new LinkedList(),null); 
        }
        return childType;
    }
    
    /**
     * Gets the bounding box for the features in this feature collection.
     * 
     * @return the envelope of the geometries contained by this feature
     *         collection.
     */
    public Envelope getBounds() {
        if (bounds == null) {
            bounds = new Envelope();

            for (Iterator i = contents.values().iterator(); i.hasNext();) {
                Envelope geomBounds = ((Feature) i.next()).getBounds();                
                // IanS - as of 1.3, JTS expandToInclude ignores "null" Envelope
                // and simply adds the new bounds...
                // This check ensures this behavior does not occur.
                if ( ! geomBounds.isNull() ) {
                    bounds.expandToInclude(geomBounds);
                }
            }
        }
        return bounds;
    }

    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    public void addListener(CollectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(CollectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * To let listeners know that something has changed.
     */
    protected void fireChange(Feature[] features, int type) {
        bounds = null;

        CollectionEvent cEvent = new CollectionEvent(this, features, type);
        
        for (int i = 0, ii = listeners.size(); i < ii; i++) {
            ((CollectionListener) listeners.get(i)).collectionChanged(cEvent);
        }
    }
        
    protected void fireChange(Feature feature, int type) {
        fireChange(new Feature[] {feature}, type);
    }
    
    protected void fireChange(Collection coll, int type) {
        Feature[] features = new Feature[coll.size()];
        features = (Feature[]) coll.toArray(features);
        fireChange(features, type);
    }

    /**
     * Ensures that this collection contains the specified element (optional
     * operation).  Returns <tt>true</tt> if this collection changed as a
     * result of the call.  (Returns <tt>false</tt> if this collection does
     * not permit duplicates and already contains the specified element.)
     * 
     * <p>
     * Collections that support this operation may place limitations on what
     * elements may be added to this collection.  In particular, some
     * collections will refuse to add <tt>null</tt> elements, and others will
     * impose restrictions on the type of elements that may be added.
     * Collection classes should clearly specify in their documentation any
     * restrictions on what elements may be added.
     * </p>
     * 
     * <p>
     * If a collection refuses to add a particular element for any reason other
     * than that it already contains the element, it <i>must</i> throw an
     * exception (rather than returning <tt>false</tt>).  This preserves the
     * invariant that a collection always contains the specified element after
     * this call returns.
     * </p>
     *
     * @param o element whose presence in this collection is to be ensured.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     */
    public boolean add(Object o) {
        return add((Feature)o,true);
    }
    protected boolean add(Feature feature, boolean fire) {
        
        // This cast is neccessary to keep with the contract of Set!
        if( feature == null ) return false; // cannot add null!
        final String ID = feature.getID();
        if( ID == null ) return false; // ID is required!
        if( contents.containsKey( ID ) ) return false; // feature all ready present
        
        //TODO check inheritance with FeatureType here!!!
        feature.setParent(this);        
        contents.put( ID, feature );
        if(fire) {
                fireChange(feature, CollectionEvent.FEATURES_ADDED);
        }
        return true;        
    }

    /**
     * Adds all of the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in
     * progress. (This implies that the behavior of this call is undefined if
     * the specified collection is this collection, and this collection is
     * nonempty.)
     *
     * @param collection elements to be inserted into this collection.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     *
     * @see #add(Object)
     */
    public boolean addAll(Collection collection) {
    	//TODO check inheritance with FeatureType here!!!
        boolean changed = false;
        
        Iterator iterator = collection.iterator();
        try {
            List featuresAdded = new ArrayList(collection.size());
            while (iterator.hasNext()) {
                Feature f = (Feature) iterator.next();
                boolean added = add(f,false);
                changed |= added;
                
                if(added) featuresAdded.add(f);
            }
    
            if (changed) {
                fireChange(featuresAdded, CollectionEvent.FEATURES_ADDED);
            }
    
            return changed;
        }
        finally {
            if( collection instanceof FeatureCollection ){
                ((FeatureCollection)collection).close( iterator );
            }
        }
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * This collection will be empty after this method returns unless it
     * throws an exception.
     */
    public void clear() {
        if(contents.isEmpty() ) return;
            
        Feature[] oldFeatures = new Feature[contents.size()];
        oldFeatures = (Feature[]) contents.values().toArray(oldFeatures);

        contents.clear();
        fireChange(oldFeatures, CollectionEvent.FEATURES_REMOVED);
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this collection
     * contains at least one element <tt>e</tt> such that <tt>(o==null ?
     * e==null : o.equals(e))</tt>.
     *
     * @param o element whose presence in this collection is to be tested.
     *
     * @return <tt>true</tt> if this collection contains the specified element
     */
    public boolean contains(Object o) {
        // The contract of Set doesn't say we have to cast here, but I think its
        // useful for client sanity to get a ClassCastException and not just a
        // false.
        if( !(o instanceof Feature) ) return false;
        
        Feature feature = (Feature) o;
        final String ID = feature.getID();
            
        return contents.containsKey( ID ); // || contents.containsValue( feature );        
    }
    
    /**
     * Test for collection membership.
     * 
     * @param collection
     * @return true if collection is completly covered
     */
    public boolean containsAll( Collection collection ) {
        Iterator iterator = collection.iterator();
        try {
            while (iterator.hasNext()) {
                Feature feature = (Feature) iterator.next();
                if( !contents.containsKey( feature.getID() )){
                    return false;
                }                
            }
            return true;
        }
        finally {
            if( collection instanceof FeatureCollection ){
                ((FeatureCollection)collection).close( iterator );
            }
        }
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    /**
     * Returns an iterator over the elements in this collection.  There are no
     * guarantees concerning the order in which the elements are returned
     * (unless this collection is an instance of some class that provides a
     * guarantee).
     *
     * @return an <tt>Iterator</tt> over the elements in this collection
     */
    public Iterator iterator() {
        final Iterator iterator = contents.values().iterator();

        return new Iterator() {
                Feature currFeature = null;
            
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public Object next() {
                    currFeature = (Feature) iterator.next();
                    return currFeature;
                }

                public void remove() {
                    iterator.remove();
                    fireChange(currFeature, CollectionEvent.FEATURES_REMOVED);
                }
            };
    }

    /**
     * Gets a FeatureIterator of this feature collection.  This allows
     * iteration without having to cast.
     *
     * @return the FeatureIterator for this collection.
     */
    public FeatureIterator features() {
        return new FeatureIteratorImpl(this);
    }

    /**
     * Removes a single instance of the specified element from this collection,
     * if it is present (optional operation).  More formally, removes an
     * element <tt>e</tt> such that <tt>(o==null ?  e==null :
     * o.equals(e))</tt>, if this collection contains one or more such
     * elements.  Returns true if this collection contained the specified
     * element (or equivalently, if this collection changed as a result of the
     * call).
     *
     * @param o element to be removed from this collection, if present.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     */
    public boolean remove(Object o) {
        if( !(o instanceof Feature)) return false;
        
        Feature f = (Feature) o;
        boolean changed = contents.values().remove(f);

        if (changed) {
            fireChange(f, CollectionEvent.FEATURES_REMOVED);
        }
        return changed;
    }

    /**
     * Removes all this collection's elements that are also contained in the
     * specified collection (optional operation).  After this call returns,
     * this collection will contain no elements in common with the specified
     * collection.
     *
     * @param collection elements to be removed from this collection.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection collection) {
        boolean changed = false;
        Iterator iterator = collection.iterator();
        try {
            List removedFeatures = new ArrayList(collection.size());
            while (iterator.hasNext()) {
                Feature f = (Feature) iterator.next();
                boolean removed = contents.values().remove(f);
                
                if(removed) {
                    changed = true;
                    removedFeatures.add(f);
                }
            }
    
            if (changed) {
                fireChange(removedFeatures, CollectionEvent.FEATURES_REMOVED);
            }
    
            return changed;
        }
        finally {
            if( collection instanceof FeatureCollection ){
                ((FeatureCollection)collection).close( iterator );
            }
        }
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this collection all of its elements that are not contained in the
     * specified collection.
     *
     * @param collection elements to be retained in this collection.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection collection) {
        List removedFeatures = new ArrayList(contents.size() - collection.size());
        boolean modified = false;
        
        for(Iterator it = contents.values().iterator(); it.hasNext(); )  {
            Feature f = (Feature) it.next();
            if(!collection.contains(f)) {
                it.remove();
                modified = true;
                removedFeatures.add(f);
            }
        }
        
        if (modified) {
            fireChange(removedFeatures, CollectionEvent.FEATURES_REMOVED);
        }

        return modified;
    }

    /**
     * Returns the number of elements in this collection.  If this collection
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this collection
     */
    public int size() {
        return contents.size();
    }

    /**
     * Returns an array containing all of the elements in this collection.  If
     * the collection makes any guarantees as to what order its elements are
     * returned by its iterator, this method must return the elements in the
     * same order.
     * 
     * <p>
     * The returned array will be "safe" in that no references to it are
     * maintained by this collection.  (In other words, this method must
     * allocate a new array even if this collection is backed by an array).
     * The caller is thus free to modify the returned array.
     * </p>
     * 
     * <p>
     * This method acts as bridge between array-based and collection-based
     * APIs.
     * </p>
     *
     * @return an array containing all of the elements in this collection
     */
    public Object[] toArray() {
        return contents.values().toArray();
    }

    /**
     * Returns an array containing all of the elements in this collection; the
     * runtime type of the returned array is that of the specified array. If
     * the collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.
     * 
     * <p>
     * If this collection fits in the specified array with room to spare (i.e.,
     * the array has more elements than this collection), the element in the
     * array immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of this
     * collection <i>only</i> if the caller knows that this collection does
     * not contain any <tt>null</tt> elements.)
     * </p>
     * 
     * <p>
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     * </p>
     * 
     * <p>
     * Like the <tt>toArray</tt> method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs
     * </p>
     * 
     * <p>
     * Suppose <tt>l</tt> is a <tt>List</tt> known to contain only strings. The
     * following code can be used to dump the list into a newly allocated
     * array of <tt>String</tt>:
     * <pre>
     *     String[] x = (String[]) v.toArray(new String[0]);
     * </pre>
     * </p>
     * 
     * <p>
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     * </p>
     *
     * @param a the array into which the elements of this collection are to be
     *        stored, if it is big enough; otherwise, a new array of the same
     *        runtime type is allocated for this purpose.
     *
     * @return an array containing the elements of this collection
     */
    public Object[] toArray(Object[] a) {
        return contents.values().toArray(a);
    }

	/* (non-Javadoc)
	 * @see org.geotools.feature.FeatureCollection#getFeatureType()
	 */
	public FeatureType getFeatureType() {
		return featureType;
	}

	private FeatureCollection parent;
	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getParent()
	 */
	public FeatureCollection getParent() {
		// TODO deal with listeners?
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setParent(org.geotools.feature.FeatureCollection)
	 */
	public void setParent(FeatureCollection collection) {
		parent = collection;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getID()
	 */
	public String getID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttributes(java.lang.Object[])
	 */
	public Object[] getAttributes(Object[] attributes) {
        return toArray( attributes );		
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String xPath) {
		if(xPath.indexOf(featureType.getTypeName())>-1)
			if(xPath.endsWith("]")){
				return contents.values(); // TODO get index and grab it                
			}
            else{
				return contents.values();
			}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttribute(int)
	 */
	public Object getAttribute(int index) {
		if(index == 0){
			return contents.values();
        }
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setAttribute(int, java.lang.Object)
	 */
	public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		if(position == 0 && val instanceof List){
            List nw = (List)val;
			if( !FeatureState.isFeatures( nw )) return;
            
            contents.clear();
            for( Iterator i = nw.iterator(); i.hasNext(); ){
                Feature feature = (Feature) i.next();    
                feature.setParent( this );
                contents.put( feature.getID(), feature );
            }
			fireChange(nw,0);
		}
	}
    
	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getNumberOfAttributes()
	 */
	public int getNumberOfAttributes() {
		return featureType == null ? 1 : featureType.getAttributeCount();		
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
		if(xPath.indexOf(featureType.getTypeName())>-1){
			if(xPath.endsWith("]")){
				// TODO get index and grab it
			}else{
				setAttribute(0,attribute);
			}
        }
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getDefaultGeometry()
	 */
	public Geometry getDefaultGeometry() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setDefaultGeometry(com.vividsolutions.jts.geom.Geometry)
	 */
	public void setDefaultGeometry(Geometry geometry) throws IllegalAttributeException {
		throw new IllegalAttributeException("Not Supported");
	}

    public void close( FeatureIterator close ) {
        if( close instanceof FeatureIteratorImpl){
        	FeatureIteratorImpl wrapper = (FeatureIteratorImpl) close;
        	wrapper.close();
        }
    }

    public void close( Iterator close ) {
        // nop
    }

    public FeatureReader reader() throws IOException {
        final FeatureIterator iterator = features(); 
        return new FeatureReader(){
            public FeatureType getFeatureType() {
                return getSchema();
            }
            public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
                return iterator.next();
            }

            public boolean hasNext() throws IOException {
                return iterator.hasNext();
            }

            public void close() throws IOException {
                DefaultFeatureCollection.this.close( iterator );
            }            
        };
    }

    public int getCount() throws IOException {
        return contents.size();
    }

    public FeatureCollection collection() throws IOException {
        FeatureCollection copy = new DefaultFeatureCollection( null, featureType );
        List list = new ArrayList( contents.size() );
        for( FeatureIterator iterator = features(); iterator.hasNext(); ){
            Feature feature = iterator.next();
            Feature duplicate;
            try {                
                duplicate = feature.getFeatureType().duplicate( feature );
            } catch (IllegalAttributeException e) {
                throw new DataSourceException( "Unable to copy "+feature.getID(), e );
            }
            list.add( duplicate );
        }
        copy.addAll( list );
        return copy;
    }

    /**
     * Optimization time ... grab the fid set so other can quickly test membership
     * during removeAll/retainAll implementations.
     * 
     * @return Set of fids.
     */
    public Set fids() {
        return Collections.unmodifiableSet( contents.keySet() );
    }

    /**
     * Accepts a visitor, which then visits each feature in the collection.
     * @throws IOException 
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress ) throws IOException {
        Iterator iterator = null;
        if (progress == null) progress = new NullProgressListener();
        try{
            float size = size();
            float position = 0;            
            progress.started();
            for( iterator = iterator(); !progress.isCanceled() && iterator.hasNext(); progress.progress( position++/size )){
                try {
                    Feature feature = (Feature) iterator.next();
                    visitor.visit(feature);
                }
                catch( Exception erp ){
                    progress.exceptionOccurred( erp );
                }
            }            
        }
        finally {
            progress.complete();            
            close( iterator );
        }
    }

    /**
     * Will return an optimized subCollection based on access
     * to the origional MemoryFeatureCollection.
     * <p>
     * This method is intended in a manner similar to subList,
     * example use:
     * <code>
     * collection.subCollection( myFilter ).clear()
     * </code>
     * </p>    
     * @param filter Filter used to determine sub collection.
     * @since GeoTools 2.2, Filter 1.1
     */
	public FeatureCollection subCollection(Filter filter) {
		if( filter == Filter.ALL || filter.equals( Filter.ALL )){
			return this;
		}		
		return new SubFeatureCollection( this, filter );
	}

    /**
     * Construct a sorted view of this content.
     * <p>
     * Sorts may be combined togther in a stable fashion, in congruence
     * with the Filter 1.1 specification.
     * </p>
     * <p>
     * This method should also be able to handle GeoTools specific
     * sorting through detecting order as a SortBy2 instance.
     * </p>
     * @param SortBy Construction of a Sort
     * @since GeoTools 2.2, Filter 1.1
     * @param order Filter 1.1 SortBy
     * @return FeatureList sorted according to provided order

     */
    public FeatureList sort(SortBy order) {
    	if( order == SortBy.NATURAL_ORDER ){
    		// return new FeatureListImpl( this );
    	}
    	if( order instanceof SortBy2){
    		SortBy2 advanced = (SortBy2) order;
    		return sort( advanced );
    	}
    	return null;
    }
    /**
     * Allows for "Advanced" sort capabilities specific to the
     * GeoTools platform!
     * <p>
     * Advanced in this case really means making use of a generic
     * Expression, rather then being limited to PropertyName.
     * </p>
     * @param order GeoTools SortBy
     * @return FeatureList sorted according to provided order
     */
    public FeatureList sort(SortBy2 order ){
    	Comparator compare;    	
    	if( order == SortBy.NATURAL_ORDER ){
    		// forward
    	}
    	else if ( order == SortBy.REVERSE_ORDER ){
    		// backwards
    	}
    	// custom
    	return null; // new OrderedFeatureList( order, compare );
    }

	public void purge() {
		// no resources were harmed in the making of this FeatureCollection
	}	
}
