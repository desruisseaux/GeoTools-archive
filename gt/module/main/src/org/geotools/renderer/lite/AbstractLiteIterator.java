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
package org.geotools.renderer.lite;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;


/**
 * Subclass that provides a convenient efficient currentSegment(float[] coords) implementation
 * that reuses always the same double array. This class and the associated subclasses are not
 * thread safe.
 *
 * @author Andrea Aime
 */
public abstract class AbstractLiteIterator implements PathIterator {
    protected double[] dcoords = new double[2];
    protected static final AffineTransform NO_TRANSFORM = new AffineTransform();

    /**
     * @see java.awt.geom.PathIterator#currentSegment(float[])
     */
    public int currentSegment(float[] coords) {
        int result = currentSegment(dcoords);
        coords[0] = (float) dcoords[0];
        coords[1] = (float) dcoords[1];

        return result;
    }
}
