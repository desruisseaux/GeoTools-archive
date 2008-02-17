/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, GeoTools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import org.geotools.math.XMath;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.image.ImageUtilities;


/**
 * A convenience class for building tiles using the same {@linkplain ImageReader image reader}
 * and organized according some common {@linkplain TileLayout tile layout}. Optionally, this
 * builder can also write the tiles to disk from an initially untiled image.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Cédric Briançon
 * @author Martin Desruisseaux
 */
public class TileBuilder {
    /**
     * Default value for {@link #prefix}. Current implementation uses "L" as in "Level".
     */
    private static final String DEFAULT_PREFIX = "L";

    /**
     * The default tile size in pixels.
     */
    private static final int DEFAULT_TILE_SIZE = 1024;

    /**
     * Minimum tile size when using {@link TileLayout#CONSTANT_GEOGRAPHIC_AREA} without
     * explicit subsamplings provided by user.
     */
    private static final int MIN_TILE_SIZE = 64;

    /**
     * The factory to use for creating {@link TileManager} instances.
     */
    protected final TileManagerFactory factory;

    /**
     * The desired layout.
     */
    private TileLayout layout;

    /**
     * The tile directory, or {@code null} for current directory.
     * It may be either a relative or absolute path.
     */
    private File directory;

    /**
     * The image reader provider. The initial value is {@code null}.
     * This value must be set before {@link Tile} objects are created.
     */
    private ImageReaderSpi tileReaderSpi;

    /**
     * The raster bounding box in pixel coordinates. The initial value is {@code null}.
     * This value must be set before {@link Tile} objects are created.
     */
    private Rectangle untiledBounds;

    /**
     * The desired tile size. The initial value is {@code null}.
     * This value must be set before {@link Tile} objects are created.
     */
    private Dimension tileSize;

    /**
     * The subsamplings to use when creating a new overview. Values at even index are
     * <var>x</var> subsamplings and values at odd index are <var>y</var> subsamplings.
     * If {@code null}, subsampling will be computed automatically from the image and
     * tile size in order to get only entire tiles.
     */
    private int[] subsamplings;

    /**
     * The expected size of row and column filed in generated names.
     */
    private transient int overviewFieldSize, rowFieldSize, columnFieldSize;

    /**
     * The prefix to put before tile filenames. If {@code null}, will be inferred
     * from the source filename.
     */
    private String prefix;

    /**
     * File extension to be given to filenames. Computed automatically by
     * {@link #createTileManager}.
     */
    private transient String extension;

    /**
     * Generates tiles using the default factory.
     */
    public TileBuilder() {
        this(null);
    }

    /**
     * Generates tiles using the specified factory.
     *
     * @param factory The factory to use, or {@code null} for the
     *        {@linkplain TileManagerFactory#DEFAULT default} one.
     */
    public TileBuilder(final TileManagerFactory factory) {
        this.factory = (factory != null) ? factory : TileManagerFactory.DEFAULT;
        layout = TileLayout.CONSTANT_TILE_SIZE;
    }

    /**
     * Returns the tile layout. The default value is
     * {@link TileLayout#CONSTANT_TILE_SIZE CONSTANT_TILE_SIZE}, which is the most efficient
     * layout available in {@code org.geotools.image.io.mosaic} implementation.
     */
    public TileLayout getTileLayout() {
        return layout;
    }

    /**
     * Sets the tile layout to the specified value. Valid values are
     * {@link TileLayout#CONSTANT_TILE_SIZE CONSTANT_TILE_SIZE} and
     * {@link TileLayout#CONSTANT_GEOGRAPHIC_AREA CONSTANT_GEOGRAPHIC_AREA}.
     */
    public void setTileLayout(final TileLayout layout) {
        if (layout != null) {
            switch (layout) {
                case CONSTANT_TILE_SIZE:
                case CONSTANT_GEOGRAPHIC_AREA: {
                    this.layout = layout;
                    return;
                }
            }
        }
        throw new IllegalArgumentException(Errors.format(
                ErrorKeys.ILLEGAL_ARGUMENT_$2, "layout", layout));
    }

    /**
     * Returns the tile directory, or {@code null} for current directory. The directory
     * may be either relative or absolute. The default value is {@code null}.
     */
    public File getTileDirectory() {
        return directory;
    }

    /**
     * Sets the directory where tiles will be read or written. May be a relative or absolute
     * path, or {@code null} (the default) for current directory.
     */
    public void setTileDirectory(final File directory) {
        this.directory = directory;
    }

