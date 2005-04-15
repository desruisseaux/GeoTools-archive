/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage;

// J2SE dependencies
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// JAI dependencies
import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;
import javax.media.jai.registry.RenderedRegistryMode;

// Geotools dependencies
import org.geotools.coverage.grid.AbstractGridCoverage;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.image.DualRectIter;


/**
 * An image that contains transformed samples.   It may be sample values after their
 * transformation to geophyics values, or the converse. Images are created using the
 * <code>SampleTranscoder.CRIF</code> inner class, where "CRIF" stands for
 * {@link java.awt.image.renderable.ContextualRenderedImageFactory}. The image
 * operation name is "org.geotools.SampleTranscode".
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class SampleTranscoder extends PointOpImage {
    /**
     * The operation name.
     * <strong>NOTE:</strong> Class {@link org.geotools.coverage.grid.GridCoverage2D}
     * uses this name, but can't refer to this constant since it is in an other package.
     */
    public static final String OPERATION_NAME = "org.geotools.SampleTranscode";

    /**
     * Category lists for each bands.
     * The array length must matches the number of bands in source image.
     */
    private final CategoryList[] categories;
    
    /**
     * Constructs a new <code>SampleTranscoder</code>.
     *
     * @param image      The source image.
     * @param categories The category lists, one for each image's band.
     * @param hints      The rendering hints.
     */
    private SampleTranscoder(final RenderedImage  image,
                             final CategoryList[] categories,
                             final RenderingHints hints)
    {
        super(image, (ImageLayout)hints.get(JAI.KEY_IMAGE_LAYOUT), hints, false);
        this.categories = categories;
        if (categories.length != image.getSampleModel().getNumBands()) {
            // Should not happen, since SampleDimension$Descriptor has already checked it.
            throw new RasterFormatException(String.valueOf(categories.length));
        }
        permitInPlaceOperation();
    }
    
    /**
     * Computes one of the destination image tile.
     *
     * @todo There is two optimisations we could do here:
     *       <ul>
     *         <li>If source and destination are the same raster, then a single
     *             {@link WritableRectIter} object would be more efficient (the
     *             hard work is to detect if source and destination are the same).</li>
     *         <li>If the destination image is a single-banded, non-interleaved
     *             sample model, we could apply the transform directly in the
     *             {@link java.awt.image.DataBuffer}. We can even avoid to copy
     *             sample value if source and destination raster are the same.</li>
     *       </ul>
     *
     * @param sources  An array of length 1 with source image.
     * @param dest     The destination tile.
     * @param destRect the rectangle within the destination to be written.
     */
    protected void computeRect(final PlanarImage[] sources,
                               final WritableRaster   dest,
                               final Rectangle    destRect)
    {
        final PlanarImage source = sources[0];
        WritableRectIter iterator = RectIterFactory.createWritable(dest, destRect);
        if (true) {
            // TODO: Detect if source and destination rasters are the same. If they are
            //       the same, we should skip this block. Iteration will then be faster.
            iterator = DualRectIter.create(RectIterFactory.create(source, destRect), iterator);
        }
        int band=0;
        if (!iterator.finishedBands()) do {
            categories[band].transform(iterator);
            band++;
        }
        while (!iterator.nextBandDone());
        assert(band == categories.length) : band;
    }




    /////////////////////////////////////////////////////////////////////////////////
    ////////                                                                 ////////
    ////////        REGISTRATION OF "SampleTranscode" IMAGE OPERATION        ////////
    ////////                                                                 ////////
    /////////////////////////////////////////////////////////////////////////////////
    /**
     * The operation descriptor for the "SampleTranscode" operation. This operation can apply the
     * {@link GridSampleDimension#getSampleToGeophysics sampleToGeophysics} transform on all pixels
     * in all bands of an image. The transformations are supplied as a list of
     * {@link GridSampleDimension}s, one for each band. The supplied {@code GridSampleDimension}
     * objects describe the categories in the <strong>source</strong> image. The target image
     * will matches sample dimension
     *
     *     <code>{@link GridSampleDimension#geophysics geophysics}(!isGeophysics)</code>,
     *
     * where <code>isGeophysics</code> is the previous state of the sample dimension.
     */
    private static final class Descriptor extends OperationDescriptorImpl {
        /**
         * Construct the descriptor.
         */
        public Descriptor() {
            super(new String[][]{{"GlobalName",  OPERATION_NAME},
                                 {"LocalName",   OPERATION_NAME},
                                 {"Vendor",      "Geotools 2"},
                                 {"Description", "Transformation from sample to geophysics values"},
                                 {"DocURL",      "http://www.geotools.org/"},
                                 {"Version",     "1.0"}},
                  new String[]   {RenderedRegistryMode.MODE_NAME}, 1,
                  new String[]   {"sampleDimensions"},          // Argument names
                  new Class []   {GridSampleDimension[].class}, // Argument classes
                  new Object[]   {NO_PARAMETER_DEFAULT},        // Default values for parameters,
                  null // No restriction on valid parameter values.
            );
        }

        /**
         * Returns <code>true</code> if the parameters are valids. This implementation check
         * that the number of bands in the source image is equals to the number of supplied
         * sample dimensions, and that all sample dimensions has categories.
         *
         * @param modeName The mode name (usually "Rendered").
         * @param param The parameter block for the operation to performs.
         * @param message A buffer for formatting an error message if any.
         */
        protected boolean validateParameters(final String      modeName,
                                             final ParameterBlock param,
                                             final StringBuffer message)
        {
            if (!super.validateParameters(modeName, param, message)) {
                return false;
            }
            final RenderedImage source = (RenderedImage) param.getSource(0);
            final GridSampleDimension[] bands = (GridSampleDimension[]) param.getObjectParameter(0);
            final int numBands = source.getSampleModel().getNumBands();
            if (numBands != bands.length) {
                message.append(Resources.format(ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH_$3,
                         new Integer(numBands), new Integer(bands.length), "SampleDimension"));
                return false;
            }
            for (int i=0; i<numBands; i++) {
                if (bands[i].categories == null) {
                    message.append(Resources.format(ResourceKeys.ERROR_BAD_PARAMETER_$2,
                                           "sampleDimensions["+i+"].categories", null));
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * The {@link RenderedImageFactory} for the "SampleTranscode" operation.
     */
    private static final class CRIF extends CRIFImpl {
        /**
         * Creates a {@link RenderedImage} representing the results of an imaging
         * operation for a given {@link ParameterBlock} and {@link RenderingHints}.
         */
        public RenderedImage create(final ParameterBlock param,
                                    final RenderingHints hints)
        {
            final RenderedImage image = (RenderedImage) param.getSource(0);
            final GridSampleDimension[] bands = (GridSampleDimension[]) param.getObjectParameter(0);
            final CategoryList[] categories = new CategoryList[bands.length];
            for (int i=0; i<categories.length; i++) {
                categories[i] = bands[i].categories;
            }
            if (image instanceof SampleTranscoder) {
                final SampleTranscoder other = (SampleTranscoder) image;
                if (isInverse(categories, other.categories)) {
                    return other.getSourceImage(0);
                }
            }
            return new SampleTranscoder(image, categories, hints);
        }

        /**
         * Checks if all categories in <code>categories1</code> are
         * equals to the inverse of <code>categories2</code>.
         */
        private static boolean isInverse(final CategoryList[] categories1,
                                         final CategoryList[] categories2)
        {
            if (categories1.length != categories2.length) {
                return false;
            }
            for (int i=0; i<categories1.length; i++) {
                if (!categories1[i].equals(categories2[i].inverse)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Register the "SampleTranscode" image operation to the operation registry of
     * the specified JAI instance. This method is invoked by the static initializer
     * of {@link GridSampleDimension}.
     */
    public static void register(final JAI jai) {
        final OperationRegistry registry = jai.getOperationRegistry();
        try {
            registry.registerDescriptor(new Descriptor());
            registry.registerFactory(RenderedRegistryMode.MODE_NAME, OPERATION_NAME,
                                     "geotools.org", new CRIF());
        } catch (IllegalArgumentException exception) {
            final LogRecord record = Resources.getResources(null).getLogRecord(Level.SEVERE,
                   ResourceKeys.ERROR_CANT_REGISTER_JAI_OPERATION_$1, OPERATION_NAME);
            record.setSourceClassName("GridSampleDimension");
            record.setSourceMethodName("<classinit>");
            record.setThrown(exception);
            AbstractGridCoverage.LOGGER.log(record);
        }
    }
}
