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

// J2SE dependencies
import java.awt.RenderingHints;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.geotools.coverage.GridSampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterValueGroup;

// Geotools dependencies
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.factory.Hints;
import org.geotools.image.ImageWorker;
import org.geotools.resources.image.CoverageUtilities;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;


/**
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 */
final class FilteredSubsampledGridCoverage2D extends GridCoverage2D {
    /**
     * Serial number for cross-version compatibility.
     */
    private static final long serialVersionUID = 5274708130300017804L;

    /**
     * Creates a filtered subsampled image for the specified source coverage.
     *
     * @todo The {@link GridGeometry2D} calculation in this method is wrong!! The calculation
     *       should <strong>always</strong> be performed on {@link MathTransform}, never on
     *       a grid range - envelope pair when the math transform is available.
     */
	FilteredSubsampledGridCoverage2D(PlanarImage image,
			GridCoverage2D sourceCoverage, int actionTaken) {
		super(sourceCoverage.getName(), image, new GridGeometry2D(
				new GeneralGridRange(image), sourceCoverage.getEnvelope()),
				(GridSampleDimension[]) (actionTaken==1 ? null : sourceCoverage
						.getSampleDimensions().clone()),
				new GridCoverage[] { sourceCoverage }, sourceCoverage
						.getProperties());
	}

    /**
     * Creates a filtered subsampled image from the specified parameters.
     */
	static Coverage create(final ParameterValueGroup parameters,
			final Hints hints) {
		// /////////////////////////////////////////////////////////////////////
		//
		// Getting the input parameters
		//
		// /////////////////////////////////////////////////////////////////////
		final Integer scaleX = (Integer) parameters.parameter("scaleX")
				.getValue();
		final Integer scaleY = (Integer) parameters.parameter("scaleY")
				.getValue();
		final float qsFilter[] = (float[]) parameters
				.parameter("qsFilterArray").getValue();
		Interpolation interpolation = (Interpolation) parameters.parameter(
				"Interpolation").getValue();
		
		

		// /////////////////////////////////////////////////////////////////////
		//
		// Getting the source coverage
		//
		// /////////////////////////////////////////////////////////////////////
		GridCoverage2D sourceCoverage = (GridCoverage2D) parameters.parameter(
				"Source").getValue();
		RenderedImage sourceImage = sourceCoverage.getRenderedImage();
		
		// /////////////////////////////////////////////////////////////////////
		//
		// Do we need to explode the Palette to RGB(A)?
		//
		// /////////////////////////////////////////////////////////////////////
		final boolean isFilterPassEverything = qsFilter.length == 1
				&& qsFilter[0] == 1;
		int actionTaken = CoverageUtilities.prepareSourcesForGCOperation(
				sourceCoverage, interpolation,!isFilterPassEverything,hints);
		switch (actionTaken) {
		case 2:
			// in this case we need to go back the geophysics view of the
			// source coverage
			sourceCoverage = sourceCoverage.geophysics(true);
			sourceImage = PlanarImage.wrapRenderedImage(sourceCoverage
					.getRenderedImage());

			break;
		case 3:
			sourceCoverage = sourceCoverage.geophysics(false);
			sourceImage = sourceCoverage.getRenderedImage();
			break;
		}


		// /////////////////////////////////////////////////////////////////////
		//
		// Managing Hints for output coverage's layout purposes
		//
		// /////////////////////////////////////////////////////////////////////
		RenderingHints targetHints = ImageUtilities
			.getRenderingHints(sourceImage);
		if (targetHints == null) {
			targetHints = new RenderingHints(hints);
		} else if (hints != null) {
			targetHints.add(hints);
		}		
		ImageLayout layout = (ImageLayout) targetHints
				.get(JAI.KEY_IMAGE_LAYOUT);
		if (layout != null) {
			layout = (ImageLayout) layout.clone();
		} else {
			layout = new ImageLayout(sourceImage);
			layout.unsetTileLayout();
			// At this point, only the color model and sample model are left
			// valids.
		}
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
		// it is crucial to correctly manage the Hints to control the
		// replacement of IndexColorModel. It is worth to point out that setting
		// the JAI.KEY_REPLACE_INDEX_COLOR_MODEL hint to true is not enough to
		// force the operators to do an expansion.
		// If we explicitly provide an ImageLayout built with the source image
		// where the CM and the SM are valid. those will be employed overriding
		// a the possibility to expand the color model.
		if (actionTaken != 1)
			targetHints.add(ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL);
		else {
			targetHints.add(ImageUtilities.REPLACE_INDEX_COLOR_MODEL);
			layout.unsetValid(ImageLayout.COLOR_MODEL_MASK);
			layout.unsetValid(ImageLayout.SAMPLE_MODEL_MASK);
		}


		// /////////////////////////////////////////////////////////////////////
		//
		// preparing the parameters for the scale operation
		//
		// /////////////////////////////////////////////////////////////////////
		final ParameterBlock pbjFilteredSubsample = new ParameterBlock();
		pbjFilteredSubsample.addSource(sourceImage);
		pbjFilteredSubsample.add(scaleX).add(scaleY).add(qsFilter).add(
				interpolation).add(sourceImage);

		// /////////////////////////////////////////////////////////////////////
		//
		// preparing the new gridgeometry
		//
		// /////////////////////////////////////////////////////////////////////
		targetHints.add(new RenderingHints(JAI.KEY_BORDER_EXTENDER, parameters
				.parameter("BorderExtender").getValue()));
		targetHints
				.add(new RenderingHints(JAI.KEY_INTERPOLATION, interpolation));
		final JAI processor = OperationJAI.getJAI(targetHints);
		
		
		// /////////////////////////////////////////////////////////////////////
		//
		// Preparing the resulting coverage
		//
		// /////////////////////////////////////////////////////////////////////
		GridCoverage2D result;
		if (!processor.equals(JAI.getDefaultInstance()))
			result= new FilteredSubsampledGridCoverage2D(processor.createNS(
					"FilteredSubsample", pbjFilteredSubsample, targetHints),
					sourceCoverage, actionTaken);
		// no supplied processor
		else
			result= new FilteredSubsampledGridCoverage2D(JAI.create(
				"FilteredSubsample", pbjFilteredSubsample, targetHints),
				sourceCoverage, actionTaken);
		//now let's see what we need to do in order to clean things up
		if(actionTaken==2)
			return result.geophysics(false);
		if(actionTaken==3)
			return result.geophysics(true);
		return result;
	}
}
