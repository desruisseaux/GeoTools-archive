/*
 * Created on 16-ago-2004
 */
package org.geotools.index.rtree.database;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * @author Tommaso Nolli
 */
public interface Dialect {

    public String getCatalogQuery();
    
    public String getCatalogInsert();
    
    public String getCreateTable(String tableName);
    
    public int getNextPageId(Connection cnn, String tableName)
    throws SQLException;

    public String getSelectPage(String tableName);
    
    public String getInsertPage(String tableName);
    
    public String getUpdatePage(String tableName);
    
}
