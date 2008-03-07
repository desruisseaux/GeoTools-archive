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

import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.awt.Rectangle;
import java.io.IOException;

import org.geotools.resources.Utilities;


/**
 * A tree node wrapping a {@link Tile}. It contains an estimation of the cost of reading this tile
 * and its children. This cost is adjusted during every addition or removal of nodes. Children are
 * managed as a linked list.
 * <p>
 * This class extends {@link Rectangle} for pure opportunist reasons, in order to reduce the amount
 * of object created (because we will have thousands of TreeNodes) and for direct (no indirection,
 * no virtual calls) invocation of {@link Rectangle} services. We do this unrecommendable practice
 * only because this class is not public. The inherited {@link Rectangle} should
 * <string>never</strong> be modified by anyone outside this class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
class TreeNode extends Rectangle implements Iterable<TreeNode>, javax.swing.tree.TreeNode {
    // TODO: should implements org.geotools.gui.swing.tree.TreeNode
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8611267926304778221L;

    /**
     * The tile wrapped by this node, or {@code null} if we should read only the children,
     * not the tile itself.
     */
    private Tile tile;

    /**
     * An estimation of the cost of reading this tile, including children.
     */
    private long cost;

    /**
     * The parent, or {@code null} if none.
     */
    private TreeNode parent;

    /**
     * The first and last children {@linkplain #addChild added} to this node,
     * or {@code null} if none.
     */
    private TreeNode firstChildren, lastChildren;

    /**
     * The previous and next sibling, or {@code null} if none. Used in order to created
     * a linked list of children.
     */
    private TreeNode previousSibling, nextSibling;

    /**
     * Creates an initially empty rectangle. Width and height are set to -1, which
     * stands for non-existant rectangle according {@link Rectangle} documentation.
     * Other fields (including subsamplings) are set to 0 or {@code null}.
     */
    public TreeNode() {
        super(-1, -1);
    }

    /**
     * Creates a node for the specified bounds with no tile.
     */
    protected TreeNode(final Rectangle bounds) {
        super(bounds);
    }

    /**
     * Creates a node for a single tile.
     *
     * @param  tile  The tile.
     * @throws IOException if an I/O operation was required and failed.
     */
    protected TreeNode(final Tile tile) throws IOException {
        super(tile.getAbsoluteRegion());
        this.tile = tile;
        /*
         * This constructor is invoked by StoredTreeNode which doesn't really has any use
         * of the cost. We set it to the amount of pixels to be read for informative purpose,
         * but we could change that in a future version.
         */
        final Rectangle bounds = tile.getRegion();
        cost = (long) bounds.width * (long) bounds.height;
    }

    /**
     * Creates a new node for the given tile.
     *
     * @param tile The tile to wrap.
     * @param readRegion The region to be read.
     * @param cost An estimation of the cost of reading the given region of the given tile.
     */
    TreeNode(final Tile tile, final Rectangle readRegion, final int cost) {
        super(readRegion);
        this.tile = tile;
        this.cost = cost;
        assert !isEmpty();
    }

    /**
     * Sets the tile to the same value than the given node. Both this node and the given one
     * are expected to be {@linkplain #isLeaf leaf} (this condition is required for keeping
     * the cost accurate). This method is not public because this condition is checked only
     * through assertions.
     */
    final void setTile(final TreeNode node) {
        assert isRoot() && isLeaf() && node.isLeaf();
        super.setBounds(node);
        tile = node.tile;
        cost = node.cost;
    }

    /**
     * Clears the tile.
     *
     * @param tileCost the cost to remove.
     */
    final void clearTile(final long tileCost) {
        assert (tile == null) ? (tileCost == 0) : (tileCost >= 0);
        tile = null;
        cost -= tileCost;
    }

    /**
     * Adds the given tile as a child of this tile. This method do nothing if the given tile
     * is null (which is typically the case when it doesn't intercept the region of interest).
     * <p>
     * This method is not public because it has pre-conditions checked by {@code assert} statements.
     */
    final void addChild(final TreeNode child) {
        if (child != null) {
            assert child.isRoot() && (tile == null || contains(child)) : child;
            child.parent = this;
            if (lastChildren == null) {
                lastChildren = firstChildren = child;
            } else {
                child.previousSibling = lastChildren;
                lastChildren.nextSibling = child;
                lastChildren = child;
            }
            TreeNode p = this;
            do {
                p.cost += child.cost;
            } while ((p = p.parent) != null);
        }
    }

    /**
     * Set the children to the given array. A null value remove all children.
     */
    public final void setChildren(final TreeNode[] children) {
        TreeNode child = firstChildren;
        while (child != null) {
            assert child.parent == this;
            cost -= child.cost;
            final TreeNode next = child.nextSibling;
            child.previousSibling = null;
            child.nextSibling     = null;
            child.parent          = null;
            child = next;
        }
        firstChildren = lastChildren = null;
        if (children != null) {
            for (TreeNode newChild : children) {
                addChild(newChild);
            }
        }
    }

    /**
     * Removes this tile and all its children from the tree.
     */
    public final void remove() {
        if (previousSibling != null) {
            previousSibling.nextSibling = nextSibling;
        }
        if (nextSibling != null) {
            nextSibling.previousSibling = previousSibling;
        }
        if (parent != null) {
            if (parent.firstChildren == this) {
                parent.firstChildren = nextSibling;
            }
            if (parent.lastChildren == this) {
                parent.lastChildren = previousSibling;
            }
            do {
                parent.cost -= cost;
            } while ((parent = parent.parent) != null);
        }
        previousSibling = null;
        nextSibling = null;
    }

    /**
     * Returns the estimated cost of reading this tile and its children.
     */
    public final long cost() {
        return cost;
    }

    /**
     * Returns {@code true} if this node has a lower cost than the specified one.
     */
    public final boolean isCheaperThan(final TreeNode other) {
        if (cost < other.cost) {
            return true;
        }
        if (cost == other.cost) {
            return getTileCount() < other.getTileCount();
        }
        return false;
    }

    /**
     * Returns the number of non-null tiles, including in children.
     */
    private int getTileCount() {
        int count = (tile != null) ? 1 : 0;
        TreeNode child = firstChildren;
        while (child != null) {
            count += child.getTileCount();
            child = child.nextSibling;
        }
        return count;
    }

    /**
     * Returns the tile in this node, or {@code null} if none. This is also the <cite>Swing</cite>
     * user object which is formatted (when available) by the {@link #toString} method, as required
     * by {@link javax.swing.JTree}.
     */
    public final Tile getUserObject() {
        return tile;
    }

    /**
     * Returns the parent node that contains this child node, or {@code null} if this node
     * is the root node.
     */
    public final TreeNode getParent() {
        return parent;
    }

    /**
     * Returns {@code true} if this node has no parent. Note that having no parent
     * implies having no sibling neither (otherwise the tree would be malformed).
     */
    private boolean isRoot() {
        return parent == null && previousSibling == null && nextSibling == null;
    }

    /**
     * Returns {@code true} if this node has no children. Note that this is slightly different from
     * {@link #getAllowsChildren} which returns {@code false} if this node <strong>can not</strong>
     * have children.
     */
    public final boolean isLeaf() {
        assert (firstChildren == null) == (lastChildren == null);
        return firstChildren == null;
    }

    /**
     * Return {@code true} in almost every case since there is no reason to prevent a tile from
     * containing smaller tiles, unless the tile bounds {@linkplain #isEmpty is empty}.
     */
    public final boolean getAllowsChildren() {
        return !super.isEmpty();
    }

    /**
     * Returns the number of children. This method is provided for <cite>Swing</cite> usage.
     * When using {@link TreeNode} directly, consider using {@link #iterator} instead.
     */
    public final int getChildCount() {
        // Don't invoke assertValid() directly or indirectly since it could cause never-ending loop.
        int count = 0;
        TreeNode child = firstChildren;
        while (child != null) {
            child = child.nextSibling;
            count++;
        }
        return count;
    }

    /**
     * Returns the children at the given index. This method is provided for <cite>Swing</cite>
     * usage. When using {@link TreeNode} directly, consider using {@link #iterator} instead.
     */
    public final TreeNode getChildAt(int index) {
        TreeNode child = firstChildren;
        while (child != null) {
            if (index == 0) {
                return child;
            }
            child = child.nextSibling;
            index--;
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the index of given node. If this node does not contain the given one,
     * -1 will be returned. This method is provided for <cite>Swing</cite> usage.
     */
    public final int getIndex(final javax.swing.tree.TreeNode node) {
        int index = 0;
        TreeNode child = firstChildren;
        while (child != null) {
            if (child == node) {
                assert getChildAt(index) == node : index;
                return index;
            }
            child = child.nextSibling;
            index++;
        }
        return -1;
    }

    /**
     * Returns an enumeration over the children. This method is provided for <cite>Swing</cite>
     * usage. When using {@link TreeNode} directly, consider using {@link #iterator} instead.
     */
    public final Enumeration<TreeNode> children() {
        return new Iter(firstChildren);
    }

    /**
     * Returns an iterator over the children.
     */
    public final Iterator<TreeNode> iterator() {
        return new Iter(firstChildren);
    }

    /**
     * The iterator over the children.
     */
    private static final class Iter implements Iterator<TreeNode>, Enumeration<TreeNode> {
        /**
         * The next children to return, or {@code null} if we have reached
         * the end of iteration.
         */
        private TreeNode children;

        /**
         * The last children returned, or {@code null} if none or removed.
         * Used for {@link #remove} implementation.
         */
        private TreeNode last;

        /**
         * Creates an iterator starting at the given element.
         */
        public Iter(final TreeNode first) {
            children = first;
        }

        /**
         * Returns {@code true} if there is more elements to return.
         */
        public boolean hasMoreElements() {
            return hasNext();
        }

        /**
         * Returns {@code true} if there is more elements to return.
         */
        public boolean hasNext() {
            return children != null;
        }

        /**
         * Returns the next element.
         */
        public TreeNode next() {
            if (children != null) {
                last = children;
                children = children.nextSibling;
                return last;
            }
            throw new NoSuchElementException();
        }

        /**
         * Returns the next element.
         */
        public TreeNode nextElement() {
            return next();
        }

        /**
         * Removes the last children returned by this iterator.
         */
        public void remove() {
            if (last == null) {
                throw new IllegalStateException();
            }
            last.remove();
            last = null;
        }
    }

    /**
     * Removes the given tile.
     *
     * @param  tile The tile to remove.
     * @return {@code true} if the given tile was found and removed.
     * @throws IOException if an I/O operation was required and failed.
     */
    public final boolean remove(final Tile tile) throws IOException {
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
    public final boolean containsAll(final Collection<Tile> tiles) throws IOException {
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
        if (equals(region)) {
            if (remove) {
                tile = null;
            }
            return true;
        }
        if (super.contains(region)) {
            TreeNode child = firstChildren;
            while (child != null) {
                if (child.contains(region, candidate, remove)) {
                    if (remove && child.tile == null && child.isLeaf()) {
                        child.remove();
                    }
                    return true;
                }
                child = child.nextSibling;
            }
        }
        return false;
    }

    /**
     * Returns the tiles entirely contained in the given region of interest (ROI).
     * The returned collection is a copy that can be modified without altering the tree.
     */
    public final Collection<Tile> containedIn(final Rectangle roi) {
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
        } else if (roi.intersects(this)) {
            TreeNode child = firstChildren;
            while (child != null) {
                child.containedIn(roi, tiles);
                child = child.nextSibling;
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
        TreeNode child = firstChildren;
        while (child != null) {
            child.copy(tiles);
            child = child.nextSibling;
        }
    }

    /**
     * Returns the tiles intersecting the given region of interest (ROI).
     * The returned collection is a copy that can be modified without altering the tree.
     */
    public final Collection<Tile> intersecting(final Rectangle roi) {
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
            TreeNode child = firstChildren;
            while (child != null) {
                child.intersecting(roi, tiles);
                child = child.nextSibling;
            }
        }
    }

    /**
     * Removes the nodes having the same bounding box than another tile. If such matchs are found,
     * they are probably tiles at a different resolution.  Retains the one which minimize the disk
     * reading, and discards the other one. This check is not generic since we search for an exact
     * match, but this case is common enough. Handling it with a {@link java.util.HashMap} will
     * help to reduce the amount of tiles to handle in a more costly way later.
     */
    final void filter(final Map<Rectangle,TreeNode> overlaps) {
        /*
         * Must process children first because if any of them are removed, it will lowered
         * the cost and consequently can change the decision taken at the end of this method.
         */
        for (TreeNode child = firstChildren; child != null; child = child.nextSibling) {
            child.filter(overlaps);
        }
        TreeNode existing = overlaps.put(this, this);
        if (existing != null && existing != this) {
            if (!isCheaperThan(existing)) {
                /*
                 * A cheaper tiles existed for the same bounds. Reinsert the previous tile in the
                 * map. We will delete this node from the tree later, except if the previous node
                 * is a children of this node. In the later case, we can't remove completly this
                 * node since it would remove its children as well, so we just nullify the tile.
                 */
                overlaps.put(existing, existing);
                if (existing.parent == this) {
                    if (tile != null) {
                        cost = 0;
                        TreeNode child = firstChildren;
                        while (child != null) {
                            cost += child.cost;
                            child = child.nextSibling;
                        }
                        tile = null;
                    }
                    return;
                }
                existing = this;
            }
            existing.remove();
            existing.removeFrom(overlaps);
        }
    }

    /**
     * Removes this node and all its children from the given map.
     */
    private void removeFrom(final Map<Rectangle,TreeNode> overlaps) {
        final TreeNode existing = overlaps.remove(this);
        if (existing != this) {
            overlaps.put(existing, existing);
        }
        TreeNode child = firstChildren;
        while (child != null) {
            child.removeFrom(overlaps);
            child = child.nextSibling;
        }
    }

    /**
     * Copies to the specified list the tiles in this node and every children nodes.
     */
    final void getTiles(final List<Tile> tiles) {
        if (tile != null) {
            tiles.add(tile);
        }
        TreeNode child = firstChildren;
        while (child != null) {
            child.getTiles(tiles);
            child = child.nextSibling;
        }
    }

    /**
     * Compares this rectangle with the specified one for equality. This method
     * <strong>must</strong> be semantically identical to {@link Rectangle#equals}
     * and the inherited {@link Rectangle#hashCode} must be unchanged. This is required
     * for proper working of {@link #filter}.
     */
    @Override
    public final boolean equals(final Object other) {
        return (other == this) || super.equals(other);
    }

    /**
     * Compares this tree and all its chilren with the specified one for equality.
     */
    public final boolean deepEquals(final TreeNode other) {
        if (other == this) {
            return true;
        }
        if (!equals(other) || !Utilities.equals(tile, other.tile)) {
            return false;
        }
        final Iterator<TreeNode> it1 = this .iterator();
        final Iterator<TreeNode> it2 = other.iterator();
        while (it1.hasNext()) {
            if (!it2.hasNext()) {
                return false;
            }
            final TreeNode t1 = it1.next();
            final TreeNode t2 = it2.next();
            return (t1 == t2) || (t1 != null && t1.deepEquals(t2));
        }
        return !it2.hasNext();
    }

    /**
     * Returns the string representation of this node. This string should holds in a single line
     * since it may be displayed in a {@link javax.swing.JTree}. The content may change in any
     * future version. It is provided mostly for debugging purpose.
     */
    @Override
    public final String toString() {
        // Don't invoke assertValid() directly or indirectly since it could cause never-ending loop.
        String text;
        if (tile != null) {
            text = tile.toString();
        } else {
            text = super.toString();
            text = text.substring(text.lastIndexOf('.') + 1);
        }
        if (!isLeaf()) {
            text = text + " (" + getChildCount() + " childs)";
        }
        return text;
    }

    /**
     * Invoked in assertion for checking the validity of the whole tree. Note that the checks are
     * performed inconditionnaly though explicit {@code if ... throw new AssertionError(...)}
     * statements rather than {@code assert} statements. The whole methods is expected to be
     * invoked in an {@code assert} statement, so we do not want to repeat the assertion status
     * check in every line of this method.
     *
     * @return The total number of nodes.
     */
    final int checkValidity() {
        int count = 1;
        long childrenCost = 0;
        TreeNode child = firstChildren;
        if (child != null) {
            if (child.previousSibling != null) {
                throw new AssertionError(this);
            }
            while (true) {
                if (child.parent != this) {
                    throw new AssertionError(child);
                }
                count += child.checkValidity();
                childrenCost += child.cost;
                final TreeNode next = child.nextSibling;
                if (next == null) {
                    break;
                }
                if (next.previousSibling != child) {
                    throw new AssertionError(child);
                }
                if (!contains(child) && width >= 0 && height >= 0) {
                    throw new AssertionError(child);
                }
                child = next;
            }
        }
        if (child != lastChildren) {
            throw new AssertionError(this);
        }
        if (childrenCost < 0 || cost < childrenCost) {
            throw new AssertionError(this);
        }
        if (tile != null) {
            if (isEmpty()) {
                throw new AssertionError(this);
            }
            final Rectangle bounds;
            try {
                bounds = tile.getAbsoluteRegion();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            if (!bounds.contains(this)) {
                throw new AssertionError(this);
            }
        }
        return count;
    }
}
