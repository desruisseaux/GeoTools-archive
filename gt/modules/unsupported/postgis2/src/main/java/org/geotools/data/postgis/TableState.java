package org.geotools.data.postgis;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;

public class TableState extends ContentState {

    private PostGISContent content;

    public TableState(PostGISContent content, ContentEntry entry) {
        super(entry);
        this.content = content;
    }

}
