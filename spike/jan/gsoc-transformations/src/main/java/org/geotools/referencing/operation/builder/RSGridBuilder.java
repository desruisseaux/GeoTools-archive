package org.geotools.referencing.operation.builder;

import java.awt.geom.Point2D;
import java.util.List;

import javax.vecmath.MismatchedSizeException;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.builder.algorithm.Quadrilateral;
import org.geotools.referencing.operation.builder.algorithm.TriangulationException;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
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
			MathTransform realToGrid) throws MismatchedSizeException,
			MismatchedDimensionException, MismatchedReferenceSystemException,
			TransformException, TriangulationException {
		
		super(vectors, dx, dy, envelope, realToGrid);
		
		DirectPosition p0 = new DirectPosition2D(envelope.getCoordinateReferenceSystem(),envelope.getLowerCorner().getOrdinate(0)-1.5,envelope.getLowerCorner().getOrdinate(1)-1.5);
		DirectPosition p2 =new DirectPosition2D(envelope.getCoordinateReferenceSystem(),envelope.getUpperCorner().getOrdinate(0)+1.5,envelope.getUpperCorner().getOrdinate(1)+1.5);
		
		DirectPosition p1 = new DirectPosition2D(envelope.getCoordinateReferenceSystem(), p0.getOrdinate(0), p2.getOrdinate(1));
		DirectPosition p3 = new DirectPosition2D(envelope.getCoordinateReferenceSystem(), p2.getOrdinate(0), p0.getOrdinate(1));
		
		Quadrilateral quad = new Quadrilateral(p0, p1, p2, p3);
	
		rsBuilder = new RubberSheetBuilder(vectors, quad);
		
	}

	protected float[] computeWarpGrid(ParameterValueGroup values) {
		  float[] warpPositions = (float[]) values.parameter("warpPositions").getValue();

	        for (int i = 0; i <= values.parameter("yNumCells").intValue(); i++) {
	            for (int j = 0; j <= values.parameter("xNumCells").intValue(); j++) {
	                Point2D shiftVector = calculateShift(new DirectPosition2D(values.parameter(
	                                "xStart").intValue()
	                            + (j * values.parameter("xStep").intValue()),
	                            values.parameter("yStart").intValue()
	                            + (i * values.parameter("yStep").intValue())));

	                double x = shiftVector.getX() + (j * values.parameter("xStep").intValue())
	                    + values.parameter("xStart").intValue();
	                double y = shiftVector.getY() + (i * values.parameter("yStep").intValue())
	                    + values.parameter("yStart").intValue();

	                warpPositions[(i * ((1 + values.parameter("xNumCells").intValue()) * 2))
	                + (2 * j)] = (float) x;

	                warpPositions[(i * ((1 + values.parameter("xNumCells").intValue()) * 2))
	                + (2 * j) + 1] = (float) y;
	            }
	        }

	        return warpPositions;
	}
	
	private Point2D calculateShift(DirectPosition2D p){
		Point2D shift = null;
		try {
			DirectPosition target = rsBuilder.getMathTransform().transform(p, null);
			shift = new DirectPosition2D(target.getOrdinate(0) - p.getOrdinate(0),
					                     target.getOrdinate(1) - p.getOrdinate(1));
		} catch (MismatchedDimensionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return shift;
	}

}
