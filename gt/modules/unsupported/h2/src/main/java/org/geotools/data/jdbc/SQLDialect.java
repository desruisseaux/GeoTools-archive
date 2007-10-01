package org.geotools.data.jdbc;

public class SQLDialect {

    /**
     * Encodes a table name by wrapping it in double quotes (").
     */
    void table( String raw, StringBuffer sql ) {
        sql.append("\"" + raw + "\"");
    }
    
    /**
     * Encodes a column name by wrapping it in double quotes (").
     */
    void column( String raw, StringBuffer sql ) {
        sql.append("\"" + raw + "\"");
    }
}
