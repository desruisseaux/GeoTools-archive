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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Geometry;


/**
 * An abstract class that uses sql statements to insert, update and delete
 * features from the database. Useful when the resultset got from the database
 * is not updatable, for example.
 *
 * @task TODO: Use prepared statements for inserts.  Jody says that oracle
 *             at least will perform faster, and I imagine postgis will
 *             too.  This will require a bit of rearchitecture, since the
 *             statement should just be made once, right now even if there
 *             were many features coming in they would all have to make
 *             a new prepared statement - should be able to do it before
 *             and then just fill it up for each feature.  And for oracle
 *             Jody has some convenience methods in his SDO stuff that
 *             works with prepared statements and STRUCTS directly.
 *             See http://jira.codehaus.org/browse/GEOT-219 (close when done).
 *
 * @author Andrea Aime
 * @version $Id$
 */
public abstract class JDBCTextFeatureWriter extends JDBCFeatureWriter {
    /** The logger for the jdbc module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.jdbc");
    FIDMapper mapper = null;

    /**
     * Creates a new instance of JDBCFeatureWriter
     *
     * @param fReader
     * @param queryData
     *
     * @throws IOException
     */
    public JDBCTextFeatureWriter(FeatureReader fReader, QueryData queryData)
        throws IOException {
        super(fReader, queryData);
        mapper = queryData.getMapper();
    }

