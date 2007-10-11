package org.geotools.data.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.SQLDialect;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

public class MySQLDialect extends SQLDialect {

    /**
     * mysql spatial types
     */
    protected Integer POINT = new Integer( 2001 );
    protected Integer LINESTRING = new Integer( 2002 );
    protected Integer POLYGON = new Integer( 2003 );
    protected Integer GEOMETRY = new Integer( 2004 );
    
    public String getNameEscape() {
        return "";
    }
    
    public String getGeometryTypeName(Integer type) {
        if ( POINT.equals( type ) ) 
            return "POINT";
        if ( LINESTRING.equals( type ) ) 
            return "LINESTRING";
        if ( POLYGON.equals( type ) ) 
            return "POLYGON";
        if ( GEOMETRY.equals( type ) ) 
            return "GEOMETRY";
        
        return super.getGeometryTypeName(type);
    }
    
    
    public Integer getGeometrySRID(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        
        //execute SELECT srid(<columnName>) FROM <tableName> LIMIT 1;
        StringBuffer sql = new StringBuffer();
        sql.append( "SELECT srid(");
        column( columnName, sql );
        sql.append( ") ");
        sql.append( "FROM ");
        
        if ( schemaName != null ) {
            schema( schemaName, sql );
            sql.append(".");
        }
        table( tableName, sql );
        sql.append( " WHERE ");
        column( columnName, sql );
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
    
    public void encodeGeometryColumn(GeometryDescriptor gatt, StringBuffer sql) {
        sql.append( "asWKB(");
        column( gatt.getLocalName(), sql );
        sql.append( ")");
    }
    
    public void encodeGeometryEnvelope(String geometryColumn, StringBuffer sql) {
        sql.append( "asWKB(");
        sql.append( "envelope(");
        column( geometryColumn, sql );
        sql.append( "))");
    }
    
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column)
            throws SQLException, IOException {
        
        String wkb = rs.getString( column );
        try {
            //TODO: srid
            Polygon polygon = (Polygon) new WKBReader().read( wkb.getBytes() );
            return polygon.getEnvelopeInternal();
        } 
        catch (ParseException e) {
            String msg = "Error decoding wkb for envelope";
            throw (IOException) new IOException( msg ).initCause( e );
        }
    }
    
    public Geometry decodeGeometryValue(Object value, GeometryDescriptor descriptor) 
        throws IOException {
        
        byte[] bytes = null;
        if ( value instanceof byte[] ) {
            bytes = (byte[]) value;
        }
        else if ( value instanceof String ) {
            bytes = ((String) value).getBytes();
        }
        
        if ( bytes != null ) {
            try {
                return new WKBReader().read( bytes );
            } catch (ParseException e) {
                String msg = "Error decoding wkb";
                throw (IOException) new IOException( msg ).initCause( e );
            }
        }
        
        return super.decodeGeometryValue(value, descriptor);
    }
    
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);
    
        //TODO: multi geometries
        mappings.put( Point.class, POINT );
        mappings.put( LineString.class, LINESTRING );
        mappings.put( Polygon.class, POLYGON );
        mappings.put( Geometry.class, GEOMETRY );
    }
    
    
    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        super.registerSqlTypeToClassMappings(mappings);
        
        //TODO: multi geometries
        mappings.put( POINT, Point.class );
        mappings.put( LINESTRING, LineString.class );
        mappings.put( POLYGON, Polygon.class );
        mappings.put( GEOMETRY, Geometry.class );
    }
    
    public void registerSqlTypeNameToClassMappings(
            Map<String, Class<?>> mappings) {
       super.registerSqlTypeNameToClassMappings(mappings);
      
       mappings.put( "point", Point.class );
       mappings.put( "linestring", LineString.class );
       mappings.put( "polygon", Polygon.class );
       mappings.put( "geometry", Geometry.class );
    }
    
    public void encodePostCreateTable(String tableName, StringBuffer sql) {
        //TODO: make this configurable
        sql.append( "ENGINE=InnoDB");
    }
    
    public void primaryKey(String column, StringBuffer sql) {
        column(column,sql);
        sql.append( " int AUTO_INCREMENT PRIMARY KEY" );
        
    }

    public Object getNextPrimaryKeyValue(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        
        Statement st = cx.createStatement();
        try {
            String sql = "SELECT max( " + columnName + ")+1" +
            " FROM " + schemaName + "." + tableName; 
            JDBCDataStore.LOGGER.fine( sql );
            
            ResultSet rs = st.executeQuery( sql );
            try {
                rs.next();
                return new Integer( rs.getInt( 1 ) ); 
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
