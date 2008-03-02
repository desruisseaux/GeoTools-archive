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
import java.awt.geom.AffineTransform;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import org.geotools.coverage.grid.ImageGeometry;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.resources.UnmodifiableArrayList;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.util.Comparators;
import org.geotools.util.LRULinkedHashMap;


/**
 * A collection of {@link Tile} objects to be given to {@link MosaicImageReader}. This base
 * class does not assume that the tiles are arranged in any particular order (especially grids).
 * But subclasses can make such assumption for better performances.
 * <p>
 * {@code TileManager}s are {@linkplain Serializable serializable} if all their tiles have a
 * serializable {@linkplain Tile#getInput input}. The {@link ImageReaderSpi} doesn't need to
 * be serializable, but its class must be known to {@link javax.imageio.spi.IIORegistry} at
 * deserialization time.
 * <p>
 * This class is thread-safe but default implementation is not scalable to a high number of
 * concurrent threads. Up to 4 concurrent calls to {@link #getTile getTile} should be okay,
 * more may slow down the execution.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TileManager implements Serializable {
    /**
     * For cross-version compatibility during serialization.
     */
    private static final long serialVersionUID = -6070623930537957163L;

    /**
     * The expected maximal number of concurrent threads using the same {@link TileManager}
     * instance. There is no risk of heratic behavior if the number of concurrent threads
     * exceed this constant - {@link TileManager} would just become slower.
     * <p>
     * This number should be small because the code is rather simple and not designed for
     * scalability in highly concurrent context.
     */
    private static final int CONCURRENT_THREADS = 4;

    /**
     * The tiles sorted by {@linkplain Tile#getInput input}) first, then by
     * {@linkplain Tile#getImageIndex image index}. If an iteration must be
     * performed over every tiles, doing the iteration in this array order
     * should be more efficient than other order.
     */
    private final Tile[] tiles;

    /**
     * All tiles wrapped in an unmodifiable list.
     * <p>
     * Consider this field as final. It is not because it needs to be set by {@link #readObject}.
     * If this field become public or protected in a future version, then we should make it final
     * and use reflection like {@link org.geotools.coverage.grid.GridCoverage2D#readObject}.
     */
    private transient Collection<Tile> allTiles;

    /**
     * The {@linkplain #tiles} as a trees for faster access. The array and its elements
     * will be created only when first needed. Every elements after the first one are
     * {@linkplain RTree#clone clones}.
     * <p>
     * The work performed by {@link RTree} may be relatively expensive, so we use different
     * instance per thread if there is concurrent usage of {@link TileManager}.
     */
    private transient RTree[] trees;

    /**
     * The tiles in the region of interest. We need to retains the last collection because
     * {@link MosaicImageReader} implementation may ask the same one a few consecutive times.
     * Only the last collection would have been enough if we didn't allowed concurrent access
     * to {@link TileManager}. But because of concurrency, we need a little bit more than only
     * the last request.
     * <p>
     * Note that we make no attempt to block a thread if an other thread is already computing tiles
     * for the same ROI. The intend here is to preserve (if possible) the last calculation performed
     * by any thread, not to implement a real cache. The later is assumed caller's responsability.
     */
    private transient Map<SubsampledRectangle,Collection<Tile>> tilesOfInterest;

    /**
     * The region enclosing all tiles. Will be computed only when first needed.
     */
    private transient Rectangle region;

    /**
     * The tile dimensions. Will be computed only when first needed.
     */
    private transient Dimension tileSize;

    /**
     * The grid geometry, including the "<cite>grid to real world</cite>" transform.  This is
     * provided by {@link TileManagerFactory} when this information is available and returned
     * by {@link #getGridGeometry}, but is not used by this class.
     */
    ImageGeometry geometry;

    /**
     * All image providers used as an unmodifiable set.
     * <p>
     * Consider this field as final. It is not because it needs to be set by {@link #readObject}.
     * If this field become public or protected in a future version, then we should make it final
     * and use reflection like {@link org.geotools.coverage.grid.GridCoverage2D#readObject}.
     */
    private transient Set<ImageReaderSpi> providers;

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
    }

    /**
     * Sets the {@linkplain Tile#getGridTocRS grid to CRS} transform for every tiles. A copy of
     * the supplied affine transform is {@linkplain AffineTransform#scale scaled} according the
     * {@linkplain Tile#getSubsampling subsampling} of each tile. Tiles having the same
     * subsampling will share the same immutable instance of affine transform.
     * <p>
     * The <cite>grid to CRS</cite> transform is not necessary for proper working of {@linkplain
     * MosaicImageReader mosaic image reader}, but is provided as a convenience for users.
     * <p>
     * This method can be invoked only once.
     *
     * @param gridToCRS The "grid to CRS" transform.
     * @throws IllegalStateException if a transform was already assigned to at least one tile.
     */
    public synchronized void setGridToCRS(final AffineTransform gridToCRS)
            throws IllegalStateException
    {
        final Map<Dimension,AffineTransform> shared = new HashMap<Dimension,AffineTransform>();
        for (final Tile tile : tiles) {
            final Dimension subsampling = tile.getSubsampling();
            AffineTransform at = shared.get(subsampling);
            if (at == null) {
                at = new AffineTransform(gridToCRS);
                at.scale(subsampling.width, subsampling.height);
                at = new XAffineTransform(at);
                shared.put(subsampling, at);
            }
            tile.setGridToCRS(at);
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
    final synchronized Rectangle getRegion() throws IOException {
        if (region == null) {
            final RTree tree = getTree();
            try {
                region = tree.getBounds();
            } finally {
                release(tree);
            }
        }
        return region;
    }

    /**
     * Returns the RTree, creating it if necessary. Calls to this method must be followed by a
     * {@code try ... finally} block with call to {@link #release} in the {@code finally} block.
     */
    private RTree getTree() throws IOException {
        assert Thread.holdsLock(this);
        if (trees == null) {
            final ThreadGroup threads = new ThreadGroup("RTree");
            final TreeNode    root    = new TreeNode(tiles, threads);
            final RTree       tree    = new RTree(root);
            final RTree[]     trees   = new RTree[CONCURRENT_THREADS];
            trees[0] = tree;
            root.join(threads); // Wait for the background construction to finish.
            root.setReadOnly();
            assert root.containsAll(allTiles);
            this.trees = trees; // Save last so it is saved only on success.
        }
        /*
         * Returns the first instance available for use,
         * creating a new one if we hit an empty slot.
         */
        for (int i=0; i<trees.length; i++) {
            RTree tree = trees[i];
            if (tree == null) {
                trees[i] = tree = trees[0].clone();
            } else if (tree.inUse) {
                continue;
            }
            tree.inUse = true;
            return tree;
        }
        // Every instances are in use. Returns a clone to be discarted after usage.
        return trees[0].clone();
    }

    /**
     * Releases a tree acquired by {@link #getTree}. We do not synchronize
     * because {@link RTree#inUse} is declared as a volatile field.
     */
    private void release(final RTree tree) {
        // Safety for avoiding NullPointerException to be thrown in 'finally' block.
        if (tree != null) {
            tree.inUse = false;
        }
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
     * @param region
     *          The region of interest (shall not be {@code null}).
     * @param subsampling
     *          On input, the number of source columns and rows to advance for each pixel. On
     *          output, the effective values to use. Those values may be different only if
     *          {@code subsamplingChangeAllowed} is {@code true}.
     * @param subsamplingChangeAllowed
     *          If {@code true}, this method is allowed to replace {@code subsampling} by the
     *          highest subsampling that overviews can handle, not greater than the given
     *          subsampling.
     * @return The tiles that intercept the given region.
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getImageReader reader} and this operation failed.
     */
    public Collection<Tile> getTiles(final Rectangle region, final Dimension subsampling,
                                     final boolean subsamplingChangeAllowed) throws IOException
    {
        final SubsampledRectangle regionOfInterest = new SubsampledRectangle(region, subsampling);
        Collection<Tile> values;
        final RTree tree;
        synchronized (this) {
            if (tilesOfInterest == null) {
                tilesOfInterest = LRULinkedHashMap.createForRecentAccess(CONCURRENT_THREADS * 2);
                /*
                 * We create a map with greater capacity than the expected maximum number of
                 * concurrent threads because if a thread is fast enough for executing two queries
                 * while an other thread executed only one, the result of the slow thread would be
                 * lost. Using a capacity twice bigger is an arbitrary choice (a thread could be 3
                 * time faster), but we assume that it is enough for typical usages. Insuffisient
                 * value can slow down the execution, but the result still valids.
                 */
            }
            values = tilesOfInterest.get(regionOfInterest);
            if (values != null) {
                return values;
            }
            tree = getTree();
        }
        try {
            // Initializes the tree with the search criterions.
            tree.regionOfInterest = regionOfInterest;
            tree.subsamplingChangeAllowed = subsamplingChangeAllowed;
            values = UnmodifiableArrayList.wrap(tree.searchTiles());
            synchronized (this) {
                // After the search, saves the results.
                tilesOfInterest.put(regionOfInterest, values);
                subsampling.setSize(regionOfInterest.xSubsampling, regionOfInterest.ySubsampling);
            }
        } finally {
            release(tree);
        }
        return values;
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
    final synchronized Dimension getTileSize() throws IOException {
        if (tileSize == null) {
            final RTree tree = getTree();
            try {
                tileSize = tree.getTileSize();
            } finally {
                release(tree);
            }
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

    /**
     * Checks for file existence and image size of every tiles and reports any error found.
     *
     * @param out Where to report errors ({@code null} for default, which is the
     *            {@linkplain System#out standard output stream}).
     */
    public void printErrors(PrintWriter out) {
        if (out == null) {
            out = new PrintWriter(System.out, true);
        }
        for (final Tile tile : tiles) {
            final int imageIndex = tile.getImageIndex();
            ImageReader reader = null;
            String message = null;
            try {
                final Rectangle region = tile.getRegion();
                reader = tile.getImageReader(null, true, true);
                final int width  = reader.getWidth (imageIndex);
                final int height = reader.getHeight(imageIndex);
                if (width != region.width || height != region.height) {
                    message = Errors.format(ErrorKeys.UNEXPECTED_IMAGE_SIZE);
                }
            } catch (IOException exception) {
                message = exception.toString();
            } catch (RuntimeException exception) {
                message = exception.toString();
            }
            if (message != null) {
                out.println(tile);
                out.print("    ");
                out.println(message);
            }
            if (reader != null) {
                reader.dispose();
            }
        }
    }

    /**
     * Invoked on deserialization. Restores the transient fields that are usuly computed at
     * construction time. Doing so immediately instead of relying on lazy creation allows us
     * to avoid synchronization.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        allTiles = UnmodifiableArrayList.wrap(tiles);
        providers = new LinkedHashSet<ImageReaderSpi>(4);
        for (final Tile tile : tiles) {
            providers.add(tile.getImageReaderSpi());
        }
        providers = Collections.unmodifiableSet(providers);
    }
}
