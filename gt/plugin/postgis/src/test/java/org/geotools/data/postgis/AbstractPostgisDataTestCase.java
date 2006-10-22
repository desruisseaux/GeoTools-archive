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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

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
	PostgisConnectionFactory pcFactory;
	
	public AbstractPostgisDataTestCase(String name) {
		super(name);
	}

	public String getFixtureFile() {
		return "fixture.properties";
	}
	
    protected void setUp() throws Exception {
        super.setUp();

        f = PostgisTests.newFixture(getFixtureFile());
        
        pcFactory = new PostgisConnectionFactory(f.host,
                f.port.intValue(), f.database);
        pool = pcFactory.getConnectionPool(f.user, f.password);

        setUpRoadTable();
        setUpRiverTable();
        setUpLakeTable();

        if (CHECK_TYPE) {
            checkTypesInDataBase();
            CHECK_TYPE = false; // just once
        }

        	
        data = new PostgisDataStore(pool, f.schema, getName(),
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
        if (pcFactory != null && pool != null) {
        	pcFactory.free(pool);
        	pcFactory = null;
        	pool.close();
        }
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
            s.execute("SELECT dropgeometrycolumn( '" + f.schema
                + "','road','geom')");
        } catch (Exception ignore) {}

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE " + f.schema + ".road");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            //postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE " + f.schema + ".road (fid varchar PRIMARY KEY, id int )");
            s.execute("SELECT AddGeometryColumn('" + f.schema
                + "', 'road', 'geom', 0, 'LINESTRING', 2);");
            s.execute("ALTER TABLE " + f.schema + ".road add name varchar;");

            for (int i = 0; i < roadFeatures.length; i++) {
                Feature feature = roadFeatures[i];

                //strip out the road. 
                String fid = feature.getID().substring("road.".length());
                String ql = "INSERT INTO " + f.schema + ".road (fid,id,geom,name) VALUES ("
                    + "'" + fid + "'," + feature.getAttribute("id") + ","
                    + "GeometryFromText('"
                    + ((Geometry) feature.getAttribute("geom")).toText() + "', 0 ),"
                    + "'" + feature.getAttribute("name") + "')";

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
            s.execute("SELECT dropgeometrycolumn( '" + f.schema
                + "','lake','geom')");
        } catch (Exception ignore) {}

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE " + f.schema + ".lake");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            //postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE " + f.schema + ".lake ( id int )");
            s.execute("SELECT AddGeometryColumn('" + f.schema
                + "', 'lake', 'geom', 0, 'POLYGON', 2);");
            s.execute("ALTER TABLE " + f.schema + ".lake add name varchar;");
            		

            for (int i = 0; i < lakeFeatures.length; i++) {
                Feature feature = lakeFeatures[i];

                //strip out the lake. 
                String ql = "INSERT INTO " + f.schema + ".lake (id,geom,name) VALUES ("
                    + feature.getAttribute("id") + "," + "GeometryFromText('"
                    + ((Geometry) feature.getAttribute("geom")).toText() + "', 0 ),"
                    + "'" + feature.getAttribute("name") + "')";

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
	            s.execute("SELECT dropgeometrycolumn( '" + f.schema
	                + "','road','geom')");
	        } catch (Exception ignore) {}
	        
	        try {
	            s.execute("SELECT dropgeometrycolumn( '" + f.schema
	                + "','river','geom')");
	        } catch (Exception ignore) {}
	
	        try {
	            s.execute("SELECT dropgeometrycolumn( '" + f.schema
	                + "','lake','geom')");
	        } catch (Exception ignore) {}
	        
	        try {
	        	s.execute("DROP TABLE " + f.schema + ".road");
	        } catch (Exception ignore) {}
	        
	        try {    
	            s.execute("DROP TABLE " + f.schema + ".river");
	        } catch (Exception ignore) {}
	        
	        try {    
	            s.execute("DROP TABLE " + f.schema + ".lake");
	        } catch (Exception ignore) {}
        
        }
	    finally {
            conn.close();
        }
    }

    protected void setUpRiverTable() throws Exception {
        Connection conn = pool.getConnection();

        try {
            Statement s = conn.createStatement();
            s.execute("SELECT dropgeometrycolumn( '" + f.schema
                + "','river','geom')");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();
            s.execute("DROP TABLE " + f.schema + ".river");
        } catch (Exception ignore) {
        }

        try {
            Statement s = conn.createStatement();

            //postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            s.execute("CREATE TABLE " + f.schema + ".river(fid varchar PRIMARY KEY, id int)");
            s.execute("SELECT AddGeometryColumn('" + f.schema
                + "', 'river', 'geom', 0, 'MULTILINESTRING', 2);");
            s.execute("ALTER TABLE " + f.schema + ".river add river varchar");
            s.execute("ALTER TABLE " + f.schema + ".river add flow float8");

            for (int i = 0; i < riverFeatures.length; i++) {
                Feature feature = riverFeatures[i];
                String fid = feature.getID().substring("river.".length());
                s.execute(
                    "INSERT INTO " + f.schema + ".river (fid, id, geom, river, flow) VALUES ("
                    + "'" + fid + "'," + feature.getAttribute("id") + ","
                    + "GeometryFromText('" + feature.getAttribute("geom").toString()
                    + "', 0 )," + "'" + feature.getAttribute("river") + "',"
                    + feature.getAttribute("flow") + ")");
            }
        } finally {
            conn.close();
        }
    }

   

}