    /**
     * Override that uses sql statements to perform the operation.
     *
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#doInsert(org.geotools.data.jdbc.MutableFIDFeature)
     */
    protected void doInsert(MutableFIDFeature current)
        throws IOException, SQLException {
        LOGGER.fine("inserting into postgis feature " + current);

        Statement statement = null;
        Connection conn = null;

        try {
            conn = queryData.getConnection();
            statement = conn.createStatement();

            String sql = makeInsertSql(current);
            LOGGER.fine(sql);
            statement.executeUpdate(sql);

            // should the ID be generated during an insert, we need to read it back
            // and set it into the feature
          if (((mapper.getColumnCount() > 0)
          && mapper.hasAutoIncrementColumns())) {
//          if (((mapper.getColumnCount() > 0))) {
                current.setID(mapper.createID(conn, current, statement));
            }
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    String msg = "Error closing JDBC Statement";
                    LOGGER.log(Level.WARNING, msg, e);
                }
            }
        }
    }

    /**
     * Creates a sql insert statement.  Uses each feature's schema, which makes
     * it possible to insert out of order, as well as inserting less than all
     * features.
     *
     * @param feature the feature to add.
     *
     * @return an insert sql statement.
     *
     * @throws IOException
     */
    protected String makeInsertSql(Feature feature) throws IOException {
        FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();
        FeatureType fetureType = ftInfo.getSchema();

        String tableName = encodeName(fetureType.getTypeName());
        AttributeType[] attributeTypes = fetureType.getAttributeTypes();

        String attrValue;

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
	    String colName = encodeName(attributeTypes[i].getName());
            statementSQL.append(colName).append(",");
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ')');
        statementSQL.append(" VALUES (");

        if (!mapper.returnFIDColumnsAsAttributes()
                && !mapper.hasAutoIncrementColumns()) {
            String FID = mapper.createID(queryData.getConnection(), feature,
                    null);
            if( current instanceof MutableFIDFeature ){
                ((MutableFIDFeature)current).setID(FID);
            }
            Object[] primaryKey = mapper.getPKAttributes(FID);

            for (int i = 0; i < primaryKey.length; i++) {
                if (!mapper.isAutoIncrement(i)) {
                    attrValue = addQuotes(primaryKey[i]);
                    statementSQL.append(attrValue).append(",");
                }
            }
        }

        Object[] attributes = feature.getAttributes(null);

        for (int i = 0; i < attributeTypes.length; i++) {
            if (attributeTypes[i].isGeometry()) {
                String geomName = attributeTypes[i].getName();
                int srid = ftInfo.getSRID(geomName);
                attrValue = getGeometryInsertText((Geometry) attributes[i], srid);
            } else {
                attrValue = addQuotes(attributes[i]);
            }

            statementSQL.append(attrValue + ",");
        }

        statementSQL.setCharAt(statementSQL.length() - 1, ')');

        return (statementSQL.toString());
    }

    /**
     * Adds quotes to an object for storage in postgis.  The object should be a
     * string or a number.  To perform an insert strings need quotes around
     * them, and numbers work fine with quotes, so this method can be called
     * on unknown objects.
     *
     * @param value The object to add quotes to.
     *
     * @return a string representation of the object with quotes.
     */
    protected String addQuotes(Object value) {
        String retString;

        if (value != null) {
            retString = "'" + doubleQuote(value) + "'";
        } else {
            retString = "null";
        }

        return retString;
    }

    String doubleQuote(Object obj) {
        return obj.toString().replaceAll("'", "''");
    }

    /**
     * Encodes the tableName, default is to do nothing, but postgis will
     * override and put double quotes around the tablename.
     */
    protected String encodeName(String tableName) {
	return tableName;
    }

    /**
     * Turns a geometry into the textual version needed for the sql statement
     *
     * @param geom
     * @param srid
     *
     * @return
     */
    protected abstract String getGeometryInsertText(Geometry geom, int srid) throws IOException;

    /**
     * Override that uses sql statements to perform the operation.
     *
     * @see org.geotools.data.FeatureWriter#remove()
     */
    public void remove() throws IOException {
        LOGGER.fine("inserting into postgis feature " + current);

        Statement statement = null;
        Connection conn = null;

        try {
            conn = queryData.getConnection();
            statement = conn.createStatement();

            String sql = makeDeleteSql(current);
            LOGGER.fine(sql);
            //System.out.println(sql);
            statement.executeUpdate(sql);
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    String msg = "Error closing JDBC Statement";
                    LOGGER.log(Level.WARNING, msg, e);
                }
            }
        }
    }

    /**
     * Generates the query for the sql delete statement
     *
     * @param feature
     *
     * @return
     *
     * @throws IOException
     */
    protected String makeDeleteSql(Feature feature) throws IOException {
        FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();
        FeatureType fetureType = ftInfo.getSchema();

        String tableName = encodeName(fetureType.getTypeName());

        StringBuffer statementSQL = new StringBuffer("DELETE FROM " + tableName
                + " WHERE ");
        Object[] pkValues = mapper.getPKAttributes(feature.getID());

        for (int i = 0; i < mapper.getColumnCount(); i++) {
            statementSQL.append(mapper.getColumnName(i)).append(" = ").append(addQuotes(
                    pkValues[i]));

            if (i < (mapper.getColumnCount() - 1)) {
                statementSQL.append(" AND ");
            }
        }

        return (statementSQL.toString());
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

        Statement statement = null;
        Connection conn = null;

        try {
            conn = queryData.getConnection();
            statement = conn.createStatement();

            String sql = makeUpdateSql(live, current);
            LOGGER.fine(sql);
            System.out.println(sql);
            statement.executeUpdate(sql);
        } catch (SQLException sqle) {
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqle);
            queryData.close(sqle);
            throw new DataSourceException(msg, sqle);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    String msg = "Error closing JDBC Statement";
                    LOGGER.log(Level.WARNING, msg, e);
                }
            }
        }
    }

    /**
     * Generate the update sql statement
     *
     * @param live
     * @param current
     *
     * @return
     *
     * @throws IOException
     */
    protected String makeUpdateSql(Feature live, Feature current)
        throws IOException {
        FeatureTypeInfo ftInfo = queryData.getFeatureTypeInfo();
        FeatureType featureType = ftInfo.getSchema();
        AttributeType[] attributes = featureType.getAttributeTypes();

        String tableName = encodeName(featureType.getTypeName());

        StringBuffer statementSQL = new StringBuffer("UPDATE " + tableName
                + " SET ");

        for (int i = 0; i < current.getNumberOfAttributes(); i++) {
            Object currAtt = current.getAttribute(i);
            Object liveAtt = live.getAttribute(i);

            if (!DataUtilities.attributesEqual(currAtt, liveAtt)) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.fine("modifying att# " + i + " to " + currAtt);
                }
		String colName = encodeName(attributes[i].getName());
                statementSQL.append(colName).append(" = ")
                            .append(addQuotes(currAtt)).append(", ");
            }
        }

        statementSQL.setLength(statementSQL.length() - 2);
        statementSQL.append(" WHERE ");

        Object[] pkValues = mapper.getPKAttributes(current.getID());

        for (int i = 0; i < mapper.getColumnCount(); i++) {
            statementSQL.append(mapper.getColumnName(i)).append(" = ").append(addQuotes(
                    pkValues[i]));

            if (i < (mapper.getColumnCount() - 1)) {
                statementSQL.append(" AND ");
            }
        }

        return (statementSQL.toString());
    }

    /**
     * This version does not use QueryData udpate/insert/remove methods, but
     * uses separate queries instead
     *
     * @see org.geotools.data.jdbc.JDBCFeatureWriter#useQueryDataForInsert()
     */
    protected boolean useQueryDataForInsert() {
        return false;
    }
}
