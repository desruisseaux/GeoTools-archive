/**
 * 
 */
package org.geotools.feature.xpath;

import java.util.List;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.geotools.feature.Descriptors;
import org.opengis.feature.Attribute;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;

/**
 * JXPath property handler that works on Attribute
 * 
 * @author Gabriel Roldan
 */
public class AttributeTypePropertyHandler implements DynamicPropertyHandler {

	public String[] getPropertyNames(java.lang.Object o) {
		AttributeType att = (AttributeType) o;
		String[] propNames = null;
		if (att instanceof ComplexType) {
			ComplexType complexType = (ComplexType) att;
			List<AttributeType> childTypes = Descriptors.types(complexType.getDescriptor());
			propNames = new String[childTypes.size()];
			for (int i = 0; i < propNames.length; i++) {
				propNames[i] = childTypes.get(i).name();
			}
		}
		return propNames;
	}

	/**
	 * Returns the {@linkplain org.geotools.feature.impl.AttributeImpl} or
	 * subclass of it contained by the
	 * {@linkplain org.geotools.feature.impl.ComplexAttributeImpl}
	 * <code>o</code>.
	 */
	public Object getProperty(Object o, String propName) {
		Object value = null;
		if (o instanceof ComplexType) {
			ComplexType<?> complex = (ComplexType) o;
			Descriptor schema = complex.getDescriptor();
			AttributeType type = Descriptors.type(schema, propName);
			value = type;
		}
		return value;
	}

	public void setProperty(Object feature, String propertyName, Object value) {
		throw new UnsupportedOperationException("not yet implemented");
		/*
		 * try { ((Feature) feature).setAttribute(propertyName, value); } catch
		 * (IllegalAttributeException e) { throw new JXPathException("Setting
		 * attribute " + propertyName + ": " + e.getMessage(), e); }
		 */
	}
}