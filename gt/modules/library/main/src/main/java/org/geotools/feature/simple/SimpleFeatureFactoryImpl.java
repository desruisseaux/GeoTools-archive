package org.geotools.feature.simple;

import java.util.List;

import org.geotools.feature.AttributeImpl;
import org.geotools.feature.GeometryAttributeImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Construct specific types for SimpleFeatures.
 * <p>
 * Please note that this factory is <b>direct</b> and will implement what
 * you ask for - if you need asistence use a builder.
 * </p>
 * @author Jody Garnett
 */
public class SimpleFeatureFactoryImpl implements SimpleFeatureFactory {

	
	public Attribute createAttribute(Object value, AttributeDescriptor descriptor, String id) {
		return new AttributeImpl(value,descriptor,id);
	}
	
	public GeometryAttribute createGeometryAttribute(Object value, AttributeDescriptor descriptor, String id, CoordinateReferenceSystem crs) {
		return new GeometryAttributeImpl(value,descriptor,id,crs);
	}
	
	public SimpleFeatureCollection createSimpleFeatureCollection(
		SimpleFeatureCollectionType type, String id
	) {
		throw new UnsupportedOperationException();
		//return new SimpleFeatureCollectionImpl(type, id);
	}

	public SimpleFeature createSimpleFeature( List properties, SimpleFeatureType type, String id) {
		return new SimpleFeatureImpl( properties, type, id );
	}
}
