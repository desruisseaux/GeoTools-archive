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
 */
public class PostgisFIDMapperFactory extends DefaultFIDMapperFactory {
    protected FIDMapper buildNoPKMapper(String schema, String tableName,
        Connection connection) {
        return new OIDFidMapper();
    }

    protected FIDMapper buildLastResortFidMapper(String schema,
        String tableName, Connection connection, ColumnInfo[] colInfos) {
        return new OIDFidMapper();
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
