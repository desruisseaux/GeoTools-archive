/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.gml.schema;

import java.util.List;

import org.opengis.feature.AttributeName;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;


/**
 * {@link org.opengis.feature.schema.AttributeDescriptor} proxy. 
 * <p>
 * Actual instance will be resolved agains the provided TypeFactory.
 * 
 * Intended use is only while the xml schema parsing project is being executed
 * and the attribute type referenced by an &lt;element type="ns:TypeName"/&gt; 
 * has not yet been resolved.
 * </p>
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @since 2.3.x
 * @source $URL$
 */
class ProxiedAttributeDescriptor implements AttributeDescriptor {

	private AttributeDescriptor proxied;
	private final AttributeName NAME;
	private TypeFactory typeFactory;
	private final int minOccurs, maxOccurs;
	
	public ProxiedAttributeDescriptor(TypeFactory tf, AttributeName nodeName, int min, int max) {
		if (tf == null) {
			throw new NullPointerException("TypeFactory");
		}
		if(nodeName == null){
			throw new NullPointerException("node name=");
		}
		NAME = nodeName;
		typeFactory = tf;
		minOccurs = min;
		maxOccurs = max;
	}

	private AttributeDescriptor resolve(){
		if(proxied == null){
			proxied = typeFactory.getDescriptor(NAME);
		}
		return proxied;
	}
	/**
	 * Returns the type of this attribute descriptor, will
	 * make a lookup in type factory on the first call to this
	 * method, so be sure its not used until the type is
	 * registered.
	 */
	public AttributeType/*<?>*/ getType() {
		return resolve() == null? null : resolve().getType();
	}
	
	public AttributeName getName() {
		return NAME;
	}
	
	public int getMinOccurs(){
		return minOccurs;
	}
	
	public int getMaxOccurs(){
		return maxOccurs;
	}
	
	/**
	 * Allows the association of process specific information
	 * (such as XML prefix) with an attribute descriptor.
	 * 
	 * @param key Object used to allow String and Enum keys
	 * @param value Associated with key
	 */
	public void putClientProperty( Object key, Object value ){
		resolve().putClientProperty(key, value);
	}
	
	/**
	 * Retrive associated process specific information
	 * (such as XML prefix).
	 * 
	 * @param key Object used to allow String and Enum keys
	 */	
	public Object getClientProperty( Object key ){
		return resolve().getClientProperty(key);
	}

	
	public int hashCode(){
		return 37 ^ NAME.hashCode();
	}
	
	public boolean equals(Object o){
		if(!(o instanceof ProxiedAttributeDescriptor))
			return false;
		if(!super.equals(o))
			return false;
		
		ProxiedAttributeDescriptor d = (ProxiedAttributeDescriptor)o;
		return NAME.equals(d.NAME);
	}	
	
	public String toString(){
		StringBuffer sb = new StringBuffer("ProxiedAttributeDescriptor[name=");
		sb.append(NAME);
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Does nothing, guess this method will be removed
	 * from the interface
	 */
	public void validate(List content){
	}
}
