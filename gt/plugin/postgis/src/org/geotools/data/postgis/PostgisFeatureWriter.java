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
package org.geotools.data.postgis;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.jdbc.JDBCTextFeatureWriter;
import org.geotools.data.jdbc.QueryData;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class PostgisFeatureWriter extends JDBCTextFeatureWriter {

    /** Well Known Text writer (from JTS). */
    protected static WKTWriter geometryWriter = new WKTWriter();

    public PostgisFeatureWriter(FeatureReader fReader, QueryData queryData) throws IOException {
        super(fReader, queryData);
    }

    protected String getGeometryInsertText(Geometry geom, int srid) {
            String geoText = geometryWriter.write(geom);
            String sql = "GeometryFromText('" + geoText + "', " + srid + ")";

            return sql;
        }
}
