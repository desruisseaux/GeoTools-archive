/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.wms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.catalog.CatalogEntry;
import org.geotools.data.ows.Layer;


public class WMSLayerCatalogEntry implements CatalogEntry {
    WebMapServer wms;
    Layer layer;
    Map metadatas;   
    
    public WMSLayerCatalogEntry(WebMapServer wms, Layer layer) {
        this.wms = wms;
        this.layer = layer;
        metadatas = new HashMap();
        
        WMSLayerMetadataEntity metadata = new WMSLayerMetadataEntity(layer);        
        metadatas.put( metadata.getName(), metadata );
    }

    /**
     * Access Layer.
     * 
     * @see org.geotools.catalog.CatalogEntry#resource()
     * @return an org.geotools.data.ows.Layer
     */
    public Object resource() {
        return layer;
    }
    
    /** Access to parent WebMapServer */
    public WebMapServer getWMS() {
        return wms;
    }
    
    /**
     * Layer name, for display in user interface.
     * 
     * @return Display name for this Layer
     */
    public String getDataName() {
        return layer.getName();
    }

    /**
     * Map of MetadataEntity by name
     * 
     * @return Map of Metadata by name
     */
    public Map metadata() {
        return Collections.unmodifiableMap( metadatas );
    }
    
    /**
     * Names of available metadata.
     * 
     * @return names of available metadata
     */
    public String[] getMetadataNames() {
        return (String[]) metadatas.keySet().toArray( new String[ metadata().size() ] );
    }
    
    public boolean equals( Object obj ) {
        if (obj == null || !(obj instanceof WMSLayerCatalogEntry)) {
            return false;
        }
        WMSLayerCatalogEntry other = (WMSLayerCatalogEntry) obj; 
        // wms is currently identity
        return (wms == null? other.wms == null :
                              wms.equals(other.wms)) &&
               (layer == null? other.layer == null :
                               layer.equals(other.layer));
    }
    /**
     * To to base on the same things as equals
     * hascode on wms, layer and metadatas.
     * 
     * @see java.lang.Object#hashCode()
     * @return hasCode of WMSLayerCatalogEntry
     */
    public int hashCode() {
        return (wms == null? 0 : wms.hashCode() ) |
               (layer == null?  0 : layer.hashCode() << 30 ) |
               (metadatas == null ? 0 : metadatas.hashCode() << 60 );
    }

}