    /**
     * Returns the {@linkplain ImageReader image reader} provider to use for reading tiles.
     * The initial value is {@code null}, which means that the provider should be the same
     * than the one detected by {@link #writeFromUntiledImage writeFromUntiledImage}.
     */
    public ImageReaderSpi getTileReaderSpi() {
        return tileReaderSpi;
    }

    /**
     * Sets the {@linkplain ImageReader image reader} provider for each tiles to be read.
     * A {@code null} value means that the provider should be automatically detected by
     * {@link #writeFromUntiledImage writeFromUntiledImage}.
     */
    public void setTileReaderSpi(final ImageReaderSpi provider) {
        this.tileReaderSpi = provider;
    }

    /**
     * Returns the bounds of the untiled image, or {@code null} if not set. In the later case, the
     * bounds will be inferred from the input image when {@link #writeFromUntiledImage} is invoked.
     */
    public Rectangle getUntiledImageBounds() {
        return (untiledBounds != null) ? (Rectangle) untiledBounds.clone() : null;
    }

    /**
     * Sets the bounds of the untiled image to the specified value.
     * A {@code null} value discarts any value previously set.
     */
    public void setUntiledImageBounds(final Rectangle bounds) {
        untiledBounds = (bounds != null) ? new Rectangle(bounds) : null;
    }

    /**
     * Returns the tile size. If no tile size has been explicitly set, then a default tile size
     * will be computed from the {@linkplain #getUntiledImageBounds untiled image bounds}. If no
     * size can be computed, then this method returns {@code null}.
     *
     * @see #suggestTileSize
     */
    public Dimension getTileSize() {
        if (tileSize == null) {
            final Rectangle untiledBounds = getUntiledImageBounds();
            if (untiledBounds == null) {
                return null;
            }
            int width  = untiledBounds.width;
            int height = untiledBounds.height;
            width  = suggestTileSize(width);
            height = (height == untiledBounds.width) ? width : suggestTileSize(height);
            tileSize = new Dimension(width, height);
        }
        return (Dimension) tileSize.clone();
    }

