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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import org.geotools.util.logging.Logging;
import org.geotools.resources.Classes;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A tile to be read by {@link MosaicImageReader}. Each tile must contains the following:
 * <p>
 * <ul>
 *   <li>An {@link ImageReader} instance. The same image reader is typically used for every tiles,
 *       but this is not mandatory (more image readers <em>may</em> improve performance at the
 *       expanse of more memory and {@linkplain ImageInputStream image input stream} retention).
 *       The {@linkplain #getInput input} will be assigned to the image reader before every tile
 *       to be read.</li>
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
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Tile implements Comparable<Tile> {
    /**
     * The tile manager that own this tile. Will be set by {@link TileManager} constructor
     * only, and should not be modified after that point.
     */
    TileManager manager;

    /**
     * The image reader to use. The same reader is typically given to every {@code Tile} objects
     * to be given to the same {@link MosaicImageReader} instance, but this is not mandatory.
     * However the same image reader should not be shared between two different
     * {@code MosaicImageReader} instances.
     */
    private final ImageReader reader;

    /**
     * The input to be given to the image reader. If the reader can not read that input
     * directly, it will be wrapped in an {@linkplain ImageInputStream image input stream}.
     * Note that this field must stay the <em>unwrapped</em> input. If the wrapped input is
     * wanted, use {@link ImageReader#getInput} instead.
     */
    private final Object input;

    /**
     * The image index of the tile to be read. This is often 0.
     */
    private final int imageIndex;

    /**
     * The pixel size relative to the finest pyramid level. If this tile is the
     * finest level, then the value shall be 1. Should never be 0 or negative.
     */
    private final int dx, dy;

    /**
     * The upper-left corner in the destination image. Should be considered as final, since
     * this class is supposed to be mostly immutable. However the value can be changed by
     * {@link #translate} before an instance is made public.
     */
    private int x, y;

    /**
     * The size of the image to be read, or 0 if not yet computed.
     */
    private int width, height;

    /**
     * Creates a tile for the given reader, input and origin. This constructor can be used when
     * the size of the image to be read by the supplied reader is unknown. This size will be
     * fetched automatically the first time {@link #getRegion} is invoked.
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
     * @param pixelSize
     *          Pixel size relative to the finest resolution in an image pyramid,
     *          or {@code null} if none. If non-null, width and height should be
     *          strictly positive.
     */
    public Tile(final ImageReader reader, final Object input, final int imageIndex,
                final Point origin, final Dimension pixelSize)
    {
        ensureNonNull("reader", reader);
        ensureNonNull("input",  input);
        ensureNonNull("origin", origin);
        checkImageIndex(imageIndex);
        this.reader     = reader;
        this.input      = input;
        this.imageIndex = imageIndex;
        this.x          = origin.x;
        this.y          = origin.y;
        if (pixelSize != null) {
            dx = pixelSize.width;
            dy = pixelSize.height;
            ensureValidPixelSize();
        } else {
            dx = dy = 1;
        }
    }

    /**
     * Creates a tile for the given reader, input and region. This constructor can be used when
     * the size of the image to be read by the supplied reader is known. It avoid the cost of
     * fetched the size from the reader when {@link #getRegion} will be invoked.
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
     * @param region
     *          The region in the destination image. The {@linkplain Rectangle#width width} and
     *          {@linkplain Rectangle#height height} should match the image size.
     * @param pixelSize
     *          Pixel size relative to the finest resolution in an image pyramid,
     *          or {@code null} if none. If non-null, width and height should be
     *          strictly positive.
     */
    public Tile(final ImageReader reader, final Object input, final int imageIndex,
                final Rectangle region, final Dimension pixelSize)
    {
        ensureNonNull("reader", reader);
        ensureNonNull("input",  input);
        ensureNonNull("region", region);
        if (region.isEmpty()) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RECTANGLE_$1, region));
        }
        checkImageIndex(imageIndex);
        this.reader     = reader;
        this.input      = input;
        this.imageIndex = imageIndex;
        this.x          = region.x;
        this.y          = region.y;
        this.width      = region.width;
        this.height     = region.height;
        if (pixelSize != null) {
            dx = pixelSize.width;
            dy = pixelSize.height;
            ensureValidPixelSize();
        } else {
            dx = dy = 1;
        }
    }

    /**
     * Ensures that the pixel size is strictly positive.
     */
    private void ensureValidPixelSize() {
        int n;
        if ((n=dx) < 1 || (n=dy) < 1) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NOT_GREATER_THAN_ZERO_$1, n));
        }
    }

    /**
     * Ensures that the given argument is non-null.
     */
    static void ensureNonNull(final String argument, final Object value) {
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
     * <p>
     * This method is invoked automatically by {@link MosaicImageReader} and should not needs
     * to be invoked directly. If an {@linkplain ImageInputStream image input stream} has been
     * created, it will be closed automatically when needed.
     *
     * @param seekForwardOnly If {@code true}, images and metadata may only be read
     *                        in ascending order from the input source.
     * @param ignoreMetadata  If {@code true}, metadata may be ignored during reads.
     * @return An image reader with its {@linkplain ImageReader#getInput input} set.
     * @throws IOException if the image reader can't be initialized.
     */
    protected ImageReader getPreparedReader(final boolean seekForwardOnly,
                                            final boolean ignoreMetadata)
            throws IOException
    {
        final ImageReader  reader = getReader();
        final Object        input = getInput();
        final Object currentInput = (manager != null) ? manager.getRawInput(reader) : null;
        /*
         * If the current reader input is suitable, we will keep it in order to preserve
         * any data that may be cached in the ImageReader instance. Only if the input is
         * not suitable, we will invoke ImageReader.setInput(...).
         */
        final boolean sameInput = Utilities.equals(input, currentInput);
        if ( !sameInput                                      ||
            ( getImageIndex() <  reader.getMinIndex())       ||
            (!seekForwardOnly && reader.isSeekForwardOnly()) ||
            (!ignoreMetadata  && reader.isIgnoringMetadata()))
        {
            Object actualInput = reader.getInput();
            reader.setInput(null); // Necessary for releasing the stream, in case it holds it.
            if (manager != null) {
                manager.setRawInput(reader, null); // For keeping the map consistent.
            }
            ImageInputStream stream = null;
            if (actualInput instanceof ImageInputStream) {
                stream = (ImageInputStream) actualInput;
            }
            final ImageReaderSpi spi = reader.getOriginatingProvider();
            if (spi == null || isValidInput(spi.getInputTypes(), input)) {
                // We are allowed to use the input directly. Closes the stream
                // as a paranoiac safety (it should not be opened anyway).
                if (stream != null) {
                    stream.close();
                }
                actualInput = input;
            } else {
                // We are not allowed to use the input directly. Creates a new input
                // stream, or reuse the previous one if it still useable.
                if (stream != null) {
                    if (sameInput) try {
                        stream.seek(0);
                    } catch (IndexOutOfBoundsException e) {
                        // We tried to reuse the same stream in order to preserve cached data, but it was
                        // not possible to seek to the begining. Closes it; we will open a new one later.
                        Logging.recoverableException(Tile.class, "getPreparedReader", e);
                        stream.close();
                        stream = null;
                    } else {
                        stream.close();
                        stream = null;
                    }
                }
                if (stream == null) {
                    stream = ImageIO.createImageInputStream(input);
                    if (stream == null) {
                        throw new FileNotFoundException(Errors.format(
                                ErrorKeys.FILE_DOES_NOT_EXIST_$1, input));
                    }
                }
                actualInput = stream;
            }
            reader.setInput(actualInput, seekForwardOnly, ignoreMetadata);
            if (manager != null) {
                manager.setRawInput(reader, input);
            }
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
     * Returns the pixel size relative to the finest level in an image pyramid.
     * This method never returns {@code null}.
     */
    public Dimension getPixelSize() {
        return new Dimension(dx, dy);
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
     * @return The region in the destination image.
     * @throws IOException if it was necessary to fetch the image dimension from the
     *         {@linkplain #getReader reader} and this operation failed.
     */
    public Rectangle getRegion() throws IOException {
        if (width == 0 && height == 0) {
            final ImageReader reader = getPreparedReader(true, true);
            width  = reader.getWidth (imageIndex);
            height = reader.getHeight(imageIndex);
        }
        return new Rectangle(x, y, width, height);
    }

    /**
     * Returns {@code true} if calls to {@link #getRegion}, {@link #intersects} and related
     * methods will be cheap. For internal usage by {@link TileManager#getTiles} only.
     */
    final boolean isGetRegionCheap() {
        return width != 0 || height != 0;
    }

    /**
     * Translates this tile. For internal usage by {@link TileBuilder} only. This method
     * shound not be invoked anymore after a {@code Tile} instance has been made public.
     */
    final void translate(final int dx, final int dy) {
        x += dx;
        y += dy;
    }

    /**
     * Returns {@code true} if this tile {@linkplain #getRegion region} intersects the given
     * rectangle.
     *
     * @throws IOException if it was necessary to fetch the image dimension from the
     *         {@linkplain #getReader reader} and this operation failed.
     */
    public boolean intersects(final Rectangle region) throws IOException {
        if (region.x + region.width <= x || region.y + region.height <= y) {
            // Cheap test before to invoke 'getRegion()', which may be costly.
            return false;
        }
        return getRegion().intersects(region);
    }

    /**
     * Compares two tiles for order. Tiles are sorted by increasing image index. If the image
     * index are the same, then they are sorted by increasing <var>y</var> coordinate first,
     * then by <var>x</var> coordinate.
     * <p>
     * This ordering allows efficient access for tiles that use the same
     * {@linkplain #getReader image reader} and {@linkplain #getInput input}.
     */
    public final int compareTo(final Tile other) {
        int c = imageIndex - other.imageIndex;
        if (c == 0) {
            c = y - other.y;
            if (c == 0) {
                c = x - other.x;
                if (c == 0) {
                    c = dy - other.dy;
                    if (c == 0) {
                        c = dx - other.dx;
                    }
                }
            }
        }
        return c;
    }

    /**
     * Compares this tile with the specified one for equality. The default implementation compares
     * every properties except the {@link #getRegion region} width and height, because they may be
     * expensive to compute.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object != null && object.getClass().equals(getClass())) {
            final Tile that = (Tile) object;
            if (x == that.x && y == that.y && dx == that.dx && dy == that.dy &&
                imageIndex == that.imageIndex &&
                Utilities.deepEquals(input,  that.input) &&
                Utilities.equals    (reader, that.reader))
            {
                // We don't compare width and height - see javadoc. We could compare them only
                // if already defined, but it could lead to inconsistent behavior (tiles equal
                // at some time, then not equal anymore later).
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a hash code value for this tile. The default implementation uses the
     * {@linkplain #getReader reader}, {@linkplain #getInput input} and {@linkplain
     * #getImageIndex image index}, which should be suffisient for uniquely distinguish
     * every tiles.
     */
    @Override
    public int hashCode() {
        return reader.hashCode() + Utilities.deepHashCode(input) + 37*imageIndex;
    }

    /**
     * Returns a string representation of this tile. The returned string is mostly for
     * debugging purpose and could change in any future version.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(this)).append('[');
        final ImageReader reader = getReader();
        if (reader != null) {
            final ImageReaderSpi spi = reader.getOriginatingProvider();
            if (spi != null) {
                final String[] formats = spi.getFormatNames();
                if (formats != null && formats.length != 0) {
                    buffer.append("reader=\"").append(formats[0]).append("\", ");
                }
            }
        }
        buffer.append("input=\"").append(Utilities.deepToString(getInput())).append("\", ");
        if (!isGetRegionCheap()) {
            final Point origin = getOrigin();
            buffer.append(  "x=").append(origin.x)
                  .append(", y=").append(origin.y);
        } else try {
            final Rectangle region = getRegion();
            buffer.append(  "x=")     .append(region.x)
                  .append(", y=")     .append(region.y)
                  .append(", width=") .append(region.width)
                  .append(", height=").append(region.height);
        } catch (IOException e) {
            // Should not happen since we checked that 'getRegion' should be easy.
            // If it happen anyway, put the exception message at the place where
            // coordinates were supposed to appear, so we can debug.
            buffer.append(e);
        }
        return buffer.append(']').toString();
    }
}
