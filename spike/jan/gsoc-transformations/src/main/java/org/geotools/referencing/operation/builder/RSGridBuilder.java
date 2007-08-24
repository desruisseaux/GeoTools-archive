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

import javax.vecmath.MismatchedSizeException;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.algorithm.Quadrilateral;
import org.geotools.referencing.operation.builder.algorithm.TriangulationException;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


public class RSGridBuilder extends WarpGridBuilder {
    private final RubberSheetBuilder rsBuilder;

    /**
     * Builds controlling Grid using RubberSheet Transformation
     * @param vectors
     * @param dx
     * @param dy
     * @param envelope
     * @param realToGrid
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws MismatchedReferenceSystemException
     * @throws TransformException
     */
    public RSGridBuilder(List vectors, double dx, double dy, Envelope envelope,
        MathTransform realToGrid)
        throws MismatchedSizeException, MismatchedDimensionException, NoSuchIdentifierException,
            MismatchedReferenceSystemException, TransformException, TriangulationException {
        super(vectors, dx, dy, envelope, realToGrid);

        Envelope gridEnvelope = CRS.transform(worldToGrid, envelope);
             
        double enlarge = gridEnvelope.getLength(0)*0.01;
        DirectPosition p0 = new DirectPosition2D(
        		gridEnvelope.getLowerCorner().getOrdinate(0)-enlarge,
        		gridEnvelope.getLowerCorner().getOrdinate(1)-enlarge);
        
        DirectPosition p2 = new DirectPosition2D(
        		gridEnvelope.getUpperCorner().getOrdinate(0)+enlarge,
        		gridEnvelope.getUpperCorner().getOrdinate(1)+enlarge);              

        DirectPosition p1 = new DirectPosition2D(
                p0.getOrdinate(0), p2.getOrdinate(1));
        DirectPosition p3 = new DirectPosition2D(
                p2.getOrdinate(0), p0.getOrdinate(1));
        
        List gridMP = super.getGridMappedPositions();
        CoordinateReferenceSystem crs = ((MappedPosition)gridMP.get(0)).getSource().getCoordinateReferenceSystem();
        p0 = new DirectPosition2D(crs, p0.getOrdinate(0), p0.getOrdinate(1));
        p1 = new DirectPosition2D(crs, p1.getOrdinate(0), p1.getOrdinate(1));
        p2 = new DirectPosition2D(crs, p2.getOrdinate(0), p2.getOrdinate(1));
        p3 = new DirectPosition2D(crs, p3.getOrdinate(0), p3.getOrdinate(1));
        
        
        Quadrilateral quad = new Quadrilateral(p0, p1, p2, p3);  
        rsBuilder = new RubberSheetBuilder(super.getGridMappedPositions(), quad);
    }

    /**
     * Generates grid of source points.
     * @param values general values of grid
     * @return generated grid
     */
    private float[] generateSourcePoints(ParameterValueGroup values) {
        float[] sourcePoints = ((float[]) values.parameter("warpPositions").getValue());

        for (int i = 0; i <= values.parameter("yNumCells").intValue(); i++) {
            for (int j = 0; j <= values.parameter("xNumCells").intValue(); j++) {
                float x = (j * values.parameter("xStep").intValue())
                    + values.parameter("xStart").intValue();
                float y = (i * values.parameter("yStep").intValue())
                    + values.parameter("yStart").intValue();

                sourcePoints[(i * ((1 + values.parameter("xNumCells").intValue()) * 2)) + (2 * j)] = (float) x;

                sourcePoints[(i * ((1 + values.parameter("xNumCells").intValue()) * 2)) + (2 * j)
                + 1] = (float) y;
            }
        }

        return sourcePoints;
    }

    /**
     * Computes target grid.
     * @return computed target grid.
     */
    protected float[] computeWarpGrid(ParameterValueGroup values) throws FactoryException {
        float[] source = generateSourcePoints(values);

      
      try {
		rsBuilder.getMathTransform().transform(source, 0, source, 0, (source.length + 1) / 2);
	} catch (TransformException e) {
		  throw new FactoryException(Errors.format(ErrorKeys.CANT_TRANSFORM_VALID_POINTS), e);			       			
	} 
       

        return source;
    }   
}
