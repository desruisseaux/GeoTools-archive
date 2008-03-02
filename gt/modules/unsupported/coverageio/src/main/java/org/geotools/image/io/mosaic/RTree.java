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
 * This class is <strong>not</strong> thread safe.
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
    private final Rectangle relativeReadBounds;

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
     * The tiles that are under consideration during a search.
     * Set to {@code null} once a search is finished.
     */
    private Map<Rectangle,Tile> candidates;

    /**
     * Cost of reading tiles as the estimated
     * {@linkplain Tile#countUnwantedPixels amount of unwanted pixels}.
     */
    protected long cost;

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
        relativeReadBounds  = new Rectangle();
        subsamplingDone     = new HashSet<Dimension>();
        subsamplingToTry    = new LinkedList<Dimension>();
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
     *   <li>{@link #xSubsampling}</li>
     *   <li>{@link #ySubsampling}</li>
     *   <li>{@link #subsamplingChangeAllowed}</li>
     * </ul>
     * <p>
     * On output, the following fields will be set:
     * <ul>
     *   <li>{@link #xSubsampling} if {@link #allowSubsamplingChange} is {@code true}</li>
     *   <li>{@link #ySubsampling} if {@link #allowSubsamplingChange} is {@code true}</li>
     *   <li>{@link #count} as the estimated {@linkplain Tile#countUnwantedPixels amount
     *       of unwanted pixels}.</li>
     * </ul>
     */
    public Tile[] searchTiles() throws IOException {
        assert subsamplingDone.isEmpty() && subsamplingToTry.isEmpty() && candidates == null;
        subsampling = regionOfInterest.getSubsampling();
        Map<Rectangle,Tile> bestCandidates = null;
        long lowestCost = Long.MAX_VALUE;
        try {
            do {
                cost = 0;
                if (candidates == bestCandidates) {
                     // Works on a new map in order to protect 'bestCandidates' from changes.
                    candidates = new LinkedHashMap<Rectangle,Tile>();
                }
                searchTiles(root);
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
            subsamplingDone.clear();
            subsamplingToTry.clear();
            candidates = null;
        }
        /*
         * TODO: sort the result.
         */
        final Collection<Tile> tiles = bestCandidates.values();
        return tiles.toArray(new Tile[tiles.size()]);
    }

    /**
     * Searchs the tiles starting from the given node. This method invokes
     * itself recursively for scanning the child nodes down the tree.
     */
    private void searchTiles(final TreeNode node) throws IOException {
        if (!node.intersects(regionOfInterest)) {
            return;
        }
        /*
         * Checks if the tile is able to read its image at the given subsampling or any smaller
         * subsampling. If not (which is indicated by a null floor), there is no reason to keep
         * it for further examination, but we still need to investigate its children.
         */
        final Tile tile = node.getTile();
        if (tile != null) {
            assert node.equals(tile.getAbsoluteRegion()) : tile;
            final Dimension floor = tile.getSubsamplingFloor(subsampling);
            if (floor == null) {
                /*
                 * A tile is unable to read its image at the given subsampling or any smaller
                 * subsampling. There is no reason to keep it for further examination in this
                 * method. Remove it for faster execution in subsequent run of the outer loop.
                 */
            } else {
                if (floor != subsampling) {
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
                    final Rectangle readBounds = node.intersection(regionOfInterest);
                    final Tile previousTile = candidates.put(readBounds, tile);
                    relativeReadBounds.setBounds(readBounds);
                    relativeSubsampling.setSize(subsampling);
                    int delta = tile.countUnwantedPixelsFromAbsolute(
                            relativeReadBounds, relativeSubsampling);
                    if (previousTile == null) {
                        cost += delta;
                    } else {
                        /*
                         * Found a tile with the same bounding box than the new tile. It is
                         * probably a tile at a different resolution. Retains the one which
                         * minimize the disk reading, and discard the other one. This check
                         * is not generic since we search for an exact match, but this case
                         * is common enough. Handling it with a HashMap will help to reduce
                         * the amount of tiles to handle in a more costly way later.
                         */
                        relativeReadBounds.setBounds(readBounds);
                        relativeSubsampling.setSize(subsampling);
                        delta -= previousTile.countUnwantedPixelsFromAbsolute(
                                relativeReadBounds, relativeSubsampling);
                        if (delta >= 0) {
                            // Previous tile had a cost equals or lower.
                            // Keep the old tile, discart the new one.
                            candidates.put(readBounds, previousTile);
                        } else {
                            // We accept the new tile. Adjust the cost (which is now lower).
                            cost += delta;
                        }
                    }
                }
            }
        }
        final List<TreeNode> children = node.getChildren();
        if (children != null) {
            for (final TreeNode child : children) {
                searchTiles(child);
            }
        }
    }
}
