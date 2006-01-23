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
 * 26-may-2005 D. Adler Removed returnFIDColumnsAsAttributes
 *                      variable and related accessor method.
 */
package org.geotools.data.jdbc.fidmapper;

import org.geotools.feature.Feature;
import java.io.IOException;
import java.rmi.server.UID;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Types;


/**
 * Basic FIDMapper implementation that maps the FID of a  Feature to a VARCHAR
 * column
 *
 * @author aaime Andrea Aime
 * @author Dani Daniele Franzoni
 * @source $URL$
 */
public class BasicFIDMapper extends AbstractFIDMapper {
    private static final long serialVersionUID = 1L;

    /** The name of the field of the primary key */
    private final String fieldName;

    /** The size of the field of the primary key */
    private final int fieldSize;

    /**
     * Create a new BasicFIDMapper
     *
     * @param fieldName DOCUMENT ME!
     * @param fieldSize DOCUMENT ME!
     */
    public BasicFIDMapper(String fieldName, int fieldSize) {
        this(fieldName, fieldSize, false);
    }

    /**
     * Create a new BasicFIDMapper
     *
     * @param fieldName DOCUMENT ME!
     * @param fieldSize DOCUMENT ME!
     * @param returnFIDColumnsAsAttributes DOCUMENT ME!
     */
    public BasicFIDMapper(String fieldName, int fieldSize,
        boolean returnFIDColumnsAsAttributes) {
        this.fieldName = fieldName;
        this.fieldSize = fieldSize;
        this.returnFIDColumnsAsAttributes = returnFIDColumnsAsAttributes;
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
    public Object[] getPKAttributes(String FID) {
        return new Object[] { FID };
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
            return (fieldName);
        } else {
            return null;
        }
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnType(int)
     */
    public int getColumnType(int colIndex) {
        if (colIndex == 0) {
            return (Types.VARCHAR);
        } else {
            throw new ArrayIndexOutOfBoundsException(colIndex);
        }
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnSize(int)
     */
    public int getColumnSize(int colIndex) {
        if (colIndex == 0) {
            return (fieldSize);
        } else {
            return 0;
        }
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnDecimalDigits(int)
     */
    public int getColumnDecimalDigits(int colIndex) {
        return (0);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isAutoIncrement(int)
     */
    public boolean isAutoIncrement(int colIndex) {
        return false;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof BasicFIDMapper)) {
            return false;
        }

        BasicFIDMapper other = (BasicFIDMapper) object;

        return other.fieldName.equals(fieldName)
        && (other.fieldSize == fieldSize);
    }

    /**
     * This kind of FIDMapper does not generate keys, they must be already
     * present in the primary key.
     *
     * @see org.geotools.data.fidmapper.FIDMapper#createID(Connection, Feature,
     *      Statement)
     */
    public String createID(Connection conn, Feature feature, Statement statement)
        throws IOException {
        return (new UID()).toString();
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#initSupportStructures()
     */
    public void initSupportStructures() {
        // nothing to do
    }
}
