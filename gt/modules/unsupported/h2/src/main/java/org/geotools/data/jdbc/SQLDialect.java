package org.geotools.data.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.GeometryDescriptor;
import org.openplans.spatialdbbox.JTS;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class SQLDialect {

    /**
     * Returns the string used to escape names.
     */
    String getNameEscape() {
        return "\"";
    }
    
    /**
     * Encodes a schema name by wrapping it in double quotes (").
     */
    void schema( String raw, StringBuffer sql ) {
        sql.append("\"").append(raw).append("\"");
    }
    
    /**
     * Encodes a table name by wrapping it in double quotes (").
     */
    void table( String raw, StringBuffer sql ) {
        sql.append("\"").append(raw).append("\"");
    }
    
    /**
     * Encodes a column name by wrapping it in double quotes (").
     */
    void column( String raw, StringBuffer sql ) {
        sql.append("\"").append(raw).append("\"");
    }
    
    /**
     * Encodes the primary key column of a table.
     */
    void primaryKey( String column, StringBuffer sql ) {
        column(column,sql);
        sql.append( " int AUTO_INCREMENT(1) PRIMARY KEY" );
    }
    
    /**
     * Encodes a value.
     * 
     */
    void value( Object value, Class type, StringBuffer sql ) {
        if ( CharSequence.class.isAssignableFrom( type ) ) {
            sql.append("'").append(value).append("'");
        }
        else {
            sql.append(value);
        }
    }
    
    /**
     * Encodes a geometry value.
     */
    void encodeGeometryValue( Geometry value, StringBuffer sql ) {
        WKTWriter wkt = new WKTWriter();
        sql.append("GeometryFromText('").append(wkt.write(value))
            .append( "')");
    }
    
    /**
     * decodes a geometry value
     */
    Geometry decodeGeometryValue( Object value, GeometryDescriptor descriptor ) {
        if ( value instanceof byte[] ) {
            return JTS.geometryFromBytes((byte[])value);
        }
        
        return null;
    }
    
    /**
     * Flag indicating wether the sql dialect can calculate extends using an 
     * aggregate function.
     * <p>
     * This method is called before 
     * </p>
     * 
     */
    boolean hasAggregateExtent() {
        return false;
    }
    
    /**
     * Encodes the function for calculating spatial extent using an aggregate
     * function.
     */
    void aggregateExtent( String geometryColumn, StringBuffer sql ) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Encodes the function for spatial extent, given the name of the geometry 
     * column.
     * <p>
     * The <tt>geometry</tt> is not previously encoded, so it should be escaped
     * with a call to {@link #column(String, StringBuffer)}.  
     * </p>
     */
    void extent( String geometryColumn, StringBuffer sql ) {
       sql.append( "extent(" );
       column( geometryColumn, sql );
       sql.append( ")");
    }
    
    /**
     * Decodes an envelope or extent from a result set + column.
     */
    ReferencedEnvelope envelope( ResultSet rs, int column ) throws SQLException {
        //TODO: change spatialdb in a box to return ReferencedEnvelope
        return ReferencedEnvelope.reference( (Envelope) rs.getObject( column ) );
    }
    
    void nextPrimaryKeyValue( String tableName, String columnName, StringBuffer sql ) {
        sql.append( "SELECT CURRENT_VALUE FROM INFORMATION_SCHEMA.SEQUENCES WHERE ")
            .append( "SEQUENCE_CATALOG = '").append( tableName ).append( "';");
    }
    
    Object getNextPrimaryKeyValue( String schemaName, String tableName, String columnName, Connection cx ) 
        throws SQLException {
        
        Statement st = cx.createStatement();
        try {
            ResultSet rs = st.executeQuery( 
                "SELECT b.COLUMN_DEFAULT " + 
                " FROM INFORMATION_SCHEMA.INDEXES A, INFORMATION_SCHEMA.COLUMNS B " + 
                "WHERE a.TABLE_NAME = b.TABLE_NAME " +
                " AND a.COLUMN_NAME = b.COLUMN_NAME " +
                " AND a.TABLE_NAME = '" + tableName + "' " +
                " AND a.COLUMN_NAME = '" + columnName + "' " + 
                " AND a.PRIMARY_KEY = TRUE"
            );
            

            //figure out which sequence to query
            String sequence = null;
            try {
                //TODO: there has to be a better way to do this
                rs.next();
                String string = rs.getString( 1 );
                sequence = string.substring( string.indexOf( "SYSTEM_SEQUENCE"), string.length()-1 );
            }
            finally {
                JDBCDataStore.closeSafe( rs );
            }
            try {
                if ( schemaName != null ) {
                    rs = st.executeQuery( "SELECT CURRVAL('" + schemaName + "','" + sequence + "')");
                    
                }
                else {
                    rs = st.executeQuery( "SELECT CURRVAL('" + sequence + "')");    
                }
                
                rs.next();
                
                int value = rs.getInt( 1 );
                return new Integer( value + 1 );
            }
            finally {
                JDBCDataStore.closeSafe( rs );
            }

        }
        finally {
            JDBCDataStore.closeSafe( st );
            
        }
    }
}
