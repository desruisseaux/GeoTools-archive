package org.geotools.data.postgis;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.geotools.data.DataTestCase;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.fidmapper.BasicFIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import org.geotools.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

public class AbstractPostgisDataTestCase extends DataTestCase {

	static boolean WKB_ENABLED = true;
	static boolean CHECK_TYPE = false;
	
	PostgisTests.Fixture f;
	ConnectionPool pool;
	PostgisDataStore data;
	
	public AbstractPostgisDataTestCase(String name) {
		super(name);
	}

	public String getFixtureFile() {
		return "fixture.properties";
	}
	
    protected void setUp() throws Exception {
        super.setUp();

        f = PostgisTests.newFixture(getFixtureFile());
        
        PostgisConnectionFactory factory1 = new PostgisConnectionFactory(f.host,
                f.port.intValue(), f.database);
        pool = factory1.getConnectionPool(f.user, f.password);

        setUpRoadTable();
        setUpRiverTable();
        setUpLakeTable();

        if (CHECK_TYPE) {
            checkTypesInDataBase();
            CHECK_TYPE = false; // just once
        }

        data = new PostgisDataStore(pool, "public", getName(),
                PostgisDataStore.OPTIMIZE_SAFE);
        data.setWKBEnabled(WKB_ENABLED);
        data.setFIDMapper("road",
            new TypedFIDMapper(new BasicFIDMapper("fid", 255, false), "road"));
        data.setFIDMapper("river",
            new TypedFIDMapper(new BasicFIDMapper("fid", 255, false), "river"));
        data.setFIDMapper("testset",
            new TypedFIDMapper(new BasicFIDMapper("gid", 255, true), "testset"));
    }
    
    
    protected void tearDown() throws Exception {
        data = null;

        PostgisConnectionFactory factory1 = new PostgisConnectionFactory("hydra",
                "5432", "jody");
        factory1.free(pool);

        //pool.close();
        super.tearDown();
    }
    
    protected void checkTypesInDataBase() throws SQLException {
        Connection conn = pool.getConnection();

        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = 
                //md.getTables( catalog, null, null, null );
                md.getTables(null, "public", "%", new String[] { "TABLE", });
            ResultSetMetaData rsmd = rs.getMetaData();
            int NUM = rsmd.getColumnCount();
            System.out.print(" ");

            for (int i = 1; i <= NUM; i++) {
                System.out.print(rsmd.getColumnName(i));
                System.out.flush();
                System.out.print(":");
                System.out.flush();
                System.out.print(rsmd.getColumnClassName(i));
                System.out.flush();

                if (i < NUM) {
                    System.out.print(",");
                    System.out.flush();
                }
            }

            System.out.println();

            while (rs.next()) {
                System.out.print(rs.getRow());
                System.out.print(":");
                System.out.flush();

                for (int i = 1; i <= NUM; i++) {
                    System.out.print(rsmd.getColumnName(i));
                    System.out.flush();
                    System.out.print("=");
                    System.out.flush();
                    System.out.print(rs.getString(i));
                    System.out.flush();

                    if (i < NUM) {
                        System.out.print(",");
                        System.out.flush();
                    }
                }

                System.out.println();
            }
        } finally {
            conn.close();
        }
    }

    protected void setUpRoadTable() throws Exception {
        Connection conn = pool.getConnection();
        conn.setAutoCommit(true);

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + f.database
                + "','road','geom')");
        } catch (Exception ignore) {}

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE road");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            //postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE road (fid varchar PRIMARY KEY, id int )");
            s.execute("SELECT AddGeometryColumn('" + f.database
                + "', 'road', 'geom', 0, 'LINESTRING', 2);");
            s.execute("ALTER TABLE road add name varchar;");

            for (int i = 0; i < roadFeatures.length; i++) {
                Feature f = roadFeatures[i];

                //strip out the road. 
                String fid = f.getID().substring("road.".length());
                String ql = "INSERT INTO road (fid,id,geom,name) VALUES ("
                    + "'" + fid + "'," + f.getAttribute("id") + ","
                    + "GeometryFromText('"
                    + ((Geometry) f.getAttribute("geom")).toText() + "', 0 ),"
                    + "'" + f.getAttribute("name") + "')";

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
            s.execute("SELECT dropgeometrycolumn( '" + f.database
                + "','lake','geom')");
        } catch (Exception ignore) {}

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE lake");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            //postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE lake ( id int )");
            s.execute("SELECT AddGeometryColumn('" + f.database
                + "', 'lake', 'geom', 0, 'POLYGON', 2);");
            s.execute("ALTER TABLE lake add name varchar;");

            for (int i = 0; i < lakeFeatures.length; i++) {
                Feature f = lakeFeatures[i];

                //strip out the road. 
                String fid = f.getID().substring("lake.".length());
                String ql = "INSERT INTO lake (id,geom,name) VALUES ("
                    + f.getAttribute("id") + "," + "GeometryFromText('"
                    + ((Geometry) f.getAttribute("geom")).toText() + "', 0 ),"
                    + "'" + f.getAttribute("name") + "')";

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
            s.execute("SELECT dropgeometrycolumn( '" + f.database
                + "','road','geom')");
            s.execute("SELECT dropgeometrycolumn( '" + f.database
                + "','river','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE road");
            s.execute("DROP TABLE river");
        } catch (Exception ignore) {
        } finally {
            conn.close();
        }
    }

    protected void setUpRiverTable() throws Exception {
        Connection conn = pool.getConnection();

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + f.database
                + "','river','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE river");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            //postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE river(fid varchar PRIMARY KEY, id int)");
            s.execute("SELECT AddGeometryColumn('" + f.database
                + "', 'river', 'geom', 0, 'MULTILINESTRING', 2);");
            s.execute("ALTER TABLE river add river varchar");
            s.execute("ALTER TABLE river add flow float8");

            for (int i = 0; i < riverFeatures.length; i++) {
                Feature f = riverFeatures[i];
                String fid = f.getID().substring("river.".length());
                s.execute(
                    "INSERT INTO river (fid, id, geom, river, flow) VALUES ("
                    + "'" + fid + "'," + f.getAttribute("id") + ","
                    + "GeometryFromText('" + f.getAttribute("geom").toString()
                    + "', 0 )," + "'" + f.getAttribute("river") + "',"
                    + f.getAttribute("flow") + ")");
            }
        } finally {
            conn.close();
        }
    }

   

}
