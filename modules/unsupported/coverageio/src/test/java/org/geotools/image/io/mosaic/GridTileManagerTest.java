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

import java.io.IOException;


/**
 * Tests {@link GridTileManager}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class GridTileManagerTest extends TileManagerTest {
    /**
     * The tile manager factory to be given to the {@linkplain #builder builder}. This method
     * make sure that only {@link GridTileManager} instances are created. Then we inherit the
     * test suite from the base class.
     *
     * @return The tile manager factory to use.
     * @throws IOException If an I/O operation was required and failed.
     */
    @Override
    protected TileManagerFactory getTileManagerFactory() throws IOException {
        return new TileManagerFactory(null) {
            @Override
            protected TileManager createGeneric(final Tile[] tiles) throws IOException {
                return new GridTileManager(tiles);
            }
        };
    }
}
