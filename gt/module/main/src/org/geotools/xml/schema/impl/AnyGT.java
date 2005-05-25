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
package org.geotools.xml.schema.impl;

import java.net.URI;

import org.geotools.xml.schema.Any;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class AnyGT extends Any {
    /**
    private String id = null;
    private int min = 1;
    private int max = 1;
    private URI ns = null;
    */
    private AnyGT() {
        this( null );
    }

    public AnyGT(URI namespace) {
        this( namespace, 1, 1);
    }

    public AnyGT(URI namespace, int min, int max) {
        super( namespace, null, min, max );
        /*
        ns = namespace;
        this.min = min;
        this.max = max;
        */
    }    
/*
    public String getId() {
        return id;
    }

    public int getMaxOccurs() {
        return max;
    }
    public int getMinOccurs() {
        return min;
    }

    public URI getNamespace() {
        return ns;
    }

    public int getGrouping() {
        return ElementGrouping.ANY;
    }
    public Element findChildElement(String name) {
        return null; // not implemented yet, use a utility function
    }*/
}
