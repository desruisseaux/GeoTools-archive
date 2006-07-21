/**
 * 
 */
package org.geotools.coverage.processing.operation;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.RenderedImage;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.CannotCropException;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridRange;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.InternationalString;

/**
 * This class is responsible for applying a crop operation to a source coverage
 * with a specified envelope.
 * 
 * @author Simone Giannecchini
 * 
 */
final class CroppedCoverage2D extends GridCoverage2D {
	/**
	 * 
	 */
	private static final long serialVersionUID = -501742139906901754L;

	private CroppedCoverage2D(InternationalString name,
			RenderedOp croppedImage, GridGeometry2D croppedGeometry,
			GridCoverage2D source) {
		super(name.toString(), croppedImage, croppedGeometry,
				(GridSampleDimension[]) source.getSampleDimensions().clone(),
				new GridCoverage[] { source }, null);
	}

	/**
	 * Applies the band select operation to a grid coverage.
	 * 
	 * @param parameters
	 *            List of name value pairs for the parameters.
	 * @param A
	 *            set of rendering hints, or {@code null} if none.
	 * @return The result as a grid coverage.
	 */
	static GridCoverage2D create(final ParameterValueGroup parameters,
			RenderingHints hints) {
		// /////////////////////////////////////////////////////////////////////
		//
		// GET SOURCES
		//
		// /////////////////////////////////////////////////////////////////////
		final GridCoverage2D source = (GridCoverage2D) parameters.parameter(
				"Source").getValue();
		final GeneralEnvelope intersectionEnvelope = (GeneralEnvelope) parameters
				.parameter("Envelope").getValue();
		try {
			// /////////////////////////////////////////////////////////////////////
			//
			// get the cropped grid geometry
			//
			// /////////////////////////////////////////////////////////////////////
			final GridGeometry2D croppedGeometry = getCroppedGridGeometry(
					intersectionEnvelope, source);
			if (croppedGeometry == null) {
				throw new CannotCropException(Errors.format(ErrorKeys.CANT_CROP));
			}
			final GridRange croppedRange = croppedGeometry.getGridRange();
			final int xAxis = croppedGeometry.gridDimensionX;
			final int yAxis = croppedGeometry.gridDimensionY;
			final double minX = croppedRange.getLower(xAxis);
			final double minY = croppedRange.getLower(yAxis);
			final double width = croppedRange.getLength(xAxis);
			final double height = croppedRange.getLength(yAxis);

			// /////////////////////////////////////////////////////////////////////
			//
			// get the rendered image and crop it
			//
			// /////////////////////////////////////////////////////////////////////
			final RenderedImage sourceImage = source.getRenderedImage();
			final ParameterBlockJAI pbjCrop = new ParameterBlockJAI("Crop");
			pbjCrop.addSource(sourceImage);

			// /////////////////////////////////////////////////////////////////////
			//
			// The source coverage is now selected and will not change anymore.
			// Gets the JAI instance and factories to use from the rendering
			// hints.
			//
			// /////////////////////////////////////////////////////////////////////
			final JAI processor = OperationJAI.getJAI(hints);

			// executing the crop
			pbjCrop.setParameter("x", new Float(minX));
			pbjCrop.setParameter("y", new Float(minY));
			pbjCrop.setParameter("width", new Float(width));
			pbjCrop.setParameter("height", new Float(height));
			final RenderedOp croppedImage = processor.createNS("Crop", pbjCrop,
					hints);

			// executing the transalte to have minx and miny set to zero
			final ParameterBlockJAI pbjTranslate = new ParameterBlockJAI(
					"Translate");
			pbjTranslate.addSource(croppedImage);
			pbjTranslate.setParameter("xTrans", new Float(-minX));
			pbjTranslate.setParameter("yTrans", new Float(-minY));
			if (hints.get(JAI.KEY_INTERPOLATION) != null)
				pbjTranslate.setParameter("interpolation", hints
						.get(JAI.KEY_INTERPOLATION) != null);
			final RenderedOp translatedImage = processor.createNS("Translate",
					pbjTranslate, hints);

			// /////////////////////////////////////////////////////////////////////
			//
			// Creating the cropped coverage.
			//
			// /////////////////////////////////////////////////////////////////////
			return new CroppedCoverage2D(source.getName(), translatedImage,
					new GridGeometry2D(new GeneralGridRange(translatedImage),
							croppedGeometry.getEnvelope()), source);
		} catch (TransformException e) {
			throw new CannotCropException(
					Errors.format(ErrorKeys.CANT_CROP), e);
		} catch (NoninvertibleTransformException e) {
			throw new CannotCropException(
					Errors.format(ErrorKeys.CANT_CROP), e);
		}

		// something bad happened

	}

	/**
	 * Cropping the grid geometry to the requested area. We do not recheck for
	 * intersection here.
	 * 
	 * @param intersectionEnvelope
	 * @param gridCoverage2
	 * @return
	 * @throws NoninvertibleTransformException
	 * @throws TransformException
	 * @throws TransformException
	 */
	private static GridGeometry2D getCroppedGridGeometry(
			GeneralEnvelope intersectionEnvelope, GridCoverage gridCoverage)
			throws NoninvertibleTransformException, TransformException {

		// do we actually need to crop?
		if (intersectionEnvelope.isEmpty())
			return null;
		// do we have to intersect?
		if (intersectionEnvelope.equals(gridCoverage.getEnvelope()))
			return new GridGeometry2D(gridCoverage.getGridGeometry()
					.getGridRange(), gridCoverage.getEnvelope());

		// if I get here I have something to crop
		// getting the old grid range for using it later
		final GeneralGridRange oldRange = (GeneralGridRange) gridCoverage
				.getGridGeometry().getGridRange();
		// using the world to grid transform for going from envelope to new grid
		// range.
		final AffineTransform gridToWorld = new AffineTransform(
				(AffineTransform) gridCoverage.getGridGeometry()
						.getGridToCoordinateSystem());

		// build the new range by adding a -0.5 translation to keep into
		// account  translation of grid geometry constructor
		gridToWorld.translate(-0.5, -0.5);
		final MathTransform worldToGridTransform = ProjectiveTransform
				.create(gridToWorld.createInverse());
		final GeneralEnvelope finalGridRange = CRSUtilities.transform(
				worldToGridTransform, intersectionEnvelope);
		finalGridRange.intersect(new GeneralEnvelope(oldRange.toRectangle()));

		return new GridGeometry2D(new GeneralGridRange(finalGridRange),
				intersectionEnvelope);

	}
}
