package org.geotools.tile;

import java.util.SortedSet;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

public interface TileMapInfo {
    
    CoordinateReferenceSystem getCRS();
    
    /**
     * Describes the range of ZoomLevels supported.
     * 
     * @return SortedSet of ZoomLevel
     */
    SortedSet getZoomLevels();
}
