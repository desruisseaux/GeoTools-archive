package org.geotools.tile;

import java.util.SortedSet;

import org.geotools.catalog.GeoResourceInfo;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public interface TileMapInfo extends GeoResourceInfo {   
    /**
     * Describes the range of ZoomLevels supported.
     * 
     * @return SortedSet of ZoomLevel
     */
    SortedSet getZoomLevels();
}
