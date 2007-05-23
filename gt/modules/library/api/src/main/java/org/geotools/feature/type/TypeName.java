/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.feature.type;

import org.geotools.resources.Utilities;


/**
 * Simple implementation of TypeName.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class TypeName implements org.opengis.feature.type.TypeName {
    /** namespace / scope */
    protected String namespace;

    /** local part */
    protected String local;

    /**
     * Constructs an instance with the local part set. Namespace / scope is
     * set to null.
     *
     * @param local The local part of the name.
     */
    public TypeName(String local) {
        this(null, local);
    }

    /**
     * Constructs an instance with the local part and namespace set.
     *
     * @param namespace The namespace or scope of the name.
     * @param local The local part of the name.
     *
     */
    public TypeName(String namespace, String local) {
        this.namespace = namespace;
        this.local = local;
    }

    public boolean isGlobal() {
        return getNamespaceURI() == null;
    }

    public String getNamespaceURI() {
        return namespace;
    }

    public String getLocalPart() {
        return local;
    }

    public String getURI() {
        if ((namespace == null) && (local == null)) {
            return null;
        }

        if (namespace == null) {
            return local;
        }

        if (local == null) {
            return namespace;
        }

        return new StringBuffer(namespace).append(':').append(local).toString();
    }

    /**
     * value object with equality based on name and namespace.
     */
    public int hashCode() {
        String uri = getURI();

        return (uri != null) ? uri.hashCode() : 0;
    }

    /**
     * value object with equality based on name and namespace.
     */
    public boolean equals(Object obj) {
        if (obj instanceof TypeName) {
            TypeName other = (TypeName) obj;

            return Utilities.equals(getURI(), other.getURI());
        }

        return false;
    }

    /** name or namespace:name */
    public String toString() {
        return getURI();
    }
}
