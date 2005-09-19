package org.geotools.feature.type;

import java.util.Collection;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.Schemas;
import org.opengis.feature.schema.Schema;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.filter.Filter;

public class ComplexTypeImpl extends TypeImpl implements ComplexType {
	
	protected final Schema SCHEMA;
	
	public ComplexTypeImpl( String name, Schema schema){
		this( new QName( name), null );
	}
	public ComplexTypeImpl( QName name, Schema schema){
		super( name, null );
		SCHEMA = schema;
	}
	public ComplexTypeImpl(QName name, Schema schema, boolean identified, Class binding, boolean nillable, Set<Filter> restrictions){
		super(name, binding, identified, nillable, restrictions );
		SCHEMA = schema;
	}	
	public ComplexTypeImpl(QName name, Schema schema, boolean identified, Class binding, boolean nillable, Set<Filter> restrictions, ComplexType superType, boolean isAbstract){
		super(name, binding, identified, nillable, restrictions, superType, isAbstract );
		SCHEMA = schema;
	}
	public ComplexType getSuper() {
		return (ComplexType) SUPER;
	}
	public Schema getSchema() {
		return SCHEMA;
	}

	public Collection<AttributeType> types() {
		return Schemas.types( SCHEMA );
	}

	public AttributeType type(String name) {
		return Schemas.type( SCHEMA, name );
	}

}
