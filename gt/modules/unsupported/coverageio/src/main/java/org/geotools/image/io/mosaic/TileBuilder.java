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
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
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
     * The minimum tile size.
     */
    private Dimension minimumTileSize;

    /**
     * The preferred subsampling to use when creating a new overview.
     */
    private int xSubsampling, ySubsampling;

    /**
     * The expected size of row and column filed in generated names.
     */
    private transient int rowFieldSize, columnFieldSize;

    /**
     * The prefix to put before tile filenames. If {@code null}, will be inferred
     * from the source filename.
     */
    private String prefix;

    /**
     * File extension to be given to filename. Computed automatically by
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
        minimumTileSize = new Dimension(64, 64);
        layout = TileLayout.CONSTANT_TILE_SIZE;
        xSubsampling = 2;
        ySubsampling = 2;
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
     * Returns the tile size, or {@code null} if not set. In the later case, the tile size will
     * be inferred from the untiled image size when {@link #createTileManager()} is invoked.
     */
    public Dimension getTileSize() {
        return (tileSize != null) ? (Dimension) tileSize.clone() : null;
    }

    /**
     * Sets the tile size. A {@code null} value discarts any value previously set.
     */
    public void setTileSize(final Dimension size) {
        if (size == null) {
            tileSize = null;
        } else {
            if (size.width < minimumTileSize.width || size.height < minimumTileSize.height) {
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.ILLEGAL_ARGUMENT_$1, "size"));
            }
            tileSize = new Dimension(size);
        }
    }

    /**
     * Returns the minimum tile size.
     */
    public Dimension getMinimumTileSize() {
        return (Dimension) minimumTileSize.clone();
    }

    /**
     * Sets the minimum tile size.
     */
    public void setMinimumTileSize(final Dimension size) {
        if (size.width < 2 || size.height < 2) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$1, "size"));
        }
        minimumTileSize = new Dimension(size);
    }

    /**
     * Sets the preferred subsampling for overview computations. This is used for computing a
     * new overview from the previous one. The value must be equals or greater than 2.
     */
    public void setPreferredSubsampling(final Dimension subsampling) {
        if (subsampling.width < 2 || subsampling.height < 2) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$1, "subsampling"));
        }
        xSubsampling = subsampling.width;
        ySubsampling = subsampling.height;
    }

    /**
     * Returns the preferred subsampling for overview computations.
     * The default value is (2,2).
     */
    public Dimension getPreferredSubsampling() {
        return new Dimension(xSubsampling, ySubsampling);
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
        untiledBounds = getUntiledImageBounds();
        if (untiledBounds == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
        }
        tileSize = getTileSize();
        if (tileSize == null) {
            tileSize = untiledBounds.getSize();
            tileSize = ImageUtilities.toTileSize(tileSize);
        }
        /*
         * Selects an arbitrary prefix if none was explicitly defined. Then
         * selects the longuest file extension (e.g. "tiff" instead of "tif").
         */
        if (prefix == null) {
            prefix = "L";
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
        columnFieldSize = 0;
        rowFieldSize = 0;
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
        int overview = 0;
        do {
            final int xmin = imageBounds.x;
            final int ymin = imageBounds.y;
            final int xmax = imageBounds.x + imageBounds.width;
            final int ymax = imageBounds.y + imageBounds.height;
            computeFieldSizes(imageBounds, tileBounds);
            int y = 0;
            for (tileBounds.y = ymin; tileBounds.y < ymax; tileBounds.y += tileBounds.height) {
                int x = 0;
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
            if (tileBounds.contains(imageBounds)) {
                break;
            }
            Dimension change = changeForNextSubsampling(tileBounds);
            subsampling.width  *= change.width;
            subsampling.height *= change.height;
            imageBounds.width  /= change.width;
            imageBounds.height /= change.height;
            imageBounds.x      /= change.width;
            imageBounds.y      /= change.height;
            if (constantArea) {
                tileBounds.width  /= change.width;
                tileBounds.height /= change.height;
            }
        } while (tileBounds.width >= minimumTileSize.width || tileBounds.height >= minimumTileSize.height);
        final TileManager[] managers = factory.create(tiles);
        return managers[0];
    }

    /**
     * Returns a divisor for the given tile which is close to the
     * {@linkplain #getPreferredSubsampling preferred subsampling}.
     */
    private Dimension changeForNextSubsampling(final Rectangle tileBounds) {
        return new Dimension(changeForNextSubsampling(tileBounds.width,  xSubsampling),
                             changeForNextSubsampling(tileBounds.height, ySubsampling));
    }

    /**
     * Returns a divisor for the specified size which is close to the specified subsampling.
     * This method verifies that the given size is divisible by the given subsampling. If not,
     * it tries to add or substract 1 to the subsampling, in order to search an appropriate
     * divisor. This method is applied on the width and the height.
     *
     * @param  size A size for the raster. It could be the width or height value.
     * @param  subsampling The preferred subsampling for divising the size.
     * @return The proposed subsampling value.
     */
    private static int changeForNextSubsampling(final int size, final int subsampling) {
        if (size % subsampling != 0) {
            for (int i=1; i<=5; i++) {
                int candidate = subsampling - i;
                if (candidate > 1 && size % candidate == 0) {
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
        column(buffer, imageBounds.width / tileBounds.width, 0);
        columnFieldSize = buffer.length();
        buffer.setLength(0);
        row(buffer, imageBounds.height / tileBounds.height, 0);
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
        buffer.append(overview + 1).append('_');
        column(buffer, column, columnFieldSize);
        row(buffer, row, rowFieldSize);
        return buffer.append('.').append(extension).toString();
    }

    /**
     * Formats a row number in base 10.
     *
     * @param buffer The buffer where to write the row number.
     * @param row    The row number to format, starting at 0.
     * @param size   The expected width (for padding with '0').
     */
    private static void row(final StringBuilder buffer, final int row, final int size) {
        final String s = Integer.toString(row + 1);
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
     * @param column The column number to format, starting at 0.
     * @param size   The expected width (for padding with 'A').
     */
    private static void column(final StringBuilder buffer, int column, final int size) {
        if (size > 1 || column >= 26) {
            column(buffer, column / 26, size - 1);
            column %= 26;
        }
        buffer.append((char) ('A' + column));
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
        protected boolean filter(final ImageReader reader) throws IOException {
            final Rectangle bounds = new Rectangle();
            bounds.width  = reader.getWidth (inputIndex);
            bounds.height = reader.getHeight(inputIndex);
            // Sets only after successful reading of image size.
            final ImageReaderSpi spi = reader.getOriginatingProvider();
            if (spi != null && getTileReaderSpi() == null) {
                setTileReaderSpi(spi);
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
            } else {
                filename = input.toString();
                filename = filename.substring(filename.lastIndexOf('/') + 1);
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
