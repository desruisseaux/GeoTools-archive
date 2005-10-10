/**
 * 
 */
package org.geotools.feature.xpath;

import java.util.List;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.geotools.feature.Descriptors;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;

/**
 * JXPath property handler that works on Attribute
 * 
 * @author Gabriel Roldan
 */
public class AttributePropertyHandler implements DynamicPropertyHandler {

	public String[] getPropertyNames(java.lang.Object o) {
		Attribute att = (Attribute) o;
		String[] propNames = null;
		if (att instanceof ComplexAttribute) {
			ComplexAttribute complex = (ComplexAttribute) att;
			ComplexType type = complex.getType();
			List<AttributeType> contentTypes = Descriptors.types(type.getDescriptor());
			propNames = new String[contentTypes.size()];
			for (int i = 0; i < propNames.length; i++) {
				propNames[i] = contentTypes.get(i).name();
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
		if (o instanceof ComplexAttribute) {
			ComplexAttribute complex = (ComplexAttribute) o;
			// TODO: its just a test, still needs to handle namespaces when
			// propName comes with a
			// namespace prefix
			AttributeType type = Descriptors.type(complex.getType()
					.getDescriptor(), propName);
			value = complex.get(type);
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