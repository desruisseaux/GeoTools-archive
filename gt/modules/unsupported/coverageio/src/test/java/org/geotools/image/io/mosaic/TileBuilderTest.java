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
import java.io.File;
import java.io.IOException;
import javax.imageio.spi.ImageReaderSpi;

import junit.framework.TestCase;


/**
 * Tests {@link TileBuilder}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TileBuilderTest extends TestCase {
    /**
     * Tests with a set of files corresponding to a Blue Marble mosaic.
     */
    public void testBlueMarble() throws IOException {
        final TileBuilder builder = new TileBuilder();

        assertNull(builder.getTileReaderSpi());
        builder.setTileReaderSpi("png");
        final ImageReaderSpi spi = builder.getTileReaderSpi();
        assertNotNull(spi);

        final int S = 21600;
        final File directory = new File("geodata"); // Dummy directory - will not be read.
        final Tile[] tiles = new Tile[] {
            new Tile(spi, new File(directory, "A1.png"), 0, new Rectangle(0*S, 0, S, S)),
            new Tile(spi, new File(directory, "B1.png"), 0, new Rectangle(1*S, 0, S, S)),
            new Tile(spi, new File(directory, "C1.png"), 0, new Rectangle(2*S, 0, S, S)),
            new Tile(spi, new File(directory, "D1.png"), 0, new Rectangle(3*S, 0, S, S)),
            new Tile(spi, new File(directory, "A2.png"), 0, new Rectangle(0*S, S, S, S)),
            new Tile(spi, new File(directory, "B2.png"), 0, new Rectangle(1*S, S, S, S)),
            new Tile(spi, new File(directory, "C2.png"), 0, new Rectangle(2*S, S, S, S)),
            new Tile(spi, new File(directory, "D2.png"), 0, new Rectangle(3*S, S, S, S))
        };

        Rectangle bounds = new Rectangle(S*4, S*2);
        builder.setUntiledImageBounds(bounds);
        assertEquals(bounds, builder.getUntiledImageBounds());

        Dimension size = builder.getTileSize();
        assertEquals(960, size.width);
        assertEquals(900, size.height);

        Dimension[] subsamplings = builder.getSubsamplings();
        int[] width  = new int[] {1,2,3,3,5,6,9,10,10,15,18,18,30,45,90};
        int[] height = new int[] {1,2,3,4,4,6,8, 8,12,16,16,24,24,48,90};
        for (int i=0; i<subsamplings.length; i++) {
            assertEquals("width["  + i + ']', width [i], subsamplings[i].width);
            assertEquals("height[" + i + ']', height[i], subsamplings[i].height);
        }

        builder.setTileSize(new Dimension(960,960));
        builder.setSubsamplings((Dimension[]) null); // For forcing new computation.
        subsamplings = builder.getSubsamplings();
        width  = new int[] {1,3,5,9,15,45,90};
        height = new int[] {1,3,5,9,15,45,90};
        for (int i=0; i<subsamplings.length; i++) {
            assertEquals("width["  + i + ']', width [i], subsamplings[i].width);
            assertEquals("height[" + i + ']', height[i], subsamplings[i].height);
        }

        builder.setTileDirectory(new File("S960")); // Dummy directory - will not be written.
        TileManager tileManager = builder.createTileManager(tiles, 0, false);
        assertEquals(4733, tileManager.getTiles().size());
        final String asText = tileManager.toString();
        assertFalse(asText.trim().length() == 0);
    }
}
