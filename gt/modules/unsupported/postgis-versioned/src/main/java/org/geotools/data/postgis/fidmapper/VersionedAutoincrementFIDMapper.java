/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
import java.sql.Statement;
import java.sql.Types;

import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.fidmapper.MultiColumnFIDMapper;
import org.geotools.feature.Feature;

/**
 * Covers both the basic and multicolumn fid mappers
 * 
 * @author aaime
 * @since 2.4
 * 
 */
class VersionedAutoincrementFIDMapper extends MultiColumnFIDMapper implements VersionedFIDMapper {
    protected PostGISAutoIncrementFIDMapper autoIncrementMapper;

    public VersionedAutoincrementFIDMapper(String tableSchemaName, String tableName,
            String colName, int colType, int colSize) {
        super(tableSchemaName, tableName, new String[] { colName, "revision" }, new int[] {
                colType, Types.NUMERIC }, new int[] { colSize, 8 }, new int[] { 0, 0 },
                new boolean[] { true, false });
        returnFIDColumnsAsAttributes = true;
        autoIncrementMapper = new PostGISAutoIncrementFIDMapper(tableName, colName, colType);
    }

    public String getUnversionedFid(String versionedFID) {
        // we assume revision is the last column, since it has been added with
        // an alter table "add". Also, we make the fid "typed" to ensure WFS keeps on working
        return tableName + "." + versionedFID.substring(0, versionedFID.lastIndexOf('&'));
    }

    public Object[] getUnversionedPKAttributes(String FID) throws IOException {
        // check we can parse this
        if (!FID.startsWith(tableName + "."))
            throw new DataSourceException("The FID is invalid, should start with '" + tableName
                    + ".'");

        // leverate superclass parsing, then throw away the last element
        Object[] values = getPKAttributes(FID.substring(tableName.length() + 1) + "&0");
        Object[] unversioned = new Object[values.length - 1];
        System.arraycopy(values, 0, unversioned, 0, unversioned.length);
        return unversioned;
    }

    public String createID(Connection conn, Feature feature, Statement statement)
            throws IOException {
        if (feature.getAttribute(colNames[0]) == null) {
            try {
                String id = autoIncrementMapper.createID(conn, feature, statement);
                feature.setAttribute(colNames[0], new Long(id));
            } catch (Exception e) {
                throw new DataSourceException("Could not generate key for the "
                        + "unset primary key column " + colNames[0], e);
            }
        }
        return super.createID(conn, feature, statement);
    }

}
