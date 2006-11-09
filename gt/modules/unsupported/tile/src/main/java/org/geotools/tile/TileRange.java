package org.geotools.tile;

import java.util.Set;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.util.ProgressListener;

public interface TileRange {
    ReferencedEnvelope getBounds(); // bounds of tiles represented, w/ CRS

    void load( ProgressListener monitor ); // monitor advances as each tile is available
    boolean isLoaded();
    void refresh( ProgressListener monitor ); // leaves tiles as is, but redraws

    /**
     * Tiles in range
     * 
     * @return Set of GridCoverage
     */
    Set getTiles();
}
