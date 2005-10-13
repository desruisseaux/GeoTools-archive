package org.geotools.feature.simple;

import java.util.List;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.impl.FeatureImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

public class SimpleFeatureImpl extends FeatureImpl implements SimpleFeature {

	public SimpleFeatureImpl(String id, SimpleFeatureType type) {
		this(id, type, null);
	}

	public SimpleFeatureImpl(String id, SimpleFeatureType type,
			List<? extends Object> values) {
		super(id, type);

		List<AttributeDescriptor> contents = type.getDescriptor().sequence();
		List<Attribute> atts = super.attribtues;

		AttributeFactory attFactory = new AttributeFactoryImpl();
		int idx = 0;
		for (AttributeDescriptor desc : contents) {
			Attribute att = attFactory.create(desc.getType(), null);
			Object value = null;
			if (values != null) {
				value = values.get(idx);
			}
			att.set(value);
			atts.add(att);
			idx++;
		}
	}

	/**
	 * Restricted to SimpleFeatureType
	 * <p>
	 * This restriction enabled client code to confidently assume that each
	 * attribute occurs in the perscribed order and that no super types are
	 * used.
	 * </p>
	 */
	public SimpleFeatureType getType() {
		return (SimpleFeatureType) super.getType();
	}

	/**
	 * Retrive value by attribute name.
	 * 
	 * @param name
	 * @return Attribute Value associated with name
	 */
	public Object get(String name) {
		for (Attribute att : super.attribtues) {
			AttributeType type = att.getType();
			String attName = type.name();
			if (attName.equals(name)) {
				return att.get();
			}
		}
		return null;
	}

	public Object get(AttributeType type) {
		if (!super.types().contains(type)) {
			throw new IllegalArgumentException(
					"this feature content model has no type " + type);
		}
		for (Attribute att : super.attribtues) {
			if (att.getType().equals(type)) {
				return att.get();
			}
		}
		throw new Error();
	}

	/**
	 * Access attribute by "index" indicated by SimpleFeatureType.
	 * 
	 * @param index
	 * @return
	 */
	public Object get(int index) {
		Attribute att = super.attribtues.get(index);
		return att == null ? null : att.get();
		// return values().get(index);
	}

	/**
	 * Modify attribute with "name" indicated by SimpleFeatureType.
	 * 
	 * @param name
	 * @param value
	 */
	public void set(String name, Object value) {
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
	public void set(int index, Object value) {
		List<Attribute> contents = super.getAttributes();
		Attribute attribute = contents.get(index);
		attribute.set(value);
	}

}
