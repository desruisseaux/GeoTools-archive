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

/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class DefaultAttributeGroup implements AttributeGroup {
    private String anyAttributeNamespace;
    private Attribute[] attributes;
    private String id;
    private String name;
    private String namespace;

    private DefaultAttributeGroup() {
    }

    /**
     * Creates a new DefaultAttributeGroup object.
     *
     * @param id DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param namespace DOCUMENT ME!
     * @param attributes DOCUMENT ME!
     * @param anyAttributeNamespace DOCUMENT ME!
     */
    public DefaultAttributeGroup(String id, String name, String namespace,
        Attribute[] attributes, String anyAttributeNamespace) {
        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.attributes = attributes;
        this.anyAttributeNamespace = anyAttributeNamespace;
    }

    /**
     * @see org.geotools.xml.xsi.AttributeGroup#getAnyAttributeNameSpace()
     */
    public String getAnyAttributeNameSpace() {
        return anyAttributeNamespace;
    }

    /**
     * @see org.geotools.xml.xsi.AttributeGroup#getAttributes()
     */
    public Attribute[] getAttributes() {
        return attributes;
    }

    /**
     * @see org.geotools.xml.xsi.AttributeGroup#getId()
     */
    public String getId() {
        return id;
    }

    /**
     * @see org.geotools.xml.xsi.AttributeGroup#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.geotools.xml.xsi.AttributeGroup#getNamespace()
     */
    public String getNamespace() {
        return namespace;
    }
}
