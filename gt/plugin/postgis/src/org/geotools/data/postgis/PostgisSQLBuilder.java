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

import org.geotools.data.jdbc.DefaultSQLBuilder;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderPostgis;


/**
 * Builds sql for postgis.
 *
 * @author Chris Holmes
 */
public class PostgisSQLBuilder extends DefaultSQLBuilder {
    /** If true, WKB format is used instead of WKT */
    protected boolean WKBEnabled = false;
    
    /** If true, ByteA function is used to transfer WKB data*/
    protected boolean byteaEnabled = false;

    /**
     *
     */
    public PostgisSQLBuilder(int srid) {
        this((SQLEncoder) new SQLEncoderPostgis(srid));
    }

    /**
     * Constructor with encoder.
     *
     * @param encoder
     */
    public PostgisSQLBuilder(SQLEncoder encoder) {
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
            sql.append("\""+mapper.getColumnName(i)+"\""); //DJB: add quotes in.  NOTE: if FID  mapper isnt oid (ie. PK - Primary Key), you could be requesting PK columns multiple times 

            if ((attributes.length > 0) || (i < (mapper.getColumnCount() - 1))) {
                sql.append(", ");
            }
        }

        for (int i = 0; i < attributes.length; i++) {
            String colName = attributes[i].getName();

            if (attributes[i].isGeometry()) {
                
                if (WKBEnabled) {
                    if(byteaEnabled) {
                        sql.append("bytea(AsBinary(force_2d(\"" + colName + "\"), 'XDR'))");
                    } else {
                        sql.append("AsBinary(force_2d(\"" + colName + "\"), 'XDR')");
                    }
                } else {
                    sql.append("AsText(force_2d(\"" + colName + "\"))");
                }
            } else {
                sql.append("\"" + colName + "\"");
            }

            if (i < (attributes.length - 1)) {
                sql.append(", ");
            }
        }
    }

    /**
     * Consutrcts FROM clause for featureType
     * 
     * <p>
     * sql: <code>FROM typeName</code>
     * </p>
     *
     * @param sql
     * @param typeName
     */
    public void sqlFrom(StringBuffer sql, String typeName) {
        sql.append(" FROM ");
        sql.append("\"" + typeName + "\"");
    }

    /**
     * Constructs WHERE clause, if needed, for FILTER.
     * 
     * <p>
     * sql: <code>WHERE filter encoding</code>
     * </p>
     *
     * @param sql DOCUMENT ME!
     * @param preFilter DOCUMENT ME!
     *
     * @throws SQLEncoderException DOCUMENT ME!
     */
    public void sqlWhere(StringBuffer sql, Filter preFilter)
        throws SQLEncoderException {
        if ((preFilter != null) || (preFilter == Filter.NONE)) {
            String where = encoder.encode(preFilter);
            sql.append(" ");
            sql.append(where);
        }
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
    
    /**
     * Enables the use of the bytea function to transfer faster WKB geometries
     * @return
     */
    public boolean isByteaEnabled() {
        return byteaEnabled;
    }
    /**
     * Enables/disables the use of the bytea function
     * @param byteaEnable
     */
    public void setByteaEnabled(boolean byteaEnable) {
        byteaEnabled = byteaEnable;
    }
}
