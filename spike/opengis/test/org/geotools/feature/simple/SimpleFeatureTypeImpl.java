package org.geotools.feature.simple;

import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.Descriptors;
import org.geotools.feature.type.FeatureTypeImpl;
import org.geotools.filter.Filter;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.simple.SimpleDescriptor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;

public class SimpleFeatureTypeImpl extends FeatureTypeImpl implements
		SimpleFeatureType {
	
	
	public SimpleFeatureTypeImpl(QName name, SimpleDescriptor schema, GeometryType defaultGeometry,
			Set<Filter> restrictions, SimpleFeatureType superType, boolean isAbstract){
		super(name, schema, defaultGeometry, restrictions, superType, isAbstract);
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
	 * JG: Must be null for truely simple content.
	 * @return null, as no super types are allowed
	 * GR:  GR: I guess not, at least super must be simple too
	 */
	@Override
	public SimpleFeatureType getSuper(){
		return (SimpleFeatureType)super.getSuper();
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
		return Descriptors.types(getDescriptor());
	}
}
