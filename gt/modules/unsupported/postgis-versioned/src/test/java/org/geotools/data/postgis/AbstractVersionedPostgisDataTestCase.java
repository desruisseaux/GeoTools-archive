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
import java.sql.Statement;

import org.geotools.data.DataTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class AbstractVersionedPostgisDataTestCase extends DataTestCase {
    PostgisTests.Fixture f;

    ConnectionPool pool;

    VersionedPostgisDataStore store;

    PostgisConnectionFactory pcFactory;

    private FeatureType railType;

    private Feature[] railFeatures;

    private Envelope railBounds;

    public AbstractVersionedPostgisDataTestCase(String name) {
        super(name);
    }

    public String getFixtureFile() {
        return "versioned.properties";
    }

    protected void setUp() throws Exception {
        super.setUp();

        f = PostgisTests.newFixture(getFixtureFile());

        pcFactory = new PostgisConnectionFactory(f.host, f.port.intValue(), f.database);
        pool = pcFactory.getConnectionPool(f.user, f.password);

        setUpLakeTable();
        setUpRiverTable();
        setUpRoadTable();
        setUpRailTable();

        // make sure versioned metadata is not in the way
        SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.TBL_TABLESCHANGED, false);
        SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.TBL_VERSIONEDTABLES, false);
        SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.TBL_CHANGESETS, true);
    }

    protected void dataSetUp() throws Exception {
        super.dataSetUp();

        railType = DataUtilities.createType(getName() + ".rail",
                "geom:LineString:nillable");
        railFeatures = new Feature[1];
        // 0,0 +-----------+ 10,10
        railFeatures[0] = railType.create(new Object[] { line(new int[] { 0,0, 10, 10}) },
                "rail.1");
        railBounds = new Envelope();
        railBounds.expandToInclude(railFeatures[0].getBounds());
    }

    protected VersionedPostgisDataStore getDataStore() throws IOException {
        if (store == null) {
            store = buildDataStore();
        }
        return store;
    }

    /**
     * Builds a brand new datastore
     * 
     * @return
     * @throws IOException
     */
    protected VersionedPostgisDataStore buildDataStore() throws IOException {
        VersionedPostgisDataStore ds = new VersionedPostgisDataStore(pool, f.schema, getName(),
                PostgisDataStore.OPTIMIZE_SQL);
        ds.setWKBEnabled(true);
        return ds;
    }

    protected void tearDown() throws Exception {
        store = null;
        if (pcFactory != null && pool != null) {
            pcFactory.free(pool);
            pcFactory = null;
            pool.close();
        }
        super.tearDown();
    }

    protected void setUpRoadTable() throws Exception {
        Connection conn = pool.getConnection();
        conn.setAutoCommit(true);

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + f.schema + "','road','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE " + f.schema + ".road");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            // postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE " + f.schema
                    + ".road (fid varchar PRIMARY KEY, id int ) WITH OIDS");
            s.execute("SELECT AddGeometryColumn('" + f.schema
                    + "', 'road', 'geom', 0, 'LINESTRING', 2);");
            s.execute("ALTER TABLE " + f.schema + ".road add name varchar;");

            for (int i = 0; i < roadFeatures.length; i++) {
                Feature feature = roadFeatures[i];

                // strip out the road.
                String fid = feature.getID().substring("road.".length());
                String ql = "INSERT INTO " + f.schema + ".road (fid,id,geom,name) VALUES (" + "'"
                        + fid + "'," + feature.getAttribute("id") + "," + "GeometryFromText('"
                        + ((Geometry) feature.getAttribute("geom")).toText() + "', 0 )," + "'"
                        + feature.getAttribute("name") + "')";

                s.execute(ql);
            }
        } finally {
            conn.close();
        }
    }

    protected void setUpLakeTable() throws Exception {
        Connection conn = pool.getConnection();
        conn.setAutoCommit(true);

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + f.schema + "','lake','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE " + f.schema + ".lake");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            // postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE " + f.schema + ".lake ( id int ) WITH OIDS");
            s.execute("SELECT AddGeometryColumn('" + f.schema
                    + "', 'lake', 'geom', 0, 'POLYGON', 2);");
            s.execute("ALTER TABLE " + f.schema + ".lake add name varchar;");

            for (int i = 0; i < lakeFeatures.length; i++) {
                Feature feature = lakeFeatures[i];

                // strip out the lake.
                String ql = "INSERT INTO " + f.schema + ".lake (id,geom,name) VALUES ("
                        + feature.getAttribute("id") + "," + "GeometryFromText('"
                        + ((Geometry) feature.getAttribute("geom")).toText() + "', 0 )," + "'"
                        + feature.getAttribute("name") + "')";

                s.execute(ql);
            }
        } finally {
            conn.close();
        }
    }

    protected void setUpRailTable() throws Exception {
        Connection conn = pool.getConnection();
        conn.setAutoCommit(true);

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + f.schema + "','rail','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE " + f.schema + ".rail");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            // postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE " + f.schema + ".rail ( id serial primary key ) WITH OIDS");
            s.execute("SELECT AddGeometryColumn('" + f.schema
                    + "', 'rail', 'geom', 0, 'LINESTRING', 2);");

            for (int i = 0; i < railFeatures.length; i++) {
                Feature feature = railFeatures[i];

                // strip out the lake.
                String ql = "INSERT INTO " + f.schema + ".rail (geom) VALUES ("
                        + "GeometryFromText('"
                        + ((Geometry) feature.getAttribute("geom")).toText() + "', 0 ))";

                s.execute(ql);
            }
        } finally {
            conn.close();
        }
    }

    protected void killTestTables() throws Exception {
        Connection conn = pool.getConnection();

        try {
            Statement s = conn.createStatement();

            try {
                s.execute("SELECT dropgeometrycolumn( '" + f.schema + "','road','geom')");
            } catch (Exception ignore) {
            }

            try {
                s.execute("SELECT dropgeometrycolumn( '" + f.schema + "','river','geom')");
            } catch (Exception ignore) {
            }

            try {
                s.execute("SELECT dropgeometrycolumn( '" + f.schema + "','lake','geom')");
            } catch (Exception ignore) {
            }
            
            try {
                s.execute("SELECT dropgeometrycolumn( '" + f.schema + "','rail','geom')");
            } catch (Exception ignore) {
            }

            try {
                s.execute("DROP TABLE " + f.schema + ".road");
            } catch (Exception ignore) {
            }

            try {
                s.execute("DROP TABLE " + f.schema + ".river");
            } catch (Exception ignore) {
            }

            try {
                s.execute("DROP TABLE " + f.schema + ".lake");
            } catch (Exception ignore) {
            }
            
            try {
                s.execute("DROP TABLE " + f.schema + ".rail");
            } catch (Exception ignore) {
            }

        } finally {
            conn.close();
        }
    }

    protected void setUpRiverTable() throws Exception {
        Connection conn = pool.getConnection();

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + f.schema + "','river','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE " + f.schema + ".river");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            // postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE " + f.schema
                    + ".river(fid varchar PRIMARY KEY, id int) WITH OIDS");
            s.execute("SELECT AddGeometryColumn('" + f.schema
                    + "', 'river', 'geom', 0, 'MULTILINESTRING', 2);");
            s.execute("ALTER TABLE " + f.schema + ".river add river varchar");
            s.execute("ALTER TABLE " + f.schema + ".river add flow float8");

            for (int i = 0; i < riverFeatures.length; i++) {
                Feature feature = riverFeatures[i];
                String fid = feature.getID().substring("river.".length());
                s
                        .execute("INSERT INTO " + f.schema
                                + ".river (fid, id, geom, river, flow) VALUES (" + "'" + fid + "',"
                                + feature.getAttribute("id") + "," + "GeometryFromText('"
                                + feature.getAttribute("geom").toString() + "', 0 )," + "'"
                                + feature.getAttribute("river") + "',"
                                + feature.getAttribute("flow") + ")");
            }
        } finally {
            conn.close();
        }
    }
}
