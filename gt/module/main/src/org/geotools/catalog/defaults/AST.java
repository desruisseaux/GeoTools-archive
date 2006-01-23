/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.catalog.defaults;

/**
 * Use the visitor pattern to traverse the AST
 *
 * @author David Zwiers, Refractions Research
 *
 * @since 0.6
 * @source $URL$
 */
public interface AST {
    public static final int AND = 1;
    public static final int OR = 2;
    public static final int NOT = 4;
    public static final int LITERAL = 0;

    public boolean accept(String datum);

    public int type();

    /**
     * may be null
     *
     * @return DOCUMENT ME!
     */
    public AST getLeft();

    /**
     * may be null
     *
     * @return DOCUMENT ME!
     */
    public AST getRight();
}
