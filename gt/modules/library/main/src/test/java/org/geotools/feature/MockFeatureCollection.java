/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    Created on August 12, 2003, 7:29 PM
 */
package org.geotools.feature;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author  jamesm
 * @source $URL$
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
    
    public ReferencedEnvelope getBounds() {
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

	public Geometry getPrimaryGeometry() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPrimaryGeometry(Geometry geometry) throws IllegalAttributeException {
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

	public Object getDefaultGeometryValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public AttributeType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public List getTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getValue(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getValue(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object operation(String name, Object parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDefaultGeometryValue(Object geometry) {
		// TODO Auto-generated method stub
		
	}

	public void setValue(String name, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void setValue(int index, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void setValues(List values) {
		// TODO Auto-generated method stub
		
	}

	public void setValues(Object[] values) {
		// TODO Auto-generated method stub
		
	}

	public CoordinateReferenceSystem getCRS() {
		// TODO Auto-generated method stub
		return null;
	}

	public GeometryAttribute getDefaultGeometry() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getUserData(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	public void putUserData(Object key, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void setCRS(CoordinateReferenceSystem crs) {
		// TODO Auto-generated method stub
		
	}

	public void setDefaultGeometry(GeometryAttribute geometryAttribute) {
		// TODO Auto-generated method stub
		
	}

	public Collection associations() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection attributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object get() {
		// TODO Auto-generated method stub
		return null;
	}

	public List get(Name name) {
		// TODO Auto-generated method stub
		return null;
	}

	public AttributeDescriptor getDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void set(Object newValue) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	public boolean nillable() {
		// TODO Auto-generated method stub
		return false;
	}

	public Object operation(Name name, List parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public PropertyDescriptor descriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public Name name() {
		// TODO Auto-generated method stub
		return null;
	}

	public SimpleFeatureCollectionType getFeatureCollectionType() {
		// TODO Auto-generated method stub
		return null;
	}

	public SimpleFeatureType getMemberType() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection memberTypes() {
		return Collections.EMPTY_SET;
	}
	
	public void accepts(org.opengis.feature.FeatureVisitor visitor, org.opengis.util.ProgressListener progress) {
		// TODO Auto-generated method stub
		
	}
}
