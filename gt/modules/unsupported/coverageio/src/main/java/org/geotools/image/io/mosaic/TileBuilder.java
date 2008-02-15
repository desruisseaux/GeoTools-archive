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
import java.util.ArrayList;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import org.geotools.math.XMath;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


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
     * The default tile size.
     */
    private static final int DEFAULT_TILE_SIZE = 1024;

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
            int nx, ny;
            switch (layout) {
                default: // A conservative choice for unknown tile layout.
                case CONSTANT_GEOGRAPHIC_AREA: {
                    nx = tileSize.width;
                    ny = tileSize.height;
                    break;
                }
                case CONSTANT_TILE_SIZE: {
                    nx = untiledBounds.width;
                    ny = untiledBounds.height;
                    if (nx % tileSize.width  == 0) nx /= tileSize.width;
                    if (ny % tileSize.height == 0) ny /= tileSize.height;
                    break;
                }
            }
            final int[] width  = XMath.divisors(nx);
            final int[] height = (nx == ny) ? width : XMath.divisors(ny);
            int length = Math.min(width.length, height.length);
            // Trims subsamplings which would produce tiles bigger than the image.
            do {
                if (length == 0) {
                    return null;
                }
                --length;
            } while (width [length] * tileSize.width  > untiledBounds.width &&
                     height[length] * tileSize.height > untiledBounds.height);
            length++;
            subsamplings = new int[length * 2];
            int source = 0;
            for (int i=0; i<length; i++) {
                subsamplings[source++] = width [i];
                subsamplings[source++] = height[i];
            }
        }
        final Dimension[] dimensions = new Dimension[subsamplings.length];
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
        untiledBounds = getUntiledImageBounds(); // Force computation, if any.
        if (untiledBounds == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
        }
        tileSize = getTileSize(); // Force computation
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
        final Dimension  subsampling = new Dimension(1,1);
        /*
         * Computes the number of overviews that we may expect. This loop is identical to the
         * loop after minus the code related to Tile objects creation. More specifically, its
         * stop condition must be identical.
         */
        int overview = 0;
        do {
            overview++;
            tileBounds.setLocation(imageBounds.x, imageBounds.y);
            if (tileBounds.contains(imageBounds)) {
                break;
            }
            nextSubsampling(subsampling);
            imageBounds.setRect(untiledBounds);
            divide(imageBounds, subsampling);
            if (constantArea) {
                tileBounds.setSize(tileSize);
                divide(tileBounds, subsampling);
            }
        } while (isValidSize(tileBounds));
        overviewFieldSize = ((int) Math.log10(overview)) + 1; // Really want rounding toward 0.
        /*
         * Now process to Tile objects creation.
         */
        tileBounds .setSize(tileSize);
        imageBounds.setRect(untiledBounds);
        subsampling.setSize(1,1);
        overview = 0;
        do {
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
            overview++;
            tileBounds.setLocation(xmin, ymin);
            if (tileBounds.contains(imageBounds)) {
                break;
            }
            nextSubsampling(subsampling);
            imageBounds.setRect(untiledBounds);
            divide(imageBounds, subsampling);
            if (constantArea) {
                tileBounds.setSize(tileSize);
                divide(tileBounds, subsampling);
            }
        } while (isValidSize(tileBounds));
        final TileManager[] managers = factory.create(tiles);
        return managers[0];
    }

    /**
     * Returns {@code true} if the specified rectangle has a size equals or greater than the
     * minimum tile size.
     */
    private boolean isValidSize(final Rectangle bounds) {
        throw new UnsupportedOperationException("Unfinished work"); // TODO
//        return bounds.width >= minimumTileSize.width || bounds.height >= minimumTileSize.height;
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
     * Searchs for a subsampling which is close to a multiple of
     * {@linkplain #getPreferredSubsampling preferred subsampling}.
     * The subsampling dimension is updated in-place.
     */
    private void nextSubsampling(final Dimension subsampling) {
        throw new UnsupportedOperationException("Unfinished work"); // TODO
    }

    /**
     * Returns a divisor for the specified size which is close to the specified subsampling.
     * This method verifies that the given size is divisible by the given subsampling. If not,
     * it tries to add or substract 1 to the subsampling, in order to search an appropriate
     * divisor. This method is applied on the width and the height.
     *
     * @param  size A size for the raster. It could be the width or height value.
     * @param  subsampling The preferred subsampling for divising the size.
     * @param  lower The minimal subsampling, exclusive.
     * @return The proposed subsampling value.
     */
    private static int nextSubsampling(final int size, int subsampling, final int lower) {
        if (subsampling <= lower) {
            subsampling = lower + 1;
        }
        if (size % subsampling != 0) {
            for (int i=1; i<=5; i++) {
                int candidate = subsampling - i;
                if (candidate > lower && size % candidate == 0) {
                    return candidate;
                }
                candidate = subsampling + i;
                if (size % candidate == 0) {
                    return candidate;
                }
            }
        }
        return subsampling;
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
     * Generates a name, for the current tile, based on the position of this tile in the raster.
     * For example, a tile at the first level of overview, which is localized on the 5th column
     * and 2nd row will have the name "L1_E2".
     *
     * @param  overview  The level of overview. First overview is 0.
     * @param  column    The index of columns. First column is 0.
     * @param  row       The index of rows. First row is 0.
     * @return A name based on the position of the tile in the whole raster.
     */
    private String generateFilename(final int overview, final int column, final int row) {
        final StringBuilder buffer = new StringBuilder(prefix);
        format10(buffer, overview, overviewFieldSize); buffer.append('_');
        format26(buffer, column,   columnFieldSize);
        format10(buffer, row,      rowFieldSize);
        return buffer.append('.').append(extension).toString();
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
        /** Index of the untiled image to read. */
        private final int inputIndex;

        /** The tiles created by {@link TileBuilder#createTileManager}. Will be set by {@link #filter}. */
        TileManager tiles;

        /** Creates a writer for an untiled image to be read at the given index. */
        Writer(final int inputIndex) {
            this.inputIndex = inputIndex;
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
     * Creates a tile manager from an untiled image and write the tiles. The image bounds and
     * reader SPI are inferred from the input, unless they were explicitly specified.
     *
     * @param input The untiled image input, typically as a {@link File}.
     * @param inputIndex Index of image to read, typically 0.
     */
    public TileManager writeFromUntiledImage(final Object input, final int inputIndex)
            throws IOException
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
        final Writer writer = new Writer(inputIndex);
        writer.writeFromInput(input, inputIndex, 0);
        return writer.tiles;
    }
}
