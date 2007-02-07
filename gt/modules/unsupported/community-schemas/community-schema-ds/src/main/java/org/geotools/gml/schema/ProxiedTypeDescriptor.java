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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.feature.AttributeName;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;


/**
 * An {@link org.opengis.feature.schema.AttributeDescriptor} whose type
 * reference aquiring is being delayed until its first use, when it
 * will be asked for to the provided {@link org.opengis.feature.type.TypeFactory}
 * <p>
 * Intended use is only while the xml schema parsing project is being executed
 * and the attribute type referenced by an &lt;element type="ns:TypeName"/&gt; 
 * has not yet been resolved.
 * </p>
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @since 2.3.x
 * @source $URL$
 */
class ProxiedTypeDescriptor implements AttributeDescriptor {
	private AttributeType TYPE;
	private final AttributeName NAME;
	private final int MIN;
	private final int MAX;
	
	private Map CLIENT_PROPS;

	private TypeFactory typeFactory;
	private final AttributeName TYPE_NAME;
	
	public ProxiedTypeDescriptor(TypeFactory tf, AttributeName nodeName, AttributeName typeName, int min, int max) {
		if (tf == null) {
			throw new NullPointerException("TypeFactory");
		}
		if(nodeName == null || typeName == null){
			throw new NullPointerException("node name=" + nodeName +  ",type name=" + typeName);
		}

		MIN = min;
		MAX = max;
		NAME = nodeName;
		TYPE_NAME = typeName;
		CLIENT_PROPS = Collections.EMPTY_MAP;
		typeFactory = tf;
	}
	
	/**
	 * Returns the type of this attribute descriptor, will
	 * make a lookup in type factory on the first call to this
	 * method, so be sure its not used until the type is
	 * registered.
	 */
	public AttributeType/*<?>*/ getType() {
		if(TYPE == null){
			TYPE = typeFactory.getType(TYPE_NAME);
		}
		return TYPE;
	}
	
	public AttributeName getName() {
		return NAME;
	}
	
	public int getMinOccurs(){
		return MIN;
	}
	
	public int getMaxOccurs(){
		return MAX;
	}
	
	/**
	 * Allows the association of process specific information
	 * (such as XML prefix) with an attribute descriptor.
	 * 
	 * @param key Object used to allow String and Enum keys
	 * @param value Associated with key
	 */
	public void putClientProperty( Object key, Object value ){
		if(Collections.EMPTY_MAP.equals(this.CLIENT_PROPS)){
			CLIENT_PROPS = new HashMap();
		}
		CLIENT_PROPS.put(key, value);
	}
	
	/**
	 * Retrive associated process specific information
	 * (such as XML prefix).
	 * 
	 * @param key Object used to allow String and Enum keys
	 */	
	public Object getClientProperty( Object key ){
		return CLIENT_PROPS.get(key);
	}

	
	public int hashCode(){
		return (37 * MIN + 37 * MAX) ^ (TYPE_NAME.hashCode() * NAME.hashCode() * CLIENT_PROPS.hashCode());
	}
	
	public boolean equals(Object o){
		if(!(o instanceof ProxiedTypeDescriptor))
			return false;
		if(!super.equals(o))
			return false;
		
		ProxiedTypeDescriptor d = (ProxiedTypeDescriptor)o;
		return NAME.equals(d.NAME) && 
				TYPE_NAME.equals(d.TYPE_NAME) && 
				CLIENT_PROPS.equals(d.CLIENT_PROPS);
	}	
	
	public String toString(){
		StringBuffer sb = new StringBuffer("ProxiedTypeDescriptor[name=")
		.append(NAME)
		.append(", min=")
		.append(MIN)
		.append(", max=")
		.append(MAX)
		.append(", type=" + TYPE_NAME);
		if(CLIENT_PROPS.size() > 0){
			sb.append(", clientProperties=")
			.append(CLIENT_PROPS);
		}
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
