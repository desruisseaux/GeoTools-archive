package org.geotools.tile;

import java.util.HashMap;
import java.util.Map;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Represents tiled content avaialble at a range of zoom levels.
 * <p>
 * Each zoom level has a corrasponding TileSet.
 * </p>
 * @author jgarnett
 */
public class TileMap {
    TileMapInfo info;
    Map map; // from ZoomLevel to TileSet
    
    TileMap( TileMapInfo info ){
        this.info = info;
        map = new HashMap();
    }
 
    CoordinateReferenceSystem getCRS(){
        return info.getCRS();
    }
    TileMapInfo getInfo(){
        return info;
    }

    public synchronized TileSet getTileSet( ZoomLevel zoom ){
        if( map.containsKey( zoom )){
            return (TileSet) map.get( zoom );
        }
        TileSet tileset = createTileSet( zoom );
        map.put( zoom, tileset );
        return tileset;
    }

    /**
     * Lazy creation of TileSet.
     * 
     * Actual TileSet instance created by TileStratagy.
     * 
     * @param zoom
     * @return TileSet
     */
    private TileSet createTileSet( ZoomLevel zoom ) {
        // TODO Auto-generated method stub
        return null;
    }
}
