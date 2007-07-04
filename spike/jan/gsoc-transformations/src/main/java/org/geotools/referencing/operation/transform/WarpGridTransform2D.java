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
package org.geotools.referencing.operation.transform;

import javax.media.jai.Warp;
import javax.media.jai.WarpGrid;

import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.operation.MathTransformProvider;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Transformation;


/**
 * 
 * @author jezekjan
 *
 */
public class WarpGridTransform2D extends WarpTransform2D {
    private final Warp warp;
    private final Warp inverse;

    public WarpGridTransform2D(int xStart, int xStep, int xNumCells, int yStart, int yStep,
        int yNumCells, float[] warpPositions) {
        super(new WarpGrid(xStart, xStep, xNumCells, yStart, yStep, yNumCells, warpPositions),
            new WarpGrid(xStart, xStep, xNumCells, yStart, yStep, yNumCells, warpPositions));
        warp = new WarpGrid(xStart, xStep, xNumCells, yStart, yStep, yNumCells, warpPositions);
        
        
        inverse = null;
        /*
        for (int i=0; i<warpPositions.length; i++){
        	warpPositions[i]=-warpPositions[i];
        }
        inverse = new WarpGrid(xStart, xStep, xNumCells, yStart, yStep, yNumCells, warpPositions); //TODO generate inverse warpgrid
        System.out.print("dsd");
        */
        // super(warp, null);
    }

    public WarpGridTransform2D(Warp warp, Warp inverse){
    	super(warp,inverse);
    	this.warp = warp;
    	this.inverse = inverse;
    
    }
       
    
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }

    public static class Provider extends MathTransformProvider {
        /** Serial number for interoperability with different versions. */
        private static final long serialVersionUID = -123487815665723468L;

        /** Descriptor for the "{@link WarpGrid#getXStart  xStart}" parameter value. */
        public static final ParameterDescriptor xStart = new DefaultParameterDescriptor("xStart",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getXStep xStep}" parameter value. */
        public static final ParameterDescriptor xStep = new DefaultParameterDescriptor("xStep",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getXNumCells xNumCells}" parameter value. */
        public static final ParameterDescriptor xNumCells = new DefaultParameterDescriptor("xNumCells",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getYStart yStart}" parameter value. */
        public static final ParameterDescriptor yStart = new DefaultParameterDescriptor("yStart",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getYStep yStep}" parameter value. */
        public static final ParameterDescriptor yStep = new DefaultParameterDescriptor("yStep",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getYNumCells yNumCells}" parameter value. */
        public static final ParameterDescriptor yNumCells = new DefaultParameterDescriptor("yNumCells",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#warpPositions  warpPositions}" parameter value. */
        public static final ParameterDescriptor warpPositions = new DefaultParameterDescriptor("warpPositions",
                float[].class, null, null);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.GEOTOOLS, "WarpGrid")
                },
                new ParameterDescriptor[] {
                    xStart, xStep, xNumCells, yStart, yStep, yNumCells, warpPositions
                });

        /**
         * Create a provider for warp transforms.
         */
        public Provider() {
            super(2, 2, PARAMETERS);
        }

        /**
         * Returns the operation type.
         */
        public Class getOperationType() {
            return Transformation.class;
        }

        /**
         * Creates a warp transform from the specified group of parameter values.
         *
         * @param  values The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup values)
            throws ParameterNotFoundException {
            final int XSTART = intValue(xStart, values);
            final int XSTEP = intValue(xStep, values);
            final int XNUMCELLS = intValue(xNumCells, values);
            final int YSTART = intValue(yStart, values);
            final int YSTEP = intValue(yStep, values);
            final int YNUMCELLS = intValue(yNumCells, values);
            final float[] WARPPOSITIONS = (float[]) value(warpPositions, values);

            
            final Warp warp;
            warp = new WarpGrid(XSTART, XSTEP, XNUMCELLS, YSTART, YSTEP, YNUMCELLS, WARPPOSITIONS);
            //TODO - inverse transform
            
             return new WarpGridTransform2D(warp, warp);
        }
    }
}