    /**
     * Sets the tile size. A {@code null} value discarts any value previously set.
     */
    public void setTileSize(final Dimension size) {
        if (size == null) {
            tileSize = null;
        } else {
            if (size.width < 2 || size.height < 2) {
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.ILLEGAL_ARGUMENT_$1, "size"));
            }
            tileSize = new Dimension(size);
        }
    }

    /**
     * Suggests a tile size using default values.
     */
    private static int suggestTileSize(final int imageSize) {
        return suggestTileSize(imageSize, DEFAULT_TILE_SIZE,
                DEFAULT_TILE_SIZE - DEFAULT_TILE_SIZE/4, DEFAULT_TILE_SIZE + DEFAULT_TILE_SIZE/4);
    }

    /**
     * Suggests a tile size ({@linkplain Dimension#width width} or {@linkplain Dimension#height
     * height}) for the given image size. This methods search for a value <var>x</var> inside the
     * {@code [minSize...maxSize]} range where {@code imageSize}/<var>x</var> has the largest amount
     * of {@linkplain XMath#divisors divisors}. If more than one value have the same amount of
     * divisors, then the one which is the closest to {@code tileSize} is returned.
     *
     * @param  imageSize The image size.
     * @param  tileSize  The preferred tile size. Must be inside the {@code [minSize...maxSize]} range.
     * @param  minSize   The minimum size, inclusive. Must be greater than 0.
     * @param  maxSize   The maximum size, inclusive. Must be equals or greater that {@code minSize}.
     * @return The suggested tile size. Inside the {@code [minSize...maxSize]} range except
     *         if {@code imageSize} was smaller than {@link minSize}.
     * @throws IllegalArgumentException if any argument doesn't meet the above-cited conditions.
     */
    public static int suggestTileSize(final int imageSize, final int tileSize,
                                      final int minSize,   final int maxSize)
            throws IllegalArgumentException
    {
        if (minSize <= 1 || minSize > maxSize) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.BAD_RANGE_$2, minSize, maxSize));
        }
        if (tileSize < minSize || tileSize > maxSize) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.VALUE_OUT_OF_BOUNDS_$3, tileSize, minSize, maxSize));
        }
        if (imageSize <= minSize) {
            return imageSize;
        }
        int numDivisors = 0;
        int best = tileSize;
        for (int i=minSize; i<=maxSize; i++) {
            if (imageSize % i != 0) {
                continue;
            }
            final int n = XMath.divisors(imageSize / i).length;
            if (n < numDivisors) {
                continue;
            }
            if (n == numDivisors) {
                if (Math.abs(i - tileSize) >= Math.abs(best - tileSize)) {
                    continue;
                }
            }
            best = i;
            numDivisors = n;
        }
        return best;
    }

    /**
     * Returns the subsampling for overview computations. If no subsamplings were {@linkplain
     * #setSubsamplings(Dimension[]) explicitly set}, then this method computes automatically
     * some subsamplings from the {@linkplain #getUntiledImageBounds untiled image bounds} and
     * {@linkplain #getTileSize tile size}. If no subsampling can be computed, then this method
     * returns {@code null}.
     */
    public Dimension[] getSubsamplings() {
        if (subsamplings == null) {
            final Rectangle untiledBounds = getUntiledImageBounds();
            if (untiledBounds == null) {
                return null;
            }
            final Dimension tileSize = getTileSize();
            if (tileSize == null) {
                return null;
            }
            /*
             * If the tile layout is CONSTANT_GEOGRAPHIC_AREA, increasing the subsampling will have
             * the effect of reducing the tile size by the same amount, so we are better to choose
             * subsamplings that are divisors of the tile size.
             *
             * If the tile layout is CONSTANT_TILE_SIZE, increasing the subsampling will have the
             * effect of reducing the number of tiles required for covering the whole image. So we
             * are better to choose subsamplings that are divisors of the number of tiles. However
             * if the number of tiles are not integers, we can't do much.
             *
             * In the later case (non-integer amount of tiles) and in the case where the tile layout
             * is unknown, we don't really know what to choose. We fallback on some values that seem
             * reasonable, but our fallback may change in future version. It doesn't hurt any code
             * in this module - the only consequence is that tiling may be suboptimal.
             */
            final boolean constantArea = TileLayout.CONSTANT_GEOGRAPHIC_AREA.equals(layout);
            int nx = tileSize.width;
            int ny = tileSize.height;
            if (!constantArea) {
                if (untiledBounds.width  % nx == 0) nx = untiledBounds.width  / nx;
                if (untiledBounds.height % ny == 0) ny = untiledBounds.height / ny;
            }
            int[] xSubsamplings = XMath.divisors(nx);
            int[] ySubsamplings;
            if (nx == ny) {
                ySubsamplings = xSubsamplings;
            } else {
                ySubsamplings = XMath.divisors(ny);
                /*
                 * Subsamplings are different along x and y axis. We need at least arrays of the
                 * same length.
                 */
                final int[] newX = new int[xSubsamplings.length + ySubsamplings.length];
                final int[] newY = new int[xSubsamplings.length + ySubsamplings.length];
                int ix=0, iy=0; nx=0; ny=0;
                while (ix < xSubsamplings.length && iy < ySubsamplings.length) {
                    final int sx = newX[nx++] = xSubsamplings[ix];
                    final int sy = newY[ny++] = ySubsamplings[iy];
                    if (sx <= sy) ix++;
                    if (sy <= sx) iy++;
                }
                // TODO: we have reach this point for tonigh...
            }
            /*
             * Trims the subsamplings which would produce tiles smaller than the minimum size
             * (for CONSTANT_GEOGRAPHIC_AREA layoy) or which would produce more than one tile
             * enclosing the whole image (for CONSTANT_TILE_SIZE layout). First, we calculate
             * as (nx,ny) the maximum subsamplings expected (inclusive). Then we search those
             * maximum in the actual subsampling and assign to (nx,ny) the new array length.
             */
            if (constantArea) {
                nx = tileSize.width  / MIN_TILE_SIZE;
                ny = tileSize.height / MIN_TILE_SIZE;
            } else {
                nx = (untiledBounds.width  - 1) / tileSize.width  + 1;
                ny = (untiledBounds.height - 1) / tileSize.height + 1;
            }
            nx = Arrays.binarySearch(xSubsamplings, nx); if (nx < 0) nx = ~nx; else nx++;
            ny = Arrays.binarySearch(ySubsamplings, ny); if (ny < 0) ny = ~ny; else ny++;
            final int length = Math.min(nx, ny);
            subsamplings = new int[length * 2];
            int source = 0;
            for (int i=0; i<length; i++) {
                subsamplings[source++] = xSubsamplings[i];
                subsamplings[source++] = ySubsamplings[i];
            }
        }
        final Dimension[] dimensions = new Dimension[subsamplings.length / 2];
        int source = 0;
        for (int i=0; i<dimensions.length; i++) {
            dimensions[i] = new Dimension(subsamplings[source++], subsamplings[source++]);
        }
        return dimensions;
    }

    /**
     * Sets the subsamplings for overview computations. The number of overview levels created
     * by this {@code TileBuilder} will be equals to the {@code subsamplings} array length.
     * <p>
     * Subsamplings most be explicitly provided for {@link TileLayout#CONSTANT_GEOGRAPHIC_AREA},
     * but is optional for {@link TileLayout#CONSTANT_TILE_SIZE}. In the later case subsamplings
     * may be {@code null} (the default), in which case they will be automatically computed from
     * the {@linkplain #getUntiledImageBounds untiled image bounds} and {@linkplain #getTileSize
     * tile size} in order to have only entire tiles (i.e. tiles in last columns and last rows
     * don't need to be cropped).
     */
    public void setSubsamplings(final Dimension[] subsamplings) {
        final int[] newSubsamplings;
        if (subsamplings == null) {
            newSubsamplings = null;
        } else {
            int target = 0;
            newSubsamplings = new int[subsamplings.length * 2];
            for (int i=0; i<subsamplings.length; i++) {
                final Dimension subsampling = subsamplings[i];
                final int xSubsampling = subsampling.width;
                final int ySubsampling = subsampling.height;
                if (xSubsampling < 1 || ySubsampling < 1) {
                    throw new IllegalArgumentException(Errors.format(
                            ErrorKeys.ILLEGAL_ARGUMENT_$1, "subsamplings[" + i + ']'));
                }
                newSubsamplings[target++] = xSubsampling;
                newSubsamplings[target++] = ySubsampling;
            }
        }
        this.subsamplings = newSubsamplings;
    }

    /**
     * Sets uniform subsamplings for overview computations. This convenience method delegates to
     * {@link #setSubsamplings(Dimension[])} with the same value affected to both
     * {@linkplain Dimension#width width} and {@linkplain Dimension#height height}.
     */
    public void setSubsamplings(final int[] subsamplings) {
        final Dimension[] newSubsamplings;
        if (subsamplings == null) {
            newSubsamplings = null;
        } else {
            newSubsamplings = new Dimension[subsamplings.length];
            for (int i=0; i<subsamplings.length; i++) {
                final int subsampling = subsamplings[i];
                newSubsamplings[i] = new Dimension(subsampling, subsampling);
            }
        }
        // Delegates to setSubsamplings(Dimension[]) instead of performing the same work in-place
        // (which would have been more efficient) because the user may have overriden the former.
        setSubsamplings(newSubsamplings);
    }

    /**
     * Creates a tile manager from the informations supplied in above setters.
     * The following methods must be invoked prior this one:
     * <p>
     * <ul>
     *   <li>{@link #setUntiledImageBounds}</li>
     *   <li>{@link #setTileReaderSpi}</li>
     * </ul>
     */
    public TileManager createTileManager() {
        tileReaderSpi = getTileReaderSpi();
        if (tileReaderSpi == null) {
            // TODO: We may try to detect automatically the Spi in a future version.
            throw new IllegalStateException(Errors.format(ErrorKeys.NO_IMAGE_READER));
        }
        untiledBounds = getUntiledImageBounds(); // Forces computation, if any.
        if (untiledBounds == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
        }
        tileSize = getTileSize(); // Forces computation
        if (tileSize == null) {
            tileSize = ImageUtilities.toTileSize(untiledBounds.getSize());
        }
        /*
         * Selects an arbitrary prefix if none was explicitly defined. Then
         * selects the longuest file extension (e.g. "tiff" instead of "tif").
         */
        if (prefix == null) {
            prefix = DEFAULT_PREFIX;
        }
        extension = "";
        final String[] suffix = tileReaderSpi.getFileSuffixes();
        if (suffix != null) {
            for (int i=0; i<suffix.length; i++) {
                final String s = suffix[i];
                if (s.length() > extension.length()) {
                    extension = s;
                }
            }
        }
        overviewFieldSize = 0;
        rowFieldSize      = 0;
        columnFieldSize   = 0;
        switch (layout) {
            case CONSTANT_GEOGRAPHIC_AREA: return createTileManager(true);
            case CONSTANT_TILE_SIZE:       return createTileManager(false);
            default: throw new IllegalStateException(layout.toString());
        }
    }

    /**
     * Creates tiles for the following cases:
     * <ul>
     *   <li>covering a constant geographic region. The tile size will reduce as we progress into
     *       overviews levels. The {@link #minimumTileSize} value is the stop condition - no smaller
     *       tiles will be created.</li>
     *   <li>tiles of constant size in pixels. The stop condition is when a single tile cover
     *       the whole image.</li>
     * </ul>
     */
    private TileManager createTileManager(final boolean constantArea) {
        final List<Tile> tiles       = new ArrayList<Tile>();
        final Rectangle  tileBounds  = new Rectangle(tileSize);
        final Rectangle  imageBounds = new Rectangle(untiledBounds);
        Dimension[] subsamplings = getSubsamplings();
        if (subsamplings == null) {
            final int n;
            if (constantArea) {
                n = Math.max(tileBounds.width, tileBounds.height) / MIN_TILE_SIZE;
            } else {
                n = Math.max(imageBounds.width  / tileBounds.width,
                             imageBounds.height / tileBounds.height);
            }
            subsamplings = new Dimension[n];
            for (int i=1; i<=n; i++) {
                subsamplings[i-1] = new Dimension(i,i);
            }
        }
        overviewFieldSize = ((int) Math.log10(subsamplings.length)) + 1;
        for (int overview=0; overview<subsamplings.length; overview++) {
            final Dimension subsampling = subsamplings[overview];
            imageBounds.setRect(untiledBounds);
            divide(imageBounds, subsampling);
            if (constantArea) {
                tileBounds.setSize(tileSize);
                divide(tileBounds, subsampling);
            }
            final int xmin = imageBounds.x;
            final int ymin = imageBounds.y;
            final int xmax = imageBounds.x + imageBounds.width;
            final int ymax = imageBounds.y + imageBounds.height;
            computeFieldSizes(imageBounds, tileBounds);
            int x=0, y=0;
            for (tileBounds.y = ymin; tileBounds.y < ymax; tileBounds.y += tileBounds.height) {
                x = 0;
                for (tileBounds.x = xmin; tileBounds.x < xmax; tileBounds.x += tileBounds.width) {
                    final Rectangle clippedBounds = tileBounds.intersection(imageBounds);
                    final File file = new File(directory, generateFilename(overview, x, y));
                    final Tile tile = new Tile(tileReaderSpi, file, 0, clippedBounds, subsampling);
                    tiles.add(tile);
                    x++;
                }
                y++;
            }
        }
        final TileManager[] managers = factory.create(tiles);
        return managers[0];
    }

    /**
     * Divides a rectangle by the given subsampling.
     */
    private static void divide(final Rectangle bounds, final Dimension subsampling) {
        bounds.x      /= subsampling.width;
        bounds.y      /= subsampling.height;
        bounds.width  /= subsampling.width;
        bounds.height /= subsampling.height;
    }

    /**
     * Computes the values for {@link #columnFieldSize} and {@link #rowFieldSize}.
     * They will be used by {@link #generateFilename}.
     */
    private void computeFieldSizes(final Rectangle imageBounds, final Rectangle tileBounds) {
        final StringBuilder buffer = new StringBuilder();
        format26(buffer, imageBounds.width / tileBounds.width, 0);
        columnFieldSize = buffer.length();
        buffer.setLength(0);
        format10(buffer, imageBounds.height / tileBounds.height, 0);
        rowFieldSize = buffer.length();
    }

    /**
     * Formats a number in base 10.
     *
     * @param buffer The buffer where to write the row number.
     * @param n      The row number to format, starting at 0.
     * @param size   The expected width (for padding with '0').
     */
    private static void format10(final StringBuilder buffer, final int n, final int size) {
        final String s = Integer.toString(n + 1);
        for (int i=size-s.length(); --i >= 0;) {
            buffer.append('0');
        }
        buffer.append(s);
    }

    /**
     * Formats a column in base 26. For example the first column is {@code 'A'}. If there is
     * more columns than alphabet has letters, then another letter is added in the same way.
     *
     * @param buffer The buffer where to write the column number.
     * @param n      The column number to format, starting at 0.
     * @param size   The expected width (for padding with 'A').
     */
    private static void format26(final StringBuilder buffer, int n, final int size) {
        if (size > 1 || n >= 26) {
            format26(buffer, n / 26, size - 1);
            n %= 26;
        }
        buffer.append((char) ('A' + n));
    }

    /**
     * The mosaic image writer to be used by {@link TileBuilder#writeFromUntiledImage}.
     */
    private final class Writer extends MosaicImageWriter {
        /**
         * Index of the untiled image to read.
         */
        private final int inputIndex;

        /**
         * {@code true} if tiles should be written.
         */
        private final boolean writeTiles;

        /**
         * The tiles created by {@link TileBuilder#createTileManager}.
         * Will be set by {@link #filter} and read by {@link TileBuilder}.
         */
        TileManager tiles;

        /**
         * Creates a writer for an untiled image to be read at the given index.
         */
        Writer(final int inputIndex, final boolean writeTiles) {
            this.inputIndex = inputIndex;
            this.writeTiles = writeTiles;
        }

        /**
         * Returns {@code true} if tiles should be written.
         */
        @Override
        boolean isWriteEnabled() {
            return writeTiles;
        }

        /**
         * Creates the tiles for the specified untiled images.
         */
        @Override
        protected boolean filter(ImageReader reader) throws IOException {
            final Rectangle bounds = new Rectangle();
            bounds.width  = reader.getWidth (inputIndex);
            bounds.height = reader.getHeight(inputIndex);
            // Sets only after successful reading of image size.
            if (reader instanceof MosaicImageReader) {
                reader = ((MosaicImageReader) reader).getTileReader();
            }
            if (reader != null) { // May be null as a result of above line.
                final ImageReaderSpi spi = reader.getOriginatingProvider();
                if (spi != null && getTileReaderSpi() == null) {
                    setTileReaderSpi(spi);
                }
            }
            setUntiledImageBounds(bounds);
            tiles = createTileManager();
            setOutput(tiles);
            return true;
        }
    }

    /**
     * Creates a tile manager from an untiled image. The {@linkplain #getUntiledImageBounds
     * untiled image bounds} and {@linkplain #getTileReaderSpi tile reader SPI} are inferred
     * from the input, unless they were explicitly specified.
     * <p>
     * Optionnaly if {@code writeTiles} is {@code true}, then pixel values are read from the
     * untiled images, organized in tiles as specified by the {@link TileManager} to be returned
     * and saved to disk. This work is done using a default {@link MosaicImageWriter}.
     *
     * @param input      The untiled image input, typically as a {@link File}.
     * @param inputIndex Index of image to read, typically 0.
     * @param writeTiles If {@code true}, tiles are created and saved to disk.
     * @throws IOException if an error occured while reading the untiled image or (only if
     *         {@code writeTiles} is {@code true}) while writting the tiles to disk.
     */
    public TileManager createTileManager(final Object input, final int inputIndex,
                                         final boolean writeTiles) throws IOException
    {
        if (prefix == null) {
            String filename;
            if (input instanceof File) {
                filename = ((File) input).getName();
            } else if (input instanceof URI || input instanceof URL || input instanceof CharSequence) {
                filename = input.toString();
                filename = filename.substring(filename.lastIndexOf('/') + 1);
            } else {
                filename = DEFAULT_PREFIX;
            }
            int length = filename.lastIndexOf('.');
            if (length < 0) {
                length = filename.length();
            }
            int i;
            for (i=0; i<length; i++) {
                if (!Character.isLetter(filename.charAt(i))) {
                    break;
                }
            }
            prefix = filename.substring(0, i);
        }
        final Writer writer = new Writer(inputIndex, writeTiles);
        writer.writeFromInput(input, inputIndex, 0);
        return writer.tiles;
    }

    /**
     * Generates a filename for the current tile based on the position of this tile in the raster.
     * For example, a tile in the first overview level, which is localized on the 5th column and
     * 2nd row may have a name like "{@code L1_E2.png}".
     * <p>
     * Subclasses may override this method if they want more control on generated tile filenames.
     *
     * @param  overview  The level of overview. First overview is 0.
     * @param  column    The index of columns. First column is 0.
     * @param  row       The index of rows. First row is 0.
     * @return A filename based on the position of the tile in the whole raster.
     */
    protected String generateFilename(final int overview, final int column, final int row) {
        final StringBuilder buffer = new StringBuilder(prefix);
        format10(buffer, overview, overviewFieldSize); buffer.append('_');
        format26(buffer, column,   columnFieldSize);
        format10(buffer, row,      rowFieldSize);
        return buffer.append('.').append(extension).toString();
    }
}
