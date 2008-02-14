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

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.awt.Rectangle;
import java.io.IOException;


/**
 * A temporary placeholder while we wait for a real RTree implementation.
 *
 * @todo Should be replaced by iteration over the values returned by a RTree. We could consider
 *       org.geotools.index.RTree, but we need to clean that code first (API that should not be
 *       public, should be a java.util.Collection, avoid dependencies to JTS, search(Envelope)
 *       should returns a Collection backed by lazy iterator, etc.) and we may need to add a
 *       'RTree subtree(Envelope)' method.
 *
 *       NOTE: For the purpose of {@link TileManager}, we will need a RTree implementation that
 *       preserve insertion order (like LinkedHashSet). We will also need a way to returns all
 *       elements (maybe we should extends {@link java.util.AbstractSequentialList} directly).
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class RTree {
    /**
     * The tiles.
     */
    private final Tile[] tiles;

    /**
     * The region for each tile.
     */
    private final Rectangle[] regions;

    /**
     * Creates a tree with the given tiles.
     */
    public RTree(final Tile[] tiles) throws IOException {
        this.tiles = tiles;
        regions = new Rectangle[tiles.length];
        for (int i=0; i<tiles.length; i++) {
            regions[i] = tiles[i].getAbsoluteRegion();
        }
    }

    /**
     * Returns the tiles intersecting the given region.
     *
     * @todo Before this API goes public we need to clarify if the returned collection should be
     *       semantically a copy or be backed by the underlying RTree (i.e. removing an element
     *       from the view remove the corresponding element from the RTree). The later would be
     *       better if not to hard to implement. We must keep in mind that current usage of this
     *       method in {@link TileManager} expects a copy, so it will need to be modified.
     */
    public Collection<Tile> intersect(final Rectangle region) {
        final List<Tile> interest = new LinkedList<Tile>();
        for (int i=0; i<regions.length; i++) {
            if (region.intersects(regions[i])) {
                interest.add(tiles[i]);
            }
        }
        return interest;
    }

    /**
     * Returns the tiles entirely contained in the given region.
     *
     * @todo Before this API goes public we need to clarify if the returned collection should be
     *       semantically a copy or be backed by the underlying RTree (i.e. removing an element
     *       from the view remove the corresponding element from the RTree). The later would be
     *       better if not to hard to implement. We must keep in mind that current usage of this
     *       method in {@link MosaicImageWriter} expects a copy, so it will need to be modified.
     */
    public Collection<Tile> containedIn(final Rectangle region) {
        final List<Tile> interest = new LinkedList<Tile>();
        for (int i=0; i<regions.length; i++) {
            if (region.contains(regions[i])) {
                interest.add(tiles[i]);
            }
        }
        return interest;
    }
}
