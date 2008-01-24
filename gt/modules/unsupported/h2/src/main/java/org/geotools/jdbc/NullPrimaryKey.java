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

    public String generate(Connection cx, SQLDialect dialect)
        throws Exception {
        return SimpleFeatureBuilder.createDefaultFeatureId();
    }
}
