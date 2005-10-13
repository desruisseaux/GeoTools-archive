package org.geotools.feature.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.Descriptors;
import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.schema.NodeImpl;
import org.geotools.feature.simple.SimpleDescriptorImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.filter.Filter;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.simple.SimpleDescriptor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class TypeFactoryImpl implements TypeFactory {

	private static final String GML_NSURI = "http://www.opengis.net/gml";

	private static SimpleFeatureType GML_FEATURE;

	private static FeatureCollectionType GML_FEATURECOLLECTION;

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
			if(crs == null && (superType instanceof GeometryType)){
				crs = ((GeometryType)superType).getCRS();
			}
			return new GeometryTypeImpl(name, binding, identified, nillable,
					restrictions, superType, false, crs);
		}

		return new AttributeTypeImpl(name, binding, identified, nillable,
				restrictions, superType, false); // simple types can't be
		// abstract
	}

	public GeometryType createGeometryType(QName name, Class binding,
			boolean nillable, CoordinateReferenceSystem crs) {
		return createGeometryType(name, binding, false, nillable, crs, null,
				null);
	}

	public GeometryType createGeometryType(QName name, Class binding,
			boolean identified, boolean nillable,
			CoordinateReferenceSystem crs, Set<Filter> restrictions,
			GeometryType superType) {

		if (!Geometry.class.isAssignableFrom(binding)) {
			throw new IllegalArgumentException("binding");
		}

		return (GeometryType) createType(name, binding, identified, nillable,
				restrictions, superType, crs);
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
		return createType(name, schema, false, false, null);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createType(javax.xml.namespace.QName,
	 *      org.opengis.feature.schema.Descriptor, boolean, java.lang.Class,
	 *      boolean, java.util.Set)
	 */
	public ComplexTypeImpl createType(QName name, Descriptor schema,
			boolean identified, boolean nillable, Set<Filter> restrictions) {
		return createType(name, schema, identified, nillable, restrictions,
				null, false);
	}

	/**
	 * @see org.opengis.feature.type.TypeFactory#createType(javax.xml.namespace.QName,
	 *      org.opengis.feature.schema.Descriptor, boolean, java.lang.Class,
	 *      boolean, java.util.Set, org.opengis.feature.type.ComplexType,
	 *      boolean)
	 */
	public ComplexTypeImpl createType(QName name, Descriptor schema,
			boolean identified, boolean nillable, Set<Filter> restrictions,
			ComplexType superType, boolean isAbstract) {
		checkNameValidity(name);
		if (schema == null) {
			throw new NullPointerException(
					"A schema descriptor must be provided");
		}

		if (superType != null) {
			if (superType instanceof FeatureCollectionType) {
				return createFeatureCollectionType(name, null, schema, null,
						restrictions, (FeatureCollectionType) superType,
						isAbstract);
			} else if (superType instanceof FeatureType) {
				return createFeatureType(name, schema, null, restrictions,
						(FeatureType) superType, isAbstract);
			}
		}
		return new ComplexTypeImpl(name, schema, identified, nillable,
				restrictions, superType, isAbstract);
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
		if (superType == null) {
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

		if (superType == null) {
			superType = getGmlAbstractFeatureType();
		} else {
			// TODO: this needs proper factory lookup system
			Descriptors helper = new Descriptors(new DescriptorFactoryImpl());
			Descriptor newDescriptor = helper.subtype(
					superType.getDescriptor(), schema);
			schema = (SimpleDescriptor) newDescriptor;
		}

		return new SimpleFeatureTypeImpl(name, schema, defaultGeometry,
				restrictions, superType, isAbstract);
	}

	public FeatureCollectionType createFeatureCollectionType() {
		return createFeatureCollectionType(null, null, null, null, null, null,
				false);
	}

	public FeatureCollectionType createFeatureCollectionType(
			FeatureType membersType) {
		return createFeatureCollectionType(null, membersType);
	}

	/**
	 * Creates a FeatureCollectionType named <code>name</code> whose member
	 * Features can be of any FeatureType.
	 * 
	 * @param name
	 * @return
	 */
	public FeatureCollectionType createFeatureCollectionType(QName name) {
		return createFeatureCollectionType(name, null, null, null, null, null,
				false);
	}

	/**
	 * Creates a FeatureCollectionType named <code>name</code> whose member
	 * Features can be only of <code>membersType</code> FeatureType.
	 * 
	 * @param name
	 * @param membersType
	 * @return
	 */
	public FeatureCollectionType createFeatureCollectionType(QName name,
			FeatureType membersType) {
		Set<FeatureType> members = null;
		if (membersType != null) {
			members = new HashSet<FeatureType>();
			members.add(membersType);
		}
		return createFeatureCollectionType(name, members, null, null, null,
				null, false);
	}

	/**
	 * Creates a FeatureCollectionType named <code>name</code> whose member
	 * Features can be of any of the FeatureTypes in <code>membersTypes</code>.
	 * <p>
	 * All parametesr may be null, in which case sensible defaults will be
	 * applied.
	 * </p>
	 * 
	 * @param name
	 *            name of FeatureCollectionType. Required if
	 *            <code>schema != null</code>. Otherwise, <code>null</code>
	 *            is passed, <code>gml:FeatureCollection</code> will be used.
	 * @param membersTypes
	 *            list of allowable FeatureTypes that Feature members must
	 *            adhere to.
	 * @param schema
	 *            the schema for the Feature representation of the collection.
	 *            You will generally pass <code>null</code>, at least you
	 *            want to add attributes to the FeatureCollection itself.
	 * @param defaultGeom
	 *            only needed if adding attributes to the Feature aspect of the
	 *            collection. Use <code>null</code> if you don't add
	 *            GeometryTypes or are just adding one, in which case it will be
	 *            used as the default geometry.
	 * @param restrictions
	 *            restrictions applied to contained Features.
	 * @param superType
	 *            the FeatureCollectionType the created one inherits from.
	 * @param isAbstract
	 *            wether the created FeatureCollectionType is abstract.
	 * @return
	 */
	public FeatureCollectionTypeImpl createFeatureCollectionType(QName name,
			Set<FeatureType> membersTypes, Descriptor schema,
			GeometryType defaultGeom, Set<Filter> restrictions,
			FeatureCollectionType<?> superType, boolean isAbstract) {
		if (name == null) {
			if (schema != null) {
				throw new NullPointerException("extending schema requires name");
			}
			name = new QName(GML_NSURI, "FeatureCollection");
		}
		if (membersTypes == null || membersTypes.isEmpty()) {
			membersTypes = new HashSet<FeatureType>();
			membersTypes.add(getGmlAbstractFeatureType());
		}

		// create descriptor of allowable members
		DescriptorFactory descFactory = new DescriptorFactoryImpl();
		Descriptor members = null;
		if (membersTypes.size() == 1) {
			FeatureType singleType = membersTypes.iterator().next();
			members = descFactory.node(singleType, 0, Integer.MAX_VALUE);
		} else {
			Set<AttributeDescriptor> memberDescriptors = new HashSet<AttributeDescriptor>();
			for (FeatureType type : membersTypes) {
				AttributeDescriptor attDesc = descFactory.node(type, 1, 1);
				memberDescriptors.add(attDesc);
			}
			members = descFactory.choice(memberDescriptors, 0,
					Integer.MAX_VALUE);
		}

		if (superType == null) {
			superType = getGmlAbstractFeatureCollectionType();
		}

		// schema for the Feature aspect of the collection
		if (schema == null) {
			schema = superType.getDescriptor();
		} else {
			// TODO: this needs proper factory lookup system
			Descriptors helper = new Descriptors(new DescriptorFactoryImpl());
			schema = helper.subtype(superType.getDescriptor(), schema);
		}

		final FeatureCollectionTypeImpl type;

		type = new FeatureCollectionTypeImpl(name, members, schema,
				defaultGeom, restrictions, superType, isAbstract);
		return type;
	}

	/**
	 * Returns the abstract top level FeatureType in the GML namespace.
	 * <p>
	 * We can decide to have different implementations of this factory, say
	 * GML2TypeFactory, GML3TypeFactory, etc, and provide the appropriate
	 * default schema here. Currently the returned FeatureType has no attributes
	 * </p>
	 * 
	 * @return
	 */
	public static synchronized SimpleFeatureType getGmlAbstractFeatureType() {
		if (GML_FEATURE == null) {
			QName name = new QName(GML_NSURI, "Feature");
			List<AttributeDescriptor> atts = Collections.emptyList();
			SimpleDescriptor schema = new SimpleDescriptorImpl(atts);
			GML_FEATURE = new SimpleFeatureTypeImpl(name, schema, null, null,
					null, true);
		}
		return GML_FEATURE;
	}

	/**
	 * Returns the abstract top level FeatureCollectionType in the GML
	 * namespace.
	 * <p>
	 * We can decide to have different implementations of this factory, say
	 * GML2TypeFactory, GML3TypeFactory, etc, and provide the appropriate
	 * default schema here. Currently the returned FeatureType has no attributes
	 * </p>
	 * TODO: review
	 * 
	 * @return
	 */
	public static synchronized FeatureCollectionType getGmlAbstractFeatureCollectionType() {
		if (GML_FEATURECOLLECTION == null) {
			FeatureType<?> featureType = getGmlAbstractFeatureType();

			// TODO: this needs proper factory lookup system
			DescriptorFactory descFactory = new DescriptorFactoryImpl();

			AttributeDescriptor memberDescriptor = descFactory.node(
					featureType, 1, Integer.MAX_VALUE);
			List<Descriptor> contents = Arrays
					.asList(new Descriptor[] { memberDescriptor });

			Descriptor schema = descFactory.ordered(contents, 0,
					Integer.MAX_VALUE);

			Descriptors helper = new Descriptors(descFactory);
			Descriptor newDescriptor = helper.subtype(featureType
					.getDescriptor(), schema);

			schema = newDescriptor;

			final QName name = new QName(GML_NSURI, "AbstractFeatureCollection");
			final Descriptor members = memberDescriptor;
			final GeometryType defaultGeom = null;
			final Set<Filter> restrictions = null;
			final FeatureType superType = featureType;
			final boolean isAbstract = true;

			GML_FEATURECOLLECTION = new FeatureCollectionTypeImpl(name,
					members, schema, defaultGeom, restrictions, superType,
					isAbstract);
		}
		return GML_FEATURECOLLECTION;
	}

}
