/*
 * MockFeatureCollection.java
 *
 * Created on August 12, 2003, 7:29 PM
 */

package org.geotools.feature;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.Filter;
import org.geotools.filter.SortBy;
import org.geotools.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author  jamesm
 */
public class MockFeatureCollection implements org.geotools.feature.FeatureCollection {
    
    /** Creates a new instance of MockFeatureCollection */
    public MockFeatureCollection() {
    }
    
    public void addListener(CollectionListener listener) {
    }
    
    public FeatureIterator features() {
        return null;
    }
    
    public com.vividsolutions.jts.geom.Envelope getBounds() {
        return null;
    }
    
    public void removeListener(CollectionListener listener) {
       
    }
    
    public boolean add(Object o) {
        return false;
    }
    
    public boolean addAll(java.util.Collection c) {
        return false;
    }
    
    public void clear() {
    }
    
    public boolean contains(Object o) {
        return false;
    }
    
    public boolean containsAll(java.util.Collection c) {
        return false;
    }
    
    public boolean equals(Object o) {
        return false;
    }
    
    public int hashCode() {
        return 0;
    }
    
    public boolean isEmpty() {
        return false;
    }
    
    public java.util.Iterator iterator() {
        return null;
    }
    
    public boolean remove(Object o) {
        return false;
    }
    
    public boolean removeAll(java.util.Collection c) {
        return false;
    }
    
    public boolean retainAll(java.util.Collection c) {
        return false;
    }
    
    public int size() {
        return 0;
    }
    
    public Object[] toArray() {
        return null;
    }
    
    public Object[] toArray(Object[] a) {
        return null;
    }

	/* (non-Javadoc)
	 * @see org.geotools.feature.FeatureCollection#getFeatureType()
	 */
	public FeatureType getFeatureType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getParent()
	 */
	public FeatureCollection getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setParent(org.geotools.feature.FeatureCollection)
	 */
	public void setParent(FeatureCollection collection) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getID()
	 */
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttributes(java.lang.Object[])
	 */
	public Object[] getAttributes(Object[] attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String xPath) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getAttribute(int)
	 */
	public Object getAttribute(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setAttribute(int, java.lang.Object)
	 */
	public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getNumberOfAttributes()
	 */
	public int getNumberOfAttributes() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#getDefaultGeometry()
	 */
	public Geometry getDefaultGeometry() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.feature.Feature#setDefaultGeometry(com.vividsolutions.jts.geom.Geometry)
	 */
	public void setDefaultGeometry(Geometry geometry) throws IllegalAttributeException {
		// TODO Auto-generated method stub
		
	}

    public FeatureType getSchema() {
        return null;
    }

    public FeatureReader reader() throws IOException {
        return null;
    }

    public int getCount() throws IOException {
        return 0;
    }

    public FeatureCollection collection() throws IOException {
        return null;
    }

    public void close( Iterator iterator ) {
    }

    public void close( FeatureIterator iterator ) {
    }
    
    public void accepts(FeatureVisitor visitor, ProgressListener progress ) throws IOException {        
    }

	public FeatureCollection subCollection(Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureList sort(SortBy order) {
		// TODO Auto-generated method stub
		return null;
	}

	public void purge() {
		// TODO Auto-generated method stub
		
	}
}
