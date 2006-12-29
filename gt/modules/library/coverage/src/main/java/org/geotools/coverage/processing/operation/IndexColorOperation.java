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

//J2SE dependencies
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.util.Arrays;

import javax.media.jai.ImageLayout;
import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.ParameterList;

import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operation2D;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.resources.image.CoverageUtilities;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterValueGroup;


/**
 * Operation applied only on image's colors. This operation work only for source
 * image using an {@link IndexColorModel}.
 * 
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Consider moving this class to the {@link org.geotools.coverage.processing} package.
 */
abstract class IndexColorOperation extends Operation2D {
    /**
     * Constructs an operation.
     */
    public IndexColorOperation(final DefaultParameterDescriptorGroup descriptor) {
        super(descriptor);
    }

    /**
     * Performs the color transformation. This method invokes the
     * {@link #transformColormap transformColormap(...)} method with current RGB
     * colormap, the source {@link SampleDimension} and the supplied parameters.
     * 
     * @param parameters The parameters.
     * @param hints Rendering hints (ignored in this implementation).
     *
     * @throws IllegalArgumentException if the candidate image do not use an
     *         {@link IndexColorModel}.
     */
    public Coverage doOperation(final ParameterValueGroup parameters, final Hints hints) {
        final GridCoverage2D source = (GridCoverage2D) parameters.parameter("Source").getValue();
        final GridCoverage2D visual = source.geophysics(false);
        final RenderedImage  image  = visual.getRenderedImage();
        final GridSampleDimension[] bands = visual.getSampleDimensions();
        final int visibleBand = CoverageUtilities.getVisibleBand(image);
        ColorModel model = image.getColorModel();
        boolean colorChanged = false;
        final int numBands = bands.length;
        for (int i=0; i<numBands; i++) {
            GridSampleDimension band = bands[i];
            final ColorModel candidate = (i == visibleBand) ?
                image.getColorModel() : band.getColorModel();
            if (!(candidate instanceof IndexColorModel)) {
                /*
                 * Source don't use an index color model.
                 */
                // TODO: localize this message.
                throw new IllegalArgumentException(
                        "Current implementation requires IndexColorModel");
            }
            final IndexColorModel colors = (IndexColorModel) candidate;
            final int mapSize = colors.getMapSize();
            final int[] ARGB = new int[mapSize];
            colors.getRGBs(ARGB);
            band = transformColormap(ARGB, i, band, parameters);
            if (!bands[i].equals(band)) {
                bands[i] = band;
                colorChanged = true;
            } else if (!colorChanged) {
                final int[] original = new int[mapSize];
                colors.getRGBs(original);
                colorChanged = Arrays.equals(original, ARGB);
            }
            if (i == visibleBand) {
                model = ColorUtilities.getIndexColorModel(ARGB, numBands, visibleBand);
            }
        }
        if (!colorChanged) {
            return source;
        }
        final int computeType = (image instanceof OpImage) ?
                ((OpImage) image).getOperationComputeType() : OpImage.OP_COMPUTE_BOUND;
        final ImageLayout layout = new ImageLayout().setColorModel(model);
        final RenderedImage newImage = new NullOpImage(image, layout, null, computeType);
        final GridCoverage2D target = FactoryFinder.getGridCoverageFactory(null).create(
                    visual.getName(), newImage,
                    visual.getCoordinateReferenceSystem2D(),
                    visual.getGridGeometry().getGridToCRS(),
                    bands, new GridCoverage[] { visual }, null);

        if (source != visual) {
            return target.geophysics(true);
        }
        return target;
    }

    /**
     * Transform the supplied RGB colors. This method is automatically invoked
     * by {@link #doOperation(ParameterList)} for each band in the source
     * {@link GridCoverage}. The {@code ARGB} array contains the ARGB values
     * from the current source and should be overridden with new ARGB values
     * for the destination image.
     * 
     * @param ARGB
     *            Alpha, Red, Green and Blue components to transform.
     * @param band
     *            The band number, from 0 to the number of bands in the image -1.
     * @param sampleDimension
     *            The sample dimension of band <code>band</code>.
     * @param parameters
     *            The user-supplied parameters.
     * @return A sample dimension identical to <code>sampleDimension</code>
     *         except for the colors. Subclasses may conservatively returns
     *         <code>sampleDimension</code>.
     */
    protected abstract GridSampleDimension transformColormap(final int[] ARGB, final int band,
            final GridSampleDimension sampleDimension, final ParameterValueGroup parameters);
}
