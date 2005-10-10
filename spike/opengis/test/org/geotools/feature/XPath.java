package org.geotools.feature;

import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.AttributeType;

public class XPath {

	/**
	 * Shortcut to {@link #get(ComplexAttribute, String) get((ComplexAttribute)attribute, xpath);}.
	 * <p>
	 * Fails if <code>attribute</code> is not complex.
	 * </p>
	 * @param attribute
	 * @param xpath
	 * @return
	 */
	public static Object get(Attribute attribute, String xpath){
		if(!(attribute instanceof ComplexAttribute)){
			throw new IllegalArgumentException(
					"attribute must be complex in order to evaluate: " + attribute);
		}
		return get((ComplexAttribute)attribute, xpath);
	}

	/**
	 * Toy implementation just to get Filter working 
	 * @param attribute
	 * @param xpath
	 * @return
	 */
	public static Object get(ComplexAttribute attribute, String xpath){
		Descriptor schema = attribute.getType().getDescriptor();
		if(xpath.indexOf("/") != -1){
			throw new UnsupportedOperationException(
					"XPath not properly implemented yet, just getting single nested attributes");
		}
		
		String name = xpath;
		AttributeType type = Descriptors.type(schema, name);
		Object value = attribute.get(type);
		return value;
	}
}
