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
import javax.imageio.stream.ImageInputStream;
import org.geotools.resources.Classes;
import org.geotools.resources.UnmodifiableArrayList;


/**
 * A collection of {@link Tile} objects at one image index. This base class do not assumes that
 * the tiles are arranged in any particular order (especially grids). But subclasses can make such
 * assumption for better performances.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TileManager implements Comparator<Tile> {
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
     * The tiles in the region of interest, or {@code null} if not yet computed.
     */
    private Collection<Tile> tilesOfInterest;

    /**
     * The subsampling used at the time {@link #tilesOfInterest} has been computed.
     */
    private int xSubsampling, ySubsampling;

    /**
     * The region of interest.
     */
    private final Rectangle regionOfInterest;

    /**
     * The region enclosing all tiles. Will be computed only when first needed.
     */
    private Rectangle region;

    /**
     * The tile dimensions. Will be computed only when first needed.
     */
    private Dimension tileSize;

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
    public TileManager(final Tile[] tiles) {
        this(Arrays.asList(tiles));
    }

    /**
     * Creates a manager for the given tiles.
     */
    public TileManager(final Collection<Tile> tiles) {
        final Tile[] tilesArray = tiles.toArray(new Tile[tiles.size()]);
        Arrays.sort(tilesArray);
        /*
         * Puts together the tiles that use the same input. For those that use
         * different input, we will order by image index first, then (y,x) order.
         */
        final Map<ReaderInputPair,List<Tile>> tilesByInput;
        tilesByInput = new LinkedHashMap<ReaderInputPair, List<Tile>>();
        readerInputs = new IdentityHashMap<ImageReader, Object>();
        readers      = Collections.unmodifiableSet(readerInputs.keySet());
        for (final Tile tile : tilesArray) {
            final ReaderInputPair key = new ReaderInputPair(tile);
            readerInputs.put(key.reader, null);
            List<Tile> sameInputs = tilesByInput.get(key);
            if (sameInputs == null) {
                sameInputs = new ArrayList<Tile>(4);
                tilesByInput.put(key, sameInputs);
            }
            sameInputs.add(tile);
        }
        /*
         * Overwrites the tiles array with the same tiles, but ordered with same input firsts.
         */
        int numTiles = 0;
fill:   for (final List<Tile> sameInputs : tilesByInput.values()) {
            assert !sameInputs.isEmpty();
            for (final Tile tile : sameInputs) {
                if (!tile.setOwner(this, numTiles)) {
                    // Found a tile already in use by an other TileManager.
                    // Stop the loop now; we will thrown an exception later.
                    break fill;
                }
                tilesArray[numTiles++] = tile;
            }
        }
        /*
         * If we stopped the loop before we assigned every tiles, this is because the above
         * loop has found a tile already in use by an other TileManager. Release every
         * previous tiles already assigned before to throw the exception.
         */
        if (numTiles != tilesArray.length) {
            while (--numTiles >= 0) {
                tilesArray[numTiles].setOwner(null, 0);
            }
            throw new IllegalArgumentException("Tile already in use"); // TODO: localize
        }
        this.tiles = tilesArray;
        allTiles = UnmodifiableArrayList.wrap(tilesArray);
        regionOfInterest = new Rectangle();
    }

    /**
     * Compares two tiles for order. The tiles must be members of the collection returned by
     * {@link #getTiles}. This comparator groups together the tiles that use the same reader
     * and input. Reading tiles in sorted order should be more efficient than a random order.
     *
     * @throws IllegalArgumentException If one of the tiles is not a member of the collection
     *         returned by {@link #getTiles}.
     * @see Tile#compareTo
     */
    public int compare(final Tile tile1, final Tile tile2) throws IllegalArgumentException {
        return tile2.getRank(this) - tile1.getRank(this);
    }

    /**
     * Returns the raw input (<strong>not</strong> wrapped in an image input stream) for the
     * given reader. This method is invoked by {@link Tile#getPreparedReader} only.
     */
    final Object getRawInput(final ImageReader reader) {
        return readerInputs.get(reader);
    }

    /**
     * Sets the raw input (<strong>not</strong> wrapped in an image input stream) for the
     * given reader. The input can be set to {@code null}, but we don't allow entry removal
     * on intend since the keys need to be returned by {@link #getReaders}.
     * <p>
     * This method is invoked by {@link Tile#getPreparedReader} only.
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
     * Computes the region and tile size for all tiles.
     *
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getReader reader} and this operation failed.
     */
    private void initialize() throws IOException {
        for (final Tile tile : getTiles()) {
            final Rectangle expand = tile.getRegion();
            if (region == null) {
                region = expand;
            } else {
                region.add(expand);
            }
            if (tileSize == null) {
                tileSize = expand.getSize();
            } else {
                if (expand.width  > tileSize.width)  tileSize.width  = expand.width;
                if (expand.height > tileSize.height) tileSize.height = expand.height;
            }
        }
    }

    /**
     * Returns the region enclosing all tiles.
     *
     * @return The region. <strong>Do not modify</strong> since it may be a direct reference to
     *         internal structures.
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getReader reader} and this operation failed.
     */
    public Rectangle getRegion() throws IOException {
        if (region == null) {
            initialize();
        }
        return region;
    }

    /**
     * Returns all tiles.
     */
    public Collection<Tile> getTiles() {
        return allTiles;
    }

    /**
     * Returns every tiles that intersect the given region.
     *
     * @param  region The region of interest (shall not be {@code null}).
     * @param  xSubsampling The number of source columns to advance for each pixel.
     * @param  ySubsampling The number of source rows to advance for each pixel.
     * @return The tiles that intercept the given region.
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getReader reader} and this operation failed.
     */
    public Collection<Tile> getTiles(final Rectangle region, final int xSubsampling, final int ySubsampling)
            throws IOException
    {
        if (tilesOfInterest == null || !regionOfInterest.equals(region)) {
            tilesOfInterest = null; // Safety in case of failure.
            regionOfInterest.setBounds(region);
            tilesOfInterest = getTileOfInterest(xSubsampling, ySubsampling);
        }
        return tilesOfInterest;
    }

    /**
     * Returns every tiles that intersect the {@linkplain #regionOfInterest region of interest},
     * which must be set before this methos is invoked. At the difference of {@link #getTiles},
     * this method does not use any cache - the search is performed inconditionnaly.
     */
    private Collection<Tile> getTileOfInterest(final int xSubsampling, final int ySubsampling)
            throws IOException
    {
        final Map<Rectangle,Tile> interest = new LinkedHashMap<Rectangle,Tile>();
        for (final Tile tile : tiles) {
            if (tile.canSubsample(xSubsampling, ySubsampling)) {
                // TODO: This check should be replaced by iteration over the values returned
                //       by a RTree. We could consider org.geotools.index.RTree, but we need
                //       to clean that code first (API that should not be public, should be
                //       a java.util.Collection, avoid dependencies to JTS, search(Envelope)
                //       should returns a Collection backed by lazy iterator, etc.) and we
                //       may need to add a 'RTree subtree(Envelope)' method.
                Rectangle region = tile.getRegion();
                if (regionOfInterest.intersects(region)) {
                    region = region.intersection(regionOfInterest);
                    final Tile old = interest.put(region, tile);
                    if (old != null) {
                        /*
                         * Found a tile with the same bounding box than the new tile. It is
                         * probably a tile at a different resolution. Retains the one which
                         * minimize the disk reading, and discard the other one. This check
                         * is not generic since we search for an exact match, but this case
                         * is common enough. Handling it with a HashMap will help to reduce
                         * the amount of tiles to handle in a more costly way later.
                         */
                        if (old .countWastedPixels(region, xSubsampling, ySubsampling) <
                            tile.countWastedPixels(region, xSubsampling, ySubsampling))
                        {
                            interest.put(region, old); // Keep the old tile, discart the new one.
                        }
                    }
                }
            }
        }
        /*
         * TODO: We could put more logic here by removing overlapping tiles, if any.
         * TODO: sort the result.
         */
        return Collections.unmodifiableCollection(interest.values());
    }

    /**
     * Returns {@code true} if there is more than one tile.
     */
    public boolean isImageTiled() {
        // Don't invoke 'getTiles' because we want to avoid the call to Tile.getRegion().
        return tiles.length >= 2;
    }

    /**
     * Returns the tiles dimension.
     *
     * @return The tiles dimension. <strong>Do not modify</strong> since it may be a direct
     *         reference to internal structures.
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getReader reader} and this operation failed.
     */
    public Dimension getTileSize() throws IOException {
        if (tileSize == null) {
            initialize();
        }
        return tileSize;
    }

    /**
     * Closes the specified stream, if it is closeable.
     */
    private static void close(final Object input) throws IOException {
        if (input instanceof ImageInputStream) {
            ((ImageInputStream) input).close();
        } else if (input instanceof Closeable) {
            ((Closeable) input).close();
        }
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
                if (input != rawInput) {
                    close(input);
                }
            } else {
                close(input);
                if (rawInput != input) {
                    close(rawInput);
                }
                reader.dispose();
            }
        }
    }

    /**
     * Returns a string representation of this tile manager.
     */
    @Override
    public String toString() {
        return Tile.toString(allTiles);
    }
}
