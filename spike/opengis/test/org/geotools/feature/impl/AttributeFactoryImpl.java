package org.geotools.feature.impl;

import java.rmi.server.UID;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.feature.impl.attribute.BooleanAttribute;
import org.geotools.feature.impl.attribute.GeometricAttributeType;
import org.geotools.feature.impl.attribute.NumericAttributeType;
import org.geotools.feature.impl.attribute.TemporalAttributeType;
import org.geotools.feature.impl.attribute.TextualAttributeType;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Factory for creating instances of the Attribute family of classes.
 * 
 * @author Ian Schneider
 * @author Gabriel Roldan
 * @version $Id$
 */
public class AttributeFactoryImpl implements AttributeFactory {

	/**
	 * 
	 */
	public Attribute create(AttributeType type, String id) {
		if (type == null) {
			throw new NullPointerException("type");
		}
		id = createIdIfNeeded(type, id);
		return create(type, id, null);
	}

	public Attribute create(AttributeType type, String id, Object value) {
		if (type == null) {
			throw new NullPointerException("type");
		}
		Attribute att;
		if (type instanceof SimpleFeatureType) {
			att = create((SimpleFeatureType) type, id);
		} else if (type instanceof FeatureCollectionType) {
			att = create((FeatureCollectionType)type, id);
		} else if (type instanceof FeatureType) {
			att = create((FeatureType) type, id);
		} else if (type instanceof ComplexType) {
			att = create((ComplexType) type, id);
		} else {
			Class clazz = type.getBinding();
			if (Number.class.isAssignableFrom(clazz)) {
				att = new NumericAttributeType(type, value);
			} else if (CharSequence.class.isAssignableFrom(clazz)) {
				att = new TextualAttributeType(type, value);
			} else if (java.util.Date.class.isAssignableFrom(clazz)) {
				att = new TemporalAttributeType(type, value);
			} else if (Boolean.class.isAssignableFrom(clazz)) {
				att = new BooleanAttribute(type, value);
			} else if (Geometry.class.isAssignableFrom(clazz)) {
				att = new GeometricAttributeType(type, value, id);
			} else {
				att = new AttributeImpl(id, type);
			}
		}

		//set the attribute content, may fail if
		//!type.isNillable() and value == null
		att.set(value);

		return att;
	}

	public GeometryAttribute create(GeometryType type, String id) {
		return create(type, id, (CoordinateReferenceSystem) null);
	}

	public GeometryAttribute create(GeometryType type, String id,
			CoordinateReferenceSystem crs) {
		return create(type, id, null, null);
	}

	public GeometryAttribute create(GeometryType type, String id, Geometry value) {
		return create(type, id, null, value);
	}

	public GeometryAttribute create(GeometryType type, String id,
			CoordinateReferenceSystem crs, Geometry value) {
		id = createIdIfNeeded(type, id);
		return new GeometricAttributeType(type, crs, value, id);
	}

	public SimpleFeature create(SimpleFeatureType type, String id) {
		id = createIdIfNeeded(type, id);
		return new SimpleFeatureImpl(id, type);
	}

	public SimpleFeature create(SimpleFeatureType type, String id,
			List<? extends Object> values) {
		id = createIdIfNeeded(type, id);
		return new SimpleFeatureImpl(id, type, values);
	}

	/**
	 * 
	 * @param type
	 * @param values
	 * @return
	 */
	public SimpleFeature create(SimpleFeatureType type, String id,
			Object[] values) {
		return create(type, id, Arrays.asList(values));
	}

	public ComplexAttribute create(ComplexType type, String id) {
		id = createIdIfNeeded(type, id);
		return new ComplexAttributeImpl(id, type);
	}

	public Feature create(FeatureType type, String id) {
		id = createIdIfNeeded(type, id);
		return new FeatureImpl(id, type);
	}

	public FeatureCollection create(FeatureCollectionType type, String id) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private String createIdIfNeeded(AttributeType type, String id) {
		if (type.isIdentified() && id == null) {
			id = type.name() + "-" + (new UID()).toString();
		}
		return id;
	}

}
