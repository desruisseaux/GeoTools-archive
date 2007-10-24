package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.geotools.data.FeatureStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

/**
 * FeatureStore implementation for jdbc based relational database tables.
 * <p>
 * All the operations of this class are delegated to {@link JDBCFeatureCollection}
 * via the {@link #all(ContentState)} and {@link #filtered(ContentState, Filter)}
 * methods.
 * 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 */
public final class JDBCFeatureStore extends ContentFeatureStore {

    /**
     * primary key of the table
     */
    PrimaryKey primaryKey;
    
    /**
     * Creates the new feature store.
     * @param entry The datastore entry.
     */
    public JDBCFeatureStore(ContentEntry entry) throws IOException {
        super(entry);
        
        //TODO: cache this
        primaryKey =  ((JDBCDataStore) entry.getDataStore()).getPrimaryKey(entry);
    }
    
    /**
     * Type narrow to {@link JDBCDataStore}.
     */
    public JDBCDataStore getDataStore() {
        return (JDBCDataStore) super.getDataStore();
    }
    /**
     * Type narrow to {@link JDBCState}.
     */
    public JDBCState getState() {
        return (JDBCState) super.getState();
    }
    
    /**
     * Returns the primary key of the table backed by feature store.
     */
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }
    
    /**
     * This method operates by delegating to the 
     * {@link JDBCFeatureCollection#update(AttributeDescriptor[], Object[])}
     * method provided by the feature collection resulting from 
     * {@link #filtered(ContentState, Filter)}.
     * 
     * @see FeatureStore#modifyFeatures(AttributeDescriptor[], Object[], Filter)
     */
    public void modifyFeatures(AttributeDescriptor[] type, Object[] value,
            Filter filter) throws IOException {
        
        if ( filter == null ) {
            String msg = "Must specify a filter, must not be null.";
            throw new IllegalArgumentException( msg );
        }
        
        JDBCFeatureCollection features = 
            (JDBCFeatureCollection) filtered(getState(), filter);
        features.update(type, value);
    }
    
    /**
     * Builds the feature type from database metadata.
     */
    protected SimpleFeatureType buildFeatureType() throws IOException {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder( );
        
        //set up the name
        String tableName = entry.getName().getLocalPart();
        tb.setName( tableName );
        
        //set the namespace, if not null
        if (entry.getName().getNamespaceURI() != null) {
            tb.setNamespaceURI(entry.getName().getNamespaceURI());
        }
        else {
            //use the data store
            tb.setNamespaceURI( getDataStore().getNamespaceURI() );
        }
        
        //grab the schema
        String databaseSchema = getDataStore().getDatabaseSchema();
        
        //ensure we have a connection
        Connection cx = getDataStore().getConnection( getState() );
        
        //get metadata about columns from database
        try {
            
            
            DatabaseMetaData metaData = cx.getMetaData();

            /*
             *        <LI><B>COLUMN_NAME</B> String => column name
             *        <LI><B>DATA_TYPE</B> int => SQL type from java.sql.Types
             *        <LI><B>TYPE_NAME</B> String => Data source dependent type name,
             *  for a UDT the type name is fully qualified
             *        <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
             *            types this is the maximum number of characters, for numeric or
             *            decimal types this is precision.
             *        <LI><B>BUFFER_LENGTH</B> is not used.
             *        <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
             *        <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
             *        <LI><B>NULLABLE</B> int => is NULL allowed.
             *      <UL>
             *      <LI> columnNoNulls - might not allow <code>NULL</code> values
             *      <LI> columnNullable - definitely allows <code>NULL</code> values
             *      <LI> columnNullableUnknown - nullability unknown
             *      </UL>
             *         <LI><B>COLUMN_DEF</B> String => default value (may be <code>null</code>)
             *        <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
             *      does not allow NULL values; "YES" means the column might
             *      allow NULL values.  An empty string means nobody knows.
             */
            ResultSet columns = metaData.getColumns(null, databaseSchema, tableName, "%");

            /*
             *        <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
             *        <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
             *        <LI><B>TABLE_NAME</B> String => table name
             *        <LI><B>COLUMN_NAME</B> String => column name
             *        <LI><B>KEY_SEQ</B> short => sequence number within primary key
             *        <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
             */
            ResultSet primaryKeys = 
                metaData.getPrimaryKeys(null, databaseSchema, tableName);

            try {
                SQLDialect dialect = getDataStore().getSQLDialect();
                while (columns.next()) {
                    String name = columns.getString("COLUMN_NAME");
    
                    //do not include primary key in the type
                    while (primaryKeys.next()) {
                        String keyName = primaryKeys.getString("COLUMN_NAME");
    
                        if (name.equals(keyName)) {
                            name = null;
    
                            break;
                        }
                    }
    
                    primaryKeys.beforeFirst();
    
                    if (name == null) {
                        continue;
                    }
    
                    //get the type
                    int dataType = columns.getInt("DATA_TYPE");
                    String typeName = columns.getString( "TYPE_NAME");
                    
                    //get the mapping from the integer type
                    Class binding = getDataStore().getMapping(dataType);
                    if ( binding == null ) {
                        //not found, try getting from the data type name
                        binding = getDataStore().getMapping( typeName );
                        
                        if ( binding == null ) {
                            //not found, one last try, ask the dialect
                            binding = dialect.getMapping( databaseSchema, tableName, name, dataType, cx);
                        }
                    }
                    
                    //if still not found, resort to Object
                    if ( binding == null ) {
                        JDBCDataStore.LOGGER.warning("Could not find mapping for:" + dataType );
                        binding = Object.class;
                    }
                    
                    //if the binding is a geometry, try to figure out its srid
                    if ( Geometry.class.isAssignableFrom( binding ) ) {
                        //add the attribute as a geometry, try to figure out 
                        // its srid first
                        Integer srid = null;
                        try {
                            srid = dialect.getGeometrySRID( databaseSchema , tableName, name, cx );
                        }
                        catch( Exception e ) {
                            String msg = "Error occured determing srid for " 
                                + tableName + "." + name;
                            getDataStore().LOGGER.log( Level.WARNING, msg, e );
                        }
                        
                        tb.add( name, binding, srid );
                    }
                    else {
                      //add the attribute
                        tb.add(name, binding);    
                    }
                    
                }
    
                return tb.buildFeatureType();
            }
            finally {
                getDataStore().closeSafe( columns );
                getDataStore().closeSafe( primaryKeys );
            }
           
        }
        catch( SQLException e ) {
            String msg = "Error occurred building feature type";
            throw (IOException) new IOException( ).initCause(e); 
        } 
    }
    
    protected JDBCFeatureCollection all(ContentState state) {
        return new JDBCFeatureCollection( this, getState() );
    }

    protected JDBCFeatureCollection filtered(ContentState state, Filter filter) {
        return new JDBCFeatureCollection( this, getState(), filter );
    }
}
