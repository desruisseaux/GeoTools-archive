/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.hsql;

import java.util.Collections;
import java.util.Map;

import org.geotools.data.jdbc.JDBCDataStoreConfig;

/**
 * An extension of the JDBC Data Store Config API for the HSQL database platform.
 * This class basically overrides any user input for the Database Schema Name
 * attribute and instead, uses the default DB Schema Name for HSQL.
 * <br>
 * Please see {@link org.geotools.data.jdbc.JDBCDataStoreConfig class} usage details.
 * 
 * @author Amr Alam, Refractions Research, aalam@refractions.net
 */
public class HsqlDataStoreConfig extends JDBCDataStoreConfig {
	/**
     * Construct <code>HsqlDataStoreConfig</code>.
     *
     * @param namespace
     * @param databaseSchemaName
     * @param fidColumnOverrideMap
     * @param fidGenerationMap
     */
    public HsqlDataStoreConfig(String namespace, String databaseSchemaName,
        Map fidColumnOverrideMap, Map fidGenerationMap) {
        this(namespace, databaseSchemaName, Long.MAX_VALUE);
    }

    /**
     * Construct <code>HsqlDataStoreConfig</code>.
     *
     * @param namespace
     * @param databaseSchemaName
     * @param typeHandlerCacheTimeout
     */
    public HsqlDataStoreConfig(String namespace, String databaseSchemaName,
         long typeHandlerCacheTimeout) {
    	//We basically want to set the DB Schema name to null, ie. use the default one.
    	super(namespace, "PUBLIC", typeHandlerCacheTimeout);
    }
    
    /**
     * Creates a new JDBCDataStoreConfig with the specified Namespace and Schema
     * names. The Schema name is overridden in favor of the HSQL default Schema name.
     * 
     * @param namespace
     * @param schemaName
     * @return
     */
    public static JDBCDataStoreConfig createWithNameSpaceAndSchemaName(
        String namespace, String schemaName) {
        return new JDBCDataStoreConfig(namespace, "PUBLIC",
            Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    }
}
