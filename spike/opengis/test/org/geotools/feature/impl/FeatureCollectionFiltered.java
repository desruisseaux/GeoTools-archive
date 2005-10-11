package org.geotools.feature.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

/**
 * This is is a sample sub collection.
 * 
 * @author Jody Garnett
 */
public class FeatureCollectionFiltered implements FeatureCollection {
	protected FeatureCollection collection;
	protected Filter filter;
	
	public FeatureCollectionFiltered( FeatureCollection collection, Filter filter){
		if( filter.equals(Filter.ALL) ){
			throw new IllegalArgumentException("A subcollection with Filter.ALL is a null operation");
		}
		if( filter.equals(Filter.NONE) ){
			throw new IllegalArgumentException("A subcollection with Filter.NONE should be a FeatureCollectionEmpty");
		}
		if( collection instanceof FeatureCollectionFiltered){
			FeatureCollectionFiltered filtered = (FeatureCollectionFiltered) collection;
			collection = filtered.collection;
			this.filter = filtered.filter.and( filter );
		}
		else {
			this.collection = collection;
			this.filter = filter;
		}
	}
    public FeatureCollectionFiltered( String id, FeatureCollectionType type ){~
    	delegate = new FeatureImpl( id, type );
    }
    
	public FeatureCollectionType getType() {
		return collection.getType();
	}
	/** TODO: implement this - it is the point ;-) */
	public Iterator<Feature> features() {
		throw new NotImplementedError("ha ha");
	}

	public void close(Iterator<Feature> iterator) {
		// nop
	}

	public String getID() {
		return collection.getID();
	}
	
	public CoordinateReferenceSystem getCRS() {
		return collection.getCRS();
	}

	public GeometryAttribute getDefaultGeometry() {
		return collection.getDefaultGeometry();
	}

	public List<Attribute> get() {
		return collection.get();
	}

	public void set(List<Attribute> attributes) {
		collection.set( attributes );	
	}

	public List<AttributeType> types() {
		return collection.types();
	}

	public List<Object> values() {
		return collection.values();
	}

	public Object get(AttributeType type) {
		return collection.get( type );
	}

	public void set(Object newValue) {
		collection.set( newValue );
	}

	public FeatureCollection subCollection(Filter filter) {
		if( filter.equals( Filter.NONE )){
			return this;
		}
		if( filter.equals( Filter.ALL)){
			// TODO empty
		}		
		return new FeatureCollectionFiltered( this, filter );
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
}
