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
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.geotools.coverage.grid.ImageGeometry;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.resources.UnmodifiableArrayList;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.util.FrequencySortedSet;
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
 * <p>
 * This class is thread-safe but default implementation is not scalable to a high number of
 * concurrent threads. Up to 4 concurrent calls to {@link #getTiles getTiles} should be okay,
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
     * The region enclosing all tiles. Will be computed only when first needed.
     */
    private transient Rectangle region;

    /**
     * The tile dimensions. Will be computed only when first needed.
     */
    private transient Dimension tileSize;

    /**
     * The grid geometry, including the "<cite>grid to real world</cite>" transform.
     * This is provided by {@link TileManagerFactory} when this information is available.
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
     * A view of the tile as a Swing tree. Created only when first requested.
     */
    private transient TreeModel swing;

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
        providers    = new FrequencySortedSet<ImageReaderSpi>(4, true);
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
     * @throws IOException if an I/O operation was required and failed.
     */
    public synchronized void setGridToCRS(final AffineTransform gridToCRS)
            throws IllegalStateException, IOException
    {
        if (geometry != null) {
            throw new IllegalStateException();
        }
        final Map<Dimension,AffineTransform> shared = new HashMap<Dimension,AffineTransform>();
        AffineTransform at = new XAffineTransform(gridToCRS);
        shared.put(new Dimension(1,1), at);
        geometry = new ImageGeometry(getRegion(), at);
        for (final Tile tile : tiles) {
            final Dimension subsampling = tile.getSubsampling();
            at = shared.get(subsampling);
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
     * Returns the grid geometry, including the "<cite>grid to real world</cite>" transform.
     * This information is typically available only when {@linkplain AffineTransform affine
     * transform} were explicitly given to {@linkplain Tile#Tile(ImageReaderSpi,Object,int,
     * Dimension,AffineTransform) tile constructor}.
     *
     * @return The grid geometry, or {@code null} if this information is not available.
     * @throws IOException if an I/O operation was required and failed.
     *
     * @see Tile#getGridToCRS
     */
    public ImageGeometry getGridGeometry() throws IOException {
        if (geometry == null) {
            /*
             * The gridToCRS transform is the same one than the one of the tile having origin at
             * (0,0) and subsampling of (1,1).  So we search for exactly this tile and currently
             * accept no other one. In a future version we could accept an other tile (but which
             * one?) and translate the affine transform...  But the result could be wrong if the
             * gridToCRS transform is not computed by RegionCalculator. Only the particular tile
             * searched by current implementation should be okay in all cases.
             */
            for (final Tile tile : tiles) {
                final Dimension subsampling = tile.getSubsampling();
                if (subsampling.width != 1 || subsampling.height != 1) {
                    continue;
                }
                final Point origin = tile.getLocation();
                if (origin.x != 0 || origin.y != 0) {
                    continue;
                }
                final AffineTransform gridToCRS = tile.getGridToCRS();
                if (gridToCRS == null) {
                    continue;
                }
                geometry = new ImageGeometry(getRegion(), gridToCRS);
                break;
            }
        }
        return geometry;
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
    private synchronized RTree getTree() throws IOException {
        if (trees == null) {
            final TreeNode root  = new GridNode(tiles);
            final RTree    tree  = new RTree(root);
            final RTree[]  trees = new RTree[CONCURRENT_THREADS];
            trees[0] = tree;
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
    private synchronized void release(final RTree tree) {
        // Safety for avoiding NullPointerException to be thrown in 'finally' block.
        if (tree != null) {
            tree.inUse = false;
        }
    }

    /**
     * Returns all image reader providers used by the tiles. The set will typically contains
     * only one element, but more are allowed. In the later case, the entries in the set are
     * sorted from the most frequently used provider to the less frequently used.
     *
     * @see MosaicImageReader#getTileReaderSpis
     */
    public Set<ImageReaderSpi> getImageReaderSpis() {
        return providers;
    }

    /**
     * Creates a tile with a {@linkplain Tile#getRegion region} big enough for containing
     * {@linkplain #getTiles every tiles}. The created tile has a {@linkplain Tile#getSubsampling
     * subsampling} of (1,1). This is sometime useful for creating a "virtual" image representing
     * the assembled mosaic as a whole.
     *
     * @param  provider
     *              The image reader provider to be given to the created tile, or {@code null} for
     *              inferring it automatically. In the later case the provider is inferred from the
     *              input suffix if any (e.g. the {@code ".png"} extension in a filename), or
     *              failing that most frequently used provider is selected.
     * @param  input
     *              The input to be given to the created tile. It doesn't need to be an existing
     *              {@linkplain java.io.File file} or URI since this method will not attempt to
     *              read it.
     * @param  imageIndex
     *              The image index to be given to the created tile (usually 0).
     * @return A global tile big enough for containing every tiles in this manager.
     * @throws NoSuchElementException
     *              If this manager do not contains at least one tile.
     * @throws IOException
     *              If an I/O operation was required and failed.
     */
    public Tile createGlobalTile(ImageReaderSpi provider, final Object input, final int imageIndex)
            throws NoSuchElementException, IOException
    {
        if (provider == null) {
            // Following line may throw the NoSuchElementException documented in javadoc.
            provider = getImageReaderSpis().iterator().next();
            ImageReaderSpi inferred = Tile.getImageReaderSpi(input);
            if (inferred != null && inferred != provider) {
                final Collection<String> f1 = Arrays.asList(provider.getFormatNames());
                final Collection<String> f2 = Arrays.asList(inferred.getFormatNames());
                if (!f1.containsAll(f2)) {
                    provider = inferred;
                }
            }
        }
        final Tile tile;
        final ImageGeometry geometry = getGridGeometry();
        if (geometry == null) {
            tile = new Tile(provider, input, imageIndex, getRegion());
        } else {
            tile = new Tile(provider, input, imageIndex, geometry.getGridRange());
            tile.setGridToCRS(geometry.getGridToCRS());
        }
        return tile;
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
     * @return The tiles that intercept the given region. May be empty but never {@code null}.
     * @throws IOException if it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getImageReader reader} and this operation failed.
     */
    public Collection<Tile> getTiles(final Rectangle region, final Dimension subsampling,
                                     final boolean subsamplingChangeAllowed) throws IOException
    {
        final RTree tree = getTree();
        final Collection<Tile> values;
        try {
            // Initializes the tree with the search criterions.
            tree.regionOfInterest = region;
            tree.setSubsampling(subsampling);
            tree.subsamplingChangeAllowed = subsamplingChangeAllowed;
            values = tree.searchTiles();
            subsampling.setSize(tree.xSubsampling, tree.ySubsampling);
            tree.regionOfInterest = null; // Just as a safety (not really required).
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
     * Returns this tree as a <cite>Swing</cite> tree model. The labels displayed in this tree
     * may change in any future version. This method is provided only as a debugging tools.
     */
    public synchronized TreeModel toSwingTree() throws IOException {
        if (swing == null) {
            final RTree tree = getTree();
            try {
                swing = new DefaultTreeModel(tree.root);
            } finally {
                release(tree);
            }
        }
        return swing;
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
        providers = new FrequencySortedSet<ImageReaderSpi>(4, true);
        for (final Tile tile : tiles) {
            providers.add(tile.getImageReaderSpi());
        }
        providers = Collections.unmodifiableSet(providers);
    }
}
