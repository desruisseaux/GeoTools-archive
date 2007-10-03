package org.geotools.feature;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.Descriptors;
import org.geotools.feature.type.Types;
import org.geotools.resources.Utilities;
import org.geotools.util.Converters;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import sun.security.action.GetBooleanAction;

/**
 * Simple, mutable class to store attributes.
 * 
 * @author Rob Hranac, VFNY
 * @author Chris Holmes, TOPP
 * @author Ian Schneider
 * @author Jody Garnett
 * @author Gabriel Roldan
 * @version $Id$
 */
public class AttributeImpl extends PropertyImpl implements Attribute {

	/**
	 * id of the attribute.
	 */
	protected final String id;

	public AttributeImpl(Object content, AttributeDescriptor descriptor,
			String id) {
	    super( content, descriptor );
	    this.id = id;
	    
	    Types.validate(this, getValue());
	}

	public AttributeImpl(Object content, AttributeType type, String id) {
	    this( content, new AttributeDescriptorImpl( type, type.getName(), 1, 1, true, null), id );
	}

	public String getID() {
		return id;
	}
	
	public AttributeDescriptor getDescriptor() {
	    return (AttributeDescriptor) super.getDescriptor();
	}
	
	public AttributeType getType() {
	    return (AttributeType) super.getType();
	}
	
	/**
	 * Override of setValue to convert the newValue to specified type if need
	 * be.
	 */
	public void setValue(Object newValue) throws IllegalArgumentException,
			IllegalStateException {

		newValue = parse(newValue);

		//TODO: remove this validation
		Types.validate(getType(), this, newValue);
		super.setValue( newValue );
	}

	/**
	 * Override of hashCode.
	 * 
	 * @return hashCode for this object.
	 */
	public int hashCode() {
	    return super.hashCode() + ( 37 * (id == null ? 0 : id.hashCode()) );
	}

	/**
	 * Override of equals.
	 * 
	 * @param other
	 *            the object to be tested for equality.
	 * 
	 * @return whether other is equal to this attribute Type.
	 */
	public boolean equals(Object obj) {
	    if ( this == obj ) {
	        return true;
	    }
	    
		if (!(obj instanceof AttributeImpl)) {
			return false;
		}

		if (!super.equals(obj)) {
		    return false;
		}
		
		AttributeImpl att = (AttributeImpl) obj;
		
		return Utilities.equals( id, att.id );
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append(":");
        sb.append(getDescriptor().getName().getLocalPart());
        if(!getDescriptor().getName().getLocalPart().equals(getDescriptor().getType().getName().getLocalPart()) ||
                id != null){
            sb.append("<");
            sb.append(getDescriptor().getType().getName().getLocalPart());
            if( id != null ){
                sb.append( " id=");
                sb.append( id );
            }
            sb.append(">");
        }
        sb.append("=");
        sb.append(value);
        return sb.toString();
	}
	
	/**
	 * Allows this Attribute to convert an argument to its prefered storage
	 * type. If no parsing is possible, returns the original value. If a parse
	 * is attempted, yet fails (i.e. a poor decimal format) throw the Exception.
	 * This is mostly for use internally in Features, but implementors should
	 * simply follow the rules to be safe.
	 * 
	 * @param value
	 *            the object to attempt parsing of.
	 * 
	 * @return <code>value</code> converted to the preferred storage of this
	 *         <code>AttributeType</code>. If no parsing was possible then
	 *         the same object is returned.
	 * 
	 * @throws IllegalArgumentException
	 *             if parsing is attempted and is unsuccessful.
	 */
	protected Object parse(Object value) throws IllegalArgumentException {
    	if ( value != null ) {
    		Class target = getType().getBinding(); 
    		if ( !target.isAssignableFrom( value.getClass() ) ) {
    			// attempt to convert
    			Object converted = Converters.convert(value,target);
    			if ( converted != null ) {
    				value = converted;
    			}
    		}
    	}
    	
    	return value;
    }
}
