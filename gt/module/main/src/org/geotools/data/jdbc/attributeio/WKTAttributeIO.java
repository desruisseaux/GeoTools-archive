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
package org.geotools.data.jdbc.attributeio;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import org.geotools.data.DataSourceException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


/**
 * An attribute IO object that can read and write geometries encoded into
 * WKT format. 
 *
 * @author wolf
 */
public class WKTAttributeIO implements AttributeIO {
    WKTReader reader;
    WKTWriter writer;

    /**
     * Lazily initialize the WKTReader
     *
     * @return
     */
    private WKTReader getWKTReader() {
        if (reader == null) {
            reader = new WKTReader();
        }

        return reader;
    }

    /**
     * Lazily initialize the WKTWriter
     *
     * @return
     */
    private WKTWriter getWKTWriter() {
        if (writer == null) {
            writer = new WKTWriter();
        }

        return writer;
    }

    /**
     * @see org.geotools.data.jdbc.attributeio.AttributeIO#read(java.sql.ResultSet,
     *      int)
     */
    public Object read(ResultSet rs, int position) throws IOException {
        try {
            String wkt = rs.getString(position);

            return getWKTReader().read(wkt);
        } catch (SQLException e) {
            throw new DataSourceException("Sql reading problem", e);
        } catch (ParseException e) {
            throw new DataSourceException("Could not parse WKT", e);
        }
    }

    /**
     * @see org.geotools.data.jdbc.attributeio.AttributeIO#write(java.sql.ResultSet,
     *      int, java.lang.Object)
     */
    public void write(ResultSet rs, int position, Object value)
        throws IOException {
        try {
            if (value == null) {
                rs.updateNull(position);
            } else {
                Geometry g = (Geometry) value;
                String wkt = getWKTWriter().write(g);
                rs.updateString(position, wkt);
            }
        } catch (Exception e) {
            throw new DataSourceException("Sql writing problem", e);
        }
    }
    
    /**
     * @see org.geotools.data.jdbc.attributeio.AttributeIO#write(java.sql.ResultSet,
     *      int, java.lang.Object)
     */
    public void write(PreparedStatement ps, int position, Object value)
        throws IOException {
        try {
            if (value == null) {
                ps.setNull(position, Types.VARCHAR);
            } else {
                Geometry g = (Geometry) value;
                String wkt = getWKTWriter().write(g);
                ps.setString(position, wkt);
            }
        } catch (Exception e) {
            throw new DataSourceException("Sql writing problem", e);
        }
    }
}
