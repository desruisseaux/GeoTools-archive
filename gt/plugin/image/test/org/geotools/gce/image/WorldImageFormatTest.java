/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.gce.image;

import junit.framework.TestCase;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldImageFormatTest extends TestCase {
    private WorldImageFormat format;

    public WorldImageFormatTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        format = new WorldImageFormat();
    }

    public void testGetWorldExtension() {
        assertEquals(WorldImageFormat.getWorldExtension("png"), ".pgw");
        assertEquals(WorldImageFormat.getWorldExtension("gif"), ".gfw");
        assertEquals(WorldImageFormat.getWorldExtension("jpg"), ".jgw");
        assertEquals(WorldImageFormat.getWorldExtension("jpeg"), ".jgw");
        assertEquals(WorldImageFormat.getWorldExtension("tif"), ".tfw");
        assertEquals(WorldImageFormat.getWorldExtension("tiff"), ".tfw");
    }
}
