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
import java.util.Random;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;

import junit.framework.TestCase;


/**
 * Tests {@link TreeNode}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TreeNodeTest extends TestCase {
    /**
     * Tests with a set of files corresponding to a Blue Marble mosaic.
     */
    public void testTreeNode() throws IOException {
        final MosaicBuilder builder = new MosaicBuilder();
        builder.setTileSize(new Dimension(960,960));
        final int S = 21600;
        final Tile[] originalTiles = MosaicBuilderTest.getBlueMarbleTiles(builder, S);
        final TileManager manager = builder.createTileManager(originalTiles, 0, false);
        final Tile[] tiles = manager.getTiles().toArray(new Tile[manager.getTiles().size()]);
        assertEquals(4733, tiles.length);

        // TreeNode has many assert statements, so we want them enabled.
        assertTrue(TreeNode.class.desiredAssertionStatus());
        /*
         * At first, tests the creation of the tree without multi-threading.
         */
        final TreeNode tree = new TreeNode(tiles, null);
        tree.join(null);
        assertNotNull(tree.getTile());
        assertEquals(tree, tree);
        assertTrue (tree.containsAll(manager.getTiles()));
        assertFalse(tree.containsAll(Arrays.asList(originalTiles)));
        final Rectangle bounds = new Rectangle(S*4, S*2);
        final Rectangle roi = new Rectangle();
        final Random random = new Random(4353223575290515986L);
        for (int i=0; i<100; i++) {
            roi.x      = random.nextInt(bounds.width);
            roi.y      = random.nextInt(bounds.height);
            roi.width  = random.nextInt(bounds.width  / 4);
            roi.height = random.nextInt(bounds.height / 4);
            final Set<Tile> intersect1 = toSet(tree.intersecting(roi));
            final Set<Tile> intersect2 = intersecting(tiles, roi);
            final Set<Tile> contained1 = toSet(tree.containedIn(roi));
            final Set<Tile> contained2 = containedIn(tiles, roi);
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
        final TreeNode tree2 = new TreeNode(tiles, threads);
        tree2.join(threads);
        assertTrue(threads.isDestroyed());
        assertEquals(tree, tree2);
        /*
         * Tests removal of nodes.
         */
        tree.setReadOnly();
        assertEquals(tree, tree2);
        for (int i=0; i<tiles.length; i += 10) {
            assertTrue(tree2.remove(tiles[i]));
        }
        assertFalse(tree.equals(tree2));
        for (int i=0; i<20; i++) {
            roi.x      = random.nextInt(bounds.width);
            roi.y      = random.nextInt(bounds.height);
            roi.width  = random.nextInt(bounds.width  / 4);
            roi.height = random.nextInt(bounds.height / 4);
            final Set<Tile> intersect1 = toSet(tree2.intersecting(roi));
            final Set<Tile> intersect2 = intersecting(tiles, roi);
            final Set<Tile> contained1 = toSet(tree2.containedIn(roi));
            final Set<Tile> contained2 = containedIn(tiles, roi);
            boolean removedSome = false;
            for (int j=0; j<tiles.length; j += 10) {
                final Tile tile = tiles[j];
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
            tree.remove(tiles[100]);
            fail("Removal should not be allowed on a read-only tree.");
        } catch (UnsupportedOperationException e) {
            // This is the expected exception.
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
