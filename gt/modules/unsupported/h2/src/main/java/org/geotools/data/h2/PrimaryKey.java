package org.geotools.data.h2;

import java.net.URLDecoder;

/**
 * Primary key of a table.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class PrimaryKey {
	
	/**
	 * The columns making up the primary key
	 */
	public Column[] columns;
	
	/**
	 * Creates a new primary key.
	 */
	public PrimaryKey( Column[] columns ) {
		this.columns = columns;
	}
	
	/**
	 * Decodes a featureId into an array of objects which map to the columns 
	 * of the primary key.
	 * @param fid
	 * @return
	 * @throws Exception
	 */
	public Object[] decode( String fid ) throws Exception {
		
		Object[] values = new Object[ columns.length ];
		String[] tokens = fid.split("&");
		
		if ( tokens.length != columns.length) {
			throw new RuntimeException( "fid: " + fid + " does not map to primary key with " + columns.length + " elements" );
		}

		for (int i = 0; i < tokens.length; i++) {
		    values[ i ] = URLDecoder.decode( tokens[ i ], "UTF-8" );
		}

		return values;
		
	}
	
	/**
	 * A column in a primary key.
	 * 
	 */
	static class Column {
		/**
		 * THe column name;
		 */
		public String name;
		/**
		 * The column type.
		 */
		public Class type;
		
		public Column( String name, Class type ) {
			this.name = name;
			this.type = type;
		}
		
	}
	
	
}
