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
package org.geotools.metadata.iso19115;

import java.util.HashSet;

/**
 * Acts as a typed Set while we wait for Java 5.0.
 * 
 * @author Jody Garnett, Refractions Research
 */
public class SetOf extends HashSet {
    Class type;
    public SetOf( Class type ){
        this.type = type;
    }
    /* (non-Javadoc)
     * @see java.util.HashSet#add(java.lang.Object)
     */
    public boolean add(Object obj) {
        if( obj != null && !type.isInstance( obj ) ){
            throw new IllegalArgumentException( "Cannot add "+obj.getClass().getName() + " to set of "+type.getName() );
        }
        return super.add( obj );
    }
}
