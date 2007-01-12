package org.geotools.data.postgis.table;

import java.sql.ResultSetMetaData;

import org.geotools.feature.Feature;

/**
 * Captures information on handling a PostGIS table (or view).
 * <p>
 * This class captures the following information for a table or view:
 * <ul>
 * <li>interaction with "geometry columns" (declaired in the GEOMETRY_COLUMNS table
 *     or otherwise)
 * <li>interaction with the "feature identifier"
 * </ul>
 * @author Jody Garnett, Refractions Research Inc.
 */
public abstract class Table {
    //protected final Schema SCHEMA;
    //protected final String TABLE;
    protected final ResultSetMetaData METADATA;
    
    /**
     * Constructed with the table metadata.
     * 
     * @param metadata
     */
    public Table( ResultSetMetaData metadata ){
        METADATA = metadata;
    }
    
    /**
     * Generate a FeatureID for the provided feature.
     * 
     * @param feature
     * @return FeatureID
     */
    abstract String generateFid( Feature feature );   
}