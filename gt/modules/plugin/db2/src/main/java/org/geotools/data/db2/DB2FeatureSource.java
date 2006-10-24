/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005. All rights reserved.
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
package org.geotools.data.db2;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSourceException;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.JDBCFeatureSource;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;


/**
 * DB2 Feature Source implementation.  Overrides functionality in
 * JDBCFeatureSource to provide more efficient or more appropriate
 * DB2-specific implementation.
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2FeatureSource extends JDBCFeatureSource {

    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.db2");

    /**
     * Constructs a feature source based on a DB2 data store for a specified
     * feature type.
     *
     * @param dataStore
     * @param featureType
     */
    public DB2FeatureSource(DB2DataStore dataStore, FeatureType featureType) {
        super(dataStore, featureType);

    }

    /**
     * Closes everything associated with a query, the ResultSet, Statement and
     * Connection.
     *
     * @param rs the ResultSet
     * @param stmt the Statement
     * @param conn the Connection
     * @param transaction the Transaction
     * @param e the SQLException, if any, or null
     */
    protected void closeAll(ResultSet rs, Statement stmt, Connection conn,
        Transaction transaction, SQLException e) {
        close(rs);
        close(stmt);
        close(conn, transaction, e);
    }

    /**
     * Gets the bounds of the feature using the specified query.
     *
     * @param query a query object.
     *
     * @return the envelope representing the bounds of the features.
     *
     * @throws IOException if there was an encoder problem.
     * @throws DataSourceException if there was an error executing the query to
     *         get the bounds.
     */
    public Envelope getBounds(Query query) throws IOException {
        Envelope env = new Envelope();
        CoordinateReferenceSystem crs = null;

        if (getSchema() != null) {
            String typeName = getSchema().getTypeName();
            GeometryAttributeType geomType = getSchema()
                .getDefaultGeometry();

            if (query.getFilter() != Filter.ALL) {
                String sqlStmt = null;

                try {
                    DB2SQLBuilder builder = (DB2SQLBuilder) ((DB2DataStore)this.getDataStore())
                        .getSqlBuilder(typeName);
                    sqlStmt = builder.buildSQLBoundsQuery(typeName, geomType,
                            query.getFilter());
                } catch (SQLEncoderException e) {
                    throw new IOException("SQLEncoderException: " + e);
                }

                Connection conn = null;
                Transaction transaction = null;
                Statement statement = null;
                ResultSet results = null;

                try {
                    conn = getConnection();
                    transaction = getTransaction();
                    statement = conn.createStatement();
                    results = statement.executeQuery(sqlStmt);

                    if (results.next()) {
                        double minx = results.getDouble(1);
                        double miny = results.getDouble(2);
                        double maxx = results.getDouble(3);
                        double maxy = results.getDouble(4);
                        env = new Envelope(minx, maxx, miny, maxy);
                    } else {
                        env = new Envelope();
                    }
                } catch (SQLException e) {
                    closeAll(results, statement, conn, transaction, e);
                    System.out.println(e);
                    throw new DataSourceException("Could not get bounds "
                        + query.getHandle(), e);
                }

                closeAll(results, statement, conn, transaction, null);
            }

            crs = geomType.getCoordinateSystem();
            env = new ReferencedEnvelope(env, crs);
        }

        LOGGER.finer("Bounds: " + env.toString());

        return env;
    }
    
}
