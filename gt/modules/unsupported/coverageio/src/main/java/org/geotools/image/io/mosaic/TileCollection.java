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
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageReader;
import org.geotools.resources.Classes;
import org.geotools.resources.Utilities;
import org.geotools.resources.UnmodifiableArrayList;


/**
 * A collection of {@link Tile} objects. This base class do not assumes that the tiles are arranged
 * in any particular order (especially grids). But subclasses can make such assumption for better
 * performances.
 * <p>
 * <strong>This class do not clone any value given as parameter, or any returned value.</strong>
 * Do not modify them. This class is not public for this reason.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
class TileCollection {
    /**
     * The tiles sorted by ({@linkplain Tile#getReader reader}, {@linkplain Tile#getInput input})
     * first, then by {@linkplain Tile#getImageIndex image index}. If an iteration must be performed
     * over every tiles, doing the iteration in this array order should be more efficient than other
     * order.
     */
    private final Tile[] tiles;
    
    /**
     * All tiles wrapped in an unmodifiable list.
     */
    private final Collection<Tile> allTiles;

    /**
     * The tiles in the area of interest. Elements can be {@code null} if not yet computed.
     */
    private final Collection<Tile>[] tilesOfInterest;

    /**
     * The area of interest. Elements can be {@code null} if we should returns all tiles.
     */
    private final Rectangle[] areasOfInterest;

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
     * The input given (maybe indirectly) to each image readers. Many tiles can share the same
     * reader, but that reader can have have only one {@linkplain ImageReader#setInput input set}.
     * It may be the {@linkplain Tile#getInput tile input}, but not necessary since the later may
     * have been wrapped in an {@linkplain ImageInputStream image input stream} before to be given
     * to the image reader. The values in this map are the {@linkplain Tile#getInput tile inputs}
     * <strong>before</strong> they have been wrapped in an image input stream.
     */
    private final Map<ImageReader,Object> readerInputs;

    /**
     * An unmodifiable view of the set of image readers. Will typically contains only
     * one reader, but could contains more.
     */
    private final Set<ImageReader> readers;

    /**
     * Creates a manager for the given tiles.
     */
    public TileCollection(final Tile[] tiles) {
        this(Arrays.asList(tiles));
    }

    /**
     * Creates a manager for the given tiles.
     */
    public TileCollection(final Collection<Tile> tiles) {
        /*
         * Puts together the tiles that use the same input. For those that
         * use different input, we will preserve the user-supplied order.
         */
        final Map<ReaderInputPair,List<Tile>> tilesByInput;
        tilesByInput = new LinkedHashMap<ReaderInputPair, List<Tile>>();
        readerInputs = new IdentityHashMap<ImageReader, Object>();
        readers      = Collections.unmodifiableSet(readerInputs.keySet());
        for (final Tile tile : tiles) {
            final ReaderInputPair key = new ReaderInputPair(tile);
            readerInputs.put(key.reader, null);
            List<Tile> sameInputs = tilesByInput.get(key);
            if (sameInputs == null) {
                sameInputs = new ArrayList<Tile>();
                tilesByInput.put(key, sameInputs);
            }
            sameInputs.add(tile);
        }
        /*
         * For every tiles using the same input, sort them by increasing image index order.
         * We take the opportunity for counting the number of images.
         */
        int numTiles  = 0;
        int numImages = 0;
        this.tiles = new Tile[tiles.size()];
fill:   for (final List<Tile> sameInputs : tilesByInput.values()) {
            assert !sameInputs.isEmpty();
            Collections.sort(sameInputs);
            final int imageIndex = sameInputs.get(sameInputs.size() - 1).getImageIndex();
            if (imageIndex >= numImages) {
                numImages = imageIndex + 1;
            }
            for (final Tile tile : sameInputs) {
                synchronized (tile) {
                    if (tile.manager != null) {
                        // Found a tile already in use by an other TileCollection.
                        // Stop the loop now; we will thrown an exception later.
                        break fill;
                    }
                    tile.manager = this;
                }
                this.tiles[numTiles++] = tile;
            }
        }
        /*
         * If we stopped the loop before we assigned every tiles, this is because the above
         * loop has found a tile already in use by an other TileCollection. Release every
         * previous tiles already assigned before to throw the exception.
         */
        if (numTiles != this.tiles.length) {
            while (--numTiles >= 0) {
                this.tiles[numTiles].manager = null;
            }
            throw new IllegalArgumentException("Tile already in use"); // TODO: localize
        }
        allTiles = UnmodifiableArrayList.wrap(this.tiles);
        /*
         * Now initializes the various caches.
         */
        @SuppressWarnings("unchecked") // Generic array creation.
        final Collection<Tile>[] tilesOfInterest = new Collection[numImages];
        this.tilesOfInterest = tilesOfInterest;
        this.areasOfInterest = new Rectangle [numImages];
        this.areas           = new Rectangle [numImages];
        this.tileSizes       = new Dimension [numImages];
    }

    /**
     * Returns the raw input (<strong>not</strong> wrapped in an image input stream) for the
     * given reader.
     */
    final Object getRawInput(final ImageReader reader) {
        return readerInputs.get(reader);
    }

    /**
     * Sets the raw input (<strong>not</strong> wrapped in an image input stream) for the
     * given reader. The input can be set to {@code null}, but we don't allow entry removal
     * on intend since the keys need to be returned by {@link #getReaders}.
     */
    final void setRawInput(final ImageReader reader, final Object input) {
        readerInputs.put(reader, input);
    }

    /**
     * Returns the set of image readers. Will typically contains only one reader,
     * but could contains more.
     */
    public Set<ImageReader> getReaders() {
        return readers;
    }

    /**
     * Returns a reader sample, or {@code null}. This method tries to returns an instance of the
     * most specific reader class. If no suitable instance is found, then it returns {@code null}.
     * <p>
     * This method is typically invoked for fetching an instance of {@code ImageReadParam}. We
     * look for the most specific class because it may contains additional parameters that are
     * ignored by super-classes. If we fail to find a suitable instance, then the caller shall
     * fallback on the {@link ImageReader} default implementation.
     */
    final ImageReader getReader() {
        final Set<ImageReader> readers = getReaders();
        Class<?> type = Classes.specializedClass(readers);
        while (type!=null && ImageReader.class.isAssignableFrom(type)) {
            for (final ImageReader candidate : readers) {
                if (type.equals(candidate.getClass())) {
                    return candidate;
                }
            }
            type = type.getSuperclass();
        }
        return null;
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
            for (final Tile tile : getTiles(imageIndex, null)) {
                final Rectangle expand = tile.getArea();
                if (area == null) {
                    area = expand;
                } else {
                    area.add(expand);
                }
            }
            areas[imageIndex] = area;
        }
        return area;
    }

    /**
     * Returns all tiles.
     */
    public Collection<Tile> getTiles() {
        return allTiles;
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
            interest = new ArrayList<Tile>();
            /*
             * TODO: we should use an RTree here except for the tiles where isGetAreaCheap()
             *       returns 'false'. The loop below would still be used for the laters, but
             *       the tiles for which 'isGetAreaCheap()' become 'true' would move to the
             *       RTree.
             */
            for (final Tile tile : tiles) {
                if (tile.getImageIndex() == imageIndex) {
                    if (area == null || tile.intersects(area)) {
                        interest.add(tile);
                    }
                }
            }
            /*
             * TODO: We could put more logic here by removing overlapping tiles, if any.
             */
            interest = Collections.unmodifiableCollection(interest);
            tilesOfInterest[imageIndex] = interest;
            areasOfInterest[imageIndex] = area;
        }
        return interest;
    }

    /**
     * Returns {@code true} if there is more than one tile for the given image index.
     */
    public boolean isImageTiled(final int imageIndex) {
        boolean found = false;
        // Don't invoke 'getTiles' because we want to avoid the call to Tile.getArea().
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
     * Returns the tiles dimension.
     *
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getReader reader} and this operation failed.
     */
    public Dimension getTileSize(final int imageIndex) throws IOException {
        Dimension size = tileSizes[imageIndex];
        if (size == null) {
            int width  = 0;
            int height = 0;
            for (final Tile tile : getTiles(imageIndex, null)) {
                final Rectangle area = tile.getArea();
                if (area.width  > width)  width  = area.width;
                if (area.height > height) height = area.height;
            }
            size = new Dimension(width, height);
            tileSizes[imageIndex] = size;
        }
        return size;
    }

    /**
     * Closes every streams. If {@code dispose} is {@code true}, also
     * {@linkplain ImageReader#dispose disposes} every image readers.
     */
    public void close(final boolean dispose) throws IOException {
        for (final Map.Entry<ImageReader,Object> entry : readerInputs.entrySet()) {
            final ImageReader reader = entry.getKey();
            final Object    rawInput = entry.getValue();
            final Object       input = reader.getInput();
            entry .setValue(null);
            reader.setInput(null);
            if (!dispose) {
                if (input != rawInput && input instanceof Closeable) {
                    ((Closeable) input).close();
                }
            } else {
                if (input instanceof Closeable) {
                    ((Closeable) input).close();
                }
                if (rawInput != input && rawInput instanceof Closeable) {
                    ((Closeable) rawInput).close();
                }
                reader.dispose();
            }
        }
    }
}
