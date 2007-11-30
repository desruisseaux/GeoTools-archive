/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.image.io.mosaic;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A tile to be read by {@link MosaicImageReader}. Each tile must contains the following:
 * <p>
 * <ul>
 *   <li>An {@link ImageReader} instance. The same image reader is typically used for every tiles,
 *       but this is not mandatory. The {@linkplain #getInput input} will be assigned to the image
 *       reader before every tile to be read.</li>
 *
 *   <li>An input, typically a {@linkplain java.io.File file} or {@linkplain java.net.URL URL}.
 *       The input is typically different for every tile to be read, but this is not mandatory.
 *       For example different tiles could be stored at different {@linkplain #getImageIndex
 *       image index} in the same file.</li>
 *
 *   <li>An image index to be given to {@link ImageReader#read(int)} for reading the tile.</li>
 *
 *   <li>The upper-left corner in the destination image as a {@linkplain Point point}, or the
 *       upper-left corner together with the image size as a {@linkplain Rectangle rectangle}.</li>
 * </ul>
 * <p>
 * If the upper-left corner has been given as a {@linkplain Point point}, then the
 * {@linkplain ImageReader#getWidth width} and {@linkplain ImageReader#getHeight height}
 * will be obtained from the image reader when first needed, which may have a slight performance
 * cost. If the upper-left corner has been given as a {@linkplain Rectangle rectangle} instead,
 * then this performance cost is avoided but the user is responsible for the accuracy of the
 * information provided.
 * <p>
 * The tiles are not required to be arranged on a regular grid, but performances may be
 * better if they are.
 *
 * @since 2.5
 * @input $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Tile {
    /**
     * The image reader to use. The same reader is typically given to every {@code Tile} objects
     * to be given to the same {@link MosaicImageReader} instance, but this is not mandatory.
     * However the same image reader should not be shared between two different
     * {@code MosaicImageReader} instances.
     */
    private final ImageReader reader;

    /**
     * The input to be given to the image reader.
     */
    private final Object input;

    /**
     * If the reader can not read {@linkplain #input} directly, an image input stream
     * wrapping it. Otherwise, {@code null}.
     */
    private ImageInputStream stream;

    /**
     * The image index of the tile to be read. This is often 0.
     */
    private final int imageIndex;

    /**
     * The upper-left corner in the destination image.
     */
    private final int x, y;

    /**
     * The size of the image to be read, or 0 if not yet computed.
     */
    private int width, height;

    /**
     * Creates a tile for the given reader, input and origin. This constructor can be used when
     * the size of the image to be read by the supplied reader is unknown. This size will be
     * fetched automatically the first time {@link #getArea} is invoked.
     *
     * @param reader
     *          The image reader to use. The same reader is typically given to every {@code Tile}
     *          objects to be given to the same {@link MosaicImageReader} instance, but this is
     *          not mandatory. However the same image reader should not be shared between two
     *          different {@code MosaicImageReader} instances.
     * @param input
     *          The input to be given to the image reader.
     * @param imageIndex
     *          The image index of the tile to be read. This is often 0.
     * @param origin
     *          The upper-left corner in the destination image.
     */
    public Tile(final ImageReader reader, final Object input, final int imageIndex, final Point origin) {
        ensureNonNull("reader", reader);
        ensureNonNull("input",  input);
        ensureNonNull("origin", origin);
        checkImageIndex(imageIndex);
        this.reader     = reader;
        this.input      = input;
        this.imageIndex = imageIndex;
        this.x          = origin.x;
        this.y          = origin.y;
    }

    /**
     * Creates a tile for the given reader, input and area. This constructor can be used when
     * the size of the image to be read by the supplied reader is known. It avoid the cost of
     * fetched the size from the reader when {@link #getArea} will be invoked.
     *
     * @param reader
     *          The image reader to use. The same reader is typically given to every {@code Tile}
     *          objects to be given to the same {@link MosaicImageReader} instance, but this is
     *          not mandatory. However the same image reader should not be shared between two
     *          different {@code MosaicImageReader} instances.
     * @param input
     *          The input to be given to the image reader.
     * @param imageIndex
     *          The image index of the tile to be read. This is often 0.
     * @param area
     *          The area in the destination image. The {@linkplain Rectangle#width width} and
     *          {@linkplain Rectangle#height height} should match the image size.
     */
    public Tile(final ImageReader reader, final Object input, final int imageIndex, final Rectangle area) {
        ensureNonNull("reader", reader);
        ensureNonNull("input",  input);
        ensureNonNull("area",   area);
        if (area.isEmpty()) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RECTANGLE_$1, area));
        }
        checkImageIndex(imageIndex);
        this.reader     = reader;
        this.input      = input;
        this.imageIndex = imageIndex;
        this.x          = area.x;
        this.y          = area.y;
        this.width      = area.width;
        this.height     = area.height;
    }

    /**
     * Ensures that the given argument is non-null.
     */
    private static void ensureNonNull(final String argument, final Object value) {
        if (value == null) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NULL_ARGUMENT_$1, argument));
        }
    }

    /**
     * Ensures that the given image index is valid. The upper value ({@link Short#MAX_VALUE})
     * is completly arbitrary and checked only as a safety in order to avoid unraisonable high
     * values that are probably bugs.
     */
    private static void checkImageIndex(final int imageIndex) {
        if (imageIndex < 0 || imageIndex > Short.MAX_VALUE) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.INDEX_OUT_OF_BOUNDS_$1, imageIndex));
        }
    }

    /**
     * Returns {@code true} if the specified input is valid for the given array of input types.
     */
    private static boolean isValidInput(final Class<?>[] types, final Object input) {
        if (types != null) {
            for (final Class<?> type : types) {
                if (type!=null && type.isInstance(input)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the {@linkplain #getReader image reader} ready for reading an image from the
     * {@linkplain #getInput input}. If the image reader is already setup with the right input,
     * then the reader is returned immediately. Otherwise if the image reader can accept the
     * {@linkplain #getInput input} directly, than that input is given to the image reader.
     * Otherwise the input is wrapped in an {@linkplain ImageInputStream image input stream}.
     *
     * @param seekForwardOnly
     *          If {@code true}, images and metadata may only be read in ascending order
     *          from the input source.
     * @param ignoreMetadata
     *          If {@code true}, metadata may be ignored during reads.
     * @return
     *          An image reader with its {@linkplain ImageReader#getInput input} set.
     * @throws IOException if the image reader can't be initialized.
     */
    public ImageReader getPreparedReader(final boolean seekForwardOnly, final boolean ignoreMetadata)
            throws IOException
    {
        final ImageReader reader = getReader();
        final Object input = getInput();
        Object validInput = stream;
        if (validInput == null) {
            validInput = input;
        }
        /*
         * If the current reader input is suitable, we will keep it in order to preserve
         * any data that may be cached in the ImageReader instance. Only if the input is
         * not suitable, we will invoke ImageReader.setInput(...).
         */
        if (( validInput      != reader.getInput())          ||
            ( getImageIndex() <  reader.getMinIndex())       ||
            (!seekForwardOnly && reader.isSeekForwardOnly()) ||
            (!ignoreMetadata  && reader.isIgnoringMetadata()))
        {
            reader.setInput(null); // Necessary for releasing the stream, in case it holds it.
            if (stream != null) try {
                stream.seek(0);
            } catch (IndexOutOfBoundsException e) {
                // We tried to reuse the same stream in order to preserve cached data, but it was
                // not possible to seek to the begining. Closes it; we will open a new one later.
                recoverableException(Tile.class, "getPreparedReader", e);
                stream.close();
                stream = null;
            }
            final ImageReaderSpi spi = reader.getOriginatingProvider();
            if (spi == null || isValidInput(spi.getInputTypes(), input)) {
                // We are allowed to use the input directly. Closes the stream
                // as a paranoiac safety (it should not be opened anyway).
                if (stream != null) {
                    stream.close();
                    stream = null;
                }
                validInput = input;
            } else {
                // We are not allowed to use the input directly. Creates a new input
                // stream, or reuse the previous one if it still useable.
                if (stream == null) {
                    stream = ImageIO.createImageInputStream(input);
                }
                validInput = stream;
            }
            reader.setInput(validInput, seekForwardOnly, ignoreMetadata);
        }
        return reader;
    }

    /**
     * Returns the image reader.
     */
    public ImageReader getReader() {
        return reader;
    }

    /**
     * Returns the input to be given to the image reader.
     */
    public Object getInput() {
        return input;
    }

    /**
     * Returns the image index to be given to the image reader.
     */
    public int getImageIndex() {
        return imageIndex;
    }

    /**
     * Returns the upper-left corner in the destination image.
     */
    public Point getOrigin() {
        return new Point(x,y);
    }

    /**
     * Returns the upper-left corner in the destination image, with the image size.
     * If this tile has been created with the {@linkplain #Tile(ImageReader, Object,
     * Rectangle constructor expecting a rectangle), a copy of the specified rectangle
     * is returned. Otherwise the image {@linkplain ImageReader#getWidth width} and
     * {@linkplain ImageReader#getHeight height} are read from the image reader and
     * cached for future usage.
     *
     * @return The area in the destination image.
     * @throws IOException if it was necessary to fetch the image dimension from the
     *         {@linkplain #getReader reader} and this operation failed.
     */
    public Rectangle getArea() throws IOException {
        if (width == 0 && height == 0) {
            final ImageReader reader = getPreparedReader(true, true);
            int value;
            value = reader.getWidth (imageIndex); if (value > width ) width  = value;
            value = reader.getHeight(imageIndex); if (value > height) height = value;
        }
        return new Rectangle(x, y, width, height);
    }

    /**
     * Returns {@code true} if calls to {@link #getArea}, {@link #intersects} and related
     * methods will be cheap. For internal usage by {@link TileCollection#getTiles} only.
     */
    final boolean isGetAreaCheap() {
        return width != 0 || height != 0;
    }

    /**
     * Returns {@code true} if this tile {@linkplain #getArea area} intersects the given
     * rectangle.
     *
     * @throws IOException if it was necessary to fetch the image dimension from the
     *         {@linkplain #getReader reader} and this operation failed.
     */
    public boolean intersects(final Rectangle area) throws IOException {
        if (area.x + area.width <= x || area.y + area.height <= y) {
            // Cheap test before to invoke 'getArea()', which may be costly.
            return false;
        }
        return getArea().intersects(area);
    }

    /**
     * Invoked when an exception occured that can be safely ignored.
     */
    static void recoverableException(final Class<?> classe, final String method,
                                     final RuntimeException exception)
    {
        Utilities.recoverableException("org.geotools.image.io.mosaic", classe, method, exception);
    }
}
