/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2006, Geotools Project Managment Committee (PMC)
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

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.CannotCropException;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.resources.coverage.FeatureUtilities;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * This class is responsible for applying a crop operation to a source coverage
 * with a specified envelope.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
final class CroppedCoverage2D extends GridCoverage2D {
	/**
	 * Serial number for interoperability with different versions.
	 */
	private static final long serialVersionUID = -501742139906901754L;

	private final static PrecisionModel pm;

	private final static GeometryFactory gf;
	static {
		// getting default hints
		final Hints defaultHints = GeoTools.getDefaultHints();

		// check if someone asked us to use a specific precision model
		final Object o = defaultHints.get(Hints.JTS_PRECISION_MODEL);
		if (o != null)
			pm = (PrecisionModel) o;
		else {
			pm = new PrecisionModel();
		}
		gf = new GeometryFactory(pm, 0);

	}

	/**
	 * Convenience constructor for a {@link CroppedCoverage2D}.
	 * 
	 * @param name
	 *            for this {@link GridCoverage2D}.
	 * @param sourceRaster
	 *            is the raster that will be the back-end for this
	 *            {@link GridCoverage2D}.
	 * @param croppedGeometry
	 *            is the {@link GridGeometry2D} for this new
	 *            {@link CroppedCoverage2D}.
	 * @param source
	 *            is the original {@link GridCoverage2D}.
	 * @param actionTaken
	 *            it is used to do the necessary postprocessing for supporting
	 *            paletted images.
	 * @param rasterSpaceROI
	 *            in case we used the JAI's mosaic with a ROI this
	 *            {@link java.awt.Polygon} will hold the used roi.ù
	 * @param hints
     *          An optional set of hints, or {@code null} if none.
	 */
	private CroppedCoverage2D(InternationalString name,
			PlanarImage sourceRaster, GridGeometry2D croppedGeometry,
			GridCoverage2D source, int actionTaken,
			java.awt.Polygon rasterSpaceROI, Hints hints) {
		super(name.toString(), sourceRaster, croppedGeometry,
				(GridSampleDimension[]) (actionTaken == 1 ? null : source
						.getSampleDimensions().clone()),
				new GridCoverage[] { source },
				rasterSpaceROI != null ? Collections.singletonMap("GC_ROI",
						rasterSpaceROI) : null,hints);
	}

