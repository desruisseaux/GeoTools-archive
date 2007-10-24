package org.geotools.data.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;

import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.SQLDialect;
import org.opengis.feature.type.GeometryDescriptor;
import org.openplans.spatialdbbox.JTS;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTWriter;

public class H2Dialect extends SQLDialect {

    public String getNameEscape() {
        return "\"";
    }
    
    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        super.registerSqlTypeToClassMappings(mappings);
        
        //geometries
        //mappings.put(new Integer(Types.OTHER), Geometry.class);
        mappings.put(new Integer(Types.BLOB), Geometry.class);
    }
    
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);
        
        //geometries
        /*
        mappings.put(Geometry.class, new Integer(Types.OTHER));
        mappings.put(Point.class, new Integer(Types.OTHER));
        mappings.put(LineString.class, new Integer(Types.OTHER));
        mappings.put(Polygon.class, new Integer(Types.OTHER));
        */
        mappings.put(Geometry.class, new Integer(Types.BLOB));
        mappings.put(Point.class, new Integer(Types.BLOB));
        mappings.put(LineString.class, new Integer(Types.BLOB));
        mappings.put(Polygon.class, new Integer(Types.BLOB));
    }
    
    public Integer getGeometrySRID(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        
        //execute SELECT srid(<columnName>) FROM <tableName> LIMIT 1;
        StringBuffer sql = new StringBuffer();
        sql.append( "SELECT getSRID(");
        encodeColumnName( columnName, sql );
        sql.append( ") ");
        sql.append( "FROM ");
        
        if ( schemaName != null ) {
            encodeTableName( schemaName, sql );
            sql.append(".");
        }
        encodeSchemaName( tableName, sql );
        sql.append( " WHERE ");
        encodeColumnName( columnName, sql );
        sql.append( " is not null LIMIT 1");
        
        JDBCDataStore.LOGGER.fine( sql.toString() );
        
        Statement st = cx.createStatement();
        try {
            
            ResultSet rs = st.executeQuery( sql.toString() );
            try {
                if ( rs.next() ) {
                    return new Integer( rs.getInt( 1 ) );
                }
                else {
                    //could not find out
                    return null;
                }
            }
            finally {
                JDBCDataStore.closeSafe( rs );
            }
          
        }
        finally {
            JDBCDataStore.closeSafe( st );
        }
    }
    
    
    public void encodeGeometryEnvelope(String geometryColumn, StringBuffer sql) {
        //TODO: change spatialdbbox to use envelope
        sql.append( "envelope(" );
        encodeColumnName( geometryColumn, sql );
        sql.append( ")");
    }
    
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column)
            throws SQLException, IOException {
        //TODO: change spatialdb in a box to return ReferencedEnvelope
        return (Envelope) rs.getObject( column );
    }
    
    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql)
            throws IOException {
        sql.append( "GeomFromText ('" );
        sql.append( new WKTWriter().write( value ) );
        sql.append( "',");
        sql.append( srid );
        sql.append( ")");
    }
    
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor,
            ResultSet rs, String column, GeometryFactory factory) throws IOException, SQLException {
        
        byte[] bytes = rs.getBytes( column );
        try {
            return new WKBReader(factory).read( bytes );
        } 
        catch (ParseException e) {
            throw (IOException) new IOException().initCause(e);
        }
        //return JTS.geometryFromBytes( bytes );
    }
    
    public void encodePrimaryKey(String column, StringBuffer sql) {
        encodeColumnName(column,sql);
        sql.append( " int AUTO_INCREMENT(1) PRIMARY KEY" );
    }
    
    public Object getNextPrimaryKeyValue(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
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
