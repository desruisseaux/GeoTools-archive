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
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Geometry;


/**
 * An abstract class that uses prepared statements to insert, update and delete
 * features from the database. Useful when the resultset got from the database
 * is not updatable, and to get peak performance thru the use of prepared
 * statements and batch updates.
 * 
 * <p>
 * Assumptions made by the code:
 * 
 * <ul>
 * <li>
 * if the primary key contains auto-increment columns, it is composed solely of
 * auto-increment columns;
 * </li>
 * <li>
 * the primary key never contains geometric attributes;
 * </li>
 * </ul>
 * </p>
 *
 * @author Andrea Aime
 */
public abstract class JDBCPSFeatureWriter extends JDBCFeatureWriter {
    /** The logger for the jdbc module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.jdbc");
    FIDMapper mapper = null;
    PreparedStatement insertStatement;
    PreparedStatement deleteStatement;
    PreparedStatement updateStatement;

    /**
     * Creates a new instance of JDBCFeatureWriter
     *
     * @param fReader
     * @param queryData
     *
     * @throws IOException
     */
    public JDBCPSFeatureWriter(FeatureReader fReader, QueryData queryData)
        throws IOException {
        super(fReader, queryData);
        mapper = queryData.getMapper();
    }

    /**
     * Override that uses prepared statements to perform the operation.
     *
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#doInsert(org.geotools.data.jdbc.MutableFIDFeature)
     */
    protected void doInsert(MutableFIDFeature current)
        throws IOException, SQLException {
        LOGGER.fine("inserting into database feature " + current);

        // lazily create the insert statement
        if (insertStatement != null) {
            insertStatement = createInsertStatement(queryData.getConnection(),
                    queryData.getFeatureType());
        }

        try {
            fillInsertParameters(insertStatement, current);
            insertStatement.executeUpdate();

            // should the ID be generated during an insert, we need to read it back
            // and set it into the feature
            if (((mapper.getColumnCount() > 0)
                    && mapper.hasAutoIncrementColumns())) {
                current.setID(mapper.createID(queryData.getConnection(),
                        current, insertStatement));
            }
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        }
    }

    /**
     * Fills the insert parameters
     *
     * @param statement
     * @param feature
     *
     * @throws IOException
     * @throws SQLException
     */
    private void fillInsertParameters(PreparedStatement statement,
        MutableFIDFeature feature) throws IOException, SQLException {
        int baseIndex = fillPrimayKeyParameters(statement, feature, 1);

        Object[] attributes = feature.getAttributes(null);
        AttributeType[] attributeTypes = feature.getFeatureType()
                                                .getAttributeTypes();
        AttributeIO[] aios = queryData.getAttributeHandlers();
        FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();

        for (int i = 0; i < attributeTypes.length; i++) {
            if (attributeTypes[i].isGeometry()) {
                String geomName = attributeTypes[i].getName();
                int srid = ftInfo.getSRID(geomName);
                // ((Geometry) attributes[i]).setSRID(srid); // SRID is a bad assumption
                aios[i].write(statement, baseIndex + i, attributes[i]);
            } else {
                aios[i].write(statement, baseIndex + i, attributes[i]);
            }
        }
    }

    /**
     * Creates the prepared statement for feature inserts
     *
     * @param conn
     * @param featureType
     *
     * @return
     *
     * @throws SQLException
     */
    protected PreparedStatement createInsertStatement(Connection conn,
        FeatureType featureType) throws SQLException {
        AttributeType[] attributeTypes = featureType.getAttributeTypes();
        String tableName = featureType.getTypeName();

        StringBuffer statementSQL = new StringBuffer("INSERT INTO " + tableName
                + "(");

        if (!mapper.returnFIDColumnsAsAttributes()) {
            for (int i = 0; i < mapper.getColumnCount(); i++) {
                if (!mapper.isAutoIncrement(i)) {
                    statementSQL.append(mapper.getColumnName(i)).append(",");
                }
            }
        }

        for (int i = 0; i < attributeTypes.length; i++) {
            statementSQL.append(attributeTypes[i].getName()).append(",");
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ')');
        statementSQL.append(" VALUES (");

        // append primary key placeholders, if any
        if (!mapper.returnFIDColumnsAsAttributes()
                && !mapper.hasAutoIncrementColumns()) {
            for (int i = 0; i < mapper.getColumnCount(); i++) {
                statementSQL.append("?,");
            }
        }

        // append attribute columns placeholders
        for (int i = 0; i < attributeTypes.length; i++) {
            if (attributeTypes[i].isGeometry()) {
                statementSQL.append("?");
            } else {
                statementSQL.append(getGeometryPlaceHolder(attributeTypes[i]));
            }

            statementSQL.append(",");
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ')');

        String sql = statementSQL.toString();

        return conn.prepareStatement(sql);
    }

