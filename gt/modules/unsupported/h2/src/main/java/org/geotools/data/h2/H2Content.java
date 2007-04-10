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
package org.geotools.data.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.ConnectionPoolDataSource;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.geotools.catalog.GeoResourceInfo;
import org.geotools.data.store.Content;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureList;


public class H2Content extends Content {
    /**
     * connection pool
     */
    ConnectionPoolDataSource connectionPoolDataSource;

    /**
     * The database schema.
     */
    String databaseSchema;

    public H2Content(ConnectionPoolDataSource connectionPoolDataSource) {
        this.connectionPoolDataSource = connectionPoolDataSource;
    }

    void setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
    }

    String getDatabaseSchema() {
        return databaseSchema;
    }

    ConnectionPoolDataSource getConnectionPoolDataSource() {
        return connectionPoolDataSource;
    }

    /**
     * Convenience method for grabbing a new connection.
     * <p>
     * Callers of this method should close the connection when done with it.
     * </p>.
     *
     */
    Connection connection() throws SQLException {
        return getConnectionPoolDataSource().getPooledConnection().getConnection();
    }

    public ContentEntry entry(ContentDataStore dataStore, TypeName typeName) {
        return new H2ContentEntry((H2DataStore) dataStore, typeName);
    }

    public ContentState state(ContentEntry entry) {
        return new H2ContentState(entry);
    }

    public FeatureCollection all(ContentState state) {
        return new H2FeatureCollectionAll((H2ContentState) state);
    }

    public FeatureCollection filter(ContentState state, Filter filter) {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureCollection readonly(ContentState state, Filter filter) {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureList sorted(ContentState state, Filter filter, List order) {
        // TODO Auto-generated method stub
        return null;
    }

    public List getTypeNames() throws IOException {
        try {
            return H2Utils.typeNames(this);
        } catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    public GeoResourceInfo info(ContentState state) {
        // TODO Auto-generated method stub
        return null;
    }
}
