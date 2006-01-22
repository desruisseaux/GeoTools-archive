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
package org.geotools.data.postgis.attributeio;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.wkb4j.engine.WKBParser;
import org.wkb4j.jts.JTSFactory;

import com.vividsolutions.jts.geom.Geometry;


/**
 * An attribute IO implementation that can manage the WKB
 *
 * @author Andrea Aime
 * @source $URL$
 */
public class PgWKBAttributeIO implements AttributeIO {
    private boolean useByteArray;
    
    public PgWKBAttributeIO(boolean useByteArray) {
        this.useByteArray = useByteArray;
    }
    
    /**
     * Turns a char that encodes four bits in hexadecimal notation into a byte
     *
     * @param c
     *
     * @return
     */
    public static byte getFromChar(char c) {
        if (c <= '9') {
            return (byte) (c - '0');
        } else if (c <= 'F') {
            return (byte) (c - 'A' + 10);
        } else {
            return (byte) (c - 'a' + 10);
        }
    }

    /**
     * This method will convert a String of hex characters that represent  the
     * hexadecimal form of a Geometry in Well Known Binary representation to a
     * JTS  Geometry object.
     *
     * @param wkb a String of hex characters where each character  represents a
     *        hex value. In particular, each character is a value of 0-9, A, B
     *        ,C, D, E, or F.
     *
     * @return a JTS Geometry object that is equivalent to the WTB
     *         representation passed in by param wkb
     *
     * @throws IOException if more than one geometry object was found in  the
     *         WTB representation, or if the parser could not parse the WKB
     *         representation.
     */
    private Geometry WKB2Geometry(byte[] wkbBytes)
        throws IOException {
        // convert the byte[] to a JTS Geometry object
    	
    	if (wkbBytes == null)  //DJB: null value from database --> null geometry (the same behavior as WKT).  NOTE: sending back a GEOMETRYCOLLECTION(EMPTY) is also a possibility, but this is not the same as NULL 
    		return null; 
    	
        JTSFactory factory = new JTSFactory();
        WKBParser parser = new WKBParser(factory);
        try {
            parser.parseData(wkbBytes, 42102);
        } catch (Exception e) {
            throw new DataSourceException("An exception occurred while parsing WKB data", e);
        }

        ArrayList geoms = factory.getGeometries();

        if (geoms.size() > 0) {
            return (Geometry) geoms.get(0);
        } else if (geoms.size() > 1) {
            throw new IOException(
                "Found more than one Geometry in WKB representation ");
        } else {
            throw new IOException(
                "Could not parse WKB representations -  found no Geometries ");
        }
    }
    
    private byte[] hexToBytes(String wkb) {
      // convert the String of hex values to a byte[]
      byte[] wkbBytes = new byte[wkb.length() / 2];

      for (int i = 0; i < wkbBytes.length; i++) {
          byte b1 = getFromChar(wkb.charAt(i * 2));
          byte b2 = getFromChar(wkb.charAt((i * 2) + 1));
          wkbBytes[i] = (byte) ((b1 << 4) | b2);
      }
      
      return wkbBytes;
    }
    
    private byte[] byteaToBytes(byte[] bytes) {
        for(int i = 0; i < bytes.length; i++) {
            if(bytes[i] >= 'A')
                bytes[i] -= ('A' + 10);
            else
                bytes[i] -= '0';
        }
        return bytes;
    }

    /**
     * @see org.geotools.data.jdbc.attributeio.AttributeIO#read(java.sql.ResultSet,
     *      int)
     */
    public Object read(ResultSet rs, int position) throws IOException {
        try {
            if(useByteArray) {
                return WKB2Geometry(rs.getBytes(position));
            } else {
                return WKB2Geometry(hexToBytes(rs.getString(position)));
            }
        } catch (SQLException e) {
            throw new DataSourceException("SQL exception occurred while reading the geometry.", e);
        }
    }

    /**
     * @param metaData
     * @throws SQLException
     */
    private void printMetadata(ResultSetMetaData md) throws SQLException {
        for (int i = 1; i <= md.getColumnCount(); i++) {
            System.out.println(i + " " + md.getColumnName(i) + " " + md.getColumnTypeName(i));
        }
        
    }

    /**
     * Unsupported, will throw an UnsupportedOperationException
     * @see org.geotools.data.jdbc.attributeio.AttributeIO#write(java.sql.ResultSet,
     *      int, java.lang.Object)
     */
    public void write(ResultSet rs, int position, Object value)
        throws IOException {
        try {
            if(value == null) {
                rs.updateNull(position);
            } else {
                rs.updateString(position, WKBEncoder.encodeGeometryHex((Geometry) value));
            }
        } catch (SQLException e) {
            throw new DataSourceException("SQL exception occurred while reading the geometry.", e);
        }
    }

    /**
     * @see org.geotools.data.jdbc.attributeio.AttributeIO#write(java.sql.PreparedStatement, int, java.lang.Object)
     */
    public void write(PreparedStatement ps, int position, Object value) throws IOException {
        try {
            if(value == null) {
                ps.setNull(position, Types.OTHER);
            } else {
                ps.setString(position, WKBEncoder.encodeGeometryHex((Geometry) value));
            }
        } catch (SQLException e) {
            throw new DataSourceException("SQL exception occurred while reading the geometry.", e);
        }
        
    }
}
