/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005. All rights reserved.
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
package org.geotools.data.db2;

import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.jdbc.fidmapper.FIDMapperFactory;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * Exercise DB2FIDMapperFactory.
 *
 * @author David Adler - IBM Corporation
 */
public class DB2FIDMapperFactoryTest extends DB2TestCase {
    public void testMappers() throws Exception {
        String catalog = null;
        String schema = null;
        String tableName = null;
        Connection conn = null;
        FIDMapper fm;
        String wrapperDesc = null;
        FIDMapperFactory fmFact = new DB2FIDMapperFactory();

        conn = getLocalConnection();
        tableName = "FIDAUTOINC";
        fm = fmFact.getMapper(catalog, schema, tableName, conn);
        wrapperDesc = toString(fm);
        assertEquals(tableName, wrapperDesc,
            "class org.geotools.data.jdbc.fidmapper.AutoIncrementFIDMapper:1:IDCOL:4:-1:-1:false:true:");

        conn = getLocalConnection();
        tableName = "FIDCHARPRIKEY";
        fm = fmFact.getMapper(catalog, schema, tableName, conn);
        wrapperDesc = toString(fm);
        assertEquals(tableName, wrapperDesc,
            "class org.geotools.data.jdbc.fidmapper.BasicFIDMapper:1:IDCOL:12:32:32:true:false:");

        conn = getLocalConnection();
        tableName = "FIDNOPRIKEY";
        fm = fmFact.getMapper(catalog, schema, tableName, conn);
        wrapperDesc = toString(fm);
        assertEquals(tableName, wrapperDesc,
            "class org.geotools.data.db2.DB2NullFIDMapper:0::false:false:");

        conn = getLocalConnection();
        tableName = "FIDINTPRIKEY";
        fm = fmFact.getMapper(catalog, schema, tableName, conn);
        wrapperDesc = toString(fm);
        assertEquals(tableName, wrapperDesc,
            "class org.geotools.data.jdbc.fidmapper.MaxIncFIDMapper:1:IDCOL:12:255:255:true:false:");

        conn = getLocalConnection();
        tableName = "FIDVCHARPRIKEY";
        fm = fmFact.getMapper(catalog, schema, tableName, conn);
        wrapperDesc = toString(fm);
        assertEquals(tableName, wrapperDesc,
            "class org.geotools.data.jdbc.fidmapper.BasicFIDMapper:1:IDCOL:12:32:32:true:false:");

        conn = getLocalConnection();
        tableName = "FIDMCOLPRIKEY";
        fm = fmFact.getMapper(catalog, schema, tableName, conn);
        wrapperDesc = toString(fm);
        assertEquals(tableName, wrapperDesc,
            "class org.geotools.data.jdbc.fidmapper.MultiColumnFIDMapper:2:IDCOL1:1:32:32:true:false:");

        // Don't know why, but DefaultFIDFactory.getPkColumnInfo closes the connection
        conn = getLocalConnection();

        try {
            tableName = "NoTable";
            fm = fmFact.getMapper(catalog, schema, tableName, conn);
            fail("Didn't get exception on invalid tableName");
        } catch (SchemaNotFoundException e) {
            assertEquals("Unexpected exception", e.getMessage(),
                "Feature type could not be found for NoTable");
        } catch (IOException e) {
            fail("Unexpected exception: " + e);
        }

        try {
            conn.close();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    String toString(FIDMapper fm) {
        String mapperName = ((TypedFIDMapper) fm).getWrappedMapper().getClass()
                             .toString();
        String colInfo = "";

        if (fm.getColumnCount() > 0) {
            colInfo = fm.getColumnName(0) + ":" + fm.getColumnType(0) + ":"
                + fm.getColumnSize(0) + ":" + fm.getColumnDecimalDigits(0);
        }

        String s = mapperName + ":" + fm.getColumnCount() + ":" + colInfo + ":"
            + fm.returnFIDColumnsAsAttributes() + ":"
            + fm.hasAutoIncrementColumns() + ":" + "";

        return s;
    }
}
