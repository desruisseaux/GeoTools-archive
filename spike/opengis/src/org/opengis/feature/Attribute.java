package org.opengis.feature;

import org.opengis.feature.type.Type;


public interface Attribute {

	/**
	 * Access name from Type, derrived property does not use Java Bean
	 * conventions
	 * 
	 * @return getType().getName().toString()
	 */
	String name();

	/**
	 * Access to the content of this attribtue.
	 * 
	 * @return Value of the type indicated by type()
	 */
	Object get();

	/**
	 * Set content to newZValue
	 * @param newValue
	 *            Must be of type indicated by type()
	 */
	void set(Object newValue);

	/**
	 * Indicate the Type of this content.
	 * 
	 * @return Type information descirbing allowable content
	 */
	public Type getType();
}