/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.feature;

/**
 * DOCUMENT ME!
 *
 * @author dzwiers
 */
public interface SimpleFeature extends Feature {
    /**
     * Gets a reference to the schema for this feature. This method should
     * always return DefaultFeatureType Object.  This will be explicitly
     * posible in Java 1.5 (dz)
     *
     * @return A reference to this simple feature's schema.
     */
    FeatureType getFeatureType();

    /**
     * Sets all attributes for this feature, passed as a complex object array.
     * Note that this array must conform to the internal schema for this
     * feature, or it will throw an exception.  Checking this is, of course,
     * left to the feature to do internally.  Well behaved features should
     * always fully check the passed attributes against thier schema before
     * adding them.  Since this is a SimpleFeature, the number of attributes
     * will be exactly  the same as the number of attribute types. Attribute
     * values will be paired  with attribute types based on array indexes.
     *
     * @param attributes All feature attributes.
     *
     * @throws IllegalAttributeException Passed attributes do not match schema.
     */
    void setAttributes(Object[] attributes) throws IllegalAttributeException;

    /**
     * This is the same as the parent declaration, except that when the
     * instance  is not specified for the xPath, [0] will be added as there is
     * only ever one  Attribute value for an AttributeType
     *
     * @param xPath XPath representation of attribute location.
     *
     * @return A copy of the requested attribute, null if the requested xpath
     *         is not found, or NULL_ATTRIBUTE.
     *
     * @see Feature#getAttribute(String)
     */
    Object getAttribute(String xPath);

    /**
     * Gets an attribute by the given zero-based index. Unlike the parent
     * interface,  this index is guaranteed to match the index of
     * AttributeType in the FeatureType.
     *
     * @param index The requested index. Must be 0 &lt;= idx &lt;
     *        getNumberOfAttributes().
     *
     * @return A copy of the requested attribute, or NULL_ATTRIBUTE.
     */
    Object getAttribute(int index);

    /**
     * Sets an attribute by the given zero-based index. Unlike the parent
     * interface,  this index is guaranteed to match the index of
     * AttributeType in the FeatureType.
     *
     * @param position The requested index. Must be 0 &lt;= idx &lt;
     *        getNumberOfAttributes()
     * @param val An object representing the attribute being set
     *
     * @throws IllegalAttributeException if the passed in val does not validate
     *         against the AttributeType at that position.
     * @throws ArrayIndexOutOfBoundsException if an invalid position is given
     */
    void setAttribute(int position, Object val)
        throws IllegalAttributeException, ArrayIndexOutOfBoundsException;
}
