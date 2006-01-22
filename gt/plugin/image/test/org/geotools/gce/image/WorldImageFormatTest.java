/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gce.image;

import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 *
 * @author rgoulds
 * @source $URL$
 */
public class WorldImageFormatTest extends TestCase {
    private WorldImageFormatTest format;

    public WorldImageFormatTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        format = new WorldImageFormatTest("test");
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
