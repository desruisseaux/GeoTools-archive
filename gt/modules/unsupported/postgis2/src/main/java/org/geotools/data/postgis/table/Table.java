package org.geotools.data.postgis.table;

import java.sql.ResultSetMetaData;

import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.Feature;
import org.geotools.feature.type.TypeName;

/**
 * Captures information on handling a PostGIS table (or view).
 * <p>
 * This class captures the following information for a table or view:
 * <ul>
 * <li>geometry policy: interaction with "geometry columns" (declaired in the GEOMETRY_COLUMNS table
 * <li>interaction with "geometry columns" (declared in the GEOMETRY_COLUMNS table
 *     or otherwise)
 * <li>identification policy: interaction with the "feature identifier"
 * </ul>
 * @author Jody Garnett, Refractions Research Inc.
 */
public abstract class Table extends ContentEntry {
    
    //protected final Schema SCHEMA;
    //protected final String TABLE;
    protected final ResultSetMetaData METADATA;
    
    /**
     * Constructed with the table metadata.
     * 
     * @param metadata
     */
    public Table( ContentDataStore datastore, TypeName typeName ){
        super( datastore, typeName );
        METADATA = null; // look up?
    }
    
}