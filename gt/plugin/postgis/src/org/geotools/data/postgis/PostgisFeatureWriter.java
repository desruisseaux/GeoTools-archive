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
import org.geotools.data.postgis.attributeio.WKBEncoder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class PostgisFeatureWriter extends JDBCTextFeatureWriter {

    /** Well Known Text writer (from JTS). */
    protected static WKTWriter geometryWriter = new WKTWriter();
    private boolean WKBEnabled;
    

    public PostgisFeatureWriter(FeatureReader fReader, QueryData queryData, boolean WKBEnabled) throws IOException {
        super(fReader, queryData);
        this.WKBEnabled = WKBEnabled;
    }

    protected String getGeometryInsertText(Geometry geom, int srid) throws IOException {
        if(WKBEnabled) {
            String wkb = WKBEncoder.encodeGeometryHex(geom);
            return "GeomFromWKB('" + wkb + "', " + srid + ")";
        }
            String geoText = geometryWriter.write(geom);
            return "GeometryFromText('" + geoText + "', " + srid + ")";
        }
    

    /**
     * Returns true if the WKB format is used to transfer geometries, false
     * otherwise
     *
     * @return
     */
    public boolean isWKBEnabled() {
        return WKBEnabled;
    }

    /**
     * If turned on, WKB will be used to transfer geometry data instead of  WKT
     *
     * @param enabled
     */
    public void setWKBEnabled(boolean enabled) {
        WKBEnabled = enabled;
    }
}
