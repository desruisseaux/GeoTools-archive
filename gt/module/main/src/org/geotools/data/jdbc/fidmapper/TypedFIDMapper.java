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
package org.geotools.data.jdbc.fidmapper;

import java.io.IOException;
import java.sql.Connection;

import org.geotools.feature.Feature;



/**
 * This fidmapper just takes another fid mapper and builds fids 
 * based on the wrapped FIDMapper by prefixing them with the feature
 * type name, that is, the resulting fid follow the &ltfeatureTypeName&gt.&ltbasic_fid&gt
 * pattern.
 * @author wolf
 */
public class TypedFIDMapper extends AbstractFIDMapper {
    private String featureTypeName;
    private FIDMapper wrapped;

    /**
     * Creates a new TypedFIDMapper object.
     *
     * @param FIDColumn 
     * @param featureTypeName 
     */
    public TypedFIDMapper(FIDMapper wrapped, String featureTypeName) {
        if(wrapped == null)
            throw new IllegalArgumentException("The wrapped feature mapper cannot be null");
        
        if(featureTypeName == null)
            throw new IllegalArgumentException("The featureTypeName cannot be null");
        
        this.wrapped = wrapped;
        this.featureTypeName = featureTypeName;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getID(java.lang.Object[])
     */
    public String getID(Object[] attributes) {
        return featureTypeName + "." + wrapped.getID(attributes);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getPKAttributes(java.lang.String)
     */
    public Object[] getPKAttributes(String FID) throws IOException {
        int pos = FID.indexOf(".");

        return wrapped.getPKAttributes(FID.substring(pos + 1));
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#returnFIDColumnsAsAttributes()
     */
    public boolean returnFIDColumnsAsAttributes() {
        return wrapped.returnFIDColumnsAsAttributes();
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnCount()
     */
    public int getColumnCount() {
        return wrapped.getColumnCount();
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnName(int)
     */
    public String getColumnName(int colIndex) {
        return wrapped.getColumnName(colIndex);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnType(int)
     */
    public int getColumnType(int colIndex) {
        return wrapped.getColumnType(colIndex);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnSize(int)
     */
    public int getColumnSize(int colIndex) {
        return wrapped.getColumnSize(colIndex);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnDecimalDigits(int)
     */
    public int getColumnDecimalDigits(int colIndex) {
        return wrapped.getColumnSize(colIndex);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isAutoIncrement(int)
     */
    public boolean isAutoIncrement(int colIndex) {
        return wrapped.isAutoIncrement(colIndex);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof TypedFIDMapper)) {
            return false;
        }

        TypedFIDMapper other = (TypedFIDMapper) object;

        return other.wrapped.equals(wrapped)
        && (other.featureTypeName == featureTypeName);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#createID(java.sql.Connection, org.geotools.feature.Feature)
     */
    public String createID(Connection conn, Feature feature) throws IOException {
        return featureTypeName + "." + wrapped.createID(conn, feature);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#initSupportStructures()
     */
    public void initSupportStructures() {
        wrapped.initSupportStructures();        
    }
}
