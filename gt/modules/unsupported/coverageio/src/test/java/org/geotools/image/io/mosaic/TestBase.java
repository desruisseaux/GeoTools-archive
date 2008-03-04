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

import javax.swing.JTree;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import junit.framework.TestCase;


/**
 * Base class for tests.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class TestBase extends TestCase {
    /**
     * Source tile size for BlueMarble mosaic.
     */
    protected static final int SOURCE_SIZE = 21600;

    /**
     * Target tile size for BlueMarble mosaic.
     */
    protected static final int TARGET_SIZE = 960;

    /**
     * The mosaic builder used for creating {@link #blueMarbleTiles}.
     */
    protected MosaicBuilder builder;

    /**
     * Tiles given as input to the {@linkplain #builder}.
     */
    protected Tile[] sourceTiles;

    /**
     * Tiles produces as output by the {@linkplain #builder}.
     */
    protected Tile[] targetTiles;

    /**
     * The tile manager for {@link #targetTiles}.
     */
    protected TileManager manager;

    /**
     * Initializes every fields declared in this {@link TestBase} class.
     */
    @Override
    protected void setUp() throws IOException {
        assertTrue("Assertions should be enabled.", MosaicBuilder.class.desiredAssertionStatus());

        builder = new MosaicBuilder();
        assertNull("No initial provider expected.", builder.getTileReaderSpi());
        builder.setTileReaderSpi("png");
        final ImageReaderSpi spi = builder.getTileReaderSpi();
        assertNotNull("Provider should be defined.", spi);

        final File directory = new File("geodata"); // Dummy directory - will not be read.
        final int S = SOURCE_SIZE; // For making reading easier below.
        sourceTiles = new Tile[] {
            new Tile(spi, new File(directory, "A1.png"), 0, new Rectangle(0*S, 0, S, S)),
            new Tile(spi, new File(directory, "B1.png"), 0, new Rectangle(1*S, 0, S, S)),
            new Tile(spi, new File(directory, "C1.png"), 0, new Rectangle(2*S, 0, S, S)),
            new Tile(spi, new File(directory, "D1.png"), 0, new Rectangle(3*S, 0, S, S)),
            new Tile(spi, new File(directory, "A2.png"), 0, new Rectangle(0*S, S, S, S)),
            new Tile(spi, new File(directory, "B2.png"), 0, new Rectangle(1*S, S, S, S)),
            new Tile(spi, new File(directory, "C2.png"), 0, new Rectangle(2*S, S, S, S)),
            new Tile(spi, new File(directory, "D2.png"), 0, new Rectangle(3*S, S, S, S))
        };
        builder.setTileDirectory(new File("S960")); // Dummy directory - will not be written.
        builder.setTileSize(new Dimension(TARGET_SIZE, TARGET_SIZE));
        manager = builder.createTileManager(sourceTiles, 0, false);
        targetTiles = manager.getTiles().toArray(new Tile[manager.getTiles().size()]);
    }

    /**
     * Shows the given tree in a Swing widget. This is used for debugging purpose only.
     */
    final void show(final TreeNode root) {
        final JFrame frame = new JFrame("TreeNode");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(new JTree(root)));
        frame.pack();
        frame.setVisible(true);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // Go back to work.
        }
    }
}
