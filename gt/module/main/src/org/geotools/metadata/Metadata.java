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
package org.geotools.metadata;

import java.util.List;


/**
 * The MetadataEntity is a set of metadata elements describing the same aspect of data
 *
 * May contain one or more metadata entities
 * Equivalent to a clas in UML terminology.
 *
 * The MetadataEntity interface is similar to that of the Feature Interface
 * The subinterfaces of MetadataEntity are used to specify which MetadataElements
 * are required, or possible.
 *
 * Any getXXX() method declared in a MetadataEntity
 * <i>subinterfaces</i> indicate the MetadataElements
 * that may be found as a elemtent of the particular Metadata Type.
 *
 * This contract is required because MetadataEntity does not have
 * type classes that describe the elements that can be expected
 * for a particular metadata.  Features have a FeatureType that
 * describe the FeatureAttributes.  In Metadata the getXXX() methods
 * declare the MetadataElement structure.
 *
 * @see Metadata interface as an example
 *
 * @author jeichar
 */
public interface Metadata {
    /**
     * Copy all the MetadataElements of this Metadata into the given list. If the
     * argument list is null, a new one will be created. Gets all MetadataElements
     * from this Entity, returned as a complex object list.
     *
     * @param attributes A list to copy elements into. May be null.
     *
     * @return The list passed in, or a new one if null.
     */
    public List getElements(List list);

    /**
     * Gets a collection of Metadata.Elements for this Metadata at the location specified by xPath.
     * Due to the complex nature of xpath, the return Object may be a single MetadataElement/Entity
     * or a java.util.Collection containing a mix of MetadataEntities and/or MetadataElements
     *
     * @param xPath XPath representation of element location.
     *
     * @return A copy of the requested element, null if the requested xpath
     *         is not found
     */
    Object getElement(String xPath);

    /**
     * Gets the value of the element that the parameter represents
     * 
     *  
     *
     * @param element A Metadata.Element that indicates the value the caller wishes to obtain
     * @return The value of the element the parameter represents
     * 		null if the current Metadata does not contain the Element
     */
    public Object getElement(Element element);

    /**
     * Returns the Metadata.Entity that describes the current Metadata object
     *
     * @return the Metadata.Entity that describes the current Metadata object
     */
    public Entity getEntity();

    /**
     *
     * Describes a the *type* of a Metadata.
     *
     * Similar in purpose as that of a Feature's FeatureType class.
     *
     * Allow for introspection of the Metadata.  The list of Metadata.Elements that a metadata
     * has can be obtained through the Entity of a metadata.
     *
     * @author jeichar
     *
     */
    public static interface Entity {
        /**
         * The XPath is used to identify Metadata.Elements in the Metadata data hierarchy
         * If the xpath has wild cards a List of Metadata Elements will be returned.
         * 
         * 
         * @param xpath an XPath statement that indicates 0 or more Metadata.Elements.
         * @return Null if no elements are found to match the xpath 
         * 		A Metadata.Element if exactly one is found to match the xpath
         * 		A List is many Metadata.Elements are found to match the xpath.
         */
        public Object getElement(String xpath);

        /**
         * Get a List of all the Elements this Entity contains
         * Only the elements contained by the current Entity are returned, in other words
         * this method is not recursive, elements in sub-enities are not returned.
         *
         * @return a List of all the Elements this Entity contains
         */
        public List getElements();
    }

    /**
     * An Element is a discreet unit of metadata.  It is analogous to a attribute in UML terminology
     * 
     * Metadata Elements are unique within a metadata entity
     * 
     * Used to inspect the type and structure of a metadata element 
     * 
     * @author jeichar
     *
     */
    public static interface Element {
        /**
         * Gets the type of this element.
         *
         * @return Type.
         */
        public Class getType();

        /**
         * Gets the name of this element.
         *
         * @return Name.
         */
        public String getName();

        /**
         * Returns whether nulls are allowed for this element.
         *
         * @return true if nulls are permitted, false otherwise.
         */
        public boolean isNillable();

        /**
         * Whether or not this element is complex in any way.  If it is
         * not nested then the code can just do the default processing, such
         * as printing the element directly, for example.  If it is nested then
         * that indicates there is more to be done, and the actual ElementType
         * should be determined and processed accordingly.
         *
         * @return <code>true</code> if Any
         *
         */
        public boolean isMetadataEntity();

        /**
         * If the current element is an entity then the entity object that describes the 
         * current element is returned.
         *
         * @return Null, if not a metadata entity (isMetadataEntity returns false)
         * 		The Metadata.Entity object describing the current element if current element is
         * 		an entity 
         */
        public Entity getEntity();
    }
}
