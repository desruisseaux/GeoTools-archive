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
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.geotools.xml.gml.GMLSchema;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * A basic implementation of FeatureCollection which use a {@link LinkedHashSet} for
 * its internal storage. This preserves ordering and allows a faster iteration
 * speed then a {@link HashSet}.
 *
 * @author Ian Schneider
 * @version $Id: DefaultFeatureCollection.java,v 1.6 2003/12/04 23:17:23 aaime Exp $
 */
public class DefaultFeatureCollection extends AbstractCollection implements FeatureCollection {
    /** Internal feature storage list */
    private List features = new LinkedList();

    /** Internal listener storage list */
    private List listeners = new ArrayList(2);

    /** Internal envelope of bounds. */
    private Envelope bounds = null;
    
    private String id; /// fid

    /**
     * This class is protected to discourage direct usage... opportunistic
     * reuse is encouraged, but only for the purposes of testing or other
     * specialized  uses. Normal creation should occur through
     * org.geotools.core.FeatureCollections.newCollection().
     * @param id may be null ... feature id
     * @param FeatureType optional, may be null
     */
    protected DefaultFeatureCollection(String id, FeatureType featureType) {
    	this.id = id;
    	if(featureType == null){
    		List ats = new LinkedList();
    		ats.add(new DefaultFeatureType("AbstractFeatureType",GMLSchema.NAMESPACE,new LinkedList(),new LinkedList(),null));
    		featureType = new DefaultFeatureType("AbstractFeatureColletionType",GMLSchema.NAMESPACE,ats,new LinkedList(),null);
    	}
    	this.featureType = featureType;
    }
    private FeatureType featureType;

    /**
     * Gets the bounding box for the features in this feature collection.
     *
     * @return the envelope of the geometries contained by this feature
     *         collection.
     */
    public Envelope getBounds() {
        if (bounds == null) {
            bounds = new Envelope();

            for (Iterator i = features.iterator(); i.hasNext();) {
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
    	//TODO check inheritance with FeatureType here!!!
        Feature feature = (Feature) o;

        // This cast is neccessary to keep with the contract of Set!
        boolean changed = features.add(feature);

        if (changed) {
            fireChange(feature, CollectionEvent.FEATURES_ADDED);
        }

        return changed;
    }

    /**
     * Adds all of the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in
     * progress. (This implies that the behavior of this call is undefined if
     * the specified collection is this collection, and this collection is
     * nonempty.)
     *
     * @param c elements to be inserted into this collection.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     *
     * @see #add(Object)
     */
    public boolean addAll(Collection c) {
    	//TODO check inheritance with FeatureType here!!!
        boolean changed = false;
        Iterator iter = c.iterator();

        List featuresAdded = new ArrayList(c.size());
        while (iter.hasNext()) {
            Feature f = (Feature) iter.next();
            boolean added = features.add(f);
            changed |= added;
            
            if(added) featuresAdded.add(f);
        }

        if (changed) {
            fireChange(featuresAdded, CollectionEvent.FEATURES_ADDED);
        }

        return changed;
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * This collection will be empty after this method returns unless it
     * throws an exception.
     */
    public void clear() {
        if(features.size() == 0)
            return;
        
        Feature[] oldFeatures = new Feature[features.size()];
        oldFeatures = (Feature[]) features.toArray(oldFeatures);

        features.clear();
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
        return features.contains((Feature) o);
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    public boolean isEmpty() {
        return features.isEmpty();
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
        final Iterator iterator = features.iterator();

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
        return new FeatureIterator(this);
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
        Feature f = (Feature) o;
        boolean changed = features.remove(f);

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
     * @param c elements to be removed from this collection.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection c) {
        boolean changed = false;
        Iterator fIter = c.iterator();

        List removedFeatures = new ArrayList(c.size());
        while (fIter.hasNext()) {
            Feature f = (Feature) fIter.next();
            boolean removed = features.remove(f);
            
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

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this collection all of its elements that are not contained in the
     * specified collection.
     *
     * @param c elements to be retained in this collection.
     *
     * @return <tt>true</tt> if this collection changed as a result of the call
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection c) {
        List removedFeatures = new ArrayList(features.size() - c.size());
        boolean modified = false;
        for(Iterator it = features.iterator(); it.hasNext(); )  {
            Feature f = (Feature) it.next();
            if(!c.contains(f)) {
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
        return features.size();
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
        return features.toArray();
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
        return features.toArray(a);
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
		return features.toArray(new Feature[features.size()]);
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String xPath) {
		if(xPath.indexOf(featureType.getTypeName())>-1)
			if(xPath.endsWith("]")){
				// TODO get index and grab it
				return features;
			}else{
				return features;
			}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttribute(int)
	 */
	public Object getAttribute(int index) {
		if(index == 0)
			return features;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setAttribute(int, java.lang.Object)
	 */
	public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		if(position == 0 && val instanceof List){
			List nw = (List)val;
			Iterator i = nw.iterator();
			while(i.hasNext()){
				if(!(i.next() instanceof Feature))
					return;
			}
			features = nw;
			fireChange(nw,0);
		}
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getNumberOfAttributes()
	 */
	public int getNumberOfAttributes() {
		return features.size();
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
		if(xPath.indexOf(featureType.getTypeName())>-1)
			if(xPath.endsWith("]")){
				// TODO get index and grab it
			}else{
				setAttribute(0,attribute);
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
}
