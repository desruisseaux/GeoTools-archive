package org.opengis.feature.type;

import java.util.Collection;

import org.opengis.feature.schema.Schema;

public interface ComplexType extends AttributeType {
	/**
	 * Java class bound to this complex type.
	 * <p>
	 * Note this method will return null if the complex type does not
	 * bind to a Java object.
	 * </p>
	 * @return Java binding, or null if not applicable
	 */
	Class getBinding();

	/** Super is restricted to other ComplexType */
	ComplexType getSuper();
	
	/**
	 * Access to multiplicity and order of allowed content.
	 * <p>
	 * Follows JavaBeans naming convention indicating this is part of
	 * our data model.
	 * </p>
	 * @see types
	 */
	Schema getSchema();

	/**
	 * Describes allowable content, indicates containment not validation .
	 * <p>
	 * This method could be removed - the information is completly
	 * available through schema();
	 * </p>
	 * This information is gathered from a breadth first search
	 * of schema(). Collection is returned so that FlatFeatureType
	 * can return an List, where as usually a Set is returned.
	 * </p>
	 * <p>
	 * Note: a AttributeType may be returned by more then one AttributeDescriptor in a Schema,
	 * if it is allowed in more then one ChoiceSchema or Sequence. While this
	 * represents a form of multiplicity it does not indicate any difference
	 * in containment.
	 * </p>
	 * <p>
	 * Follows Collections naming conventions indicating this is a derrived
	 * quality and not part of our data model.
	 * </p>
	 * @see getSchema
	 */
	Collection<AttributeType> types();
	
	/**
	 * Works as a search through available types().
	 * <p>
	 * This method is useful when there is no namespace collision on names,
	 * as with simple content.
	 * </p>
	 * @param name
	 * @return The "first" type that with AttributeType.getName
	 */
	AttributeType type( String name );

}
