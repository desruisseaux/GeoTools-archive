package org.geotools.data.postgis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostgisTests.Fixture;

/**
 * Sets up various dummy tables/sequences, for extension.
 * 
 * @author Cory Horner, Refractions Research
 */
public class AbstractPostgisOnlineTestCase extends TestCase {
    
    protected PostgisDataStore ds;
    
    /** simple table with serial (int4) primary key */
    final protected String table1 = "tmp_pgtest1";
    /** simple table with int4 primary key and sequence as default value */
    final protected String table2 = "tmp_pgtest2";
    /** simple table with bigserial (int8) primary key */
    final protected String table3 = "tmp_pgtest3";
    /** simple table with int8 primary key and sequence as default value */
    final protected String table4 = "tmp_pgtest4";
    /** simple table with serial (int4) primary key, WITHOUT OIDS, and space in name */
    final protected String table5 = "tmp_pgtest 5";
    /** simple table with int4 primary key, sequence as default value, WITHOUT OIDS, and space in name */
    final protected String table6 = "tmp_pgtest 6";
    
    Fixture fixture;
    
    protected void setUp() throws Exception {
        super.setUp();
        //connect
        fixture = PostgisTests.newFixture("fixture.properties");
        Map params = PostgisTests.getParams(fixture);
        ds = (PostgisDataStore) DataStoreFinder.getDataStore(params);
        
        //create dummy tables
        Statement st = getConnection().createStatement();
        dropTables(st);
        createTables(st);
        st.close();
    }
    
    public Connection getConnection() throws SQLException {
        return ds.getConnectionPool().getConnection();
    }
    
    protected void tearDown() throws Exception {
        Statement st = getConnection().createStatement();
        dropTables(st);
        st.close();
        //ds.getConnectionPool().close(); //is this killing our other tests?
        super.tearDown();
    }
    
    protected void createTables(Statement st) throws SQLException {
        //NOTE: no geometry columns!!!
        createTable1(st);
        createTable2(st);
        createTable3(st);
        createTable4(st);
        createTable5(st);
        createTable6(st);
    }
    
    protected void createTable1(Statement st) throws SQLException {
        String sql = "CREATE TABLE " + table1 + "(" + "fid serial NOT NULL,"
                + "name varchar(10)," + "CONSTRAINT " + table1
                + "_pkey PRIMARY KEY (fid)" + ") WITH OIDS;";
        st.execute(sql);
    }

    protected void createTable2(Statement st) throws SQLException {
        String sql = "CREATE SEQUENCE " + table2
                + "_fid_seq INCREMENT 1 MINVALUE 1 "
                + "MAXVALUE 9223372036854775807 START 1001 CACHE 1;"
                + "CREATE TABLE " + table2 + "("
                + "fid int4 NOT NULL DEFAULT nextval('" + table2
                + "_fid_seq'::text)," + "name varchar(10)," + "CONSTRAINT "
                + table2 + "_pkey PRIMARY KEY (fid)" + ") WITH OIDS;";
        st.execute(sql);
    }

    protected void createTable3(Statement st) throws SQLException {
        String sql = "CREATE TABLE " + table3 + "(" + "fid bigserial NOT NULL,"
                + "name varchar(10)," + "CONSTRAINT " + table3
                + "_pkey PRIMARY KEY (fid)" + ") WITH OIDS;";
        st.execute(sql);
    }

    protected void createTable4(Statement st) throws SQLException {
        String sql = "CREATE SEQUENCE " + table4
                + "_fid_seq INCREMENT 1 MINVALUE 1 "
                + "MAXVALUE 9223372036854775807 START 1000001 CACHE 1;"
                + "CREATE TABLE " + table4 + "("
                + "fid int8 NOT NULL DEFAULT nextval('" + table4
                + "_fid_seq'::text)," + "name varchar(10)," + "CONSTRAINT "
                + table4 + "_pkey PRIMARY KEY (fid)" + ") WITH OIDS;";
        st.execute(sql);
    }

    protected void createTable5(Statement st) throws SQLException{
        String sql = "CREATE TABLE \"" + table5 + "\" ("
                + "fid serial NOT NULL," + "name varchar(10),"
                + "CONSTRAINT \"" + table5 + "_pkey\" PRIMARY KEY (fid)"
                + ") WITHOUT OIDS;";
        st.execute(sql);
    }

    protected void createTable6(Statement st) throws SQLException {
        String sql = "CREATE SEQUENCE \"" + table6
                + "_fid_seq\" INCREMENT 1 MINVALUE 1 "
                + "MAXVALUE 9223372036854775807 START 1001 CACHE 1;"
                + "CREATE TABLE \"" + table6 + "\" ("
                + "fid int4 NOT NULL DEFAULT nextval('\"" + table6
                + "_fid_seq\"'::text)," + "name varchar(10)," + "CONSTRAINT \""
                + table6 + "_pkey\" PRIMARY KEY (fid)" + ") WITHOUT OIDS;";
        st.execute(sql);
    }

    protected void dropTables(Statement st) throws SQLException {
        dropTable(st, table1);
        dropTable(st, table2);
        dropSequence(st, table2 + "_fid_seq");
        dropTable(st, table3);
        dropTable(st, table4);
        dropSequence(st, table4 + "_fid_seq");
        dropTable(st, table5);
        dropTable(st, table6);
        dropSequence(st, table6 + "_fid_seq");
    }
    
    protected void dropTable(Statement st, String tableName) throws SQLException {
        String sql = "SELECT COUNT(tablename) FROM pg_tables WHERE tablename = '" + tableName + "'";
        ResultSet rs = st.executeQuery(sql);
        rs.next();
        int exists = rs.getInt(1);
        rs.close();
        if (exists > 0) {
            sql = "DROP TABLE \"" + tableName + "\"";
            st.execute(sql);
        }
    }

    protected void dropSequence(Statement st, String sequenceName) throws SQLException {
        String sql = "SELECT COUNT(relid) FROM pg_statio_all_sequences WHERE relname = '" + sequenceName + "'";
        ResultSet rs = st.executeQuery(sql);
        rs.next();
        int exists = rs.getInt(1);
        rs.close();
        if (exists > 0) {
            sql = "DROP SEQUENCE \"" + sequenceName + "\"";
            st.execute(sql);
        }
    }
}
