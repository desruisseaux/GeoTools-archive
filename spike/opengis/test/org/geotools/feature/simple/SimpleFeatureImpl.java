package org.geotools.feature.simple;

import java.util.List;

import org.geotools.feature.impl.FeatureImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

public class SimpleFeatureImpl extends FeatureImpl implements SimpleFeature {

	public SimpleFeatureImpl(String id, SimpleFeatureType type){
		super(id, type);
	}
	

	/**
	 * Restricted to SimpleFeatureType
	 * <p>
	 * This restriction enabled client code to confidently
	 * assume that each attribute occurs in the perscribed order
	 * and that no super types are used.
	 * </p>
	 */
	public SimpleFeatureType getType(){
		return (SimpleFeatureType)super.getType();
	}

	/**
	 * Retrive value by attribute name.
	 * @param name
	 * @return Attribute Value associated with name
	 */
	public Object get(String name){
		return super.get(name);
	}
	
	
	/**
	 * Access attribute by "index" indicated by SimpleFeatureType.
	 * 
	 * @param index
	 * @return
	 */
	public Object get( int index ){
		AttributeType type = getType().get(index);
		return super.get(type);
	}

	/**
	 * Modify attribute with "name" indicated by SimpleFeatureType.
	 * 
	 * @param name
	 * @param value
	 */
	public void set( String name, Object value ){
		AttributeType type = getType().get(name);
		List<AttributeType> types = getType().types();
		int idx = types.indexOf(type);
		set(idx, value);
	}

	/**
	 * Modify attribute at the "index" indicated by SimpleFeatureType.
	 * 
	 * @param index
	 * @param value
	 */
	public void set(int index, Object value){
		List<Attribute> contents = super.getAttributes();
		Attribute attribute = contents.get(index);
		attribute.set(value);
	}
	
}
