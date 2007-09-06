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
package org.geotools.xml.impl.jxpath;

import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.geotools.feature.Feature;


public class FeaturePropertyIterator implements NodeIterator {
    /**
     * The feature node pointer
     */
    FeaturePointer pointer;

    /**
     * The feature.
     */
    Feature feature;

    /**
     * current position
     */
    int position;

    public FeaturePropertyIterator(FeaturePointer pointer) {
        this.pointer = pointer;
        feature = (Feature) pointer.getImmediateNode();
        position = 1;
    }

    public int getPosition() {
        return position;
    }

    public boolean setPosition(int position) {
        this.position = position;

        return position <= feature.getNumberOfAttributes();
    }

    public NodePointer getNodePointer() {
        return new FeaturePropertyPointer(pointer, position - 1);
    }
}
