/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.mysql;

import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.filter.SQLEncoder;

/**
 * A MySQL-specific instance of DefaultSQLBuilder, which supports MySQL 4.1's geometric
 * datatypes.
 * @author Gary Sheppard garysheppard@psu.edu
 * @author Andrea Aime aaime@users.sourceforge.net
 * @source $URL$
 */
public class MySQLSQLBuilder extends DefaultSQLBuilder {
    
    /**
     * @deprecated please use MySQLSQLBuilder(encoder, ft)
     * @param encoder
     */
	public MySQLSQLBuilder(SQLEncoder encoder) {
        super(encoder);
    }

    public MySQLSQLBuilder(SQLEncoder encoder, FeatureType ft) {
        super(encoder, ft, null);
    }

    /**
     * Produces the select information required.
     * 
     * <p>
     * The featureType, if known, is always requested.
     * </p>
     * 
     * <p>
     * sql: <code>featureID (,attributeColumn)</code>
     * </p>
     * 
     * <p>
     * We may need to provide AttributeReaders with a hook so they can request
     * a wrapper function.
     * </p>
     *
     * @param sql
     * @param mapper     
     * @param attributes
     */
    public void sqlColumns(StringBuffer sql, FIDMapper mapper, AttributeType[] attributes) {
        for (int i = 0; i < mapper.getColumnCount(); i++) {
            sql.append(mapper.getColumnName(i));
            if (attributes.length > 0 || i < (mapper.getColumnCount() - 1)) {
                sql.append(", ");
            }
        }

        for (int i = 0; i < attributes.length; i++) {
            String colName = attributes[i].getName();

            if (attributes[i] instanceof GeometryAttributeType) {
                sql.append("AsText(" + attributes[i].getName() + ") AS " + attributes[i].getName());
            } else {
                sql.append(colName);
            }

            if (i < (attributes.length - 1)) {
                sql.append(", ");
            }
        }
    }

}
