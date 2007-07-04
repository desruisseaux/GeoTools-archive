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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.MismatchedSizeException;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.transform.WarpGridTransform2D;
import org.geotools.referencing.operation.transform.WarpTransform2D;


/**
 *
 * @author jezekjan
 *
 */
public class WarpGridBuilder extends MathTransformBuilder {
    /**
     * Grid width
     */
    private int width;

    /**
     * Grid height
     */
    private int height;

    /**
     * Envelope for generated Grid
     */
    Envelope envelope;

    /**
     * List of Mapped Positions in ggrid coordinates
     */
    List /*<MappedPosition>*/ localpositions = new ArrayList();

    /**
     * GridValues like maxx maxt dx dy etc..
     */
    GridParamValues globalValues;

    /**
     * RealToGrid Math Transform
     */
    MathTransform realToGrid;

    /**
     * Grid of x shifts
     */
    private float[][] dxgrid;

    /**
     * Grid of y shifts
     */
    private float[][] dygrid;
    private float[] warpPositions;

    /**
     * Construts Builder from
     * @param vectors
     * @param dx The horizontal spacing between grid cells.
     * @param dy The vertical spacing between grid cells.
     * @param Envelope Envelope of generated grid.
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws MismatchedReferenceSystemException
     */
    public WarpGridBuilder(List vectors, double dx, double dy, Envelope envelope,
        MathTransform realToGrid)
        throws MismatchedSizeException, MismatchedDimensionException,
            MismatchedReferenceSystemException, TransformException {
        this.realToGrid = realToGrid;

        globalValues = new GridParamValues(envelope, realToGrid, dx, dy);

        super.setMappedPositions(transformMPToGrid(vectors));

        warpPositions = (float[]) globalValues.WarpGridParameters.parameter("warpPositions")
                                                                 .getValue();

        this.envelope = envelope;

        this.computeWarpGrid();
    }

    /**
     * Transforms MappedPostions to grid system
     *
     */
    private List transformMPToGrid(List MappedPositions) {
        for (Iterator i = MappedPositions.iterator(); i.hasNext();) {
            MappedPosition mp = (MappedPosition) i.next();

            try {
                DirectPosition2D gridSource = new DirectPosition2D();
                DirectPosition2D gridTarget = new DirectPosition2D();
                realToGrid.transform(mp.getSource(), gridSource);
                realToGrid.transform(mp.getTarget(), gridTarget);
                localpositions.add(new MappedPosition(gridSource, gridTarget));
            } catch (MismatchedDimensionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TransformException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return localpositions;
    }

    private void ensureVectorsInsideEnvelope() {
        /* @TODO - ensure that source MappedPositions are inside the  envelope*/
    }

    /*
     *
     * (non-Javadoc)
     * @see org.geotools.referencing.operation.builder.MathTransformBuilder#computeMathTransform()
     */
    protected MathTransform computeMathTransform() throws FactoryException {
        return (WarpTransform2D) (new WarpGridTransform2D.Provider()).createMathTransform(globalValues
            .getWarpGridParameters());
    }

    /*
     * (non-Javadoc)
     * @see org.geotools.referencing.operation.builder.MathTransformBuilder#getMinimumPointCount()
     */
    public int getMinimumPointCount() {
        return 1;
    }

    /**
     * Computes GridWarp Positions using IDW interpolatio.
     *
     */
    private void computeWarpGrid() {
        ParameterValueGroup WarpParams = globalValues.WarpGridParameters;

        for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
            for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
                Point2D shiftVector = calculateShift(new DirectPosition2D(WarpParams.parameter(
                                "xStart").intValue()
                            + (j * WarpParams.parameter("xStep").intValue()),
                            WarpParams.parameter("yStart").intValue()
                            + (i * WarpParams.parameter("yStep").intValue())));

                double x = shiftVector.getX() + (j * WarpParams.parameter("xStep").intValue())
                    + WarpParams.parameter("xStart").intValue();
                double y = shiftVector.getY() + (i * WarpParams.parameter("yStep").intValue())
                    + WarpParams.parameter("yStart").intValue();

                warpPositions[(i * ((1 + WarpParams.parameter("xNumCells").intValue()) * 2))
                + (2 * j)] = (float) x;

                warpPositions[(i * ((1 + WarpParams.parameter("xNumCells").intValue()) * 2))
                + (2 * j) + 1] = (float) y;
            }
        }

        globalValues.setGridWarpPostions(warpPositions);
    }

    public float[][] getDxGrid() {
        return dxgrid;
    }

    public float[][] getDyGrid() {
        return dygrid;
    }

