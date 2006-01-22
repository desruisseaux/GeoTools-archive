/*
 *    Geotools2 - OpenSource mapping toolkit
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

import org.geotools.data.db2.filter.SQLEncoderDB2;
import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.feature.AttributeType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import java.util.logging.Logger;


/**
 * A DB2-specific subclass of DefaultSQLBuilder, which supports DB2 Spatial
 * Extender geometry datatypes.
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2SQLBuilder extends DefaultSQLBuilder {
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.db2");
    private String tableSchema = null;
    private String tableName = null;

    /**
     * Creates a DB2SQLBuilder that will provide a table schema to qualify
     * table names. The table schema is provided by the DB2DataStore which
     * means that a given DataStore can only access tables within a single
     * schema.
     * 
     * <p>
     * It would be better if the table schema was managed by FeatureTypeHandler
     * or FeatureType.
     * </p>
     *
     * @param encoder an SQLEncoder
     * @param tableSchema table schema to qualify table names
     * @param tableName the table name to be used by this SQL builder
     */
    public DB2SQLBuilder(SQLEncoder encoder, String tableSchema,
        String tableName) {
        super(encoder);
        this.tableSchema = tableSchema;
        this.tableName = tableName;
    }

    /**
     * Generates the select column specification for a DB2 geometry column.
     * 
     * <p>
     * Overrides sqlGeometryColumn in DefaultSQLBuilder
     * </p>
     *
     * @param sql A StringBuffer that the column specification can be appended
     *        to.
     * @param geomAttribute An AttributeType for a geometry attribute
     */
    public void sqlGeometryColumn(StringBuffer sql, AttributeType geomAttribute) {
        sql.append(sqlGeometryColumnName(geomAttribute) + "..ST_AsText()");
    }

    /**
     * Gets the escaped geometry column name.
     *
     * @param geomAttribute the geometry attribute.
     *
     * @return the String with the escaped name.
     */
    String sqlGeometryColumnName(AttributeType geomAttribute) {
        return this.encoder.escapeName(geomAttribute.getName());
    }

    /**
     * Generates the SELECT clause values to get the geometry min-max values.
     *
     * @param geomAttribute the geometry attribute.
     *
     * @return the string with the 4 column expressions.
     */
    String sqlGeometryMinMaxValues(AttributeType geomAttribute) {
        String sql;
        String gcName = sqlGeometryColumnName(geomAttribute);
        sql = "MIN(db2gse.ST_MinX(" + gcName + ")), " + "MIN(db2gse.ST_MinY("
            + gcName + ")), " + "MAX(db2gse.ST_MaxX(" + gcName + ")), "
            + "MAX(db2gse.ST_MaxY(" + gcName + ")) ";

        return sql;
    }

    /**
     * Construct the FROM clause for a feature type.  Prefixes the typeName
     * with the table schema provided when this class was constructed.
     * 
     * <p>
     * This method could be promoted to DefaultSQLBuilder if the table schema
     * was propagated up.
     * </p>
     * 
     * <p>
     * Overrides sqlFrom in DefaultSQLBuilder
     * </p>
     *
     * @param sql StringBuffer to be appended to
     * @param typeName Name of the type (table)
     */
    public void sqlFrom(StringBuffer sql, String typeName) {
        sql.append(" FROM ");
        sql.append(getSchemaTableName(typeName));
    }

    /**
     * Builds the SQL query to get the bounds (min-max coordinate values) of a
     * geometry column for a given filter.
     *
     * @param typeName the feature type name.
     * @param geomAttr the geometry attribute.
     * @param filter the filter expression.
     *
     * @return the string to perform the SQL query.
     *
     * @throws SQLEncoderException
     */
    public String buildSQLBoundsQuery(String typeName, AttributeType geomAttr,
        Filter filter) throws SQLEncoderException {
        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("SELECT ");
        sqlBuffer.append(sqlGeometryMinMaxValues(geomAttr));
        sqlFrom(sqlBuffer, typeName);
        sqlWhere(sqlBuffer, filter);

        String sqlStmt = sqlBuffer.toString();
        LOGGER.finer(sqlStmt);

        return sqlStmt;
    }

    /**
     * Gets the SQL encoder associated with this SQL builder.
     *
     * @return the associated encoder
     */
    SQLEncoderDB2 getEncoder() {
        return (SQLEncoderDB2) this.encoder;
    }

    /**
     * Gets the concatenated schema name and table name needed by DB2.
     *
     * @param tableName
     *
     * @return concatenated schema and table name
     */
    String getSchemaTableName(String tableName) {
        return escapeName(this.tableSchema) + "." + escapeName(tableName);
    }

    /**
     * Gets the concatenated schema name and table name needed by DB2.
     *
     * @return concatenated schema and table name
     */
    String getSchemaTableName() {
        return escapeName(this.tableSchema) + "." + escapeName(this.tableName);
    }

    /**
     * "escape" the specified name. This is currently delegated to the encoder
     * object and for DB2 this means that the specified name will be
     * surrounded by double-quote characters in order to ensure case
     * sensitivity.
     *
     * @param name
     *
     * @return escaped name
     */
    String escapeName(String name) {
        return this.encoder.escapeName(name);
    }
}
