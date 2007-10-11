package org.geotools.data.h2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;

import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.SQLDialect;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class H2Dialect extends SQLDialect {

    public String getNameEscape() {
        return "\"";
    }
    
    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        super.registerSqlTypeToClassMappings(mappings);
        
        //geometries
        mappings.put(new Integer(Types.OTHER), Geometry.class);
    }
    
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);
        
        //geometries
        mappings.put(Geometry.class, new Integer(Types.OTHER));
        mappings.put(Point.class, new Integer(Types.OTHER));
        mappings.put(LineString.class, new Integer(Types.OTHER));
        mappings.put(Polygon.class, new Integer(Types.OTHER));
    }
    
    public Integer getGeometrySRID(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        
        //execute SELECT srid(<columnName>) FROM <tableName> LIMIT 1;
        StringBuffer sql = new StringBuffer();
        sql.append( "SELECT getSRID(");
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
}
