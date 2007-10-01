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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.geotools.data.Transaction;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.feature.Name;
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
        Connection cx = connection();
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
    
    protected String createTableSQL( SimpleFeatureType featureType, Connection cx ) 
        throws Exception {
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
        

        //build the table sql
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE ");

        encodeDatabaseSchema(sql);
        dialect.table( featureType.getTypeName(), sql );
        sql.append(" ( ");

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
        
        encodeDatabaseSchema(sql);
        dialect.table( featureType.getTypeName(), sql );
        
        if ( filter != null ) {
            //encode filter
        }
        
        return sql.toString();
        
    }
    
    /**
     * Helper method to check for null and encode database schema.
     */
    protected void encodeDatabaseSchema( StringBuffer sql ) {
        if ( databaseSchema != null ) {
            dialect.schema(databaseSchema, sql);
            sql.append( "." ); 
        }
    }
    
    protected PrimaryKey getPrimaryKey( ContentEntry entry ) throws IOException {
	 
        JDBCState state = (JDBCState) entry.getState(Transaction.AUTO_COMMIT);
        if ( state.getPrimaryKey() == null ) {
            synchronized ( this ) {
                if ( state.getPrimaryKey() == null ) {
                    //get metadata from database
                    Connection cx = connection();

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
                            ArrayList keyColumns = new ArrayList();

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
                                
                                keyColumns.add(new PrimaryKey.Column(columnName, columnType));
                            }

                            state.setPrimaryKey(
                                new PrimaryKey((PrimaryKey.Column[]) keyColumns.toArray( new PrimaryKey.Column[keyColumns.size()]))
                            );
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
    
    public PrimaryKey getPrimaryKey( SimpleFeatureType featureType ) throws IOException {
    	return getPrimaryKey( ensureEntry( featureType.getName() ) );
	}
    
  
    /**
     * Convenience method for grabbing a new connection.
     * <p>
     * Callers of this method should close the connection when done with it.
     * </p>.
     *
     */
    public final Connection connection() {
        try {
			return getDataSource().getConnection();
		} 
        catch (SQLException e) {
        	throw new RuntimeException( "Unable to obtain connection", e );
		}
    }
    
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new JDBCFeatureSource( entry );
    }
    
    protected ContentState createContentState(ContentEntry entry) {
    	return new JDBCState( entry );
    }
    
    protected List createTypeNames() throws IOException {
        Connection cx = connection();
        
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
                    typeNames.add(new Name(tableName));
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

    protected void closeSafe( ResultSet rs ) {
        try {
            rs.close();
        }
        catch( SQLException e ) {
            String msg = "Error occurred closing result set";
            LOGGER.warning(msg);
            if ( LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, msg, e );
            }
        }
    }
    
    protected void closeSafe( Statement st ) {
        try {
            st.close();
        }
        catch( SQLException e ) {
            String msg = "Error occurred closing statement";
            LOGGER.warning(msg);
            if ( LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, msg, e );
            }
        }
    }
    protected void closeSafe( Connection cx ) {
        try {
            cx.close();
        } 
        catch (SQLException e) {
            String msg = "Error occurred closing connection";
            LOGGER.warning(msg);
            if ( LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, msg, e );
            }
        }
    }
}
