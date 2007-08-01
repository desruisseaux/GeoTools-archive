package org.geotools.data.db2;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.geotools.test.OnlineTestCase;

public abstract class AbstractDB2OnlineTestCase extends OnlineTestCase {
    protected DB2DataStore ds;

    protected String getFixtureId() {
            return "db2.localType4";
    }
    
    public Connection getConnection() throws Exception {
    	DataSource d =  ds.getDataSource();
        return d.getConnection();
    }
    /**
     * Local utility method to make a copy of a Map object.
     * 
     * <p>
     * Used to make copies of a HashMap of Param objects but should work for
     * any Map.
     * </p>
     *
     * @param params an arbitrary Map object
     *
     * @return a copy of the input Map object
     */
    protected Map copyParams(Map params) {
        Map<String, Object> p2 = new HashMap<String, Object>();
        Set keys = params.keySet();
        Iterator it = keys.iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            p2.put(key, params.get(key));
        }

        return p2;
    }
    protected DB2DataStore getDataStore() throws Exception {
        return ds;
    }
    protected void connect() throws Exception {
        ds = (DB2DataStore) new DB2DataStoreFactory().createDataStore(getParams());
        if (ds == null) {
        	throw (new Exception("Datastore not found"));
        }
        resetTables();


    }
    protected void dropTables(Statement st) throws Exception {

    }
    protected void resetTables() throws Exception {
    	String emptyRoadsSQL = "delete from \"Test\".\"Roads\"";
    	String emptyPlacesSQL = "delete from \"Test\".\"Places\"";
    	String insertRoadsSQL = "insert into \"Test\".\"Roads\" select * from \"Test\".\"Roads0\"";   	
    	String insertPlacesSQL = "insert into \"Test\".\"Places\"(\"Name\",\"Geom\") select \"Name\",\"Geom\" from \"Test\".\"Places0\"";   	

    	int rc = 0;
        Statement st = getConnection().createStatement();   
        rc = st.executeUpdate(emptyRoadsSQL);
        rc = st.executeUpdate(emptyPlacesSQL);
        rc = st.executeUpdate(insertRoadsSQL);         
        rc = st.executeUpdate(insertPlacesSQL);        
        st.close();
    }
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();

        params.put(DB2DataStoreFactory.DBTYPE.key, "DB2");
        params.put(DB2DataStoreFactory.HOST.key, fixture
                .getProperty("host"));
        params.put(DB2DataStoreFactory.PORT.key, fixture
                .getProperty("portnum"));
        params.put(DB2DataStoreFactory.TABSCHEMA.key, fixture
                .getProperty("tabschema"));
        params.put(DB2DataStoreFactory.DATABASE.key, fixture
                .getProperty("dbname"));
        params.put(DB2DataStoreFactory.USER.key, fixture
                .getProperty("user"));
        params.put(DB2DataStoreFactory.PASSWD.key, fixture
                .getProperty("password"));


        return params;
    }


    protected void disconnect() throws Exception {
        Statement st = getConnection().createStatement();
        dropTables(st);
        st.close();
        ds = null;
    }
}
