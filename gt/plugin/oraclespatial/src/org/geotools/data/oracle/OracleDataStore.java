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

import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;

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
import org.geotools.geometry.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
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
		} else if (tablename.startsWith("BIN$"))  { // Added to ignore some Oracle 10g tables
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

    /**
     * Overrides the buildAttributeType method to check for SDO_GEOMETRY columns.
     * @see http://download-west.oracle.com/docs/cd/B14117_01/appdev.101/b10826.pdf
     * 
     *  TODO: Determine the specific type of the geometry.
     */
    protected AttributeType buildAttributeType(ResultSet rs) throws IOException {
    	final int TABLE_NAME = 3;
        final int COLUMN_NAME = 4;
        final int TYPE_NAME = 6;
        final int IS_NULLABLE = 18; // "NO", "YES" or ""
        try {
			if (rs.getString(TYPE_NAME).equals("SDO_GEOMETRY")) {
			    String tableName = rs.getString(TABLE_NAME);
                String columnName = rs.getString(COLUMN_NAME);
                String isNullable = rs.getString( IS_NULLABLE );
                return getSDOGeometryAttribute(tableName, columnName, "YES".equals(isNullable) );
			} else  {
			    return super.buildAttributeType(rs);
			}
		} catch (SQLException e) {
			throw new DataSourceException("Sql error occurred", e);
		}
    }
   
    /**
     * Construct and SDO_GEOMETRY attribute.
     * 
     * @see org.geotools.data.jdbc.JDBCDataStore#buildAttributeType(java.sql.ResultSet) 
     * @param tableName
     * @param columnName
     * @param isNillable 
     * @return
     */
    private AttributeType getSDOGeometryAttribute(String tableName, String columnName, boolean isNullable ) {
	    // HACK! Assume SRID matches EPSG number? No but it will do something for now ...
	    // research required (p89) b10826.pdf above
    	
    	int srid = 0; // aka NULL
		try {
			srid = determineSRID( tableName, columnName );
			CoordinateReferenceSystem crs = determineCRS( srid );
			if( crs != null ){
				return AttributeTypeFactory.newAttributeType(columnName, Geometry.class,isNullable, 0, null, crs );
			}
		} catch (IOException e) {
			LOGGER.warning( "Could not map SRID "+srid+" to CRS:"+e );
		}
    	return AttributeTypeFactory.newAttributeType(columnName, Geometry.class,isNullable);		
	}
    protected CoordinateReferenceSystem determineCRS(int srid ) throws IOException {
    	Connection conn = getConnection(Transaction.AUTO_COMMIT);;
    	String wkt=null;
    	try {
    		Statement st = conn.createStatement();
    		st.execute("select wktext from cs_srs where srid = "+srid );
    		
    		ResultSet set = st.getResultSet();
    		if( !set.next() ) return null;
    		wkt = set.getString(1);    	
    		return CRS.parseWKT( wkt );
    	}
    	catch( FactoryException parse){
    		throw (IOException) new IOException( "Unabled to parse WKTEXT into a CRS:"+wkt ).initCause( parse );
    	}    	
    	catch( SQLException sql ){
    		throw (IOException) new IOException( "No CRS for srid "+srid ).initCause( sql );
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
    
    /**
     * Retrieve approx bounds of all Features.
     * <p>
     * This result is suitable for a quick map display, illustrating the data.
     * This value is often stored as metadata in databases such as oraclespatial.
     * </p>
     * @return null as a generic implementation is not provided.
     */
    public Envelope getEnvelope( String typeName ){
    	Connection conn = null;
    	try {
    		conn= getConnection( Transaction.AUTO_COMMIT );
	    	Statement st = conn.createStatement();
	    	st.execute("SELECT srid,diminfo FROM USER_SDO_GEOM_METADATA where TABLE_NAME = 'ORA_TEST_LINES'");    	
	    	ResultSet set = st.getResultSet();    	
	    	set.next();
	    	
	    	int srid = set.getInt( 1 );
	    	CoordinateReferenceSystem crs = determineCRS( srid );
	    	ARRAY array= (ARRAY) set.getObject(2);    	
	    	Datum data[] = array.getOracleArray();
	    	
	    	double minx = Double.NaN;
	    	double miny = Double.NaN;
	    	double maxx = Double.NaN;
	    	double maxy = Double.NaN;
	    	
	    	for( int i =0; i<data.length; i++){
	    		Datum datum = data[i]; 
	    		System.out.println( datum.getClass() );
	    		STRUCT diminfo = (STRUCT) datum;
	    		Datum info[] = diminfo.getOracleAttributes();
	    		String ord = info[0].stringValue();
	    		double min = info[1].doubleValue();
	    		double max = info[2].doubleValue();
  	    	    // TODO use this for accurate JTS PercisionModel!	    		
	    		// double precision = info[3].doubleValue();
	    		if( "X".equalsIgnoreCase( ord )){
	    			minx = min; maxx= max;
	    		}
	    		if( "Y".equalsIgnoreCase( ord )){
	    			miny = min; maxy= max;
	    		}    		
	    	}
	    	Envelope extent = new Envelope(minx,maxx, miny,maxy );
	    	JTS.ReferencedEnvelope ref = new JTS.ReferencedEnvelope( extent, crs );
	    	return ref;
	    }
    	catch( Exception erp ){
    		LOGGER.warning( erp.toString() );
    		return null;
    	}
	    finally{
	    	if( conn != null ){	    		
	    		try {
					conn.close();
				} catch (SQLException e) {
				}
	    	}
	    }
    }
}