package org.geotools.feature.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import jj2000.j2k.NotImplementedError;

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

import sun.security.action.GetBooleanAction;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * This is is a sample sub collection.
 * 
 * @author Jody Garnett
 */
public class FeatureCollectionFiltered implements FeatureCollection {
	protected FeatureCollection collection;

	protected Filter filter;

	public FeatureCollectionFiltered(FeatureCollection collection, Filter filter) {
		if (filter.equals(Filter.ALL)) {
			throw new IllegalArgumentException(
					"A subcollection with Filter.ALL is a null operation");
		}
		if (filter.equals(Filter.NONE)) {
			throw new IllegalArgumentException(
					"A subcollection with Filter.NONE should be a FeatureCollectionEmpty");
		}
		if (collection instanceof FeatureCollectionFiltered) {
			FeatureCollectionFiltered filtered = (FeatureCollectionFiltered) collection;
			collection = filtered.collection;
			this.filter = filtered.filter.and(filter);
		} else {
			this.collection = collection;
			this.filter = filter;
		}
	}

	public FeatureCollectionType<?> getType() {
		return collection.getType();
	}

	public Iterator<Feature> features() {
		Iterator<Feature>iterator = collection.iterator();
		Iterator<Feature>filtered = new FilteringIterator(iterator, filter);
		return filtered;
	}

	public void close(Iterator<Feature> iterator) {
		// nop
	}

	public void add(int index, Attribute value){
		throw new UnsupportedOperationException();
	}

	public Attribute remove(int index){
		throw new UnsupportedOperationException();
	}

	public String getID() {
		return collection.getID();
	}

	public Envelope getBounds(){
		return FeatureCollectionImpl.getBounds(iterator());
	}
	
	public CoordinateReferenceSystem getCRS() {
		return collection.getCRS();
	}

	public Geometry getDefaultGeometry() {
		return collection.getDefaultGeometry();
	}

	public void setDefaultGeometry(Geometry g) {
		throw new UnsupportedOperationException();
	}

	public List<Attribute> getAttributes() {
		return collection.getAttributes();
	}

	public List<Attribute> getAttributes(String name) {
		return collection.getAttributes(name);
	}
	
	public List<Attribute> getAttributes(QName name) {
		return collection.getAttributes(name);
	}

	public Object get(){
		return collection.get();
	}
	
	public void set(List<Attribute> attributes) {
		collection.set(attributes);
	}

	public List<AttributeType> types() {
		return collection.types();
	}

	public List<Object> values() {
		return collection.values();
	}

	public Object get(AttributeType type) {
		return collection.get(type);
	}

	public void set(Object newValue) {
		collection.set(newValue);
	}

	public FeatureCollection subCollection(Filter filter) {
		if (filter.equals(Filter.NONE)) {
			return this;
		}
		if (filter.equals(Filter.ALL)) {
			// TODO empty
		}
		return new FeatureCollectionFiltered(this, filter);
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	public boolean contains(Object arg0) {
		return false;
	}

	public Iterator<Feature> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean add(Feature arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addAll(Collection<? extends Feature> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void clear() {
		// TODO Auto-generated method stub
	}

	private static class FilteringIterator implements Iterator<Feature> {
		private Iterator<Feature> delegate;

		private Filter filter;

		private Feature next;

		public FilteringIterator(Iterator<Feature> delegate, Filter filter) {
			this.delegate = delegate;
			this.filter = filter;
			next = getNext();
		}

		private Feature getNext() {
			Feature f = null;
			while (delegate.hasNext()) {
				f = delegate.next();
				if (filter.contains(f)){
					return f;
				}
			}
			return null;
		}

		public boolean hasNext() {
			return next != null;
		}

		public Feature next() {
			if(next == null){
				throw new NoSuchElementException();
			}
			Feature current = next;
			next = getNext();
			return current;
		}

		public void remove() {
			delegate.remove();
		}
	}

}
