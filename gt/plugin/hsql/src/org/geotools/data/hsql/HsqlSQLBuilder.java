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
package org.geotools.data.hsql;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.filter.SQLEncoder;


/**
 * A HSQL-specific instance of DefaultSQLBuilder, which supports MySQL 4.1's
 * geometric datatypes.
 *
 * @author Amr Alam, Refractions Research
 * @author Gary Sheppard garysheppard&#64;psu.edu
 * @author Andrea Aime aaime&#64;users.sourceforge.net
 * @source $URL$
 */
public class HsqlSQLBuilder extends DefaultSQLBuilder {
    public HsqlSQLBuilder(SQLEncoder encoder) {
        super(encoder);
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
    public void sqlColumns(StringBuffer sql, FIDMapper mapper,
        AttributeType[] attributes) {
        for (int i = 0; i < mapper.getColumnCount(); i++) {
            sql.append(mapper.getColumnName(i));

            if ((attributes.length > 0) || (i < (mapper.getColumnCount() - 1))) {
                sql.append(", ");
            }
        }

        for (int i = 0; i < attributes.length; i++) {
            String colName = attributes[i].getName();

            //if (attributes[i].isGeometry()) {
            if (Geometry.class.isAssignableFrom(attributes[i].getType())) {
                //Don't think we need this...geometries are stored as text in the DB
                //sql.append("toText(" + attributes[i].getName() + ") AS " + attributes[i].getName());
                sql.append(colName);
            } else {
                sql.append(colName);
            }

            if (i < (attributes.length - 1)) {
                sql.append(", ");
            }
        }
    }
}