	/**
	 * Applies the band select operation to a grid coverage.
	 * 
	 * @param parameters
	 *            List of name value pairs for the parameters.
	 * @param sourceCoverage
	 *            is the source {@link GridCoverage2D} that we want to crop.
	 * @param hints
	 *            A set of rendering hints, or {@code null} if none.
	 * @param sourceGridToWorldTransform
	 *            is the 2d grid-to-world transform for the source coverage.
	 * @param scaleFactor
	 *            for the grid-to-world transform.
	 * @return The result as a grid coverage.
	 */
	static GridCoverage2D create(final ParameterValueGroup parameters,
			Hints hints, GridCoverage2D sourceCoverage,
			AffineTransform sourceGridToWorldTransform, double scaleFactor) {

		// /////////////////////////////////////////////////////////////////////
		//
		// Getting the source coverage and its child geolocation objects
		//
		// /////////////////////////////////////////////////////////////////////
		final RenderedImage sourceImage = sourceCoverage.getRenderedImage();
		final Envelope2D sourceEnvelope = sourceCoverage.getEnvelope2D();
		final GridGeometry2D sourceGridGeometry = ((GridGeometry2D) sourceCoverage
				.getGridGeometry());
		final GeneralGridRange sourceGridRange = (GeneralGridRange) sourceGridGeometry
				.getGridRange();
		final double rotationEsteem = XAffineTransform
				.getRotation(sourceGridToWorldTransform);

		// /////////////////////////////////////////////////////////////////////
		//
		// Now we try to understand if we have a simple scale and translate or a
		// more elaborated grid-to-world transformation n which case a simple
		// crop could not be enough, but we may need a more elaborated chain of
		// operation in order to do a good job.
		//
		// /////////////////////////////////////////////////////////////////////
		boolean isSimpleTransform = false;
		// check if there is a valid rotation value (it could be 0!)
		if (!Double.isNaN(rotationEsteem)) {
			// XXX make this parametric somehow
			if (Math.abs(rotationEsteem) < 1E-3)
				// there is no rotation
				isSimpleTransform = true;
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Managing Hints, especially for output coverage's layout purposes
		//
		// /////////////////////////////////////////////////////////////////////
		RenderingHints targetHints = ImageUtilities
				.getRenderingHints(sourceImage);
		if (targetHints == null) {
			targetHints = new RenderingHints(null);
		} 
		if (hints != null) {
			targetHints.add(hints);
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Interpolation
		//
		// /////////////////////////////////////////////////////////////////////
		Interpolation interpolation = (Interpolation) targetHints
				.get(JAI.KEY_INTERPOLATION);
		if (interpolation == null)
			interpolation = (Interpolation) ImageUtilities.NN_INTERPOLATION_HINT
					.get(JAI.KEY_INTERPOLATION);

		// /////////////////////////////////////////////////////////////////////
		//
		// Do we need to explode the Palette to RGB(A)?
		//
		// /////////////////////////////////////////////////////////////////////
		int actionTaken = 0;

		// //
		//
		// Layout
		//
		// //
		ImageLayout layout = (ImageLayout) targetHints
				.get(JAI.KEY_IMAGE_LAYOUT);
		if (layout != null) {
			layout = (ImageLayout) layout.clone();
		} else {
			layout = new ImageLayout(sourceImage);
			layout.unsetTileLayout();
			// At this point, only the color model and sample model are left
			// valid.
		}
		// crop will ignore minx, miny width and height
		if ((layout.getValidMask() & (ImageLayout.TILE_WIDTH_MASK
				| ImageLayout.TILE_HEIGHT_MASK
				| ImageLayout.TILE_GRID_X_OFFSET_MASK | ImageLayout.TILE_GRID_Y_OFFSET_MASK)) == 0) {
			layout.setTileGridXOffset(layout.getMinX(sourceImage));
			layout.setTileGridYOffset(layout.getMinY(sourceImage));
			final int width = layout.getWidth(sourceImage);
			final int height = layout.getHeight(sourceImage);
			if (layout.getTileWidth(sourceImage) > width)
				layout.setTileWidth(width);
			if (layout.getTileHeight(sourceImage) > height)
				layout.setTileHeight(height);
		}
		targetHints.put(JAI.KEY_IMAGE_LAYOUT, layout);

		// /////////////////////////////////////////////////////////////////////
		//
		// prepare the processor to use for this operation
		//
		// /////////////////////////////////////////////////////////////////////
		final JAI processor = OperationJAI.getJAI(targetHints);
		final boolean useProvidedProcessor = !processor.equals(JAI
				.getDefaultInstance());

		try {
			// /////////////////////////////////////////////////////////////////////
			//
			// Get the crop envelope and do your thing!
			//
			// /////////////////////////////////////////////////////////////////////
			final GeneralEnvelope cropEnvelope = (GeneralEnvelope) parameters
					.parameter("Envelope").getValue();
			// should we conserve the crop envelope?
			final Boolean conserveEnvelope = (Boolean) parameters.parameter(
					"ConserveEnvelope").getValue();

			// ////////////////////////////////////////////////////////////////////
			//
			// Do we actually need to crop?
			//
			// If the intersection envelope is empty or if the intersection
			// envelope is (almost) the same of the original envelope we just
			// return (with different return values).
			//
			// ////////////////////////////////////////////////////////////////////
			if (cropEnvelope.isEmpty())
				throw new CannotCropException(Errors
						.format(ErrorKeys.CANT_CROP));
			if (cropEnvelope.equals(sourceEnvelope, scaleFactor / 2.0, false))
				return sourceCoverage;

			// //
			//
			// build the new range by keeping into
			// account translation of grid geometry constructor for respecting
			// OGC PIXEL-IS-CENTER ImageDatum assumption.
			//
			// //
			final AffineTransform sourceWorldToGridTransform = sourceGridToWorldTransform
					.createInverse();
			// finalGridRange will hold the rectangular crop area at the end of
			// this operation
			final Rectangle2D finalGridRange = XAffineTransform.transform(
					sourceWorldToGridTransform, cropEnvelope.toRectangle2D(),
					null);
			// intersection with the original range in order to not try to crop
			// outside the image bounds
			XRectangle2D.intersect(finalGridRange, sourceGridRange
					.toRectangle(), finalGridRange);

			// ////////////////////////////////////////////////////////////////////
			//
			//
			// It is worth to point out that doing a crop the G2W transform
			// should not change while the envelope might change a little bit as
			// a consequence of the roundings of the underlying image datum
			// which uses integer factors. This can be avoided using the
			// conserveEnvelope param. If the user does not explicitly asks to
			// conserve the crop envelope we will conserve the original
			// grid-to-world transform.
			//
			// ////////////////////////////////////////////////////////////////////
			final GeneralGridRange newRange = new GeneralGridRange(
					new GeneralEnvelope(finalGridRange),PixelInCell.CELL_CORNER);
			// we do not have to crop in this case (should not really happen at
			// this time)
			if (newRange.equals(sourceGridRange) && isSimpleTransform)
				return sourceCoverage;

			// ////////////////////////////////////////////////////////////////////
			//
			// if I get here I have something to crop
			// using the world-to-grid transform for going from envelope to the
			// new grid range.
			//
			// ////////////////////////////////////////////////////////////////////
			final int xAxis = sourceGridGeometry.gridDimensionX;
			final int yAxis = sourceGridGeometry.gridDimensionY;
			final double minX = newRange.getLower(xAxis);
			final double minY = newRange.getLower(yAxis);
			final double width = newRange.getLength(xAxis);
			final double height = newRange.getLength(yAxis);
			assert width > 0;
			assert height > 0;

			// /////////////////////////////////////////////////////////////////////
			//
			// get the rendered image and crop it
			//
			// /////////////////////////////////////////////////////////////////////
			final PlanarImage croppedImage;
			final ParameterBlock pbj = new ParameterBlock();
			pbj.addSource(sourceImage);
			java.awt.Polygon rasterSpaceROI = null;
			if (isSimpleTransform) {

				// /////////////////////////////////////////////////////////////////////
				//
				// We got a simple scale and translate transform, JAI crop will
				// suffice.
				//
				// /////////////////////////////////////////////////////////////////////
				// executing the crop
				pbj.add(Float.valueOf((float) minX));
				pbj.add(Float.valueOf((float) minY));
				pbj.add(Float.valueOf((float) width));
				pbj.add(Float.valueOf((float) height));
				if (!useProvidedProcessor)
					croppedImage = JAI.create("Crop", pbj, targetHints);
				else
					croppedImage = processor.createNS("Crop", pbj, targetHints);
			} else {
				// /////////////////////////////////////////////////////////////////////
				//
				// We don't have a simple scale and translate transform, JAI
				// crop MAY NOT suffice. Let's decide whether or not we'll use
				// the Mosaic.
				//
				// /////////////////////////////////////////////////////////////////////
				// //
				//
				// Convert the crop envelope into a polygon and the use the
				// world-to-grid transform to get a ROI for the source coverage.
				//
				// //
				final Rectangle2D rect = cropEnvelope.toRectangle2D();
				final Coordinate[] coord = new Coordinate[] {
						new Coordinate(rect.getMinX(), rect.getMinY()),
						new Coordinate(rect.getMinX(), rect.getMaxY()),
						new Coordinate(rect.getMaxX(), rect.getMaxY()),
						new Coordinate(rect.getMaxX(), rect.getMinY()),
						new Coordinate(rect.getMinX(), rect.getMinY()) };
				final LinearRing ring = gf.createLinearRing(coord);
				final Polygon modelSpaceROI = new Polygon(ring, null, gf);

				// check that we have the same thing here
				assert modelSpaceROI.getEnvelopeInternal().equals(
						new ReferencedEnvelope(rect, cropEnvelope
								.getCoordinateReferenceSystem()));
				// //
				//
				// Now convert this polygon back into a shape for the source
				// raster space.
				//
				// //
				final List<Point2D> points = new ArrayList<Point2D>(5);
				rasterSpaceROI = FeatureUtilities.convertPolygonToPointArray(
						modelSpaceROI, ProjectiveTransform
								.create(sourceWorldToGridTransform), points);

				final double cropArea = ((GeneralGridRange) newRange)
						.toRectangle().width
						* ((GeneralGridRange) newRange).toRectangle().height;
				final double roiArea = Math.abs(area((Point2D[]) points
						.toArray(new Point2D[] {})));
				final double roiOpt = parameters.parameter("ROITolerance")
						.doubleValue();
				final String operatioName;
				if (roiOpt * cropArea > roiArea) {
					// executing the mosaic
					final ROIShape roi = new ROIShape(rasterSpaceROI);
					pbj.add(MosaicDescriptor.MOSAIC_TYPE_OVERLAY);
					pbj.add(null);
					pbj.add(new ROI[] { roi });
					pbj.add(null);
					pbj.add(CoverageUtilities
							.getBackgroundValues(sourceCoverage));
					// nice trick, we use the layout to do the actual crop
					Rectangle2D roiBounds = roi.getBounds2D();
					XRectangle2D.intersect(roiBounds, sourceGridRange
							.toRectangle(), roiBounds);
					layout.setMinX((int) (roiBounds.getMinX() + 0.5));
					layout.setWidth((int) (roiBounds.getWidth() + 0.5));
					layout.setMinY((int) (roiBounds.getMinY() + 0.5));
					layout.setHeight((int) (roiBounds.getHeight() + 0.5));

					operatioName = "Mosaic";
				} else {
					// executing the crop
					pbj.add(Float.valueOf((float) minX));
					pbj.add(Float.valueOf((float) minY));
					pbj.add(Float.valueOf((float) width));
					pbj.add(Float.valueOf((float) height));
					operatioName = "Crop";
				}

				if (!useProvidedProcessor)
					croppedImage = JAI.create(operatioName, pbj, targetHints);
				else
					croppedImage = processor.createNS(operatioName, pbj,
							targetHints);
			}
			if (conserveEnvelope.booleanValue()&&isSimpleTransform)
				return new CroppedCoverage2D(sourceCoverage.getName(),
						croppedImage, new GridGeometry2D(new GeneralGridRange(
								croppedImage), cropEnvelope), sourceCoverage,
						actionTaken, rasterSpaceROI,hints);
			else
				return new CroppedCoverage2D(sourceCoverage.getName(),
						croppedImage, new GridGeometry2D(new GeneralGridRange(
								croppedImage), sourceGridGeometry
								.getGridToCRS2D(PixelOrientation.CENTER),
								sourceCoverage.getCoordinateReferenceSystem()),
						sourceCoverage, actionTaken, rasterSpaceROI,hints);

		} catch (TransformException e) {
			throw new CannotCropException(Errors.format(ErrorKeys.CANT_CROP), e);
		} catch (NoninvertibleTransformException e) {
			throw new CannotCropException(Errors.format(ErrorKeys.CANT_CROP), e);
		}

	}

	/**
	 * Function to calculate the area of a polygon, according to the algorithm
	 * defined at http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/
	 * 
	 * @param polyPoints
	 *            array of points in the polygon
	 * @return area of the polygon defined by pgPoints
	 */
	private static double area(Point2D[] polyPoints) {
		int i, j, n = polyPoints.length;
		double area = 0;

		for (i = 0; i < n; i++) {
			j = (i + 1) % n;
			area += polyPoints[i].getX() * polyPoints[j].getY();
			area -= polyPoints[j].getX() * polyPoints[i].getY();
		}
		area /= 2.0;
		return (area);
	}
}
