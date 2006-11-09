package org.geotools.tile.cache;

import java.util.Set;

import org.geotools.geometry.Envelope2D;
import org.opengis.util.ProgressListener;

public interface TileRange {

    /**
     * Bounds of this tile range.
     * 
     * @return bounds of tiles in this range
     */
    Envelope2D getBounds();

    void load( ProgressListener monitor ); // monitor advances as each tile is available
    boolean isLoaded();
    void refresh( ProgressListener monitor ); // leaves tiles as is, but redraws

    /**
     * Tiles in range
     * 
     * @return Set of GridCoverage2d
     */
    Set getTiles();
}
