package org.opengis.feature.type;

import javax.xml.namespace.QName;

import org.opengis.feature.schema.SimpleDescriptor;

/**
 * Defines a imple feature model of attribute in a perscribed order.
 * <p>
 * This interface also defines several helper methods that only
 * make sense given the above constratins.
 * </p>
 *  
 * @author jgarnett
 */
public interface SimpleFeatureType extends FeatureType {
	
	/**
	 * Must be null for truely simple content.
	 */
	public SimpleFeatureType getSuper();
	
	/**
	 * Indicates a director ordering of AttributeDescriptors.
	 */
	public SimpleDescriptor getDescriptor();

	/**
	 * Retrive attributeType by qualified name
	 */
	AttributeType get( QName name );
	
	/**
	 * Retrive attributeType by name.
	 * <p>
	 * If you do run into a conflict (the same local part used in two 
	 * namespace) please use a complete Qualified name to disambiguate.
	 * </p>
	 */
	AttributeType get( String name );
	
}
