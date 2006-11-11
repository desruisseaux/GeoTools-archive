package org.geotools.tile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.geotools.catalog.AbstractService;
import org.geotools.catalog.Catalog;
import org.geotools.catalog.ServiceInfo;
import org.geotools.util.ProgressListener;

/**
 * Service handle responsible for creating a TileServer.
 * <p>
 * This is a handle only; it can be used to:
 * <ul>
 * <li>Access TileInfo
 * <li>Access TileServer
 * <ul>
 */
public class TileService extends AbstractService {
    
    private TileServiceInfo info;
    
    public TileService( Catalog parent ) {
        super(parent);
    }
    
    public TileService( Catalog parent, Map params ) {
        super(parent, params);
    }
    
    /**
     * Provides a TileServiceInfo.
     */
    public ServiceInfo getInfo( ProgressListener monitor ) throws IOException {
        if( info != null ) return info;
//      Ask stratagy object for "info"...        
        return info;
    }

    public List members( ProgressListener monitor ) throws IOException {
//      Ask stratagy object for "tilemaps"...  
        return null;
    }

    public Object resolve( Class adaptee, ProgressListener monitor ) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean canResolve( Class adaptee ) {
        return false;
    }

    public URI getIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
