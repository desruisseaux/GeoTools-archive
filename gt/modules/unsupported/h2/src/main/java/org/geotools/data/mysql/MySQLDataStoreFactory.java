package org.geotools.data.mysql;

import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;

import java.util.Map;
import java.util.Iterator;
import java.io.IOException;

/**
 * DataStoreFactory for MySQL database.
 * 
 * @author David Winslow, The Open Planning Project
 *
 */
public class MySQLDataStoreFactory extends JDBCDataStoreFactory{

    protected SQLDialect createSQLDialect(){
        return new MySQLDialect();
    }

    protected String getDriverClassName(){
        return "com.mysql.jdbc.Driver";
    }
    
    protected String getDatabaseID(){
        return "mysql";
    }

    public String getDescription(){
        return "MySQL Database";
    }
}
