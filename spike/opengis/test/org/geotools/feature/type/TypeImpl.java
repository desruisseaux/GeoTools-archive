package org.geotools.feature.type;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;

public class TypeImpl implements AttributeType {
	// binding
	final protected QName NAME;
	protected final boolean IDENTIFIED;	
	final protected Class BINDING;
	// validation 
	final protected boolean NILLABLE;
	final Set<Filter> RESTRICTIONS;
	// supertypes
	final protected AttributeType SUPER;
	final protected boolean ABSTRACT;
	
	public TypeImpl( String name, Class binding ){
		this( new QName( name ), binding );
	}
	public TypeImpl( QName name, Class binding ){
		this( name, binding, false, false, null, null, false );
	}
	public TypeImpl( QName name, Class binding, boolean identified, boolean nillable, Set<Filter> restrictions ){
		this( name, binding, identified, nillable, restrictions, null, false );
	}
	public TypeImpl( QName name, Class binding, boolean identified, boolean nillable, Set<Filter> restrictions, AttributeType superType, boolean isAbstract ){
		NAME = name;
		BINDING = binding;
		IDENTIFIED = identified;
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
	public boolean isIdentified() {
		return IDENTIFIED;
	}
	public QName getName() {
		return NAME;
	}
	public String name() {
		return NAME.getLocalPart();
	}
	public Class getBinding() {
		return BINDING;
	}
	public Boolean isNilable() {
		return NILLABLE;
	}	
	public Set<Filter> getRestrictions() {
		return RESTRICTIONS;
	}
	public AttributeType getSuper() {
		return SUPER;
	}
	public boolean isAbstract() {
		return ABSTRACT;
	}
}
