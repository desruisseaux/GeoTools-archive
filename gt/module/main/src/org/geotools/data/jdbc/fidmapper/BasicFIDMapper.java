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
 *
 * TODO: 26-may-2005 D. Adler Removed returnFIDColumnsAsAttributes
 *                      variable and related accessor method.
 * 12-jul-2006 D. Adler GEOT-728 Refactor FIDMapper classes
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


    /**
     * Create a new BasicFIDMapper
     *
     * @param fieldName 
     * @param fieldSize 
     */
    public BasicFIDMapper(String fieldName, int fieldSize) {
        this(fieldName, fieldSize, false);
    }

    /**
     * Create a new BasicFIDMapper
     *
     * @param tableSchemaName
     * @param tableName
     * @param fieldName 
     * @param fieldSize 
     * @param returnFIDColumnsAsAttributes 
     */
    public BasicFIDMapper(String tableSchemaName, String tableName, String fieldName, int fieldSize,
        boolean returnFIDColumnsAsAttributes) {
    	super(tableSchemaName, tableName);
        this.returnFIDColumnsAsAttributes = returnFIDColumnsAsAttributes;
        setInfo(fieldName, Types.VARCHAR, fieldSize, 0, false);
    }
    
    /**
     * Create a new BasicFIDMapper
     *

     * @param fieldName 
     * @param fieldSize 
     * @param returnFIDColumnsAsAttributes 
     */
    public BasicFIDMapper(String fieldName, int fieldSize,
        boolean returnFIDColumnsAsAttributes) {
    	this(null, null, fieldName, fieldSize, returnFIDColumnsAsAttributes);
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
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof BasicFIDMapper)) {
            return false;
        }

        BasicFIDMapper other = (BasicFIDMapper) object;

        return other.getColumnName().equals(getColumnName())
        && (other.getColumnSize() == getColumnSize());
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

}
