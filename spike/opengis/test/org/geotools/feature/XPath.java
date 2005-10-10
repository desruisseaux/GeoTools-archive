package org.geotools.feature;

import java.util.logging.Logger;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.geotools.feature.impl.AttributeImpl;
import org.geotools.feature.impl.ComplexAttributeImpl;
import org.geotools.feature.impl.FeatureImpl;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.xpath.AttributePropertyHandler;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.AttributeType;

public class XPath {
	private static final Logger LOGGER = Logger.getLogger(
			XPath.class.getPackage().getName());
	
	static{
		JXPathIntrospector.registerDynamicClass(AttributeImpl.class, AttributePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(ComplexAttributeImpl.class, AttributePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(FeatureImpl.class, AttributePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(SimpleFeatureImpl.class, AttributePropertyHandler.class);
		LOGGER.info("Registered " + AttributePropertyHandler.class.getName() + 
				" to handle geotools Attribute xpath expressions");
	}

	/**
	 * Applies the xpath expression given by <code>xpath</code> to the attributes
	 * of <code>feature</code> and returns the result.
	 * 
	 * @param feature
	 * @param xpath
	 * @return
	 */
	public static Object get(final Attribute att, final String xpath){
	 	JXPathContext ctx = JXPathContext.newContext(att);

	 	Object retVal = ctx.getValue(xpath);
	 	
	 	return retVal;
	}
	
	public static void set(final Attribute att, final String xpath, Object value){
		JXPathContext.newContext(att).setValue(xpath, value);
	}

	/*
	public static Object get(Attribute attribute, String xpath){
		if(!(attribute instanceof ComplexAttribute)){
			throw new IllegalArgumentException(
					"attribute must be complex in order to evaluate: " + attribute);
		}
		return get((ComplexAttribute)attribute, xpath);
	}

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
	*/
}
