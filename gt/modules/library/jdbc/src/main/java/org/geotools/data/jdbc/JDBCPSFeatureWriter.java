/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

/**
 * An abstract class that uses prepared statements to insert, update and delete features from the
 * database. Useful when the resultset got from the database is not updatable, and to get peak
 * performance thru the use of prepared statements and batch updates.
 * <p>
 * Assumptions made by the code:
 * <ul>
 * <li> if the primary key contains auto-increment columns, it is composed solely of auto-increment
 * columns; </li>
 * <li> the primary key never contains geometric attributes; </li>
 * </ul>
 * </p>
 * 
 * @author Andrea Aime
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/jdbc/src/main/java/org/geotools/data/jdbc/JDBCPSFeatureWriter.java $
 */
public abstract class JDBCPSFeatureWriter extends JDBCFeatureWriter {
    /** The logger for the jdbc module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.data.jdbc");

    protected final FIDMapper mapper;

    PreparedStatement insertStatement;

    PreparedStatement deleteStatement;

    PreparedStatement updateStatement;

    /**
     * Creates a new instance of JDBCFeatureWriter
     * 
     * @param fReader
     * @param queryData
     * @throws IOException
     */
    public JDBCPSFeatureWriter(FeatureReader<SimpleFeatureType, SimpleFeature> fReader,
                               QueryData queryData) throws IOException {
        super(fReader, queryData);
        mapper = queryData.getMapper();
    }

    /**
     * Override that uses prepared statements to perform the operation.
     * 
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#doInsert(org.geotools.data.jdbc.MutableFIDFeature)
     */
    protected void doInsert(MutableFIDFeature current) throws IOException, SQLException {
        LOGGER.fine("inserting into database feature " + current);

        // lazily create the insert statement
        final Connection connection = queryData.getConnection();
        if (insertStatement == null) {
            final SimpleFeatureType featureType = queryData.getFeatureType();
            insertStatement = createInsertStatement(connection, featureType);
        }

        try {
            fillInsertParameters(insertStatement, current);
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing column values";
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        }

        executeInsert(insertStatement);

        // should the ID be generated during an insert, we need to read it back
        // and set it into the feature
        //final int pkColumnCount = mapper.getColumnCount();
        //if (((pkColumnCount > 0) && mapper.hasAutoIncrementColumns())) {
            final String fid = mapper.createID(connection, current, insertStatement);
            current.setID(fid);
        //}
    }

