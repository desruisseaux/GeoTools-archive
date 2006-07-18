/**
 * 
 */
package org.geotools.coverage.processing.operation;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.CannotCropException;
import org.geotools.coverage.processing.Operation2D;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.processing.OperationNotFoundException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;

/**
 * The crop operation is responsible for selecting geographic subareas of the
 * source coverage.
 * 
 * @author Simone Giannecchini
 */
public class Crop extends Operation2D {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4466072819239413456L;

	/**
	 * The parameter descriptor for the sample dimension indices.
	 */
	public static final ParameterDescriptor CROP_ENVELOPE = new DefaultParameterDescriptor(
			Citations.OGC, "Envelope", GeneralEnvelope.class, // Value class
			null, // Array of valid values
			null, // Default value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			false); // Parameter is optional

	/**
	 * @param operation
	 * @throws OperationNotFoundException
	 */
	public Crop() {
		super(new DefaultParameterDescriptorGroup(Citations.OGC,
				"CoverageCrop", new ParameterDescriptor[] { SOURCE_0,
						CROP_ENVELOPE }));

	}

	protected Coverage doOperation(ParameterValueGroup parameters, Hints hints) {
		// /////////////////////////////////////////////////////////////////////
		//
		// Checking input parameteres
		//
		// ///////////////////////////////////////////////////////////////////
		if (parameters.parameter("Source") == null
				|| !(parameters.parameter("Source").getValue() instanceof GridCoverage2D))
			throw new CannotCropException(Errors.format(ErrorKeys.CANT_CROP_$1,
					"Source", GridCoverage2D.class.toString()));

		if (parameters.parameter("Envelope") == null
				|| !(parameters.parameter("Envelope").getValue() instanceof GeneralEnvelope))
			throw new CannotCropException(Errors.format(ErrorKeys.CANT_CROP_$1,
					"Envelope", GeneralEnvelope.class.toString()));

		// /////////////////////////////////////////////////////////////////////
		//
		// Initialization
		//
		// /////////////////////////////////////////////////////////////////////
		final GridCoverage2D source = (GridCoverage2D) parameters.parameter(
				"Source").getValue();
		final GeneralEnvelope sourceEnvelope = (GeneralEnvelope) source
				.getEnvelope();
		final GeneralEnvelope destinationEnvelope = (GeneralEnvelope) parameters
				.parameter("Envelope").getValue();
		if (destinationEnvelope.getCoordinateReferenceSystem() == null)
			destinationEnvelope.setCoordinateReferenceSystem(source
					.getCoordinateReferenceSystem2D());

		// /////////////////////////////////////////////////////////////////////
		//
		// crs have to be equals
		//
		// /////////////////////////////////////////////////////////////////////
		if (!CRSUtilities.equalsIgnoreMetadata(sourceEnvelope
				.getCoordinateReferenceSystem(), destinationEnvelope
				.getCoordinateReferenceSystem()))
			throw new CannotCropException(Errors.format(ErrorKeys.CANT_CROP_$2));
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
		if (!intersectionEnvelope.equals(sourceEnvelope)) {// TODO @task make
			// me parametric
			parameters.parameter("Envelope").setValue(
					intersectionEnvelope.clone());
			return CroppedCoverage2D
					.create(parameters,
							(hints instanceof Hints) ? (Hints) hints
									: new Hints(hints));
		} else
			return source;

	}

}
