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
import java.io.IOException;
import org.geotools.resources.XArray;


/**
 * An R-Tree like structure having a {@link TreeNode} has its root. This is not a real RTree but
 * provides a few similar features tuned for {@link TileManager} needs (especially regarding the
 * management of subsampling information).
 * <p>
 * This class is <strong>not</strong> thread safe. Instances can be {@linkplain #clone cloned} if
 * needed for concurrent access in different threads. The {@link TreeNode} will not be duplicated
 * so cloning an {@link RTree} can be seen as creating a new worker for the same tree.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class RTree implements Comparator<GridNode> {
    /**
     * The root of the tree.
     */
    protected final TreeNode root;

    /**
     * The requested region. This field must be set before {@link #searchTiles} is invoked.
     */
    protected Rectangle regionOfInterest;

    /**
     * The subsamplings. Before the search, must be set at the requested subsamplings.
     * After the search, they are set to the subsamplings of the best set of tiles found.
     */
    protected int xSubsampling, ySubsampling;

    /**
     * {@code true} if the search is allowed to look for tiles with finer subsampling than the
     * specified one. This field must be set before {@link #searchTiles} is invoked.
     */
    protected boolean subsamplingChangeAllowed;

    /**
     * Initialized to ({@link #xSubsampling}, {@link #ySubsampling}) at the begining
     * of a search, then modified during the search for internal purpose.
     */
    private Dimension subsampling;

    /**
     * Modified value of {@link #subsampling}.
     * This is a temporary value modified during searchs.
     */
    private final Dimension tmpSubsampling;

    /**
     * A modified value of the region to read.
     * This is a temporary value modified during searchs.
     */
    private final Rectangle tmpReadRegion;

    /**
     * The subsampling done so far. This is used during
     * search and emptied once the search is finished.
     */
    private final Set<Dimension> subsamplingDone;

    /**
     * Additional subsampling to try. This is used during
     * search and emptied once the search is finished.
     */
    private final Queue<Dimension> subsamplingToTry;

    /**
     * {@code true} if this {@code RTree} instance is currently in use by any thread, or
     * {@code false} if it is available for use.
     */
    volatile boolean inUse;

    /**
     * Creates a RTree using the given root node.
     */
    public RTree(final TreeNode root) {
        this.root = root;
        tmpSubsampling   = new Dimension();
        tmpReadRegion    = new Rectangle();
        subsamplingDone  = new HashSet<Dimension>();
        subsamplingToTry = new LinkedList<Dimension>();
    }

    /**
     * Returns a copy of this tree.
     */
    @Override
    public RTree clone() {
        return new RTree(root);
    }

    /**
     * For sorting the tree nodes in the same order than in the array given to
     * the {@link GridNode} constructor.
     */
    public int compare(final GridNode o1, final GridNode o2) {
        return o1.index - o2.index;
    }

    /**
     * Sets the subsampling to the specified value.
     */
    public void setSubsampling(final Dimension subsampling) {
        xSubsampling = subsampling.width;
        ySubsampling = subsampling.height;
    }

    /**
     * Returns the bounding box of all tiles.
     */
    public Rectangle getBounds() {
        return new Rectangle(root);
    }

    /**
     * Returns the largest tile width and largest tile height in the children,
     * not scanning into subtrees.
     */
    public Dimension getTileSize() {
        final Dimension tileSize = new Dimension();
        for (final TreeNode node : root) {
            final GridNode child = (GridNode) node;
            final int width  = child.width  / child.xSubsampling;
            final int height = child.height / child.ySubsampling;
            if (width  > tileSize.width)  tileSize.width  = width;
            if (height > tileSize.height) tileSize.height = height;
        }
        return tileSize;
    }

    /**
     * Returns every tiles that intersect the {@linkplain #regionOfInterest region of interest},
     * which must be set before this method is invoked. This method does not use any cache - the
     * search is performed inconditionnaly.
     * <p>
     * On input, the following fields must be set:
     * <ul>
     *   <li>{@link #regionOfInterest}</li>
     *   <li>{@link #subsamplingChangeAllowed}</li>
     * </ul>
     * <p>
     * On output, the following fields will be set:
     * <ul>
     *   <li>{@link SubsampledRectangle#xSubsampling} and {@link SubsampledRectangle#ySubsampling}
     *       if {@link #allowSubsamplingChange} is {@code true}</li>
     * </ul>
     */
    public Tile[] searchTiles() throws IOException {
        assert subsamplingDone.isEmpty() && subsamplingToTry.isEmpty();
        subsampling = new Dimension(xSubsampling, ySubsampling);
        Map<Rectangle,TreeNode> candidates     = null;
        Map<Rectangle,TreeNode> bestCandidates = null;
        long lowestCost = Long.MAX_VALUE;
        try {
            do {
                if (candidates == bestCandidates) {
                     // Works on a new map in order to protect 'bestCandidates' from changes.
                    candidates = new LinkedHashMap<Rectangle,TreeNode>();
                }
                final TreeNode selected = addTileCandidate(root, candidates);
                /*
                 * We now have the final set of tiles for current subsampling. Checks if the cost
                 * of this set is lower than previous sets, and keep as "best candidates" if it is.
                 * If there is other subsamplings to try, we redo the process again in case we find
                 * cheaper set of tiles.
                 */
                if (selected != null) {
                    selected.filter(candidates);
                    final long cost = selected.cost();
                    if (cost < lowestCost) {
                        lowestCost = cost;
                        bestCandidates = candidates;
                        setSubsampling(subsampling);
                    } else {
                        candidates.clear(); // Reuses the existing HashMap.
                    }
                }
            } while ((subsampling = subsamplingToTry.poll()) != null);
        } finally {
            subsamplingToTry.clear();
            subsamplingDone .clear();
        }
        /*
         * TODO: sort the result. I'm not sure that it is worth, but if we decide that it is,
         * we should use the Comparator<GridNode> implemented by this class.
         */
        final Tile[] tiles = new Tile[bestCandidates.size()];
        int count = 0;
        for (final TreeNode node : bestCandidates.values()) {
            final Tile tile = node.getUserObject();
            if (tile != null) {
                tiles[count++] = tile;
            }
        }
        return XArray.resize(tiles, count);
    }

    /**
     * Searchs the tiles starting from the given node. This method invokes
     * itself recursively for scanning the child nodes down the tree.
     * <p>
     * If this method <em>added</em> some tiles to the reading process, their region (identical to
     * the keys in the {@code candidates} hash map) are {@linkplain SelectedNode#addChild added as
     * child} of the returned object. The children does not include tiles that <em>replaced</em>
     * existing ones rather than adding a new ones.
     *
     * @param  node The root of the subtree to examine.
     * @param  candidates The tiles that are under consideration during a search.
     * @return The tile to be read, or {@code null} if it doesn't intersect the area of interest.
     */
    private TreeNode addTileCandidate(final TreeNode node, final Map<Rectangle,TreeNode> candidates)
            throws IOException
    {
        if (!node.intersects(regionOfInterest)) {
            return null;
        }
        TreeNode selectedTile = null;
        final Tile tile = node.getUserObject();
        if (tile != null) {
            assert node.equals(tile.getAbsoluteRegion()) : tile;
            final Dimension floor = tile.getSubsamplingFloor(subsampling);
            if (floor == null) {
                /*
                 * The tile in the given node is unable to read its image at the given subsampling
                 * or any smaller subsampling. Skip this tile. However we may try its children at
                 * the end of this method, since they typically have a finer subsampling.
                 */
            } else if (floor != subsampling) {
                /*
                 * The tile in the given node is unable to read its image at the given subsampling,
                 * but would be capable if the subsampling was smaller. If we are allowed to change
                 * the setting, add this item to the queue of subsamplings to try later.
                 */
                if (subsamplingChangeAllowed) {
                    if (subsamplingDone.add(floor)) {
                        subsamplingToTry.add(floor);
                    }
                }
            } else {
                /*
                 * The tile is capable to read its image at the given subsampling.
                 * Computes the cost that reading this tile would have.
                 */
                final Rectangle readRegion = node.intersection(regionOfInterest);
                tmpReadRegion.setBounds(readRegion);
                tmpSubsampling.setSize(subsampling);
                final int cost = tile.countUnwantedPixelsFromAbsolute(tmpReadRegion, tmpSubsampling);
                selectedTile = new TreeNode(tile, readRegion, cost);
            }
        }
        if (!node.isLeaf()) {
            /*
             * If the region to read encompass entirely this node (otherwise reading a few childs
             * may be cheaper) and if the children subsampling are not higher than the tile's one
             * (they are usually not), then there is no need to continue down the tree since the
             * childs can not do better than this node.
             */
            if (selectedTile != null) {
                if (selectedTile.boundsEquals(node) && !tile.isFinerThan(subsampling)) {
                    return selectedTile;
                }
            } else {
                selectedTile = new TreeNode(null, node.intersection(regionOfInterest), 0);
            }
            /*
             * Process the children, including children of children, etc. through recursive
             * invocations of this method.
             */
            final long cost = selectedTile.cost();
            for (final TreeNode child : node) {
                selectedTile.addChild(addTileCandidate(child, candidates));
            }
            if (selectedTile.cost() - cost >= cost) {
                selectedTile.setChildren(null);
            } else {
                selectedTile.clearTile(cost);
            }
        }
        return selectedTile;
    }

    /**
     * Returns {@code true} if the rectangles in the given collection fill completly the given
     * ROI with no empty space.
     *
     * @todo This method is not yet correctly implemented. For now we performs a naive check
     *       which is suffisient for common {@link TileLayout}. We may need to revisit this
     *       method in a future version.
     */
    static boolean dense(final Rectangle roi, final Iterable<? extends Rectangle> regions) {
        Rectangle bounds = null;
        for (final Rectangle rect : regions) {
            final Rectangle inter = roi.intersection(rect);
            if (bounds == null) {
                bounds = inter;
            } else {
                bounds.add(inter); // See java.awt.Rectangle javadoc for empty rectangle handling.
            }
        }
        return bounds == null || bounds.equals(roi);
    }
}
