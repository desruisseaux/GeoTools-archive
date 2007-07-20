package org.geotools.feature.simple;

import java.util.List;
import java.util.Set;

import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * Default factory for creating simple feature types.
 * 
 * @author Jody Garnett
 * @author Justin Deoliveira
 */
public class SimpleTypeFactoryImpl /*extends TypeFactoryImpl*/ implements
		SimpleTypeFactory {
	
	public SimpleTypeFactoryImpl() {
		super();
	}

	public SimpleFeatureType createSimpleFeatureType(TypeName name,
			List schema, AttributeDescriptor defaultGeometry,
			CoordinateReferenceSystem crs, Set restrictions,
			InternationalString description) {

		return new SimpleFeatureTypeImpl(name,schema,defaultGeometry,crs,restrictions,description);
	}

	public SimpleFeatureCollectionType createSimpleFeatureCollectionType(
			TypeName name, SimpleFeatureType member, InternationalString description) {
		throw new UnsupportedOperationException();
		//return new SimpleFeatureCollectionTypeImpl(name, member, description);
	}

}
