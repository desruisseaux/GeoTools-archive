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
package org.geotools.data.geometryless;

import java.util.logging.Logger;

import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.filter.SQLEncoder;


/**
 * A an extension of DefaultSQLBuilder, which supports point geometries  that
 * are specified with x,y columns
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 */
public class BBOXSQLBuilder extends DefaultSQLBuilder {
    /** The logger for the mysql module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.geometryless");
    private String XMinColumnName = null;
    private String YMinColumnName = null;
    private String XMaxColumnName = null;
    private String YMaxColumnName = null;

    public BBOXSQLBuilder(SQLEncoder encoder, String minx, String miny, String maxx, String maxy) {
        super(encoder);
        this.XMinColumnName = minx;
        this.YMinColumnName = miny;
        this.XMaxColumnName = maxx;
        this.YMaxColumnName = maxy;
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
            LOGGER.finest(mapper.getColumnName(i));
            sql.append(mapper.getColumnName(i));

            if ((attributes.length > 0) || (i < (mapper.getColumnCount() - 1))) {
                sql.append(", ");
            }
        }

        for (int i = 0; i < attributes.length; i++) {
            String colName = attributes[i].getName();

            LOGGER.finest(attributes[i].getName() + " isGeom: "
                + attributes[i].isGeometry());

            //Here we want the x and y columns to be requested.
            if (attributes[i].isGeometry()) {
                sql.append(        XMinColumnName + "," + YMinColumnName  + ", " + XMaxColumnName+ ", " + YMaxColumnName);

                //"AsText(" + attributes[i].getName() + ") AS " + attributes[i].getName());
            } else {
                sql.append(colName);
            }

            if (i < (attributes.length - 1)) {
                sql.append(", ");
            }
        }
    }
}
