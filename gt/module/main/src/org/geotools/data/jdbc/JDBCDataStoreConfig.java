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
/* $Id: JDBCDataStoreConfig.java,v 1.1.2.2 2004/04/18 09:19:43 aaime Exp $
 *
 * Created on 8/01/2004
 */
package org.geotools.data.jdbc;

import java.util.Collections;
import java.util.Map;


/**
 * Configuration object for JDBCDataStore.
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: aaime $
 * @version $Id: JDBCDataStoreConfig.java,v 1.1.2.2 2004/04/18 09:19:43 aaime Exp $ Last Modified: $Date: 2004/04/18 09:19:43 $
 */
public class JDBCDataStoreConfig {
    public static final String FID_GEN_INSERT_NULL = "INSERT_NULL";
    public static final String FID_GEN_MANUAL_INC = "MANUAL_INC";
    public static final String DEFAULT_FID_GEN_KEY = "DEFAULT_GEN";
    public static final String DEFAULT_FID_GEN = FID_GEN_INSERT_NULL;
    private final String namespace;
    private final String databaseSchemaName;
    protected final long typeHandlerCacheTimout;

    public JDBCDataStoreConfig() {
        this(null, null, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    }

    public JDBCDataStoreConfig(String namespace, String databaseSchemaName,
        Map fidColumnOverrideMap, Map fidGenerationMap) {
        this(namespace, databaseSchemaName, Long.MAX_VALUE);
    }

    public JDBCDataStoreConfig(String namespace, String databaseSchemaName,
         long typeHandlerCacheTimeout) {
        this.namespace = namespace;
	if (databaseSchemaName == null || databaseSchemaName.equals("")) {
	    this.databaseSchemaName = null;
	} else {
	    this.databaseSchemaName = databaseSchemaName;
        }
	this.typeHandlerCacheTimout = typeHandlerCacheTimeout;
    }

    public static JDBCDataStoreConfig createWithNameSpaceAndSchemaName(
        String namespace, String schemaName) {
        return new JDBCDataStoreConfig(namespace, schemaName,
            Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    }

    public static JDBCDataStoreConfig createWithSchemaNameAndFIDGenMap(
        String schemaName, Map fidGenerationMap) {
        return new JDBCDataStoreConfig(null, schemaName, Collections.EMPTY_MAP,
            fidGenerationMap);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the databaseSchemaName.
     */
    public String getDatabaseSchemaName() {
        return databaseSchemaName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return
     */
    public long getTypeHandlerTimeout() {
        return typeHandlerCacheTimout;
    }

}
