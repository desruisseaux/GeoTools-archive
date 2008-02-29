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


/**
 * A collection of {@link Tile} objects to be given to {@link MosaicImageReader}. This base
 * class does not assume that the tiles are arranged in any particular order (especially grids).
 * But subclasses can make such assumption for better performances.
 * <p>
 * {@code TileManager}s are {@linkplain Serializable serializable} if all their tiles have a
 * serializable {@linkplain Tile#getInput input}. The {@link ImageReaderSpi} doesn't need to
 * be serializable, but its class must be known to {@link javax.imageio.spi.IIORegistry} at
 * deserialization time.
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
     * The tiles in the region of interest, or {@code null} if not yet computed.
     */
    private transient Collection<Tile> tilesOfInterest;

    /**
     * The {@linkplain #tiles} in a tree for faster access.
     * Will be created only when first needed.
     *
     * @todo If profiling show contention in calls to the synchronized {@link #searchTiles}
     *       method, we could consider putting this tree in a {@link ThreadLocal} variable.
     *       The {@link ThreadLocal#initialValue} should be overriden in order to returns
     *       the {@link RTree#clone} of a single tree instance. Then the {@link #searchTiles}
     *       method should synchronize only when checking for the cached values, and for caching
     *       the result. The call to {@link RTree#searchTiles} itself would be unsynchronized.
     */
    private transient RTree tree;

    /**
     * The subsampling used at the time {@link #tilesOfInterest} has been computed.
     */
    private transient int xSubsampling, ySubsampling;

    /**
     * The region of interest. The reference is final, but the rectangle values will change.
     * <p>
     * Consider this field as final. It is not because it needs to be set by {@link #readObject}.
     * If this field become public or protected in a future version, then we should make it final
     * and use reflection like {@link org.geotools.coverage.grid.GridCoverage2D#readObject}.
     */
    private transient Rectangle regionOfInterest;

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
     * Creates a new tile manager initialized to the same values than the given one. This is
     * useful when wanting concurrent access to the same tile informations from different threads.
     *
     * @throws IOException if an I/O operation was required but failed.
     */
    protected TileManager(final TileManager copy) throws IOException {
        synchronized (copy) {
            tiles            = copy.tiles;
            allTiles         = copy.allTiles;
            tilesOfInterest  = copy.tilesOfInterest;
            tree             = copy.getTree().clone(); // Because RTree is not thread-safe.
            xSubsampling     = copy.xSubsampling;
            ySubsampling     = copy.ySubsampling;
            regionOfInterest = new Rectangle(copy.regionOfInterest); // Must be own instance.
            region           = copy.region;
            tileSize         = copy.tileSize;
            geometry         = copy.geometry;
        }
    }

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
    public synchronized Rectangle getRegion() throws IOException {
        if (region == null) {
            region = getTree().getBounds();
        }
        return new Rectangle(region);
    }

    /**
     * Returns the RTree, creating it if necessary.
     */
    private RTree getTree() throws IOException {
        assert Thread.holdsLock(this);
        if (tree == null) {
            final ThreadGroup threads = new ThreadGroup("TreeNode");
            final TreeNode root = new TreeNode(tiles, threads);
            final RTree tree = new RTree(root);
            root.join(threads);
            root.setReadOnly();
            assert root.containsAll(allTiles);
            this.tree = tree; // Save only on success.
        }
        return tree;
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
    public synchronized Collection<Tile> getTiles(final Rectangle region,
            final Dimension subsampling, final boolean subsamplingChangeAllowed) throws IOException
    {
        if (tilesOfInterest == null || !regionOfInterest.equals(region) ||
            xSubsampling != subsampling.width || ySubsampling != subsampling.height)
        {
            // Initializes the tree with the search criterions.
            final RTree tree = getTree();
            tree.regionOfInterest.setBounds(region);
            tree.xSubsampling = subsampling.width;
            tree.ySubsampling = subsampling.height;
            tree.subsamplingChangeAllowed = subsamplingChangeAllowed;

            // Performs the search and saves the results.
            final Tile[] result = tree.searchTiles();
            tilesOfInterest = UnmodifiableArrayList.wrap(result);
            regionOfInterest.setBounds(tree.regionOfInterest);
            xSubsampling = tree.xSubsampling;
            ySubsampling = tree.ySubsampling;
            subsampling.setSize(xSubsampling, ySubsampling);
        }
        return tilesOfInterest;
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
    public synchronized Dimension getTileSize() throws IOException {
        if (tileSize == null) {
            for (final Tile tile : getTiles()) {
                final Rectangle expand = tile.getAbsoluteRegion();
                if (tileSize == null) {
                    tileSize = expand.getSize();
                } else {
                    if (expand.width  > tileSize.width)  tileSize.width  = expand.width;
                    if (expand.height > tileSize.height) tileSize.height = expand.height;
                }
            }
        }
        return new Dimension(tileSize);
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
        regionOfInterest = new Rectangle();
    }
}