    /**
     * Returns the placeholder for the geometry in the insert/update statement.
     * May be something like "?", "geomFromBinary(?)" and so on, that is, the
     * geometry itself of some function that turns whatever the geometric
     * AttributeIO generates into a geometry for the database.
     *
     * @param type
     *
     * @return
     */
    protected abstract String getGeometryPlaceHolder(AttributeType type);

    /**
     * Override that uses prepared statements to perform the operation.
     *
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#doInsert(org.geotools.data.jdbc.MutableFIDFeature)
     */
    protected void remove(MutableFIDFeature current)
        throws IOException, SQLException {
        LOGGER.fine("inserting into database feature " + current);

        // lazily create the delete statement
        if (deleteStatement != null) {
            deleteStatement = createDeleteStatement(queryData.getConnection(),
                    queryData.getFeatureType());
        }

        try {
            fillDeleteParameters(deleteStatement, current);
            deleteStatement.executeUpdate();
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        }
    }

    /**
     * Fills the delete statement parameters
     *
     * @param statement
     * @param feature
     *
     * @throws IOException
     * @throws SQLException
     */
    private void fillDeleteParameters(PreparedStatement statement,
        MutableFIDFeature feature) throws IOException, SQLException {
        fillPrimayKeyParameters(statement, feature, 1);
    }

    /**
     * Fills into a prepared statement the primary key values starting from the
     * baseIndex index.
     *
     * @param statement
     * @param feature
     * @param baseIndex
     *
     * @return
     *
     * @throws IOException
     * @throws SQLException
     */
    private int fillPrimayKeyParameters(PreparedStatement statement,
        Feature feature, int baseIndex) throws IOException, SQLException {
        if (!mapper.returnFIDColumnsAsAttributes()
                && !mapper.hasAutoIncrementColumns()) {
            String FID = mapper.createID(queryData.getConnection(), feature,
                    null);
            Object[] primaryKey = mapper.getPKAttributes(FID);

            for (int i = 0; i < primaryKey.length; i++) {
                statement.setObject(i + 1, primaryKey[i]);
            }

            baseIndex += primaryKey.length;
        }

        return baseIndex;
    }

    /**
     * Creates the prepared statement for feature deletes
     *
     * @param conn
     * @param featureType
     *
     * @return
     *
     * @throws SQLException
     */
    protected PreparedStatement createDeleteStatement(Connection conn,
        FeatureType featureType) throws SQLException {
        AttributeType[] attributeTypes = featureType.getAttributeTypes();
        String tableName = featureType.getTypeName();

        StringBuffer statementSQL = new StringBuffer("DELETE " + tableName
                + "WHERE ");

        if (!mapper.returnFIDColumnsAsAttributes()) {
            for (int i = 0; i < mapper.getColumnCount(); i++) {
                if (!mapper.isAutoIncrement(i)) {
                    statementSQL.append(mapper.getColumnName(i)).append(" = ?");

                    if (i < (mapper.getColumnCount() - 1)) {
                        statementSQL.append(" AND ");
                    }
                }
            }
        }

        String sql = statementSQL.toString();

        return conn.prepareStatement(sql);
    }

