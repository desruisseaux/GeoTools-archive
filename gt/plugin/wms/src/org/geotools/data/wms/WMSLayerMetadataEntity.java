/*
 * Created on Sep 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.catalog.AbstractMetadataEntity;
import org.geotools.data.ows.Layer;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSLayerMetadataEntity extends AbstractMetadataEntity {
    private Layer layer;
    public static final String TYPE_NAME = "layer";
    
    public WMSLayerMetadataEntity (Layer layer) {
        this.layer = layer;
    }
    
    public String getName() {
        return layer.getName();
    }
    
    public String getTitle() {
        return layer.getTitle();
    }
    
    public boolean isQueryable() {
        return layer.isQueryable();
    }
    
    public List getStyles() {
        return Collections.unmodifiableList(layer.getStyles());
    }
    
    public Set getCRS() {
        return Collections.unmodifiableSet(layer.getSrs());
    }
    
    public Map getBoundingBoxes() {
        return Collections.unmodifiableMap(layer.getBoundingBoxes());
    }
    
}
