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
import javax.media.jai.WarpGrid;
import javax.vecmath.MismatchedSizeException;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.transform.LocalizationGrid;
import org.geotools.referencing.operation.transform.WarpGridTransform2D;
import org.geotools.referencing.operation.transform.WarpTransform2D;


/**
 *
 * @author jezekjan
 *
 */
public class LocalizationGridBuilder extends MathTransformBuilder {
    /**
     * LocalizationGrid that will be computed
     */
    private LocalizationGrid gridShift;

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
    WorldParamValues globalValues;

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
    public LocalizationGridBuilder(List vectors, double dx, double dy, Envelope envelope,
        MathTransform realToGrid)
        throws MismatchedSizeException, MismatchedDimensionException,
            MismatchedReferenceSystemException {
        // super.setMappedPositions(vectors);
        this.realToGrid = realToGrid;

        globalValues = new WorldParamValues((float) envelope.getMinimum(0),
                (float) envelope.getMinimum(1), (float) envelope.getMaximum(0),
                (float) envelope.getMaximum(1), (float) dx, (float) dy, realToGrid);
        /*globalValues = new WorldParamValues((float) envelope.getLowerCorner().getCoordinates()[0],
           (float) envelope.getLowerCorner().getCoordinates()[1],
           (float) envelope.getUpperCorner().getCoordinates()[0],
           (float) envelope.getUpperCorner().getCoordinates()[1], (float) dx, (float) dy,
           realToGrid);
         */
        transformMPToGrid(vectors);
        super.setMappedPositions(localpositions);
        height = globalValues.PARAMETERS.parameter("yNumCells").intValue(); // globalValues.getGridHeight();
        width = globalValues.PARAMETERS.parameter("xNumCells").intValue();
        this.envelope = envelope;
        //  gridShift = new LocalizationGrid(width, height);
        //dxgrid = new float[width][height];
        //dygrid = new float[width][height];
        warpPositions = new float[2 * (width + 1) * (height + 1)];
        //    this.gridShiftToEnv = ProjectiveTransform.createScale(2, 0.5);//envelope.getLength(0) / width);
        this.computeWarpGrid();

        //	gridShift.transform((AffineTransform)this.gridShiftToEnv, null);
    }

    /**
     * Transforms MappedPostions to grid system
     *
     */
    private void transformMPToGrid(List MappedPositions) {
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
        return this.buildWarpGrid();

        //return gridShift.getPolynomialTransform(0);
        //return new WarpGridTransform2D(0, 100, width, 0, 100, height, warpPositions);

        //  return new WarpGridTransform2D(0, (int)globalValues.dx ,width,0, (int)globalValues.dy ,height , warpPositions); 
        // return (new WarpTransform2D((gridShift.getPolynomialTransform(0));
    }

    /*
     * (non-Javadoc)
     * @see org.geotools.referencing.operation.builder.MathTransformBuilder#getMinimumPointCount()
     */
    public int getMinimumPointCount() {
        return 1;
    }

    /**
     *
     *
     */
    private void computeGrid() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //   Point2D shiftVector = null; //calculateShifts(new DirectPosition2D((i * globalValues.dx)
                //+ globalValues.xmin, (j * globalValues.dy) + globalValues.ymin));
                Point2D shiftVector = calculateShift(new DirectPosition2D(globalValues.PARAMETERS.parameter(
                                "xStart").doubleValue()
                            + (i * globalValues.PARAMETERS.parameter("xStep").doubleValue()),
                            globalValues.PARAMETERS.parameter("yStart").doubleValue()
                            + (j * globalValues.PARAMETERS.parameter("yStep").doubleValue())));

                double x = i + shiftVector.getX();
                double y = j + shiftVector.getY();

                gridShift.setLocalizationPoint(i, j, x, y);
                warpPositions[(2 * (i + 1) * (j + 1)) - 1] = (float) x;
                warpPositions[2 * (i + 1) * (j + 1)] = (float) y;

