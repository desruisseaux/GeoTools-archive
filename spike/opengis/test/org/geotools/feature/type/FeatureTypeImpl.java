package org.geotools.feature.type;

import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.feature.schema.Schema;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

public class FeatureTypeImpl extends ComplexTypeImpl implements FeatureType {
	protected AttributeType DEFAULT;

	public FeatureTypeImpl(String name, Schema schema, AttributeType defaultGeom ) {
		this( new QName( name ), schema, defaultGeom );
	}	
	public FeatureTypeImpl(QName name, Schema schema, AttributeType defaultGeom ) {
		super(name, schema, true, null, false, null);
		DEFAULT = defaultGeom;
	}
	public FeatureTypeImpl(QName name, Schema schema, AttributeType defaultGeom, Set<Filter> restrictions, ComplexType superType, boolean isAbstract){
		super(name, schema, true, null, false, restrictions, superType, isAbstract );
	}
	public AttributeType getDefaultGeometry() {
		return DEFAULT;
	}
}
