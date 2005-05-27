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

import org.geotools.data.DataSourceException;
import org.geotools.feature.Feature;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Statement;


/**
 * A simple implementation of FIDMapper for multi column primary keys
 *
 * @author wolf
 */
public class MultiColumnFIDMapper extends AbstractFIDMapper {
    private static final long serialVersionUID = 1L;
    private static final String UTF8 = "UTF-8";
    private boolean[] autoIncrement;
    private int[] colTypes;
    private String[] colNames;
    private int[] colDecimalDigits;
    private int[] colSizes;

    /**
     * Builds a new instance of the MultiColumnFIDMapper
     *
     * @param colNames - column names
     * @param colTypes - column types, see {@link java.sql.Types}
     * @param colSizes - column sizes
     * @param colDecimalDigits - column decimals
     * @param autoIncrement - flags for auto-increment tests
     *
     * @throws IllegalArgumentException
     */
    public MultiColumnFIDMapper(String[] colNames, int[] colTypes,
        int[] colSizes, int[] colDecimalDigits, boolean[] autoIncrement) {
        if ((colNames == null) || (colTypes == null) || (autoIncrement == null)) {
            throw new IllegalArgumentException(
                "Column description arrays must be not null");
        }

        if (colNames.length == 0) {
            throw new IllegalArgumentException(
                "Column description arrays must be not empty");
        }

        if ((colNames.length != colTypes.length)
                || (colNames.length != autoIncrement.length)) {
            throw new IllegalArgumentException(
                "Column description arrays must have the same size");
        }

        this.colNames = colNames;
        this.colTypes = colTypes;
        this.colSizes = colSizes;
        this.colDecimalDigits = colDecimalDigits;
        this.autoIncrement = autoIncrement;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#initSupportStructures()
     */
    public void initSupportStructures() {
        // nothing to do
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getID(java.lang.Object[])
     */
    public String getID(Object[] attributes) {
        StringBuffer sb = new StringBuffer();

        try {
            for (int i = 0; i < attributes.length; i++) {
                sb.append(URLEncoder.encode(attributes[i].toString(), UTF8));

                if (i < (attributes.length - 1)) {
                    sb.append("&");
                }
            }
        } catch (UnsupportedEncodingException e) {
            // c'mon, don't tell me UTF-8 is not supported ;-)
        }

        return sb.toString();
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getPKAttributes(java.lang.String)
     */
    public Object[] getPKAttributes(String FID) throws IOException {
        String[] attributes = FID.split("&");

        if (attributes.length != colNames.length) {
            throw new DataSourceException(
                "The FID is not compatible with MultiColumnFIDMapper, was expecting "
                + colNames.length + " URL-encoded columns and got "
                + attributes.length + " columns");
        }

        for (int i = 0; i < attributes.length; i++) {
            attributes[i] = URLDecoder.decode(attributes[i], UTF8);
        }

        return attributes;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#createID(java.sql.Connection,
     *      org.geotools.feature.Feature, Statement)
     */
    public String createID(Connection conn, Feature feature, Statement statement)
        throws IOException {
        String[] attValues = new String[colNames.length];

        for (int i = 0; i < colNames.length; i++) {
            attValues[i] = feature.getAttribute(colNames[i]).toString();
        }

        return getID(attValues);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#returnFIDColumnsAsAttributes()
     */
    public boolean returnFIDColumnsAsAttributes() {
        return true;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnCount()
     */
    public int getColumnCount() {
        return colNames.length;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnName(int)
     */
    public String getColumnName(int colIndex) {
        return colNames[colIndex];
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnType(int)
     */
    public int getColumnType(int colIndex) {
        return colTypes[colIndex];
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnSize(int)
     */
    public int getColumnSize(int colIndex) {
        return colSizes[colIndex];
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnDecimalDigits(int)
     */
    public int getColumnDecimalDigits(int colIndex) {
        return colDecimalDigits[colIndex];
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isAutoIncrement(int)
     */
    public boolean isAutoIncrement(int colIndex) {
        return autoIncrement[colIndex];
    }
}
