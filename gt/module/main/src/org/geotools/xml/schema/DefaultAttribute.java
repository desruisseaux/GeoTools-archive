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

/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class DefaultAttribute implements Attribute {
    private String defualT;
    private String fixed;
    private String id;
    private String name;
    private URI namespace;
    private int use;
    private SimpleType type;
    private boolean form;

    private DefaultAttribute() {
    }

    /**
     * Creates a new DefaultAttribute object.
     *
     * @param id DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param namespace DOCUMENT ME!
     * @param type DOCUMENT ME!
     * @param use DOCUMENT ME!
     * @param defaulT DOCUMENT ME!
     * @param fixed DOCUMENT ME!
     * @param form DOCUMENT ME!
     */
    public DefaultAttribute(String id, String name, URI namespace,
        SimpleType type, int use, String defaulT, String fixed, boolean form) {
        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.type = type;
        this.use = use;
        this.defualT = defaulT;
        this.fixed = fixed;
        this.form = form;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getDefault()
     */
    public String getDefault() {
        return defualT;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getFixed()
     */
    public String getFixed() {
        return fixed;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#isForm()
     */
    public boolean isForm() {
        return form;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getId()
     */
    public String getId() {
        return id;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getUse()
     */
    public int getUse() {
        return use;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getSimpleType()
     */
    public SimpleType getSimpleType() {
        return type;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getNamespace()
     */
    public URI getNamespace() {
        return namespace;
    }
}
