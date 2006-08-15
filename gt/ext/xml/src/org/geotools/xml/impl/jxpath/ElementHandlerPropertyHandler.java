package org.geotools.xml.impl.jxpath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.impl.ElementHandler;

/**
 * A property handler which allows instances of 
 * {@link org.geotools.xml.impl.ElementHandler} to be handled as beans.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ElementHandlerPropertyHandler 
	implements DynamicPropertyHandler {

	public String[] getPropertyNames(Object object) {
		ElementHandler handler = (ElementHandler)object;
		
		List children = handler.getChildHandlers();
		if (children == null || children.isEmpty())
			return new String[]{};

		HashSet properties = new HashSet();
		//String[] properties = new String[children.size()];
		for (int i = 0; i < children.size(); i++) {
			ElementHandler child = (ElementHandler)children.get(i);
			ElementInstance element = (ElementInstance)child.getComponent();
			
			//properties[i] = element.getName();
			properties.add(element.getName());
		}
		
		return (String[])properties.toArray(new String[properties.size()]);
	}

	public Object getProperty(Object object, String propertyName) {
		ElementHandler handler = (ElementHandler)object;
		
		List children = handler.getChildHandlers();
		if (children == null || children.isEmpty())
			return null;

		List property = new ArrayList();
		for (int i = 0; i < children.size(); i++) {
			ElementHandler child = (ElementHandler)children.get(i);
			ElementInstance element = (ElementInstance)child.getComponent();
			
			if (propertyName.equals(element.getName()))
				property.add(child);
				//return child; 
		}
		
		return property;
		//return null;
	}

	public void setProperty(Object object, String propertyName, Object value) {
		throw new UnsupportedOperationException();
	}

}
