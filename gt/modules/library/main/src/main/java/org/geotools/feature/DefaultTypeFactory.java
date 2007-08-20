package org.geotools.feature;

import java.util.List;
import java.util.Set;

import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * An extension of {@link SimpleTypeFactoryImpl} which creates 
 * {@link DefaultFeatureType} instances.
 *  
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * 
 * @deprecated use {@link SimpleTypeFactory}, this class is only provided to 
 * maintain backwards compatability for transition to geoapi feature model and 
 * will be removed in subsequent versions.
 * 
 * @since 2.5
 *
 */
public class DefaultTypeFactory extends SimpleTypeFactoryImpl {

	/**
	 * Override which returns {@link DefaultFeatureType}.
	 */
	public SimpleFeatureType createSimpleFeatureType(Name name, List schema, AttributeDescriptor defaultGeometry, CoordinateReferenceSystem crs, Set restrictions, InternationalString description) {
		return new DefaultFeatureType(name,schema,defaultGeometry,crs,restrictions,description);
	}
}
