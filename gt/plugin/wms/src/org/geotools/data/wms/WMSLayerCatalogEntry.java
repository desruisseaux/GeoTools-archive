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

import org.geotools.data.ows.Layer;
import org.opengis.catalog.CatalogEntry;
import org.opengis.catalog.MetadataEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class WMSLayerCatalogEntry implements CatalogEntry {
    WebMapServer wms;
    Layer layer;
    List metadatas;

    public WMSLayerCatalogEntry(WebMapServer wms, Layer layer) {
        this.wms = wms;
        this.layer = layer;
        metadatas = new ArrayList();
        metadatas.add(new WMSLayerMetadataEntity(layer));
    }

    /* (non-Javadoc)
     * @see org.opengis.catalog.CatalogEntry#getResource()
     */
    public Object getResource() {
        return wms;
    }

    /* (non-Javadoc)
     * @see org.opengis.catalog.CatalogEntry#getDataName()
     */
    public String getDataName() {
        return layer.getName();
    }

    /* (non-Javadoc)
     * @see org.opengis.catalog.CatalogEntry#getNumMetadata()
     */
    public int getNumMetadata() {
        return metadatas.size();
    }

    /* (non-Javadoc)
     * @see org.opengis.catalog.CatalogEntry#getMetadataNames()
     */
    public String[] getMetadataNames() {
        String[] names = new String[4];
        names[0] = WMSLayerMetadataEntity.TYPE_NAME;

        return names;
    }

    /* (non-Javadoc)
     * @see org.opengis.catalog.CatalogEntry#getMetadata(int)
     */
    public MetadataEntity getMetadata(int index) {
        return (MetadataEntity) metadatas.get(index);
    }

    /* (non-Javadoc)
     * @see org.opengis.catalog.CatalogEntry#getMetadata(java.lang.String)
     */
    public MetadataEntity getMetadata(String name) {
        if (WMSLayerMetadataEntity.TYPE_NAME == name) {
            return (MetadataEntity) metadatas.get(0);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.catalog.CatalogEntry#iterator()
     */
    public Iterator iterator() {
        return metadatas.iterator();
    }
}
