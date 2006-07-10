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

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.FeatureReader;
import org.geotools.data.db2.filter.SQLEncoderDB2;
import org.geotools.data.jdbc.FeatureTypeInfo;
import org.geotools.data.jdbc.JDBCFeatureWriter;
import org.geotools.data.jdbc.JDBCTextFeatureWriter;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import java.io.IOException;
import java.sql.Types;


/**
 * DOCUMENT ME!
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2FeatureWriter extends JDBCTextFeatureWriter {
    private DB2SQLBuilder sqlBuilder;

    /**
     * DOCUMENT ME!
     *
     * @param reader
     * @param queryData
     * @param sqlBuilder DOCUMENT ME!
     *
     * @throws IOException
     */
    public DB2FeatureWriter(FeatureReader reader, QueryData queryData,
        DB2SQLBuilder sqlBuilder) throws IOException {
        super(reader, queryData);
        this.sqlBuilder = sqlBuilder;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.JDBCTextFeatureWriter#getGeometryInsertText(com.vividsolutions.jts.geom.Geometry, int)
     */
    protected String getGeometryInsertText(Geometry geom, int srid)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Generates the query for the sql delete statement
     *
     * @param feature
     *
     * @return
     *
     * @throws IOException
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    protected String makeDeleteSql(Feature feature) throws IOException {
        FIDMapper mapper = this.queryData.getMapper();

        StringBuffer statementSQL = new StringBuffer("DELETE FROM "
                + this.sqlBuilder.getSchemaTableName() + " WHERE ");
        Object[] pkValues = mapper.getPKAttributes(feature.getID());

        if (mapper.getColumnCount() == 0) {
            // can't delete without a primary key
            throw new UnsupportedOperationException();
        }

        for (int i = 0; i < mapper.getColumnCount(); i++) {
            statementSQL.append(this.sqlBuilder.escapeName(mapper.getColumnName(
                        i))).append(" = ");

            // don't put quotes around numeric values 
            if (isTypeNumeric(mapper.getColumnType(i))) {
                statementSQL.append(pkValues[i]);
            } else {
                statementSQL.append(addQuotes(pkValues[i]));
            }

            if (i < (mapper.getColumnCount() - 1)) {
                statementSQL.append(" AND ");
            }
        }

        return (statementSQL.toString());
    }

    /**
     * Checks if column type is numeric
     *
     * @param columnType
     *
     * @return numeric
     */
    protected boolean isTypeNumeric(int columnType) {
        boolean numeric = false;

        if ((columnType == Types.BIT) || (columnType == Types.TINYINT)
                || (columnType == Types.SMALLINT)
                || (columnType == Types.INTEGER)
                || (columnType == Types.BIGINT) || (columnType == Types.FLOAT)
                || (columnType == Types.REAL) || (columnType == Types.DOUBLE)
                || (columnType == Types.NUMERIC)
                || (columnType == Types.DECIMAL)) {
            numeric = true;
        }

        return (numeric);
    }
}
