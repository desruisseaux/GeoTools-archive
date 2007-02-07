/**
 * 
 */
package org.geotools.feature.xpath;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.geotools.feature.Types;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;

/**
 * JXPath property handler that works on Attribute.
 * 
 * @author Gabriel Roldan
 * @author Justin Deoliveira
 */
public class AttributePropertyHandler implements DynamicPropertyHandler {

	
	public String[] getPropertyNames(Object o) {
		Attribute att = (Attribute) o;
		
		//we only work on complex attributes
		if (att instanceof ComplexAttribute) {
			
			ComplexType type = (ComplexType) att.getType();
			Collection attributes = type.attributes(); 
			
			String[] propNames = new String[attributes.size()];
			int i = 0;
			for (Iterator itr = attributes.iterator(); itr.hasNext(); i++) {
				AttributeDescriptor descriptor = (AttributeDescriptor) itr.next();
				
				//JD: this ignores namespaces
				propNames[i] = descriptor.getName().getLocalPart();
			}
			
			return propNames;
		}
		
		return null;
	}

	public Object getProperty(Object o, String propName) {
		Object value = null;
		if (o instanceof ComplexAttribute) {
			ComplexAttribute attribute = (ComplexAttribute) o;
			Name name = Types.attributeName(propName);
			
			List found = attribute.get(name);
			
			value = found.size() == 0 ? 
					null : (found.size() == 1? found.get(0) : found);
			
//			if(value == null && propName.equals(complex.getType().getName().getLocalPart())){
//				value = o;
//			}
		}
		return value;
	}

	public void setProperty(Object att, String name, Object value) {
//		Attribute attribute = (Attribute)att;
//		if (att instanceof ComplexAttribute) {
//			
//		}
//		else {
//			//just set the value
//			
//		}
//		
//		if(!(attribute instanceof ComplexAttribute)){
//			if(!propertyName.equals(attribute.getType().getName().getLocalPart())){
//				throw new IllegalArgumentException("only self reference to type allowed for simple attributes");
//			}
//			attribute.set(value);
//		}else{
//			ComplexAttribute complex = (ComplexAttribute)attribute;
//			List/*<Attribute>*/atts = complex.get(new org.geotools.util.AttributeName(propertyName));
//			if(atts.size() == 0){
//				throw new IllegalArgumentException("No attributes of type " + propertyName + " found");
//			}
//			((Attribute)atts.get(0)).set(value);
//		}
	}
}