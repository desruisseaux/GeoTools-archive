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
/*
 * Created on 18-apr-2004
 */
package org.geotools.data.jdbc.fidmapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.feature.FeatureType;


/**
 * Default FID mapper that works with default FID mappers.
 *
 * @author wolf
 */
public class DefaultFIDMapperFactory implements FIDMapperFactory {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.jdbc");
    private boolean returningTypedFIDMapper = true;
    

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapperFactory#getMapper(java.lang.String,
     *      java.sql.DatabaseMetaData)
     */
    public FIDMapper getMapper(String catalog, String schema, String tableName,
        Connection connection) throws IOException {
        ColumnInfo[] colInfos = getPkColumnInfo(catalog, schema, tableName, connection);
        FIDMapper mapper = null;

        if (colInfos.length == 0) {
            mapper = new NullFIDMapper();
        } else if (colInfos.length > 1) {
            mapper = buildMultiColumnFIDMapper(colInfos);
        } else {
            ColumnInfo ci = colInfos[0];

            if (ci.autoIncrement) {
                mapper = new AutoIncrementFIDMapper(ci.colName, ci.dataType);
            } else if (isIntegralType(ci.dataType)) {
                mapper = new MaxIncFIDMapper(tableName, ci.colName, ci.dataType);
            } else if(isTextType(ci.dataType)) {
                mapper = new BasicFIDMapper(ci.colName, ci.size);
            } else {
                throw new RuntimeException("Cannot map this column type: " + ci);
            }
        }
        
        if(returningTypedFIDMapper && mapper != null)
            return new TypedFIDMapper(mapper, tableName);
        else
            return mapper;
    }

    /**
     * DOCUMENT ME!
     *
     * @param colInfos
     *
     * @return
     */
    protected FIDMapper buildMultiColumnFIDMapper(ColumnInfo[] colInfos) {
        String[] colNames = new String[colInfos.length];
        int[] colTypes = new int[colInfos.length];
        int[] colSizes = new int[colInfos.length];
        int[] colDecimalDigits = new int[colInfos.length];
        boolean[] autoIncrement = new boolean[colInfos.length];

        for (int i = 0; i < colInfos.length; i++) {
            ColumnInfo ci = colInfos[i];
            colNames[i] = ci.colName;
            colTypes[i] = ci.dataType;
            colSizes[i] = ci.size;
            colDecimalDigits[i] = ci.decimalDigits;
            autoIncrement[i] = ci.autoIncrement;
        }

        return new MultiColumnFIDMapper(colNames, colTypes, colSizes, colDecimalDigits, autoIncrement);
    }

    protected ColumnInfo[] getPkColumnInfo(String catalog, String schema, String typeName,
        Connection conn) throws SchemaNotFoundException, DataSourceException {
        final int NAME_COLUMN = 4;
        final int TYPE_NAME = 6;
        final int COLUMN_NAME = 4;
        final int DATA_TYPE = 5;
        ResultSet tableInfo = null;
        ResultSet pkInfo = null;
        boolean pkMetadataFound = false;

        try {
            DatabaseMetaData dbMetaData = conn.getMetaData();

            Map pkMap = new HashMap();
            pkInfo = dbMetaData.getPrimaryKeys(catalog, schema, typeName);
            pkMetadataFound = true;

            while (pkInfo.next()) {
                ColumnInfo ci = new ColumnInfo();
                ci.colName = pkInfo.getString("COLUMN_NAME");
                ci.keySeq = pkInfo.getInt("KEY_SEQ");
                pkMap.put(ci.colName, ci);
            }

            tableInfo = dbMetaData.getColumns(catalog, schema, typeName, "%");

            boolean tableInfoFound = false;

            while (tableInfo.next()) {
                tableInfoFound = true;

                String columnName = tableInfo.getString("COLUMN_NAME");
                ColumnInfo ci = (ColumnInfo) pkMap.get(columnName);

                if (ci != null) {
                    ci.dataType = tableInfo.getInt("DATA_TYPE");
                    ci.size = tableInfo.getInt("COLUMN_SIZE");
                    ci.decimalDigits = tableInfo.getInt("DECIMAL_DIGITS");
                    ci.autoIncrement = isAutoIncrement(catalog, schema, typeName, conn, tableInfo,
                            columnName, ci.dataType);
                }
            }

            if (!tableInfoFound) {
                throw new SchemaNotFoundException(typeName);
            }

            Collection columnInfos = pkMap.values();

            return (ColumnInfo[]) columnInfos.toArray(new ColumnInfo[columnInfos.size()]);
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, sqlException);
            conn = null; // prevent finally block from reclosing
            if(pkMetadataFound)
                throw new DataSourceException("SQL Error building FeatureType for " + typeName + " "
                    + sqlException.getMessage(), sqlException);
            else
                throw new SchemaNotFoundException(typeName, sqlException);
        } finally {
            JDBCUtils.close(tableInfo);
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
        }
    }

    /**
     * Returns true if the specified column is auto-increment. This method is left protected so
     * that specific datastore implementations can put their own logic, should the default one be
     * ineffective or have bad performance
     *
     * @param catalog
     * @param schema
     * @param tableName
     * @param conn
     * @param tableInfo
     * @param columnName
     * @param dataType
     *
     * @return
     *
     * @throws SQLException
     */
    protected boolean isAutoIncrement(String catalog, String schema, String tableName,
        Connection conn, ResultSet tableInfo, String columnName, int dataType)
        throws SQLException {
        // if it's not an integer type it can't be an auto increment type
        if (!isIntegralType(dataType)) {
            return false;
        }

        // ok, it's an integer. To know if it's an auto-increment let's have a look at resultset metadata
        // and try to force it to get just a single row for exploring the metadata
        boolean autoIncrement = false;
        Statement statement = null;
        ResultSet rs = null;

        try {
            statement = conn.createStatement();
            statement.setFetchSize(1);
            rs = statement.executeQuery("Select " + columnName + " from " + tableName);

            java.sql.ResultSetMetaData rsInfo = rs.getMetaData();
            autoIncrement = rsInfo.isAutoIncrement(1);
        } finally {
            JDBCUtils.close(statement);
            JDBCUtils.close(rs);
        }

        return autoIncrement;
    }

    protected boolean isIntegralType(int dataType) {
        return (dataType == Types.BIGINT) || (dataType == Types.INTEGER)
        || (dataType == Types.NUMERIC) || (dataType == Types.SMALLINT)
        || (dataType == Types.TINYINT);
    }
    
    protected boolean isTextType(int dataType) {
            return (dataType == Types.VARCHAR) || (dataType == Types.CHAR)
            || (dataType == Types.CLOB);
        }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapperFactory#getMapper(org.geotools.feature.FeatureType)
     */
    public FIDMapper getMapper(FeatureType featureType) {
        return new BasicFIDMapper("ID", 255, false);
    }

    protected class ColumnInfo implements Comparable {
        String colName;
        int dataType;
        int size;
        int decimalDigits;
        boolean autoIncrement;
        int keySeq;

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            return keySeq - ((ColumnInfo) o).keySeq;
        }
        
        public String toString() {
            return "ColumnInfo, name(" + colName + "), type(" + dataType + ") size(" + size + ") decimalDigits(" + decimalDigits + ") autoIncrement(" + autoIncrement + ")";
        }
    }
}
