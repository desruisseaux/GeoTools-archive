package org.geotools.data.jdbc;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.collection.DelegateFeatureWriter;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.feature.Name;
import org.geotools.filter.FilterCapabilities;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class JDBCDataStore extends ContentDataStore {
    /**
     * Mappings from sql type, to java class
     */
    static HashMap MAPPINGS = new HashMap();

    static {
        MAPPINGS.put(new Integer(Types.VARCHAR), String.class);
        MAPPINGS.put(new Integer(Types.CHAR), String.class);
        MAPPINGS.put(new Integer(Types.LONGVARCHAR), String.class);

        MAPPINGS.put(new Integer(Types.BIT), Boolean.class);
        MAPPINGS.put(new Integer(Types.BOOLEAN), Boolean.class);

        MAPPINGS.put(new Integer(Types.TINYINT), Short.class);
        MAPPINGS.put(new Integer(Types.SMALLINT), Short.class);

        MAPPINGS.put(new Integer(Types.INTEGER), Integer.class);
        MAPPINGS.put(new Integer(Types.BIGINT), Long.class);

        MAPPINGS.put(new Integer(Types.REAL), Float.class);
        MAPPINGS.put(new Integer(Types.FLOAT), Double.class);
        MAPPINGS.put(new Integer(Types.DOUBLE), Double.class);

        MAPPINGS.put(new Integer(Types.DECIMAL), BigDecimal.class);
        MAPPINGS.put(new Integer(Types.NUMERIC), BigDecimal.class);

        MAPPINGS.put(new Integer(Types.DATE), Date.class);
        MAPPINGS.put(new Integer(Types.TIME), Time.class);
        MAPPINGS.put(new Integer(Types.TIMESTAMP), Timestamp.class);

        MAPPINGS.put(new Integer(Types.OTHER), Geometry.class);
    }

    /**
     * Mappings from java class to sql tpe
     */
    static HashMap RMAPPINGS = new HashMap();

    static {
        RMAPPINGS.put(String.class, new Integer(Types.VARCHAR));

        RMAPPINGS.put(Boolean.class, new Integer(Types.BOOLEAN));

        RMAPPINGS.put(Short.class, new Integer(Types.SMALLINT));

        RMAPPINGS.put(Integer.class, new Integer(Types.INTEGER));
        RMAPPINGS.put(Long.class, new Integer(Types.BIGINT));

        RMAPPINGS.put(Float.class, new Integer(Types.REAL));
        RMAPPINGS.put(Double.class, new Integer(Types.DOUBLE));

        RMAPPINGS.put(BigDecimal.class, new Integer(Types.NUMERIC));

        RMAPPINGS.put(Date.class, new Integer(Types.DATE));
        RMAPPINGS.put(Time.class, new Integer(Types.TIME));
        RMAPPINGS.put(Timestamp.class, new Integer(Types.TIMESTAMP));

        RMAPPINGS.put(Geometry.class, new Integer(Types.OTHER));
        RMAPPINGS.put(Point.class, new Integer(Types.OTHER));
        RMAPPINGS.put(LineString.class, new Integer(Types.OTHER));
        RMAPPINGS.put(Polygon.class, new Integer(Types.OTHER));
        
    }
    
	/**
	 * logging instance
	 */
	public static final Logger LOGGER = Logger.getLogger( "org.geotools.data.jdbc" );
	/**
	 * data source
	 */
	protected DataSource dataSource;
	/**
	 * the dialect of sql
	 */
	protected SQLDialect dialect = new SQLDialect();
	/**
     * The database schema.
     */
    protected String databaseSchema;
    
    /**
     * sql type to java class mappings
     */
    protected HashMap/*<Integer,Class>*/ sqlTypeToClassMappings = MAPPINGS;
    /**
     * java class to sql type mappings;
     */
    protected HashMap/*<Class,Integer>*/ classToSqlTypeMappings = RMAPPINGS;
    
    /**
     * filter capabilities of the datastore
     */
    protected FilterCapabilities filterCapabilities;
    
    public void setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
    }
    
    public String getDatabaseSchema() {
        return databaseSchema;
    }

    public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
    
    public DataSource getDataSource() {
		return dataSource;
	}
    
    public void setSQLDialect( SQLDialect dialect ) {
        this.dialect = dialect;
    }
    
    public SQLDialect getSQLDialect() {
        return dialect;
    }
    
    public void setFilterCapabilities(FilterCapabilities filterCapabilities) {
        this.filterCapabilities = filterCapabilities;
    }
    
    public FilterCapabilities getFilterCapabilities() {
        return filterCapabilities;
    }
    
    public HashMap getClassToSqlTypeMappings() {
        return classToSqlTypeMappings;
    }
    
    public void setClassToSqlTypeMappings(HashMap classToSqlTypeMappings) {
        this.classToSqlTypeMappings = classToSqlTypeMappings;
    }
    
    public HashMap getSqlTypeToClassMappings() {
        return sqlTypeToClassMappings;
    }
    
    public void setSqlTypeToClassMappings(HashMap sqlTypeToClassMappings) {
        this.sqlTypeToClassMappings = sqlTypeToClassMappings;
    }
    
    public Class getMapping( int sqlType ) {
        Class mapping = (Class) getSqlTypeToClassMappings().get(new Integer(sqlType));
        if ( mapping == null ) {
            LOGGER.warning("No mapping for " + sqlType);
            mapping = Object.class;
        }
        
        return mapping;
    }
    
    public Integer getMapping( Class clazz ) {
        Integer mapping = (Integer) getClassToSqlTypeMappings().get( clazz );
        if ( mapping == null ) {
            LOGGER.warning("No mapping for " + clazz.getName() );
        }
        
        return mapping;
    }
    
    public Logger getLogger() {
    	return LOGGER;
    }
    
    public void createSchema(final SimpleFeatureType featureType) throws IOException {
  
        if (entry(featureType.getName()) != null) {
            String msg = "Schema '" + featureType.getName() + "' already exists";
            throw new IllegalArgumentException(msg);
        }
        //execute the create table statement
        //TODO: create a primary key and a spatial index
        Connection cx = createConnection();
        try {
            String sql = createTableSQL( featureType, cx );
            LOGGER.fine( sql );
            Statement st = cx.createStatement();
            try {
                st.execute(sql);    
            }
            finally {
                closeSafe( st );
            }
        }
        catch( Exception e ) {
            String msg = "Error occurred creating table";
            throw (IOException) new IOException( msg ).initCause( e );
        }
        finally {
            closeSafe( cx );
        }
        
        //create a content entry for the type
        ContentEntry entry = new ContentEntry( this, featureType.getName() );
        
        //cache the feature type
        entry.getState( Transaction.AUTO_COMMIT ).setFeatureType( featureType );
        entries.put(entry.getName(), entry);
    }
    
    public FeatureWriter getFeatureWriter(String typeName, Filter filter,
            Transaction tx) throws IOException {
        
        JDBCFeatureCollection collection = 
            (JDBCFeatureCollection) getFeatureSource( typeName,tx).getFeatures( filter );
        return new DelegateFeatureWriter( collection.getSchema(), collection.writer() );
    }
    
    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction tx) 
        throws IOException {
        
        JDBCFeatureCollection collection = 
            (JDBCFeatureCollection) getFeatureSource(typeName,tx).getFeatures();
        return new DelegateFeatureWriter( collection.getSchema(), collection.inserter() );
    }
    
    protected String createTableSQL( SimpleFeatureType featureType, Connection cx ) 
        throws Exception {
        StringBuffer sql = new StringBuffer();
        
        //figure out what the sql types are corresponding to the feature type
        // attributes
        int[] sqlTypes = new int[featureType.getAttributeCount()];
        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            AttributeDescriptor attributeType = featureType.getAttribute(i);
            Class clazz = attributeType.getType().getBinding();
            Integer sqlType = getMapping(clazz);
            if ( sqlType == null ) {
                LOGGER.warning("No sql type mapping for: " + clazz );
                sqlType = Types.OTHER;
            }
            
            sqlTypes[i] = sqlType;
        }
        
        //figure out the type names that correspond to the sql types from 
        // the database metadata
        String[] sqlTypeNames = new String[sqlTypes.length];
        DatabaseMetaData metaData = cx.getMetaData();
        /*
         *      <LI><B>TYPE_NAME</B> String => Type name
         *        <LI><B>DATA_TYPE</B> int => SQL data type from java.sql.Types
         *        <LI><B>PRECISION</B> int => maximum precision
         *        <LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal
         *      (may be <code>null</code>)
         *        <LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal
           (may be <code>null</code>)
         *        <LI><B>CREATE_PARAMS</B> String => parameters used in creating
         *      the type (may be <code>null</code>)
         *        <LI><B>NULLABLE</B> short => can you use NULL for this type.
         *      <UL>
         *      <LI> typeNoNulls - does not allow NULL values
         *      <LI> typeNullable - allows NULL values
         *      <LI> typeNullableUnknown - nullability unknown
         *      </UL>
         *        <LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive.
         *        <LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
         *      <UL>
         *      <LI> typePredNone - No support
         *      <LI> typePredChar - Only supported with WHERE .. LIKE
         *      <LI> typePredBasic - Supported except for WHERE .. LIKE
         *      <LI> typeSearchable - Supported for all WHERE ..
         *      </UL>
         *        <LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned.
         *        <LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value.
         *        <LI><B>AUTO_INCREMENT</B> boolean => can it be used for an
         *      auto-increment value.
         *        <LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name
         *      (may be <code>null</code>)
         *        <LI><B>MINIMUM_SCALE</B> short => minimum scale supported
         *        <LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
         *        <LI><B>SQL_DATA_TYPE</B> int => unused
         *        <LI><B>SQL_DATETIME_SUB</B> int => unused
         *        <LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
         */
        ResultSet types = metaData.getTypeInfo();
        try {
            while (types.next()) {
                int sqlType = types.getInt("DATA_TYPE");
                String sqlTypeName = types.getString("TYPE_NAME");

                for (int i = 0; i < sqlTypes.length; i++) {
                    if (sqlType == sqlTypes[i]) {
                        sqlTypeNames[i] = sqlTypeName;
                    }
                }
            }    
        }
        finally {
            closeSafe( types );
        }
        

        //build the create table sql
        sql.append("CREATE TABLE ");
        
        encodeTableName( featureType.getTypeName(), sql );
        sql.append(" ( ");

        //fid column
        dialect.primaryKey( "fid", sql );
        sql.append( ", ");
        
        //normal attributes
        for (int i = 0; i < sqlTypeNames.length; i++) {
            AttributeDescriptor att = featureType.getAttribute(i);

            //the column name
            dialect.column(att.getLocalName(), sql);
            sql.append(" ");

            //sql type name
            sql.append(sqlTypeNames[i]);

            if (i < (sqlTypeNames.length - 1)) {
                sql.append(", ");
            }
        }

        sql.append(" );");
        return sql.toString();
    }
    
    protected String selectSQL( SimpleFeatureType featureType, Filter filter ) {
        StringBuffer sql = new StringBuffer();
        sql.append( "SELECT * FROM ");
        
        encodeTableName( featureType.getTypeName(), sql );
        
        if ( filter != null ) {
            //encode filter
            try {
                FilterToSQL toSQL = createFilterToSQL( featureType );
                sql.append( " ").append( toSQL.encodeToString( filter ) );
            } 
            catch (FilterToSQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        return sql.toString();
        
    }
    
    protected String selectBoundsSQL( SimpleFeatureType featureType, Filter filter ) {
        StringBuffer sql = new StringBuffer();
        
        sql.append( "SELECT " );
        
        //check for aggregate vs per row bounds
        String geometryColumn = featureType.getDefaultGeometry().getLocalName();
        if ( dialect.hasAggregateExtent() ) {
            dialect.aggregateExtent(geometryColumn, sql);
        }
        else {
            dialect.extent( geometryColumn, sql );
        }
        sql.append( "FROM" );
        encodeTableName( featureType.getTypeName(), sql );
        
        if ( filter != null ) {
            //encode filter
            try {
                FilterToSQL toSQL = createFilterToSQL( featureType );
                sql.append( " ").append( toSQL.encodeToString( filter ) );
            } 
            catch (FilterToSQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        return sql.toString();
    }
    
    protected String selectCountSQL( SimpleFeatureType featureType, Filter filter ) {
        StringBuffer sql = new StringBuffer();
        
        sql.append( "SELECT count(*) FROM " );
        encodeTableName( featureType.getTypeName(), sql );
        
        if ( filter != null ) {
            //encode filter
            try {
                FilterToSQL toSQL = createFilterToSQL( featureType );
                sql.append( " " ).append( toSQL.encodeToString( filter ) );
            } 
            catch (FilterToSQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        return sql.toString();
    }
    
    protected String deleteSQL( SimpleFeatureType featureType, Filter filter ) {
        StringBuffer sql = new StringBuffer();
        
        sql.append( "DELETE FROM " );
        encodeTableName( featureType.getTypeName(), sql );
        
        if ( filter != null ) {
          //encode filter
            try {
                FilterToSQL toSQL = createFilterToSQL( featureType );
                sql.append( " " ).append( toSQL.encodeToString( filter ) );
            } 
            catch (FilterToSQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        return sql.toString();
    }
    
    protected String insertSQL( SimpleFeatureType featureType, SimpleFeature feature ) {
        StringBuffer sql = new StringBuffer();
        sql.append( "INSERT INTO " );
        encodeTableName(featureType.getTypeName(), sql);
        
        
        //column names
        sql.append( " ( ");
        for ( int i = 0; i < featureType.getAttributeCount(); i++ ) {
            dialect.column(featureType.getAttribute(i).getLocalName(), sql );
            sql.append( "," );
        }
        
        //TODO: check for primary key that is not auto incrementing
//        PrimaryKey key = getPrimaryKey(featureType);
//        for ( int i = 0; i < key.columns.length; i++ ) {
//            dialect.column(key.columns[i].name, sql);
//            sql.append(", ");
//        }
        sql.setLength(sql.length()-1);
        
        //values
        sql.append( " ) VALUES ( ");
        for ( int i = 0; i < featureType.getAttributeCount(); i++ ) {
            AttributeDescriptor att = featureType.getAttribute(i);
            Class binding = att.getType().getBinding();
            
            Object value = feature.getAttribute( att.getLocalName() );
            if ( value == null && !att.isNillable()) {
                //TODO: throw an exception
            }
            
            if ( Geometry.class.isAssignableFrom(binding) ) {
                dialect.encodeGeometryValue( (Geometry) value, sql );
            }
            else {
                dialect.value(value, binding, sql);    
            }
            
            sql.append( "," );
        }
        sql.setLength(sql.length()-1);
        
        sql.append(");");
        return sql.toString();
    }
    
    protected String updateSQL( SimpleFeatureType featureType, AttributeDescriptor[] attributes, Object[] values, Filter filter ) {
        StringBuffer sql = new StringBuffer();
        sql.append( "UPDATE ");
        encodeTableName(featureType.getTypeName(), sql);
        
        sql.append( " SET ");
        for ( int i = 0; i < attributes.length; i++ ) {
            dialect.column( attributes[i].getLocalName(), sql);
            sql.append( " = " );
            dialect.value( values[i], attributes[i].getType().getBinding(), sql );
            sql.append( "," );
        }
        sql.setLength( sql.length()-1 );
        sql.append( " ");
        
        if ( filter != null ) {
            //encode filter
              try {
                  FilterToSQL toSQL = createFilterToSQL( featureType );
                  sql.append( " " ).append( toSQL.encodeToString( filter ) );
              } 
              catch (FilterToSQLException e) {
                  throw new RuntimeException(e);
              }
          }
          
        return sql.toString();
    }
    
    protected FilterToSQL createFilterToSQL(SimpleFeatureType featureType ) {
        //set up a fid mapper
        //TODO: remove this
        final PrimaryKey key;
        try {
            key = getPrimaryKey( featureType );
        } 
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        
        FIDMapper mapper = new FIDMapper() {

            public String createID(Connection conn, SimpleFeature feature,
                    Statement statement) throws IOException {
                return null;
            }

            public int getColumnCount() {
                return 1;
            }

            public int getColumnDecimalDigits(int colIndex) {
                return 0;
            }

            public String getColumnName(int colIndex) {
                return key.getColumnName();
            }

            public int getColumnSize(int colIndex) {
                return 0;
            }

            public int getColumnType(int colIndex) {
                return 0;
            }

            public String getID(Object[] attributes) {
                return null;
            }

            public Object[] getPKAttributes(String FID) throws IOException {
                try {
                    return new Object[]{key.decode(FID)};
                } 
                catch (Exception e) {
                    throw (IOException) new IOException().initCause( e );
                }
            }

            public boolean hasAutoIncrementColumns() {
                return false;
            }

            public void initSupportStructures() {
            }

            public boolean isAutoIncrement(int colIndex) {
                return false;
            }

            public boolean isVolatile() {
                return false;
            }

            public boolean returnFIDColumnsAsAttributes() {
                return false;
            }
            
        };
        FilterToSQL toSQL = new FilterToSQL();
        toSQL.setFeatureType(featureType);
        toSQL.setSqlNameEscape( dialect.getNameEscape() );
        toSQL.setFIDMapper( mapper );
        
        return toSQL;
    }
    
    /**
     * Helper method to encode table name which checks if a schema is set and
     * prefixes the table name with it.
     */
    protected void encodeTableName(  String tableName, StringBuffer sql ) {
        if ( databaseSchema != null ) {
            dialect.schema(databaseSchema, sql);
            sql.append( "." ); 
        }
        dialect.table( tableName, sql);
    }
    
    protected PrimaryKey getPrimaryKey( ContentEntry entry ) throws IOException {
	 
        JDBCState state = (JDBCState) entry.getState(Transaction.AUTO_COMMIT);
        if ( state.getPrimaryKey() == null ) {
            synchronized ( this ) {
                if ( state.getPrimaryKey() == null ) {
                    //get metadata from database
                    Connection cx = createConnection();

                    try {
                        String tableName = entry.getName().getLocalPart();
                        DatabaseMetaData metaData = cx.getMetaData();
                        ResultSet primaryKey = 
                            metaData.getPrimaryKeys(null,databaseSchema, tableName);

                        try {
                            /*
                             *        <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
                             *        <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
                             *        <LI><B>TABLE_NAME</B> String => table name
                             *        <LI><B>COLUMN_NAME</B> String => column name
                             *        <LI><B>KEY_SEQ</B> short => sequence number within primary key
                             *        <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
                             */
                            ArrayList keys = new ArrayList();
                            while (primaryKey.next()) {
                                String columnName = primaryKey.getString("COLUMN_NAME");

                                //look up the type ( should only be one row )
                                ResultSet columns = 
                                    metaData.getColumns(null, databaseSchema, tableName, columnName);
                                columns.next();

                                int binding = columns.getInt("DATA_TYPE");
                                Class columnType = getMapping( binding );
                                if ( columnType == null ) {
                                    LOGGER.warning("No class for sql type " + binding );
                                    columnType = Object.class;
                                }
                                
                                //determine which type of primary key we have
                                PrimaryKey key = null;
                                
                                //1. Auto Incrementing?
                                Statement st = cx.createStatement();
                                try {
                                    //not actually going to get data
                                    st.setFetchSize(1);
                                    
                                    StringBuffer sql = new StringBuffer();
                                    sql.append( "SELECT ");
                                    dialect.column( columnName, sql );
                                    sql.append( " FROM ");
                                    encodeTableName( tableName, sql );
                                    
                                    sql.append( " WHERE 0=1" );
                                    
                                    ResultSet rs= st.executeQuery( sql.toString() );
                                    try {
                                        if ( rs.getMetaData().isAutoIncrement(1) ) {
                                            key = new AutoGeneratedPrimaryKey(tableName, columnName, columnType);
                                        }
                                    }
                                    finally {
                                        closeSafe( rs );
                                    }
                                }
                                finally {
                                    closeSafe( st );
                                }
                                
                                //2. Has a sequence?
                                if ( key == null ) {
                                    //TODO: look for a sequence
                                }
                                
                                if ( key == null ) {
                                    String msg = "Could not determine how to map " +
                                		"primary key values for (" + tableName + "," + 
                                		columnName + "), restorting to null mapping.";
                                    LOGGER.warning(msg);
                                    
                                    key = new NullPrimaryKey(tableName,columnName,columnType);
                                }
                                
                                keys.add( key );
                            }

                            if ( keys.isEmpty() ) {
                                String msg = "No primary key found for " + tableName + "."; 
                                LOGGER.warning(msg);
                                
                                state.setPrimaryKey( new NullPrimaryKey(tableName, null,null) );
                            }
                            else if ( keys.size() > 1 ) {
                                //TODO: create a composite key
                                
                            }
                            else {
                                state.setPrimaryKey( (PrimaryKey) keys.get( 0 ) );    
                            }
                            
                        }
                        finally {
                            closeSafe( primaryKey );
                        }
                       
                    }
                    catch( SQLException e ) {
                        String msg = "Error looking up primary key";
                        throw (IOException) new IOException(msg).initCause(e);
                    
                    } finally {
                        closeSafe( cx );
                    }
                }
            }
        }
        
        return state.getPrimaryKey();
    }
    
    protected PrimaryKey getPrimaryKey( SimpleFeatureType featureType ) throws IOException {
    	return getPrimaryKey( ensureEntry( featureType.getName() ) );
	}
    
    protected ReferencedEnvelope getBounds( SimpleFeatureType featureType, Filter filter, Connection cx ) throws IOException {
       
        String sql = selectBoundsSQL(featureType, filter);
        LOGGER.fine( sql );
        
        try {
            Statement st = cx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            try {
                rs.next();
                ReferencedEnvelope bounds = dialect.envelope(rs, 1);
                
                //keep going to handle case where envelope is not calculated
                // as aggregate function
                while( rs.next() ) {
                    bounds.expandToInclude(dialect.envelope(rs,1));
                }
                
                return bounds;
            } 
            
            finally {
                closeSafe( rs );
                closeSafe( st );    
            }
        }
        catch (SQLException e) {
            String msg = "Error occured calculating bounds";
            throw (IOException) new IOException(msg).initCause( e );
        }
    }
    
    protected int getCount(SimpleFeatureType featureType, Filter filter, Connection cx ) throws IOException {
        String sql = selectCountSQL(featureType, filter);
        LOGGER.fine( sql );
        
        try {
            Statement st = cx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            try {
                rs.next();
                return rs.getInt(1);
            } 
            
            finally {
                closeSafe( rs );
                closeSafe( st );    
            }
        }
        catch (SQLException e) {
            String msg = "Error occured calculating count";
            throw (IOException) new IOException(msg).initCause( e );
        }
    }
    
    protected void insert( SimpleFeatureType featureType, SimpleFeature feature, Connection cx ) throws IOException {
        insert( Collections.singletonList(feature), featureType, cx );
    }
    
    protected void insert( Collection features, SimpleFeatureType featureType, Connection cx ) throws IOException {
        PrimaryKey key = getPrimaryKey( featureType );
        
        // we do this in a synchronized block because we need to do two queries,
        // first to figure out what the id will be, then the insert statement
        synchronized ( this )  {
            Statement st = null;
            try {
                st = cx.createStatement();
            
                for ( Iterator f = features.iterator(); f.hasNext(); ) {
                    SimpleFeature feature = (SimpleFeature) f.next();
                    
                    //figure out what the next fid will be
                    String fid = null;
                    try {
                        Object value = 
                            dialect.getNextPrimaryKeyValue(databaseSchema, key.getTableName(), key.getColumnName(), cx);
                        fid = value.toString();
                    }
                    catch( SQLException e ) {
                        String msg = "Error obtaining next feature id";
                        throw (IOException) new IOException(msg).initCause(e);
                    }
                    
                    String sql = insertSQL( featureType, feature );
                    LOGGER.fine(sql);
                
                    //TODO: execute in batch to improve performance?
                    st.execute( sql );
                       
                    //report the feature id as user data since we cant set the fid
                    feature.getUserData().put( "fid", fid );
                    
                }
                
                //st.executeBatch();
            } 
            catch (SQLException e) {
                String msg = "Error inserting features";
                throw (IOException) new IOException(msg).initCause(e);
            }
            finally {
                closeSafe( st );
            }
        }
       
    }
    
    protected void update( SimpleFeatureType featureType, AttributeDescriptor[] attributes, Object[] values, Filter filter,  Connection cx ) 
        throws IOException {
        
        String sql = updateSQL( featureType, attributes, values, filter );
        LOGGER.fine( sql );
        
        try {
            Statement st = cx.createStatement();
            try {
                st.execute( sql );
            } 
            
            finally {
                closeSafe( st );
            }
        }
        catch (SQLException e) {
            String msg = "Error occured updating features";
            throw (IOException) new IOException(msg).initCause( e );
        }
        
    }
    
    protected void delete( SimpleFeatureType featureType, Filter filter, Connection cx ) throws IOException {
        String sql = deleteSQL( featureType, filter );
        LOGGER.fine( sql );
        try {
            Statement st = cx.createStatement();
            try {
                st.execute( sql );
            } 
            
            finally {
                closeSafe( st );    
            }
        }
        catch (SQLException e) {
            String msg = "Error occured calculating bounds";
            throw (IOException) new IOException(msg).initCause( e );
        }
    }
    
    protected final Connection getConnection( JDBCFeatureSource featureSource ) {
        JDBCState state = featureSource.getState();
        Connection cx = state.getConnection();
        
        if ( cx == null ) {
            synchronized ( state ) {
                //create a new connection
                cx = createConnection();
                
                //set auto commit to false if tx != auto commit
                try {
                    cx.setAutoCommit( featureSource.getTransaction() == Transaction.AUTO_COMMIT );
                }
                catch( SQLException e ) {
                    throw new RuntimeException( e );
                }
                
                state.setConnection( cx );    
            }
        }
        
        return cx;
    }
    
    /**
     * Convenience method for grabbing a new connection.
     * <p>
     * Callers of this method should close the connection when done with it.
     * </p>.
     *
     */
    protected final Connection createConnection() {
        try {
			Connection cx = getDataSource().getConnection();
			
			//TODO: make this configurable
			cx.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
			
			return cx;
		} 
        catch (SQLException e) {
        	throw new RuntimeException( "Unable to obtain connection", e );
		}
    }
    
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new JDBCFeatureStore( entry );
    }
    
    protected ContentState createContentState(ContentEntry entry) {
    	return new JDBCState( entry );
    }
    
    protected List createTypeNames() throws IOException {
        Connection cx = createConnection();
        
        /*
         *        <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
         *        <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
         *        <LI><B>TABLE_NAME</B> String => table name
         *        <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
         *                        "VIEW",        "SYSTEM TABLE", "GLOBAL TEMPORARY",
         *                        "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
         *        <LI><B>REMARKS</B> String => explanatory comment on the table
         *  <LI><B>TYPE_CAT</B> String => the types catalog (may be <code>null</code>)
         *  <LI><B>TYPE_SCHEM</B> String => the types schema (may be <code>null</code>)
         *  <LI><B>TYPE_NAME</B> String => type name (may be <code>null</code>)
         *  <LI><B>SELF_REFERENCING_COL_NAME</B> String => name of the designated
         *                  "identifier" column of a typed table (may be <code>null</code>)
         *        <LI><B>REF_GENERATION</B> String => specifies how values in
         *                  SELF_REFERENCING_COL_NAME are created. Values are
         *                  "SYSTEM", "USER", "DERIVED". (may be <code>null</code>)
         */
        List typeNames = new ArrayList();
        try {
            DatabaseMetaData metaData = cx.getMetaData();
            ResultSet tables = metaData.getTables(null, databaseSchema, "%", null);
            
            try {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    typeNames.add(new Name(namespaceURI, tableName));
                }    
            }
            finally {
                closeSafe( tables );
            }
            
        } catch (SQLException e) {
            throw (IOException) new IOException("Error occurred getting table name list.").initCause(e);
        }
        finally {
            closeSafe( cx );
        }

        return typeNames;
    }

    protected static void closeSafe( ResultSet rs ) {
        if ( rs == null ) 
            return;
            
        try {
            rs.close();
        }
        catch( SQLException e ) {
            String msg = "Error occurred closing result set";
            LOGGER.warning(msg);
            if ( LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, msg, e );
            }
        }
    }
    
    protected static void closeSafe( Statement st ) {
        if ( st == null ) 
            return;
        
        try {
            st.close();
        }
        catch( SQLException e ) {
            String msg = "Error occurred closing statement";
            LOGGER.warning(msg);
            if ( LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, msg, e );
            }
        }
    }
    protected static void closeSafe( Connection cx ) {
        if ( cx == null ) 
            return;
        
        try {
            cx.close();
        } 
        catch (SQLException e) {
            String msg = "Error occurred closing connection";
            LOGGER.warning(msg);
            if ( LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, msg, e );
            }
        }
    }
}
