package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class JDBCFeatureSource extends ContentFeatureSource {

	PrimaryKey primaryKey;
	
    public JDBCFeatureSource(ContentEntry entry) throws IOException {
        super(entry);
        
        primaryKey =  ((JDBCDataStore) entry.getDataStore()).getPrimaryKey(entry);
    }

    public JDBCDataStore getDataStore() {
        return (JDBCDataStore) super.getDataStore();
    }
    
    public JDBCState getState() {
        return (JDBCState) super.getState();
    }
    
    public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}
    
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
        
        //ensure we have a connection
        Connection cx = getDataStore().getConnection( this );
        
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
            ResultSet columns = 
                metaData.getColumns(null, getDataStore().getDatabaseSchema(), tableName, "%");

            /*
             *        <LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
             *        <LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
             *        <LI><B>TABLE_NAME</B> String => table name
             *        <LI><B>COLUMN_NAME</B> String => column name
             *        <LI><B>KEY_SEQ</B> short => sequence number within primary key
             *        <LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
             */
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, getDataStore().getDatabaseSchema(),
                   tableName);

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
    
                    //get the mapping
                    Class binding = getDataStore().getMapping(dataType);
                    if ( binding == null ) {
                        JDBCDataStore.LOGGER.warning("Could not find mapping for:" + dataType );
                        binding = Object.class;
                    }
                    
                    //add the attribute
                    tb.add(name, binding);
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
