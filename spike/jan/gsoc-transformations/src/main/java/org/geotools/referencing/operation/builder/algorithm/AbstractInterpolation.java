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
package org.geotools.referencing.operation.builder.algorithm;

import java.util.HashMap;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.geotools.geometry.DirectPosition2D;


public abstract class AbstractInterpolation {
    private final HashMap positions;
    private final double dx;
    private final double dy;
    private final Envelope env;
    private final int xNumCells;
    private final int yNumCells;
    private float[] gridValues;
    private float[][] grid2D;

    /**
     *
     * @param positions keys - point (DirectPosition), values - point values
     */
    public AbstractInterpolation(HashMap positions) {
        this.positions = positions;
        this.dx = 0;
        this.dy = 0;
        this.env = null;

        this.xNumCells = 0;
        this.yNumCells = 0;
    }

    /**
     * Abstract class for interpolation algorithms
     * @param positions keys - point (DirectPosition), values - point values
     * @param dx
     * @param dy
     * @param envelope
     */
    public AbstractInterpolation(HashMap positions, double dx, double dy, Envelope env) {
        this.positions = positions;
        this.dx = dx;
        this.dy = dy;
        this.env = env;

        this.xNumCells = (int) Math.floor(env.getLength(0) / dx);
        this.yNumCells = (int) Math.floor(env.getLength(1) / dy);

        //gridValues = new float[xNumCells*yNumCells];
    }

    public AbstractInterpolation(HashMap positions, int xNumOfCells, int yNumOfCells, Envelope env) {
        this.positions = positions;
        this.xNumCells = xNumOfCells;
        this.yNumCells = yNumOfCells;
        this.env = env;

        dx = env.getLength(0) / xNumOfCells;
        dy = env.getLength(1) / yNumOfCells;

        //gridValues = new float[xNumCells*yNumCells];
    }

    /**
     * Returns interpolated value in position of specified point
     * @return interpolated value
     */
    abstract protected float getGridValue(DirectPosition p);

    /**
     * Returns array of float of interpolated grid values.
     * The values are in row order. The dimension
     * id number of columns * number of rows.
     * @return Values of grid coordinates
     */
    private float[] buildGrid() {
        gridValues = new float[(xNumCells + 1) * (yNumCells + 1)];

        for (int i = 0; i <= yNumCells; i++) {
            for (int j = 0; j <= xNumCells; j++) {
                gridValues[(i * (1 + xNumCells)) + j] = getGridValue(new DirectPosition2D(env.getLowerCorner()
                                                                                             .getOrdinate(0)
                            + (j * dx), env.getLowerCorner().getOrdinate(1) + (i * dy)));
            }
        }

        return gridValues;
    }

    /**
     *
     * @return
     */
    public float[] getGrid() {
        if (gridValues == null) {
            gridValues = buildGrid();
        }

        return gridValues;
    }

    /**
     *
     * @return
     * @throws TransformException
     */
    public float[][] get2DGrid() {
        if ((grid2D == null) || (grid2D.length == 0)) {
            final float[] warpPositions = getGrid();

            grid2D = new float[yNumCells + 1][xNumCells + 1];

            for (int i = 0; i <= yNumCells; i++) {
                for (int j = 0; j <= xNumCells; j++) {
                    grid2D[i][j] = getGrid()[(int) ((i * (xNumCells + 1)) + (j))];
                }
            }
        }

        return grid2D;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public Envelope getEnv() {
        return env;
    }

    public int getXNumCells() {
        return xNumCells;
    }

    public int getYNumCells() {
        return yNumCells;
    }

    public HashMap getPositions() {
        return positions;
    }
}
