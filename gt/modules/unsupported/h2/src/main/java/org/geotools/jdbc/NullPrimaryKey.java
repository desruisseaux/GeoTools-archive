package org.geotools.jdbc;

import java.sql.Connection;

import org.geotools.feature.simple.SimpleFeatureBuilder;


/**
 * Primary key for tables which do not have a primary key.
 * <p>
 * New key values are generated "from thin air" and are not persistent.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class NullPrimaryKey extends PrimaryKey {

    protected NullPrimaryKey(String tableName, String columnName, Class type) {
        super(tableName, columnName, type);
    }

    public String generate(Connection cx, SQLDialect dialect) throws Exception {
        return SimpleFeatureBuilder.createDefaultFeatureId();
    }

}
