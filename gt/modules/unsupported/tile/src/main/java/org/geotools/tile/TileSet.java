package org.geotools.tile;

import java.awt.Rectangle;

import org.opengis.coverage.grid.GridCoverage;
import org.opengis.filter.spatial.BBOX;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Set of tiles at a provided ZoomLevel.
 * <p>
 * This TIleSet can be understood according to:
 * <ul>
 * <li>getMap().getBounds() - Envelope including CRS
 * <li>getZoomLevel().getRows();
 * <li>getZoomLevel().getRows();
 * </ul>
 */
public class TileSet {    
    ZoomLevel level;
    TileMap context;
    
    TileSet( TileMap tilemap, ZoomLevel zoom ){
        level = zoom;
        context = tilemap;
    }
    
    TileMap getTileMap(){
        return context;
    }
    ZoomLevel getZoomLevel(){
        return level;
    }

    /**
     * Direct request for tile, non caching.
     * 
     * @param request
     * @return
     */
    GridCoverage getTile( int row, int col ){
        // delegate out to TileStratagy to handle
        // this request.        
        return null;
    }
    
    /**
     * Request tiles in a provided range
     * @param bbox Understood to match CRS
     * @return
     */
    TileRange getTileRange( Rectangle range ){
        // delegate to cache for access policy
        // delegate to stratagy object for render & access
        return null;
    }
    /**
     * Request tiles in a provided range
     * 
     * @param bbox Understood to match CRS
     * @return
     */
    TileRange getTileRange( Envelope bbox ){
        // delegate to cache for access policy
        // delegate to stratagy object for render & access
        return null;
    }
}