                //   dxgrid[i][j] = (new Double(shiftVector.getX())).floatValue();
                //   dygrid[i][j] = (new Double(shiftVector.getY())).floatValue();
            }
        }
    }

    private void computeWarpGrid() {
        ParameterValueGroup WarpParams = globalValues.WarpGridParameters;

        for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
            for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
                //   Point2D shiftVector = null; //calculateShifts(new DirectPosition2D((i * globalValues.dx)
                //+ globalValues.xmin, (j * globalValues.dy) + globalValues.ymin));
                Point2D shiftVector = calculateShift(new DirectPosition2D(WarpParams.parameter(
                                "xStart").intValue()
                            + (j * WarpParams.parameter("xStep").intValue()),
                            WarpParams.parameter("yStart").intValue()
                            + (i * WarpParams.parameter("yStep").intValue())));

                double x = shiftVector.getX() + (j * WarpParams.parameter("xStep").intValue())
                    + WarpParams.parameter("xStart").intValue();
                double y = shiftVector.getY() + (i * WarpParams.parameter("yStep").intValue())
                    + WarpParams.parameter("yStart").intValue();
                ;

                warpPositions[(i * ((1 + WarpParams.parameter("yNumCells").intValue()) * 2))
                + (2 * j)] = (float) x;
                warpPositions[(i * ((1 + WarpParams.parameter("yNumCells").intValue()) * 2))
                + (2 * j) + 1] = (float) y;
            }
        }

        globalValues.setGridWarpPostions(warpPositions);
    }

    private WarpTransform2D buildWarpGrid() {
        return (WarpTransform2D) (new WarpGridTransform2D.Provider()).createMathTransform(globalValues
            .getWarpGridParameters());
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
    private static class WorldParamValues {
        /** Descriptor for the "{@link WarpGrid#getXStart  xStart}" parameter value. */
        private static final ParameterDescriptor xStart = new DefaultParameterDescriptor("xStart",
                double.class, null, null);
        private static final ParameterDescriptor xEnd = new DefaultParameterDescriptor("xEnd",
                double.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getXStep xStep}" parameter value. */
        private static final ParameterDescriptor xStep = new DefaultParameterDescriptor("xStep",
                double.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getXNumCells xNumCells}" parameter value. */
        private static final ParameterDescriptor xNumCells = new DefaultParameterDescriptor("xNumCells",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getYStart yStart}" parameter value. */
        private static final ParameterDescriptor yStart = new DefaultParameterDescriptor("yStart",
                double.class, null, null);
        private static final ParameterDescriptor yEnd = new DefaultParameterDescriptor("yEnd",
                double.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getYStep yStep}" parameter value. */
        private static final ParameterDescriptor yStep = new DefaultParameterDescriptor("yStep",
                double.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getYNumCells yNumCells}" parameter value. */
        private static final ParameterDescriptor yNumCells = new DefaultParameterDescriptor("yNumCells",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#warpPositions  warpPositions}" parameter value. */
        private static final ParameterDescriptor warpPositions = new DefaultParameterDescriptor("warpPositions",
                float[].class, null, null);

        /**
         * The parameters group.
         */
        final static ParameterDescriptorGroup DESRIPTORS = new DefaultParameterDescriptorGroup("WorldGridValues",
                new ParameterDescriptor[] {
                    xStart, xStep, xNumCells, xEnd, yStart, yStep, yNumCells, yEnd, warpPositions
                });
        final static ParameterGroup PARAMETERS = new ParameterGroup(DESRIPTORS);
        private ParameterValueGroup WarpGridParameters;

        /**
         * TODO -Replace this with ParametrValueGroup
         * Constructs ahelper class for handling global parameters.
         * @param xmin xmin
         * @param ymin ymin
         * @param xmax xmax
         * @param ymax ymax
         * @param dx dx
         * @param dy dy
         */
        public WorldParamValues(float xmin, float ymin, float xmax, float ymax, float dx, float dy,
            MathTransform RealToGrid) {
            super();

            PARAMETERS.parameter("xStart").setValue((float) xmin);
            PARAMETERS.parameter("xEnd").setValue((float) xmax);
            PARAMETERS.parameter("xStep").setValue((float) dx);
            PARAMETERS.parameter("xNumCells").setValue((int) Math.ceil(Math.abs(xmax - xmin) / dx));

            PARAMETERS.parameter("yStart").setValue((float) ymin);
            PARAMETERS.parameter("yEnd").setValue((float) ymax);
            PARAMETERS.parameter("yStep").setValue((float) dy);
            PARAMETERS.parameter("yNumCells").setValue((int) Math.ceil(Math.abs(ymax - ymin) / dy));

            transformToGridValues((ParameterValueGroup) PARAMETERS, RealToGrid);
        }

        /**
         * Sets WarpPositions in real world coordinates
         * @param warpPos
         */
        public void setWorldWarpPostions(float[] warpPos) {
            PARAMETERS.parameter("warpPositions").setValue(warpPos);
        }

        public void setGridWarpPostions(float[] warpPos) {
            WarpGridParameters.parameter("warpPositions").setValue(warpPos);
        }

        public ParameterValueGroup getWarpGridParameters() {
            return WarpGridParameters;
        }

        /**
         * Transforms real word values into the grid values
         * @param WorldToGrid
         * @return
         */
        private void transformToGridValues(ParameterValueGroup values, MathTransform WorldToGrid) {
            ParameterValueGroup parameters = null;

            //ParameterValueGroup results = (ParameterValueGroup) values.clone();
            final double WorldXMIN = values.parameter("xStart").doubleValue();
            final double WorldYMIN = values.parameter("yStart").doubleValue();

            final double WorldXMAX = values.parameter("xEnd").doubleValue();
            final double WorldYMAX = values.parameter("yEnd").doubleValue();

            final double WorldXSTEP = values.parameter("xStep").doubleValue();
            final double WorldYSTEP = values.parameter("yStep").doubleValue();

            final double WorldXNUMCELLS = values.parameter("xNumCells").doubleValue();
            final double WorldYNUMCELLS = values.parameter("yNumCells").doubleValue();

            DirectPosition result = new DirectPosition2D();

            try {
                // Calculates grid start 
                WorldToGrid.transform((DirectPosition) (new DirectPosition2D(WorldXMIN, WorldYMAX)),
                    result);

                final double GridXStart = result.getCoordinates()[0] + 0.5; // center of the pixel
                final double GridYStart = result.getCoordinates()[1] + 0.5; //center of the pixel

                // calculates scale on axis
                WorldToGrid.transform((DirectPosition) (new DirectPosition2D(WorldXMAX, WorldYMAX)),
                    result);

                final double xScale = (WorldXMAX - WorldXMIN) / ((result.getCoordinates()[0] + 0.5)
                    - GridXStart);
                final double yScale = (WorldYMAX - WorldYMIN) / ((result.getCoordinates()[0] + 0.5)
                    - GridYStart);

                final DefaultMathTransformFactory factory = new DefaultMathTransformFactory();

                WarpGridParameters = factory.getDefaultParameters("WarpGrid");
                WarpGridParameters.parameter("xStart").setValue((int) GridXStart);
                WarpGridParameters.parameter("yStart").setValue((int) GridYStart);

                WarpGridParameters.parameter("xStep")
                                  .setValue((int) (values.parameter("xStep").doubleValue() / xScale));

                WarpGridParameters.parameter("yStep")
                                  .setValue((int) (values.parameter("yStep").doubleValue() / yScale));

                WarpGridParameters.parameter("xNumCells")
                                  .setValue((int) (values.parameter("xNumCells").doubleValue()));

                WarpGridParameters.parameter("yNumCells")
                                  .setValue((int) (values.parameter("yNumCells").doubleValue()));
            } catch (InvalidParameterValueException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParameterNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvalidParameterTypeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchIdentifierException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TransformException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
