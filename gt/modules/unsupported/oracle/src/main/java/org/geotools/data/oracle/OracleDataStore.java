/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    Created on 16/10/2003
 */
package org.geotools.data.oracle;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.oracle.FeatureTypeHandler;
import org.geotools.data.jdbc.JDBC1DataStore;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCFeatureWriter;
import org.geotools.data.jdbc.JDBCTransactionState;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.attributeio.BasicAttributeIO;
import org.geotools.data.jdbc.fidmapper.DefaultFIDMapperFactory;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.jdbc.fidmapper.FIDMapperFactory;
import org.geotools.data.oracle.attributeio.SDOAttributeIO;
import org.geotools.data.oracle.io.JDBCFeatureReader;
import org.geotools.data.view.DefaultView;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.SchemaException;
import org.opengis.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderOracle;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 * @source $URL$
 */
public class OracleDataStore implements DataStore {
     private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");
     
     protected ConnectionPool connectionPool;    
        
     /**
     * Maps SQL types to Java classes. This might need to be fleshed out more
     * later, Ive ignored complex types such as ARRAY, BLOB and CLOB. It is
     * protected so subclasses can override it I guess.
     *
     * <p>
     * These mappings were taken from
     * http://java.sun.com/j2se/1.3/docs/guide/jdbc/getstart/mapping.html#997737
     * </p>
     */
    protected static final Map TYPE_MAPPINGS = new HashMap();

    static {
        TYPE_MAPPINGS.put(new Integer(Types.VARCHAR), String.class);
        TYPE_MAPPINGS.put(new Integer(Types.CHAR), String.class);
        TYPE_MAPPINGS.put(new Integer(Types.LONGVARCHAR), String.class);

        TYPE_MAPPINGS.put(new Integer(Types.BIT), Boolean.class);
        TYPE_MAPPINGS.put(new Integer(Types.BOOLEAN), Boolean.class);

        TYPE_MAPPINGS.put(new Integer(Types.TINYINT), Short.class);
        TYPE_MAPPINGS.put(new Integer(Types.SMALLINT), Short.class);

        TYPE_MAPPINGS.put(new Integer(Types.INTEGER), Integer.class);
        TYPE_MAPPINGS.put(new Integer(Types.BIGINT), Long.class);

        TYPE_MAPPINGS.put(new Integer(Types.REAL), Float.class);
        TYPE_MAPPINGS.put(new Integer(Types.FLOAT), Double.class);
        TYPE_MAPPINGS.put(new Integer(Types.DOUBLE), Double.class);

        TYPE_MAPPINGS.put(new Integer(Types.DECIMAL), BigDecimal.class);
        TYPE_MAPPINGS.put(new Integer(Types.NUMERIC), BigDecimal.class);

        TYPE_MAPPINGS.put(new Integer(Types.DATE), java.sql.Date.class);
        TYPE_MAPPINGS.put(new Integer(Types.TIME), java.sql.Time.class);
        TYPE_MAPPINGS.put(new Integer(Types.TIMESTAMP),
                java.sql.Timestamp.class);
    }
    
    private BasicAttributeIO basicAttributeIO;

    /** Manages listener lists for FeatureSource implementations */
    public FeatureListenerManager listenerManager = new FeatureListenerManager();

    private LockingManager lockingManager = createLockingManager();

    protected final JDBCDataStoreConfig config;

    protected FeatureTypeHandler typeHandler = null;

    /**
     * The character(s) to surround schema, table and column names an SQL query
     * to support mixed-case and non-English names
     */
    protected String sqlNameEscape = "";

    /**
     * When true, writes are allowed also on tables with volatile FID mappers.
     * False by default
     *
     * @see FIDMapper#isVolatile()
     */
    protected boolean allowWriteOnVolatileFIDs;

    /**
     * The transaction isolation level to use in a transaction.  One of
     * Connection.TRANSACTION_READ_UNCOMMITTED, TRANSACTION_READ_COMMITTED,
     * TRANSACTION_REPEATABLE_READ, or SERIALIZABLE.
     * 
     * Connection.TRANSACTION_NONE may also be used to indicate "use default".
     */
    protected int transactionIsolation = Connection.TRANSACTION_NONE;

    /**
     * Create a connection for your JDBC1 database
     */
    protected Connection createConnection() throws SQLException {
        return connectionPool.getConnection();
    }
    
