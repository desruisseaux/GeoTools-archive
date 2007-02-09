package org.geotools.data.h2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.store.ContentEntry;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.opengis.feature.type.TypeName;

/**
 * Convenience methods used by the H2 data store.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class H2Utils {

	/**
	 * Creates a list of the type names ( table names )available via the given 
	 * content.
	 * 
	 * @return A list of {@link TypeName}.
	 */
	static final List typeNames( H2Content content ) throws Exception {
		
		Connection conn = content.connection();
		
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet tables = 
				metaData.getTables( null, content.getDatabaseSchema(), "%", null );
			/*
			 *	<LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
		     *	<LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
		     *	<LI><B>TABLE_NAME</B> String => table name
		     *	<LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
		     *			"VIEW",	"SYSTEM TABLE", "GLOBAL TEMPORARY", 
		     *			"LOCAL TEMPORARY", "ALIAS", "SYNONYM".
		     *	<LI><B>REMARKS</B> String => explanatory comment on the table
		     *  <LI><B>TYPE_CAT</B> String => the types catalog (may be <code>null</code>)
		     *  <LI><B>TYPE_SCHEM</B> String => the types schema (may be <code>null</code>)
		     *  <LI><B>TYPE_NAME</B> String => type name (may be <code>null</code>)
		     *  <LI><B>SELF_REFERENCING_COL_NAME</B> String => name of the designated 
		     *                  "identifier" column of a typed table (may be <code>null</code>)
		     *	<LI><B>REF_GENERATION</B> String => specifies how values in 
		     *                  SELF_REFERENCING_COL_NAME are created. Values are
		     *                  "SYSTEM", "USER", "DERIVED". (may be <code>null</code>)	
		     */
			
			List typeNames = new ArrayList();
			while( tables.next() ) {
				String tableName = tables.getString( "TABLE_NAME" );
				typeNames.add( new org.geotools.feature.type.TypeName( tableName ) );
			}
			
			return typeNames;
		}
		finally {
			conn.close();
		}
		
	}
	
	/**
	 * Returns a list of sql type names which correspond to the content entry.
	 * 
	 * @param state The content state.
	 * 
	 * @return A list of database dependent type names.
	 * 
	 * @throws Exception Any I/O errors that occur.
	 */
	static final String[] sqlTypeNames( H2ContentState state ) throws Exception {
		H2DataStore dataStore = state.getDataStore();
		ContentEntry entry = state.getEntry();
		
		FeatureType featureType = state.featureType();
		
		//figure out what the sql types are
		int[] sqlTypes = new int[ featureType.getAttributeCount() ];
		for ( int i = 0; i < featureType.getAttributeCount(); i++) {
			AttributeType attributeType = featureType.getAttributeType( i );
			Class clazz = attributeType.getType();
			
			sqlTypes[ i ] = H2TypeBuilder.mapping( clazz );
		}
		
		//get metadata about types from the database
    	Connection conn = state.getDataStore().connection();
    	try {
    		DatabaseMetaData metaData = conn.getMetaData();
    		
    		/*
    		 *<LI><B>TYPE_NAME</B> String => Type name
    	     *	<LI><B>DATA_TYPE</B> int => SQL data type from java.sql.Types
    	     *	<LI><B>PRECISION</B> int => maximum precision
    	     *	<LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal 
    	     *      (may be <code>null</code>)
    	     *	<LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal 
    	     (may be <code>null</code>)
    	     *	<LI><B>CREATE_PARAMS</B> String => parameters used in creating 
    	     *      the type (may be <code>null</code>)
    	     *	<LI><B>NULLABLE</B> short => can you use NULL for this type.
    	     *      <UL>
    	     *      <LI> typeNoNulls - does not allow NULL values
    	     *      <LI> typeNullable - allows NULL values
    	     *      <LI> typeNullableUnknown - nullability unknown
    	     *      </UL>
    	     *	<LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive.
    	     *	<LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
    	     *      <UL>
    	     *      <LI> typePredNone - No support
    	     *      <LI> typePredChar - Only supported with WHERE .. LIKE
    	     *      <LI> typePredBasic - Supported except for WHERE .. LIKE
    	     *      <LI> typeSearchable - Supported for all WHERE ..
    	     *      </UL>
    	     *	<LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned.
    	     *	<LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value.
    	     *	<LI><B>AUTO_INCREMENT</B> boolean => can it be used for an 
    	     *      auto-increment value.
    	     *	<LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name 
    	     *      (may be <code>null</code>)
    	     *	<LI><B>MINIMUM_SCALE</B> short => minimum scale supported
    	     *	<LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
    	     *	<LI><B>SQL_DATA_TYPE</B> int => unused
    	     *	<LI><B>SQL_DATETIME_SUB</B> int => unused
    	     *	<LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
    	     */
    		ResultSet types = metaData.getTypeInfo();
    		
    		try {
    			
    			//figure out the type names that correspond to the sql types 
    			String[] sqlTypeNames = new String[ sqlTypes.length ];
    			
    			while( types.next() ) {
    				int sqlType = types.getInt( "DATA_TYPE" );
    				String sqlTypeName = types.getString( "TYPE_NAME" );
    				
    				for ( int i = 0; i < sqlTypes.length; i++ ) {
    					if ( sqlType == sqlTypes[ i ] ) {
    						sqlTypeNames[ i ] = sqlTypeName;
    					}
    				}
        		}	
    			
    			return sqlTypeNames;
    		
    		}
    		finally {
    			types.close();
    		}
    	}
    	finally {
    		conn.close();
    	}
    		
	}
	
	/**
	 * Builds a feature type for content entry.
	 * 
	 * @param state The content state.
	 * @param builder The builder used to create the type.
	 * 
	 * @return The built type.
	 * 
	 * @throws Exception Any I/O errors that occur.
	 */
	static final FeatureType buildFeatureType( H2ContentState state, H2TypeBuilder builder ) throws Exception {
		
		H2DataStore dataStore = state.getDataStore();
		ContentEntry entry = state.getEntry();
		
		//set up the name
    	builder.setName( entry.getName().getLocalPart() );
    	
    	//set the namespace, if not null
    	if ( entry.getName().getNamespaceURI() != null ) {
    		builder.setNamespaceURI( entry.getName().getNamespaceURI() );	
    	}
    	
    	//get metadata about columns from database
    	Connection conn = state.getDataStore().connection();
    	try {
    		DatabaseMetaData metaData = conn.getMetaData();
    		
    		/*
		     *	<LI><B>COLUMN_NAME</B> String => column name
		     *	<LI><B>DATA_TYPE</B> int => SQL type from java.sql.Types
		     *	<LI><B>TYPE_NAME</B> String => Data source dependent type name,
		     *  for a UDT the type name is fully qualified
		     *	<LI><B>COLUMN_SIZE</B> int => column size.  For char or date
		     *	    types this is the maximum number of characters, for numeric or
		     *	    decimal types this is precision.
		     *	<LI><B>BUFFER_LENGTH</B> is not used.
		     *	<LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
		     *	<LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
		     *	<LI><B>NULLABLE</B> int => is NULL allowed.
		     *      <UL>
		     *      <LI> columnNoNulls - might not allow <code>NULL</code> values
		     *      <LI> columnNullable - definitely allows <code>NULL</code> values
		     *      <LI> columnNullableUnknown - nullability unknown
		     *      </UL>
		     * 	<LI><B>COLUMN_DEF</B> String => default value (may be <code>null</code>)
		     *	<LI><B>IS_NULLABLE</B> String => "NO" means column definitely 
		     *      does not allow NULL values; "YES" means the column might 
		     *      allow NULL values.  An empty string means nobody knows.
    		 */
    		ResultSet columns = 
    			metaData.getColumns( null, dataStore.getDatabaseSchema(), entry.getTypeName(), "%");
    		 
    		/*
			 *	<LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
		     *	<LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
		     *	<LI><B>TABLE_NAME</B> String => table name
		     *	<LI><B>COLUMN_NAME</B> String => column name
		     *	<LI><B>KEY_SEQ</B> short => sequence number within primary key
		     *	<LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
    		 */
    		ResultSet primaryKeys = 
    			metaData.getPrimaryKeys( null, dataStore.getDatabaseSchema(), entry.getTypeName() );
    		
    		try {
	    		while ( columns.next() ) {
	    			String name = columns.getString( "COLUMN_NAME" );
	    			
	    			//do not include primary key in the type
	    			while( primaryKeys.next() ) {
	    				String keyName = primaryKeys.getString( "COLUMN_NAME" );
	    				if ( name.equals( keyName ) ) {
	    					name = null;
	    					break;
	    				}
	    			}
	    			primaryKeys.beforeFirst();

	    			if ( name == null ) {
	    				continue;
	    			}
	    			
	    			//get the type
	    			int binding = columns.getInt( "DATA_TYPE" );
	    			
	    			//add the attribute
	    			builder.attribute( name, binding );
	    		}
	    		
	    		return builder.feature();
    		}
    		finally {
    			columns.close();
    			primaryKeys.close();
    		}
    	}
    	finally {
    		conn.close();
    	}
    	
	}
	
	/**
	 * Determines the elements of a primary key of a content entry.
	 * 
	 * @param state The content state.
	 * 
	 * @return An array {@link PrimaryKey}, one for each element of the primary key.
	 * 
	 * @throws Exception Any I/O errors that occur.
	 */
	static final PrimaryKey primaryKey( H2ContentState state ) throws Exception {
		
		H2DataStore dataStore = state.getDataStore();
		ContentEntry entry = state.getEntry();
		
		//get metadata from database
    	Connection conn = state.getDataStore().connection();
    	try {
    		DatabaseMetaData metaData = conn.getMetaData();
    		ResultSet primaryKey = 
    			metaData.getPrimaryKeys( null, dataStore.getDatabaseSchema(), entry.getTypeName() );
    		
    		/* 
    	     *	<LI><B>TABLE_CAT</B> String => table catalog (may be <code>null</code>)
    	     *	<LI><B>TABLE_SCHEM</B> String => table schema (may be <code>null</code>)
    	     *	<LI><B>TABLE_NAME</B> String => table name
    	     *	<LI><B>COLUMN_NAME</B> String => column name
    	     *	<LI><B>KEY_SEQ</B> short => sequence number within primary key
    	     *	<LI><B>PK_NAME</B> String => primary key name (may be <code>null</code>)
    		 */
    		ArrayList keyColumns = new ArrayList();
    		while( primaryKey.next() ) {
    			String columnName = primaryKey.getString( "COLUMN_NAME" );
    			
    			//look up the type ( should only be one row )
    			ResultSet columns = 
    				metaData.getColumns( null, dataStore.getDatabaseSchema(), entry.getTypeName(), columnName ); 
    			columns.next();
    			int binding = columns.getInt( "DATA_TYPE" );
    			Class columnType = H2TypeBuilder.mapping( binding );
    			
    			keyColumns.add( new PrimaryKey.Column( columnName, columnType ) );
    		}
    		
    		return new PrimaryKey( (PrimaryKey.Column[]) keyColumns.toArray( new PrimaryKey.Column[ keyColumns.size() ] ) );
    	}
    	finally {
    		conn.close();
    	}
	}
}
