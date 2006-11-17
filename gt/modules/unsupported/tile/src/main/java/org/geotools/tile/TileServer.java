package org.geotools.tile;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.opengis.util.ProgressListener;


public class TileServer {
    
    private GridInfo info;
    private List maps;
    
    public TileServer( URL server, ProgressListener monitor ){
        info = new GridInfo( server, monitor );
        maps = aquireTileMaps( info );
    }
    
    private List aquireTileMaps( GridInfo info2 ) {
        return Collections.EMPTY_LIST;
    }

    /** Incase info was created beforehand */
    public TileServer( GridInfo info ){
        this.info = info;        
    }
    
    GridInfo getInfo(){
        return info;
    }
    List/*<TileMap*/ getTileMapList(){
        return maps;
    }
}
