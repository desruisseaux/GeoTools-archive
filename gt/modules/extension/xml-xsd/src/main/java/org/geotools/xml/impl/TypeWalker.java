/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.xml.impl;

import org.eclipse.xsd.XSDTypeDefinition;


public class TypeWalker {
    /** bottom type in hierachy **/
    XSDTypeDefinition base;

    public TypeWalker(XSDTypeDefinition base) {
        this.base = base;
    }

    public void walk(Visitor visitor) {
        XSDTypeDefinition type = base;

        while (type != null) {
            //do the visit, if visitor returns false, break out
            if (!visitor.visit(type)) {
                break;
            }

            //get the next type
            if (type.equals(type.getBaseType())) {
                break;
            }

            type = type.getBaseType();
        }
    }

    public static interface Visitor {
        /**
         * Supplies the current type to the visitor.
         *
         * @param type The current type.
         *
         * @return True to signal that the walk should continue, false to
         * signal the walk should stop.
         */
        boolean visit(XSDTypeDefinition type);
    }
}
