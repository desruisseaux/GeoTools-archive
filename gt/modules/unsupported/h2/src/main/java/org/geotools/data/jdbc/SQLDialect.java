package org.geotools.data.jdbc;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.GeometryDescriptor;
import org.openplans.spatialdbbox.JTS;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public abstract class SQLDialect {

    /**
     * Returns the string used to escape names.
     * <p>
     * This value is used to escape column, relation, and schema names.
     * </p>
     */
    public String getNameEscape() {
        return "\"";
    }
    
    /**
     * Quick accessor for {@link #getNameEscape()}.
     */
    protected final String ne() {
        return getNameEscape();
    }
    
    
    /**
     * Registers the sql type name to java type mappings that the dialect uses when 
     * reading and writing objects to and from the database.
     * <p>
     * Subclasses should extend (not override) this method to provide additional
     * mappings, or to override mappings provided by this implementation. This 
     * implementation provides the following mappings:
     * </p>
     */
    public void registerSqlTypeNameToClassMappings( Map<String,Class<?>> mappings ) {
        //TODO: do the normal types
    }
    
    /**
     * Registers the sql type to java type mappings that the dialect uses when 
     * reading and writing objects to and from the database.
     * <p>
     * Subclasses should extend (not override) this method to provide additional
     * mappings, or to override mappings provided by this implementation. This 
     * implementation provides the following mappings:
     * </p>
     * 
     */
    public void registerSqlTypeToClassMappings( Map<Integer,Class<?>> mappings ) {
        mappings.put(new Integer(Types.VARCHAR), String.class);
        mappings.put(new Integer(Types.CHAR), String.class);
        mappings.put(new Integer(Types.LONGVARCHAR), String.class);

        mappings.put(new Integer(Types.BIT), Boolean.class);
        mappings.put(new Integer(Types.BOOLEAN), Boolean.class);

        mappings.put(new Integer(Types.TINYINT), Short.class);
        mappings.put(new Integer(Types.SMALLINT), Short.class);

        mappings.put(new Integer(Types.INTEGER), Integer.class);
        mappings.put(new Integer(Types.BIGINT), Long.class);

        mappings.put(new Integer(Types.REAL), Float.class);
        mappings.put(new Integer(Types.FLOAT), Double.class);
        mappings.put(new Integer(Types.DOUBLE), Double.class);

        mappings.put(new Integer(Types.DECIMAL), BigDecimal.class);
        mappings.put(new Integer(Types.NUMERIC), BigDecimal.class);

        mappings.put(new Integer(Types.DATE), Date.class);
        mappings.put(new Integer(Types.TIME), Time.class);
        mappings.put(new Integer(Types.TIMESTAMP), Timestamp.class);

        //subclasses should extend to provide additional
    }
    
    /**
     * Registers the java type to sql type mappings that the datastore uses when 
     * reading and writing objects to and from the database.
     * * <p>
     * Subclasses should extend (not override) this method to provide additional
     * mappings, or to override mappings provided by this implementation. This 
     * implementation provides the following mappings:
     * </p>
     */
    public void registerClassToSqlMappings( Map<Class<?>,Integer> mappings ) {
        mappings.put(String.class, new Integer(Types.VARCHAR));

        mappings.put(Boolean.class, new Integer(Types.BOOLEAN));

        mappings.put(Short.class, new Integer(Types.SMALLINT));

        mappings.put(Integer.class, new Integer(Types.INTEGER));
        mappings.put(Long.class, new Integer(Types.BIGINT));

        mappings.put(Float.class, new Integer(Types.REAL));
        mappings.put(Double.class, new Integer(Types.DOUBLE));

        mappings.put(BigDecimal.class, new Integer(Types.NUMERIC));

        mappings.put(Date.class, new Integer(Types.DATE));
        mappings.put(Time.class, new Integer(Types.TIME));
        mappings.put(Timestamp.class, new Integer(Types.TIMESTAMP));
        
        //subclasses should extend and provide additional
    }
    
    /**
     * Returns the name of a geometric type based on its integer constant.
     * <p>
     * 
     * </p>
     */
    public String getGeometryTypeName( Integer type ) {
        return null;
    }
    
    /**
     * Returns the srid of the geometry column.
     */
    public Integer getGeometrySRID( String schemaName, String tableName, String columnName, Connection cx )
        throws SQLException {
       
        return null;
    }
    
    /**
     * Encodes the geometry column in a SELECT statement.
     */
    public void encodeGeometryColumn( GeometryDescriptor gatt, StringBuffer sql ) {
        column( gatt.getLocalName(), sql );
    }
    
    /**
     * Encodes the function for spatial extent, given the name of the geometry 
     * column.
     * <p>
     * The <tt>geometry</tt> is not previously encoded, so it should be escaped
     * with a call to {@link #column(String, StringBuffer)}.  
     * </p>
     */
    public void encodeGeometryEnvelope( String geometryColumn, StringBuffer sql ) {
       sql.append( "extent(" );
       column( geometryColumn, sql );
       sql.append( ")");
    }
    
    /**
     * Decodes an envelope or extent from a result set + column.
     */
    public Envelope decodeGeometryEnvelope( ResultSet rs, int column ) throws SQLException, IOException {
        //TODO: change spatialdb in a box to return ReferencedEnvelope
        return (Envelope) rs.getObject( column );
    }
    
    /**
     * Encodes a geometry value.
     */
    public void encodeGeometryValue( Geometry value, StringBuffer sql ) throws IOException {
        WKTWriter wkt = new WKTWriter();
        sql.append("GeometryFromText('").append(wkt.write(value))
            .append( "')");
    }
    
    /**
     * decodes a geometry value
     */
    public Geometry decodeGeometryValue( Object value, GeometryDescriptor descriptor ) 
        throws IOException {
        if ( value instanceof byte[] ) {
            return JTS.geometryFromBytes((byte[])value);
        }
        
        return null;
    }
    
    /**
     * Encodes the alias of a column.
     */
    public void encodeColumnAlias( String raw, StringBuffer sql ) {
        sql.append( " as ");
        column( raw, sql );
    }
    
    /**
     * Encodes anything post a create table statement.
     */
    public void encodePostCreateTable( String tableName , StringBuffer sql ) {
        
    }
    
    /**
     * Encodes a schema name by wrapping it in double quotes (").
     */
    public void schema( String raw, StringBuffer sql ) {
        sql.append(ne()).append(raw).append(ne());
    }
    
    /**
     * Encodes a table name by wrapping it in double quotes (").
     */
    public void table( String raw, StringBuffer sql ) {
        sql.append(ne()).append(raw).append(ne());
    }
    
    /**
     * Encodes a column name by wrapping it in double quotes (").
     */
    public void column( String raw, StringBuffer sql ) {
        sql.append(ne()).append(raw).append(ne());
    }
    
    
    /**
     * Encodes the primary key column of a table.
     */
    public void primaryKey( String column, StringBuffer sql ) {
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
    
    
    
    void nextPrimaryKeyValue( String tableName, String columnName, StringBuffer sql ) {
        sql.append( "SELECT CURRENT_VALUE FROM INFORMATION_SCHEMA.SEQUENCES WHERE ")
            .append( "SEQUENCE_CATALOG = '").append( tableName ).append( "';");
    }
    
    public Object getNextPrimaryKeyValue( String schemaName, String tableName, String columnName, Connection cx ) 
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
