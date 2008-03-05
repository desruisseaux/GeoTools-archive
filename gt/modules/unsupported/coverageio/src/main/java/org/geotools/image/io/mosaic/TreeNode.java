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
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Enumeration;
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
 * The value of the inherited rectangle is the {@linkplain Tile#getAbsoluteRegion absolute region}
 * of the tile, computed and stored once for ever for efficienty during searchs. This class extends
 * {@link Rectangle} for pure opportunist reasons, in order to reduce the amount of object created
 * (because we will have thousands of TreeNodes) and for direct (no indirection, no virtual calls)
 * invocation of {@link Rectangle} services. We authorize ourself this unrecommendable practice only
 * because this class is not public. The inherited {@link Rectangle} should <string>never</strong>
 * be modified by anyone outside this class.
 * <p>
 * Note that the {@link #compareTo} method is inconsistent with {@link #equals}. It should
 * be considered as an implementation details exposed because this class is not public.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial") // Will not be serialized anyway.
final class TreeNode extends SubsampledRectangle implements Comparable<TreeNode>,
        javax.swing.tree.TreeNode // TODO: should be org.geotools.gui.swing.tree.TreeNode
{
    /**
     * The parent, or {@code null} if none. This is not used by {@link TreeNode} itself,
     * but is provided for implementing the {@link javax.swing.tree.TreeNode} interface.
     */
    private TreeNode parent;

    /**
     * The parent tile which may contains other tiles. May be {@code null} for the root
     * (but not always), but initially non-null for every childs. However may be set to
     * {@code null} at some later stage if the node has been removed from the tree.
     */
    private Tile tile;

    /**
     * The index, used for preserving order compared to the user-specified one.
     */
    protected final int index;

    /**
     * The children that are fully enclosed in the {@linkplain #tile}, or {@code null} if none
     * (except for the root node, which may have an empty children list but never {@code null}).
     */
    private List<TreeNode> children;

    /**
     * {@code true} if at least one tile overlaps an other tile in direct {@linkplain #children}.
     * As a special case, if a tile has exactly the same bounding box than an other tile, then we
     * do not consider it as an overlap. This is because those exact matchs are easy to handle by
     * {@link RTree}.
     */
    private boolean overlaps;




    ////////////////////////////////////////////////////////////////////////
    ////////                                                        ////////
    ////////                C O N S T R U C T I O N                 ////////
    ////////                                                        ////////
    ////////////////////////////////////////////////////////////////////////




    /**
     * Creates a node for the specified bounds with no subsampling and no tile.
     */
    private TreeNode(final Rectangle bounds) {
        super(bounds);
        tile = null;
        index = -1;
    }

    /**
     * Creates a node for a single tile.
     *
     * @param  tile  The tile.
     * @param  index The original index in the user-specified array.
     * @throws IOException if an I/O operation was required and failed.
     */
    private TreeNode(final Tile tile, final int index) throws IOException {
        super(tile.getAbsoluteRegion(), tile.getSubsampling());
        this.tile  = tile;
        this.index = index;
    }

    /**
     * Builds the root of a tree for the given tiles.
     *
     * @param  tiles The tiles to be inserted in the tree.
     * @throws IOException if an I/O operation was required and failed.
     */
    public TreeNode(final Tile[] tiles) throws IOException {
        /*
         * Sorts the TreeNode with biggest tree first (this is required for the algorithm building
         * the tree). Note that the TreeNode array should be created before any sorting is applied,
         * because its creation may involve disk reading and those reading are more efficient when
         * performed in the tiles iteration order (assuming this array was sorted by TileManager).
         */
        final TreeNode[] nodes = new TreeNode[tiles.length];
        for (int i=0; i<tiles.length; i++) {
            nodes[i] = new TreeNode(tiles[i], i);
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
                if (!root.contains(nodes[i])) {
                    root = null;
                    break;
                }
            }
        }
        if (root != null) {
            setBounds(root);
            tile         = root.tile;
            index        = root.index;
            xSubsampling = root.xSubsampling;
            ySubsampling = root.ySubsampling;
        } else {
            index = -1;
        }
        /*
         * Now inserts every nodes in the tree. At first we try to add each node into the smallest
         * parent that can contain it and align it on a grid. If the node can not be aligned, then
         * we add it into the smallest parent regardless of alignment. The grid condition produces
         * good results for TileLayout.CONSTANT_TILE_SIZE. However it may not work so well with
         * random tiles (open issue).
         */
        for (int i=(index >= 0 ? 1 : 0); i<nodes.length; i++) {
            final TreeNode child = nodes[i];
            TreeNode parent = smallest(child, true);
            if (parent == this && !isGridded(child)) {
                parent = smallest(child, false);
            }
            if (parent.children == null) {
                parent.children = new LinkedList<TreeNode>();
            } else if (!parent.overlaps) {
                for (final TreeNode existing : parent.children) {
                    if (child.intersects(existing) && !child.boundsEquals(existing)) {
                        parent.overlaps = true;
                        break;
                    }
                }
            }
            parent.children.add(child);
        }
        /*
         * Calculates the bounds only for root node, if not already computed. We do not iterate
         * down the tree since every children should have their bounds set to the tile bounds.
         */
        if (children != null && tile == null) {
            assert (width | height) < 0 : this;
            for (final TreeNode child : children) {
                // No need to invoke setBounds for the first child since Rectangle.add(Rectangle)
                // takes care of that if the width or height is negative (specified in javadoc).
                add(child);
            }
        }
        splitOverlappingChildren(); // Must be after bounds calculation.
        postTreeCreation();
    }

    /**
     * Returns the smallest tree node containing the given region. This method do not very if
     * {@code this} node {@linkplain #contains contains} the given bounds - we assume that the
     * caller verified that. This is required by the constructor which may invoke this method
     * from the root with an empty bounding box.
     *
     * @param  The bounds to check for inclusion.
     * @param  {@code true} if we require that the node can align the given bounds on a grid.
     * @return The smallest node, or {@code this} if none (never {@code null}).
     */
    private TreeNode smallest(final Rectangle bounds, final boolean gridded) {
        TreeNode smallest = this;
        if (children != null) {
            long smallestArea = (long) width * (long) height;
            for (final TreeNode child : children) {
                if (child.contains(bounds)) {
                    final TreeNode candidate = child.smallest(bounds, gridded);
                    if (!gridded || candidate.isGridded(bounds)) {
                        final long area = (long) candidate.width * (long) candidate.height;
                        if (area < smallestArea) {
                            smallestArea = area;
                            smallest = candidate;
                        }
                    }
                }
            }
        }
        return smallest;
    }

    /**
     * Returns {@code true} if the given child is layered on a grid in this node.
     */
    private boolean isGridded(final Rectangle child) {
        return width  % child.width  == 0 && (child.x - x) % child.width  == 0 &&
               height % child.height == 0 && (child.y - y) % child.height == 0;
    }

    /**
     * Makes sure that this node and all its children do not contains overlapping tiles. If at
     * least one overlapping is found, then the nodes are reorganized in non-overlapping sub-nodes.
     * Algorithm overview:
     * <p>
     * <ol>
     *   <li>For the current {@linkplain #children}, keeps the first node and remove every nodes
     *       that overlap with a previous one (except special cases described above). The removed
     *       nodes are stored in a temporary list.</li>
     *   <li>The nodes selected in the previous step are groupped in a new {@link TreeNode},
     *       which will be the first {@linkplain #children} of this tile.</li>
     *   <li>Repeat the process with the nodes that were removed in the first step. Each new
     *       group is added as a new {@linkplain #children} in this node.</li>
     * </ol>
     * <p>
     * <b>special case:</b> if an overlapping is found but the two nodes have identical bounds,
     * then they are considered as if they did not overlap. This exception exists because this
     * trivial overlap is easy to detect and to process by {@link RTree}.
     */
    private void splitOverlappingChildren() {
        if (children != null) {
            assert !isEmpty() : this; // Requires that bounds has been computed.
            for (final TreeNode child : children) {
                child.splitOverlappingChildren();
            }
        }
        if (!overlaps) {
            return;
        }
        List<TreeNode> toProcess = children;
        children = new LinkedList<TreeNode>();
        int bestIndex=0, bestDistance=0;
        /*
         * The loop below is for processing a group of nodes. A "group of nodes" is either
         * the initial children list (on the first iteration), or the nodes that have not be
         * retained in a previous run of this loop. In the later case, those remaining nodes
         * need to be examined again and again until they are classified in some group.
         */
        while (!toProcess.isEmpty()) {
            final List<TreeNode> retained = new LinkedList<TreeNode>();
            final List<TreeNode> removed  = new LinkedList<TreeNode>();
            TreeNode added = toProcess.remove(0);
            retained.add(added);
            /*
             * The loop below is for moving every non-overlapping nodes to the "retained" list,
             * begining with the first node that we retained unconditionnaly in the above line
             * (we need to start with one node in order to select non-overlapping nodes...)
             */
            ListIterator<TreeNode> it;
            while ((it = toProcess.listIterator()).hasNext()) {
                TreeNode best = null;
                /*
                 * The loop below is for removing every nodes that overlap with the "added" node,
                 * and select only one node (the "best" one) in the non-overlapping nodes. We
                 * select the closest tile (relative to the "added" one) rather than the first
                 * one because the retention order is significant.
                 */
                do {
                    final TreeNode candidate = it.next();
                    final int distance = added.distance(candidate);
                    if (distance < 0) {
                        /*
                         * Found an overlapping tile.  Accept inconditionnaly the tile if its bounds
                         * is exactly equals to the "added" tile (this is the special case described
                         * in the method javadoc above). Otherwise remove it from the toProcess list
                         * and search for an other tile.
                         */
                        if (added.boundsEquals(candidate)) {
                            retained.add(candidate);
                        } else {
                            removed.add(candidate);
                        }
                        it.remove();
                    } else if (best == null || distance < bestDistance) {
                        /*
                         * The tile do not overlaps. Retain it only if it is the closest one
                         * to the "added" tile.  Otherwise left it in the toProcess list for
                         * consideration in a future iteration.
                         */
                        bestDistance = distance;
                        bestIndex = it.previousIndex();
                        best = candidate;
                        // Note: if the distance is 0 we could break the loop as an optimization
                        // (since we can't get anything better), but we don't because we still
                        // need to remove the overlapping tiles that may be present in toProcess.
                    }
                } while (it.hasNext());
                /*
                 * If we found no non-overlapping tile, we are done. The toProcess list should be
                 * empty now (it will be tested in an assert statement after the loop). Otherwise
                 * move the best tile from the "toProcess" to the "retained" list.
                 */
                if (best == null) {
                    break;
                }
                if (toProcess.remove(bestIndex) != best) {
                    throw new AssertionError(bestIndex);
                }
                retained.add(best);
                added = best;
            }
            assert toProcess.isEmpty() : toProcess;
            assert Collections.disjoint(retained, removed);
            final TreeNode child = new TreeNode(this);
            child.children = retained;
            children.add(child);
            toProcess = removed;
        }
        overlaps = false;
    }

    /**
     * Invoked when the tree construction is completed with every nodes assigned to its final
     * parent. This method calculate the values that depend on the child hierarchy, including
     * subsampling.
     */
    private void postTreeCreation() {
        if (children != null) {
            for (final TreeNode child : children) {
                child.parent = this;
                child.postTreeCreation();
                if (child.xSubsampling > xSubsampling) xSubsampling = child.xSubsampling;
                if (child.ySubsampling > ySubsampling) ySubsampling = child.ySubsampling;
            }
        }
    }

    /**
     * Comparator for sorting tiles by descreasing area and subsamplings. The
     * {@linkplain TreeNode#TreeNode(Tile[]) constructor} expects this order for inserting
     * a tile into the smallest tile that can contains it. If two tiles cover the same area,
     * then they are sorted by descreasing subsamplings. The intend is that tiles sorted last
     * are usually the ones with finest resolution no matter if the layout is "constant area"
     * or "constant tile size".
     * <p>
     * If two tiles have the same area and subsampling, then their order is unchanged on the
     * basis that initial order, when sorted by {@link TileManager}, should be efficient for
     * reading tiles sequentially.
     * <p>
     * This method is inconsistent with {@link #equals}. It is okay for our usage of it,
     * which should be restricted to this {@link TreeNode} package-privated class only.
     */
    public int compareTo(final TreeNode that) {
        long a1 = (long) this.width * (long) this.height;
        long a2 = (long) that.width * (long) that.height;
        if (a1 > a2) return -1; // Greatest values first
        if (a1 < a2) return +1;
        a1 = (long) this.xSubsampling * (long) this.ySubsampling;
        a2 = (long) that.xSubsampling * (long) that.ySubsampling;
        if (a1 > a2) return -1;
        if (a1 < a2) return +1;
        return 0;
    }

    /**
     * Compares this tree with the specified one for equality. Note that we inherit the
     * {@link #hashCode} method from {@link Rectangle}, which is suffisient for meeting
     * the consistency requirement.  The quality of the hash code value does not really
     * matter since this tree is not aimed to be inserted in a hash map.
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof TreeNode)) {
            return true; // For consistency with Rectangle.equals which accepts arbitrary Rectangle.
        }
        final TreeNode that = (TreeNode) other;
        return Utilities.equals(this.tile,     that.tile) &&
               Utilities.equals(this.children, that.children);
    }

    /**
     * Returns the string representation of this node. This string should holds in a single line
     * since it may be displayed in a {@link javax.swing.JTree}. The content may change in any
     * future version. It is provided mostly for debugging purpose.
     */
    @Override
    public String toString() {
        if (tile != null) {
            return tile.toString();
        }
        final String string = super.toString();
        return string.substring(string.lastIndexOf('.') + 1);
    }




    ////////////////////////////////////////////////////////////////////////
    ////////                                                        ////////
    ////////           Q U E R I Y N G   T H E   T R E E            ////////
    ////////                                                        ////////
    ////////////////////////////////////////////////////////////////////////




    /**
     * Returns the tile in this node, or {@code null} if none. This is also the <cite>Swing</cite>
     * user object which is formatted (when available) by the {@link #toString} method, as required
     * by {@link javax.swing.JTree}.
     */
    public Tile getUserObject() {
        return tile;
    }

    /**
     * Returns the parent node that contains this child node.
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Returns {@code true} if this node has no children. Note that this is slightly different from
     * {@link #getAllowsChildren} which returns {@code false} if this node <strong>can not</strong>
     * have children.
     */
    public boolean isLeaf() {
        return children == null;
    }

    /**
     * Return {@code true} in almost every case since there is no reason to prevent a tile from
     * containing smaller tiles, unless the tile bounds {@linkplain #isEmpty is empty}.
     */
    public boolean getAllowsChildren() {
        return !isEmpty();
    }

    /**
     * Returns the number of children. This method is provided for <cite>Swing</cite> usage.
     * When using {@link TreeNode} directly, consider using {@link #getChildren} instead.
     */
    public int getChildCount() {
        return (children != null) ? children.size() : 0;
    }

    /**
     * Returns the children at the given index. This method is provided for <cite>Swing</cite>
     * usage. When using {@link TreeNode} directly, consider using {@link #getChildren} instead.
     */
    public TreeNode getChildAt(final int index) {
        return children.get(index);
    }

    /**
     * Returns the children, or {@code null} if none.
     */
    public List<TreeNode> getChildren() {
        return children;
    }

    /**
     * Returns an enumeration over the children. This method is provided for <cite>Swing</cite>
     * usage. When using {@link TreeNode} directly, consider using {@link #getChildren} instead.
     */
    public Enumeration<TreeNode> children() {
        Collection<TreeNode> children = this.children;
        if (children == null) {
            children = Collections.emptyList();
        }
        return Collections.enumeration(children);
    }

    /**
     * Returns the index of given node. If this node does not contain the given one,
     * -1 will be returned. This method is provided for <cite>Swing</cite> usage.
     */
    public int getIndex(final javax.swing.tree.TreeNode node) {
        return (children != null) ? children.indexOf(node) : -1;
    }

    /**
     * Set this tree as read-only. An optional comparator can be specified for sorting
     * tiles. As a side effect, this method will also reduce the amount of memory used.
     */
    public void setReadOnly(final Comparator<TreeNode> comparator) {
        if (children != null) {
            final TreeNode[] array = children.toArray(new TreeNode[children.size()]);
            if (comparator != null) {
                Arrays.sort(array, comparator);
            }
            children = UnmodifiableArrayList.wrap(array);
            for (final TreeNode node : children) {
                node.setReadOnly(comparator);
            }
        }
    }

    /**
     * Removes the given tile.
     *
     * @param  tile The tile to remove.
     * @return {@code true} if the given tile was found and removed.
     * @throws IOException if an I/O operation was required and failed.
     * @throws UnsupportedOperationException if {@link #setReadOnly} has been invoked.
     */
    public boolean remove(final Tile tile) throws IOException, UnsupportedOperationException {
        return contains(tile.getAbsoluteRegion(), tile, true);
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
            if (!contains(tile.getAbsoluteRegion(), tile, false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if this tree contains the given tile. This method
     * invokes itself recursively for scanning through the subtrees.
     *
     * @param  region The result of {@link Tile#getAbsoluteRegion}.
     * @param  candidate The tile to test for presence in this tree.
     * @param  {@code true} if the node containing the tile should be removed.
     * @return {@code true} if the given tile is presents in this tree.
     */
    private boolean contains(final Rectangle region, final Tile candidate, final boolean remove) {
        // Tests with == rather than Object.equals(Object) in order to protect ourself from user
        // overriding of 'equals'. This is correct for the usage that we make in this package.
        if (candidate == tile) {
            assert region.equals(this);
            if (remove) {
                tile = null;
            }
            return true;
        }
        if (children != null && contains(region)) {
            final Iterator<TreeNode> it = children.iterator();
            while (it.hasNext()) {
                final TreeNode node = it.next();
                if (node.contains(region, candidate, remove)) {
                    if (remove && node.tile == null && node.children == null) {
                        it.remove();
                        if (children.isEmpty()) {
                            children = null;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
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
        if (roi.contains(this)) {
            copy(tiles);
        } else if (children != null && roi.intersects(this)) {
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
                assert contains(child) : child;
                child.copy(tiles);
            }
        }
    }

    /**
     * Returns the tiles intersecting the given region of interest (ROI).
     * The returned collection is a copy that can be modified without altering the tree.
     */
    public Collection<Tile> intersecting(final Rectangle roi) {
        final List<Tile> tiles = new LinkedList<Tile>();
        intersecting(roi, tiles);
        return tiles;
    }

    /**
     * Adds the tiles intersecting the given region of interest (ROI) to the given array.
     * This method invokes itself recursively down the tree.
     */
    private void intersecting(final Rectangle roi, final Collection<Tile> tiles) {
        if (intersects(roi)) {
            if (tile != null) {
                tiles.add(tile);
            }
            if (children != null) {
                for (final TreeNode child : children) {
                    child.intersecting(roi, tiles);
                }
            }
        }
    }
}
