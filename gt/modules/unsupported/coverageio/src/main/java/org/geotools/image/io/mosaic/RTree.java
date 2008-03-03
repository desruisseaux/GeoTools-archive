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
final class RTree {
    /**
     * The root of the tree.
     */
    private final TreeNode root;

    /**
     * The requested region. This field must be set before {@link #searchTiles} is invoked.
     * Before the search, its {@link SubsampledRectangle#xSubsampling xSubsampling} and
     * {@link SubsampledRectangle#ySubsampling ySubsampling} fields are the requested subsamplings.
     * After the search, they are set to the subsamplings of the best set of tiles found.
     */
    protected SubsampledRectangle regionOfInterest;

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
    private final Dimension relativeSubsampling;

    /**
     * A modified value of the region to read.
     * This is a temporary value modified during searchs.
     */
    private final Rectangle relativeReadRegion;

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
     * A stack of regions added by {@link #addTileCandidate} while iterating down the
     * tree through children. This is a LIFO stack (<cite>last in, first out</cite>).
     */
    private final List<Rectangle> candidateStack;

    /**
     * Cost of elements added in {@link #candidateStack} as the estimated
     * {@linkplain Tile#countUnwantedPixels amount of unwanted pixels}.
     * For internal usage by {@link #addTileCandidate} only.
     */
    private long costOfStack;

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
        relativeSubsampling = new Dimension();
        relativeReadRegion  = new Rectangle();
        subsamplingDone     = new HashSet<Dimension>();
        subsamplingToTry    = new LinkedList<Dimension>();
        candidateStack      = new ArrayList<Rectangle>(4);
    }

