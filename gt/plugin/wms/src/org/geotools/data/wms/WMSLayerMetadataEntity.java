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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.catalog.AbstractMetadataEntity;
import org.geotools.data.ows.Layer;


public class WMSLayerMetadataEntity extends AbstractMetadataEntity {
    public static final String TYPE_NAME = "layer";
    private Layer layer;

    public WMSLayerMetadataEntity(Layer layer) {
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
