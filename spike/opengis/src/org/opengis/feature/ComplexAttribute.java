package org.opengis.feature;

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;

/**
 * 
 * @author Jody Garnett
 * @author Gabriel Roldan
 */
public interface ComplexAttribute extends Attribute {
	/**
	 * Access the type of this construct.
	 */
	ComplexType<?> getType();

	/**
	 * Access to contents of this Feature.
	 * <p>
	 * This represents the contents of this ComplexAttribute value, the methods
	 * types() and values() return "views" into this data structure in a manner
	 * similar to Maps.keySet() and values().
	 * </p>
	 * <p>
	 * For a complex type, overrides {@linkplain Attribute#get()} to denote the
	 * prescribed content type of a complex is a list of Attribute instances.
	 * </p>
	 * 
	 * @see types()
	 * @see values()
	 */
	List<Attribute> get();

	/*
	 * * <p> Java Bean property conventions are used to indicate that this is
	 * part of our data model. </p> 
	 * List<Attribute> getAttributes();
	 */

	/**
	 * Sets the complete contents of this Attribute, that must be valid against
	 * the type's schema descriptor.
	 * <p>
	 * Note this is the only way of modifying the internal structure of a
	 * complex type.
	 * </p>
	 * 
	 * @param attribute
	 * @throws IllegalArgumentException
	 */
	void set(List<Attribute> newValue);

	/**
	 * List view of attribtue types, in a manner similar Map.keys().
	 * <p>
	 * The content available through types() an values() are considered a view
	 * of attribtues(). Order is maintained, and removing content will result in
	 * a modification to all three lists. in a manner simialr to Map.keysSet()
	 * and Map.values().
	 * <p>
	 * Collections naming conventions are used to indicate this is a view into
	 * our data model.
	 */
	List<AttributeType> types();

	/**
	 * Value view of attribtue types, in a manner similar Map.values().
	 * <p>
	 * The content avalable through types() an values() are considered a view of
	 * attribtues(). Order is maintained, and removing content will result in a
	 * modification to all three lists. in a manner simialr to Map.keysSet() and
	 * Map.values().
	 * <p>
	 * Collections naming conventions are used to indicate this is a view into
	 * our data model.
	 */
	List<Object> values();

	/**
	 * Inserts the specified Attribute at the specified position in this
	 * atttribute's contents list. Shifts the element currently at that position
	 * (if any) and any subsequent elements to the right (adds one to their
	 * indices).
	 * 
	 * @param index
	 *            index at which the specified element is to be inserted.
	 * @param value
	 *            Attribute to be inserted.
	 * 
	 * @throws NullPointerException
	 *             if the specified value is null.
	 * @throws IllegalArgumentException
	 *             if some aspect of the specified element prevents it from
	 *             being added to this list.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index &lt; 0 || index &gt;
	 *             size()).
	 */
	public void add(int index, Attribute value);

	/**
	 * Removes the element at the specified position in this attribute's
	 * contents list. Shifts any subsequent attributes to the left (subtracts
	 * one from their indices), and revalidates the contents against the schema.
	 * <p>
	 * If the resulting content structure does not validates against the schema,
	 * an IllegalArgumentException is thrown and this Attribute's contents are
	 * not modified.
	 * </p>
	 * 
	 * @param index
	 *            the index of the element to removed.
	 * @return the element previously at the specified position.
	 * 
	 * @throws UnsupportedOperationException
	 *             if the <tt>remove</tt> method is not supported by this
	 *             list.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index &lt; 0 || index &gt;=
	 *             size()).
	 */
	public Attribute remove(int index);
	
	/**
	 * Access to value associated with name.
	 * <p>
	 * This method acts as a precanned search of getAttribtues() based on
	 * AttributeType, where type is determined by getType().type( name )
	 * <ul>
	 * <li>AttributeType by Descriptor 1:1 - it will return an Object of the
	 * bound java class indicated by AttributeType
	 * <li>AttributeType by Descriptor 0:* - it will return a possibly empty
	 * List of the bound java class indicated by AttributeType
	 * </p>
	 * <p>
	 * This method is not considered useful for accessing content with
	 * multiplicity
	 * 
	 * @param name
	 * @return Object, or List based on type
	 * 
	 * Object get( String name );
	 */
	/**
	 * Access to value associated with type.
	 * 
	 * @param type
	 * @return Object, or List based on schema referencing AttributeType
	 */
	Object get(AttributeType type);
}