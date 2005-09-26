package org.opengis.feature.schema;


/**
 * Descriptor captures the order of types withing a ComplexType.
 * <p>
 * Order is defined by a tree structure of the following:
 * <ul>
 * <li>AttributeDescriptor - indicating a AttributeType with multiplicity information
 * <li>ChoiceDescriptor - indicating a choice between options
 * <lI>OrderedSchema - indicating a perscribed order is required
 * <li>AllDescriptor - indicating an unordered set
 * </ul>
 * These form the substance of the GeoTools Feature Model, as a convience 
 * a further subclass of OrderedDescriptor is available for simple content.
 * </p>
 * 
 * @author Jody Garnett
 *
 */
public interface Descriptor {
	public int getMinOccurs();
	public int getMaxOccurs();
}
