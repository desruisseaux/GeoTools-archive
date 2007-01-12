package org.geotools.data.postgis.table;

import java.net.URI;

import javax.swing.Icon;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.data.postgis.PostGISContent;
import org.geotools.data.store.ContentState;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class TableInfo implements GeoResourceInfo {

    public TableInfo(PostGISContent content, ContentState state) {
    }

    public Envelope getBounds() {
        return null;
    }

    public CoordinateReferenceSystem getCRS() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public Icon getIcon() {
        return null;
    }

    public String[] getKeywords() {
        return null;
    }

    public String getName() {
        return null;
    }

    public URI getSchema() {
        return null;
    }

    public String getTitle() {
        return null;
    }

}
