/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.processing;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROIShape;
import javax.media.jai.StatisticsOpImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.ImagingParameterDescriptorsDecorator;
import org.geotools.parameter.ImagingParameters;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This class is the root class for the Statistics operations based on
 * {@link JAI}'s {@link StatisticsOpImage} like Extrema and Histogram. It
 * provides basica capabilities for management of geospatial parameters like
 * {@link javax.media.jai.ROI}s and subsampling factors.
 * 
 * @author Simone Giannecchini
 * 
 */
public abstract class AbstractStatisticsOperationJAI extends
		AbstractOperationJAIDecorator {
	//
	/** {@link Logger} for this class. */
	public final static Logger LOGGER = Logger
			.getLogger("org.geotools.coverage.processing");

	/**
	 * The parameter descriptor for the SPATIAL_SUBSAMPLING_X
	 */
	public static final ParameterDescriptor SPATIAL_SUBSAMPLING_X = new DefaultParameterDescriptor(
			Citations.OGC, "xPeriod", Double.class, // Value class (mandatory)
			null, // Array of valid values
			null, // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true);

	/**
	 * The parameter descriptor for the SPATIAL_SUBSAMPLING_Y
	 */
	public static final ParameterDescriptor SPATIAL_SUBSAMPLING_Y = new DefaultParameterDescriptor(
			Citations.OGC, "yPeriod", Double.class, // Value class (mandatory)
			null, // Array of valid values
			null, // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true);

	/**
	 * The parameter descriptor for the coordinate reference system.
	 */
	public static final ParameterDescriptor ROI = new DefaultParameterDescriptor(
			Citations.OGC, "roi", Polygon.class, // Value class (mandatory)
			null, // Array of valid values
			null, // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true);

	static {
		final Map replacedDescriptors = new HashMap(2);
		replacedDescriptors.put("xPeriod", SPATIAL_SUBSAMPLING_X);
		replacedDescriptors.put("yPeriod", SPATIAL_SUBSAMPLING_Y);
		replacedDescriptors.put("roi", ROI);
		REPLACED_DESCRIPTORS = Collections.unmodifiableMap(replacedDescriptors);
	}

	/**
	 * @param operation
	 */
	public AbstractStatisticsOperationJAI(OperationDescriptor operation) {
		super(operation);
	}

	/**
	 * @param operationDescriptor
	 * @param decorator
	 */
	public AbstractStatisticsOperationJAI(
			OperationDescriptor operationDescriptor,
			ImagingParameterDescriptorsDecorator decorator) {
		super(operationDescriptor, decorator);
	}

	public AbstractStatisticsOperationJAI(String name) {
		super(name);
	}

	protected ParameterBlockJAI prepareParameters(ParameterValueGroup parameters) {
		// /////////////////////////////////////////////////////////////////////
		//
		// Make a copy of the input parameters.
		//
		// ///////////////////////////////////////////////////////////////////
		final ImagingParameters copy = (ImagingParameters) descriptor
				.createValue();
		final ParameterBlockJAI block = (ParameterBlockJAI) copy.parameters;
		try {

			// /////////////////////////////////////////////////////////////////////
			//
			//
			// Now trancode the parameters as needed by this operation.
			//
			//
			// ///////////////////////////////////////////////////////////////////
			// XXX make it robust
			final GridCoverage2D source = (GridCoverage2D) parameters
					.parameter(operation.getSourceNames()[PRIMARY_SOURCE_INDEX])
					.getValue();
			final AffineTransform gridToWorldTransform = new AffineTransform(
					(AffineTransform) ((GridGeometry2D) source
							.getGridGeometry()).getGridToCRS2D());
			gridToWorldTransform.translate(-0.5, -0.5);
			final MathTransform worldToGridTransform;
			try {
				worldToGridTransform = ProjectiveTransform
						.create(gridToWorldTransform.createInverse());
			} catch (NoninvertibleTransformException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				// fallback block settings in case something bad happened
				block.setParameter("xPeriod", new Integer(1));
				block.setParameter("yPeriod", new Integer(1));
				return block;
			}

			// /////////////////////////////////////////////////////////////////////
			//
			// Transcode the xPeriod and yPeriod params by applying the
			// WorldToGRid
			// transformation for the source coverage.
			//
			// I am assuming that the supplied values are in the same
			// CRS as the source coverage. We here apply
			//
			// /////////////////////////////////////////////////////////////////////
			final double xPeriod = parameters.parameter("xPeriod")
					.doubleValue();
			final double yPeriod = parameters.parameter("yPeriod")
					.doubleValue();

			// //
			//
			// get the original envelope and the crs
			//
			// //
			final CoordinateReferenceSystem crs = source
					.getCoordinateReferenceSystem2D();
			final Envelope2D envelope = source.getEnvelope2D();
			// build the new one that spans over the requested area
			// NOTE:
			final DirectPosition2D LLC = new DirectPosition2D(crs, envelope.x,
					envelope.y);
			LLC.setCoordinateReferenceSystem(crs);
			final DirectPosition2D URC = new DirectPosition2D(crs, envelope.x
					+ xPeriod, envelope.y + yPeriod);
			URC.setCoordinateReferenceSystem(crs);
			final Envelope2D shrinkedEnvelope = new Envelope2D(LLC, URC);

			// transform back into raster space
			final Rectangle2D transformedEnv = CRS.transform(
					worldToGridTransform, shrinkedEnvelope).toRectangle2D();

			// block settings
			block.setParameter("xPeriod", new Integer((int) transformedEnv
					.getWidth()));
			block.setParameter("yPeriod", new Integer((int) transformedEnv
					.getHeight()));

			// /////////////////////////////////////////////////////////////////////
			//
			// Transcode the polygon parameter into a roi.
			//
			// I am assuming that the supplied values are in the same
			// CRS as the source coverage. We here apply
			//
			// /////////////////////////////////////////////////////////////////////
			final Object o = parameters.parameter("roi").getValue();
			if (o != null && o instanceof Polygon) {
				final Polygon roiInput = (Polygon) o;
				if (new ReferencedEnvelope(roiInput.getEnvelopeInternal(),
						source.getCoordinateReferenceSystem2D())
						.intersects((Envelope) new ReferencedEnvelope(envelope))) {
					final java.awt.Polygon shapePolygon = convertPolygon(
							roiInput, worldToGridTransform);

					block.setParameter("roi", new ROIShape(shapePolygon));
				}
			}
			return block;
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			// return defaults
			return block;
		}
	}

	/**
	 * Converte a JTS {@link Polygon}, which represents a ROI, int an AWT
	 * {@link java.awt.Polygon} by means of the provided {@link MathTransform}.
	 * 
	 * @param roiInput
	 *            the input ROI as a JTS {@link Polygon}.
	 * @param worldToGridTransform
	 *            the {@link MathTransform} to tapply to the input ROI.
	 * @return an AWT {@link java.awt.Polygon}.
	 * @throws TransformException
	 *             in case the provided {@link MathTransform} chockes.
	 */
	private static java.awt.Polygon convertPolygon(final Polygon roiInput,
			MathTransform worldToGridTransform) throws TransformException {
		final boolean isIdentity = worldToGridTransform.isIdentity();
		final java.awt.Polygon retValue = new java.awt.Polygon();
		final double coords[] = new double[2];
		final LineString exteriorRing = roiInput.getExteriorRing();
		final CoordinateSequence exteriorRingCS = exteriorRing
				.getCoordinateSequence();
		final int numCoords = exteriorRingCS.size();
		for (int i = 0; i < numCoords; i++) {
			// get the actual coord
			coords[0] = exteriorRingCS.getX(i);
			coords[1] = exteriorRingCS.getY(i);

			// transform it
			if (!isIdentity)
				worldToGridTransform.transform(coords, 0, coords, 0, 1);

			// send it back to the returned polygon
			retValue.addPoint((int) (coords[0] + 0.5d),
					(int) (coords[1] + 0.5d));

		}

		// return the created polygon.
		return retValue;
	}
}
