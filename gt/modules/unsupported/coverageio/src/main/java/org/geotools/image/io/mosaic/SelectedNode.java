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
import java.awt.Rectangle;


/**
 * A tree node selected because of its inclusion in a Region Of Interest (ROI).
 * It contains an estimation of the cost of reading this tile and its children.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial") // Not expected to be serialized.
final class SelectedNode extends TreeNode {
    /**
     * An estimation of the cost of reading this tile, including children.
     */
    protected long cost;

    /**
     * Creates a new node for the given tile.
     *
     * @param tile The tile to wrap.
     * @param readRegion The region to be read.
     * @param cost An estimation of the cost of reading the given region of the given tile.
     */
    SelectedNode(final Tile tile, final Rectangle readRegion, final int cost) {
        super(readRegion);
        this.tile = tile;
        this.cost = cost;
        assert !isEmpty();
    }

    /**
     * Adds the given tile as a child of this tile.
     *
     * @throws ClassCastException if the given child is not an instance of {@code SelectedNode}.
     *         This is an intentional restriction in order to avoid more subtile bugs later.
     */
    @Override
    public void addChild(final TreeNode child) throws ClassCastException {
        super.addChild(child);
        if (child != null) {
            final long added = ((SelectedNode) child).cost;
            if (added != 0) {
                SelectedNode parent = this;
                do {
                    parent.cost += added;
                } while ((parent = (SelectedNode) parent.getParent()) != null);
            }
        }
    }

    /**
     * Set the children to the given array. A null value remove all children.
     */
    @Override
    public void setChildren(final TreeNode[] children) {
        final long removed = childrenCost();
        if (removed != 0) {
            SelectedNode parent = this;
            do {
                parent.cost -= removed;
            } while ((parent = (SelectedNode) parent.getParent()) != null);
        }
        super.setChildren(children);
    }

    /**
     * Removes this tile and all its children from the tree.
     */
    @Override
    public void remove() {
        if (cost != 0) {
            TreeNode parent = this;
            while ((parent = parent.getParent()) != null) {
                ((SelectedNode) parent).cost -= cost;
            }
        }
        super.remove();
    }

    /**
     * Returns the cost of children only, not including the {@linkplain #tile} in this node.
     */
    private long childrenCost() {
        long c = 0;
        for (final TreeNode child : this) {
            c += ((SelectedNode) child).cost;
        }
        assert cost >= c;
        return c;
    }

    /**
     * Returns {@code true} if this node has a lower cost than the specified one.
     */
    public final boolean isCheaperThan(final SelectedNode other) {
        if (cost < other.cost) {
            return true;
        }
        if (cost == other.cost) {
            return getTileCount() < other.getTileCount();
        }
        return false;
    }

    /**
     * Removes the nodes having the same bounding box than another tile. If such matchs are found,
     * they are probably tiles at a different resolution.  Retains the one which minimize the disk
     * reading, and discards the other one. This check is not generic since we search for an exact
     * match, but this case is common enough. Handling it with a {@link java.util.HashMap} will
     * help to reduce the amount of tiles to handle in a more costly way later.
     */
    final void filter(final Map<Rectangle,SelectedNode> overlaps) {
        /*
         * Must process children first because if any of them are removed, it will lowered
         * the cost and consequently can change the decision taken at the end of this method.
         */
        for (final TreeNode child : this) {
            ((SelectedNode) child).filter(overlaps);
        }
        SelectedNode existing = overlaps.put(this, this);
        if (existing != null && existing != this) {
            if (!isCheaperThan(existing)) {
                /*
                 * A cheaper tiles existed for the same bounds. Reinsert the previous tile in the
                 * map. We will delete this node from the tree later, except if the previous node
                 * is a children of this node. In the later case, we can't remove completly this
                 * node since it would remove its children as well, so we just nullify the tile.
                 */
                overlaps.put(existing, existing);
                if (existing.getParent() == this) {
                    if (tile != null) {
                        tile = null;
                        cost = childrenCost();
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
    private void removeFrom(final Map<Rectangle,SelectedNode> overlaps) {
        final SelectedNode existing = overlaps.remove(this);
        if (existing != this) {
            overlaps.put(existing, existing);
        }
        for (final TreeNode child : this) {
            ((SelectedNode) child).removeFrom(overlaps);
        }
    }

    /**
     * Invoked in assertion for checking the validity of the whole tree.
     */
    @Override
    int checkValidity() {
        if (childrenCost() > cost) {
            throw new AssertionError(this);
        }
        return super.checkValidity();
    }
}
