/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.postgis.fidmapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.geotools.data.jdbc.fidmapper.AutoIncrementFIDMapper;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.Feature;

/**
 * Generate FID based on an auto increment function, the most stable approach for
 * use with editing.
 * 
 * @author Jesse Eichar, Refractions Research, Inc.
 */
public class PostGISAutoIncrementFIDMapper extends AutoIncrementFIDMapper
                implements FIDMapper {

        private static final long serialVersionUID = -6082930630426171079L;

        boolean can_usepg_get_serial_sequence=true;

        public PostGISAutoIncrementFIDMapper(String tableName, String colName, int dataType) {
                super(tableName, colName, dataType);
        }


        public String createID( Connection conn, Feature feature, Statement statement )
            throws IOException {
            if (can_usepg_get_serial_sequence) {
                try {
                    String sql = "SELECT currval(pg_get_serial_sequence(\'";
                    String schema = getTableSchemaName();
                    if (schema != null && !schema.equals("")) {
                        sql = sql + schema + "."; 
                    }
                    sql = sql + getTableName() + "\',\'" + getColumnName() + "\'))";
                    statement.execute(sql); 
                    ResultSet resultSet = statement.getResultSet();
                    if (resultSet.next() && resultSet.getString("currval")!=null )
                        return resultSet.getString("currval");
                    else
                        return findInsertedFID(conn, feature, statement);
                } catch (Exception e) {
                    can_usepg_get_serial_sequence = false;
                    return findInsertedFID(conn, feature, statement);
                }
            } else {
                return findInsertedFID(conn, feature, statement);
            }
        }

        /**
         * PostGIS couldn't supply the answer as we'd like, so we'll try to find it
         * on our own. 
         */
        private String findInsertedFID( Connection conn, Feature feature, Statement statement )
            throws IOException {
            String sql = "SELECT \"" + getColumnName() + "\" FROM \"";
            String schema = getTableSchemaName();
            if (schema != null && !schema.equals("")) {
                sql = sql + schema + "\".\""; 
            }
            sql = sql + getTableName() + "\" ORDER BY \"" + getColumnName()
                + "\" DESC LIMIT 1;"; 
            try {
                statement.execute(sql); 
                ResultSet resultSet = statement.getResultSet();
                resultSet.next();
                return resultSet.getString(getColumnName());
            } catch (Exception e) { //i surrender
                return super.createID(conn, feature, statement);
            }
        }
}
