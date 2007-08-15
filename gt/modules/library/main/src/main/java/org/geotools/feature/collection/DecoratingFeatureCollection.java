package org.geotools.feature.collection;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
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

import com.vividsolutions.jts.geom.Geometry;

/**
 * A FeatureCollection which completley delegates to another FeatureCollection.
 * <p>
 * This class should be subclasses by classes which must somehow decorate 
 * another FeatureCollection and override the relevant methods. 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class DecoratingFeatureCollection implements FeatureCollection {

    /**
     * the delegate
     */
	protected FeatureCollection delegate;

	protected DecoratingFeatureCollection(FeatureCollection delegate) {
		this.delegate = delegate;
	}
	
	public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
		delegate.accepts(visitor, progress);
	}

	public void accepts(org.opengis.feature.FeatureVisitor visitor, org.opengis.util.ProgressListener progress) {
		delegate.accepts(visitor, progress);
	}

	public boolean add(Object o) {
		return delegate.add(o);
	}

	public boolean addAll(Collection c) {
		return delegate.addAll(c);
	}

	public void addListener(CollectionListener listener) throws NullPointerException {
		delegate.addListener(listener);
	}

	public Collection associations() {
		return delegate.associations();
	}

	public Collection attributes() {
		return delegate.attributes();
	}
    public List getAttributes() {
        return delegate.getAttributes();
    }
	public void clear() {
		delegate.clear();
	}

	public void close(FeatureIterator close) {
		delegate.close(close);
	}

	public void close(Iterator close) {
		delegate.close(close);
	}

	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	public boolean containsAll(Collection c) {
		return delegate.containsAll(c);
	}

	public PropertyDescriptor descriptor() {
		return delegate.descriptor();
	}

	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	public FeatureIterator features() {
		return delegate.features();
	}

	public Object getValue() {
		return delegate.getValue();
	}

	public List get(Name name) {
		return delegate.get(name);
	}

	public Object getAttribute(int index) {
		return delegate.getAttribute(index);
	}

	public Object getAttribute(String xPath) {
		return delegate.getAttribute(xPath);
	}

	public Object[] getAttributes(Object[] attributes) {
		return delegate.getAttributes(attributes);
	}

	public ReferencedEnvelope getBounds() {
		return delegate.getBounds();
	}

	public CoordinateReferenceSystem getCRS() {
		return delegate.getCRS();
	}

	public GeometryAttribute getDefaultGeometry() {
		return delegate.getDefaultGeometry();
	}

	public Object getDefaultGeometryValue() {
		return delegate.getDefaultGeometryValue();
	}

	public AttributeDescriptor getDescriptor() {
		return delegate.getDescriptor();
	}

	public SimpleFeatureCollectionType getFeatureCollectionType() {
		return delegate.getFeatureCollectionType();
	}

	public FeatureType getFeatureType() {
		return delegate.getFeatureType();
	}

	public String getID() {
		return delegate.getID();
	}

	public Collection memberTypes() {
		return delegate.memberTypes();
	}
	
	public SimpleFeatureType getMemberType() {
		return delegate.getMemberType();
	}

	public int getNumberOfAttributes() {
		return delegate.getNumberOfAttributes();
	}

	public Geometry getPrimaryGeometry() {
		return delegate.getPrimaryGeometry();
	}

	public FeatureType getSchema() {
		return delegate.getSchema();
	}

	public SimpleFeatureCollectionType getType() {
		return delegate.getType();
	}

	public List getTypes() {
		return delegate.getTypes();
	}

	public Object getUserData(Object key) {
		return delegate.getUserData(key);
	}

	public Object getValue(int index) {
		return delegate.getValue(index);
	}

	public Object getValue(String name) {
		return delegate.getValue(name);
	}

	public List getValues() {
		return delegate.getValues();
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Iterator iterator() {
		return delegate.iterator();
	}

	public Name name() {
		return delegate.name();
	}

	public boolean nillable() {
		return delegate.nillable();
	}

	public Object operation(Name name, List parameters) {
		return delegate.operation(name, parameters);
	}

//	public Object operation(String name, Object parameters) {
//		return delegate.operation(name, parameters);
//	}

	public void purge() {
		delegate.purge();
	}

	public void putUserData(Object key, Object value) {
		delegate.putUserData(key, value);
	}

	public boolean remove(Object o) {
		return delegate.remove(o);
	}

	public boolean removeAll(Collection c) {
		return delegate.removeAll(c);
	}

	public void removeListener(CollectionListener listener) throws NullPointerException {
		delegate.removeListener(listener);
	}

	public boolean retainAll(Collection c) {
		return delegate.retainAll(c);
	}

	public void setValue(Object newValue) throws IllegalArgumentException {
		delegate.setValue(newValue);
	}
	public void setValue(List newList) throws IllegalArgumentException {
        delegate.setValue(newList);
    }
	public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		delegate.setAttribute(position, val);
	}

	public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
		delegate.setAttribute(xPath, attribute);
	}

	public void setCRS(CoordinateReferenceSystem crs) {
		delegate.setCRS(crs);
	}

	public void setDefaultGeometry(GeometryAttribute geometryAttribute) {
		delegate.setDefaultGeometry(geometryAttribute);
	}

	public void setDefaultGeometryValue(Object geometry) {
		delegate.setDefaultGeometryValue(geometry);
	}

	public void setPrimaryGeometry(Geometry geometry) throws IllegalAttributeException {
		delegate.setPrimaryGeometry(geometry);
	}

	public void setValue(int index, Object value) {
		delegate.setValue(index, value);
	}

	public void setValue(String name, Object value) {
		delegate.setValue(name, value);
	}

	public void setValues(List values) {
		delegate.setValues(values);
	}

	public void setValues(Object[] values) {
		delegate.setValues(values);
	}

	public int size() {
		return delegate.size();
	}

	public FeatureCollection sort(SortBy order) {
		return delegate.sort(order);
	}

	public FeatureCollection subCollection(Filter filter) {
		return delegate.subCollection(filter);
	}

	public Object[] toArray() {
		return delegate.toArray();
	}

	public Object[] toArray(Object[] a) {
		return delegate.toArray(a);
	}
	
	
}
