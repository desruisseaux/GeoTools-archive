/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.image.io;

import java.util.Iterator;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;

import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.event.IIOReadWarningListener;

import org.geotools.util.NumberRange;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Locales;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.IndexedResourceBundle;
import org.geotools.image.io.metadata.GeographicMetadata;


/**
 * Base class for reader of geographic images. The default implementation assumes that only one
 * {@linkplain ImageTypeSpecifier image type} is supported (as opposed to the arbitrary number
 * allowed by the standard {@link ImageReader}). It also provides a default image type built
 * automatically from a color palette and a range of valid values.
 * <p>
 * More specifically, this class provides the following conveniences to implementors:
 *
 * <ul>
 *   <li><p>Provides default {@link #getNumImages} and {@link #getNumBands} implementations,
 *       which return 1. This default behavior matches simple image formats like flat binary
 *       files or ASCII files. Those methods need to be overrided for more complex image
 *       formats.</p></li>
 *
 *   <li><p>Provides {@link #checkImageIndex} and {@link #checkBandIndex} convenience methods.
 *       Those methods are invoked by most implementation of public methods. They perform their
 *       checks based on the informations provided by the above-cited {@link #getNumImages} and
 *       {@link #getNumBands} methods.</p></li>
 *
 *   <li><p>Provides default implementations of {@link #getImageTypes} and {@link #getRawImageType},
 *       which assume that only one {@linkplain ImageTypeSpecifier image type} is supported. The
 *       default image type is created from the informations provided by {@link #getRawDataType}
 *       and {@link #getImageMetadata}.</p></li>
 *
 *   <li><p>Provides {@link #getStreamMetadata} and {@link #getImageMetadata} default
 *       implementations, which return {@code null} as authorized by the specification.
 *       Note that subclasses should consider returning {@link GeographicMetadata} instances.</p></li>
 * </ul>
 * 
 * Images may be flat binary or ASCII files with no meta-data and no color information.
 * Their pixel values may be floating point values instead of integers. The default
 * implementation assumes floating point values and uses a grayscale color space scaled
 * to fit the range of values. Displaying such an image may be very slow. Consequently,
 * users who want to display image are encouraged to change data type and color space with
 * <a href="http://java.sun.com/products/java-media/jai/">Java Advanced Imaging</a>
 * operators after reading.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class GeographicImageReader extends ImageReader {
    /**
     * The logger to use for events related to this image reader.
     */
    public static final Logger LOGGER = Logger.getLogger("org.geotools.image.io");

    /**
     * Tells if we should use {@link javax.media.jai.ComponentSampleModelJAI}
     * instead of the more standard {@link java.awt.image.ComponentSampleModel}.
     * There is two problems with model provided with J2SE 1.4:
     * <p>
     * <ul>
     *   <li>As of J2SE 1.4.0, {@link ImageTypeSpecifier#createBanded} doesn't accept
     *       {@link DataBuffer#TYPE_FLOAT} and {@link DataBuffer#TYPE_DOUBLE} argument.</li>
     *   <li>As of JAI 1.1, operators don't accept Java2D's {@link java.awt.image.DataBufferFloat}
     *       and {@link java.awt.image.DataBufferDouble}. They require JAI's DataBuffer instead.</li>
     * </ul>
     * <p>
     * This flag is set to {@code true} for J2SE 1.4. It may be
     * changed to {@code false} with future J2SE and JAI versions.
     */
    static final boolean USE_JAI_MODEL = true;

    /**
     * Metadata for each images, or {@code null} if not yet created.
     */
    private transient GeographicMetadata[] metadata;

    /**
     * Constructs a new image reader.
     *
     * @param provider The {@link ImageReaderSpi} that is invoking this constructor,
     *        or {@code null} if none.
     */
    protected GeographicImageReader(final ImageReaderSpi provider) {
        super(provider);
        availableLocales = Locales.getAvailableLocales();
    }

    /**
     * Sets the input source to use.
     *
     * @param input           The input object to use for future decoding.
     * @param seekForwardOnly If {@code true}, images and metadata may only be read
     *                        in ascending order from this input source.
     * @param ignoreMetadata  If {@code true}, metadata may be ignored during reads.
     */
    //@Override
    public void setInput(final Object  input,
                         final boolean seekForwardOnly,
                         final boolean ignoreMetadata)
    {
        metadata = null; // Clears the cache
        super.setInput(input, seekForwardOnly, ignoreMetadata);
    }

    /**
     * Returns the resources for formatting error messages.
     */
    final IndexedResourceBundle getErrorResources() {
        return Errors.getResources(getLocale());
    }

    /**
     * Ensures that the specified image index is inside the expected range.
     * The expected range is {@link #minIndex minIndex} inclusive (initially 0)
     * to <code>{@link #getNumImages getNumImages}(false)</code> exclusive.
     *
     * @param  imageIndex Index to check for validity.
     * @throws IndexOutOfBoundsException if the specified index is outside the expected range.
     * @throws IOException If the operation failed because of an I/O error.
     */
    protected void checkImageIndex(final int imageIndex)
            throws IOException, IndexOutOfBoundsException
    {
        final int numImages = getNumImages(false);
        if (imageIndex < minIndex || (imageIndex >= numImages && numImages >= 0)) {
            throw new IndexOutOfBoundsException(getErrorResources().getString(
                    ErrorKeys.VALUE_OUT_OF_BOUNDS_$3,
                    new Integer(imageIndex), new Integer(minIndex), new Integer(numImages-1)));
        }
    }

    /**
     * Ensures that the specified band index is inside the expected range. The expected
     * range is 0 inclusive to <code>{@link #getNumBands getNumBands}(imageIndex)</code>
     * exclusive.
     *
     * @param  imageIndex The image index.
     * @param  bandIndex Index to check for validity.
     * @throws IndexOutOfBoundsException if the specified index is outside the expected range.
     * @throws IOException If the operation failed because of an I/O error.
     */
    protected void checkBandIndex(final int imageIndex, final int bandIndex)
            throws IOException, IndexOutOfBoundsException
    {
        // Call 'getNumBands' first in order to call 'checkImageIndex'.
        final int numBands = getNumBands(imageIndex);
        if (bandIndex >= numBands || bandIndex < 0) {
            throw new IndexOutOfBoundsException(getErrorResources().getString(
                    ErrorKeys.VALUE_OUT_OF_BOUNDS_$3,
                    new Integer(bandIndex), new Integer(0), new Integer(numBands-1)));
        }
    }

    /**
     * Returns the number of images available from the current input source.
     * The default implementation returns 1.
     *
     * @param  allowSearch If true, the number of images will be returned
     *         even if a search is required.
     * @return The number of images, or -1 if {@code allowSearch}
     *         is false and a search would be required.
     *
     * @throws IllegalStateException if the input source has not been set.
     * @throws IOException if an error occurs reading the information from the input source.
     */
    public int getNumImages(final boolean allowSearch) throws IllegalStateException, IOException {
        if (input != null) {
            return 1;
        }
        throw new IllegalStateException(getErrorResources().getString(ErrorKeys.NO_IMAGE_INPUT));
    }

    /**
     * Returns the number of bands available for the specified image.
     * The default implementation returns 1.
     *
     * @param  imageIndex  The image index.
     * @throws IOException if an error occurs reading the information from the input source.
     */
    public int getNumBands(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return 1;
    }

    /**
     * Returns metadata associated with the input source as a whole. Since many raw images
     * can't store metadata, the default implementation returns {@code null}.
     *
     * @throws IOException if an error occurs during reading.
     */
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    /**
     * Returns metadata associated with the given image. Since many raw images
     * can't store metadata, the default implementation returns {@code null}.
     *
     * @param  imageIndex The image index.
     * @return The metadata, or {@code null} if none.
     * @throws IOException if an error occurs during reading.
     */
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return null;
    }

    /**
     * Returns a helper parser for metadata associated with the given image. This implementation
     * invokes  <code>{@linkplain #getImageMetadata getImageMetadata}(imageIndex)</code>,  wraps
     * the result in a {@link GeographicMetadata} object if non-null and caches the result.
     *
     * @param  imageIndex The image index.
     * @return The geographic metadata, or {@code null} if none.
     * @throws IOException if an error occurs during reading.
     */
    public GeographicMetadata getGeographicMetadata(final int imageIndex) throws IOException {
        // Checks if a cached instance is available.
        if (metadata != null && imageIndex >= 0 && imageIndex < metadata.length) {
            final GeographicMetadata parser = metadata[imageIndex];
            if (parser != null) {
                return parser;
            }
        }
        // Checks if metadata are availables. If the user set 'ignoreMetadata' to 'true',
        // we override his setting since we really need metadata for creating a ColorModel.
        final IIOMetadata candidate;
        final boolean oldIgnore = ignoreMetadata;
        try {
            ignoreMetadata = false;
            candidate = getImageMetadata(imageIndex);
        } finally {
            ignoreMetadata = oldIgnore;
        }
        if (candidate == null) {
            return null;
        }
        // Wraps the IIOMetadata into a GeographicMetadata object,
        // if it was not already of the appropriate type.
        final GeographicMetadata parser;
        if (candidate instanceof GeographicMetadata) {
            parser = (GeographicMetadata) candidate;
        } else {
            parser = new GeographicMetadata(this);
            parser.mergeTree(candidate);
        }
        if (metadata == null) {
            metadata = new GeographicMetadata[Math.max(imageIndex+1, 4)];
        }
        if (imageIndex >= metadata.length) {
            metadata = (GeographicMetadata[]) XArray.resize(metadata, Math.max(imageIndex+1, metadata.length*2));
        }
        metadata[imageIndex] = parser;
        return parser;
    }

    /**
     * Returns a collection of {@link ImageTypeSpecifier} containing possible image
     * types to which the given image may be decoded. The default implementation
     * returns a singleton containing {@link #getRawImageType(int)}.
     *
     * @param  imageIndex The index of the image to be retrieved.
     * @return A set of suggested image types for decoding the current given image.
     * @throws IOException If an error occurs reading the format information from the input source.
     */
    public Iterator getImageTypes(final int imageIndex) throws IOException {
        return Collections.singleton(getRawImageType(imageIndex)).iterator();
    }

    /**
     * Returns an image type specifier indicating the {@link SampleModel} and {@link ColorModel}
     * which most closely represents the "raw" internal format of the image. The default
     * implementation delegates to {@link #getRawImageType(int,int)}.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The image type (never {@code null}).
     * @throws IOException If an error occurs reading the format information from the input source.
     */
    //@Override
    public ImageTypeSpecifier getRawImageType(final int imageIndex) throws IOException {
        return getRawImageType(imageIndex, getNumBands(imageIndex));
    }

    /**
     * Returns an image type specifier indicating the {@link SampleModel} and {@link ColorModel}
     * which most closely represents the "raw" internal format of the image. The default
     * implementation returns one of the following:
     * <p>
     * <ul>
     *   <li>If the {@linkplain #originatingProvider originating provider} declares a
     *       {@linkplain GeographicImageReader.Spi#getForcedImageType forced image type} to
     *       be applied to every images of this format, then this type is returned.</li>
     *   <li>Otherwise a default image type is computed with a {@linkplain BandedSampleModel
     *       banded sample model} of {@linkplain #getRawDataType raw data type} and a color
     *       model calibrated for the {@linkplain #getExpectedRange expected range} of sample
     *       values. Note that the later may be expensive since it may require a full scan of
     *       image data.</li>
     * </ul>
     *
     * @param imageIndex The index of the image to be queried.
     * @param numDstBand The number of bands, usually equals to {@link #getNumBands} but not always.
     *        It may be a smaller number if the user requested only a subset of available bands
     *        during a {@link #read(int,ImageReadParam)} operation.
     * @return The image type (never {@code null}).
     * @throws IOException If an error occurs while reading the format information from the input
     *         source.
     *
     * @see GeographicImageReader.Spi#getForcedImageType
     */
    protected ImageTypeSpecifier getRawImageType(final int imageIndex, final int numDstBand)
            throws IOException
    {
        if (originatingProvider instanceof Spi) {
            final Spi spi = (Spi) originatingProvider;
            final ImageTypeSpecifier candidate = spi.getForcedImageType(imageIndex);
            if (candidate != null && candidate.getNumBands() == numDstBand) {
                return candidate;
            }
        }
        final int bandIndex = 0; // TODO
        final int dataType = getRawDataType(imageIndex);
        final NumberRange range = getExpectedRange(imageIndex, bandIndex);
        return GeographicImageReadParam.getImageTypeSpecifier(dataType, range, numDstBand);
    }

    /**
     * Returns the data type which most closely represents the "raw"
     * internal data of the image. It should be a constant from
     * {@link DataBuffer}. Common types are {@link DataBuffer#TYPE_INT},
     * {@link DataBuffer#TYPE_FLOAT} and {@link DataBuffer#TYPE_DOUBLE}.
     * The default implementation returns {@code TYPE_FLOAT}.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The data type ({@code TYPE_FLOAT} by default).
     * @throws IOException If an error occurs reading the format information
     *         from the input source.
     */
    public int getRawDataType(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return DataBuffer.TYPE_FLOAT;
    }

    /**
     * Returns the expected range of values for a band. Implementation
     * may read image data, or just returns some raisonable range. The
     * default implementation try to get this informations from image
     * metadata.
     *
     * @param  imageIndex The image index.
     * @param  bandIndex The band index. Valid index goes from {@code 0} inclusive
     *         to {@code getNumBands(imageIndex)} exclusive. Index are independent
     *         of any {@link ImageReadParam#setSourceBands} setting.
     * @return The expected range of values, or {@code null} if unknow.
     * @throws IOException If an error occurs reading the data information from the input source.
     *
     * @deprecated Consider removing this method.
     */
    public NumberRange getExpectedRange(final int imageIndex, final int bandIndex)
            throws IOException
    {
        final GeographicMetadata parser = getGeographicMetadata(imageIndex);
        if (parser != null) {
            return parser.getBand(imageIndex).getValidRange();
        }
        return null;
    }

    /**
     * Flips the source region vertically. This method should be invoked straight after
     * {@link #computeRegions computeRegions} when the image to be read will be flipped
     * vertically, for example when the {@linkplain java.awt.image.Raster raster} sample
     * values are filled in a "{@code for (y=ymax-1; y>=ymin; y--)}" loop instead of
     * "{@code for (y=ymin; y<ymax; y++)}".
     * <p>
     * This method should be invoked as in the example below:
     *
     * <blockquote><pre>
     * computeRegions(param, srcWidth, srcHeight, image, srcRegion, destRegion);
     * flipVertically(param, srcHeight, srcRegion);
     * </pre></blockquote>
     *
     * @param param     The {@code param}     argument given to {@code computeRegions}.
     * @param srcHeight The {@code srcHeight} argument given to {@code computeRegions}.
     * @param srcRegion The {@code srcRegion} argument given to {@code computeRegions}.
     */
    protected static void flipVertically(final ImageReadParam param, final int srcHeight,
                                         final Rectangle srcRegion)
    {
        final int spaceLeft = srcRegion.y;
        srcRegion.y = srcHeight - (srcRegion.y + srcRegion.height);
        /*
         * After the flip performed by the above line, we still have 'spaceLeft' pixels left for
         * a downward translation.  We usually don't need to care about if, except if the source
         * region is very close to the bottom of the source image,  in which case the correction
         * computed below may be greater than the space left.
         *
         * We are done if there is no vertical subsampling. But if there is subsampling, then we
         * need an adjustment. The flipping performed above must be computed as if the source
         * region had exactly the size needed for reading nothing more than the last line, i.e.
         * 'srcRegion.height' must be a multiple of 'sourceYSubsampling' plus 1. The "offset"
         * correction is computed below accordingly.
         */
        if (param != null) {
            int offset = (srcRegion.height - 1) % param.getSourceYSubsampling();
            srcRegion.y += offset;
            offset -= spaceLeft;
            if (offset > 0) {
                // Happen only if we are very close to image border and
                // the above translation bring us outside the image area.
                srcRegion.height -= offset;
            }
        }
    }

    /**
     * Invoked when a warning occured. The default implementation make the following choice:
     * <p>
     * <ul>
     *   <li>If at least one {@linkplain IIOReadWarningListener warning listener}
     *       has been {@linkplain #addIIOReadWarningListener specified}, then the
     *       {@link IIOReadWarningListener#warningOccurred warningOccurred} method is
     *       invoked for each of them and the log record is <strong>not</strong> logged.</li>
     *
     *   <li>Otherwise, the log record is sent to the {@linkplain #LOGGER logger}.</li>
     * </ul>
     *
     * Subclasses may override this method if more processing is wanted, or for
     * throwing exception if some warnings should be considered as fatal errors.
     */
    public void warningOccurred(final LogRecord record) {
        if (warningListeners == null) {
            LOGGER.log(record);
        } else {
            processWarningOccurred(IndexedResourceBundle.format(record));
        }
    }

    /**
     * To be overriden and made {@code protected} by {@link StreamImageReader} only.
     */
    void close() throws IOException {
        metadata = null;
    }




    /**
     * Service provider interface (SPI) for {@link GeographicImageReader}s.
     *
     * @since 2.4
     * @source $URL$
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static abstract class Spi extends ImageReaderSpi {
        /**
         * Constructs a new SPI for {@link GeographicImageReader}. This constructor
         * initializes the following fields to default values:
         *
         * <ul>
         *   <li>Image format names ({@link #names}):
         *       An array of lenght 1 containing the {@code name} argument.
         *
         *   <li>MIME type ({@link #MIMETypes}):
         *       An array of length 1 containing the {@code mime} argument.
         * </ul>
         *
         * Others fields should be set by subclasses
         * (usually in their constructors).
         *
         * @param name Format name, or {@code null} to let {@link #names} unset.
         * @param mime MIME type, or {@code null} to let {@link #MIMETypes} unset.
         */
        public Spi(final String name, final String mime) {
            if (name != null) {
                names = new String[] {name};
            }
            if (mime != null) {
                MIMETypes = new String[] {mime};
            }
            vendorName = "Geotools";
        }

        /**
         * Returns the image type to apply to every images, or {@code null} if none. The
         * default implementation always returns {@code null}, which means that the image
         * type will be computed dynamically from image data. Subclasses may override this
         * method for two reasons:
         * <p>
         * <ul>
         *   <li>When a constant color palette is wanted for a whole data series, for example
         *       for all <cite>Sea Surface Temperature</cite> (SST) images from some specific
         *       provider. A constant color palette make it easier to compare different images
         *       at different time.</li>
         *   <li>For improving performances, since the default {@link GeographicImageReader}
         *       implementation may scan the whole image in order to get the minimum and
         *       maximum sample values, and build an appropriate color scale from that.</li>
         * </ul>
         * <p>
         * In the particular case of images using an {@linkplain java.awt.image.IndexColorModel
         * index color model}, the {@link Palette} class provides convenience methods for creating
         * the type specified. Subclasses can use a code similar to:
         *
         * <blockquote><pre>
         * return {@linkplain PaletteFactory}.getDefault()
         *         .{@linkplain PaletteFactory#getPalettePadValueFirst getPalettePadValueFirst}(PALETTE_NAME, MAXIMUM - MINIMUM)
         *         .{@linkplain Palette#getImageTypeSpecifier getImageTypeSpecifier}();
         * </pre></blockquote>
         *
         * where {@code PALETTE_NAME} is a string used in order to locate a file of RGB values,
         * and {@code MINIMUM} / {@code MAXIMUM} are some fixed range of legal sample values.
         *
         * @see GeographicImageReader#getRawImageType(int,int)
         */
        public ImageTypeSpecifier getForcedImageType(final int imageIndex) throws IOException {
            return null;
        }
    }
}
