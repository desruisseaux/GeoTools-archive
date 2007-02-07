package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.AttributeFactoryImpl;
import org.geotools.feature.AttributeImpl;
import org.geotools.feature.attribute.TextualAttribute;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.BooleanAttribute;
import org.opengis.feature.simple.NumericAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.TemporalAttribute;
import org.opengis.feature.simple.TextAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeName;

/**
 * Construct specific types for SimpleFeatures.
 * <p>
 * Please note that this factory is <b>direct</b> and will implement what
 * you ask for - if you need asistence use a builder.
 * </p>
 * @author Jody Garnett
 */
public class SimpleFeatureFactoryImpl extends AttributeFactoryImpl implements
		SimpleFeatureFactory {

    /**
     * Create a list of properties from provided type + values.
     * <p>
     * Package visible for use by SimpleFeatureImpl constructors.
     * </p>
     * @return List<Attribute> based on provided values
     */
	public static List attributes( SimpleFeatureType type, Object values[] ){
		if( values == null ){
			values = new Object[ type.types().size()];
		}
		List attributes = new ArrayList( values.length );
		int index = 0;
		for( Iterator i=type.getProperties().iterator(); i.hasNext(); index++ ){
			AttributeDescriptor descriptor = (AttributeDescriptor) i.next();
			Object value = index < values.length ? values[ index ] : null;
			Attribute attribute = new AttributeImpl( value, descriptor, null ); 
			attributes.add( attribute );
		}
		return attributes;		
	}
	
	public BooleanAttribute createBooleanAttribute(Boolean value,
			AttributeDescriptor descriptor) {
		return new org.geotools.feature.attribute.BooleanAttribute(value,
				descriptor);
	}

	public NumericAttribute createNumericAttribute(Number value,
			AttributeDescriptor descriptor) {
		return new org.geotools.feature.attribute.NumericAttribute(value,
				descriptor);
	}

	public TemporalAttribute createTemporalAttribute(Date value,
			AttributeDescriptor descriptor) {
		return new org.geotools.feature.attribute.TemporalAttribute(value,
				descriptor);
	}

	public TextAttribute createTextAttribute(CharSequence value,
			AttributeDescriptor descriptor) {
		return new TextualAttribute(value, descriptor);
	}
	
	public Feature createFeature(Collection value, AttributeDescriptor desc, String id) {
		throw new UnsupportedOperationException("SimpleFeature cannot be nested");
	}
	public SimpleFeature createSimpleFeature(SimpleFeatureType type, String id, Object[] values) {
		return new SimpleFeatureImpl( type, id, values );
	}

	public SimpleFeatureCollection createSimpleFeatureCollection(SimpleFeatureCollectionType type, String id) {
		return new SimpleFeatureCollectionImpl(type, id);
	}

	final static AttributeDescriptor find( List descriptors, TypeName name ){
		if( name == null ) return null;
		for( Iterator i = descriptors.iterator(); i.hasNext(); ){
			AttributeDescriptor attributeType = (AttributeDescriptor) i.next();
			if( name.equals( attributeType.getType().getName() ) ){
				return attributeType;
			}
		}
		return null; // no default geometry here?
	}

	/** Create AttributeDescriptorImpl for this simple type */
	final static List descriptors( List typeList ){
		List descriptors = new ArrayList( typeList.size() );
		for( Iterator i = typeList.iterator(); i.hasNext(); ){
			AttributeType attributeType = (AttributeType) i.next();
			AttributeDescriptor attribute = new AttributeDescriptorImpl(attributeType, attributeType.getName(), 1,1,true );
			descriptors.add( attribute );
		}
		return descriptors;
	}
	final static TypeName geometryName( AttributeType geom ){
		if( geom == null ) return null;
		return geom.getName();
		
	}
}
