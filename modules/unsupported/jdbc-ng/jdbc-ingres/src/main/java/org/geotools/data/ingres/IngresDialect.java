package org.geotools.data.ingres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.jdbc.BasicSQLDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class IngresDialect extends BasicSQLDialect {

    final static Map<String, Class> TYPE_TO_CLASS_MAP = new HashMap<String, Class>() {
        {
        	put("POINT", Point.class);
        	put("LINESTRING", LineString.class);
        	put("POLYGON", Polygon.class);
            put("MULTIPOINT", MultiPoint.class);
            put("MULTILINESTRING", MultiLineString.class);
            put("MULTIPOLYGON", MultiPolygon.class);
            put("GEOMCOLLECTION", GeometryCollection.class);
        }
    };
	
    /** Whether to use only primary filters for BBOX filters */
    boolean looseBBOXEnabled = false;
	
    public IngresDialect(JDBCDataStore dataStore) {
        super(dataStore);
        
    }
    
    public boolean isLooseBBOXEnabled() {
        return looseBBOXEnabled;
    }

    public void setLooseBBOXEnabled(boolean looseBBOXEnabled) {
        this.looseBBOXEnabled = looseBBOXEnabled;
    }

    @Override
    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql) throws IOException {
    }
    
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs,
            String column, GeometryFactory factory, Connection cx ) throws IOException, SQLException {
    	return null;
    }
    
    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column, Connection cx)
            throws SQLException, IOException {
        return null;
    }

    @Override
    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
        Connection cx) throws SQLException {
    	
        // first attempt, try with the geometry metadata
        Statement statement = null;
        ResultSet result = null;
        Integer srid = null;
        try {
            if (schemaName == null)
                schemaName = "geotools"; //default schema
            
            String sqlStatement = "SELECT SRID FROM GEOMETRY_COLUMNS WHERE " //
                    + "F_TABLE_SCHEMA = '" + schemaName + "' " //
                    + "AND F_TABLE_NAME = '" + tableName + "' " //
                    + "AND F_GEOMETRY_COLUMN = '" + columnName + "'";

            LOGGER.log(Level.FINE, "Geometry type check; {0} ", sqlStatement);
            statement = cx.createStatement();
            result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                srid = result.getInt(1);
            }
            dataStore.closeSafe(result);
            
            // if srid is null or -1, then the srid is undefined in the GEOMETRY_COLUMNS table
            if(srid == null || srid.intValue() == -1) {
            	
                sqlStatement = "SELECT FIRST 1 SRID(" + columnName + ") " +
                               "FROM " + schemaName + "." + tableName;
                
                result = statement.executeQuery(sqlStatement);
                
                if (result.next()) {
                    srid = result.getInt(1);
                }
            }
        } finally {
            dataStore.closeSafe(result);
            dataStore.closeSafe(statement);
        }

        return srid;
    }
    
    
    @Override
    public void encodePrimaryKey(String column, StringBuffer sql) {
    	super.encodePrimaryKey(column, sql);
    	sql.append(" NOT NULL");
    }

    @Override
    public void encodeGeometryEnvelope(String tableName, String geometryColumn, StringBuffer sql) {
    }

    /**Determines the class mapping for a particular column of a table.*/
    @Override
    public Class<?> getMapping(ResultSet columnMetaData, Connection cx)
            throws SQLException {
    	
    	final int SCHEMA_NAME = 2;
        final int TABLE_NAME = 3;
        final int COLUMN_NAME = 4;
        // grab the information we need to proceed
        String tableName = columnMetaData.getString(TABLE_NAME);
        String columnName = columnMetaData.getString(COLUMN_NAME);
        String schemaName = columnMetaData.getString(SCHEMA_NAME);
        
        Statement statement = null;
        ResultSet result = null;
        String gType = null;
        try {
            String sqlStatement = "SELECT GEOMETRY_TYPE FROM GEOMETRY_COLUMNS WHERE " //
                    + "F_TABLE_SCHEMA = '" + schemaName + "' " //
                    + "AND F_TABLE_NAME = '" + tableName + "' " //
                    + "AND F_GEOMETRY_COLUMN = '" + columnName + "'";

            LOGGER.log(Level.FINE, "Geometry type check; {0} ", sqlStatement);
            statement = cx.createStatement();
            result = statement.executeQuery(sqlStatement);
       
            
            if (result.next()) {
                gType = result.getString(1);  
            }

        } finally {
            dataStore.closeSafe(result);
            dataStore.closeSafe(statement);
        }

        // decode the type into
        // Here gType may be null if database fail to give us an answer,
        // however if it did give an answer, make sure its without leading or trailing whitespaces.
        Class geometryClass = (Class) TYPE_TO_CLASS_MAP.get(gType==null? gType : gType.trim());

        return geometryClass;
    }
    
    
    @Override
    //to register additional mappings if necessary
    public void registerSqlTypeNameToClassMappings(Map<String, Class<?>> mappings) {
        super.registerSqlTypeNameToClassMappings(mappings);

    }
    
    @Override
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);
    }

    @Override
    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        super.registerSqlTypeToClassMappings(mappings);
    }
        	
    @Override
    public String getSequenceForColumn(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
    	
        return null;
    }
    
        
    @Override
    public Object getNextSequenceValue(String schemaName, String sequenceName,
            Connection cx) throws SQLException {
    	return null;
    }

    @Override
    public boolean includeTable(String schemaName, String tableName, Connection cx)
            throws SQLException {
        if (tableName.equals("geometry_columns")) {
            return false;
        } else if (tableName.startsWith("spatial_ref_sys")) {
            return false;
        } else if (tableName.equals("geography_columns")) {
            return false;
        }
       // others?
        return true;
    }

    @Override   
    public boolean isLimitOffsetSupported() {
    	return false;
    }
    
    @Override
    public void applyLimitOffset(StringBuffer sql, int limit, int offset) {
    }
}
