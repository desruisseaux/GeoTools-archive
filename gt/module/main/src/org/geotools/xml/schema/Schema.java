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
package org.geotools.xml.schema;

import java.net.URI;
import java.util.Arrays;

import org.geotools.factory.Factory;


/**
 * <p>
 * This Interface is intended to represent the public portion of an XML Schema.
 * By public portion, I mean the portion of the Schema which can be included
 * in an instance document, or imported into another Schema.
 * </p>
 * 
 * <p>
 * The distinction between the public portion of a XML Schema and the entire
 * XML Schema is or particular important when comparing, or printing two XML
 * Schemas. This interface does is intended to provide enough information to
 * re-create the original Schema (note the lack or annotations as an example).
 * This interface is however intended to provide functional semantic
 * equivalence. By this is mean that two XML Schemas represented using this
 * interface should have the same SET of declarations. There is no guarantee
 * that the Schema represented matches the original document with respect to
 * orderwithin the sets, except where order is explicitly defined (Sequence,
 * Choice).
 * </p>
 * 
 * <p>
 * This method must be inplemented within extensions:  public static Schema
 * getInstance();. It will be used by the Schema factory to  load the required
 * extensions into memory.
 * </p>
 *
 * @author dzwiers www.refractions.net
 */
public abstract class Schema extends com.vividsolutions.xdo.xsi.Schema implements Factory  {
    /**
     * Used to denote byte masks representing either XML block attributes or
     * XML final attributes.
     */
    public static final int NONE = 0;

    /**
     * Used to denote byte masks representing either XML block attributes or
     * XML final attributes.
     */
    public static final int EXTENSION = 1;

    /**
     * Used to denote byte masks representing either XML block attributes or
     * XML final attributes.
     */
    public static final int RESTRICTION = 2;

    /**
     * Used to denote byte masks representing either XML block attributes or
     * XML final attributes.
     */
    public static final int ALL = 4;

    /**
     * <p>
     * This method is intended to provide a list of public AttributeGroups
     * defined by this Schema. The definition of 'public AttributeGroups'
     * should be interpreted as the set of AttributeGroups availiable when
     * creating an instance document, extending the schema, or importing the
     * schema.
     * </p>
     *
     * @return AttributeGroup[]
     *
     * @see AttributeGroup
     */
    public com.vividsolutions.xdo.xsi.AttributeGroup[] getAttributeGroups() {
        return super.getAttributeGroups();
    }

    /**
     * <p>
     * This method is intended to provide a list of public Attributes defined
     * by this Schema. The definition of 'public Attributes' should be
     * interpreted as the set of Attributes availiable when creating an
     * instance document, extending the schema, or importing the schema.
     * </p>
     *
     * @return
     *
     * @see Attribute
     */
    public com.vividsolutions.xdo.xsi.Attribute[] getAttributes() {
        return super.getAttributes();
    }

    /**
     * <p>
     * This method returns the default block value associated with this schema
     * as a mask. The keys for the mask are represented as constants at the
     * head of this file. As defined in the XML Schema specification, element
     * and type blocks should only be extended to include this block if one is
     * not specified.
     * </p>
     *
     * @return Block Mask
     */
    public int getBlockDefault() {
        return super.getBlockDefault();
    }

    /**
     * <p>
     * This method is intended to provide a list of public ComplexTypes defined
     * by this Schema. The definition of 'public ComplexTypes' should be
     * interpreted as the set of ComplexTypes availiable when creating an
     * instance document, extending the schema, or importing the schema.
     * </p>
     *
     * @return
     *
     * @see ComplexType
     */
    public com.vividsolutions.xdo.xsi.ComplexType[] getComplexTypes() {
        return super.getComplexTypes();
    }

