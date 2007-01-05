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
package org.geotools.data.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.jdbc.JDBCUtils;

public class SupportTableOnlineTest extends AbstractVersionedPostgisDataTestCase {

    public SupportTableOnlineTest(String name) {
        super(name);
    }

    public void testTableCreation() throws IOException, SQLException {
        buildDataStore();
        // now the tables should be there
        Connection conn = null;
        ResultSet tables = null;
        try {
            boolean changeSets = false;
            boolean tablesChanged = false;
            boolean versionedTables = false;
            conn = pool.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            String[] tableType = { "TABLE" };
            tables = meta.getTables(null, f.schema, "%", tableType);
            while (tables.next()) {
                String tableName = tables.getString(3);
                if (tableName.equals(VersionedPostgisDataStore.CHANGESETS))
                    changeSets = true;
                if (tableName.equals(VersionedPostgisDataStore.TABLESCHANGED))
                    tablesChanged = true;
                if (tableName.equals(VersionedPostgisDataStore.VERSIONEDTABLES))
                    versionedTables = true;
            }
            tables.close();

            assertTrue(changeSets);
            assertTrue(tablesChanged);
            assertTrue(versionedTables);
        } finally {
            JDBCUtils.close(tables);
            JDBCUtils.close(conn, null, null);
        }
    }

    public void testPreexistentTables() throws IOException, SQLException {
        // first call should create them, second one should not fail
        buildDataStore();
        buildDataStore();
    }

    public void testTablesInTheWay() throws IOException, SQLException {
        Connection conn = null;
        Statement st = null;
        try {
            conn = pool.getConnection();
            st = conn.createStatement();
            st.execute("CREATE TABLE " + VersionedPostgisDataStore.CHANGESETS
                    + "(ID SERIAL, STUFF VARCHAR(20))");
            try {
                buildDataStore();
                fail("Should have failed because of the pre-existing but alone version support table");
            } catch (IOException e) {
                // ok
            }
            SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.CHANGESETS, true);

            st.execute("CREATE TABLE "
                    + VersionedPostgisDataStore.TABLESCHANGED
                    + "(ID SERIAL, STUFF VARCHAR(20))");
            try {
                buildDataStore();
                fail("Should have failed because of the pre-existing but alone version support table");
            } catch (IOException e) {
                // ok
            }
            SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.TABLESCHANGED, false);

            st.execute("CREATE TABLE "
                    + VersionedPostgisDataStore.VERSIONEDTABLES
                    + "(ID SERIAL, STUFF VARCHAR(20))");
            try {
                buildDataStore();
                fail("Should have failed because of the pre-existing but alone version support table");
            } catch (IOException e) {
                // ok
            }
            SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.VERSIONEDTABLES, true);
        } finally {
            JDBCUtils.close(st);
            JDBCUtils.close(conn, null, null);
        }
    }
    
}
