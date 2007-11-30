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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageReader;
import org.geotools.resources.Utilities;


/**
 * A collection of {@link Tile} objects. The base class do not assumes that the tiles are
 * arranged on a regular grid. But subclasses can make such assumption for better performances.
 * <p>
 * <strong>This class do not clone any value given as parameter, or any returned value.</strong>
 * Do not modify them.
 *
 * @since 2.5
 * @input $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
class TileCollection {
    /**
     * The tiles.
     *
     * @todo Replace by an RTree. See the comment in {@link #getTiles}.
     */
    private final Tile[] tiles;

    /**
     * The tiles in the area of interest, or {@code null} if not yet computed.
     */
    private Collection<Tile>[] tilesOfInterest;

    /**
     * The area of interest, or {@code null} if we should returns all tiles.
     */
    private Rectangle[] areasOfInterest;

    /**
     * The area enclosing all tiles for each image index. The length of this array is the
     * number of images. The elements in the array will be computed only when first needed.
     */
    private final Rectangle[] areas;

    /**
     * The tile dimensions. Will be computed only when first needed.
     */
    private final Dimension[] tileSizes;

    /**
     * The set of image readers. Will typically contains only one reader,
     * but could contains more. Will be created only when first needed.
     */
    private Set<ImageReader> readers;

    /**
     * Creates a collection for the given tiles.
     */
    @SuppressWarnings("unchecked") // Because of array creation.
    public TileCollection(final Tile[] tiles) {
        this.tiles = tiles;
        int numImages = 0;
        for (final Tile tile : tiles) {
            final int imageIndex = tile.getImageIndex();
            if (imageIndex >= numImages) {
                numImages = imageIndex + 1;
            }
        }
        tilesOfInterest = new Collection[numImages];
        areasOfInterest = new Rectangle [numImages];
        areas           = new Rectangle [numImages];
        tileSizes       = new Dimension [numImages];
    }

    /**
     * Returns the set of image readers. Will typically contains only one reader,
     * but could contains more.
     */
    public Set<ImageReader> getReaders() {
        if (readers == null) {
            readers = new HashSet<ImageReader>();
            for (final Tile tile : tiles) {
                readers.add(tile.getReader());
            }
        }
        return readers;
    }

    /**
     * Returns the number of images.
     */
    public int getNumImages() {
        return areas.length;
    }

    /**
     * Returns the area enclosing all tiles for the given image index.
     *
     * @param  imageIndex The image index, from 0 inclusive to {@link #getNumImages} exclusive.
     * @return The area.
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getReader reader} and this operation failed.
     */
    public Rectangle getArea(final int imageIndex) throws IOException {
        Rectangle area = areas[imageIndex];
        if (area != null) {
            for (final Tile tile : tiles) {
                if (tile.getImageIndex() == imageIndex) {
                    final Rectangle expand = tile.getArea();
                    if (area == null) {
                        area = expand;
                    } else {
                        area.add(expand);
                    }
                }
            }
            areas[imageIndex] = area;
        }
        return area;
    }

    /**
     * Returns every tiles that intersect the given area.
     *
     * @param  imageIndex The image index, from 0 inclusive to {@link #getNumImages} exclusive.
     * @param  area The area of interest, or {@code null} for iterating over all tiles.
     * @return The tiles that intercept the given area.
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getReader reader} and this operation failed.
     */
    public Collection<Tile> getTiles(final int imageIndex, final Rectangle area)
            throws IOException
    {
        Collection<Tile> interest = tilesOfInterest[imageIndex];
        if (interest == null || !Utilities.equals(areasOfInterest[imageIndex], area)) {
            if (area == null) {
                interest = Arrays.asList(tiles);
            } else {
                interest = new ArrayList<Tile>();
                /*
                 * TODO: we should use an RTree here except for the tiles where isGetAreaCheap()
                 *       returns 'false'. The loop below would still be used for the laters, but
                 *       the tiles for which 'isGetAreaCheap()' become 'true' would move to the
                 *       RTree.
                 */
                for (final Tile tile : tiles) {
                    if (tile.intersects(area)) {
                        interest.add(tile);
                    }
                }
                /*
                 * TODO: We could put more logic here by removing overlapping tiles, if any.
                 */
            }
            areasOfInterest[imageIndex] = area;
            tilesOfInterest[imageIndex] = interest;
        }
        return interest;
    }

    /**
     * Returns {@code true} if there is more than one tile for the given image index.
     */
    public boolean isImageTiled(final int imageIndex) {
        boolean found = false;
        for (final Tile tile : tiles) {
            if (tile.getImageIndex() == imageIndex) {
                if (found) {
                    return true;
                }
                found = true;
            }
        }
        return false;
    }

    /**
     * Returns the tile dimension.
     *
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getReader reader} and this operation failed.
     */
    public Dimension getTileSize(final int imageIndex) throws IOException {
        Dimension size = tileSizes[imageIndex];
        if (size == null) {
            int width  = 0;
            int height = 0;
            for (final Tile tile : tiles) {
                if (tile.getImageIndex() == imageIndex) {
                    final Rectangle area = tile.getArea();
                    if (area.width  > width)  width  = area.width;
                    if (area.height > height) height = area.height;
                }
            }
            size = new Dimension(width, height);
            tileSizes[imageIndex] = size;
        }
        return size;
    }
}
