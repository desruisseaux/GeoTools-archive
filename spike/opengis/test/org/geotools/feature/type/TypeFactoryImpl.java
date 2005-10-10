package org.geotools.feature.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.Descriptors;
import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.schema.NodeImpl;
import org.geotools.feature.simple.SimpleDescriptorImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.filter.Filter;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.simple.SimpleDescriptor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class TypeFactoryImpl implements TypeFactory {
	
	private static SimpleFeatureType GML_FEATURE;

	/**
	 * @see org.opengis.feature.type.TypeFactory#createType(java.lang.String,
	 *      java.lang.Class)
	 */
	public AttributeTypeImpl createType(String name, Class binding) {
		if (name == null) {
			throw new NullPointerException("Null name not allowed");
		}
		return createType(new QName(name), binding);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createType(javax.xml.namespace.QName,
	 *      java.lang.Class)
	 */
	public AttributeTypeImpl createType(QName name, Class binding) {
		return createType(name, binding, false, true, null, null);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createType(javax.xml.namespace.QName,
	 *      java.lang.Class, boolean, boolean, java.util.Set)
	 */
	public AttributeTypeImpl createType(QName name, Class binding,
			boolean identified, boolean nillable, Set<Filter> restrictions) {
		return createType(name, binding, identified, nillable, restrictions,
				null);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createSimpleType(javax.xml.namespace.QName,
	 *      java.lang.Class, boolean, boolean, java.util.Set,
	 *      org.opengis.feature.type.AttributeType, boolean)
	 */
	public AttributeTypeImpl createType(QName name, Class binding,
			boolean identified, boolean nillable, Set<Filter> restrictions,
			AttributeType superType) {
		return createType(name, binding, identified, nillable, restrictions,
				superType, null);
	}

	public AttributeTypeImpl createType(QName name, Class binding,
			boolean identified, boolean nillable, Set<Filter> restrictions,
			AttributeType superType, Object metaData) {

		checkNameValidity(name);

		if (binding == null) {
			throw new NullPointerException(
					"Binding can't be null for simple types");
		}

		if (Geometry.class.isAssignableFrom(binding)) {
			CoordinateReferenceSystem crs = null;
			if (metaData instanceof CoordinateReferenceSystem)
				crs = (CoordinateReferenceSystem) metaData;
			return new GeometryTypeImpl(name, binding, identified, nillable,
					restrictions, superType, false, crs);
		}

		return new AttributeTypeImpl(name, binding, identified, nillable,
				restrictions, superType, false); // simple types can't be
		// abstract
	}

	private void checkNameValidity(QName name) {
		if (name == null) {
			throw new NullPointerException("Null name not allowed");
		}
		String localName = name.getLocalPart();

		if ("".equals(localName)) {
			throw new IllegalArgumentException("Empty name not allowed");
		}

		if (localName.length() != localName.trim().length()) {
			throw new IllegalArgumentException(
					"Spaces not allowed at start and end of name");
		}
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createType(java.lang.String,
	 *      org.opengis.feature.schema.Descriptor)
	 */
	public ComplexTypeImpl createType(String name, Descriptor schema) {
		return createType(new QName(name), schema);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createType(javax.xml.namespace.QName,
	 *      org.opengis.feature.schema.Descriptor)
	 */
	public ComplexTypeImpl createType(QName name, Descriptor schema) {
		return createType(name, schema, false, null, false, null);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createType(javax.xml.namespace.QName,
	 *      org.opengis.feature.schema.Descriptor, boolean, java.lang.Class,
	 *      boolean, java.util.Set)
	 */
	public ComplexTypeImpl createType(QName name, Descriptor schema,
			boolean identified, Class binding, boolean nillable,
			Set<Filter> restrictions) {
		return createType(name, schema, identified, binding, nillable,
				restrictions, null, false);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createType(javax.xml.namespace.QName,
	 *      org.opengis.feature.schema.Descriptor, boolean, java.lang.Class,
	 *      boolean, java.util.Set, org.opengis.feature.type.ComplexType,
	 *      boolean)
	 */
	public ComplexTypeImpl createType(QName name, Descriptor schema,
			boolean identified, Class binding, boolean nillable,
			Set<Filter> restrictions, ComplexType superType, boolean isAbstract) {
		checkNameValidity(name);
		if (schema == null) {
			throw new NullPointerException(
					"A schema descriptor must be provided");
		}

		if (binding != null) {
			if (FeatureCollection.class.isAssignableFrom(binding)) {
				throw new UnsupportedOperationException(
						"needs to implement featurecollection creation");
			} else if (Feature.class.isAssignableFrom(binding)) {
				if (!(superType instanceof FeatureType))
					throw new IllegalArgumentException(
							"Feature binding implies a FeatureType super type, got "
									+ superType);
				return createFeatureType(name, schema, null, restrictions,
						(FeatureType) superType, isAbstract);
			}
		}

		return new ComplexTypeImpl(name, schema, identified, binding, nillable,
				restrictions);

	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createFeatureType(java.lang.String,
	 *      org.opengis.feature.schema.Descriptor,
	 *      org.opengis.feature.type.AttributeType)
	 */
	public FeatureTypeImpl createFeatureType(String name, Descriptor schema,
			GeometryType defaultGeom) {
		return createFeatureType(new QName(name), schema, defaultGeom);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createFeatureType(javax.xml.namespace.QName,
	 *      org.opengis.feature.schema.Descriptor,
	 *      org.opengis.feature.type.AttributeType)
	 */
	public FeatureTypeImpl createFeatureType(QName name, Descriptor schema,
			GeometryType defaultGeom) {
		return createFeatureType(name, schema, defaultGeom, null, null, false);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createFeatureType(javax.xml.namespace.QName,
	 *      org.opengis.feature.schema.Descriptor,
	 *      org.opengis.feature.type.AttributeType, java.util.Set,
	 *      org.opengis.feature.type.ComplexType, boolean)
	 */
	public FeatureTypeImpl createFeatureType(QName name, Descriptor schema,
			GeometryType defaultGeom, Set<Filter> restrictions,
			FeatureType superType, boolean isAbstract) {
		checkNameValidity(name);
		if (schema == null) {
			throw new NullPointerException(
					"A schema descriptor must be provided");
		}

		if (schema instanceof SimpleDescriptor) {
			if (superType != null
					&& (!(superType instanceof SimpleFeatureType))) {
				throw new IllegalArgumentException(
						"simple descriptor requires a SimpleFeatureType as super type");
			}
			return createFeatureType(name, (SimpleDescriptor) schema,
					defaultGeom, restrictions, (SimpleFeatureType) superType,
					isAbstract);
		}
		if(superType == null){
			superType = getGmlAbstractFeatureType();
		}
		
		return new FeatureTypeImpl(name, schema, defaultGeom, restrictions,
				superType, isAbstract);
	}

	/**
	 * 
	 */
	public SimpleFeatureTypeImpl createFeatureType(String name,
			List<AttributeType> types, GeometryType defaultGeometry) {
		return createFeatureType(new QName(name), types, defaultGeometry);
	}

	/**
	 * 
	 */
	public SimpleFeatureTypeImpl createFeatureType(QName name,
			List<AttributeType> types, GeometryType defaultGeometry) {

		return createFeatureType(name, types, defaultGeometry, null, null,
				false);
	}

	public SimpleFeatureTypeImpl createFeatureType(QName name,
			List<AttributeType> types, GeometryType defaultGeometry,
			Set<Filter> restrictions, SimpleFeatureType superType,
			boolean isAbstract) {
		List<AttributeDescriptor> attDescriptors = new ArrayList<AttributeDescriptor>();

		for (AttributeType type : types) {
			AttributeDescriptor node = new NodeImpl(type);
			attDescriptors.add(node);
		}
		SimpleDescriptor descriptor = new SimpleDescriptorImpl(attDescriptors);
		return createFeatureType(name, descriptor, defaultGeometry,
				restrictions, superType, isAbstract);
	}

	public SimpleFeatureTypeImpl createFeatureType(QName name,
			SimpleDescriptor schema, GeometryType defaultGeometry,
			Set<Filter> restrictions, SimpleFeatureType superType,
			boolean isAbstract) {

		if(superType == null){
			superType = getGmlAbstractFeatureType();
		}else{
			//TODO: this needs proper factory lookup system
			Descriptors helper = new Descriptors(new DescriptorFactoryImpl());
			Descriptor newDescriptor = helper.subtype(superType.getDescriptor(), schema);
			schema = (SimpleDescriptor)newDescriptor;
		}

		return new SimpleFeatureTypeImpl(name, schema, defaultGeometry,
				restrictions, superType, isAbstract);
	}
	
	/**
	 * Returns the abstract top level FeatureType in the GML namespace.
	 * <p>
	 * We can decide to have different implementations of this factory, say
	 * GML2TypeFactory, GML3TypeFactory, etc, and provide the appropriate
	 * default schema here.
	 * Currently the returned FeatureType has no attributes
	 * </p>
	 * @return
	 */
	public synchronized SimpleFeatureType getGmlAbstractFeatureType(){
		if(GML_FEATURE == null){
			String GML_NSURI = "http://www.opengis.net/gml";
			QName name = new QName(GML_NSURI, "Feature");
			List<AttributeDescriptor>atts = Collections.emptyList();
			SimpleDescriptor schema = new SimpleDescriptorImpl(atts);
			GML_FEATURE = new SimpleFeatureTypeImpl(name, schema, null,
					null, null, true);
		}
		return GML_FEATURE;
	}
	
}