    /**
     * <p>
     * This method is intended to provide a list of public Elements defined by
     * this Schema. The definition of 'public Elements' should be interpreted
     * as the set of Elements availiable when creating an instance document,
     * extending the schema, or importing the schema.
     * </p>
     *
     * @return
     *
     * @see Element
     */
    public com.vividsolutions.xdo.xsi.Element[] getElements() {
        return super.getElements();
    }

    /**
     * <p>
     * This method returns the default final value associated with this schema
     * as a mask. The keys for the mask are represented as constants at the
     * head of this file. As defined in the XML Schema specification, element
     * and type final values should only be extended to include this final
     * value if one is not specified.
     * </p>
     *
     * @return Final Mask
     */
    public int getFinalDefault() {
        return super.getFinalDefault();
    }

    /**
     * <p>
     * This method is intended to provide a list of public Groups defined by
     * this Schema. The definition of 'public Groups' should be interpreted as
     * the set of Groups availiable when creating an instance document,
     * extending the schema, or importing the schema.
     * </p>
     *
     * @return
     *
     * @see Group
     */
    public com.vividsolutions.xdo.xsi.Group[] getGroups() {
        return super.getGroups();
    }

    /**
     * <p>
     * This method is intended to provide the ID of this Schema.
     * </p>
     *
     * @return
     */
    public String getId() {
        return super.getId();
    }

    /**
     * <p>
     * This method is intended to provide a list of public Imports defined by
     * this Schema. The definition of 'public Imports' should be interpreted
     * as the set of Imports availiable when creating an instance document,
     * extending the schema, or importing the schema.
     * </p>
     *
     * @return
     *
     * @see Schema
     */
    public com.vividsolutions.xdo.xsi.Import[] getImports() {
        return super.getImports();
    }

    /**
     * <p>
     * Gets the recommended prefix for this schema.
     * </p>
     *
     * @return
     */
    public String getPrefix() {
        return super.getPrefix();
    }

    /**
     * <p>
     * This method is intended to provide a list of public SimpleTypes defined
     * by this Schema. The definition of 'public SimpleTypes' should be
     * interpreted as the set of SimpleTypes availiable when creating an
     * instance document, extending the schema, or importing the schema.
     * </p>
     *
     * @return
     *
     * @see SimpleType
     */
    public com.vividsolutions.xdo.xsi.SimpleType[] getSimpleTypes() {
        return super.getSimpleTypes();
    }

    /**
     * <p>
     * This returns the intended use name of the Schema (kinda like an ID, for
     * a better definition see the XML Schema Specification).
     * </p>
     *
     * @return
     */
    public URI getTargetNamespace() {
        return super.getTargetNamespace();
    }

    // may be different than targNS
    public URI getURI() {
        return super.getUris()[0];
    }

    /**
     * <p>
     * This returns the Schema version ...
     * </p>
     *
     * @return
     */

    //TODO Use the Version in the merge + parsing portion for comparisons
    public String getVersion() {
        return super.getVersion();
    }

    /**
     * <p>
     * This looks to see if the URI passed in is represented by this Schema.
     * Often this method uses some heuritics on the list of included URIs.
     * This allows one Schema to represent one targetNamespace, but be
     * potentially represented in more than one file.
     * </p>
     * 
     * <p>
     * Used to determine if the uri should provided should be included in an
     * instance document.
     * </p>
     *
     * @param uri
     *
     * @return
     *
     * @see getUris()
     */
    public boolean includesURI(URI uri) {
        for (int i = 0; i < super.getUris().length; i++) {
            URI uri2 = super.getUris()[i];
            if (uri2.equals(uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Returns true when the Default Attribute Form is qualified, false
     * otherwise.
     * </p>
     *
     * @return
     */
    public boolean isAttributeFormDefault() {
        return super.isAttributeFormDefault();
    }

    /**
     * <p>
     * Returns true when the Default Element Form is qualified, false
     * otherwise.
     * </p>
     *
     * @return
     */
    public boolean isElementFormDefault() {
        return super.isElementFormDefault();
    }
}
