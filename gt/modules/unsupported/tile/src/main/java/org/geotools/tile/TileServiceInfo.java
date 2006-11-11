package org.geotools.tile;

import java.net.URI;

import javax.swing.Icon;

import org.geotools.catalog.ServiceInfo;

/**
 * Description of tile service, including name,title,icon,bounds,tile size
 */
public interface TileServiceInfo extends ServiceInfo {
    int getTileWidth();
    int getTileHeight();
}
