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
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.geotools.io.TableWriter;
import org.geotools.util.logging.Logging;
import org.geotools.resources.Classes;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * A tile to be read by {@link MosaicImageReader}. Each tile must contains the following:
 * <p>
 * <ul>
 *   <li>An {@link ImageReaderSpi} instance. The same provider is typically used for every tiles,
 *       but this is not mandatory. An {@link ImageReader image reader} will be instantiated and
 *       the {@linkplain #getInput input} will be assigned to it before a tile is read.</li>
 *
 *   <li>An input, typically a {@linkplain File file}, {@linkplain URL} or {@linkplain URI}.
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
     * The provider to use. The same provider is typically given to every {@code Tile} objects
     * to be given to the same {@link TileManager} instance, but this is not mandatory.
     */
    private final ImageReaderSpi provider;

    /**
     * The input to be given to the image reader. If the reader can not read that input
     * directly, it will be wrapped in an {@linkplain ImageInputStream image input stream}.
     * Note that this field must stay the <em>unwrapped</em> input. If the wrapped input is
     * wanted, use {@link ImageReader#getInput} instead.
     */
    private final Object input;

    /**
     * The image index to be given to the image reader for reading this tile.
     */
    private final int imageIndex;

    /**
     * The pixel size relative to the finest pyramid level. If this tile is the
     * finest level, then the value shall be 1. Should never be 0 or negative,
     * except if its value has not yet been computed.
     * <p>
     * This field should be considered as final. It is not final only because
     * {@link RegionCalculator} may computes its value automatically.
     */
    private int dx, dy;

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
     * The "grid to real world" transform, used by {@link RegionCalculator} in order to compute
     * the {@linkplain #getRegion region} for this tile. This field is set to {@code null} once
     * {@link RegionCalculator} work is in progress, and set to a new value on completion.
     * <p>
     * <b>Note:</b> {@link RegionCalculator} really needs a new instance for each tile.
     * No caching allowed.
     */
    private AffineTransform gridToCRS;

    /**
     * Creates a tile for the given provider, input and origin. This constructor can be used when
     * the size of the image to be read by the supplied reader is unknown. This size will be
     * fetched automatically the first time {@link #getRegion} is invoked.
     *
     * @param provider
     *          The image reader provider to use. The same provider is typically given to every
     *          {@code Tile} objects to be given to the same {@link TileManager} instance, but
     *          this is not mandatory.
     * @param input
     *          The input to be given to the image reader.
     * @param imageIndex
     *          The image index to be given to the image reader for reading this tile.
     * @param origin
     *          The upper-left corner in the destination image.
     * @param pixelSize
     *          Pixel size relative to the finest resolution in an image pyramid,
     *          or {@code null} if none. If non-null, width and height should be
     *          strictly positive.
     */
    public Tile(final ImageReaderSpi provider, final Object input, final int imageIndex,
                final Point origin, final Dimension pixelSize)
    {
        ensureNonNull("provider", provider);
        ensureNonNull("input",    input);
        ensureNonNull("origin",   origin);
        checkImageIndex(imageIndex);
        this.provider   = provider;
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
     * Creates a tile for the given provider, input and region. This constructor can be used when
     * the size of the image to be read by the supplied reader is known. It avoid the cost of
     * fetching the size from the reader when {@link #getRegion} will be invoked.
     *
     * @param provider
     *          The image reader provider to use. The same provider is typically given to every
     *          {@code Tile} objects to be given to the same {@link TileManager} instance, but
     *          this is not mandatory.
     * @param input
     *          The input to be given to the image reader.
     * @param imageIndex
     *          The image index to be given to the image reader for reading this tile.
     * @param region
     *          The region in the destination image. The {@linkplain Rectangle#width width} and
     *          {@linkplain Rectangle#height height} should match the image size.
     * @param pixelSize
     *          Pixel size relative to the finest resolution in an image pyramid,
     *          or {@code null} if none. If non-null, width and height should be
     *          strictly positive.
     */
    public Tile(final ImageReaderSpi provider, final Object input, final int imageIndex,
                final Rectangle region, final Dimension pixelSize)
    {
        ensureNonNull("provider", provider);
        ensureNonNull("input",    input);
        ensureNonNull("region",   region);
        if (region.isEmpty()) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RECTANGLE_$1, region));
        }
        checkImageIndex(imageIndex);
        this.provider   = provider;
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
     * Creates a tile for the given provider, input and "<cite>grid to real world</cite>" transform.
     * This constructor can be used when the {@linkplain #getOrigin origin} of the image to be read
     * by the supplied reader is unknown. The origin and the pixel size will be computed
     * automatically when this tile will be given to a {@link TileManagerFactory}.
     * <p>
     * When using this constructor, the {@link #getOrigin}, {@link #getRegion} and
     * {@link #getPixelSize} methods will throw an {@link IllegalStateException} until this tile
     * has been given to a {@link TileManager}, which will compute those values automatically.
     *
     * @param provider
     *          The image reader provider to use. The same provider is typically given to every
     *          {@code Tile} objects to be given to the same {@link TileManager} instance, but
     *          this is not mandatory.
     * @param input
     *          The input to be given to the image reader.
     * @param imageIndex
     *          The image index to be given to the image reader for reading this tile.
     * @param region
     *          The tile region, or {@code null} if unknown. The (<var>x</var>,<var>y</var>)
     *          location of this region is typically (0,0). The definitive will be computed
     *          when this tile will be given to a {@link TileManagerFactory}.
     * @param gridToCRS
     *          The "<cite>grid to real world</cite>" transform.
     */
    public Tile(final ImageReaderSpi provider, final Object input, final int imageIndex,
                final Rectangle region, final AffineTransform gridToCRS)
    {
        ensureNonNull("provider",  provider);
        ensureNonNull("input",     input);
        ensureNonNull("gridToCRS", gridToCRS);
        checkImageIndex(imageIndex);
        this.provider   = provider;
        this.input      = input;
        this.imageIndex = imageIndex;
        if (region != null) {
            if (region.isEmpty()) {
                throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RECTANGLE_$1, region));
            }
            this.x      = region.x;
            this.y      = region.y;
            this.width  = region.width;
            this.height = region.height;
        }
        this.gridToCRS  = new AffineTransform(gridToCRS); // Really needs a new instance - no cache
    }

    /**
     * Creates a tile for the given region with default pixel size. This is a constructor is
     * provided for avoiding compile-tile ambiguity between null <cite>pixel size</cite> and
     * null <cite>affine transform</cite> (the former is legal, the later is not).
     *
     * @param provider
     *          The image reader provider to use. The same provider is typically given to every
     *          {@code Tile} objects to be given to the same {@link TileManager} instance, but
     *          this is not mandatory.
     * @param input
     *          The input to be given to the image reader.
     * @param imageIndex
     *          The image index to be given to the image reader for reading this tile.
     * @param region
     *          The region in the destination image. The {@linkplain Rectangle#width width} and
     *          {@linkplain Rectangle#height height} should match the image size.
     */
    public Tile(final ImageReaderSpi provider, final Object input, final int imageIndex, final Rectangle region) {
        this(provider, input, imageIndex, region, (Dimension) null);
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
     * Ensures that the pixel size is strictly positive. This method is invoked for checking
     * user-supplied arguments, as opposed to {@link #checkGeometryValidity} which checks if
     * the size has been computed. Both methods differ in exception type for that reason.
     */
    private void ensureValidPixelSize() throws IllegalArgumentException {
        int n;
        if ((n=dx) < 1 || (n=dy) < 1) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NOT_GREATER_THAN_ZERO_$1, n));
        }
    }

    /**
     * Checks if the origin, region, and pixel size can be returned. Throw an exception if this
     * tile has been {@linkplain #Tile(ImageReaderSpi, Object, int, Dimension, AffineTransform)
     * created without origin} and not yet processed by {@link TileManagerFactory}.
     * <p>
     * <b>Note:</b> It is not strictly necessary to synchronize this method since update to a
     * {@code int} field is atomic according Java language specification, the {@link #dx} and
     * {@link #dy} fields do not change anymore as soon as they have a non-zero value (this is
     * checked by setPixelSize(Dimension) implementation) and this method succed only if both
     * fields are set. Most callers are already synchronized anyway, except {@link TileManager}
     * constructor which invoke this method only has a sanity check. It is okay to conservatively
     * get the exception in situations where a synchronized block would not have thrown it.
     *
     * @todo Localize the exception message.
     */
    final void checkGeometryValidity() throws IllegalStateException {
        if (dx == 0 || dy == 0) {
            throw new IllegalStateException("Tile must be processed by TileManagerFactory.");
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
     * Returns a reader created by the {@linkplain #getImageReaderSpi provider} and setup for
     * reading the image from the {@linkplain #getInput input}. If a reader is already setup with
     * the right input, then it is returned immediately. Otherwise if the image reader can accept
     * the {@linkplain #getInput input} directly, than that input is given to the image reader.
     * Otherwise the input is wrapped in an {@linkplain ImageInputStream image input stream}.
     * <p>
     * This method is invoked automatically by {@link MosaicImageReader} and should not needs
     * to be invoked directly. If an {@linkplain ImageInputStream image input stream} has been
     * created, it will be closed automatically when needed.
     * <p>
     * Note that this method will typically returns an instance to be shared by every tiles in
     * the given {@link MosaicImageReader}. Callers should not {@linkplain ImageReader#dispose
     * dispose} the reader or change its configuration, unless the {@code mosaic} argument was
     * null.
     *
     * @param MosaicImageReader The caller, or {@code null} if none.
     * @param seekForwardOnly If {@code true}, images and metadata may only be read
     *                        in ascending order from the input source.
     * @param ignoreMetadata  If {@code true}, metadata may be ignored during reads.
     * @return An image reader with its {@linkplain ImageReader#getInput input} set.
     * @throws IOException if the image reader can't be initialized.
     */
    protected ImageReader getImageReader(final MosaicImageReader mosaic,
                                         final boolean seekForwardOnly,
                                         final boolean ignoreMetadata)
            throws IOException
    {
        final ImageReaderSpi provider = getImageReaderSpi();
        final ImageReader reader;
        final Object currentInput;
        if (mosaic != null) {
            reader = mosaic.getTileReader(provider);
            currentInput = mosaic.getRawInput(reader);
        } else {
            reader = provider.createReaderInstance();
            currentInput = null;
        }
        /*
         * If the current reader input is suitable, we will keep it in order to preserve
         * any data that may be cached in the ImageReader instance. Only if the input is
         * not suitable, we will invoke ImageReader.setInput(...).
         */
        final Object input = getInput();
        final boolean sameInput = Utilities.equals(input, currentInput);
        if ( !sameInput                                      ||
            ( getImageIndex() <  reader.getMinIndex())       ||
            (!seekForwardOnly && reader.isSeekForwardOnly()) ||
            (!ignoreMetadata  && reader.isIgnoringMetadata()))
        {
            Object actualInput = reader.getInput();
            reader.setInput(null); // Necessary for releasing the stream, in case it holds it.
            if (mosaic != null) {
                mosaic.setRawInput(reader, null); // For keeping the map consistent.
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
            if (mosaic != null) {
                mosaic.setRawInput(reader, input);
            }
        }
        return reader;
    }

    /**
     * Returns the image reader provider. This is the provider used for creating
     * the {@linkplain ImageReader image reader} to be used for reading this tile.
     *
     * @see ImageReaderSpi#createReaderInstance()
     */
    public ImageReaderSpi getImageReaderSpi() {
        return provider;
    }

    /**
     * Returns the input to be given to the image reader for reading this tile.
     *
     * @see ImageReader#setInput
     */
    public Object getInput() {
        return input;
    }

    /**
     * Returns a short string representation of the {@linkplain #getInput input}.
     * This is only informative and the string content may change.
     */
    public String getInputName() {
        final Object input = getInput();
        if (input instanceof File) {
            return ((File) input).getName();
        }
        if (input instanceof URI) {
            return ((URI) input).getPath();
        }
        if (input instanceof URL) {
            return ((URL) input).getPath();
        }
        if (input instanceof CharSequence) {
            return input.toString();
        }
        if (input != null) {
            return input.getClass().getSimpleName();
        }
        return Utilities.deepToString(input);
    }

    /**
     * Returns a format name inferred from the {@linkplain #getImageReaderSpi provider}.
     */
    public String getFormatName() {
        return toString(getImageReaderSpi());
    }

    /**
     * Returns the image index to be given to the image reader for reading this tile.
     *
     * @see ImageReader#read(int)
     */
    public int getImageIndex() {
        return imageIndex;
    }

    /**
     * If the user-supplied transform is waiting for a processing by {@link RegionCalculator},
     * returns it. Otherwise returns {@code null}. This method is for internal usage by
     * {@link RegionCalculator} only.
     * <p>
     * See {@link #checkGeometryValidity} for a note about synchronization. When {@code clear}
     * is {@code false} (i.e. this method is invoked just in order to get a hint), it is okay
     * to conservatively return a non-null value in situations where a synchronized block would
     * have returned {@code null}.
     *
     * @param clear If {@code true}, clears the {@link #gridToCRS} field before to return. This
     *              is a way to tell that processing is in progress, and also a safety against
     *              transform usage while it may become invalid.
     * @return The transform, or {@code null} if none. This method does not clone the returned
     *         value - {@link RegionCalculator} will reference and modify directly that transform.
     */
    final AffineTransform getPendingGridToCRS(final boolean clear) {
        assert !clear || Thread.holdsLock(this); // Lock required only if 'clear' is true.
        if (dx != 0 || dy != 0) {
            // No transform waiting to be processed.
            return null;
        }
        final AffineTransform gridToCRS = this.gridToCRS;
        if (clear) {
            this.gridToCRS = null;
        }
        return gridToCRS;
    }

    /**
     * Returns the "<cite>grid to real world</cite>" transform, or {@code null} if unknown.
     * This transform is derived from the value given to the constructor, but may not be
     * identical since it may have been {@linkplain AffineTransform#translate translated}
     * in order to get a uniform grid geometry for every tiles in a {@link TileManager}.
     *
     * @throws IllegalStateException If this tile has been {@linkplain #Tile(ImageReaderSpi,
     *         Object, int, Dimension, AffineTransform) created without origin} and not yet
     *         processed by {@link TileManagerFactory}.
     *
     * @see TileManager#getGridGeometry
     */
    public synchronized AffineTransform getGridToCRS() throws IllegalStateException {
        checkGeometryValidity();
        return gridToCRS; // No need to clone since TileManagerFactory assigned an immutable instance.
    }

    /**
     * Returns the pixel size relative to the finest level in an image pyramid.
     * This method never returns {@code null}, and the width & height shall
     * never be smaller than 1.
     *
     * @throws IllegalStateException If this tile has been {@linkplain #Tile(ImageReaderSpi,
     *         Object, int, Dimension, AffineTransform) created without origin} and not yet
     *         processed by {@link TileManagerFactory}.
     *
     * @see javax.imageio.ImageReadParam#setSourceSubsampling
     */
    public synchronized Dimension getPixelSize() throws IllegalStateException {
        checkGeometryValidity();
        return new Dimension(dx, dy);
    }

    /**
     * Invoked by {@link RegionCalculator} only. No other caller allowed.
     */
    final void setPixelSize(final Dimension pixelSize) throws IllegalStateException {
        assert Thread.holdsLock(this);
        if (dx != 0 || dy != 0) {
            throw new IllegalStateException(); // Should never happen.
        }
        dx = pixelSize.width;
        dy = pixelSize.height;
        ensureValidPixelSize();
    }

    /**
     * Returns the upper-left corner in the destination image.
     *
     * @throws IllegalStateException If this tile has been {@linkplain #Tile(ImageReaderSpi,
     *         Object, int, Dimension, AffineTransform) created without origin} and not yet
     *         processed by {@link TileManagerFactory}.
     *
     * @see javax.imageio.ImageReadParam#setDestinationOffset
     */
    public synchronized Point getOrigin() throws IllegalStateException {
        checkGeometryValidity();
        return new Point(x,y);
    }

    /**
     * Returns the upper-left corner in the destination image, with the image size. If this tile
     * has been created with the {@linkplain #Tile(ImageReader,Object,int,Rectangle,Dimension)
     * constructor expecting a rectangle}, a copy of the specified rectangle is returned.
     * Otherwise the image {@linkplain ImageReader#getWidth width} and
     * {@linkplain ImageReader#getHeight height} are read from the image reader and cached for
     * future usage.
     *
     * @return The region in the destination image.
     * @throws IllegalStateException If this tile has been {@linkplain #Tile(ImageReaderSpi,
     *         Object, int, Dimension, AffineTransform) created without origin} and not yet
     *         processed by {@link TileManagerFactory}.
     * @throws IOException if it was necessary to fetch the image dimension from the
     *         {@linkplain #getImageReader reader} and this operation failed.
     *
     * @see javax.imageio.ImageReadParam#setSourceRegion
     */
    public synchronized Rectangle getRegion() throws IllegalStateException, IOException {
        checkGeometryValidity();
        if (width == 0 && height == 0) {
            final ImageReader reader = getImageReader(null, true, true);
            width  = reader.getWidth (imageIndex);
            height = reader.getHeight(imageIndex);
            reader.dispose();
        }
        return new Rectangle(x, y, width, height);
    }

    /**
     * Invoked by {@link RegionCalculator} only. No other caller allowed.
     * <p>
     * Note that invoking this method usually invalidate {@link #gridToCRS}. Calls to this method
     * should be closely followed by calls to {@link #translate} for fixing the "gridToCRS" value.
     */
    final void setRegion(final Rectangle region) {
        assert Thread.holdsLock(this);
        x      = region.x;
        y      = region.y;
        width  = region.width;
        height = region.height;
    }

    /**
     * Translates this tile. For internal usage by {@link RegionCalculator} only.
     * This method is invoked slightly after {@link #setRegion} for final adjustment.
     *
     * @param dx The translation to apply on <var>x</var> values (often 0).
     * @param dy The translation to apply on <var>y</var> values (often 0).
     * @param gridToCRS The new "<cite>grid to real world</cite>" transform to use after this
     *        translation. Should be an immutable instance because it will not be cloned.
     */
    final synchronized void translate(final int dx, final int dy, final AffineTransform gridToCRS) {
        x += dx;
        y += dy;
        this.gridToCRS = gridToCRS;
    }

    /**
     * Returns {@code true} if this tile can be used for reading an image with the given
     * subsampling. This method always returns {@code true} if the given subsampling are
     * zero. In principle, subsampling can't be zero. But {@link TileManager} uses that
     * value for iterating over all tiles.
     */
    final boolean canSubsample(final int xSubsampling, final int ySubsampling) {
        return (xSubsampling % dx) == 0 && (ySubsampling % dy) == 0;
    }

    /**
     * Returns the amount of pixels in this tile that would be useless if reading the given region
     * at the given subsampling. This method is invoked by {@link TileManager} when two or more
     * tile overlaps, in order to choose the tiles that would minimize the amount of pixels to
     * read. The default implementation computes the amount of tile pixels skipped because of
     * subsampling, added to the amount of pixels outside the region, including the pixels below
     * the bottom. The later is conservative since many file formats will stop reading as soon as
     * they reach the region bottom. Subclasses can override this method in order to alter this
     * calculation if they are sure that pixels below the region have no disk seed cost.
     *
     * @param  region The region to read.
     * @param  sourceXSubsampling The number of columns to advance between pixels.
     *         Must be strictly positive (not zero).
     * @param  sourceYSubsampling The number of rows to advance between pixels.
     *         Must be strictly positive (not zero).
     * @return The amount of pixels which would be unused if the reading was performed on this
     *         tile. Smaller number is better.
     * @throws IOException if it was necessary to fetch the image dimension from the
     *         {@linkplain #getImageReader reader} and this operation failed.
     */
    protected int countWastedPixels(Rectangle region, int xSubsampling, int ySubsampling)
            throws IOException
    {
        if (!canSubsample(xSubsampling, ySubsampling)) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                    "subsampling", "(" + xSubsampling + ',' + ySubsampling + ')'));
        }
        final Rectangle current = getRegion();
        region = current.intersection(region);
        region.width  /= dx; current.width  /= dx; xSubsampling /= dx;
        region.height /= dy; current.height /= dy; ySubsampling /= dy;
        int count;
        count  = region.width  - (region.width  / xSubsampling);
        count += region.height - (region.height / ySubsampling);
        count += (current.height - region.height) * current.width;
        count += (current.width  - region.width)  * region.height; // Really 'region', not 'current'
        return count;
    }

    /**
     * Converts {@link URL} to {@link URI} and {@link CharSequence} to {@link String} for
     * comparaison purpose. {@link File}, {@link URI} and {@link String} are not converted
     * because they are already {@linkplain Comparable comparable}.
     */
    private static Object toComparable(Object input) {
        if (input instanceof URL) try {
            input = ((URL) input).toURI();
        } catch (URISyntaxException exception) {
            // Ignores - we will keep it as a URL. Logs with "compare" as source method
            // name, since it is the public API that invoked this private method.
            Logging.recoverableException(Tile.class, "compare", exception);
        } else if (input instanceof CharSequence) {
            input = input.toString();
        }
        return input;
    }

    /**
     * Tries to converts the given input into something that can be compared to the given base.
     * Returns the input unchanged if this method doesn't know how to convert it.
     */
    private static Object toCompatible(Object input, final Object target) {
        if (target instanceof URI) {
            if (input instanceof File) {
                input = ((File) input).toURI();
            }
        } else if (target instanceof String) {
            if (input instanceof File || input instanceof URI) {
                input = input.toString();
            }
        }
        return input;
    }

    /**
     * Compares two inputs for order. {@link String}, {@link File} and {@link URI} are comparable.
     * {@link URL} are not but can be converted to {@link URI} for comparaison purpose.
     */
    @SuppressWarnings("unchecked")
    private static int compareInputs(Object input1, Object input2) {
        if (Utilities.equals(input1, input2)) {
            return 0;
        }
        input1 = toComparable(input1);
        input2 = toComparable(input2); // Must be before 'toCompatible'.
        input1 = toCompatible(input1, input2);
        input2 = toCompatible(input2, input1);
        if (input1 instanceof Comparable && input1.getClass().isInstance(input2)) {
            return ((Comparable) input1).compareTo(input2);
        }
        if (input2 instanceof Comparable && input2.getClass().isInstance(input1)) {
            return ((Comparable) input2).compareTo(input1);
        }
        int c = input1.getClass().getName().compareTo(input2.getClass().getName());
        if (c != 0) {
            return c;
        }
        /*
         * Following is an unconvenient comparaison criterion, but this fallback should never
         * occurs in typical use cases. We use it on a "better than nothing" basis. It should
         * be consistent in a given running JVM, but it not likely to be consistent when comparing
         * the same tiles in two different JVM executions. In addition there is also a slight risk
         * that this code returns 0 while we would like to return a non-zero value.
         */
        return System.identityHashCode(input2) - System.identityHashCode(input1);
    }

    /**
     * Compares two tiles for order. Tiles are sorted by {@linkplain #getInput input} first,
     * then increasing {@linkplain #getImageIndex image index}, then increasing <var>y</var>
     * coordinate, then increasing <var>x</var> coordinate.
     * <p>
     * This ordering allows efficient access for tiles that use the same
     * {@linkplain #getImageReader image reader} and {@linkplain #getInput input}.
     * <p>
     * This method is consistent with {@link #equals} in the most common case where, for every
     * tiles to be compared (typically every tiles given to a {@link TileManager} instance),
     * inputs are of the same kind (preferrably {@link File}, {@link URL}, {@link URI} or
     * {@link String}), and there is no duplicated ({@linkplain #getInput input},
     * {@linkplain #getImageIndex image index}) pair.
     */
    public final int compareTo(final Tile other) {
        int c = compareInputs(input, other.input);
        if (c == 0) {
            c = imageIndex - other.imageIndex;
            if (c == 0) {
                c = y - other.y;
                if (c == 0) {
                    c = x - other.x;
                    if (c == 0) {
                        // From this point, it doesn't matter much for disk access.
                        // But we continue to define criterions for consistency with 'equals'.
                        c = dy - other.dy;
                        if (c == 0) {
                            c = dx - other.dx;
                        }
                    }
                }
            }
        }
        return c;
    }

    /**
     * Compares this tile with the specified one for equality. Two tiles are considered equal
     * if they have the same {@linkplain #getImageReaderSpi provider}, {@linkplain #getInput
     * input}, {@linkplain #getImageIndex image index}, {@linkplain #getRegion region} and
     * {@linkplain #getPixelSize pixel size}.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object != null && object.getClass().equals(getClass())) {
            final Tile that = (Tile) object;
            if (this.x  == that.x  && this.y  == that.y   &&
                this.dx == that.dx && this.dy == that.dy  &&
                this.imageIndex == that.imageIndex        &&
                Utilities.equals(provider, that.provider) &&
                Utilities.deepEquals(input, that.input))
            {
                /*
                 * Compares width and height only if they are defined in both tiles.  We do not
                 * invoke 'getRegion()' because it may be expensive and useless anyway: If both
                 * tiles have the same image reader, image index and input, then logically they
                 * must have the same size - invoking 'getRegion()' would read exactly the same
                 * image twice.
                 */
                return (width  == 0 || that.width  == 0 || width  == that.width) &&
                       (height == 0 || that.height == 0 || height == that.height);
            }
        }
        return false;
    }

    /**
     * Returns a hash code value for this tile. The default implementation uses the
     * {@linkplain #getImageReader reader}, {@linkplain #getInput input} and {@linkplain
     * #getImageIndex image index}, which should be suffisient for uniquely distinguish
     * every tiles.
     */
    @Override
    public int hashCode() {
        return provider.hashCode() + Utilities.deepHashCode(input) + 37*imageIndex;
    }

    /**
     * Returns the name of the given provider, for {@link #toString} purpose only.
     * May returns {@code null} if the name is unknown.
     */
    static String toString(final ImageReaderSpi provider) {
        String name = null;
        if (provider != null) {
            final String[] formats = provider.getFormatNames();
            if (formats != null) {
                int length = 0;
                for (int i=0; i<formats.length; i++) {
                    final String candidate = formats[i];
                    if (candidate != null) {
                        final int lg = candidate.length();
                        if (lg > length) {
                            length = lg;
                            name = candidate;
                        }
                    }
                }
            }
        }
        return name;
    }

    /**
     * Returns a string representation of this tile. The default implementation uses only the
     * public getter methods, so if a subclass override them the effect should be visible in
     * the returned string.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(this)).append('[');
        buffer.append("format=\"").append(getFormatName())
              .append("\", input=\"").append(getInputName())
              .append("\", index=").append(getImageIndex());
        if (width == 0 && height == 0) {
            final Point origin = getOrigin();
            buffer.append(", x=").append(origin.x)
                  .append(", y=").append(origin.y);
        } else try {
            final Rectangle region = getRegion();
            buffer.append(", x=")     .append(region.x)
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

    /**
     * Returns a string representation of a collection of tiles. The tiles are formatted in a
     * table in iteration order. Tip: consider sorting the tiles before to invoke this method;
     * tiles are {@linkplain Comparable comparable} for this purpose.
     *
     * @see java.util.Collections#sort(List)
     */
    public static String toString(final Collection<Tile> tiles) {
        final TableWriter table = new TableWriter(null);
        table.nextLine(TableWriter.DOUBLE_HORIZONTAL_LINE);
        table.write("Format\tInput\tindex\tx\ty\twidth\theight\n");
        table.nextLine(TableWriter.SINGLE_HORIZONTAL_LINE);
        table.setMultiLinesCells(true);
        for (final Tile tile : tiles) {
            table.setAlignment(TableWriter.ALIGN_LEFT);
            final String format = tile.getFormatName();
            if (format != null) {
                table.write(format);
            }
            table.nextColumn();
            table.write(tile.getInputName());
            table.nextColumn();
            table.setAlignment(TableWriter.ALIGN_RIGHT);
            table.write(String.valueOf(tile.getImageIndex()));
            table.nextColumn();
            table.write(String.valueOf(tile.x));
            table.nextColumn();
            table.write(String.valueOf(tile.y));
            if (tile.width != 0 || tile.height != 0) {
                table.nextColumn();
                table.write(String.valueOf(tile.width));
                table.nextColumn();
                table.write(String.valueOf(tile.height));
            }
            table.nextLine();
        }
        table.nextLine(TableWriter.DOUBLE_HORIZONTAL_LINE);
        return table.toString();
    }
}
