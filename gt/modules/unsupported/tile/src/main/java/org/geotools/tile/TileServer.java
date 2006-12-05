package org.geotools.tile;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.tile.cache.SimpleTileCache;
import org.geotools.tile.cache.TileCache;
import org.geotools.tile.nasa.WorldWindTileStratagy;
import org.opengis.util.ProgressListener;

/**
 * Used to access a server of image tiles.
 * <p>
 * Usually a tile server offers a breakdown of tiles covering
 * a section of the earth (bounds) and a range of scales (zoom levels).
 * <p>
 * Examples:
 * <ul>
 * <li>http://labs.metacarta.com/wms-c/tilecache.py/
 * <li>http://worldwind25.arc.nasa.gov/tile/tile.aspx
 * </ul>
 * The protocol used to accesss these services is varried (from the tile conventions
 * used for nasa world wind, to the squeeky new wms-tile specification.
 * <p>
 * At this time no standardization has occured, each of these servers can
 * be accessed by a TileStratagy object, negotiating which one to use
 * is probably not automatic (yet).
 * </p>
 * @author jgarnett
 * @since 1.1.0
 */
public final class TileServer {
    
    private TileServiceInfo info;
    
    /** Map<URI,TileMap> each representing a different data layer */
    private Map layers = new HashMap();
    
    TileStratagy stratagy;
    TileCache cache;
    
    public TileServer( URL server, ProgressListener monitor ){
        stratagy = new WorldWindTileStratagy( server );
        info = stratagy.getInfo( monitor );        
        cache = new SimpleTileCache();
    }
    
    /** Incase info was created beforehand */
    public TileServer( TileServiceInfo info ){
        this.info = info;
        this.stratagy = info.getTileStratagy();
        this.cache = new SimpleTileCache();
    }
    
    public TileServiceInfo getInfo(){
        return info;
    }
    
    public List getTileMapIds( ProgressListener monitor ){
        return stratagy.getTileMapIds( info, monitor );
    }
    
    public TileMapInfo getTileMapInfo( URI id ){
        return getTileMap( id ).getInfo();
    }
    
    public synchronized TileMap getTileMap( URI id ){
        if( layers.containsKey( id )){
            return (TileMap) layers.get( id );
        }        
        TileMapInfo tileMapInfo = stratagy.getTileMapInfo( info, id, null );        
        TileMap tileMap = new TileMap( this, tileMapInfo );
        layers.put( id , tileMap );
        return tileMap;
    }

}
