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

public SimpleFeatureType getType();

/**
 * Retrive value by attribute name.
 * @param name
 * @return Attribute Value associated with name
 */
public Object get(String name);

/**
 * Retrive value by attribute name.
 * @param name
 * @return Attribute Value associated with name
 */
public Object get(QName qname);
}
