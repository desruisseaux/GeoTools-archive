/*
 * Created on 16/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.oracle;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import oracle.jdbc.OracleConnection;
//import oracle.sdoapi.OraSpatialManager;
//import oracle.sdoapi.util.GeometryMetaData;

import org.geotools.data.AttributeReader;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.FeatureTypeInfo;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.oracle.attributeio.SDOAttributeIO;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderOracle;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public class OracleDataStore extends JDBCDataStore {
    /**
     * @param connectionPool
     * @param config
     * @throws IOException
     */
    public OracleDataStore(ConnectionPool connectionPool, JDBCDataStoreConfig config) throws IOException {
        super(connectionPool, config);
    }

    /**
     * @param connectionPool
     * @throws DataSourceException
     */
    public OracleDataStore(ConnectionPool connectionPool, String schemaName, Map fidGeneration) throws IOException {
        this(connectionPool, schemaName, schemaName, fidGeneration);
    }
    
    /**
     * @param connectionPool
     * @param namespace
     * @throws DataSourceException
     */
    public OracleDataStore(ConnectionPool connectionPool, String namespace, String schemaName, Map fidGeneration) throws IOException {
        super(connectionPool, schemaName, fidGeneration, namespace);
    }

    
    /** Crops non feature type tables. 
     * There are alot of additional tables in a Oracle tablespace. This tries
     * to remove some of them.  If the schemaName is provided in the Constructor
     * then the job of narrowing down tables will be mush easier.  Otherwise
     * there are alot of Meta tables and SDO tables to cull.  This method tries
     * to remove as many as possible. 
     * 
     * @see org.geotools.data.jdbc.JDBCDataStore#allowTable(java.lang.String)
     */
    protected boolean allowTable(String tablename) {
        if (tablename.endsWith("$"))  {
            return false;
        } else if (tablename.startsWith("XDB$"))  {
            return false;
        } else if (tablename.startsWith("DR$"))  {
            return false;
        } else if (tablename.startsWith("DEF$"))  {
            return false;
        } else if (tablename.startsWith("SDO_"))  {
            return false;
        } else if (tablename.startsWith("WM$"))  {
            return false;
        } else if (tablename.startsWith("WK$"))  {
            return false;
        } else if (tablename.startsWith("AW$"))  {
            return false;
        } 
        
        return true;
    }

    /** Overrides the buildAttributeType method to check for SDO_GEOMETRY columns.
     * 
     *  TODO: Determine the specific type of the geometry.
     * @see org.geotools.data.jdbc.JDBCDataStore#buildAttributeType(java.sql.ResultSet)
     */
    protected AttributeType buildAttributeType(ResultSet rs) throws IOException {
        final int COLUMN_NAME = 4;
        final int DATA_TYPE = 5;
        final int TYPE_NAME = 6;
        
        try {
			if (rs.getString(TYPE_NAME).equals("SDO_GEOMETRY")) {
			    String columnName = rs.getString(COLUMN_NAME);
			    return AttributeTypeFactory.newAttributeType(columnName, Geometry.class);
			} else  {
			    return super.buildAttributeType(rs);
			}
		} catch (SQLException e) {
			throw new DataSourceException("Sql error occurred", e);
		}
    }
   
    
    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#determineSRID(java.lang.String, java.lang.String)
     */
    protected int determineSRID(String tableName, String geometryColumnName) throws IOException {
        OracleConnection conn = null;        
        try {        
            conn = (OracleConnection) getConnection(Transaction.AUTO_COMMIT);
            //GeometryMetaData gMetaData = OraSpatialManager.getGeometryMetaData(conn, tableName, geometryColumnName);
            //return gMetaData.getSpatialReferenceID();
            return -1;            
        }
        finally {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);            
        }        
    }
    
    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getSqlBuilder(java.lang.String)
     */
    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
    	FeatureTypeInfo info = typeHandler.getFeatureTypeInfo(typeName);
        SQLEncoder encoder = new SQLEncoderOracle(info.getSRIDs());
        return new DefaultSQLBuilder(encoder);
    }

	/**
	 * @see org.geotools.data.jdbc.JDBCDataStore#getGeometryAttributeIO(org.geotools.feature.AttributeType, org.geotools.data.jdbc.QueryData)
	 */
	protected AttributeIO getGeometryAttributeIO(AttributeType type, QueryData queryData) throws IOException {
		return new SDOAttributeIO(type, queryData);
	}

}
