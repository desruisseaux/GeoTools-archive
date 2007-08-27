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

import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.BufferedImage;

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
import org.geotools.image.io.metadata.Band;


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
     * If the distance between the minimum or maximum value and the "fill values" is greater
     * than {@value}, collapses the fill values to 0 and offsets the remaining values to the
     * range starting at 1. This is mostly an optimization for avoiding unecessary large index
     * color models, since the range between the minimum or maximum value and the fill values
     * is wasted space. We conservatively define a large threshold in order to reduce the amount
     * of cases were the users may be surprised by the compactage. This optimization can be
     * disabled by setting the threshold to a very large value like {@link Integer#MAX_VALUE}.
     */
    private static final int COMPACTAGE_THRESHOLD = 1024;

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
            throw new IndexOutOfBoundsException(indexOutOfBounds(imageIndex, minIndex, numImages));
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
            throw new IndexOutOfBoundsException(indexOutOfBounds(bandIndex, 0, numBands));
        }
    }

    /**
     * Formats an error message for an index out of bounds.
     *
     * @param index The index out of bounds.
     * @param lower The lower legal value, inclusive.
     * @param upper The upper legal value, exclusive.
     */
    private String indexOutOfBounds(final int index, final int lower, final int upper) {
        return getErrorResources().getString(ErrorKeys.VALUE_OUT_OF_BOUNDS_$3,
                new Integer(index), new Integer(lower), new Integer(upper-1));
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
     * <p>
     * Note that this method forces {@link #ignoreMetadata} to {@code false} for the time of
     * <code>{@linkplain #getImageMetadata getImageMetadata}(imageIndex)</code> execution,
     * because some image reader implementations need geographic metadata in order to infer
     * a valid {@linkplain ColorModel color model}.
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
     * Returns a collection of {@link ImageTypeSpecifier} containing possible image types to which
     * the given image may be decoded. The default implementation returns a singleton containing
     * <code>{@link #getRawImageType(int) getRawImageType}(imageIndex)</code>.
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
     * implementation delegates to the following:
     *
     * <blockquote><code>{@linkplain #getRawImageType(int,ImageReadParam,SampleConverter[])
     * getRawImageType}(imageIndex, {@linkplain #getDefaultReadParam()}, null);</code></blockquote>
     *
     * If this method needs to be overriden, consider overriding the later instead.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The image type (never {@code null}).
     * @throws IOException If an error occurs reading the format information from the input source.
     */
    //@Override
    public ImageTypeSpecifier getRawImageType(final int imageIndex) throws IOException {
        return getRawImageType(imageIndex, getDefaultReadParam(), null);
    }

    /**
     * Returns an image type specifier indicating the {@link SampleModel} and {@link ColorModel}
     * which most closely represents the "raw" internal format of the image. The default
     * implementation applies the following rules:
     *
     * <ol>
     *   <li><p>The {@linkplain Band#getValidRange range of expected values} and the
     *       {@linkplain Band#getNoDataValues pad values} are extracted from the
     *       {@linkplain #getGeographicMetadata geographic metadata}, if any.</p></li>
     *
     *   <li><p>If the given {@code parameters} argument is an instance of {@link GeographicImageReadParam},
     *       then the user-supplied {@linkplain GeographicImageReadParam#getPaletteName palette name}
     *       is fetched. Otherwise or if no palette name was explicitly set, then this method default
     *       to {@value GeographicImageReadParam#DEFAULT_PALETTE_NAME}. The palette name will be used
     *       in order to {@linkplain PaletteFactory#getColors read a predefined set of colors} (as
     *       RGB values) to be given to the {@linkplain IndexColorModel index color model}.</p></li>
     *
     *   <li><p>If the {@linkplain #getRawDataType raw data type} is {@link DataBuffer#TYPE_FLOAT
     *       TYPE_FLOAT} or {@link DataBuffer#TYPE_DOUBLE TYPE_DOUBLE}, then this method builds
     *       a {@linkplain PaletteFactory#getContinuousPalette continuous palette} suitable for
     *       the range fetched at step 1. The data are assumed <cite>geophysics</cite> values
     *       rather than some packed values. Consequently, the {@linkplain SampleConverter sample
     *       converters} will replace pad values by {@linkplain Float#NaN NaN} with no other
     *       changes.</p></li>
     *
     *   <li><p>Otherwise, if the {@linkplain #getRawDataType raw data type} is a unsigned integer type
     *       like {@link DataBuffer#TYPE_BYTE TYPE_BYTE} or {@link DataBuffer#TYPE_USHORT TYPE_USHORT},
     *       then this method builds an {@linkplain PaletteFactory#getPalette indexed palette} (i.e. a
     *       palette backed by an {@linkplain IndexColorModel index color model}) with just the minimal
     *       {@linkplain IndexColorModel#getMapSize size} needed for containing fully the range and the
     *       pad values fetched at step 1. The data are assumed <cite>packed</cite> values rather than
     *       geophysics values. Consequently, the {@linkplain SampleConverter sample converters} will
     *       be the {@linkplain SampleConverter#IDENTITY identity converter} except in the following
     *       cases:
     *       <ul>
     *         <li>The {@linkplain Band#getValidRange range of valid values} is outside the range
     *             allowed by the {@linkplain #getRawDataType raw data type} (e.g. the range of
     *             valid values contains negative integers).</li>
     *         <li>At least one {@linkplain Band#getNoDataValues pad value} is outside the range
     *             of values allowed by the {@linkplain #getRawDataType raw data type}.</li>
     *         <li>At least one {@linkplain Band#getNoDataValues pad value} is far away from the
     *             {@linkplain Band#getValidRange range of valid values} (for example 9999 while
     *             the range of valid values is [0..255]).</li>
     *       </ul>
     *       In the first case, the sample converter will shift the values to a strictly positive
     *       range and replace pad values by 0. In the last cases, this method will try to only
     *       replace the pad values by 0, without shifting the valid values if this shift can be
     *       avoided.</p></li>
     *
     *   <li><p>Otherwise, if the {@linkplain #getRawDataType raw data type} is a signed integer
     *       type like {@link DataBuffer#TYPE_SHORT TYPE_SHORT}, then this method builds an
     *       {@linkplain PaletteFactory#getPalette indexed palette} with the maximal {@linkplain
     *       IndexColorModel#getMapSize size} supported by the raw data type (note that this is
     *       memory expensive - typically 256 kilobytes). Negative values will be stored in their
     *       two's complement binary form in order to fit in the range of positive integers
     *       supported by the {@linkplain IndexColorModel index color model}.</p></li>
     * </ol>
     *
     * <b>Overriding this method</b>
     * Subclasses may override this method when a constant color {@linkplain Palette palette}
     * is wanted for a all images of some specific type, for example for all <cite>Sea Surface
     * Temperature</cite> (SST) from the same provider. A constant color palette facilitate the
     * visual comparaison of different images at different time. The example below creates such
     * hard-coded objects from constant values.
     *
     * <blockquote><code>
     * int minimum    = -2000; // </code>minimal expected value<code><br>
     * int maximum    = +2300; // </code>maximal expected value<code><br>
     * int fillValue  = -9999; // </code>Value for missing data<code><br>
     * String palette = "SST-Nasa";// </code>Named set of RGB colors<code><br>
     * converters[0] = {@linkplain SampleConverter#createOffset(double,double)
     * SampleConverter.createOffset}(1 - minimum, fillValue);<br>
     * return {@linkplain PaletteFactory#getDefault()}.{@linkplain PaletteFactory#getPalettePadValueFirst
     * getPalettePadValueFirst}(paletteName, maximum - minimum).{@linkplain Palette#getImageTypeSpecifier
     * getImageTypeSpecifier}();
     * </code></blockquote>
     *
     * @param imageIndex
     *              The index of the image to be queried.
     * @param parameters
     *              The user-supplied parameters, or {@code null}. Note: we recommand to supply
     *              {@link #getDefaultReadParam} instead of {@code null} since subclasses may
     *              override the later with default values suitable to a particular format.
     * @param  converters
     *              If non-null, an array where to store the converters created by this method.
     *              Those converters should be used by <code>{@linkplain #read(int,ImageReadParam)
     *              read}(imageIndex, parameters)</code> implementations for converting the values
     *              read in the datafile to values acceptable for the underling {@linkplain
     *              ColorModel color model}.
     * @return
     *              The image type (never {@code null}).
     * @throws IOException
     *              If an error occurs while reading the format information from the input source.
     *
     * @see #getRawDataType
     * @see #getDestination(int, ImageReadParam, int, int, SampleConverter[])
     */
    protected ImageTypeSpecifier getRawImageType(final int               imageIndex,
                                                 final ImageReadParam    parameters,
                                                 final SampleConverter[] converters)
            throws IOException
    {
        /*
         * Gets the minimal and maximal values allowed for the target image type.
         * Note that this is meanless for floating point types, so the values in
         * that case are arbitrary.
         *
         * The only integer types that are signed are SHORT (not to be confused with
         * USHORT) and INT. Other types like BYTE and USHORT are treated as unsigned.
         */
        final boolean isFloat;
        final long floor, ceil;
        final int dataType = getRawDataType(imageIndex);
        switch (dataType) {
            case DataBuffer.TYPE_UNDEFINED: // Actually we don't really know what to do for this case...
            case DataBuffer.TYPE_DOUBLE:    // Fall through since we can treat this case as float.
            case DataBuffer.TYPE_FLOAT: {
                isFloat = true;
                floor   = Long.MIN_VALUE;
                ceil    = Long.MAX_VALUE;
                break;
            }
            case DataBuffer.TYPE_INT: {
                isFloat = false;
                floor   = Integer.MIN_VALUE;
                ceil    = Integer.MAX_VALUE;
                break;
            }
            case DataBuffer.TYPE_SHORT: {
                isFloat = false;
                floor   = Short.MIN_VALUE;
                ceil    = Short.MAX_VALUE;
                break;
            }
            default: {
                isFloat = false;
                floor   = 0;
                ceil    = (1L << DataBuffer.getDataTypeSize(dataType)) - 1;
                break;
            }
        }
        /*
         * Extracts all informations we will need from the user-supplied parameters, if any.
         */
        final String paletteName;
        final int[]  sourceBands;
        final int[]  targetBands;
        final int    visibleBand;
        if (parameters != null) {
            sourceBands = parameters.getSourceBands();
            targetBands = parameters.getDestinationBands();
        } else {
            sourceBands = null;
            targetBands = null;
        }
        if (parameters instanceof GeographicImageReadParam) {
            final GeographicImageReadParam geoparam = (GeographicImageReadParam) parameters;
            paletteName = geoparam.getNonNullPaletteName();
            visibleBand = geoparam.getVisibleBand();
        } else {
            paletteName = GeographicImageReadParam.DEFAULT_PALETTE_NAME;
            visibleBand = 0;
        }
        final int numBands;
        if (sourceBands != null) {
            numBands = sourceBands.length;
        } else if (targetBands != null) {
            numBands = targetBands.length;
        } else {
            numBands = getNumBands(imageIndex);
        }
        /*
         * Computes a range of values for all bands, as the union in order to make sure that
         * we can stores every sample values. Also creates SampleConverters in the process.
         * The later is an opportunist action since we gather most of the needed information
         * during the loop.
         */
        NumberRange     allRanges        = null;
        NumberRange     visibleRange     = null;
        SampleConverter visibleConverter = SampleConverter.IDENTITY;
        double          maximumFillValue = 0; // Only in the visible band, and must be positive.
        final GeographicMetadata metadata = getGeographicMetadata(imageIndex);
        if (metadata != null) {
            final int maxBand = metadata.getNumBands();
            for (int i=0; i<numBands; i++) {
                int bandIndex = (sourceBands != null) ? sourceBands[i] : i;
                if (bandIndex < 0 || bandIndex >= maxBand) {
                    warningOccurred("getRawImageType", indexOutOfBounds(bandIndex, 0, maxBand));
                    if (sourceBands != null) {
                        continue; // Next bands may be okay.
                    } else {
                        break; // We are sure that next bands will not be better.
                    }
                }
                final Band band = metadata.getBand(bandIndex);
                final double[] fillValues = band.getNoDataValues();
                final NumberRange range = band.getValidRange();
                final double minimum, maximum;
                if (range != null) {
                    minimum = range.getMinimum();
                    maximum = range.getMaximum();
                    final double extent = maximum - minimum;
                    if (extent >= 0 && (isFloat || extent <= (ceil - floor))) {
                        allRanges = (allRanges != null) ? (NumberRange) allRanges.union(range) : range;
                    } else {
                        // Use range.getMin/MaxValue() because they may be integers rather than doubles.
                        warningOccurred("getRawImageType", Errors.format(ErrorKeys.BAD_RANGE_$2,
                                range.getMinValue(), range.getMaxValue()));
                        continue;
                    }
                } else {
                    minimum = Double.NaN;
                    maximum = Double.NaN;
                }
                // Converts the band index from 'source' space to 'destination' space.
                if (targetBands != null && bandIndex < targetBands.length) {
                    bandIndex = targetBands[bandIndex];
                }
                /*
                 * For floating point types, replaces pad values by NaN because the floating point
                 * numbers are typically used for geophysics data, so the raster is likely to be a
                 * "geophysics" view for GridCoverage2D. All other values are stored "as is"
                 * without any offset.
                 *
                 * For integer types, if the range of values from the source data file fits into
                 * the range of values allowed by the destination raster, we will use an identity
                 * converter. If the only required conversion is a shift from negative to positive
                 * values, creates an offset converter with pad values collapsed to 0.
                 */
                final SampleConverter converter;
                if (isFloat) {
                    converter = SampleConverter.createPadValuesMask(fillValues);
                } else {
                    final boolean rangeContainsZero = (minimum <= 0 && maximum >= 0);
                    boolean collapsePadValues = false;
                    if (fillValues != null) {
                        for (int j=0; j<fillValues.length; j++) {
                            double t = fillValues[j];
                            if (bandIndex == visibleBand && t > maximumFillValue) {
                                maximumFillValue = t;
                            }
                            if (t < floor || t > ceil) {
                                collapsePadValues = true;
                                continue;
                            }
                            /*
                             * Arbitrary optimization of memory usage: if there is a "large" empty
                             * space between the range of valid values and a pad value, collapses the
                             * pad values in order to avoid wasting the empty space. The threshold is
                             * arbitrary. Note that we don't perform this compactage if the range
                             * contains negative values, because no compactage is possible in this
                             * case (the IndexColorModel will consume 256 kilobytes anyway).
                             */
                            if (minimum >= 0) {
                                t = Math.max(Math.abs(minimum-t), Math.abs(t-maximum)); // May be NaN.
                                if (t > COMPACTAGE_THRESHOLD) { // Must exclude the NaN case.
                                    collapsePadValues = true;
                                }
                            }
                        }
                    }
                    if (minimum < floor || maximum > ceil) {
                        // The range of valid values is outside the range allowed by raw data type.
                        converter = SampleConverter.createOffset(1 - minimum, fillValues);
                    } else if (collapsePadValues) {
                        if (rangeContainsZero) {
                            // We need to collapse the pad values to 0, but it cause a clash
                            // with the range of valid values. So we also shift the later.
                            converter = SampleConverter.createOffset(1 - minimum, fillValues);
                        } else {
                            // We need to collapse the pad values and there is no clash.
                            converter = SampleConverter.createPadValuesMask(fillValues);
                        }
                    } else {
                        converter = SampleConverter.IDENTITY;
                    }
                }
                if (converters!=null && bandIndex>=0 && bandIndex<converters.length) {
                    converters[bandIndex] = converter;
                }
                if (bandIndex == visibleBand) {
                    visibleConverter = converter;
                    visibleRange = range;
                }
            }
        }
        /*
         * Creates a color palette suitable for the range of values in the visible band.
         * The case for floating points is the simpliest: we should not have any offset,
         * at most a replacement of pad values. In the case of integer values, we must
         * make sure that the indexed color map is large enough for containing both the
         * highest data value and the highest pad value.
         */
        if (visibleRange == null) {
            visibleRange = (allRanges != null) ? allRanges : new NumberRange(floor, ceil);
        }
        final PaletteFactory factory = PaletteFactory.getDefault();
        factory.setWarningLocale(locale);
        final Palette palette;
        if (isFloat) {
            assert visibleConverter.getOffset() == 0 : visibleConverter;
            palette = factory.getContinuousPalette(paletteName, (float) visibleRange.getMinimum(),
                    (float) visibleRange.getMaximum(), dataType, numBands, visibleBand);
        } else {
            final double offset = visibleConverter.getOffset();
            int lower = (int) Math.round(visibleRange.getMinimum() + offset);
            int upper = (int) Math.round(visibleRange.getMaximum() + offset);
            if (!visibleRange.isMinIncluded()) {
                lower++; // Must be inclusive
            }
            if (visibleRange.isMaxIncluded()) {
                upper++; // Must be exclusive
            }
            final int size = Math.max(upper, (int) Math.round(maximumFillValue) + 1);
            palette = factory.getPalette(paletteName, lower, upper, size, numBands, visibleBand);
        }
        return palette.getImageTypeSpecifier();
    }

    /**
     * Returns the data type which most closely represents the "raw" internal data of the image.
     * It should be a constant from {@link DataBuffer}. The default {@code GeographicImageReader}
     * implementation works better with the following types:
     *
     * {@link DataBuffer#TYPE_BYTE   TYPE_BYTE},
     * {@link DataBuffer#TYPE_SHORT  TYPE_SHORT},
     * {@link DataBuffer#TYPE_USHORT TYPE_USHORT} and
     * {@link DataBuffer#TYPE_FLOAT  TYPE_FLOAT}.
     *
     * The default implementation returns {@code TYPE_FLOAT} in every cases.
     * <p>
     * <strong>Handling of negative integer values</strong><br>
     * If the raw internal data contains negative values but this method still declares a unsigned
     * integer type ({@link DataBuffer#TYPE_BYTE} or {@link DataBuffer#TYPE_USHORT}), then the
     * values will be translated in order to fit in the range of strictly positive values. For
     * example if the raw internal data range from -23000 to +23000, then there is a choice:
     *
     * <ul>
     *   <li><p>If this method returns {@link DataBuffer#TYPE_SHORT}, then the data will be
     *       stored "as is" without transformation. However the {@linkplain IndexColorModel
     *       index color model} will have the maximal length allowed by 16 bits integers, with
     *       positive values in the [0 .. {@value Short#MAX_VALUE}] range and negative values
     *       wrapped in the [32768 .. 65535] range in two's complement binary form. The results
     *       is a color model consuming 256 kilobytes in every cases. The space not used by the
     *       [-23000 .. +23000] range (in the above example) is lost.</p></li>
     *
     *   <li><p>If this method returns {@link DataBuffer#TYPE_USHORT}, then the data will be
     *       translated to the smallest strictly positive range that can holds the data
     *       ([1..46000] for the above example). Value 0 is reserved for missing data. The
     *       result is a smaller {@linkplain IndexColorModel index color model} than the
     *       one used by untranslated data.</p></li>
     * </ul>
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The data type ({@link DataBuffer#TYPE_FLOAT} by default).
     * @throws IOException If an error occurs reading the format information from the input source.
     *
     * @see #getRawImageType(int, ImageReadParam, SampleConverter[])
     */
    protected int getRawDataType(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return DataBuffer.TYPE_FLOAT;
    }

    /**
     * Returns the buffered image to which decoded pixel data should be written. The image
     * is determined by inspecting the supplied parameters if it is non-null, as described
     * in the {@linkplain #getDestination(ImageReadParam,Iterator,int,int) super-class method}.
     * In the default implementation, the {@linkplain ImageTypeSpecifier image type specifier}
     * set is a singleton containing only the {@linkplain #getRawImageType(int,ImageReadParam,
     * SampleConverter[]) raw image type}.
     * <p>
     * Implementations of the {@link #read(int,ImageReadParam)} method should invoke this
     * method instead of {@link #getDestination(ImageReadParam,Iterator,int,int)}.
     *
     * @param  imageIndex The index of the image to be retrieved.
     * @param  parameters The parameter given to the {@code read} method.
     * @param  width      The true width of the image or tile begin decoded.
     * @param  height     The true width of the image or tile being decoded.
     * @param  converters If non-null, an array where to store the converters required
     *                    for converting decoded pixel data into stored pixel data.
     * @return The buffered image to which decoded pixel data should be written.
     *
     * @throws IOException If an error occurs reading the format information from the input source.
     *
     * @see #getRawImageType(int, ImageReadParam, SampleConverter[])
     */
    protected BufferedImage getDestination(final int imageIndex, final ImageReadParam parameters,
                            final int width, final int height, final SampleConverter[] converters)
            throws IOException
    {
        final Set spi = Collections.singleton(getRawImageType(imageIndex, parameters, converters));
        return getDestination(parameters, spi.iterator(), width, height);
    }

    /**
     * Returns a default parameter object appropriate for this format. The default
     * implementation constructs and returns a new {@link GeographicImageReadParam}.
     *
     * @return An {@code ImageReadParam} object which may be used.
     *
     * @todo Replace the return type by {@link GeographicImageReadParam} when we will
     *       be allowed to compile for J2SE 1.5.
     */
    //@Override
    public ImageReadParam getDefaultReadParam() {
        return new GeographicImageReadParam(this);
    }

    /**
     * Reads the image indexed by {@code imageIndex} using a default {@link ImageReadParam}.
     * This is a convenience method that calls <code>{@linkplain #read(int,ImageReadParam)
     * read}(imageIndex, {@linkplain #getDefaultReadParam})</code>.
     * <p>
     * The default Java implementation passed a {@code null} parameter. This implementation
     * passes the default parameter instead in order to improve consistency when a subclass
     * overrides {@link #getDefaultReadParam}.
     *
     * @param imageIndex the index of the image to be retrieved.
     * @return the desired portion of the image.
     *
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs during reading.
     */
    //@Override
    public BufferedImage read(final int imageIndex) throws IOException {
        return read(imageIndex, getDefaultReadParam());
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
     * Convenience method for logging a warning from the given method.
     */
    private void warningOccurred(final String method, final String message) {
        final LogRecord record = new LogRecord(Level.WARNING, message);
        record.setSourceClassName(GeographicImageReader.class.getName());
        record.setSourceMethodName(method);
        warningOccurred(record);
    }

    /**
     * To be overriden and made {@code protected} by {@link StreamImageReader} only.
     */
    void close() throws IOException {
        metadata = null;
    }
}
