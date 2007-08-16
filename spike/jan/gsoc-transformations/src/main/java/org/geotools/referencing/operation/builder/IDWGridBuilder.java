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
package org.geotools.referencing.operation.builder;

import java.util.List;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.builder.algorithm.IDWInterpolation;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


/**
 * Implementation grid builder based on inverse distance weighted (IDW) interpolation.
 *
 * @see <A HREF="http://en.wikipedia.org/wiki/Inverse_distance_weighting">IDW at Wikipedia</A>
 *
 * @author jezekjan
 *
 */
public class IDWGridBuilder extends WarpGridBuilder {
    /**
     * Constructs IDWGridBuilder from set of parameters.
     *
     * @param vectors known shift vectors
     * @param dx width of gird cell
     * @param dy height of grid cells
     * @param env Envelope of generated grid
     * @throws TransformException
     */
    public IDWGridBuilder(List vectors, double dx, double dy, Envelope env)
        throws TransformException, NoSuchIdentifierException {
        super(vectors, dx, dy, env, IdentityTransform.create(2));
    }

    /**
     * Constructs IDWGridBuilder from set of parameters. The Warp Grid values are
     * calculated in transformed coordinate system.
     * @param vectors known shift vectors
     * @param dx width of gird cell
     * @param dy height of grid cells
     * @param envelope Envelope of generated grid
     * @param realToGrid Transformation from real to grid coordinates (when working with images)
     * @throws TransformException
     */
    public IDWGridBuilder(List vectors, double dx, double dy, Envelope envelope,
        MathTransform realToGrid) throws TransformException, NoSuchIdentifierException {
        super(vectors, dx, dy, envelope, realToGrid);
    }

    protected float[] computeWarpGrid(ParameterValueGroup WarpParams)
        throws TransformException {
        float[] warpPositions = (float[]) WarpParams.parameter("warpPositions").getValue();

        IDWInterpolation dxInterpolation = new IDWInterpolation(buildPositionsMap(0));
        IDWInterpolation dyInterpolation = new IDWInterpolation(buildPositionsMap(1));

        for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
            for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
                DirectPosition2D p = (new DirectPosition2D(WarpParams.parameter("xStart").intValue()
                        + (j * WarpParams.parameter("xStep").intValue()),
                        WarpParams.parameter("yStart").intValue()
                        + (i * WarpParams.parameter("yStep").intValue())));

                double x = -dxInterpolation.getValue(p)
                    + (j * WarpParams.parameter("xStep").intValue())
                    + WarpParams.parameter("xStart").intValue();
                double y = -dyInterpolation.getValue(p)
                    + (i * WarpParams.parameter("yStep").intValue())
                    + WarpParams.parameter("yStart").intValue();

                warpPositions[(i * ((1 + WarpParams.parameter("xNumCells").intValue()) * 2))
                + (2 * j)] = (float) x;

                warpPositions[(i * ((1 + WarpParams.parameter("xNumCells").intValue()) * 2))
                + (2 * j) + 1] = (float) y;
            }
        }

        return warpPositions;
    }
}
