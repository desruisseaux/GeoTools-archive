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
 * Builds a complete SQL query to select the specified attributes for the
 * specified feature type, using a specified filter to generate a WHERE
 * clause.
 * 
 * <p>
 * The actual WHERE clause is generated by the SQLEncoder class or appropriate
 * subclass for a particular database.  If a specific encoder is to be used,
 * it must be specified to the constructor for this class.
 * </p>
 * 
 * <p>
 * In order to implement the functionality of the application-specified Filter,
 * this is split into a 'preQueryFilter' which can be incorporated into the
 * SQL query itself and a 'postQueryFilter.  The encoder capabilities are used
 * to determine how much of the function can be performed by the database
 * directly and how much has to be performed on the result set.
 * </p>
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public class DefaultSQLBuilder implements SQLBuilder {
    // The instance of the encoder to be used to generate the WHERE clause
    protected SQLEncoder encoder;

    /**
     * Constructs an instance of this class with a default SQLEncoder
     */
    public DefaultSQLBuilder() {
        this(new SQLEncoder());
    }

    /**
     * Constructs an instance of this class using the encoder class specified.
     * This will typically be from the getSqlBuilder method of a JDBCDataStore
     * subclass
     *
     * @param encoder the specific encoder to be used.
     */
    public DefaultSQLBuilder(SQLEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * Return the postQueryFilter that must be applied to the database query
     * result set.
     *
     * @param filter the application filter which must be applied
     *
     * @return the filter representing the functionality that must be performed
     *         on the result set.
     */
    public Filter getPostQueryFilter(Filter filter) {
        FilterCapabilities cap = encoder.getCapabilities();
        SQLUnpacker unpacker = new SQLUnpacker(cap);

        //figure out which of the filter we can use.
        unpacker.unPackAND(filter);

        return unpacker.getUnSupported();
    }

    /**
     * Return the preQueryFilter that can be used to generate the WHERE clause.
     *
     * @param filter the application filter which must be applied
     *
     * @return the filter representing the functionality that can be performed
     *         by the database.
     */
    public Filter getPreQueryFilter(Filter filter) {
        SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());

        //figure out which of the filter we can use.
        unpacker.unPackAND(filter);

        return unpacker.getSupported();
    }

    /**
     * Constructs the FROM clause for a featureType
     * 
     * <p>
     * sql: <code>FROM typeName</code>
     * </p>
     *
     * @param sql the StringBuffer that the WHERE clause should be appended to
     * @param typeName the name of the table (feature type) to be queried
     */
    public void sqlFrom(StringBuffer sql, String typeName) {
        sql.append(" FROM ");
        sql.append(encoder.escapeName(typeName));
    }

    /**
     * Constructs WHERE clause, if needed, for FILTER.
     * 
     * <p>
     * sql: <code>WHERE filter encoding</code>
     * </p>
     *
     * @param sql The StringBuffer that the WHERE clause should be appended to
     * @param preFilter The filter to be used by the encoder class to generate
     *        the WHERE clause
     *
     * @throws SQLEncoderException Not thrown here but may be thrown by the
     *         encoder
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
     * Constructs the full SQL SELECT statement for the supplied Filter.
     * 
     * <p>
     * The statement is constructed by concatenating the SELECT column list,
     * FROM table specification and WHERE clause appropriate to the supplied
     * Filter.
     * </p>
     *
     * @param typeName The name of the table (feature type) to be queried
     * @param mapper FIDMapper to identify the FID columns in the table
     * @param attrTypes The specific attribute columns to be selected
     * @param filter The Filter that will be used by the encoder to construct
     *        the WHERE clause
     *
     * @return The fully formed SQL SELECT statement
     *
     * @throws SQLEncoderException Not thrown by this method but may be thrown
     *         by the encoder class
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
     * Appends the names of the columns to be selected.
     * 
     * <p>
     * sqlGeometryColumn is invoked for any special handling for geometry
     * columns.
     * </p>
     *
     * @param sql StringBuffer to be appended to
     * @param mapper FIDMapper to provide the name(s) of the FID columns
     * @param attributes Array of columns to be selected
     *
     * @see postgisDataStore.SQLBuilder#sqlColumns(java.lang.StringBuffer,
     *      postgisDataStore.FIDMapper.FIDMapper,
     *      org.geotools.feature.AttributeType[])
     */
    public void sqlColumns(StringBuffer sql, FIDMapper mapper,
        AttributeType[] attributes) {
        for (int i = 0; i < mapper.getColumnCount(); i++) {
            sql.append(encoder.escapeName(mapper.getColumnName(i)) + ", ");
        }

        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].isGeometry()) {
                sqlGeometryColumn(sql, attributes[i]);
            } else {
                sql.append(encoder.escapeName(attributes[i].getName()));
            }

            if (i < (attributes.length - 1)) {
                sql.append(", ");
            }
        }
    }

    /**
     * Generates the select column specification for a geometry column.
     * 
     * <p>
     * This should typically be overridden in the subclass to return a
     * meaningful value that the attribute i/o handler can process.
     * </p>
     *
     * @param sql A StringBuffer that the column specification can be appended
     *        to
     * @param geomAttribute An AttributeType for a geometry attribute
     */
    public void sqlGeometryColumn(StringBuffer sql, AttributeType geomAttribute) {
        sql.append(encoder.escapeName(geomAttribute.getName()));
    }
}
