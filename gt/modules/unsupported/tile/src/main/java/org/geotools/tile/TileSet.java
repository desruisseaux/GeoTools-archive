package org.geotools.tile;

import java.awt.Rectangle;

import org.geotools.tile.cache.TileRange;

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
public abstract class TileSet {
    TileMap context;
    ZoomLevel level; // valid according to metadata
    TileDraw draw; // provided from stratagy
    
    protected TileSet( TileMap context, ZoomLevel level, TileDraw draw ){
        this.level = level;
        this.context = context;
        this.draw = draw;
    }
    
    public TileMap getTileMap(){
        return context;
    }
    public ZoomLevel getZoomLevel(){
        return level;
    }
    
    /**
     * Request tiles in a provided range
     * @param bbox Understood to match CRS
     * @return
     */
    public TileRange getTileRange( Rectangle range ){
        
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
    public TileRange getTileRange( Envelope bbox ){
        // TODO: caclate range based on bbox metadata
        int x=1;
        int y=1;
        int w=1;
        int h=1;
        Rectangle range = new Rectangle(x,y,w,h);
        return getTileRange( range );
    }
}