    /**
     * Fills the insert parameters
     *
     * @param statement
     * @param current
     * @param live DOCUMENT ME!
     *
     * @throws IOException
     * @throws SQLException
     */
    private void fillUpdateParameters(PreparedStatement statement,
        Feature current, Feature live) throws IOException, SQLException {
        Object[] attributes = current.getAttributes(null);
        AttributeType[] attributeTypes = current.getFeatureType()
                                                .getAttributeTypes();
        AttributeIO[] aios = queryData.getAttributeHandlers();
        FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();

        // set new vales for other fields
        for (int i = 0; i < attributeTypes.length; i++) {
            if (attributeTypes[i].isGeometry()) {
                String geomName = attributeTypes[i].getName();
                int srid = ftInfo.getSRID(geomName);
                // ((Geometry) attributes[i]).setSRID(srid); // SRID is a bad assumption
                aios[i].write(statement, i + 1, attributes[i]);
            } else {
                aios[i].write(statement, i + 1, attributes[i]);
            }
        }

        // set new values for the primary key
        int baseIndex = attributeTypes.length + 1;

        if (!mapper.returnFIDColumnsAsAttributes()
                && !mapper.hasAutoIncrementColumns()) {
            baseIndex = fillPrimayKeyParameters(statement, current, baseIndex);
        }

        // set the old values of the primary key in order to look up for the right tuple
        fillPrimayKeyParameters(statement, live, baseIndex + 1);
    }

    /**
     * Creates the prepared statement for feature updates
     *
     * @param conn
     * @param featureType
     *
     * @return
     *
     * @throws SQLException
     */
    protected PreparedStatement createUpdateStatement(Connection conn,
        FeatureType featureType) throws SQLException {
        AttributeType[] attributeTypes = featureType.getAttributeTypes();
        String tableName = featureType.getTypeName();

        // create statement piecewise on a string buffer
        StringBuffer statementSQL = new StringBuffer("UPDATE  " + tableName
                + " SET ");

        // the "SET" part updating the fields, and the primary key too, if it's
        // not generated by the DBMS
        for (int i = 0; i < attributeTypes.length; i++) {
            statementSQL.append(attributeTypes[i].getName()).append(" = ");

            if (attributeTypes[i].isGeometry()) {
                statementSQL.append("?");
            } else {
                statementSQL.append(getGeometryPlaceHolder(attributeTypes[i]));
            }

            statementSQL.append(",");
        }

        // ... the updated primary keys, if any...
        if (!mapper.returnFIDColumnsAsAttributes()
                && !mapper.hasAutoIncrementColumns()) {
            for (int i = 0; i < mapper.getColumnCount(); i++) {
                statementSQL.append(mapper.getColumnName(i)).append(" = ?,");
            }
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ' ');
        statementSQL.append(" WHERE ");

        // now append primary key placeholders
        if (!mapper.returnFIDColumnsAsAttributes()
                && !mapper.hasAutoIncrementColumns()) {
            for (int i = 0; i < mapper.getColumnCount(); i++) {
                statementSQL.append(mapper.getColumnName(i)).append(" = ?");

                if (i < (mapper.getColumnCount() - 1)) {
                    statementSQL.append(" AND ");
                }
            }
        }

        String sql = statementSQL.toString();

        return conn.prepareStatement(sql);
    }

    /**
     * Override that uses sql statements to perform the operation.
     *
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#doUpdate(org.geotools.feature.Feature,
     *      org.geotools.feature.Feature)
     */
    protected void doUpdate(Feature live, Feature current)
        throws IOException, SQLException {
        LOGGER.fine("updating postgis feature " + current);

        // lazily create the insert statement
        if (updateStatement != null) {
            updateStatement = createUpdateStatement(queryData.getConnection(),
                    queryData.getFeatureType());
        }

        try {
            fillUpdateParameters(updateStatement, current, live);
            updateStatement.executeUpdate();
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        }
    }

    /**
     * This version does not use QueryData udpate/insert/remove methods, but
     * uses separate prepared statements instead
     *
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#useQueryDataForInsert()
     */
    protected boolean useQueryDataForInsert() {
        return false;
    }

    public void close() throws IOException {
        JDBCUtils.close(insertStatement);
        JDBCUtils.close(updateStatement);
        JDBCUtils.close(deleteStatement);
        super.close();
    }
}
