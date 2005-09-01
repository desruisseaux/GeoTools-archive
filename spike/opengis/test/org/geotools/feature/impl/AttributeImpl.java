/**
 * 
 */
package org.geotools.feature.impl;

import org.opengis.feature.Attribute;
import org.opengis.feature.type.Type;

/**
 * @author Jody Garnett
 */
public class AttributeImpl implements Attribute {
	Object content;
	final Type TYPE;
	protected final String ID;	
	public AttributeImpl( Type type ){
		this( null, type );
	}
	public AttributeImpl( String id, Type type ){
		ID = id;
		TYPE = type;
	}
	public String name() {
		return getType().getName().toString();
	}
	public String getID() {
		return ID;
	}
	public Object get() {
		return content;
	}
	public void set(Object newValue) {
		content = newValue;
	}
	public Type getType() {
		return null;
	}
}
