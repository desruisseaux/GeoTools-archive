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
package org.geotools.geometry.jts;

import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.CoordinateSequence;


/**
 * Interface that should be implemented by classes able to apply the provided 
 * transformation to a coordinate sequence.
 * Use with care for the moment, since it depends on deprecated objects.
 *
 * @author Andrea Aime
 */
public interface CoordinateSequenceTransformer {
    public CoordinateSequence transform(CoordinateSequence cs,
        MathTransform2D transform) throws TransformException;
}
