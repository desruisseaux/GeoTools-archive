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

import java.util.ArrayList;

/**
 * Acts as a typed List while we wait for Java 5.0.
 * 
 * @author Jody Garnett, Refractions Research
 */
public class ListOf extends ArrayList {
    Class type;
    public ListOf( Class type ){
        this.type = type;
    }
    /* Ensure that contents are limited to instances of type */
    public boolean add(Object obj) {
        if( obj != null && !type.isInstance( obj ) ){
            throw new IllegalArgumentException( "Cannot add "+obj.getClass().getName() + " to set of "+type.getName() );
        }
        return super.add( obj );
    }
}
