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

import com.vividsolutions.xdo.xsi.ElementGrouping;

/**
 * <p>
 * This interface is intended to represent a Sequence in an XML Schema. This
 * shildren of this sequence are ElementGroupings which may involve Element
 * declarations, Choices, Groups ... or even another Sequence. We recommend
 * flattening child Sequences with the parent, creating a semantically
 * equivalent sequence in it's place.
 * </p>
 *
 * @author dzwiers www.refractions.net
 *
 * @see ElementGrouping
 */
public abstract class Sequence extends com.vividsolutions.xdo.xsi.Sequence {

    /**
     * Construct <code>Sequence</code>.
     *
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public Sequence( String arg0, int arg1, int arg2, com.vividsolutions.xdo.xsi.ElementGrouping[] arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }
    
    /**
     * 
     */
    public abstract com.vividsolutions.xdo.xsi.ElementGrouping[] getChildren();
    
    /**
     * <p>
     * The Schema ID for this sequence definition.
     * </p>
     *
     * @return
     */
    public abstract String getId();

    /**
     * @see org.geotools.xml.xsi.ElementGrouping#getMaxOccurs()
     */
    public abstract int getMaxOccurs();

    /**
     * @see org.geotools.xml.xsi.ElementGrouping#getMinOccurs()
     */
    public abstract int getMinOccurs();
}
