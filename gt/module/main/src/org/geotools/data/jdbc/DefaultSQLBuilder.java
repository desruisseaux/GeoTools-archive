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

import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLUnpacker;


/**
 * Provides  ...
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public class DefaultSQLBuilder implements SQLBuilder {
    protected SQLEncoder encoder;

    /**
     *
     */
    public DefaultSQLBuilder() {
        this(new SQLEncoder());
    }

    /**
     * DOCUMENT ME!
     *
     * @param encoder
     */
    public DefaultSQLBuilder(SQLEncoder encoder) {
        this.encoder = encoder;
    }

    public Filter getPostQueryFilter(Filter filter) {
        FilterCapabilities cap = encoder.getCapabilities();
        SQLUnpacker unpacker = new SQLUnpacker(cap);

        //figure out which of the filter we can use.
        unpacker.unPackAND(filter);

        return unpacker.getUnSupported();
    }

    public Filter getPreQueryFilter(Filter filter) {
        SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());

        //figure out which of the filter we can use.
        unpacker.unPackAND(filter);

        return unpacker.getSupported();
    }

    /**
     * Consutrcts FROM clause for featureType
     * 
     * <p>
     * sql: <code>FROM typeName</code>
     * </p>
     *
     * @param sql
     * @param typeName
     */
    public void sqlFrom(StringBuffer sql, String typeName) {
        sql.append(" FROM ");
        sql.append(typeName);
    }

    /**
     * Constructs WHERE clause, if needed, for FILTER.
     * 
     * <p>
     * sql: <code>WHERE filter encoding</code>
     * </p>
     *
     * @param sql DOCUMENT ME!
     * @param preFilter DOCUMENT ME!
     *
     * @throws SQLEncoderException DOCUMENT ME!
     */
    public void sqlWhere(StringBuffer sql, Filter preFilter)
        throws SQLEncoderException {
        if ((preFilter != null) && (preFilter != Filter.NONE)) {
            String where = encoder.encode(preFilter);
            sql.append(" ");
            sql.append(where);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName
     * @param mapper
     * @param attrTypes
     * @param filter
     *
     * @return
     *
     * @throws SQLEncoderException
     *
     * @see DefaultSQLBuilder#buildSQLQuery(String, FIDMapper, AttributeType[],
     *      Filter)
     */
    public String buildSQLQuery(String typeName, FIDMapper mapper,
        AttributeType[] attrTypes, Filter filter) throws SQLEncoderException {
        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("SELECT ");
        sqlColumns(sqlBuffer, mapper, attrTypes);
        sqlFrom(sqlBuffer, typeName);
        encoder.setFIDMapper(mapper);
        sqlWhere(sqlBuffer, filter);

        String sqlStmt = sqlBuffer.toString();

        return sqlStmt;
    }

    /**
     * @see postgisDataStore.SQLBuilder#sqlColumns(java.lang.StringBuffer, postgisDataStore.FIDMapper.FIDMapper, org.geotools.feature.AttributeType[])
     */
    public void sqlColumns(StringBuffer sql, FIDMapper mapper,
        AttributeType[] attributes) {
        for (int i = 0; i < mapper.getColumnCount(); i++) {
            sql.append(mapper.getColumnName(i) + ", ");
        }

        for (int i = 0; i < attributes.length; i++) {
            sql.append(attributes[i].getName());

            if (i < (attributes.length - 1)) {
                sql.append(", ");
            }
        }
    }
}
