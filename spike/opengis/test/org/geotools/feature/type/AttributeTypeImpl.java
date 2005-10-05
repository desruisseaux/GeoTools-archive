package org.geotools.feature.type;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;

public class AttributeTypeImpl implements AttributeType {
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
	
	public AttributeTypeImpl( String name, Class binding ){
		this( new QName( name ), binding );
	}
	public AttributeTypeImpl( QName name, Class binding ){
		this( name, binding, false, false, null, null, false );
	}
	public AttributeTypeImpl( QName name, Class binding, boolean identified, boolean nillable, Set<Filter> restrictions ){
		this( name, binding, identified, nillable, restrictions, null, false );
	}
	public AttributeTypeImpl( QName name, Class binding, boolean identified, boolean nillable, Set<Filter> restrictions, AttributeType superType, boolean isAbstract ){
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
	public Boolean isNillable() {
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
	
    /**
     * Override of hashCode.
     *
     * @return hashCode for this object.
     */
    public int hashCode() {
        return getName().hashCode() ^ (getBinding() == null? 17 : getBinding().hashCode());
    }

    /**
     * Override of equals.
     *
     * @param other the object to be tested for equality.
     *
     * @return whether other is equal to this attribute Type.
     */
    public boolean equals(Object other) {
        if (!(other instanceof AttributeTypeImpl)) {
            return false;
        }

        AttributeType att = (AttributeType) other;

        /*
         * I guess we don't allow null names?
        if (name == null) {
            if (att.getName() != null) {
                return false;
            }
        }
        */

        if (!NAME.equals(att.getName())) {
            return false;
        }

        if ( (BINDING == null && att.getBinding() != null) || (BINDING != null && !BINDING.equals(att.getBinding()))) {
            return false;
        }

    	if(IDENTIFIED != att.isIdentified()){
    		return false;
    	}

    	if(NILLABLE != att.isNillable()){
    		return false;
    	}
    	
    	if(ABSTRACT != att.isAbstract()){
    		return false;
    	}

    	if(!RESTRICTIONS.equals(att.getRestrictions())){
    		return false;
    	}
    	
    	if( (SUPER == null && att.getSuper() != null) || (SUPER != null && !SUPER.equals(att.getSuper()))){
    		return false;
    	}
    	
    	return true;
    }
	
}
