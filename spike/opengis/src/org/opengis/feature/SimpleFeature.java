package org.opengis.feature;

import javax.xml.namespace.QName;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.SimpleFeatureType;

/**
 * Feature interface customized for Simple content.
 * <p>
 * 
 * </p>
 * @author jgarnett
 *
 */
public interface SimpleFeature extends Feature {

	/**
	 * Restrictued to SimpleFeatureType
	 * <p>
	 * This restriction enabled client code to confidently
	 * assume that each attribute occurs in the perscribed order
	 * and that no super types are used.
	 * </p>
	 */
	SimpleFeatureType getType();

	/**
	 * Retrive value by attribute name.
	 * @param name
	 * @return Attribute Value associated with name
	 */
	Object get(String name);
	
	/**
	 * Retrive value by attribute name.
	 * @param name
	 * @return Attribute Value associated with name
	 */
	Object get(QName qname);
	
	/**
	 * Access attribute by "index" indicated by SimpleFeatureType.
	 * 
	 * @param index
	 * @return
	 */
	Object get( int index );
	
	/**
	 * Modify attribute at the "index" indicated by SimpleFeatureType.
	 * 
	 * @param index
	 * @param value
	 */
	void set( int index, Object value);
	
	/**
	 * Number of attributes in SimpleFeatureType.
	 * 
	 * @return number of available attribtues
	 */
	int getNumberOfAttributes();
}
