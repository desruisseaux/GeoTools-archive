/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.jdbc.fidmapper;

import org.geotools.feature.Feature;
import java.io.IOException;
import java.rmi.server.UID;
import java.sql.Connection;
import java.sql.Statement;


/**
 * Last resort fid mapper for tables that does not have a primary key. It
 * allows reading the table getting unique FIDs by using the same mechanism
 * used by DefaultFeature, but the same Feature will receive a different FID
 * each time it is loaded from the datastore.
 *
 * @author wolf
 * @source $URL$
 */
public class NullFIDMapper extends AbstractFIDMapper {
    private static final long serialVersionUID = 1L;
    private static final String ARRAY_OUT_OF_BOUND_MESSAGE = "There are no columns in this FIDMapper";

    public NullFIDMapper() {
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getID(java.lang.Object[])
     */
    public String getID(Object[] attributes) {
        return (new UID()).toString();
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getPKAttributes(java.lang.String)
     */
    public Object[] getPKAttributes(String FID) throws IOException {
        return new Object[0];
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#createID(java.sql.Connection,
     *      org.geotools.feature.Feature, Statement)
     */
    public String createID(Connection conn, Feature feature, Statement statement)
        throws IOException {
        return getID(null);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#returnFIDColumnsAsAttributes()
     */
    public boolean returnFIDColumnsAsAttributes() {
        return false;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnCount()
     */
    public int getColumnCount() {
        return 0;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnName(int)
     */
    public String getColumnName(int colIndex) {
        throw new IndexOutOfBoundsException(ARRAY_OUT_OF_BOUND_MESSAGE);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnType(int)
     */
    public int getColumnType(int colIndex) {
        throw new IndexOutOfBoundsException(ARRAY_OUT_OF_BOUND_MESSAGE);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnSize(int)
     */
    public int getColumnSize(int colIndex) {
        throw new IndexOutOfBoundsException(ARRAY_OUT_OF_BOUND_MESSAGE);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnDecimalDigits(int)
     */
    public int getColumnDecimalDigits(int colIndex) {
        throw new IndexOutOfBoundsException(ARRAY_OUT_OF_BOUND_MESSAGE);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isAutoIncrement(int)
     */
    public boolean isAutoIncrement(int colIndex) {
        throw new IndexOutOfBoundsException(ARRAY_OUT_OF_BOUND_MESSAGE);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#initSupportStructures()
     */
    public void initSupportStructures() {
        // nothing to do        
    }

    /**
     * This FID mappers generates unique IDs out of the blue using {@link UID
     * UID}
     *
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isVolatile()
     */
    public boolean isVolatile() {
        return false;
    }
}
