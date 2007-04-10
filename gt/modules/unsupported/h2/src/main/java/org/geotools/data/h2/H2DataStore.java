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
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.ConnectionPoolDataSource;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureType;


public class H2DataStore extends ContentDataStore {
    /**
     * Shared logging instance
     */
    static Logger LOGGER = Logger.getLogger("org.geotools.data");

    public H2DataStore(H2Content content) {
        super(content);
    }

    H2Content content() {
        return (H2Content) getContent();
    }

    /**
     * Returns the connection pool data source used by the datastore.
     */
    ConnectionPoolDataSource getConnectionPoolDataSource() {
        return content().getConnectionPoolDataSource();
    }

    /**
     * The Database schema the data store corresponds to.
     */
    String getDatabaseSchema() {
        return content().getDatabaseSchema();
    }

    /**
     * Convenience method for grabbing a new connection.
     * <p>
     * Callers of this method should close the connection when done with it.
     * </p>.
     *
     */
    Connection connection() throws SQLException {
        return content().connection();
    }

    //
    // data store overrides
    //
    public void createSchema(FeatureType featureType) throws IOException {
        if (entry(name(featureType.getTypeName())) != null) {
            String msg = "Schema '" + featureType.getTypeName() + "' already exists";
            throw new IllegalArgumentException(msg);
        }

        //crete a content entry for the type
        ContentEntry entry = content.entry(this, name(featureType.getTypeName()));
        H2ContentState state = (H2ContentState) entry.getState(Transaction.AUTO_COMMIT);

        //set the feature type
        //TODO: JD - this is kind of hacky, it involves knowing how content 
        // state caches feature types, hmm, something smells fishy...
        state.put(getTypeFactory().getClass(), featureType);

        //create the sql statement to create the table
        H2SQLBuilder sqlBuilder = createSQLBuilder(state);
        String sql = sqlBuilder.create();

        //execute the sql
        Connection conn = null;

        try {
            conn = connection();

            Statement st = conn.createStatement();
            st.execute(sql);

            st.close();
        } catch (SQLException e) {
            throw (IOException) new IOException().initCause(e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }

    public H2SQLBuilder createSQLBuilder(H2ContentState state) {
        return new H2SQLBuilder(state, filterFactory);
    }
}
