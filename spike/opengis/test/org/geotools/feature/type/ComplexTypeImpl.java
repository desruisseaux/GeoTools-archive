package org.geotools.feature.type;

import java.util.Collection;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.Descriptors;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.filter.Filter;

public class ComplexTypeImpl extends AttributeTypeImpl implements ComplexType {
	
	protected final Descriptor SCHEMA;
	
	public ComplexTypeImpl( String name, Descriptor schema){
		this( new QName( name), schema );
	}
	public ComplexTypeImpl( QName name, Descriptor schema){
		super( name, null );
		SCHEMA = schema;
	}
	public ComplexTypeImpl(QName name, Descriptor schema, boolean identified, Class binding, boolean nillable, Set<Filter> restrictions){
		super(name, binding, identified, nillable, restrictions );
		SCHEMA = schema;
	}	
	public ComplexTypeImpl(QName name, Descriptor schema, boolean identified, Class binding, boolean nillable, Set<Filter> restrictions, ComplexType superType, boolean isAbstract){
		super(name, binding, identified, nillable, restrictions, superType, isAbstract );
		SCHEMA = schema;
	}
	public ComplexType getSuper() {
		return (ComplexType) SUPER;
	}
	public Descriptor getDescriptor() {
		return SCHEMA;
	}

	public Collection<AttributeType> types() {
		return Descriptors.types( SCHEMA );
	}

	public AttributeType type(String name) {
		return Descriptors.type( SCHEMA, name );
	}

}