    /**
     * Returns a copy of this tree.
     */
    @Override
    public RTree clone() {
        return new RTree(root);
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
        for (final TreeNode node : root.getChildren()) {
            final int width  = node.width  / node.xSubsampling;
            final int height = node.height / node.ySubsampling;
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
     *   <li>{@link #count} as the estimated
     *       {@linkplain Tile#countUnwantedPixels amount of unwanted pixels}.</li>
     * </ul>
     */
    public Tile[] searchTiles() throws IOException {
        assert subsamplingDone .isEmpty() &&
               subsamplingToTry.isEmpty() &&
               candidateStack.isEmpty();
        subsampling = regionOfInterest.getSubsampling();
        Map<Rectangle,Tile> candidates     = null;
        Map<Rectangle,Tile> bestCandidates = null;
        long lowestCost = Long.MAX_VALUE;
        try {
            do {
                costOfStack = 0;
                if (candidates == bestCandidates) {
                     // Works on a new map in order to protect 'bestCandidates' from changes.
                    candidates = new LinkedHashMap<Rectangle,Tile>();
                }
                final long cost = addTileCandidate(root, candidates);
                /*
                 * We now have the final set of tiles for current subsampling. Checks if the cost
                 * of this set is lower than previous sets, and keep as "best candidates" if it is.
                 * If there is other subsamplings to try, we redo the process again in case we find
                 * cheaper set of tiles.
                 */
                if (cost < lowestCost) {
                    lowestCost = cost;
                    bestCandidates = candidates;
                    regionOfInterest.setSubsampling(subsampling);
                } else {
                    candidates.clear(); // Reuses the existing HashMap.
                }
            } while ((subsampling = subsamplingToTry.poll()) != null);
        } finally {
            subsamplingDone .clear();
            subsamplingToTry.clear();
            candidateStack.clear();
        }
        /*
         * TODO: sort the result. I'm not sure that it is worth, but if we decide that it is,
         * we could add an 'index' field into TreeNode to be initialized in the root TreeNode
         * construction. RTree could implement Comparator for sorting the nodes.
         */
        final Collection<Tile> tiles = bestCandidates.values();
        return tiles.toArray(new Tile[tiles.size()]);
    }

    /**
     * Searchs the tiles starting from the given node. This method invokes
     * itself recursively for scanning the child nodes down the tree.
     * <p>
     * If this method <em>added</em> some tiles to the reading process, their region (identical
     * to the keys in the {@code candidates} hash map) are added in the {@link #candidateStack}.
     * It does not include tiles that <em>replaced</em> existing ones rather than adding a new
     * ones.
     *
     * @param  node The root of the subtree to examine.
     * @param  candidates The tiles that are under consideration during a search.
     * @return The cost of the tiles added into {@code candidates}. Some child may returns a
     *         negative value if they replaced a more costly tile, but it should be unusual.
     *         The end result after completion of the root node should always be positive.
     */
    private long addTileCandidate(final TreeNode node, final Map<Rectangle,Tile> candidates)
            throws IOException
    {
        if (!node.intersects(regionOfInterest)) {
            return 0;
        }
        /*
         * Checks if the tile is able to read its image at the given subsampling or any smaller
         * subsampling. If not (which is indicated by a null floor), there is no reason to keep
         * it for further examination, but we still need to investigate its children.
         */
        long      cost       = 0;      // The value to be returned by this method.
        int       tileCost   = 0;      // Cost of the tile under the 'readRegion' key.
        boolean   tileAdded  = false;  // true if we add the tile (not if it replaces an old one).
        Rectangle readRegion = null;   // The region to be read (may be smaller than node region).
        Tile tile = node.getTile();
        if (tile != null) {
            assert node.equals(tile.getAbsoluteRegion()) : tile;
            final Dimension floor = tile.getSubsamplingFloor(subsampling);
            if (floor == null) {
                /*
                 * A tile is unable to read its image at the given subsampling or any smaller
                 * subsampling. Skip this tile. However we may try its children at the end of
                 * this method, since they typically have a finer subsampling.
                 */
            } else if (floor != subsampling) {
                /*
                 * A tile is unable to read its image at the given subsampling, but would
                 * be capable if the subsampling was smaller. If we are allowed to change
                 * the setting, add this item to the queue of subsampling to try later.
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
                readRegion = node.intersection(regionOfInterest);
                final Tile previousTile = candidates.put(readRegion, tile);
                relativeReadRegion.setBounds(readRegion);
                relativeSubsampling.setSize(subsampling);
                tileCost = tile.countUnwantedPixelsFromAbsolute(
                        relativeReadRegion, relativeSubsampling);
                if (previousTile == null) {
                    cost += tileCost;
                    tileAdded = true;
                } else {
                    /*
                     * Found a tile with the same bounding box than the new tile. It is
                     * probably a tile at a different resolution. Retains the one which
                     * minimize the disk reading, and discard the other one. This check
                     * is not generic since we search for an exact match, but this case
                     * is common enough. Handling it with a HashMap will help to reduce
                     * the amount of tiles to handle in a more costly way later. Note
                     * that the key is an intersection, not the tile bounds, so it is
                     * not completly redundant with the RTree.
                     */
                    relativeReadRegion.setBounds(readRegion);
                    relativeSubsampling.setSize(subsampling);
                    final int previousTileCost = previousTile.countUnwantedPixelsFromAbsolute(
                            relativeReadRegion, relativeSubsampling);
                    final int delta = tileCost - previousTileCost;
                    if (delta >= 0) {
                        // Previous tile had a cost equals or lower.
                        // Keep the old tile, discart the new one.
                        tile     = previousTile;
                        tileCost = previousTileCost;
                        candidates.put(readRegion, previousTile);
                    } else {
                        // We accept the new tile. Adjust the cost (which is now lower).
                        cost += delta;
                    }
                }
            }
        }
        /*
         * If we added a new tile (not replaced an existing one), add it to
         * the stack so that the caller can revert the addition if it wishs.
         */
        if (tileAdded) {
            candidateStack.add(readRegion);
            costOfStack += tileCost;
        }
        final List<TreeNode> children = node.getChildren();
        if (children != null) {
            /*
             * If the region to read encompass entirely this node (otherwise reading a few childs
             * may be cheaper) and if the children subsampling are not higher than the tile's one
             * (they are usually not), then there is no need to continue down the tree since the
             * childs can not do better than this node.
             */
            if (readRegion != null) {
                if (readRegion.equals(node) && !tile.isFinerThan(regionOfInterest)) {
                    return cost;
                }
            }
            /*
             * Process the children. They will be added to 'candidateStack', including children of
             * children through recursive invocation of this method. After the loop we will decide
             * if we keep all those bunch of children.
             */
            long childCost = costOfStack;
            final int stackBefore = candidateStack.size();
            for (final TreeNode child : children) {
                cost += addTileCandidate(child, candidates);
            }
            final int stackAfter = candidateStack.size();
            if (readRegion != null && stackAfter != stackBefore) {
                /*
                 * At least one child has been added and those childs overlap with the tile that
                 * we added previously. In the easiest case, the childs are more costly than the
                 * tile or they do not cover entirely the read region. In such case, just remove
                 * the childs and we are done.
                 */
                final List<Rectangle> added = candidateStack.subList(stackBefore, stackAfter);
                assert !added.contains(readRegion) : added;
                childCost = costOfStack - childCost;
                if (childCost >= tileCost || !node.isDense(added, readRegion)) {
                    assert candidates.keySet().containsAll(added);
                    cost -= childCost;
                    costOfStack -= childCost;
                    candidates.keySet().removeAll(added);
                    added.clear(); // Removes this range from candidateStack.
                } else {
                    /*
                     * In this alternative, the childs are cheaper than the tile, so keep the
                     * childs and remove the tile. First we check for the uncommon case where
                     * a child replaced this tile.  In such case we preserve the substitution
                     * and let the cost unchanged since the child already adjusted it.
                     */
                    final Tile previousTile = candidates.remove(readRegion);
                    if (previousTile != tile) {
                        candidates.put(readRegion, previousTile);
                    } else {
                        /*
                         * The tile has been removed from the candidates. Removes it from the stack
                         * as well. If the tile has been added by the "if (tileAdded)" block sooner
                         * in this method, we know its index (which is faster). Otherwise we need
                         * to scan through the array. In all cases the rectangle must be present.
                         */
                        cost -= tileCost;
                        costOfStack -= tileCost;
                        if (tileAdded) {
                            if (candidateStack.remove(stackBefore - 1) != readRegion) {
                                throw new AssertionError(stackBefore);
                            }
                        } else {
                            if (!candidateStack.remove(readRegion)) {
                                throw new AssertionError(readRegion);
                            }
                        }
                    }
                }
            }
        }
        return cost;
    }
}
