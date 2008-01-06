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

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform; // For javadoc
import java.io.IOException;
import javax.imageio.spi.ImageReaderSpi;
import org.geotools.coverage.grid.ImageGeometry;
import org.geotools.resources.UnmodifiableArrayList;
import org.geotools.util.Comparators;


/**
 * A collection of {@link Tile} objects to be given to {@link MosaicImageReader}. This base
 * class does not assume that the tiles are arranged in any particular order (especially grids).
 * But subclasses can make such assumption for better performances.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TileManager {
    /**
     * The tiles sorted by {@linkplain Tile#getInput input}) first, then by
     * {@linkplain Tile#getImageIndex image index}. If an iteration must be
     * performed over every tiles, doing the iteration in this array order
     * should be more efficient than other order.
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
     * The grid geometry, including the "<cite>grid to real world</cite>" transform. This is
     * provided by {@link TileManagerFactory} when this information is available, but is not
     * used by this class.
     */
    ImageGeometry geometry;

    /**
     * All image providers used as an unmodifiable set.
     */
    private final Set<ImageReaderSpi> providers;

    /**
     * Creates a manager for the given tiles. This constructor is protected for subclassing,
     * but should not invoked directly. {@code TileManager} instances should be created by
     * {@link TileManagerFactory}.
     *
     * @param tiles The tiles. This array is not cloned and elements in this array may be
     *        reordered by this constructor. The public methods in {@link TileManagerFactory}
     *        are reponsible for cloning the user-provided arrays if needed.
     */
    protected TileManager(final Tile[] tiles) {
        /*
         * Puts together the tiles that use the same input. For those that use
         * different input, we will order by image index first, then (y,x) order.
         */
        final Set<ImageReaderSpi> providers;
        final Map<ReaderInputPair,List<Tile>> tilesByInput;
        tilesByInput = new LinkedHashMap<ReaderInputPair, List<Tile>>();
        providers    = new LinkedHashSet<ImageReaderSpi>(4);
        for (final Tile tile : tiles) {
            tile.checkGeometryValidity();
            final ImageReaderSpi  spi = tile.getImageReaderSpi();
            final ReaderInputPair key = new ReaderInputPair(spi, tile.getInput());
            List<Tile> sameInputs = tilesByInput.get(key);
            if (sameInputs == null) {
                sameInputs = new ArrayList<Tile>(4);
                tilesByInput.put(key, sameInputs);
                providers.add(spi);
            }
            sameInputs.add(tile);
        }
        this.providers = Collections.unmodifiableSet(providers);
        /*
         * Overwrites the tiles array with the same tiles, but ordered with same input firsts.
         */
        @SuppressWarnings("unchecked")
        final List<Tile>[] asArray = tilesByInput.values().toArray(new List[tilesByInput.size()]);
        final Comparator<List<Tile>> comparator = Comparators.forLists();
        Arrays.sort(asArray, comparator);
        int numTiles = 0;
fill:   for (final List<Tile> sameInputs : asArray) {
            assert !sameInputs.isEmpty();
            Collections.sort(sameInputs);
            for (final Tile tile : sameInputs) {
                tiles[numTiles++] = tile;
            }
        }
        this.tiles = tiles;
        allTiles = UnmodifiableArrayList.wrap(tiles);
        regionOfInterest = new Rectangle();
    }

    /**
     * Computes the region and tile size for all tiles.
     *
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getImageReader reader} and this operation failed.
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
     * Returns all image reader providers used by the tiles. The set will typically contains
     * only one element, but more are allowed.
     *
     * @see MosaicImageReader#getTileReaderSpis
     */
    public Set<ImageReaderSpi> getImageReaderSpis() {
        return providers;
    }

    /**
     * Returns the region enclosing all tiles.
     *
     * @return The region. <strong>Do not modify</strong> since it may be a direct reference to
     *         internal structures.
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getImageReader reader} and this operation failed.
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
     *         {@linkplain Tile#getImageReader reader} and this operation failed.
     */
    public Collection<Tile> getTiles(final Rectangle region, final int xSubsampling, final int ySubsampling)
            throws IOException
    {
        if (tilesOfInterest == null || !regionOfInterest.equals(region) ||
            this.xSubsampling != xSubsampling || this.ySubsampling != ySubsampling)
        {
            this.tilesOfInterest = null; // Safety in case of failure.
            this.regionOfInterest.setBounds(region);
            this.xSubsampling    = xSubsampling;
            this.ySubsampling    = ySubsampling;
            this.tilesOfInterest = getTileOfInterest();
        }
        return tilesOfInterest;
    }

    /**
     * Returns every tiles that intersect the {@linkplain #regionOfInterest region of interest},
     * which must be set before this methos is invoked. At the difference of {@link #getTiles},
     * this method does not use any cache - the search is performed inconditionnaly.
     */
    private Collection<Tile> getTileOfInterest() throws IOException {
        final Map<Rectangle,Tile> interest = new LinkedHashMap<Rectangle,Tile>();
        for (final Tile tile : tiles) {
            if (tile.canSubsample(xSubsampling, ySubsampling)) {
                // TODO: This check should be replaced by iteration over the values returned
                //       by a RTree. We could consider org.geotools.index.RTree, but we need
                //       to clean that code first (API that should not be public, should be
                //       a java.util.Collection, avoid dependencies to JTS, search(Envelope)
                //       should returns a Collection backed by lazy iterator, etc.) and we
                //       may need to add a 'RTree subtree(Envelope)' method.
                final Rectangle region = tile.getAbsoluteRegion();
                if (regionOfInterest.intersects(region)) {
                    final Rectangle toRead = region.intersection(regionOfInterest);
                    final Tile old = interest.put(toRead, tile);
                    if (old == null) {
                        continue;
                    }
                    /*
                     * Found a tile with the same bounding box than the new tile. It is
                     * probably a tile at a different resolution. Retains the one which
                     * minimize the disk reading, and discard the other one. This check
                     * is not generic since we search for an exact match, but this case
                     * is common enough. Handling it with a HashMap will help to reduce
                     * the amount of tiles to handle in a more costly way later.
                     */
                    final int n1, n2;
                    region.setBounds(toRead);
                    n1 = old.countUnwantedPixelsFromAbsolute(region, xSubsampling, ySubsampling);
                    region.setBounds(toRead);
                    n2 = tile.countUnwantedPixelsFromAbsolute(region, xSubsampling, ySubsampling);
                    if (n1 <= n2) {
                        interest.put(toRead, old); // Keep the old tile, discart the new one.
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
     *         {@linkplain Tile#getImageReader reader} and this operation failed.
     */
    public Dimension getTileSize() throws IOException {
        if (tileSize == null) {
            initialize();
        }
        return tileSize;
    }

    /**
     * Returns the grid geometry, including the "<cite>grid to real world</cite>" transform.
     * This information is typically available only when {@linkplain AffineTransform affine
     * transform} were explicitly given to {@linkplain Tile#Tile(ImageReaderSpi,Object,int,
     * Dimension,AffineTransform) tile constructor}.
     *
     * @return The grid geometry, or {@code null} if this information is not available.
     *
     * @see Tile#getGridToCRS
     */
    public ImageGeometry getGridGeometry() {
        return geometry;
    }

    /**
     * Returns a hash code value for this tile manager.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(tiles) ^ 83;
    }

    /**
     * Compares this tile manager with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object != null && object.getClass().equals(getClass())) {
            final TileManager that = (TileManager) object;
            return Arrays.equals(this.tiles, that.tiles);
        }
        return false;
    }

    /**
     * Returns a string representation of this tile manager.
     */
    @Override
    public String toString() {
        return Tile.toString(allTiles);
    }
}
