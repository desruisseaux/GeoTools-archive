package org.geotools.feature.type;

import java.util.Collection;
import java.util.Set;

import org.geotools.feature.Schemas;
import org.opengis.feature.schema.Schema;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Restriction;
import org.opengis.feature.type.Type;
import org.opengis.util.GenericName;

public class ComplexTypeImpl extends TypeImpl implements ComplexType {
	
	protected final Schema SCHEMA;
	
	public ComplexTypeImpl( String name, Schema schema){
		this( new GenericName( name), null );
	}
	public ComplexTypeImpl( GenericName name, Schema schema){
		super( name, null );
		SCHEMA = schema;
	}
	public ComplexTypeImpl(GenericName name, Schema schema, boolean identified, Class binding, boolean nillable, Set<Restriction> restrictions){
		super(name, binding, identified, nillable, restrictions );
		SCHEMA = schema;
	}	
	public ComplexTypeImpl(GenericName name, Schema schema, boolean identified, Class binding, boolean nillable, Set<Restriction> restrictions, ComplexType superType, boolean isAbstract){
		super(name, binding, identified, nillable, restrictions, superType, isAbstract );
		SCHEMA = schema;
	}
	public ComplexType getSuper() {
		return (ComplexType) SUPER;
	}
	public Schema getSchema() {
		return SCHEMA;
	}

	public Collection<Type> types() {
		return Schemas.types( SCHEMA );
	}

	public Type type(String name) {
		return Schemas.type( SCHEMA, name );
	}

}
