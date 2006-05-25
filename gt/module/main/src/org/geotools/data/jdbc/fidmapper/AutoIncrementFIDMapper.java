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
 * 26-may-2005 D. Adler Make subclass of AbstractFIDFilter.
 */
package org.geotools.data.jdbc.fidmapper;

import org.geotools.feature.Feature;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;


/**
 * Support primary key columns that are automatically generated by the
 * database.
 *
 * @author wolf
 * @source $URL$
 */
public class AutoIncrementFIDMapper extends AbstractFIDMapper {
    private static final long serialVersionUID = 1L;
    protected String colName;
    protected int dataType;
    protected String tableName;

    /**
     * DOCUMENT ME!
     *
     * @param colName
     * @param dataType
     */
    public AutoIncrementFIDMapper(String colName, int dataType) {
    	this.tableName=tableName;
        this.colName = colName;
        this.dataType = dataType;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param colName
     * @param dataType
     */
    public AutoIncrementFIDMapper(String tableName, String colName, int dataType) {
    	this.tableName=tableName;
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
        	try {
        		pk = new Integer(Integer.parseInt(FID));
	        } catch(NumberFormatException nfe) {
	    		//if we get a really bad featureid we want to return something
	    	    //that will not mess up the database and throw an exception,
	    	    //we just want to not match against it, so we return -1
	    	    pk = new Integer(-1);
	    	}
            break;

        case Types.NUMERIC:
        	try {
        		pk = new Long(Long.parseLong(FID));
        	} catch(NumberFormatException nfe) {
        		//if we get a really bad featureid we want to return something
        	    //that will not mess up the database and throw an exception,
        	    //we just want to not match against it, so we return -1
        	    pk = new Integer(-1);
        	}
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
    	if( tableName==null || colName==null )
    		return null;
    	try {
    		String sql="select "+this.colName+" from "+tableName+" order by "+colName+" desc limit 1";
    		statement.execute(sql);
    		ResultSet resultSet = statement.getResultSet();
    		if( resultSet.next() )
    			return resultSet.getString(this.colName);
    		else
    			return null;
		} catch (SQLException e) {
			throw (IOException) new IOException().initCause(e);
		}
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

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isVolatile()
     */
    public boolean isVolatile() {
        // TODO Auto-generated method stub
        return false;
    }
}