    /**
     * Construct a JDBCDataStore with ConnectionPool and associated
     * configuration.
     * 
     * @param config
     *
     * @throws IOException
     */
    public OracleDataStore(JDBCDataStoreConfig config) throws IOException {
        this.config = config;
        this.typeHandler = getFeatureTypeHandler(config);
    }
    /**
     * @param connectionPool
     * @param config
     * @throws IOException
     */
    public OracleDataStore(ConnectionPool connectionPool, JDBCDataStoreConfig config) throws IOException {
        this( config );
        this.connectionPool = connectionPool; 
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
     * 
     * @see <a href="http://download-west.oracle.com/docs/cd/B14117_01/appdev.101/b10826.pdf">A doc from Oracle.</a>
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
			    return basicAttributeType(rs);
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
    	} finally {
             JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
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
        return new DefaultSQLBuilder(encoder, info.getSchema(), null);
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
    protected FeatureWriter createFeatureWriter(FeatureReader fReader,
        QueryData queryData) throws IOException {
        LOGGER.fine("returning jdbc feature writer");
        
        OracleFeatureWriter featureWriter = new OracleFeatureWriter(fReader, queryData);
        featureWriter.setFeatureListenerManager(this.listenerManager);
        return featureWriter;
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
	    	st.execute("SELECT srid,diminfo FROM USER_SDO_GEOM_METADATA where TABLE_NAME = '"+typeName+"'");    	
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
	    	ReferencedEnvelope ref = new ReferencedEnvelope( extent, crs );
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
    
    /** This is used by helper classes to hammer sql back to the database */
    public boolean sql( Transaction t, String sql ) throws IOException, SQLException  {
    	Connection conn = getConnection( t );
    	Statement st = conn.createStatement();
    	LOGGER.info( sql );
    	return st.execute( sql );    	
    }
    
    public void createSchema(FeatureType featureType) throws IOException {
    	String tableName = featureType.getTypeName();
    	Transaction t = new DefaultTransaction("createSchema");
    	
    	CoordinateReferenceSystem crs = featureType.getDefaultGeometry().getCoordinateSystem();
    	// TODO: lookup srid for crs
    	Envelope bounds = new Envelope();
		bounds.expandToInclude( -180, -90 );
		bounds.expandToInclude( 180, 90 );
		int srid = -1;
		
    	SQLEncoderOracle encoder = new SQLEncoderOracle("fid",srid); // should figure out from encoding
    	SqlStatementEncoder statements = new SqlStatementEncoder( encoder, tableName, "fid" );
    	
    	try {
			sql( t, "DROP TABLE "+tableName );
			sql( t, "DELETE FROM user_sdo_geom_metadata WHERE TABLE_NAME='"+tableName+"'");			
    	}
    	catch( SQLException ignore ){
    		// table probably did not exist
    	}
    	try{
    		sql( t, statements.makeCreateTableSQL( featureType ) );
    		
  
    		sql( t, statements.makeAddGeomMetadata( featureType, bounds, srid ) );
    		//sql( t, statements.makeCreateFidIndex() );
    		//sql( t, statements.makeCreateGeomIndex( featureType ) );
    		
    		t.commit();
    		
    	} catch (SQLException e) {
    		t.rollback();
    		throw (IOException) new IOException( e.getLocalizedMessage() ).initCause( e );
		}    	    	
    	finally {
    		t.close();
    	}
    }
    
    /**
     * Gets the SQL name escape string.
     *
     * <p>
     * The value of this string is prefixed and appended to table schema names,
     * table names and column names in an SQL statement to support mixed-case
     * and non-English names.
     * </p>
     *
     * @return the value of the SQL name escape string.
     */
    public String getSqlNameEscape() {
        return sqlNameEscape;
    }

    /**
     * Sets the SQL name escape string.
     *
     * <p>
     * The value of this string is prefixed and appended to table schema names,
     * table names and column names in an SQL statement to support mixed-case
     * and non-English names.
     * </p>
     *
     * <p>
     * This value is typically only set once when the DataStore implementation
     * class is constructed.
     * </p>
     *
     * @param sqlNameEscape
     *            the name escape character
     */
    protected void setSqlNameEscape(String sqlNameEscape) {
        this.sqlNameEscape = sqlNameEscape;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @deprecated This is deprecated in favour of the JDBCDataStoreConfig
     *             object. public JDBCDataStore(ConnectionPool connectionPool)
     *             throws IOException { this(connectionPool, null, new
     *             HashMap(), ""); }
     */

    /**
     * DOCUMENT ME!
     *
     * @param config
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException
     *             DOCUMENT ME!
     */
    protected FeatureTypeHandler getFeatureTypeHandler(
            JDBCDataStoreConfig config) throws IOException {
        return new FeatureTypeHandler(this, buildFIDMapperFactory(config),
                config.getTypeHandlerTimeout());
    }

    protected FIDMapperFactory buildFIDMapperFactory(JDBCDataStoreConfig config) {
        return new DefaultFIDMapperFactory();
    }

    public FIDMapper getFIDMapper(String tableName) throws IOException {
        return typeHandler.getFIDMapper(tableName);
    }

    /**
     * Allows subclass to create LockingManager to support their needs.
     *
     */
    protected LockingManager createLockingManager() {
        return new InProcessLockingManager();
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureTypes()
     */
    public String[] getTypeNames() throws IOException {
        return typeHandler.getTypeNames();
    }

    /**
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String typeName) throws IOException {
        return typeHandler.getSchema(typeName);
    }

    /**
     * Used to provide support for changing the DataStore Schema.
     *
     * <p>
     * Specifically this is intended to address updating the metadata Coordinate
     * System information.
     * </p>
     *
     * <p>
     * If we can figure out the Catalog API for metadata we will not have to use
     * such a heavy handed approach.
     * </p>
     *
     * <p>
     * Subclasses are free to implement various levels of support:
     * </p>
     *
     * <ul>
     * <li> None - table modification is not supported </li>
     * <li> CS change - ensure that the attribtue types match and only update
     * metadata but not table structure. </li>
     * <li> Allow table change opperations </li>
     * </ul>
     *
     *
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.geotools.feature.FeatureType)
     */
    public void updateSchema(String typeName, FeatureType featureType)
            throws IOException {
        throw new UnsupportedOperationException(
                "Table modification not supported");
    }

    // This is the *better* implementation of getview from AbstractDataStore
    public FeatureSource getView(final Query query) throws IOException,
            SchemaException {
        return new DefaultView(this.getFeatureSource(query.getTypeName()),
                query);
    }

    /*
     * // Jody - This is my recomendation for DataStore // in order to support
     * CS reprojection and override public FeatureSource getView(final Query
     * query) throws IOException, SchemaException { String typeName =
     * query.getTypeName(); FeatureType origionalType = getSchema(typeName);
     * //CoordinateSystem cs = query.getCoordinateSystem(); //final FeatureType
     * featureType = DataUtilities.createSubType( origionalType,
     * query.getPropertyNames(), cs ); final FeatureType featureType =
     * DataUtilities.createSubType(origionalType, query.getPropertyNames());
     * return new AbstractFeatureSource() { public DataStore getDataStore() {
     * return JDBCDataStore.this; } public void
     * addFeatureListener(FeatureListener listener) {
     * listenerManager.addFeatureListener(this, listener); } public void
     * removeFeatureListener(FeatureListener listener) {
     * listenerManager.removeFeatureListener(this, listener); } public
     * FeatureType getSchema() { return featureType; } }; }
     */

    /**
     * Default implementation based on getFeatureReader and getFeatureWriter.
     *
     * <p>
     * We should be able to optimize this to only get the RowSet once
     * </p>
     *
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource(String typeName) throws IOException {
        if (typeHandler.getFIDMapper(typeName).isVolatile()
                || allowWriteOnVolatileFIDs) {
            if (getLockingManager() != null) {
                // Use default JDBCFeatureLocking that delegates all locking
                // the getLockingManager
                //
                return new JDBCFeatureLocking(this, getSchema(typeName));
            } else {
                // subclass should provide a FeatureLocking implementation
                // but for now we will simply forgo all locking
                return new JDBCFeatureStore(this, getSchema(typeName));
            }
        } else {
            return new JDBCFeatureSource(this, getSchema(typeName));
        }
    }

    /**
     * This is a public entry point to the DataStore.
     *
     * <p>
     * We have given some though to changing this api to be based on query.
     * </p>
     *
     * <p>
     * Currently the is is the only way to retype your features to different
     * name spaces.
     * </p>
     * (non-Javadoc)
     *
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.feature.FeatureType,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureReader getFeatureReader(final FeatureType requestType,
            final Filter filter, final Transaction transaction)
            throws IOException {
        String typeName = requestType.getTypeName();
        FeatureType schemaType = getSchema(typeName);

        int compare = DataUtilities.compare(requestType, schemaType);

        Query query;

        if (compare == 0) {
            // they are the same type
            //
            query = new DefaultQuery(typeName, filter);
        } else if (compare == 1) {
            // featureType is a proper subset and will require reTyping
            //
            String[] names = attributeNames(requestType, filter);
            query = new DefaultQuery(typeName, filter, Query.DEFAULT_MAX,
                    names, "getFeatureReader");
        } else {
            // featureType is not compatiable
            //
            throw new IOException("Type " + typeName + " does match request");
        }

        if ((filter == Filter.EXCLUDE) || filter.equals(Filter.EXCLUDE)) {
            return new EmptyFeatureReader(requestType);
        }

        FeatureReader reader = getFeatureReader(query, transaction);

        if (compare == 1) {
            reader = new ReTypeFeatureReader(reader, requestType);
        }

        return reader;
    }

    /**
     * Gets the list of attribute names required for both featureType and filter
     *
     * @param featureType
     *            The FeatureType to get attribute names for.
     * @param filter
     *            The filter which needs attributes to filter.
     *
     * @return The list of attribute names required by a filter.
     *
     * @throws IOException
     *             If we can't get the schema.
     */
    protected String[] attributeNames(FeatureType featureType, Filter filter)
            throws IOException {
        String typeName = featureType.getTypeName();
        FeatureType origional = getSchema(typeName);
        SQLBuilder sqlBuilder = getSqlBuilder(typeName);

        if (featureType.getAttributeCount() == origional.getAttributeCount()) {
            // featureType is complete (so filter must require subset
            return DataUtilities.attributeNames(featureType);
        }

        String[] typeAttributes = DataUtilities.attributeNames(featureType);
        String[] filterAttributes = DataUtilities.attributeNames(sqlBuilder
                .getPostQueryFilter(filter));

        if ((filterAttributes == null) || (filterAttributes.length == 0)) {
            // no filter attributes required
            return typeAttributes;
        }

        Set set = new HashSet();
        set.addAll(Arrays.asList(typeAttributes));
        set.addAll(Arrays.asList(filterAttributes));

        if (set.size() == typeAttributes.length) {
            // filter required a subset of featureType attributes
            return typeAttributes;
        } else {
            return (String[]) set.toArray(new String[set.size()]);
        }
    }

    /**
     * The top level method for getting a FeatureReader.
     *
     * <p>
     * Chris- I've gone with the Query object aswell. It just seems to make more
     * sense. This is pretty well split up across methods. The hooks for DB
     * specific AttributeReaders are createResultSetReader and
     * createGeometryReader.
     * </p>
     *
     * <p>
     * JG- I have implemented getFeatureReader( FeatureType, Filter,
     * Transasction) ontop of this method, it will Retype as required
     * </p>
     *
     * @param query
     *            The Query to get a FeatureReader for.
     * @param trans
     *            The transaction this read operation is being performed in.
     *
     * @return A FeatureReader that contains features defined by the query.
     *
     * @throws IOException
     *             If an error occurs executing the query.
     * @throws DataSourceException
     */
    public FeatureReader getFeatureReader(Query query, Transaction trans)
            throws IOException {
        String typeName = query.getTypeName();
        FeatureType featureType = getSchema(typeName);
        FeatureTypeInfo typeInfo = typeHandler.getFeatureTypeInfo(typeName);

        SQLBuilder sqlBuilder = getSqlBuilder(typeName);
        
        Filter preFilter = sqlBuilder.getPreQueryFilter(query.getFilter()); //process in DB
        Filter postFilter = sqlBuilder.getPostQueryFilter(query.getFilter()); //process after DB
        
        //JD: This is bad, we should not assume we have the right to change the query object
        Filter originalFilter = query.getFilter();
        ((DefaultQuery) query).setFilter(preFilter);
        
        String[] requestedNames = propertyNames(query);
        String[] propertyNames;

        // DJB: changed to account for miss-ordered queries
        if (allSameOrder(requestedNames, featureType)) {
            // because we have everything, the filter can run
            propertyNames = requestedNames;
        } else if (requestedNames.length <= featureType.getAttributeCount()) {
            // we will need to reType this :-)
            //
            // check to make sure we have enough for the filter
            //
            String[] filterNames = DataUtilities.attributeNames(preFilter);

            Set set = new HashSet();
            set.addAll(Arrays.asList(requestedNames));
            set.addAll(Arrays.asList(filterNames));

            if (set.size() == requestedNames.length) {
                propertyNames = requestedNames;
            } else {
                propertyNames = (String[]) set.toArray(new String[set.size()]);
            }

            try {
                typeInfo = new FeatureTypeInfo(typeInfo.getFeatureTypeName(),
                        DataUtilities.createSubType(typeInfo.getSchema(),
                                requestedNames), typeInfo.getFIDMapper());
            } catch (SchemaException e1) {
                throw new DataSourceException("Could not create subtype", e1);
            }
        } else { // too many requested (duplicates?)
            throw new DataSourceException(typeName
                    + " does not contain requested properties:" + query);
        }

        AttributeType[] attrTypes = null;

        try {
            attrTypes = getAttributeTypes(typeName, propertyNames);
        } catch (SchemaException schemaException) {
            throw new DataSourceException(
                    "Some Attribute Names were specified that"
                            + " do not exist in the FeatureType " + typeName
                            + ". " + "Requested names: "
                            + Arrays.asList(propertyNames) + ", "
                            + "FeatureType: " + featureType, schemaException);
        }

        String sqlQuery = constructQuery(query, attrTypes);
        
        //JD: This is bad, we should not assume we have the right to change the query object
        ((DefaultQuery) query).setFilter(originalFilter);
        
        QueryData queryData = executeQuery(typeInfo, typeName, sqlQuery, trans,
                false);

        FeatureType schema;

        try {
            schema = FeatureTypeBuilder.newFeatureType(attrTypes, typeName,
                    getNameSpace());
        } catch (FactoryRegistryException e) {
            throw new DataSourceException(
                    "Schema Factory Error when creating schema for FeatureReader",
                    e);
        } catch (SchemaException e) {
            throw new DataSourceException(
                    "Schema Error when creating schema for FeatureReader", e);
        }

        FeatureReader reader;
        reader = createFeatureReader(schema, postFilter, queryData);

        if (requestedNames.length < propertyNames.length) {
            // need to scale back to what the user asked for
            // (remove the attribtues only used for postFilter)
            //
            try {
                FeatureType requestType = DataUtilities.createSubType(schema,
                        requestedNames);
                reader = new ReTypeFeatureReader(reader, requestType);
            } catch (SchemaException schemaException) {
                throw new DataSourceException("Could not handle query",
                        schemaException);
            }
        }

        // chorner: this is redundant, since we've already created the reader with the post filter attached     
        // if (postFilter != null && !postFilter.equals(Filter.INCLUDE)) {
        //     reader = new FilteringFeatureReader(reader, postFilter);
        // }
        
        return reader;
    }

    /**
     * Used internally to call the subclass hooks that construct the SQL query.
     *
     * @param query
     * @param attrTypes
     *
     *
     * @throws IOException
     * @throws DataSourceException
     */
    private String constructQuery(Query query, AttributeType[] attrTypes)
            throws IOException, DataSourceException {
        String typeName = query.getTypeName();
        SQLBuilder sqlBuilder = getSqlBuilder(query.getTypeName());
        Filter preFilter = sqlBuilder.getPreQueryFilter(query.getFilter()); //dupe?
        //Filter postFilter = sqlBuilder.getPostQueryFilter(query.getFilter());

        FIDMapper mapper = getFIDMapper(typeName);

        String sqlQuery;
        //FeatureTypeInfo info = typeHandler.getFeatureTypeInfo(typeName);
        //boolean useMax = (postFilter == null); // not used yet

        try {
            LOGGER.fine("calling sql builder with filter " + preFilter);

            if (query.getFilter() == Filter.EXCLUDE) {
                StringBuffer buf = new StringBuffer("SELECT ");
                sqlBuilder.sqlColumns(buf, mapper, attrTypes);
                sqlBuilder.sqlFrom(buf, typeName);
                buf.append(" WHERE '1' = '0'"); // NO-OP it
                sqlQuery = buf.toString();
            } else {
                sqlQuery = sqlBuilder.buildSQLQuery(typeName, mapper,
                        attrTypes, preFilter);
            }

            //order by clause
            if ( query.getSortBy() != null ) {
                //encode the sortBy clause
                StringBuffer buf = new StringBuffer();
                buf.append( sqlQuery );
                sqlBuilder.sqlOrderBy( buf, query.getSortBy() );
                
                sqlQuery = buf.toString();
            }
            
            LOGGER.fine("sql is " + sqlQuery);
        } catch (SQLEncoderException e) {
            throw new DataSourceException("Error building SQL Query", e);
        }

        return sqlQuery;
    }

    /**
     * Create a new FeatureReader based on attributeReaders.
     *
     * <p>
     * The provided <code>schema</code> describes the attributes in the
     * queryData ResultSet. This schema should cover the requirements of
     * <code>filter</code>.
     * </p>
     *
     * <p>
     * Retyping to the users requested Schema will not happen in this method.
     * </p>
     *
     * @param schema
     * @param postFilter
     *            Filter for post processing, or <code>null</code> if not
     *            required.
     * @param queryData
     *            Holds a ResultSet for attribute Readers
     *
     *
     * @throws IOException
     */
    protected FeatureReader createFeatureReader(FeatureType schema,
            Filter postFilter, QueryData queryData) throws IOException {
        FeatureReader fReader = getJDBCFeatureReader(queryData);

        if ((postFilter != null) && (postFilter != Filter.INCLUDE)) {
            fReader = new FilteringFeatureReader(fReader, postFilter);
        }

        if (postFilter == Filter.EXCLUDE) {
            return new EmptyFeatureReader(schema);
        }
        
        return fReader;
    }

    protected JDBCFeatureReader getJDBCFeatureReader(QueryData queryData)
            throws IOException {
        return new JDBCFeatureReader(queryData);
    }

    // protected final AttributeReader createAttributeReader(AttributeType[]
    // attrTypes, int fidColumnsCount, ResultSet rs) {
    // AttributeIO[] attributeIO = new AttributeIO[attrTypes.length];
    // for(int i = 0; i < attributeIO.length; i++) {
    // if(attrTypes[i].isGeometry()) {
    // attributeIO[i] = getGeometryAttributeIO(attrTypes[i]);
    // } else {
    // attributeIO[i] = getAttributeIO(attrTypes[i]);
    // }
    //
    // }
    // return new JDBCFeatureReader(attrTypes, attributeIO, fidColumnsCount,
    // rs);
    // }

    /**
     * Returns the basic AttributeIO that can read and write all of the simple
     * data types
     *
     * @param type
     *
     */
    protected AttributeIO getAttributeIO(AttributeType type) {
        if (basicAttributeIO == null) {
            basicAttributeIO = new BasicAttributeIO();
        }

        return basicAttributeIO;
    }

    /**
     * Executes the SQL Query.
     *
     * <p>
     * This is private in the expectation that subclasses should not need to
     * change this behaviour.
     * </p>
     *
     * <p>
     * Jody with a question here - I have stopped this method from closing
     * connection shared by a Transaction. It sill seems like we are leaving
     * connections open by using this method. I have also stopped QueryData from
     * doing the same thing.
     * </p>
     *
     * <p>
     * Answer from Sean: Resources for successful queries are closed when close
     * is called on the AttributeReaders constructed with the QueryData. We
     * can't close them here since they need to be open to read from the
     * ResultSet.
     * </p>
     *
     * <p>
     * Jody AttributeReader question: I looked at the code and Attribute Readers
     * do not close with respect to Transactions (they need to as we can issue a
     * Reader against a Transaction. I have changed the JDBCDataStore.close
     * method to force us to keep track of these things.
     * </p>
     *
     * <p>
     * SG: I've marked this as final since I don't think it shoudl be overriden,
     * but Im not sure
     * </p>
     *
     * @param featureTypeInfo
     * @param tableName
     * @param sqlQuery
     *            The SQL query to execute.
     * @param transaction
     *            The Transaction is included here for handling transaction
     *            connections at a later stage. It is not currently used.
     * @param forWrite
     *
     * @return The QueryData object that contains the resources for the query.
     *
     * @throws IOException
     * @throws DataSourceException
     *             If an error occurs performing the query.
     *
     * @task HACK: This is just protected for postgis FeatureWriter purposes.
     *       Should move back to private when that stuff moves more abstract
     *       here.
     */
    protected QueryData executeQuery(FeatureTypeInfo featureTypeInfo,
            String tableName, String sqlQuery, Transaction transaction,
            boolean forWrite) throws IOException {
        LOGGER.fine("About to execute query: " + sqlQuery);

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = getConnection(transaction);

            setAutoCommit(forWrite, conn);
            statement = conn.createStatement(getResultSetType(forWrite),
                    getConcurrency(forWrite));
            statement.setFetchSize(1000);
            rs = statement.executeQuery(sqlQuery);

            return new QueryData(featureTypeInfo, this, conn, statement, rs,
                    transaction);
        } catch (SQLException e) {
            // if an error occurred we close the resources
            String msg = "Error Performing SQL query: " + sqlQuery;
            LOGGER.log(Level.SEVERE, msg, e);
            JDBCUtils.close(rs);
            JDBCUtils.close(statement);
            JDBCUtils.close(conn, transaction, e);
            throw new DataSourceException(msg, e);
        }
    }

    /**
     * This method should be overridden to do nothing by DataStores where
     * setting autoCommit causes funky behaviour (ie. anytime autoCommit is
     * changed, every thing up to that point is committed...this isn't good at
     * this stage)
     *
     * @param forWrite
     * @param conn
     * @throws SQLException
     */
    protected void setAutoCommit(boolean forWrite, Connection conn)
            throws SQLException {
        if (!forWrite) {
            // for postgis streaming, but I don't believe it hurts anyone.
            conn.setAutoCommit(false);
        }
    }

    protected int getResultSetType(boolean forWrite) {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    protected int getConcurrency(boolean forWrite) {
        if (forWrite) {
            return ResultSet.CONCUR_UPDATABLE;
        } else {
            return ResultSet.CONCUR_READ_ONLY;
        }
    }

    /**
     * Gets a connection for the provided transaction.
     *
     * @param transaction
     * @return A single use connection.
     *
     * @throws IOException
     * @throws DataSourceException
     *             If the connection can not be obtained.
     */
     protected Connection getConnection(Transaction transaction)
            throws IOException {
        if (transaction != Transaction.AUTO_COMMIT) {
            // we will need to save a JDBC connection is
            // transaction.putState( connectionPool, JDBCState )
            // throw new UnsupportedOperationException("Transactions not
            // supported yet");
            
            JDBCTransactionState state;
            synchronized (transaction) {
                
                state = (JDBCTransactionState) transaction
                        .getState(this);
                
                if (state == null) {
                    try {
                        Connection conn = createConnection();
                        conn.setAutoCommit(requireAutoCommit());
                        if (getTransactionIsolation() != Connection.TRANSACTION_NONE) {
                            // for us, NONE means use the default, which is
                            // usually READ_COMMITTED
                            conn.setTransactionIsolation(getTransactionIsolation());
                        }
                        state = new JDBCTransactionState(conn);
                        transaction.putState(this, state);
                    } catch (SQLException eep) {
                        throw new DataSourceException("Connection failed:"
                                + eep, eep);
                    }
                }
            }
            return state.getConnection();
        }

        try {
            return createConnection();
        } catch (SQLException sqle) {
            throw new DataSourceException("Connection failed:" + sqle, sqle);
        }
    }

    /**
     * Obtain the transaction isolation level for connections.
     * 
     * @return Connection.TRANSACTION_* value
     * @since 2.2.0
     * @see setTransactionIsolation
     * @see <a href="http://www.postgresql.org/docs/7.4/static/transaction-iso.html">This web page</a>
     */
    public int getTransactionIsolation() {
        return transactionIsolation;
    }
     
    /**
     * Sets the transaction isolation level for connections.
     * 
     * @param value
     *            Connection.TRANSACTION_READ_UNCOMMITTED,
     *            Connection.TRANSACTION_READ_COMMITTED,
     *            Connection.TRANSACTION_REPEATABLE_READ,
     *            Connection.SERIALIZABLE, or Connection.TRANSACTION_NONE
     *            (for use default/do not set)
     * @since 2.2.0
     * @see <a href="http://www.postgresql.org/docs/7.4/static/transaction-iso.html">This web page</a>
     */
    public void setTransactionIsolation(int value) {
        transactionIsolation = value;
    }
     
    /**
     * Return true if transaction is handled on client.  Usually this will not have to be overridden.
     * @return true if transaction is handled on client.  Usually this will not have to be overridden.
     */
    protected boolean requireAutoCommit() {
        return false;
    }

    /**
     * Builds the appropriate FID mapper given a table name and a FID mapper
     * factory
     *
     * @param typeName
     * @param factory
     *
     *
     * @throws IOException
     */
    protected FIDMapper buildFIDMapper(String typeName, FIDMapperFactory factory)
            throws IOException {
        Connection conn = null;

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);

            FIDMapper mapper = factory.getMapper(null, config.getDatabaseSchemaName(), typeName, conn);

            return mapper;
        } finally {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
        }
    }

    /**
     * Builds the schema for a table in the database.
     *
     * <p>
     * This works by retrieving the column information for the table from the
     * DatabaseMetaData object. It then iterates over the information for each
     * column, calling buildAttributeType(ResultSet) to construct an
     * AttributeType for each column. The list of attribute types is then turned
     * into a FeatureType that defines the schema.
     * </p>
     *
     * <p>
     * It is not intended that this method is overriden. It should provide the
     * required functionality for most sub-classes. To add AttributeType
     * construction for vendor specific SQL types, such as geometries, override
     * the buildAttributeType(ResultSet) method.
     * </p>
     *
     * <p>
     * This may become final later. In fact Ill make it private because I don't
     * think It will need to be overriden.
     * </p>
     *
     * @param typeName
     *            The name of the table to construct a feature type for.
     * @param mapper
     *            The name of the column holding the fid.
     *
     * @return The FeatureType for the table.
     *
     * @throws IOException
     * @throws DataSourceException
     *             This can occur if there is an SQL error or an error
     *             constructing the FeatureType.
     *
     * @see JDBC1DataStore#buildAttributeType(ResultSet)
     */
    protected FeatureType buildSchema(String typeName, FIDMapper mapper)
            throws IOException {
        final int NAME_COLUMN = 4;
        final int TYPE_NAME = 6;
        Connection conn = null;
        ResultSet tableInfo = null;

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);

            DatabaseMetaData dbMetaData = conn.getMetaData();

            List attributeTypes = new ArrayList();

            tableInfo = dbMetaData.getColumns(null, config.getDatabaseSchemaName(), typeName, "%");

            boolean tableInfoFound = false;

            while (tableInfo.next()) {
                tableInfoFound = true;

                try {
                    String columnName = tableInfo.getString(NAME_COLUMN);

                    if (!mapper.returnFIDColumnsAsAttributes()) {
                        boolean isPresent = false;

                        for (int i = 0; i < mapper.getColumnCount(); i++) {
                            if (columnName.equalsIgnoreCase(mapper
                                    .getColumnName(i))) {
                                isPresent = true;

                                break;
                            }
                        }

                        if (isPresent) {
                            continue;
                        }
                    }

                    AttributeType attributeType = buildAttributeType(tableInfo);

                    if (attributeType != null) {
                        attributeTypes.add(attributeType);
                    } else {
                        LOGGER.finest("Unknown SQL Type: "
                                + tableInfo.getString(TYPE_NAME));
                    }
                } catch (DataSourceException dse) {
                    String msg = "Error building attribute type. The column will be ignored";
                    LOGGER.log(Level.WARNING, msg, dse);
                }
            }

            if (!tableInfoFound) {
                throw new SchemaNotFoundException(typeName);
            }

            AttributeType[] types = (AttributeType[]) attributeTypes
                    .toArray(new AttributeType[0]);

            return FeatureTypeBuilder.newFeatureType(types, typeName,
                    getNameSpace());
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, sqlException);
            conn = null; // prevent finally block from reclosing
            throw new DataSourceException("SQL Error building FeatureType for "
                    + typeName + " " + sqlException.getMessage(), sqlException);
        } catch (FactoryRegistryException e) {
            throw new DataSourceException("Error creating FeatureType "
                    + typeName, e);
        } catch (SchemaException e) {
            throw new DataSourceException("Error creating FeatureType for "
                    + typeName, e);
        } finally {
            JDBCUtils.close(tableInfo);
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
        }
    }

    /**
     * Constructs an AttributeType from a row in a ResultSet. The ResultSet
     * contains the information retrieved by a call to getColumns() on the
     * DatabaseMetaData object. This information can be used to construct an
     * Attribute Type.
     *
     * <p>
     * The default implementation constructs an AttributeType using the default
     * JDBC type mappings defined in JDBCDataStore. These type mappings only
     * handle native Java classes and SQL standard column types, so to handle
     * Geometry columns, sub classes should override this to check if a column
     * is a geometry column, if it is a geometry column the appropriate
     * determination of the geometry type can be performed. Otherwise,
     * overriding methods should call super.buildAttributeType.
     * </p>
     *
     * <p>
     * Note: Overriding methods must never move the current row pointer in the
     * result set.
     * </p>
     *
     * @param rs
     *            The ResultSet containing the result of a
     *            DatabaseMetaData.getColumns call.
     *
     * @return The AttributeType built from the ResultSet or null if the column
     *         should be excluded from the schema.
     *
     * @throws IOException
     *             If an error occurs processing the ResultSet.
     */
    protected AttributeType basicAttributeType(ResultSet rs) throws IOException {
        try {
            final int COLUMN_NAME = 4;
            final int DATA_TYPE = 5;

            String columnName = rs.getString(COLUMN_NAME);
            int dataType = rs.getInt(DATA_TYPE);
            Class type = (Class) TYPE_MAPPINGS.get(new Integer(dataType));

            if (type == null) {
                return null;
            } else {
                return AttributeTypeFactory.newAttributeType(columnName, type);
            }
        } catch (SQLException e) {
            throw new IOException("SQL exception occurred: " + e.getMessage());
        }
    }

    /**
     * Provides the default implementation of determining the FID column.
     *
     * <p>
     * The default implementation of determining the FID column name is to use
     * the primary key as the FID column. If no primary key is present, null
     * will be returned. Sub classes can override this behaviour to define
     * primary keys for vendor specific cases.
     * </p>
     *
     * <p>
     * There is an unresolved issue as to what to do when there are multiple
     * primary keys. Maybe a restriction that table much have a single column
     * primary key is appropriate.
     * </p>
     *
     * <p>
     * This should not be called by subclasses to retreive the FID column name.
     * Instead, subclasses should call getFeatureTypeInfo(String) to get the
     * FeatureTypeInfo for a feature type and get the fidColumn name from the
     * fidColumn name memeber.
     * </p>
     *
     * @param typeName
     *            The name of the table to get a primary key for.
     *
     * @return The name of the primay key column or null if one does not exist.
     *
     * @throws IOException
     *             This will only occur if there is an error getting a
     *             connection to the Database.
     */
    protected String determineFidColumnName(String typeName) throws IOException {
        final int NAME_COLUMN = 4;
        String fidColumnName = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);

            DatabaseMetaData dbMetadata = conn.getMetaData();
            rs = dbMetadata.getPrimaryKeys(null, null, typeName);

            if (rs.next()) {
                fidColumnName = rs.getString(NAME_COLUMN);
            }
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, sqlException);
            conn = null; // prevent finally block from reclosing
            LOGGER
                    .warning("Could not find the primary key - using the default");
        } finally {
            JDBCUtils.close(rs);
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
        }

        return fidColumnName;
    }

    /**
     * Gets the namespace of the data store. TODO: change config over to use URI
     *
     * @return The namespace.
     */
    public URI getNameSpace() {
        try {
            if (config.getNamespace() != null) {
                return new URI(config.getNamespace());
            }
        } catch (URISyntaxException e) {
            LOGGER.warning("Could not use namespace " + config.getNamespace()
                    + " - " + e.getMessage());

            return null;
        }

        return null;
    }

    /**
     * Retrieve a FeatureWriter over entire dataset.
     *
     * <p>
     * Quick notes: This FeatureWriter is often used to add new content, or
     * perform summary calculations over the entire dataset.
     * </p>
     *
     * <p>
     * Subclass may wish to implement an optimized featureWriter for these
     * operations.
     * </p>
     *
     * <p>
     * It should provide Feature for next() even when hasNext() is
     * <code>false</code>.
     * </p>
     *
     * <p>
     * Subclasses are responsible for checking with the lockingManger unless
     * they are providing their own locking support.
     * </p>
     *
     * @param typeName
     * @param transaction
     *
     *
     * @throws IOException
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      boolean, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName,
            Transaction transaction) throws IOException {
        return getFeatureWriter(typeName, Filter.INCLUDE, transaction);
    }

    /**
     * Retrieve a FeatureWriter for creating new content.
     *
     * <p>
     * Subclass may wish to implement an optimized featureWriter for this
     * operation. One based on prepaired statemnts is a possibility, as we do
     * not require a ResultSet.
     * </p>
     *
     * <p>
     * To allow new content the FeatureWriter should provide Feature for next()
     * even when hasNext() is <code>false</code>.
     * </p>
     *
     * <p>
     * Subclasses are responsible for checking with the lockingManger unless
     * they are providing their own locking support.
     * </p>
     *
     * @param typeName
     * @param transaction
     *
     *
     * @throws IOException
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      boolean, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriterAppend(String typeName,
            Transaction transaction) throws IOException {
        FeatureWriter writer = getFeatureWriter(typeName, Filter.EXCLUDE,
                transaction);

        while (writer.hasNext()) {
            writer.next(); // this would be a use for skip then :-)
        }

        return writer;
    }

    /**
     * Acquire FeatureWriter for modification of contents specifed by filter.
     *
     * <p>
     * Quick notes: This FeatureWriter is often used to remove contents
     * specified by the provided filter, or perform summary calculations.
     * </p>
     *
     * <p>
     * Subclasses are responsible for checking with the lockingManager unless
     * they are providing their own locking support.
     * </p>
     *
     * @param typeName
     * @param filter
     * @param transaction
     *
     *
     * @throws IOException
     *             If typeName could not be located
     * @throws NullPointerException
     *             If the provided filter is null
     * @throws DataSourceException
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter,
            Transaction transaction) throws IOException {
        if (filter == null) {
            throw new NullPointerException("getFeatureReader requires Filter: "
                    + "did you mean Filter.INCLUDE?");
        }

        if (transaction == null) {
            throw new NullPointerException(
                    "getFeatureReader requires Transaction: "
                            + "did you mean Transaction.AUTO_COMMIT");
        }

        FeatureType featureType = getSchema(typeName);
        FeatureTypeInfo info = typeHandler.getFeatureTypeInfo(typeName);
        LOGGER.fine("getting feature writer for " + typeName + ": " + info);

        SQLBuilder sqlBuilder = getSqlBuilder(typeName);
        Filter preFilter = sqlBuilder.getPreQueryFilter(filter);
        Filter postFilter = sqlBuilder.getPostQueryFilter(filter);
        Query query = new DefaultQuery(typeName, preFilter);
        String sqlQuery;

        try {
            sqlQuery = constructQuery(query, getAttributeTypes(typeName,
                    propertyNames(query)));
        } catch (SchemaException e) {
            throw new DataSourceException(
                    "Some Attribute Names were specified that"
                            + " do not exist in the FeatureType " + typeName
                            + ". " + "Requested names: "
                            + Arrays.asList(query.getPropertyNames()) + ", "
                            + "FeatureType: " + featureType, e);
        }

        QueryData queryData = executeQuery(typeHandler
                .getFeatureTypeInfo(typeName), typeName, sqlQuery, transaction,
                true);
        FeatureReader reader = createFeatureReader(info.getSchema(),
                postFilter, queryData);
        FeatureWriter writer = createFeatureWriter(reader, queryData);

        if ((getLockingManager() != null)
                && getLockingManager() instanceof InProcessLockingManager) {
            InProcessLockingManager inProcess = (InProcessLockingManager) getLockingManager();
            writer = inProcess.checkedWriter(writer, transaction);
        }

        // chorner: writer shouldn't have a wrapped post filter, otherwise one can't add features.
        // if ((postFilter != null) && (postFilter != Filter.INCLUDE)) {
        //     writer = new FilteringFeatureWriter(writer, postFilter);
        // }

        return writer;
    }



    /**
     * Get propertyNames in a safe manner.
     *
     * <p>
     * Method will figure out names from the schema for query.getTypeName(), if
     * query getPropertyNames() is <code>null</code>, or
     * query.retrieveAllProperties is <code>true</code>.
     * </p>
     *
     * @param query
     *
     *
     * @throws IOException
     */
    private String[] propertyNames(Query query) throws IOException {
        String[] names = query.getPropertyNames();

        if ((names == null) || query.retrieveAllProperties()) {
            String typeName = query.getTypeName();
            FeatureType schema = getSchema(typeName);

            names = new String[schema.getAttributeCount()];

            for (int i = 0; i < schema.getAttributeCount(); i++) {
                names[i] = schema.getAttributeType(i).getName();
            }
        }

        return names;
    }

    /**
     * Gets the attribute types from from a given type.
     *
     * @param typeName
     *            The name of the feature type to get the AttributeTypes for.
     * @param propertyNames
     *            The list of propertyNames to get AttributeTypes for.
     *
     * @return the array of attribute types from the schema which match
     *         propertyNames.
     *
     * @throws IOException
     *             If we can't get the schema.
     * @throws SchemaException
     *             if query contains a propertyName that is not a part of this
     *             type's schema.
     */
    protected final AttributeType[] getAttributeTypes(String typeName,
            String[] propertyNames) throws IOException, SchemaException {
        FeatureType schema = getSchema(typeName);
        AttributeType[] types = new AttributeType[propertyNames.length];

        for (int i = 0; i < propertyNames.length; i++) {
            types[i] = schema.getAttributeType(propertyNames[i]);

            if (types[i] == null) {
                throw new SchemaException(typeName
                        + " does not contain requested " + propertyNames[i]
                        + " attribute");
            }
        }

        return types;
    }

    /**
     * Locking manager used for this DataStore.
     *
     * <p>
     * By default AbstractDataStore makes use of InProcessLockingManager.
     * </p>
     *
     *
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return lockingManager;
    }

    /**
     * Sets the FIDMapper for a specific type name
     *
     * @param featureTypeName
     * @param fidMapper
     */
    public void setFIDMapper(String featureTypeName, FIDMapper fidMapper) {
        typeHandler.setFIDMapper(featureTypeName, fidMapper);
    }

    /**
     * Returns the FIDMapperFactory used for this data store
     *
     */
    public FIDMapperFactory getFIDMapperFactory() {
        return typeHandler.getFIDMapperFactory();
    }

    /**
     * Allows to override the default FIDMapperFactory.
     *
     * <p>
     * Warning: the ovveride may not be supported by all data stores, in this
     * case an exception will be thrown
     * </p>
     *
     * @param fmFactory
     *
     * @throws UnsupportedOperationException -
     *             if the datastore does not allow the factory override
     */
    public void setFIDMapperFactory(FIDMapperFactory fmFactory)
            throws UnsupportedOperationException {
        typeHandler.setFIDMapperFactory(fmFactory);
    }

    /**
     * returns true if the requested names list all the attributes in the
     * correct order.
     *
     * @param requestedNames
     * @param ft
     */
    public boolean allSameOrder(String[] requestedNames, FeatureType ft) {
        if (requestedNames.length != ft.getAttributeCount())
            return false; // incorrect # of attribute
        for (int t = 0; t < requestedNames.length; t++) {
            if (!(requestedNames[t].equals(ft.getAttributeType(t).getName())))
                return false; // name doesnt match
        }
        return true;
    }

}
