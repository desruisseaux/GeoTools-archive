package org.geotools.feature.simple;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * This implementation is capable of creating a good default choice for
 * capturing SimpleFeatureType, the focus is on corretness rather then
 * efficiency or even strict error messages.
 * 
 * @author Jody Garnett
 * @author Justin Deoliveira
 */
public class SimpleTypeFactoryImpl extends TypeFactoryImpl implements
		SimpleTypeFactory {
	
	public SimpleTypeFactoryImpl() {
		super();
	}
	public SimpleTypeFactoryImpl(CRSFactory crsFactory, FilterFactory filterFactory) {
		super( crsFactory, filterFactory );
	}
	
	public FeatureType createFeatureType(TypeName name, Collection schema,
			AttributeDescriptor defaultGeometry, CoordinateReferenceSystem crs,
			boolean isAbstract, Set restrictions, AttributeType superType,
			InternationalString description) {
		throw new UnsupportedOperationException();
//		return new SimpleFeatureTypeImpl(name, schema, defaultGeometry, crs,
//				restrictions, description);
	}

	public FeatureCollectionType createFeatureCollectionType(TypeName name,
			Collection properties, Collection members,
			AttributeDescriptor defaultGeom, CoordinateReferenceSystem crs,
			boolean isAbstract, Set restrictions, AttributeType superType,
			InternationalString description) {
		throw new UnsupportedOperationException();
//		return new SimpleFeatureCollectionTypeImpl(name,
//				(AssociationDescriptor) members.iterator().next(),
//				restrictions, description);
	}

	public org.geotools.feature.FeatureType createSimpleFeatureType( TypeName name,
			List typeList, GeometryAttributeType geometryType,
			CoordinateReferenceSystem crs, Set restrictions,
			InternationalString description ) {
		
		try {
			return new DefaultFeatureType( 
				name.getLocalPart(), name.getNamespaceURI(), typeList, Collections.EMPTY_LIST, geometryType
			);
		} 
		catch (NullPointerException e) {
			throw new RuntimeException( e );
		} 
		catch (SchemaException e) {
			throw new RuntimeException( e );
		}
	}
	
	public SimpleFeatureType createSimpleFeatureType(TypeName name,
			List typeList, AttributeType geometryType,
			CoordinateReferenceSystem crs, Set restrictions,
			InternationalString description) {

		throw new UnsupportedOperationException();
//		return new SimpleFeatureTypeImpl(name, typeList, geometryType, crs,
//				restrictions, description);
	}

	public SimpleFeatureCollectionType createSimpleFeatureCollectionType(
			TypeName name, SimpleFeatureType member, InternationalString description) {
		throw new UnsupportedOperationException();
		//return new SimpleFeatureCollectionTypeImpl(name, member, description);
	}

}
