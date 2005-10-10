/**
 * 
 */
package org.geotools.feature.impl;

import org.geotools.filter.Filter;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeType;

/**
 * Simple, mutable class to store attributes.
 * 
 * @author Rob Hranac, VFNY
 * @author Chris Holmes, TOPP
 * @author Ian Schneider
 * @author Jody Garnett
 * @author Gabriel Roldan
 * @version $Id$
 */
public class AttributeImpl implements Attribute {
	protected Object content;

	protected final AttributeType TYPE;

	protected final String ID;

	public AttributeImpl(AttributeType type) {
		this(null, type);
	}

	public AttributeImpl(String id, AttributeType type) {
		if (type == null) {
			throw new NullPointerException("type");
		}
		if (type.isAbstract()) {
			throw new UnsupportedOperationException(type.getName()
					+ " is abstract");
		}
		ID = id;
		TYPE = type;
	}

	public AttributeImpl(String id, AttributeType type, Object content) {
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

	public AttributeType getType() {
		return TYPE;
	}

	/**
	 * 
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 *             if the value has been parsed and validater, yet this
	 *             Attribute does not passes the restrictions imposed by its
	 *             AttributeType
	 */
	public void set(Object newValue) throws IllegalArgumentException,
			IllegalStateException {
		newValue = parse(newValue);
		validate(newValue);
		content = newValue;
	}

	/**
	 * Override of hashCode.
	 * 
	 * @return hashCode for this object.
	 */
	public int hashCode() {
		return 37 * TYPE.hashCode() + (37 * (ID == null ? 0 : ID.hashCode()))
				+ (37 * (content == null ? 0 : content.hashCode()));
	}

	/**
	 * Override of equals.
	 * 
	 * @param other
	 *            the object to be tested for equality.
	 * 
	 * @return whether other is equal to this attribute Type.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof AttributeImpl)) {
			return false;
		}

		AttributeImpl att = (AttributeImpl) other;

		if (!(TYPE.equals(att.TYPE))) {
			return false;
		}

		if (ID == null) {
			if (att.ID != null) {
				return false;
			}
		} else if (!ID.equals(att.ID)) {
			return false;
		}

		if (content == null) {
			if (att.content != null) {
				return false;
			}
		} else if (!content.equals(att.content)) {
			return false;
		}

		return true;
	}

	/**
	 * Allows this Attribute to convert an argument to its prefered storage
	 * type. If no parsing is possible, returns the original value. If a parse
	 * is attempted, yet fails (i.e. a poor decimal format) throw the Exception.
	 * This is mostly for use internally in Features, but implementors should
	 * simply follow the rules to be safe.
	 * 
	 * @param value
	 *            the object to attempt parsing of.
	 * 
	 * @return <code>value</code> converted to the preferred storage of this
	 *         <code>AttributeType</code>. If no parsing was possible then
	 *         the same object is returned.
	 * 
	 * @throws IllegalArgumentException
	 *             if parsing is attempted and is unsuccessful.
	 */
	protected Object parse(Object value) throws IllegalArgumentException {
		return value;
	}

	/**
	 * Whether the tested object passes the validity constraints of this
	 * Attribute's AttributeType. At a minimum it should be of the correct class
	 * specified by {@link #getType()}, non-null if isNillable is
	 * <tt>false</tt>. If The object does not validate then an
	 * IllegalArgumentException reporting the error in validation should be
	 * thrown.
	 * 
	 * @param attribute
	 *            The object to be tested for validity.
	 * 
	 * @throws IllegalArgumentException
	 *             if the object does not validate.
	 */
	protected void validate(final Object attributeContent)
			throws IllegalArgumentException {
		if (attributeContent == null) {
			if (!TYPE.isNillable()) {
				throw new NullPointerException(TYPE.getName()
						+ " is not nillable");
			}
			return;
		}
		Class clazz = attributeContent.getClass();
		Class binding = TYPE.getBinding();
		if (binding != null && !binding.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(clazz.getName()
					+ " is not an acceptable class for " + TYPE.getName()
					+ " as it is not assignable from " + binding);
		}
		Attribute fake = new Attribute() {
			public AttributeType getType() {
				return TYPE;
			}

			public String getID() {
				return ID;
			}

			public Object get() {
				return attributeContent;
			}

			public void set(Object newValue) {
				throw new UnsupportedOperationException(
						"Modification is not supported");
			}
		};
		checkRestrictions(fake);
	}

	/**
	 * Checks this Attribute instance passes the restrictions imposed by the
	 * AttributeType
	 * 
	 * @throws IllegalStateException
	 *             if at least one Filter of the AttributeType restrictions does
	 *             not contains this Attribute instance.
	 */
	public static void checkRestrictions(Attribute att)
			throws IllegalStateException {
		if (!att.getType().getRestrictions().isEmpty()) {
			for (Filter f : att.getType().getRestrictions()) {
				if (!f.contains(att)) {
					throw new IllegalStateException(
							"Attribute instance does not complies with type restriction "
									+ f);
				}
			}
		}
	}
}
