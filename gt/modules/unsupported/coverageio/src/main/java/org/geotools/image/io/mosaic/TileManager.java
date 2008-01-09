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

import java.util.*; // We use really a lot of those imports.
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform; // For javadoc
import java.io.IOException;
import javax.imageio.spi.ImageReaderSpi;
import org.geotools.coverage.grid.ImageGeometry;
import org.geotools.resources.UnmodifiableArrayList;
import org.geotools.resources.Utilities;
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
     * The {@linkplain #tiles} in a tree for faster access.
     * Will be created only when first needed.
     */
    private transient RTree tree;

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
     * The grid geometry, including the "<cite>grid to real world</cite>" transform.  This is
     * provided by {@link TileManagerFactory} when this information is available and returned
     * by {@link #getGridGeometry}, but is not used by this class.
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
            final Rectangle expand = tile.getAbsoluteRegion();
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
    public synchronized Rectangle getRegion() throws IOException {
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
    public synchronized Collection<Tile> getTiles(final Rectangle region,
            final int xSubsampling, final int ySubsampling) throws IOException
    {
        if (tilesOfInterest == null || !regionOfInterest.equals(region) ||
            this.xSubsampling != xSubsampling || this.ySubsampling != ySubsampling)
        {
            this.tilesOfInterest = null; // Safety in case of failure.
            this.xSubsampling    = xSubsampling;
            this.ySubsampling    = ySubsampling;
            this.regionOfInterest.setBounds(region);
            searchTiles(false);
        }
        return tilesOfInterest;
    }

    /**
     * Returns every tiles that intersect the {@linkplain #regionOfInterest region of interest},
     * which must be set before this method is invoked. At the difference of {@link #getTiles},
     * this method does not use any cache - the search is performed inconditionnaly.
     * <p>
     * On input, the following field must be set:
     * <ul>
     *   <li>{@link #regionOfInterest}</li>
     *   <li>{@link #xSubsampling}</li>
     *   <li>{@link #ySubsampling}</li>
     * </ul>
     * <p>
     * On output, the following field will be set:
     * <ul>
     *   <li>{@link #tilesOfInterest}</li>
     *   <li>{@link #xSubsampling} if {@code allowSubsamplingChange} is {@code true}</li>
     *   <li>{@link #ySubsampling} if {@code allowSubsamplingChange} is {@code true}</li>
     * </ul>
     *
     * @return The estimated {@linkplain Tile#countUnwantedPixels amount of unwanted pixels}.
     */
    private long searchTiles(final boolean allowSubsamplingChange) throws IOException {
        long lowestCost = Long.MAX_VALUE;
        if (tree == null) {
            tree = new RTree(tiles);
        }
        // The list of tiles to consider must be a copy, because it will be emptied.
        final Collection<Tile> tiles = tree.intersect(region);
        Map<Rectangle,Tile> candidates=null, bestCandidates=null;
        /*
         * If 'allowSubsamplingChange' is false, the 'do' block below will be executed exactly once.
         * Otherwise, 'subsamplingDone' and 'subsamplingToTry' will be created when first needed and
         * the loop will be executed as long as there is new subsamplings to try. We will retain the
         * one having the lowest cost.
         */
        Set  <Dimension> subsamplingDone  = null;
        Queue<Dimension> subsamplingToTry = Utilities.emptyQueue();
        Dimension  subsampling = new Dimension(xSubsampling, ySubsampling);
        final Dimension buffer = new Dimension();
        do {
            long cost = 0;
            if (candidates == bestCandidates) {
                 // Works on a new map in order to protect 'bestCandidates' from changes.
                candidates = new LinkedHashMap<Rectangle,Tile>();
            }
            for (final Iterator<Tile> it=tiles.iterator(); it.hasNext();) {
                final Tile tile = it.next();
                final Dimension floor = tile.getSubsamplingFloor(subsampling);
                if (floor == null) {
                    /*
                     * A tile is unable to read its image at the given subsampling or any smaller
                     * subsampling. There is no reason to keep it for further examination in this
                     * method. Remove it for faster execution in subsequent run of the outer loop.
                     */
                    it.remove();
                    continue;
                }
                if (floor != subsampling) {
                    /*
                     * A tile is unable to read its image at the given subsampling, but would
                     * be capable if the subsampling was smaller. If we are allowed to change
                     * the setting, add this item to the queue of subsampling to try later.
                     */
                    if (allowSubsamplingChange) {
                        if (subsamplingDone == null) {
                            subsamplingDone  = new HashSet<Dimension>();
                            subsamplingToTry = new LinkedList<Dimension>();
                        }
                        if (subsamplingDone.add(floor)) {
                            subsamplingToTry.add(floor);
                        }
                    }
                    continue;
                }
                /*
                 * The tile is capable to read its image at the given subsampling. Computes the
                 * cost that reading this tile would have.
                 */
                final Rectangle region = tile.getAbsoluteRegion();
                final Rectangle toRead = region.intersection(regionOfInterest);
                final Tile previousTile = candidates.put(toRead, tile);
                region.setBounds(toRead); // From this point, we will use 'region' as a buffer.
                buffer.setSize(subsampling);
                int delta = tile.countUnwantedPixelsFromAbsolute(region, buffer);
                cost += delta;
                if (previousTile == null) {
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
                region.setBounds(toRead);
                buffer.setSize(subsampling);
                delta -= previousTile.countUnwantedPixelsFromAbsolute(region, buffer);
                if (delta >= 0) {
                    // Previous tile had a cost equals or lower.
                    // Keep the old tile, discart the new one.
                    cost -= delta;
                    candidates.put(toRead, previousTile);
                }
            }
            /*
             * TODO: We could put more logic here by removing overlapping tiles, if any.
             */
            /*
             * We now have the final set of tiles for current subsampling. Checks if the cost
             * of this set is lower than previous sets, and keep as "best candidates" if it is.
             * If there is other subsamplings to try, we redo the process again in case we find
             * cheaper set of tiles.
             */
            if (cost < lowestCost) {
                lowestCost = cost;
                bestCandidates = candidates;
                xSubsampling = subsampling.width;
                ySubsampling = subsampling.height;
            } else {
                candidates.clear(); // Reuses the existing HashMap.
            }
        } while ((subsampling = subsamplingToTry.poll()) != null);
        /*
         * TODO: sort the result.
         */
        tilesOfInterest = Collections.unmodifiableCollection(bestCandidates.values());
        return lowestCost;
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
