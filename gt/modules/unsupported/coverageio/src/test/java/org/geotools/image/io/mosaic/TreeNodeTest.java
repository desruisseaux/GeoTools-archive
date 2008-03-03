/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.image.io.mosaic;

import java.util.Set;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;


/**
 * Tests {@link TreeNode} and {@link RTree}. The later is merely a wrapper around
 * {@link TreeNode} except for the {@link RTree#searchTiles} method, which is not
 * tested here (see {@link TileManagerTest} for that).
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TreeNodeTest extends TestBase {
    /**
     * The root of an RTree for {@link #targetTiles}.
     */
    private TreeNode root;

    /**
     * Initializes every fields in this class.
     */
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        assertEquals(4733, targetTiles.length);
        root = new TreeNode(targetTiles, null);
        root.join(null);
    }

    /**
     * Tests with a set of files corresponding to a Blue Marble mosaic.
     */
    public void testTreeNode() throws IOException {
        // TreeNode has many assert statements, so we want them enabled.
        assertTrue(TreeNode.class.desiredAssertionStatus());
        assertNotNull(root.getTile());
        assertEquals(root, root);
        assertTrue (root.containsAll(manager.getTiles()));
        assertFalse(root.containsAll(Arrays.asList(sourceTiles)));
        assertTrue (root.isDense(root.getChildren(), root));
        final Rectangle bounds = new Rectangle(SOURCE_SIZE*4, SOURCE_SIZE*2);
        final Rectangle roi = new Rectangle();
        final Random random = new Random(4353223575290515986L);
        for (int i=0; i<100; i++) {
            roi.x      = random.nextInt(bounds.width);
            roi.y      = random.nextInt(bounds.height);
            roi.width  = random.nextInt(bounds.width  / 4);
            roi.height = random.nextInt(bounds.height / 4);
            final Set<Tile> intersect1 = toSet(root.intersecting(roi));
            final Set<Tile> intersect2 = intersecting(targetTiles, roi);
            final Set<Tile> contained1 = toSet(root.containedIn(roi));
            final Set<Tile> contained2 = containedIn(targetTiles, roi);
            assertEquals(intersect2, intersect1);
            assertEquals(contained2, contained1);
            assertFalse (intersect1.isEmpty()); // Only for our test suite (since empty set are not forbidden)
            assertTrue  (intersect1.containsAll(contained1));
            assertFalse (contained1.containsAll(intersect1));
            if (false) {
                System.out.print(roi);
                System.out.print(" intersect=");
                System.out.print(intersect1.size());
                System.out.print(" contained=");
                System.out.println(contained1.size());
            }
        }
        /*
         * Tests multi-threading computation and ensure that it is identical
         * to the non-multithread one.
         */
        final ThreadGroup threads = new ThreadGroup("TreeNode");
        final TreeNode tree2 = new TreeNode(targetTiles, threads);
        tree2.join(threads);
        assertTrue(threads.isDestroyed());
        assertEquals(root, tree2);
        /*
         * Tests removal of nodes.
         */
        root.setReadOnly();
        assertEquals(root, tree2);
        for (int i=0; i<targetTiles.length; i += 10) {
            assertTrue(tree2.remove(targetTiles[i]));
        }
        assertFalse(root.equals(tree2));
        for (int i=0; i<20; i++) {
            roi.x      = random.nextInt(bounds.width);
            roi.y      = random.nextInt(bounds.height);
            roi.width  = random.nextInt(bounds.width  / 4);
            roi.height = random.nextInt(bounds.height / 4);
            final Set<Tile> intersect1 = toSet(tree2.intersecting(roi));
            final Set<Tile> intersect2 = intersecting(targetTiles, roi);
            final Set<Tile> contained1 = toSet(tree2.containedIn(roi));
            final Set<Tile> contained2 = containedIn(targetTiles, roi);
            boolean removedSome = false;
            for (int j=0; j<targetTiles.length; j += 10) {
                final Tile tile = targetTiles[j];
                removedSome |= intersect2.remove(tile);
                removedSome |= contained2.remove(tile);
            }
            assertTrue  (removedSome);
            assertEquals(intersect2, intersect1);
            assertEquals(contained2, contained1);
            assertTrue  (intersect1.containsAll(contained1));
            assertFalse (contained1.containsAll(intersect1));
        }
        try {
            root.remove(targetTiles[100]);
            fail("Removal should not be allowed on a read-only tree.");
        } catch (UnsupportedOperationException e) {
            // This is the expected exception.
        }
    }

    /**
     * Tests the {@link RTree} class.
     */
    public void testRTree() throws IOException {
if (true) return; // Test disabled for now.
        show(root);
        final RTree tree = new RTree(root);
        assertEquals(new Rectangle(SOURCE_SIZE*4, SOURCE_SIZE*2), tree.getBounds());
        assertEquals(new Dimension(TARGET_SIZE,   TARGET_SIZE),   tree.getTileSize());
        final int[] subsamplings = new int[] {1,3,5,9,15,45,90};
        checkSubsampling(root, subsamplings, subsamplings.length);
    }

    /**
     * Ensures that every children have the expected subsampling. This method invokes itself
     * recursively down the tree. It is an helper method for {@link #testRTree} only. Checking
     * subsampling is a convenient way to ensure that every tiles are where they should be.
     */
    private static void checkSubsampling(final TreeNode node, final int[] subsamplings, int i)
            throws IOException
    {
        final Tile tile = node.getTile();
        final String message = tile.toString();
        assertTrue(message, --i >= 0);
        final int subsampling = subsamplings[i];
        assertEquals(message, subsampling, node.xSubsampling);
        assertEquals(message, subsampling, node.ySubsampling);
        final Dimension d = tile.getSubsampling();
        assertEquals(message, subsampling, d.width);
        assertEquals(message, subsampling, d.height);

        final List<TreeNode> children = node.getChildren();
        if (children != null) {
            final Rectangle bounds = tile.getAbsoluteRegion();
            for (final TreeNode child : children) {
                assertTrue(message, bounds.contains(child.getTile().getAbsoluteRegion()));
                checkSubsampling(child, subsamplings, i);
            }
        }
    }

    /**
     * Copies the given collection into a set.
     */
    private static Set<Tile> toSet(final Collection<Tile> tiles) {
        final Set<Tile> asSet = new LinkedHashSet<Tile>(tiles);
        assertEquals(tiles.size(), asSet.size());
        return asSet;
    }

    /**
     * Returns the tiles intersecting the given region.
     */
    private static Set<Tile> intersecting(final Tile[] tiles, final Rectangle region) throws IOException {
        final Set<Tile> interest = new LinkedHashSet<Tile>();
        for (final Tile tile : tiles) {
            if (region.intersects(tile.getAbsoluteRegion())) {
                assertTrue(interest.add(tile));
            }
        }
        return interest;
    }

    /**
     * Returns the tiles entirely contained in the given region.
     */
    private static Set<Tile> containedIn(final Tile[] tiles, final Rectangle region) throws IOException {
        final Set<Tile> interest = new LinkedHashSet<Tile>();
        for (final Tile tile : tiles) {
            if (region.contains(tile.getAbsoluteRegion())) {
                assertTrue(interest.add(tile));
            }
        }
        return interest;
    }
}
