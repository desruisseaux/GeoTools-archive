/*
 * Created on 16-ago-2004
 */
package org.geotools.index.rtree.database.mysql;

import org.geotools.index.rtree.database.AbstractDialect;


/**
 * @author Tommaso Nolli
 */
public class MySqlDialect extends AbstractDialect {
    
    /**
     * @see org.geotools.index.rtree.database.Dialect#getCreateTable(java.lang.String)
     */
    public String getCreateTable(String tableName) {
        return "create table " + tableName + "(" +
               "page_id int not null," +
               "fl_leaf char(1) not null," +
               "blob_content blob";
    }
    
}
