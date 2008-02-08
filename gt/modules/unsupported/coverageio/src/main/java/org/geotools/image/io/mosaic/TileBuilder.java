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
     * The preferred subsampling to use when creating a new overview.
     */
    private int xSubsampling, ySubsampling;

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
     * bounds will be inferred from the untiled image size when {@link #writeFromUntiledImage} is
     * invoked.
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
            if (size.width < 2 || size.height < 2) {
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.ILLEGAL_ARGUMENT_$1, "size"));
            }
            tileSize = new Dimension(size);
        }
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
     * The following method must be invoked prior this one:
     * <p>
     * <ul>
     *   <li>{@link #setUntiledImageBounds}</li>
     *   <li>{@link #setTileReaderSpi}</li>
     * </ul>
     */
    public TileManager createTileManager() {
        final ImageReaderSpi tileReaderSpi = getTileReaderSpi();
        if (tileReaderSpi == null) {
            // TODO: We may try to detect automatically the Spi in a future version.
            throw new IllegalStateException(Errors.format(ErrorKeys.NO_IMAGE_READER));
        }
        final Rectangle untiledBounds = getUntiledImageBounds();
        if (untiledBounds == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
        }
        Dimension tileSize = getTileSize();
        if (tileSize == null) {
            tileSize = untiledBounds.getSize();
            tileSize = ImageUtilities.toTileSize(tileSize);
            if (tileSize.width  >= Math.max(untiledBounds.width,  512)) tileSize.width  = 512;
            if (tileSize.height >= Math.max(untiledBounds.height, 512)) tileSize.height = 512;
        }
        /*
         * Selects the longuest file extension (e.g. "tiff" instead of "tif").
         */
        String extension = "";
        final String[] suffix = tileReaderSpi.getFileSuffixes();
        if (suffix != null) {
            for (int i=0; i<suffix.length; i++) {
                final String s = suffix[i];
                if (s.length() > extension.length()) {
                    extension = s;
                }
            }
        }
        switch (layout) {
            case CONSTANT_TILE_SIZE: {
                return createConstantSize(tileReaderSpi, extension, untiledBounds, tileSize);
            }
            case CONSTANT_GEOGRAPHIC_AREA: {
                return createConstantArea(tileReaderSpi, extension, untiledBounds, tileSize);
            }
            default: {
                throw new IllegalStateException(layout.toString());
            }
        }
    }

    /**
     * Creates tiles covering a constant geographic region. This tile size will reduce as we
     * progress into overviews levels. The {@link #tileSize} field is the stop condition -
     * no smaller tiles will be created.
     */
    private TileManager createConstantArea(final ImageReaderSpi tileReaderSpi,
            final String extension, final Rectangle untiledBounds, final Dimension tileSize)
    {
        final List<Tile> tiles       = new ArrayList<Tile>();
        final Rectangle  tileBounds  = new Rectangle(untiledBounds);
        final Dimension  subsampling = new Dimension(1,1);
        int overview = 1;
        while (tileBounds.width >= tileSize.width && tileBounds.height >= tileSize.height) {
            final int xmax = (untiledBounds.x + untiledBounds.width)  / subsampling.width;
            final int ymax = (untiledBounds.y + untiledBounds.height) / subsampling.height;
            // TODO: to be given to generateName
            final int xd = (int) Math.log10((double) untiledBounds.x / (subsampling.width  * tileBounds.width));
            final int yd = (int) Math.log10((double) untiledBounds.y / (subsampling.height * tileBounds.height));
            tileBounds.y = untiledBounds.y / subsampling.height;
            int y = 0;
            do {
                tileBounds.x = untiledBounds.x / subsampling.width;
                int x = 0;
                do {
                    final File file = new File(directory, generateName(overview, x, y, extension));
                    final Tile tile = new Tile(tileReaderSpi, file, 0, tileBounds, subsampling);
                    tiles.add(tile);
                    x++;
                } while ((tileBounds.x += tileBounds.width) < xmax);
                y++;
            } while ((tileBounds.y += tileBounds.height) < ymax);
            overview++;
            Dimension newStep = changeForNextOverview(tileBounds);
            subsampling.width  *= newStep.width;
            subsampling.height *= newStep.height;
            tileBounds .width  /= newStep.width;
            tileBounds .height /= newStep.height;
        }
        final TileManager[] managers = factory.create(tiles);
        return managers[0];
    }

    /**
     * Creates tiles having a constant size in pixels. The geographic area will increase
     * as we progress into overviews level.
     */
    private TileManager createConstantSize(final ImageReaderSpi tileReaderSpi,
            final String extension, final Rectangle untiledBounds, final Dimension tileSize)
    {
        final List<Tile> tiles = new ArrayList<Tile>();
        Rectangle wholeRaster = untiledBounds;
        final Rectangle tileRect = new Rectangle(tileSize);
        Dimension subsampling = new Dimension(1,1);
        // Iterator used for the file name.
        int overview = 1, x = 1, y = 1;
        while (!tileRect.contains(wholeRaster)) {
            // Current values for the y coordinates
            int yMin = 0;
            int yMax = tileSize.height;
            while (yMin < wholeRaster.height) {
                /* Verify that we are not trying to generate a tile outside the original
                 * raster bounds, for the height.
                 * If it is the case, we take the width bound of the original raster as
                 * the width bound for the new tile.
                 */
                if (yMax > wholeRaster.height) {
                    yMax = wholeRaster.height;
                }
                // Current values for the x coordinates
                int xMin = 0;
                int xMax = tileSize.width;
                while (xMin < wholeRaster.width) {
                    /* Verify that we are not trying to generate a tile outside the original
                     * raster bounds, for the width.
                     * If it is the case, we take the height bound of the original raster as
                     * the height bound for the new tile.
                     */
                    if (xMax > wholeRaster.width) {
                        xMax = wholeRaster.width;
                    }
                    Rectangle currentRect = new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
                    final File file = new File(directory, generateName(overview, x, y, extension));
                    final Tile tile = new Tile(tileReaderSpi, file, 0, currentRect, subsampling);
                    tiles.add(tile);
                    x++;
                    xMin += tileSize.width;
                    xMax += tileSize.width;
                }
                y++;
                // Restart column index from the beginning, since we have change of row index.
                x = 1;
                yMin += tileSize.height;
                yMax += tileSize.height;
            }
            // Restart row index from the beginning, since we have change of overview index.
            y = 1;
            // Change to next level of overview.
            overview++;
            subsampling.width  *= xSubsampling;
            subsampling.height *= ySubsampling;
            wholeRaster.width  /= xSubsampling;
            wholeRaster.height /= ySubsampling;
        }
        final TileManager[] managers = factory.create(tiles);
        return managers[0];
    }

    /**
     * Returns a divisor for the given tile which is close to the
     * {@linkplain #getPreferredSubsampling preferred subsampling}.
     */
    private Dimension changeForNextOverview(final Rectangle tileBounds) {
        return new Dimension(changeForNextOverview(tileBounds.width,  xSubsampling),
                             changeForNextOverview(tileBounds.height, ySubsampling));
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
    private static int changeForNextOverview(final int size, final int subsampling) {
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
     * Generates a name, for the current tile, based on the position of this tile in the raster.
     * For example, a tile at the first level of overview, which is localized on the 5th column
     * and 2nd row will have the name "L1_E2".
     *
     * @param overview  The level of overview. First overview is 0.
     * @param x         The index of columns. First column is 0.
     * @param y         The index of rows. First row is 0.
     * @param xd        Number of leters for columns.
     * @param yd        Number of digits for rows.
     * @param extension The filename extension.
     * @return A name based on the position of the tile in the whole raster.
     */
    private static String generateName(final int overview, final int x, final int y,
            final String extension)
    {
        final StringBuilder buffer = new StringBuilder("L");
        buffer.append(overview).append('_');
        toLetters(x, buffer);
        buffer.append(y).append('.').append(extension);
        return buffer.toString();
    }

    /**
     * Converts the column index into letter. For example the first column is 'A'.
     * If there is more columns than alphabet has letters, then another letter is
     * added in the same way.
     *
     * @param column The index of the column for the tile in the original raster.
     * @param buffer The buffer where letter(s) will be added.
     */
    private static void toLetters(int column, final StringBuilder buffer) {
        if (column > 26) {
            toLetters(column / 26, buffer);
            column %= 26;
        }
        buffer.append((char) ('A' + (column - 1)));
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
     * Creates
     */
    public TileManager writeFromUntiledImage(final Object input, final int inputIndex)
            throws IOException
    {
        final Writer writer = new Writer(inputIndex);
        writer.writeFromInput(input, inputIndex, 0);
        return writer.tiles;
    }
}
