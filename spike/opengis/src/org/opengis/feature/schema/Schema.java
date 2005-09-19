package org.opengis.feature.schema;


/**
 * Schema captures the order of types withing a ComplexType.
 * <p>
 * Order is defined by a tree structure of the following:
 * <ul>
 * <li>AttributeDescriptor - indicating a AttributeType with multiplicity information
 * <li>ChoiceSchema - indicating a choice between options
 * <lI>SequenceSchema - indicating a perscribed order is required
 * <li>AllSchema - indicating an unordered set
 * </ul>
 * @author Jody Garnett
 *
 */
public interface Schema {
	public int getMinOccurs();
	public int getMaxOccurs();
}
