/*
 * Created on 16/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.oracle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.FeatureTypeInfo;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCFeatureWriter;
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
     private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");

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
        //Ok, this needs more investigation, since the config constructor being
        //used seems to ignoe the fid map stuff.  I don't quite understand it,
        //and I think it may get picked up later, or at least auto-generated
        //later - maybe this is for the user specified stuff that never got
        //implemented.  Point being this needs to be looked into, I'm just 
        //setting it like this to get things working. -ch
        this(connectionPool, new JDBCDataStoreConfig(namespace, schemaName, null, fidGeneration));

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
	LOGGER.finer("checking table name: " + tablename);
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
        } else if (tablename.startsWith("AQ$"))  {
            return false;
	} else if (tablename.startsWith("APPLY$"))  {
            return false;
	} else if (tablename.startsWith("REPCAT$"))  {
            return false;
        } else if (tablename.startsWith("CWM$"))  {
            return false;
        } else if (tablename.startsWith("CWM2$"))  {
            return false;
        } else if (tablename.startsWith("EXF$"))  {
            return false;
        } else if (tablename.startsWith("DM$"))  {
            return false;
        } 
        LOGGER.finer("returning true for tablename: " + tablename);
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
        Connection conn = null;        
        //try {        
	    //  conn = (OracleConnection) getConnection(Transaction.AUTO_COMMIT);
           //GeometryMetaData gMetaData = OraSpatialManager.getGeometryMetaData(conn, tableName, geometryColumnName);
         //return gMetaData.getSpatialReferenceID();
	//   return -1;            
       //}
	 try {
            String sqlStatement = "SELECT SRID FROM ALL_SDO_GEOM_METADATA "
                + "WHERE TABLE_NAME='" + tableName + "' AND COLUMN_NAME='"
                + geometryColumnName + "'";
            conn = getConnection(Transaction.AUTO_COMMIT);
            LOGGER.finer("the sql statement for srid is " + sqlStatement);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                int retSrid = result.getInt("srid");
                JDBCUtils.close(statement);

                return retSrid;
            } else {
                String mesg = "No geometry column row for srid in table: "
                    + tableName + ", geometry column " + geometryColumnName +
                    ", be sure column is defined in USER_SDO_GEOM_METADATA";
                throw new DataSourceException(mesg);
            }
        } catch (SQLException sqle) {
            String message = sqle.getMessage();

            throw new DataSourceException(message, sqle);
        } finally {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);            
        }        
    }
    
    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getSqlBuilder(java.lang.String)
     */
    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
    	FeatureTypeInfo info = typeHandler.getFeatureTypeInfo(typeName);
        SQLEncoder encoder = new SQLEncoderOracle(info.getSRIDs());
        encoder.setFIDMapper(getFIDMapper(typeName));
        return new DefaultSQLBuilder(encoder);
    }
    
    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getGeometryAttributeIO(org.geotools.feature.AttributeType, org.geotools.data.jdbc.QueryData)
     */
    protected AttributeIO getGeometryAttributeIO(AttributeType type, QueryData queryData) throws IOException {
	return new SDOAttributeIO(type, queryData);
    }
    
    /**
     * Returns a Oracle text based feature writer that just issues the sql
     * statements directly, as text.  Jody and Sean say things will go faster 
     * if we use updatable resultsets and all that jazz, but I can't get
     * those to work, and this does, so I'm going forth with it.
     *
     * @task TODO: Comment out this method and try out the default JDBC
     *             FeatureWriter - Jody thinks it will go faster.  It will
     *             need to be debugged, however, as it would not work.
     */ 
    protected JDBCFeatureWriter createFeatureWriter(FeatureReader fReader,
        QueryData queryData) throws IOException {
        return new OracleFeatureWriter(fReader, queryData);
    }

}
