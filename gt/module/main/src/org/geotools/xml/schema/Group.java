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

import java.net.URI;


/**
 * <p>
 * This interface is intended to represent the Group construct within XML
 * Schemas.
 * </p>
 * 
 * <p>
 * In many situations it is recommended that groups be flatened out to their
 * child declaration, removing the additional layer of indirection. Although
 * this optimization is nice, it is imposible to complete this all the time,
 * as xml schemas may include publicly viewable Group definitions.
 * </p>
 *
 * @author dzwiers www.refractions.net
 */
public abstract class Group extends com.vividsolutions.xdo.xsi.Group {
    /**
     * <p>
     * Returns the Child Schema element (Choice or Sequence) declaring valid
     * element sequences for this group.
     * </p>
     *
     * @return
     */
    public com.vividsolutions.xdo.xsi.ElementGrouping getChild() {
        return super.getChild();
    }

    /**
     * <p>
     * The Group's declaration object id.
     * </p>
     *
     * @return
     */
    public String getId() {
        return super.getId();
    }

    /**
     * <p>
     * The maximum number of times this group may appear in the instance
     * document.
     * </p>
     *
     * @return
     */
    public int getMaxOccurs() {
        return super.getMaxOccurs();
    }

    /**
     * <p>
     * The minimum number of times this group may appear in the instance
     * document.
     * </p>
     *
     * @return
     */
    public int getMinOccurs() {
        return super.getMinOccurs();
    }

    /**
     * <p>
     * The group's name in the Schema document
     * </p>
     *
     * @return
     */
    public String getName() {
        return super.getName();
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public URI getNamespace() {
        return super.getNamespace();
    }
}
