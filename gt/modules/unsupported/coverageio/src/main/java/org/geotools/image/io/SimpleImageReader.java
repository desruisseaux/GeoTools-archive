/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage; // For javadoc
import java.io.*; // Many imports, including some for javadoc only.
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.Iterator;

import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ComponentSampleModelJAI;
import javax.media.jai.util.Range;

import org.geotools.util.Logging;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.image.ComponentColorModelJAI;


/**
 * Base class for simple image decoders. "Simple" images are usually flat binary
 * or ASCII files with no meta-data and no color information. There pixel values
 * may be floating point values instead of integers.
 * <p>
 * This base class makes it easier to construct images from floating point values.
 * It provides default implementations for most {@link ImageReader} methods. Since
 * {@code SimpleImageReader} does not expect to know anything about image's color,
 * it uses a grayscale color space scaled to fit the range of values. Displaying
 * such an image may be very slow. Consequently, users who want to display image
 * are encouraged to change data type and color space with
 * <a href="http://java.sun.com/products/java-media/jai/">Java Advanced Imaging</a>
 * operators after reading.
 *
 * @since 2.1
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/coverage/src/main/java/org/geotools/image/io/SimpleImageReader.java $
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class SimpleImageReader extends ImageReader {
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
     * The stream to {@linkplain #close close} on {@link #setInput(Object,boolean,boolean)
     * setInput(...)}, {@link #reset} or {@link #dispose} method invocation. This stream is
     * typically an {@linkplain InputStream input stream} or a {@linkplain Reader reader}
     * created by {@link #getInputStream} or similar methods in subclasses.
     * <p>
     * This field is never equals to the user-specified {@linkplain #input input}, since the
     * usual {@link ImageReader} contract is to <strong>not</strong> close the user-provided
     * stream. It is set to a non-null value only if a stream has been created from an other
     * user object like {@link File} or {@link URL}.
     *
     * @see #getInputStream
     * @see org.geotools.image.io.text.TextImageReader#getReader
     * @see #close
     *
     * @since 2.4
     *
     * @todo The field type will be changed to {@link Closeable} when we will be allowed
     *       to compile for J2SE 1.5.
     */
    protected Object closeOnReset;

    /**
     * {@link #input} as an input stream, or {@code null} if none.
     *
     * @see #getInputStream
     */
    private InputStream stream;

    /**
     * The stream position when {@link #setInput} is invoked.
     */
    private long streamOrigin;

    /**
     * Constructs a new image reader.
     *
     * @param provider The {@link ImageReaderSpi} that is invoking this constructor,
     *        or {@code null} if none.
     */
    protected SimpleImageReader(final ImageReaderSpi provider) {
        super(provider);
    }

    /**
     * Sets the input source to use. Input may be one of the following object:
     * {@link File}, {@link URL}, {@link Reader} (for ASCII data), {@link InputStream} or
     * {@link ImageInputStream}. If {@code input} is {@code null}, then any currently
     * set input source will be removed.
     *
     * @param input           The input object to use for future decoding.
     * @param seekForwardOnly If {@code true}, images and metadata may only be read
     *                        in ascending order from this input source.
     * @param ignoreMetadata  If {@code true}, metadata may be ignored during reads.
     *
     * @see #getInput
     * @see #getInputStream
     */
    public void setInput(final Object  input,
                         final boolean seekForwardOnly,
                         final boolean ignoreMetadata)
    {
        closeSilently();
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (input instanceof ImageInputStream) {
            try {
                streamOrigin = ((ImageInputStream) input).getStreamPosition();
            } catch (IOException exception) {
                streamOrigin = 0;
                Logging.unexpectedException(LOGGER.getName(),
                        SimpleImageReader.class, "setInput", exception);
            }
        }
    }

    /**
     * Ensures that the specified image index is inside the expected range.
     * The expected range is {@link #minIndex minIndex} inclusive (initially 0)
     * to <code>{@link #getNumImages getNumImages}(false)</code> exclusive.
     *
     * @param  imageIndex Index to check for validity.
     * @throws IndexOutOfBoundsException if the specified index is outside the expected range.
     * @throws IOException If the operation failed because of an I/O error.
     *
     * @since 2.4
     */
    protected void checkImageIndex(final int imageIndex)
            throws IOException, IndexOutOfBoundsException
    {
        final int numImages = getNumImages(false);
        if (imageIndex < minIndex || (imageIndex >= numImages && numImages >= 0)) {
            throw new IndexOutOfBoundsException(Errors.format(ErrorKeys.VALUE_OUT_OF_BOUNDS_$3,
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
     *
     * @since 2.4
     */
    protected void checkBandIndex(final int imageIndex, final int bandIndex)
            throws IOException, IndexOutOfBoundsException
    {
        // Call 'getNumBands' first in order to call 'checkImageIndex'.
        final int numBands = getNumBands(imageIndex);
        if (bandIndex >= numBands || bandIndex < 0) {
            throw new IndexOutOfBoundsException(Errors.format(ErrorKeys.VALUE_OUT_OF_BOUNDS_$3,
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
        throw new IllegalStateException(Errors.format(ErrorKeys.NO_IMAGE_INPUT));
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
     * Returns metadata associated with the given image. Since many raw images
     * can't store metadata, the default implementation returns {@code null}.
     *
     * @throws IOException if an error occurs during reading.
     */
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return null;
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
     *       {@linkplain SimpleImageReader.Spi#getForcedImageType forced image type} to
     *       be applied to every images of this format, then this type is returned.</li>
     *   <li>Otherwise a default image type is computed with a {@linkplain BandedSampleModel
     *       banded sample model} of {@linkplain #getRawDataType raw data type} and a color
     *       model calibrated for the {@linkplain #getExpectedRange expected range} of sample
     *       values. Note that the later may be expensive since it may require a full scan of
     *       image data.</li>
     * </ul>
     *
     * @param imageIndex The index of the image to be queried.
     * @param numDstBand The number of bands, usually equals to {@link #getNumBands}
     *                   but not always. It can also be the number of destination bands
     *                   during a {@linkplain #read(int,ImageReadParam)} operation.
     * @return The image type (never {@code null}).
     * @throws IOException If an error occurs reading the format information from the input source.
     *
     * @see SimpleImageReader.Spi#getForcedImageType
     *
     * @since 2.4
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
        /*
         * Creates a default image type specifier.
         */
        final int dataType = getRawDataType(imageIndex);
        final int[] bankIndices = new int[numDstBand];
        final int[] bandOffsets = new int[numDstBand];
        for (int i=numDstBand; --i>=0;) {
            bankIndices[i] = i;
        }
        final ColorSpace colorSpace = getColorSpace(imageIndex, 0, numDstBand);
        if (USE_JAI_MODEL) {
            final ColorModel cm = new ComponentColorModelJAI(
                    colorSpace, null, false, false, Transparency.OPAQUE, dataType);
            return new ImageTypeSpecifier(cm, new ComponentSampleModelJAI(
                    dataType, 1, 1, 1, 1, bankIndices, bandOffsets));
        } else {
            return ImageTypeSpecifier.createBanded(
                    colorSpace, bankIndices, bandOffsets, dataType, false, false);
        }
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
     * may read image data, or just returns some raisonable range.
     *
     * @param  imageIndex The image index.
     * @param  bandIndex The band index. Valid index goes from {@code 0} inclusive
     *         to {@code getNumBands(imageIndex)} exclusive. Index are independent
     *         of any {@link ImageReadParam#setSourceBands} setting.
     * @return The expected range of values, or {@code null} if unknow.
     * @throws IOException If an error occurs reading the data information from the input source.
     */
    public abstract Range getExpectedRange(final int imageIndex, final int bandIndex)
            throws IOException;

    /**
     * Returns a default color space. Default implementation returns a
     * grayscale color space scaled to fit {@link #getExpectedRange}.
     *
     * @param  imageIndex The image index.
     * @param  bandIndex  The band index.
     * @param  numBands   The number of bands.
     * @return A default color space scaled to fit data.
     * @throws IOException if an input operation failed.
     */
    private ColorSpace getColorSpace(final int imageIndex,
                                     final int bandIndex,
                                     final int numBands)
            throws IOException
    {
        final int dataType = getRawDataType(imageIndex);
        if (dataType != DataBuffer.TYPE_BYTE) {
            final Range range = getExpectedRange(imageIndex, bandIndex);
            if (range!=null && Number.class.isAssignableFrom(range.getElementClass())) {
                final Number minimum = (Number) range.getMinValue();
                final Number maximum = (Number) range.getMaxValue();
                if (minimum != null && maximum != null) {
                    final float minValue = minimum.floatValue();
                    final float maxValue = maximum.floatValue();
                    if (minValue < maxValue && !Float.isInfinite(minValue) &&
                                               !Float.isInfinite(maxValue))
                    {
                        return new ScaledColorSpace(numBands, minValue, maxValue);
                    }
                }
            }
        }
        return ColorSpace.getInstance(ColorSpace.CS_GRAY);
    }

    /**
     * Convenience method returning the destination band for the specified source band.
     * If the specified source band is not to be read, then this method returns -1.
     *
     * @param sourceBands The {@linkplain ImageReadParam#getSourceBands source bands}
     *        to be read from the image file.
     * @param destinationBands The {@linkplain ImageReadParam#getDestinationBands destination bands}
     *        in which to store the values in the {@linkplain RenderedImage rendered image}.
     * @param band The source band number to be converted into a destination band number.
     * @return The destination band number, or -1 if none.
     */
    protected static int sourceToDestBand(final int[] sourceBands,
                                          final int[] destinationBands,
                                          final int   band)
    {
        if (sourceBands == null) {
            return (destinationBands != null) ? destinationBands[band] : band;
        }
        for (int i=0; i<sourceBands.length; i++) {
            if (sourceBands[i] == band) {
                return (destinationBands != null) ? destinationBands[i] : i;
            }
        }
        return -1;
    }

    /**
     * Convenience method returning the source band for the specified destination band.
     *
     * @param sourceBands The {@linkplain ImageReadParam#getSourceBands source bands}
     *        to be read from the image file.
     * @param destinationBands The {@linkplain ImageReadParam#getDestinationBands destination bands}
     *        in which to store the values in the {@linkplain RenderedImage rendered image}.
     * @param band The destination band number to be converted into a source band number.
     * @return The destination band number.
     * @throws IllegalArgumentException if no destination band was found for the given source band.
     */
    protected static int destToSourceBand(final int[] sourceBands,
                                          final int[] destinationBands,
                                          final int   band)
            throws IllegalArgumentException
    {
        if (destinationBands == null) {
            return (sourceBands != null) ? sourceBands[band] : band;
        }
        for (int i=0; i<destinationBands.length; i++) {
            if (destinationBands[i] == band) {
                return (sourceBands != null) ? sourceBands[i] : i;
            }
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                "band", new Integer(band)));
    }

    /**
     * Returns the stream length in bytes, or {@code -1} if unknown. This method checks the
     * {@linkplain #input input} type and invokes one of {@link File#length()},
     * {@link ImageInputStream#length()} ou {@link URLConnection#getContentLength()} method
     * accordingly.
     *
     * @return The stream length, or -1 is unknown.
     * @throws IOException if an I/O error occured.
     *
     * @since 2.4
     */
    protected long getStreamLength() throws IOException {
        final Object input = getInput();
        if (input instanceof ImageInputStream) {
            long length = ((ImageInputStream) input).length();
            if (length >= 0) {
                length -= streamOrigin;
            }
            return length;
        }
        if (input instanceof File) {
            return ((File) input).length();
        }
        if (input instanceof URL) {
            return ((URL) input).openConnection().getContentLength();
        }
        if (input instanceof URLConnection) {
            return ((URLConnection) input).getContentLength();
        }
        return -1;
    }

    /**
     * Returns the {@linkplain #input input} as an {@linkplain InputStream input stream} object.
     * If the input is already an input stream, it is returned unchanged. Otherwise this method
     * creates a new {@linkplain InputStream input stream} (usually <strong>not</strong>
     * {@linkplain BufferedInputStream buffered}) from {@link File}, {@link URL},
     * {@link URLConnection} or {@link ImageInputStream} inputs.
     * <p>
     * This method creates a new {@linkplain InputStream input stream} only when first invoked.
     * All subsequent calls will returns the same instance. Consequently, the returned stream
     * should never be closed by the caller. It may be {@linkplain #close closed} automatically
     * when {@link #setInput setInput(...)}, {@link #reset()} or {@link #dispose()} methods are
     * invoked.
     *
     * @return {@link #getInput} as an {@link InputStream}. This input stream is usually
     *         not {@linkplain BufferedInputStream buffered}.
     * @throws IllegalStateException if the {@linkplain #input input} is not set.
     * @throws IOException If the input stream can't be created for an other reason.
     *
     * @see #getInput
     * @see org.geotools.image.io.text.TextImageReader#getReader
     *
     * @since 2.4
     */
    protected InputStream getInputStream() throws IllegalStateException, IOException {
        if (stream == null) {
            final Object input = getInput();
            if (input == null) {
                throw new IllegalStateException(Errors.format(ErrorKeys.NO_IMAGE_INPUT));
            }
            if (input instanceof InputStream) {
                stream = (InputStream) input;
                closeOnReset = null; // We don't own the stream, so don't close it.
            } else if (input instanceof ImageInputStream) {
                stream = new InputStreamAdapter((ImageInputStream) input);
                closeOnReset = null; // We don't own the ImageInputStream, so don't close it.
            } else if (input instanceof String) {
                stream = new FileInputStream((String) input);
                closeOnReset = stream;
            } else if (input instanceof File) {
                stream = new FileInputStream((File) input);
                closeOnReset = stream;
            } else if (input instanceof URL) {
                stream = ((URL) input).openStream();
                closeOnReset = stream;
            } else if (input instanceof URLConnection) {
                stream = ((URLConnection) input).getInputStream();
                closeOnReset = stream;
            } else {
                throw new IllegalStateException(Errors.format(ErrorKeys.ILLEGAL_CLASS_$2,
                        Utilities.getShortClassName(input),
                        Utilities.getShortClassName(InputStream.class)));
            }
        }
        return stream;
    }

    /**
     * Closes the input stream created by {@link #getInputStream()}. This method does nothing
     * if the input stream is the {@linkplain #input input} instance given by the user rather
     * than a stream created by this class from a {@link File} or {@link URL} input.
     * <p>
     * This method is invoked automatically by {@link #setInput(Object,boolean,boolean)
     * setInput(...)}, {@link #reset}, {@link #dispose} or {@link #finalize} methods and
     * doesn't need to be invoked explicitly. It has protected access only in order to
     * allow overriding by subclasses.
     *
     * @throws IOException if an error occured while closing the stream.
     *
     * @see #closeOnReset
     *
     * @since 2.4
     */
    protected void close() throws IOException {
        if (closeOnReset != null) {
            // TODO: replace the remaining of this block by the following line
            //       when we will be allowed to compile for J2SE 1.5.
            //closeOnReset.close();
            if (closeOnReset instanceof InputStream) {
                ((InputStream) closeOnReset).close();
            }
            if (closeOnReset instanceof Reader) {
                ((Reader) closeOnReset).close();
            }
        }
        closeOnReset = null;
        stream = null;
    }

    /**
     * Invokes {@link #close} and log the exception if any. This method is invoked from
     * methods that do not allow {@link IOException} to be thrown. Since we will not use
     * the stream anymore after closing it, it should not be a big deal if an error occured.
     */
    private void closeSilently() {
        try {
            close();
        } catch (IOException exception) {
            Logging.unexpectedException(LOGGER.getName(), getClass(), "close", exception);
        }
    }

    /**
     * Restores the {@code SimpleImageReader} to its initial state. If an input stream were
     * created by a previous call to {@link #getInputStream}, it will be {@linkplain #close
     * closed} before to reset this reader.
     */
    //@Override
    public void reset() {
        closeSilently();
        super.reset();
    }

    /**
     * Allows any resources held by this reader to be released. If an input stream were created
     * by a previous call to {@link #getInputStream}, it will be {@linkplain #close closed}
     * before to dispose this reader.
     */
    //@Override
    public void dispose() {
        closeSilently();
        super.dispose();
    }

    /**
     * Closes the streams. This method is automatically invoked by the garbage collector.
     */
    //@Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }




    /**
     * Service provider interface (SPI) for {@link SimpleImageReader}s.
     *
     * @since 2.4
     * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/coverage/src/main/java/org/geotools/image/io/TextImageReader.java $
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static abstract class Spi extends ImageReaderSpi {
        /**
         * List of legal input types for {@link SimpleImageReader}.
         */
        private static final Class[] INPUT_TYPES = new Class[] {
            File.class,
            URL.class,
            URLConnection.class,
            InputStream.class,
            ImageInputStream.class,
            String.class // To be interpreted as file path.
        };

        /**
         * Constructs a new SPI for {@link SimpleImageReader}. This constructor
         * initializes the following fields to default values:
         *
         * <ul>
         *   <li>Image format names ({@link #names}):
         *       An array of lenght 1 containing the {@code name} argument.
         *
         *   <li>MIME type ({@link #MIMETypes}):
         *       An array of length 1 containing the {@code mime} argument.
         *
         *   <li>Input types ({@link #inputTypes}):
         *       {@link File}, {@link URL}, {@link InputStream} et {@link ImageInputStream}.</li>
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
            inputTypes = INPUT_TYPES;
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
         *   <li>For improving performances, since the default {@link SimpleImageReader}
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
         * @see SimpleImageReader#getRawImageType(int,int)
         */
        public ImageTypeSpecifier getForcedImageType(final int imageIndex) throws IOException {
            return null;
        }
    }
}
