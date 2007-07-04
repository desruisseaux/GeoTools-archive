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
public abstract class WarpGridBuilder extends MathTransformBuilder {
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
     * Construts Builder 
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
        this.envelope = envelope;                     
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
    	if (warpPositions == null ){
    		warpPositions =  (float[]) globalValues.WarpGridParameters.parameter("warpPositions")
                                                                     .getValue();
    		warpPositions = this.computeWarpGrid(globalValues.WarpGridParameters);
    		globalValues.setGridWarpPostions(warpPositions);
    	}
    	
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
    abstract protected float[] computeWarpGrid(ParameterValueGroup values);/* {
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
      
        return warpPositions;
    }*/

    public float[][] getDxGrid() {
        if ((dxgrid == null) || (dxgrid.length == 0)) {
            ParameterValueGroup WarpParams = globalValues.WarpGridParameters;
            final int xNumCells = WarpParams.parameter("xNumCells").intValue();
            final int yNumCells = WarpParams.parameter("yNumCells").intValue();
            final int xStep = WarpParams.parameter("xStep").intValue();
            final int yStep = WarpParams.parameter("yStep").intValue();

            final float[] warpPositions = (float[]) WarpParams.parameter("warpPositions").getValue();

            dxgrid = new float[xNumCells + 1][yNumCells + 1];

            for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
                for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
                    dxgrid[j][i] = (float) warpPositions[(int) ((i * (1 + xNumCells) * 2) + (2 * j))]
                        - (j * xStep);
                }
            }
        }

        return dxgrid;
    }

    public float[][] getDyGrid() {
        if ((dygrid == null) || (dygrid.length == 0)) {
            ParameterValueGroup WarpParams = globalValues.WarpGridParameters;
            final int xNumCells = WarpParams.parameter("xNumCells").intValue();
            final int yNumCells = WarpParams.parameter("yNumCells").intValue();
            final int xStep = WarpParams.parameter("xStep").intValue();
            final int yStep = WarpParams.parameter("yStep").intValue();

            final float[] warpPositions = (float[]) WarpParams.parameter("warpPositions").getValue();

            dygrid = new float[xNumCells + 1][yNumCells + 1];

            for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
                for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
                    dygrid[j][i] = (float) warpPositions[(int) ((i * (1 + xNumCells) * 2) + (2 * j)+1)]
                        - (i * yStep);
                }
            }
        }

        return dygrid;
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
