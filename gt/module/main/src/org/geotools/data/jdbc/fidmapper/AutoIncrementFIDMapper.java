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
/*
 * Created on 18-apr-2004
 */
package org.geotools.data.jdbc.fidmapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Types;

import org.geotools.feature.Feature;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class AutoIncrementFIDMapper implements FIDMapper {
    private String colName;
    private int dataType;

    /**
     * DOCUMENT ME!
     *
     * @param colName
     * @param dataType
     */
    public AutoIncrementFIDMapper(String colName, int dataType) {
        this.colName = colName;
        this.dataType = dataType;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#initSupportStructures()
     */
    public void initSupportStructures() {
        // nothing to do
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getID(java.lang.Object[])
     */
    public String getID(Object[] attributes) {
        if ((attributes != null) && (attributes.length == 1)
                && (attributes[0] != null)) {
            return attributes[0].toString();
        } else {
            return null;
        }
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getPKAttributes(java.lang.String)
     */
    public Object[] getPKAttributes(String FID) throws IOException {
        Object pk = null;

        switch (dataType) {
        case Types.INTEGER:
            pk = new Integer(Integer.parseInt(FID));

            break;

        case Types.NUMERIC:
            pk = new Long(Long.parseLong(FID));

            break;
        }

        return new Object[] { pk };
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#createID(java.sql.Connection,
     *      org.geotools.feature.Feature, Statement)
     */
    public String createID(Connection conn, Feature feature, Statement statement)
        throws IOException {
        return null;
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
        return 1;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnName(int)
     */
    public String getColumnName(int colIndex) {
        if (colIndex == 0) {
            return (colName);
        } else {
            return null;
        }
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnType(int)
     */
    public int getColumnType(int colIndex) {
        if (colIndex == 0) {
            return this.dataType;
        } else {
            throw new ArrayIndexOutOfBoundsException(colIndex);
        }
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnSize(int)
     */
    public int getColumnSize(int colIndex) {
        return -1;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnDecimalDigits(int)
     */
    public int getColumnDecimalDigits(int colIndex) {
        return 0;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isAutoIncrement(int)
     */
    public boolean isAutoIncrement(int colIndex) {
        return true;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#hasAutoIncrementColumns()
     */
    public boolean hasAutoIncrementColumns() {
        return true;
    }

	/* (non-Javadoc)
	 * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isVolatile()
	 */
	public boolean isVolatile()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
