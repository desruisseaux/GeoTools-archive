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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Collection;


/**
 * Tests {@link TileManager}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TileManagerTest extends TestBase {
    /**
     * The region of interest to be queried.
     */
    private Rectangle regionOfInterest;

    /**
     * The subsampling to be queried.
     */
    private Dimension subsampling;

    /**
     * The tiles under examination.
     */
    private Collection<Tile> tiles;

    /**
     * Initializes the fields to be used for tile searchs.
     */
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        regionOfInterest = new Rectangle(SOURCE_SIZE*4, SOURCE_SIZE*2);
        subsampling = new Dimension(90,90);
    }

    /**
     * Queries the tiles and performs some sanity check on them.
     *
     * @param expected Expected subsampling, or {@code null} if no subsampling change is allowed.
     */
    private void searchTiles(Dimension expected) throws IOException {
        tiles = manager.getTiles(regionOfInterest, subsampling, expected != null);
        final Tile[] array = tiles.toArray(new Tile[tiles.size()]);
        for (int i=0; i<array.length; i++) {
            final Rectangle bounds = array[i].getAbsoluteRegion();
            assertFalse("Tiles should not be empty.", bounds.isEmpty());
            assertTrue("Must intersects the ROI.", regionOfInterest.intersects(bounds));
            for (int j=i+1; j<array.length; j++) {
                assertFalse("Expected no overlaps.", bounds.intersects(array[j].getAbsoluteRegion()));
            }
        }
        if (expected == null) {
            expected = subsampling;
        }
        for (int i=0; i<array.length; i++) {
            assertEquals("Expected uniform subsampling.", expected, array[i].getSubsampling());
        }
    }

    /**
     * Searchs the tiles again with a different subsampling requested, and ensures that we get the
     * same collection than previous invocation of {@link #searchTiles}. The purpose of this method
     * is to ensure that the automatic adjustment of subsampling works.
     */
    private void searchSameTiles(final int xSubsampling, final int ySubsampling) throws IOException {
        final Collection<Tile> expected = tiles;
        final Dimension selected = new Dimension(subsampling);
        subsampling.setSize(xSubsampling, ySubsampling);
        searchTiles(selected);
        assertNotSame(expected, tiles);
        assertEquals (expected, tiles);
        subsampling.setSize(selected);
    }

    /**
     * Tests the search of tiles on a tile layout using constant tile size.
     */
    public void testConstantSizeLayout() throws IOException {
        int total = 0;

        subsampling.setSize(90, 90);
        searchTiles(null);
        assertEquals(1, tiles.size());
        total += tiles.size();
        searchSameTiles(100, 120);
        searchSameTiles(90,  100);
        searchSameTiles(400, 400);

        subsampling.setSize(45, 45);
        searchTiles(null);
        assertEquals(2, tiles.size());
        total += tiles.size();
        searchSameTiles(50, 60);
        searchSameTiles(45, 70);
        searchSameTiles(45, 90);

        subsampling.setSize(15, 15);
        searchTiles(null);
        assertEquals(18, tiles.size());
        total += tiles.size();
        searchSameTiles(15, 20);
        searchSameTiles(30, 70);
        searchSameTiles(18, 27);

        subsampling.setSize(9, 9);
        searchTiles(null);
        assertEquals(50, tiles.size());
        total += tiles.size();
        searchSameTiles(10, 20);
        searchSameTiles(31, 11);
        searchSameTiles(97, 13);

        subsampling.setSize(5, 5);
        searchTiles(null);
        assertEquals(162, tiles.size());
        total += tiles.size();
        searchSameTiles(7, 12);

        subsampling.setSize(3, 3);
        searchTiles(null);
        assertEquals(450, tiles.size());
        total += tiles.size();
        searchSameTiles(4, 3);

        subsampling.setSize(1, 1);
        searchTiles(null);
        assertEquals(4050, tiles.size());
        total += tiles.size();
        searchSameTiles(2, 1);

        assertEquals(4733, total);
    }
}
