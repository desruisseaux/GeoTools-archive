/**
 * 
 */
package org.geotools.feature.impl.attribute;

import org.geotools.feature.impl.AttributeImpl;
import org.opengis.feature.type.AttributeType;

/**
 * @author Gabriel Roldan
 */
public class BooleanAttribute extends AttributeImpl {

	/**
	 * @param type
	 */
	public BooleanAttribute(AttributeType type) {
		this(type, null);
	}

	/**
	 * @param id
	 * @param type
	 * @param content
	 */
	public BooleanAttribute(AttributeType type, Object content) {
		super(null, type, content);
	}

	protected Object parse(Object value) throws IllegalArgumentException {
		Class type = super.TYPE.getBinding();
		// handle null values first
		if (value == null) {
			return value;
		}

		// no parse needed here if types are compatable
		if ((value.getClass() == type)
				|| type.isAssignableFrom(value.getClass())) {
			return value;
		}

		// if it is not 0 or 1, fails
		if (value instanceof Number) {
			return DateUtil.parseBoolean(String.valueOf(((Number)value).intValue()));
		}

		if (value instanceof CharSequence) {
			return DateUtil.parseBoolean(value.toString());
		}
		// nothing else to do
		throw new IllegalArgumentException("Cannot parse " + value.getClass());
	}

}
