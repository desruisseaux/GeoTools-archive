/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.coverage.processing.operation;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.factory.Hints;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author simone
 * 
 */
final class FilteredSubsampledGridCoverage2D extends GridCoverage2D {

	public FilteredSubsampledGridCoverage2D(RenderedOp image,
			GridCoverage2D sourceCoverage) {
		super(sourceCoverage.getName(), image, new GridGeometry2D(
				new GeneralGridRange(image), sourceCoverage.getEnvelope()),
				sourceCoverage.getSampleDimensions(),
				new GridCoverage[] { sourceCoverage }, sourceCoverage
						.getProperties());
	}

	static Coverage create(ParameterValueGroup parameters, Hints hints) {
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
		final Interpolation interpolation = (Interpolation) parameters
				.parameter("Interpolation").getValue();

		// /////////////////////////////////////////////////////////////////////
		//
		// Getting the source coverage
		//
		// /////////////////////////////////////////////////////////////////////
		final GridCoverage2D sourceCoverage = (GridCoverage2D) parameters
				.parameter("Source").getValue();
		final RenderedImage sourceImage = sourceCoverage.getRenderedImage();

		// /////////////////////////////////////////////////////////////////////
		//
		// preparing the parameters for the scale operation
		//
		// /////////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjFilteredSubsample = new ParameterBlockJAI(
				"FilteredSubsample");
		pbjFilteredSubsample.setParameter("scaleX", scaleX);
		pbjFilteredSubsample.setParameter("scaleY", scaleY);
		pbjFilteredSubsample.setParameter("qsFilterArray", qsFilter);
		pbjFilteredSubsample.setParameter("Interpolation", interpolation);
		pbjFilteredSubsample.addSource(sourceImage);

		// /////////////////////////////////////////////////////////////////////
		//
		// preparing the new gridgeometry
		//
		// /////////////////////////////////////////////////////////////////////
		return new FilteredSubsampledGridCoverage2D(OperationJAI.getJAI(hints).createNS(
				"FilteredSubsample", pbjFilteredSubsample, new RenderingHints(
						JAI.KEY_BORDER_EXTENDER, parameters.parameter(
								"BorderExtender").getValue())), sourceCoverage);

	}
}
