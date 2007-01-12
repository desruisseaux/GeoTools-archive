package org.geotools.data.postgis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.geotools.data.Transaction;

public class CreatePostgisTableOnlineTest extends PostgisOnlineTestCase {

    private String table1 = "table1";
    
    protected String getFixtureId() {
        return "postgis.typical";
    }

    public void testMakeTable() throws Exception {
        Connection connection = content.getConnection(Transaction.AUTO_COMMIT);    
        Statement st = connection.createStatement();

        dropSequence(st, "table1_fid_seq");
        dropTable(st, table1);
        String sql = "CREATE TABLE " + table1 + "(" + "fid serial NOT NULL,"
                + "name varchar(10), the_geom geometry, " + "CONSTRAINT " + table1
                + "_pkey PRIMARY KEY (fid)" + ") WITHOUT OIDS;";
        st.execute(sql);
    
        //put some data in there
        String[] keys = new String[] {"name", "the_geom"};
        String[] values = new String[] {"'f1'", "GeomFromText('POINT(1294523.17592358 469418.897140173)',4326)"};
        addFeatureManual(st, table1, keys, values);
        values[0] = "'f2'";
        values[1] = "GeomFromText('POINT(1281485.7108 459444.7332)',4326)";
        addFeatureManual(st, table1, keys, values);
        
        //that was fun... let's delete it all
        //dropSequence(st, "table1_fid_seq");
        //dropTable(st, table1);
    }

    protected void addFeatureManual(Statement st, String table, String[] keys, String[] values) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO \"");
        sql.append(table);
        sql.append("\" (");
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append(keys[i]);
        }
        sql.append(") VALUES (");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append(values[i]);
        }
        sql.append(")");
        st.execute(sql.toString());
    }

    protected void dropTable(Statement st, String tableName) throws Exception {
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

    protected void dropSequence(Statement st, String sequenceName) throws Exception {
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
