/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.feature;

import org.geotools.util.Utilities;


/**
 * Simple implementation of Name.
 * <p>
 * This class emulates QName, and is used as the implementation of both AttributeName and
 * TypeName (so when the API settles down we should have a quick fix.
 * <p>
 * Its is advantageous to us to be able to:
 * <ul>
 * <li>Have a API in agreement with QName - considering our target audience
 * <li>Strongly type AttributeName and TypeName separately
 * </ul>
 * The ISO interface move towards combining the AttributeName and Attribute classes,
 * and TypeName and Type classes, while we understand the attractiveness of this on a
 * UML diagram it is very helpful to keep these concepts separate when playing with
 * a strongly typed language like java.
 * </p>
 * <p>
 * It case it is not obvious this is a value object and equality is based on
 * namespace and name.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 */
public class NameImpl implements org.opengis.feature.type.Name {
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
    public NameImpl(String local) {
        this(null, local);
    }

    /**
     * Constructs an instance with the local part and namespace set.
     *
     * @param namespace The namespace or scope of the name.
     * @param local The local part of the name.
     *
     */
    public NameImpl(String namespace, String local) {
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

        //return new StringBuffer(namespace).append(':').append(local).toString();
        // you may expect a seperator? I am afriad not - different
        // domains have different customs for seperators; plesae
        // include the seperator (if you actually need one) as part
        // of your namespace.
        return new StringBuffer(namespace).append(local).toString();
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
        if(obj == this)
            return true;
        
        if (obj instanceof NameImpl) {
            NameImpl other = (NameImpl) obj;
            if(!Utilities.equals(this.namespace, other.namespace))
                return false;
            if(!Utilities.equals(this.local, other.local))
                return false;
                
            return true;
        } else if (obj instanceof org.opengis.feature.type.Name) {
            org.opengis.feature.type.Name other = (org.opengis.feature.type.Name) obj;

            return Utilities.equals(getURI(), other.getURI());
        }

        return false;
    }

    /** name or namespace:name */
    public String toString() {
        return getURI();
    }
}
