/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.postgis.fidmapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.geotools.data.jdbc.fidmapper.DefaultFIDMapperFactory;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import org.geotools.feature.FeatureType;

/**
 * Postgis specific FIDMapperFactory that uses the {@link org.geotools.data.postgis.fidmapper.OIDFidMapper OIDFidMapper}
 * to map tables with no primary keys or tables that have weird primary keys that cannot be mapped
 * in other ways.
 * 
 * @author Andrea Aime
 *
 * @source $URL$
 */
public class PostgisFIDMapperFactory extends DefaultFIDMapperFactory {
    protected FIDMapper buildNoPKMapper(String schema, String tableName,
        Connection connection) {
        return new OIDFidMapper();
    }

    protected FIDMapper buildLastResortFidMapper(String schema,
        String tableName, Connection connection, ColumnInfo[] colInfos) {

	int major;
	try {
		major = connection.getMetaData().getDatabaseMajorVersion();
	} catch (SQLException e) {
		major=7;
	}
        if( major>7 )
            throw new IllegalArgumentException("Tables for postgis 8+ must have a primary key defined");

        return new OIDFidMapper();
    }
    
    protected FIDMapper buildSingleColumnFidMapper(String schema, String tableName, Connection connection, ColumnInfo ci) {
        if (ci.isAutoIncrement())
            return new PostGISAutoIncrementFIDMapper(tableName, ci.getColName(), ci.getDataType());
        return super.buildSingleColumnFidMapper(schema, tableName, connection, ci);
    }
    
    /**
     *  see@DefaultFIDMapperFactory in main module (jdbc)
     *   This version pre-double quotes the column name and table name and passes it to the superclass's version.
     */
    protected boolean isAutoIncrement(String catalog, String schema,
            String tableName, Connection conn, ResultSet tableInfo,
            String columnName, int dataType) throws SQLException 
	{
    	return super.isAutoIncrement( catalog, schema, "\""+tableName+"\"",conn, tableInfo,
    			"\""+columnName+"\"",dataType);
    
    }
//
//    /**
//     * @see org.geotools.data.jdbc.fidmapper.DefaultFIDMapperFactory#getMapper(org.geotools.feature.FeatureType)
//     */
//    public FIDMapper getMapper( FeatureType featureType ) {
//        return new TypedFIDMapper(new OIDFidMapper(), featureType.getTypeName());
//    }
//    
//    /**
//     * @see org.geotools.data.jdbc.fidmapper.DefaultFIDMapperFactory#getMapper(java.lang.String, java.lang.String, java.lang.String, java.sql.Connection)
//     */
//    public FIDMapper getMapper( String catalog, String schema, String tableName,
//            Connection connection ) throws IOException {
//        return new TypedFIDMapper(new OIDFidMapper(), tableName);
//    }
}
