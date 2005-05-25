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
 * This represents an abstract collection of xml element definitions within a
 * Schema.
 * </p>
 * 
 * <p>
 * To avoid multiple type checks, a group mask was include, as described below.
 * </p>
 *
 * @author dzwiers www.refractions.net
 */
public abstract class ElementGrouping extends com.vividsolutions.xdo.xsi.ElementGrouping {
    
    protected ElementGrouping( int arg0 ) {
        super(arg0);
    }
    
    protected ElementGrouping( int arg0, String arg1, int arg2, int arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }

    /**
     * ElementGrouping mask to determine the type of ElementGrouping
     * represented. This is intended to  reduce the use of the instanceof
     * operand,  increasing performance.
     */
    public static final int ELEMENT = com.vividsolutions.xdo.xsi.ElementGrouping.ELEMENT;

    /**
     * ElementGrouping mask to determine the type of ElementGrouping
     * represented. This is intended to  reduce the use of the instanceof
     * operand,  increasing performance.
     */
    public static final int GROUP = com.vividsolutions.xdo.xsi.ElementGrouping.GROUP;

    /**
     * ElementGrouping mask to determine the type of ElementGrouping
     * represented. This is intended to  reduce the use of the instanceof
     * operand,  increasing performance.
     */
    public static final int ANY = com.vividsolutions.xdo.xsi.ElementGrouping.ANY;

    /**
     * ElementGrouping mask to determine the type of ElementGrouping
     * represented. This is intended to  reduce the use of the instanceof
     * operand,  increasing performance.
     */
    public static final int SEQUENCE = com.vividsolutions.xdo.xsi.ElementGrouping.SEQUENCE;

    /**
     * ElementGrouping mask to determine the type of ElementGrouping
     * represented. This is intended to  reduce the use of the instanceof
     * operand,  increasing performance.
     */
    public static final int CHOICE = com.vividsolutions.xdo.xsi.ElementGrouping.CHOICE;

    /**
     * ElementGrouping mask to determine the type of ElementGrouping
     * represented. This is intended to  reduce the use of the instanceof
     * operand,  increasing performance.
     */
    public static final int ALL = com.vividsolutions.xdo.xsi.ElementGrouping.ALL;
    
    public static final int UNBOUNDED = com.vividsolutions.xdo.xsi.ElementGrouping.UNBOUNDED;

    public static final int UNDEFINED = com.vividsolutions.xdo.xsi.ElementGrouping.UNDEFINED;
    

    /**
     * <p>
     * Convinience method which will search for the specified element within
     * it's children. This is typically implemented recursively, and as such
     * may be expensive to execute (so don't call me too much if you want to
     * be fast).
     * </p>
     *
     * @param name The Element LocalName (namespace and prefix should not be
     *        included)
     *
     * @return Element or null if not found.
     */
    public abstract com.vividsolutions.xdo.xsi.Element findChildElement(String name);

}
