package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.feature.Descriptors;
import org.geotools.feature.type.FeatureTypeImpl;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.simple.SimpleDescriptor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;

public class SimpleFeatureTypeImpl extends FeatureTypeImpl implements
		SimpleFeatureType {
	
	
	public SimpleFeatureTypeImpl(QName name, SimpleDescriptor schema, GeometryType defaultGeometry,
			boolean isAbstract){
		super(name, schema, defaultGeometry, null, null, isAbstract);
	}

	public AttributeType get(QName qname) {
		return Descriptors.type(getDescriptor(), qname);
	}

	public AttributeType get(String name) {
		return get(new QName(name));
	}

	public AttributeType get(int index) {
		AttributeType type = null;
		Descriptor node = getDescriptor().sequence().get(index);
		if(node != null){
			type = ((AttributeDescriptor)node).getType();
		}
		return type;
	}

	/**
	 * Number of available attributes 
	 */
	public int getNumberOfAttribtues() {
		return getDescriptor().sequence().size();
	}
	
	/**
	 * Must be null for truely simple content.
	 * @return null, as no super types are allowed
	 */
	@Override
	public SimpleFeatureType getSuper(){
		return null;
	}

	/**
	 * Indicates a director ordering of AttributeDescriptors.
	 */
	@Override
	public SimpleDescriptor getDescriptor(){
		return (SimpleDescriptor)super.getDescriptor();
	}
	
	
	/**
	 * Types are returned in the perscribed index order.
	 * @return Types in prescribed order
	 */
	@Override
	public List<AttributeType> types(){
		return new ArrayList<AttributeType>(Descriptors.types(getDescriptor()));
	}
}