    /**
     * Hook for subclasses to use a specific execution mechanism other than the default
     * <code>insertStatement.executeUpdate()</code>.
     * <p>
     * Override may be needed in some cases, for example, where the execution expects a ResultSet
     * containing the autogenerated primary key.
     * </p>
     * @param insertStatement the insert statement prepared in {@link #doInsert(MutableFIDFeature)}
     * @throws IOException
     */
    protected void executeInsert(final PreparedStatement insertStatement) throws IOException {
        try {
            insertStatement.executeUpdate();
        } catch (SQLException sqle) {
            String msg = "SQL Exception executing insert statement";
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
     * @throws IOException
     * @throws SQLException
     */
    private void fillInsertParameters(PreparedStatement statement, MutableFIDFeature feature)
            throws IOException, SQLException {

        final Object[] attributes = feature.getAttributes().toArray();
        final List<AttributeDescriptor> attributeTypes = feature.getFeatureType().getAttributes();
        final AttributeIO[] aios = queryData.getAttributeHandlers();

        assert attributes.length == attributeTypes.size();
        assert attributeTypes.size() == aios.length;

        final int baseIndex = fillPrimaryKeyParameters(statement, feature, 1);

        AttributeIO attributeIO;
        Object attributeValue;
        for (int i = 0; i < attributeTypes.size(); i++) {
            attributeIO = aios[i];
            attributeValue = attributes[i];
            attributeIO.write(statement, baseIndex + i, attributeValue);
        }
    }

    /**
     * Creates the prepared statement for feature inserts
     * 
     * @param conn
     * @param featureType
     * @throws SQLException
     */
    protected PreparedStatement createInsertStatement(Connection conn, SimpleFeatureType featureType)
            throws SQLException {
        final List<AttributeDescriptor> attributeTypes = featureType.getAttributes();
        final String tableName = featureType.getTypeName();

        final StringBuffer statementSQL = new StringBuffer("INSERT INTO ");
        statementSQL.append(encodeName(tableName)).append("(");

        final int pkColumnCount = mapper.getColumnCount();

        // append primary key column names, if any
        if (!mapper.returnFIDColumnsAsAttributes()) {
            for (int i = 0; i < pkColumnCount; i++) {
                if (!mapper.isAutoIncrement(i)) {
                    statementSQL.append(mapper.getColumnName(i)).append(",");
                }
            }
        }

        // append attribute column names
        for (int i = 0; i < attributeTypes.size(); i++) {
            AttributeDescriptor attributeDescriptor = attributeTypes.get(i);
            String localName = attributeDescriptor.getLocalName();
            statementSQL.append(localName).append(",");
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ')');
        statementSQL.append(" VALUES (");

        // append primary key placeholders, if any
        if (!mapper.returnFIDColumnsAsAttributes() && !mapper.hasAutoIncrementColumns()) {
            for (int i = 0; i < pkColumnCount; i++) {
                statementSQL.append("?,");
            }
        }

        // append attribute columns placeholders
        for (int i = 0; i < attributeTypes.size(); i++) {
            AttributeDescriptor attributeDescriptor = attributeTypes.get(i);
            if (attributeDescriptor instanceof GeometryDescriptor) {
                String geometryPlaceHolder = getGeometryPlaceHolder(attributeDescriptor);
                statementSQL.append(geometryPlaceHolder);
            } else {
                statementSQL.append("?");
            }

            statementSQL.append(",");
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ')');

        return prepareInsertStatement(conn, statementSQL, featureType);
        
        //REVISIT: this is an unsupported feature in postgis jdbc3 driver
        // array of autoincrement column indices the prep statement should return
//        int[] pkColIndices = {};
//        if (mapper.hasAutoIncrementColumns()) {
//            final int colCount = mapper.getColumnCount();
//            for (int i = 1; i <= colCount; i++) {
//                if (mapper.isAutoIncrement(i)) {
//                    if (pkColIndices.length == 0) {
//                        pkColIndices = new int[] { i };
//                    } else {
//                        int[] previous = pkColIndices;
//                        pkColIndices = new int[previous.length + 1];
//                        System.arraycopy(previous, 0, pkColIndices, 0, previous.length);
//                        pkColIndices[pkColIndices.length - 1] = i;
//                    }
//                }
//            }
//        }
//        return conn.prepareStatement(sql, pkColIndices);
    }

    /**
     * Provides a hook for subclasses to fine tune the creation of the prepared statement.
     * <p>
     * This default implementation just creates the statement with the given connetion and
     * the insert request built at {@link #createInsertStatement}
     * </p>
     * @param conn
     * @param statementSQL
     * @param featureType
     * @return
     * @throws SQLException
     */
    protected PreparedStatement prepareInsertStatement(Connection conn,
            StringBuffer statementSQL,
            SimpleFeatureType featureType) throws SQLException {
        return conn.prepareStatement(statementSQL.toString());
    }

    /**
     * Returns the placeholder for the geometry in the insert/update statement. May be something
     * like "?", "geomFromBinary(?)" and so on, that is, the geometry itself of some function that
     * turns whatever the geometric AttributeIO generates into a geometry for the database.
     * 
     * @param type
     */
    protected abstract String getGeometryPlaceHolder(AttributeDescriptor type);

    /**
     * Override that uses prepared statements to perform the operation.
     * 
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#doInsert(org.geotools.data.jdbc.MutableFIDFeature)
     */
    protected void remove(MutableFIDFeature current) throws IOException, SQLException {
        LOGGER.fine("inserting into database feature " + current);

        // lazily create the delete statement
        if (deleteStatement != null) {
            deleteStatement = createDeleteStatement(queryData.getConnection(), queryData
                    .getFeatureType());
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
     * @throws IOException
     * @throws SQLException
     */
    private void fillDeleteParameters(PreparedStatement statement, MutableFIDFeature feature)
            throws IOException, SQLException {
        fillPrimaryKeyParameters(statement, feature, 1);
    }

    /**
     * Fills into a prepared statement the primary key values starting from the baseIndex index.
     * 
     * @param statement
     * @param feature
     * @param baseIndex
     * @throws IOException
     * @throws SQLException
     */
    private int fillPrimaryKeyParameters(PreparedStatement statement,
            SimpleFeature feature,
            int baseIndex) throws IOException, SQLException {
        if (!mapper.returnFIDColumnsAsAttributes() && !mapper.hasAutoIncrementColumns()) {
            final Connection connection = queryData.getConnection();
            final String FID = mapper.createID(connection, feature, null);
            final Object[] primaryKey = mapper.getPKAttributes(FID);

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
     * @throws SQLException
     */
    protected PreparedStatement createDeleteStatement(Connection conn, SimpleFeatureType featureType)
            throws SQLException {
        String tableName = featureType.getTypeName();

        StringBuffer statementSQL = new StringBuffer("DELETE " + tableName + "WHERE ");

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
     * @throws IOException
     * @throws SQLException
     */
    private void fillUpdateParameters(PreparedStatement statement,
            SimpleFeature current,
            SimpleFeature live) throws IOException, SQLException {
        List<Object> attributes = current.getAttributes();
        List<AttributeDescriptor> attributeTypes = current.getFeatureType().getAttributes();
        AttributeIO[] aios = queryData.getAttributeHandlers();
        FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();

        // set new vales for other fields
        for (int i = 0; i < attributeTypes.size(); i++) {
            aios[i].write(statement, i + 1, attributes.get(i));
        }

        // set new values for the primary key
        int baseIndex = attributeTypes.size() + 1;

        if (!mapper.returnFIDColumnsAsAttributes() && !mapper.hasAutoIncrementColumns()) {
            baseIndex = fillPrimaryKeyParameters(statement, current, baseIndex);
        }

        // set the old values of the primary key in order to look up for the right tuple
        fillPrimaryKeyParameters(statement, live, baseIndex + 1);
    }

    /**
     * Creates the prepared statement for feature updates
     * 
     * @param conn
     * @param featureType
     * @throws SQLException
     */
    protected PreparedStatement createUpdateStatement(Connection conn, SimpleFeatureType featureType)
            throws SQLException {
        List<AttributeDescriptor> attributeTypes = featureType.getAttributes();
        String tableName = featureType.getTypeName();

        // create statement piecewise on a string buffer
        StringBuffer statementSQL = new StringBuffer("UPDATE  " + tableName + " SET ");

        // the "SET" part updating the fields, and the primary key too, if it's
        // not generated by the DBMS
        for (int i = 0; i < attributeTypes.size(); i++) {
            statementSQL.append(attributeTypes.get(i).getLocalName()).append(" = ");

            if (attributeTypes.get(i) instanceof GeometryDescriptor) {
                statementSQL.append(getGeometryPlaceHolder(attributeTypes.get(i)));
            } else {
                statementSQL.append("?");
            }

            statementSQL.append(",");
        }

        // ... the updated primary keys, if any...
        if (!mapper.returnFIDColumnsAsAttributes() && !mapper.hasAutoIncrementColumns()) {
            for (int i = 0; i < mapper.getColumnCount(); i++) {
                statementSQL.append(mapper.getColumnName(i)).append(" = ?,");
            }
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ' ');
        statementSQL.append(" WHERE ");

        // now append primary key placeholders
        if (!mapper.returnFIDColumnsAsAttributes() && !mapper.hasAutoIncrementColumns()) {
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
    protected void doUpdate(SimpleFeature live, SimpleFeature current) throws IOException,
            SQLException {
        LOGGER.fine("updating postgis feature " + current);

        // lazily create the insert statement
        if (updateStatement != null) {
            updateStatement = createUpdateStatement(queryData.getConnection(), queryData
                    .getFeatureType());
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
     * This version does not use QueryData udpate/insert/remove methods, but uses separate prepared
     * statements instead
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
