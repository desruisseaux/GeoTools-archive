package org.geotools.data.postgis.table;

/**
 * Represents a Postgres Schema.
 * <p>
 * </p>
 * @author Jody Garnett, Refractions Research Inc.
 */
public class Schema {
    protected String SCHEMA_NAME;
    
    public Schema( String schemaName ){
        SCHEMA_NAME = schemaName;
    }
    
    public String toString() {
        return SCHEMA_NAME;
    }
}
