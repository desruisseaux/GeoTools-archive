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
public abstract class Facet extends com.vividsolutions.xdo.xsi.Facet {
    public Facet( int arg0, String arg1 ) {
        super(arg0, arg1);
    }

    /** DOCUMENT ME! */
    public static int ENUMERATION = com.vividsolutions.xdo.xsi.Facet.ENUMERATION;

    /** DOCUMENT ME! */
    public static int FRACTIONDIGITS = com.vividsolutions.xdo.xsi.Facet.FRACTIONDIGITS;

    /** DOCUMENT ME! */
    public static int LENGTH = com.vividsolutions.xdo.xsi.Facet.LENGTH;

    /** DOCUMENT ME! */
    public static int MAXEXCLUSIVE = com.vividsolutions.xdo.xsi.Facet.MAXEXCLUSIVE;

    /** DOCUMENT ME! */
    public static int MAXINCLUSIVE = com.vividsolutions.xdo.xsi.Facet.MAXINCLUSIVE;

    /** DOCUMENT ME! */
    public static int MAXLENGTH = com.vividsolutions.xdo.xsi.Facet.MAXLENGTH;

    /** DOCUMENT ME! */
    public static int MINEXCLUSIVE = com.vividsolutions.xdo.xsi.Facet.MINEXCLUSIVE;

    /** DOCUMENT ME! */
    public static int MININCLUSIVE = com.vividsolutions.xdo.xsi.Facet.MININCLUSIVE;

    /** DOCUMENT ME! */
    public static int MINLENGTH = com.vividsolutions.xdo.xsi.Facet.MINLENGTH;

    /** DOCUMENT ME! */
    public static int PATTERN = com.vividsolutions.xdo.xsi.Facet.PATTERN;

    /** DOCUMENT ME! */
    public static int TOTALDIGITS = com.vividsolutions.xdo.xsi.Facet.TOTALDIGITS;

    /** DOCUMENT ME! */
    public static int WHITESPACE = com.vividsolutions.xdo.xsi.Facet.WHITESPACE;

    /**
     * The Facet Type -- selected from one of the above constant values
     *
     * @return
     */
    public int getFacetType() {
        return super.getType();
    }

    /**
     * The facet's constraint
     *
     * @return
     */
    public String getValue() {
        return super.getValue();
    }
}