    /**
     * Calculates the real point shift from the iregular pairs of MappedPositions using
     * Inverse distance weighting interpolation. The distance is cartesian.
     * @param p position where we requaired the shift to be calculated
     * @return
     */
    private Point2D calculateShift(Point2D p) {
        double maxdist = (globalValues.WarpGridParameters.parameter("xStep").intValue() * globalValues.WarpGridParameters.parameter(
                "xNumCells").intValue())
            + (globalValues.WarpGridParameters.parameter("yStep").intValue() * globalValues.WarpGridParameters.parameter(
                "yNumCells").intValue());

        HashMap nearest = getNearestMappedPositions(p, maxdist, 8);

        double dx;
        double sumdx = 0;
        double dy = 0;
        double sumdy = 0;
        double sumweight = 0;

        for (Iterator i = nearest.keySet().iterator(); i.hasNext();) {
            MappedPosition mp = (MappedPosition) i.next();
            double distance = ((Double) nearest.get(mp)).doubleValue();
            double weight = (1 / Math.pow(distance, 2));

            if (distance > 0.000001) {
                sumdx = sumdx
                    + ((mp.getSource().getCoordinates()[0] - mp.getTarget().getCoordinates()[0]) * weight);
                sumdy = sumdy
                    + ((mp.getSource().getCoordinates()[1] - mp.getTarget().getCoordinates()[1]) * weight);

                sumweight = sumweight + weight;
            } else {
                dx = (mp.getSource().getCoordinates()[0] - mp.getTarget().getCoordinates()[0]);
                dy = (mp.getSource().getCoordinates()[1] - mp.getTarget().getCoordinates()[1]);

                return (new DirectPosition2D(dx, dy));
            }
        }

        dx = sumdx / sumweight;
        dy = sumdy / sumweight;

        return (new DirectPosition2D(dx, dy));
    }

    /**
     *
     * @param p
     * @param maxdistance
     * @return
     */
    private HashMap getNearestMappedPositions(Point2D p, double maxdistance) {
        return getNearestMappedPositions(p, maxdistance, this.getSourcePoints().length);
    }

    /**
     *
     * @param p
     * @param maxnumber
     * @return
     */
    private HashMap getNearestMappedPositions(Point2D p, int maxnumber) {
        return getNearestMappedPositions(p, (envelope.getLength(0) + envelope.getLength(1)),
            this.getSourcePoints().length);
    }

    /**
     * Computes nearest points.
     * @param p
     * @param maxdistance
     * @param number
     * @return
     *
     * @todo consider some indexing mechanism for finding the nearest positions
     */
    private HashMap getNearestMappedPositions(Point2D p, double maxdistance, int maxnumber) {
        HashMap nearest = new HashMap();
        MappedPosition mp = null;

        for (Iterator i = this.getMappedPositions().iterator(); i.hasNext();) {
            mp = (MappedPosition) i.next();

            double dist = p.distance((Point2D) mp.getSource());

            if ((dist < maxdistance) && (nearest.size() < maxnumber)) {
                nearest.put(mp, new Double(dist));
            }
        }

        return nearest;
    }

    /**
     *
     * @author jezekjan
     *
     */
    private static class GridParamValues {
        private ParameterValueGroup WarpGridParameters;

        public GridParamValues(Envelope env, MathTransform trans, double dx, double dy)
            throws TransformException {
            Envelope dxdy = new Envelope2D(env.getCoordinateReferenceSystem(), env.getMinimum(0),
                    env.getMinimum(1), dx, dy);

            /* Transforms dx, dy and envelope to grid system */
            dxdy = CRS.transform(trans, dxdy);
            env = CRS.transform(trans, env);

            ;

            try {
                final DefaultMathTransformFactory factory = new DefaultMathTransformFactory();
                WarpGridParameters = factory.getDefaultParameters("WarpGrid");
                WarpGridParameters.parameter("xStart").setValue((int) (env.getMinimum(0) + 0.5));
                WarpGridParameters.parameter("yStart").setValue((int) (env.getMinimum(1) + 0.5));
                WarpGridParameters.parameter("xStep").setValue((int) Math.ceil(dxdy.getLength(0)));
                WarpGridParameters.parameter("yStep").setValue((int) Math.ceil(dxdy.getLength(1)));
                WarpGridParameters.parameter("xNumCells")
                                  .setValue((int) Math.ceil(env.getLength(0) / dxdy.getLength(0)));
                WarpGridParameters.parameter("yNumCells")
                                  .setValue((int) Math.ceil(env.getLength(1) / dxdy.getLength(1)));

                WarpGridParameters.parameter("warpPositions")
                                  .setValue(new float[2 * (WarpGridParameters.parameter("xNumCells")
                                                                             .intValue() + 1) * (WarpGridParameters.parameter(
                        "yNumCells").intValue() + 1)]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setGridWarpPostions(float[] warpPos) {
            WarpGridParameters.parameter("warpPositions").setValue(warpPos);
        }

        public ParameterValueGroup getWarpGridParameters() {
            return WarpGridParameters;
        }
    }
}
