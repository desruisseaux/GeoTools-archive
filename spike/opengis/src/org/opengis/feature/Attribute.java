package org.opengis.feature;


public interface Attribute {

	/**
	 * Access name from Type, derrived property does not use Java Bean
	 * conventions
	 * 
	 * @return getType().getName().toString()
	 */
	String name();

	/** @return Value of the type indicated by type() */
	Object get();

	/**
	 * @param newValue
	 *            Must be of type indicated by type()
	 */
	void set(Object newValue);

	/*
	 * @return         Returns the type1.
	 * @uml.property   name="type" default="new org.opengis.feature.type.Type()"
	 * @uml.associationEnd   multiplicity="(1 1)" inverse="attribute:org.opengis.feature.type.Type"
	 */
	//public Type getType();
}