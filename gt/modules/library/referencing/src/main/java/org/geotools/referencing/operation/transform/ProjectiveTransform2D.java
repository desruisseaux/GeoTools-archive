/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.transform;

import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;


/**
 * Projective transform in 2D case.
 *
 * @source $URL$
 * @version $Id$
 * @author Jan Jezek
 */
final class ProjectiveTransform2D extends ProjectiveTransform implements MathTransform2D {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -3101392684596817045L;

    /**
     * Creates projective transform from a matrix.
     */
    public ProjectiveTransform2D(final Matrix matrix) {
        super(matrix);
    }
}
