package org.geotools.data.postgis.collection;

import java.sql.ResultSet;

import org.geotools.data.postgis.PostGISContent;
import org.geotools.data.postgis.table.NormalTable;
import org.geotools.data.postgis.table.Table;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureCollection;

/**
 * FeatureCollection wrapped over a result set.
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class TableFeatureCollection implements FeatureCollection {

    private NormalTable table;
    private ContentState state;

    public TableFeatureCollection(PostGISContent content, ContentState state) {
        this.state = state;
        table = (NormalTable) state.getEntry();
    }
    
}
