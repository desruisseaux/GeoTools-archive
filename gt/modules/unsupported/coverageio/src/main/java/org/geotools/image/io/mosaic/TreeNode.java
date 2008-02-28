/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, GeoTools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
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

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;

import org.geotools.resources.Utilities;
import org.geotools.resources.UnmodifiableArrayList;


/**
 * List of tiles inside the bounding box of a bigger (or equals in size) tile. This class fills
 * a similar purpose than RTree, except that we do not calculate any new bounding boxes. We try
 * to fit children in existing tile bounds on the assumption that most tile layouts are already
 * organized in some form pyramid.
 * <p>
 * Note that the {@link #compareTo} method is inconsistent with {@link #equals}. It should
 * be considered as an implementation details exposed because this class is not public.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class TreeNode implements Comparable<TreeNode>, Runnable {
    /**
     * The parent tile which may contains other tiles. May be {@code null} for the root
     * (but not always), but is garanteed non-null for every childs.
     */
    protected final Tile tile;

    /**
     * The {@linkplain Tile#getAbsoluteRegion absolute region} of the tile.
     * Computed and stored once for ever for efficienty during searchs.
     */
    private Rectangle region;

    /**
     * On construction, the {@linkplain Tile#getSubsampling subsampling}.
     * After tree completion, the finest subsampling in this tile and chlidren.
     */
    private int xSubsampling, ySubsampling;

    /**
     * The children that are fully enclosed in the {@linkplain #tile}, or {@code null} if none
     * (except for the root node, which may have an empty children list but never {@code null}).
     */
    private List<TreeNode> children;

    /**
     * Creates a node for a single tile.
     *
     * @param  tile The tile.
     * @throws IOException if an I/O operation was required and failed.
     */
    private TreeNode(final Tile tile) throws IOException {
        this.tile = tile;
        region = tile.getAbsoluteRegion();
        final Dimension subsampling = tile.getSubsampling();
        xSubsampling = subsampling.width;
        ySubsampling = subsampling.height;
    }

    /**
     * Builds the root of a tree for the given tiles. Caller should invokes {@link #join}
     * exactly once as late as possible after creation but before the first use.
     *
     * @param  threads Must be a newly allocated an initially empty thread group. May be
     *         {@code null} if multithreading is not wanted (e.g. during debugging).
     * @param  tiles The tiles to be inserted in the tree.
     * @throws IOException if an I/O operation was required and failed.
     */
    TreeNode(final Tile[] tiles, final ThreadGroup threads) throws IOException {
        /*
         * Sorts the TreeNode with biggest tree first (this is required for the algorithm building
         * the tree). Note that the TreeNode array should be created before any sorting is applied,
         * because its creation may involve disk reading and those reading are more efficient when
         * performed in the tiles iteration order (assuming this array was sorted by TileManager).
         */
        final TreeNode[] nodes = new TreeNode[tiles.length];
        for (int i=0; i<tiles.length; i++) {
            nodes[i] = new TreeNode(tiles[i]);
        }
        Arrays.sort(nodes);
        /*
         * Special case: checks if the first node contains all subsequent nodes. If this is true,
         * then there is no need to keep the special root TreeNode with the tile field set to null.
         * We can keep directly the first node instead. Doing so reduces very slightly the amount
         * of iterations in the search methods (probably an impercetible gain), and (probably more
         * noticeable) allows the multi-threading to be more effective since otherwise we would
         * have a single thread.
         *
         * Note that this special case should NOT be extended further the first node,
         * otherwise the tiles prior the retained node would be discarted.
         */
        TreeNode root = null;
        if (nodes.length != 0) {
            root = nodes[0];
            for (int i=1; i<nodes.length; i++) {
                if (!root.region.contains(nodes[i].region)) {
                    root = null;
                    break;
                }
            }
        }
        int i;
        if (root != null) {
            tile         = root.tile;
            region       = root.region;
            xSubsampling = root.xSubsampling;
            ySubsampling = root.ySubsampling;
            i = 1;
        } else {
            tile = null;
            i = 0;
        }
        children = new LinkedList<TreeNode>();
        while (i < nodes.length) {
            children.add(nodes[i++]);
        }
        /*
         * Creates the tree. For each "root" tiles, we will creates the subtree in a
         * separated thread in order to take advantage of multi-processor machines.
         */
        assert threads == null || threads.activeCount() == 0 : threads;
        createTree(threads);
        // join(threads) must be invoked at some later stage after this point.
    }

    /**
     * Organizes the {@linkplain #children} in subtrees.
     *
     * @param threads Must be a newly allocated an initially empty thread group, or {@code null}.
     */
    private void createTree(final ThreadGroup threads) {
        ListIterator<TreeNode> iterator = children.listIterator();
        while (iterator.hasNext()) {
            final TreeNode node = iterator.next();
            final int index = iterator.nextIndex();
            if (node.addChildren(iterator, threads)) {
                iterator = children.listIterator(index);
            }
        }
        children = UnmodifiableArrayList.wrap(children.toArray(new TreeNode[children.size()]));
    }

    /**
     * Adds children to this nodes. Every children found are removed from the collection.
     * Returns {@code true} if at least one candidate child moved to this node.
     */
    private boolean addChildren(final Iterator<TreeNode> candidates, final ThreadGroup threads) {
        while (candidates.hasNext()) {
            final TreeNode candidate = candidates.next();
            if (region.contains(candidate.region)) {
                candidates.remove();
                if (children == null) {
                    children = new LinkedList<TreeNode>();
                }
                children.add(candidate);
            } else {
                // Assertion expected because nodes should be sorted by decreasing area.
                // A rectangle with smaller area can not contains a rectangle with greater area.
                assert !candidate.region.contains(region) : region;
            }
        }
        /*
         * If we started from the root level, process children in a separated thread in order
         * to take advantage of multi-processor machines. Otherwise process children in current
         * thread in order to avoid creating too many threads.
         */
        if (children == null) {
            return false;
        }
        if (threads != null) {
            final Thread thread = new Thread(threads, this, "TreeNode");
            thread.start();
        } else {
            createTree(threads);
        }
        return true;
    }

    /**
     * Blocks until every threads completed their work. This method <strong>must</strong> be
     * invoked exactly once after {@link #TreeNode(Tile[])} constructor, as late as possible
     * but before first use. If this method is not invoked, {@link NullPointerException} may
     * occur randomly or the method results may be just plain false.
     *
     * @param threads The thread group given to the constructor.
     */
    final void join(final ThreadGroup threads) {
        if (threads != null) {
            final Thread[] actives = new Thread[children.size()];
            int count;
            while ((count = threads.enumerate(actives, false)) != 0) {
                for (int i=0; i<count; i++) {
                    final Thread active = actives[i];
                    assert active != Thread.currentThread() : active;
                    try {
                        active.join();
                    } catch (InterruptedException e) {
                        // Someone doesn't want to let us wait. Process other
                        // threads; we will try this one again later.
                    }
                }
            }
            threads.destroy();
        }
        /*
         * Completes the calculation of subsamplings. We take the opportunity for synchronizing
         * memory content if the calculation was performed in a separated thread.
         */
        final boolean computeRegion = (region == null);
        for (final TreeNode node : children) {
            synchronized (node) {
                node.finestSubsampling(this);
                if (computeRegion) {
                    if (region == null) {
                        region = new Rectangle(node.region);
                    } else {
                        region.add(node.region);
                    }
                }
            }
        }
    }

    /**
     * If this node uses a finer subsampling than the specified parent node, sets
     * the parent subsampling accordingly. This method invokes itself recursively.
     */
    private void finestSubsampling(final TreeNode parent) {
        if (children != null) {
            for (final TreeNode node : children) {
                if (xSubsampling == 1 && ySubsampling == 1) {
                    break; // No need to continue, since there is no lower value.
                }
                node.finestSubsampling(this);
            }
        }
        if (xSubsampling < parent.xSubsampling) parent.xSubsampling = xSubsampling;
        if (ySubsampling < parent.ySubsampling) parent.ySubsampling = ySubsampling;
    }

    /**
     * Creates the tree for the given node. This method is public as an implementation
     * side-effect, but should never be invoked directly from outside this class.
     */
    public synchronized void run() {
        createTree(null);
    }

    /**
     * Comparator for sorting tiles by descreasing area and increasing subsamplings. The
     * {@linkplain TreeNode#TreeNode(Tile[]) constructor} expects this order for inserting
     * a tile into the smallest tile that can contains it. If two tiles cover the same area,
     * then they are sorted by increasing subsamplings. The intend is that tiles sorted last
     * should be more economical to read than tiles sorted first.
     * <p>
     * If two tiles have the same area and subsampling, then their order is unchanged on the
     * basis that initial order, when sorted by {@link TileManager}, should be efficient for
     * reading tiles sequentially.
     */
    public int compareTo(final TreeNode other) {
        final Rectangle r1 = this. region;
        final Rectangle r2 = other.region;
        long a1 = (long) r1.width * (long) r1.height;
        long a2 = (long) r2.width * (long) r2.height;
        if (a1 > a2) return -1; // Greatest values first
        if (a1 < a2) return +1;
        a1 = (long) this .xSubsampling * (long) this .ySubsampling;
        a2 = (long) other.xSubsampling * (long) other.ySubsampling;
        if (a1 < a2) return -1; // Smallest values first
        if (a1 > a2) return +1;
        return 0;
    }

    /**
     * Returns {@code true} if this tree contains all the given tile.
     * This method is insensitive to the order of elements in the given array.
     *
     * @param  tiles The tiles to test for presence in this tree.
     * @return {@code true} if every given tiles are present in this tree.
     * @throws IOException if an I/O operation was required and failed.
     */
    public boolean containsAll(final Collection<Tile> tiles) throws IOException {
        for (final Tile tile : tiles) {
            if (!contains(tile, tile.getAbsoluteRegion())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if this tree contains the given tile. This method
     * invokes itself recursively for scanning through the subtrees.
     *
     * @param  candidate The tile to test for presence in this tree.
     * @param  bounds The result of {@link Tile#getAbsoluteRegion}.
     * @return {@code true} if the given tile is presents in this tree.
     */
    private boolean contains(final Tile candidate, final Rectangle bounds) {
        if (Utilities.equals(candidate, tile)) {
            assert bounds.equals(region);
            return true;
        }
        if (children != null && region.contains(bounds)) {
            for (final TreeNode node : children) {
                if (node.contains(candidate, bounds)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the tiles intersecting the given region of interest (ROI).
     * The returned collection is a copy that can be modified without altering the tree.
     */
    public Collection<Tile> intersect(final Rectangle roi) {
        final List<Tile> tiles = new LinkedList<Tile>();
        intersect(roi, tiles);
        return tiles;
    }

    /**
     * Adds the tiles intersecting the given region of interest (ROI) to the given array.
     * This method invokes itself recursively down the tree.
     */
    private void intersect(final Rectangle roi, final Collection<Tile> tiles) {
        if (region.intersects(roi)) {
            if (tile != null) {
                tiles.add(tile);
            }
            if (children != null) {
                for (final TreeNode child : children) {
                    child.intersect(roi, tiles);
                }
            }
        }
    }

    /**
     * Returns the tiles entirely contained in the given region of interest (ROI).
     * The returned collection is a copy that can be modified without altering the tree.
     */
    public Collection<Tile> containedIn(final Rectangle roi) {
        final List<Tile> tiles = new LinkedList<Tile>();
        containedIn(roi, tiles);
        return tiles;
    }

    /**
     * Returns the tiles entirely contained in the given region of interest (ROI).
     * This method invokes itself recursively down the tree.
     */
    private void containedIn(final Rectangle roi, final Collection<Tile> tiles) {
        if (roi.contains(region)) {
            copy(tiles);
        } else if (children != null && roi.intersects(region)) {
            for (final TreeNode child : children) {
                child.containedIn(roi, tiles);
            }
        }
    }

    /**
     * Copies inconditionnaly every tiles (including children) to the given list.
     * This method invokes itself recursively down the tree.
     */
    private void copy(final Collection<Tile> tiles) {
        if (tile != null) {
            tiles.add(tile);
        }
        if (children != null) {
            for (final TreeNode child : children) {
                assert region.contains(child.region) : child;
                child.copy(tiles);
            }
        }
    }

    /**
     * Returns a hash code value for this tree. This is used only for consistency with
     * {@link #equals}, but the quality of the hash code value does not really matter
     * since this tree is not aimed to be inserted in a hash map.
     */
    @Override
    public int hashCode() {
        int code = xSubsampling + 37*ySubsampling;
        if (region != null) {
            code += 31 * region.hashCode();
        }
        return code;
    }

    /**
     * Compares this tree with the specified one for equality.
     * this is used mostly for debugging purpose.
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof TreeNode) {
            final TreeNode that = (TreeNode) other;
            return this.xSubsampling == that.xSubsampling &&
                   this.ySubsampling == that.ySubsampling &&
                   Utilities.equals(this.region,   that.region) &&
                   Utilities.equals(this.tile,     that.tile) &&
                   Utilities.equals(this.children, that.children);
        }
        return false;
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
        buffer.append('[');
        if (region != null) {
            buffer.append("location=").append(region.x).append(',').append(region.y).
                   append(", size=").append(region.width).append(',').append(region.height);
        }
        return buffer.append(']').toString();
    }
}
