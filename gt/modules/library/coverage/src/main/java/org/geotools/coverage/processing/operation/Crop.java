/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.coverage.processing.operation;

import java.awt.geom.AffineTransform;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.CannotCropException;
import org.geotools.coverage.processing.Operation2D;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.referencing.CRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.processing.OperationNotFoundException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.Envelope;


/**
 * The crop operation is responsible for selecting geographic subareas of the
 * source coverage.
 *
 * @todo Consider refactoring as a {@code OperationJAI} subclass. We could get ride of the
 *       {@code CroppedGridCoverage2D} class. The main feature to add is the
 *       copy of interpolation and border extender parameters to the hints.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 * @since 2.3
 *
 * @see javax.media.jai.operator.ScaleDescriptor
 */
public class Crop extends Operation2D {
	/**
	 * Serial number for cross-version compatibility.
	 */
	private static final long serialVersionUID = 4466072819239413456L;

	/**
	 * The parameter descriptor for the sample dimension indices.
	 */
	public static final ParameterDescriptor CROP_ENVELOPE = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "Envelope", GeneralEnvelope.class, // Value class
			null, // Array of valid values
			null, // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			false); // Parameter is optional
	
	/**
	 * The parameter descriptor for the sample dimension indices.
	 */
	public static final ParameterDescriptor CONSERVE_ENVELOPE = new DefaultParameterDescriptor(
			Citations.GEOTOOLS, "ConserveEnvelope", Boolean.class, // Value
			// class
			new Boolean[]{Boolean.TRUE,Boolean.FALSE}, // Array of valid values
			Boolean.FALSE, // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is optional

	/**
	 * 
	 * @throws OperationNotFoundException
	 */
	public Crop() {
		super(new DefaultParameterDescriptorGroup(Citations.GEOTOOLS,
				"CoverageCrop", new ParameterDescriptor[] { SOURCE_0,
						CROP_ENVELOPE,CONSERVE_ENVELOPE }));

	}

	public Coverage doOperation(ParameterValueGroup parameters, Hints hints) {
		// /////////////////////////////////////////////////////////////////////
		//
		// Checking input parameteres
		//
		// ///////////////////////////////////////////////////////////////////
		final ParameterValue sourceParameter = parameters.parameter("Source");
		if (sourceParameter == null
				|| !(sourceParameter.getValue() instanceof GridCoverage2D)) {
			throw new CannotCropException(Errors.format(
					ErrorKeys.NULL_PARAMETER_$2, "Source", GridCoverage2D.class
							.toString()));
		}
		final ParameterValue envelopeParameter = parameters
				.parameter("Envelope");
		if (envelopeParameter == null
				|| !(envelopeParameter.getValue() instanceof Envelope))
			throw new CannotCropException(Errors.format(
					ErrorKeys.NULL_PARAMETER_$2, "Envelope",
					GeneralEnvelope.class.toString()));
		final ParameterValue conserveEnvelopeParameter = parameters
		.parameter("ConserveEnvelope");
		if (conserveEnvelopeParameter == null
				|| !(conserveEnvelopeParameter.getValue() instanceof Boolean))
			throw new CannotCropException(Errors.format(
					ErrorKeys.NULL_PARAMETER_$2, "ConserveEnvelope",
					Double.class.toString()));

		// /////////////////////////////////////////////////////////////////////
		//
		// Initialization
		//
		// /////////////////////////////////////////////////////////////////////
		final GridCoverage2D source = (GridCoverage2D) sourceParameter
				.getValue();
		final Envelope sourceEnvelope = source.getEnvelope();
		Envelope destinationEnvelope = (Envelope) envelopeParameter.getValue();
		CoordinateReferenceSystem sourceCRS, destinationCRS;
        sourceCRS = sourceEnvelope.getCoordinateReferenceSystem();
        destinationCRS = destinationEnvelope.getCoordinateReferenceSystem();
		if (destinationCRS == null) {
            // Do not change the user provided object - clone it first.
            final GeneralEnvelope ge = new GeneralEnvelope(destinationEnvelope);
            destinationCRS = source.getCoordinateReferenceSystem2D();
			ge.setCoordinateReferenceSystem(destinationCRS);
            destinationEnvelope = ge;
        }

		// /////////////////////////////////////////////////////////////////////
		//
		// crs have to be equals
		//
		// /////////////////////////////////////////////////////////////////////
		if (!CRS.equalsIgnoreMetadata(sourceCRS, destinationCRS)) {
			throw new CannotCropException(Errors.format(ErrorKeys.MISMATCHED_ENVELOPE_CRS_$2,
                    sourceCRS.getName().getCode(), destinationCRS.getName().getCode()));
        }
		// /////////////////////////////////////////////////////////////////////
		//
		// check the intersection and, if needed, do the operation.
		//
		// /////////////////////////////////////////////////////////////////////
		final GeneralEnvelope intersectionEnvelope = new GeneralEnvelope(
				destinationEnvelope);
		intersectionEnvelope.setCoordinateReferenceSystem(source
				.getCoordinateReferenceSystem());
		// intersect the envelopes
		intersectionEnvelope.intersect(sourceEnvelope);
		if (intersectionEnvelope.isEmpty())
			return null;
		// do we need to do something
		if (!intersectionEnvelope.equals(sourceEnvelope, XAffineTransform
				.getScale((AffineTransform)((GridGeometry2D) source.getGridGeometry())
						.getGridToCRS2D()) / 2.0)) {
			envelopeParameter.setValue(intersectionEnvelope.clone());
			return CroppedCoverage2D
					.create(parameters,
							(hints instanceof Hints) ? (Hints) hints
									: new Hints(hints));
		} else {
			return source;
		}
	}
}
