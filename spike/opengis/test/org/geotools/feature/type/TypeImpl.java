package org.geotools.feature.type;

import java.util.Collections;
import java.util.Set;

import org.opengis.feature.type.Restriction;
import org.opengis.feature.type.Type;
import org.opengis.util.GenericName;

public class TypeImpl implements Type {
	// binding
	final protected GenericName NAME;
	final protected Class BINDING;
	// validation 
	final protected boolean NILLABLE;
	final Set<Restriction> RESTRICTIONS;
	// supertypes
	final protected Type SUPER;
	final protected boolean ABSTRACT;
	
	public TypeImpl( String name, Class binding ){
		this( new GenericName( null, name ), binding );
	}
	public TypeImpl( GenericName name, Class binding ){
		this( name, binding, false, null, null, false );
	}
	public TypeImpl( GenericName name, Class binding, boolean nillable, Set<Restriction> restrictions ){
		this( name, binding, nillable, restrictions, null, false );
	}
	public TypeImpl( GenericName name, Class binding, boolean nillable, Set<Restriction> restrictions, Type superType, boolean isAbstract ){
		NAME = name;
		BINDING = binding;
		SUPER = superType;
		ABSTRACT = isAbstract;
		NILLABLE = nillable;
		if( restrictions == null ){
			RESTRICTIONS = Collections.emptySet();
		}
		else {
			RESTRICTIONS = Collections.unmodifiableSet( restrictions );
		}
	}
	
	public GenericName getName() {
		return NAME;
	}
	public String name() {
		return NAME.getName();
	}
	public Class getBinding() {
		return BINDING;
	}
	public Boolean isNilable() {
		return NILLABLE;
	}	
	public Set<Restriction> getRestrictions() {
		return RESTRICTIONS;
	}
	public Type getSuper() {
		return SUPER;
	}
	public boolean isAbstract() {
		return ABSTRACT;
	}
}
