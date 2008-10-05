/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.jdbc;

import java.sql.SQLException;

import javax.sql.DataSource;

public abstract class JDBCPrimaryKeyTestSetup extends JDBCDelegatingTestSetup {

    protected JDBCPrimaryKeyTestSetup(JDBCTestSetup delegate) {
        super( delegate );
    }
    
    protected final void setUpData() throws Exception {
        //kill all the data
        try {
            dropAutoGeneratedPrimaryKeyTable();
        } catch (SQLException e) {
        }
        try {
            dropSequencedPrimaryKeyTable();
        } catch (SQLException e) {
        }
        try {
            dropNonIncrementingPrimaryKeyTable();
        } catch (SQLException e) {
        }
        try {
            dropMultiColumnPrimaryKeyTable();
        } catch (SQLException e) {
        }
        //create all the data
        createAutoGeneratedPrimaryKeyTable();
        createSequencedPrimaryKeyTable();
        createNonIncrementingPrimaryKeyTable();
        createMultiColumnPrimaryKeyTable();
    }

    /**
     * Drops the "auto" table.
     */
    protected abstract void dropAutoGeneratedPrimaryKeyTable() throws Exception;

    /**
     * Drops the "seq" table.
     */
    protected abstract void dropSequencedPrimaryKeyTable() throws Exception;
    
    /**
     * Drops the "noninc" table.
     */
    protected abstract void dropNonIncrementingPrimaryKeyTable() throws Exception;
    
    /**
     * Drops the "multi" table.
     */
    protected abstract void dropMultiColumnPrimaryKeyTable() throws Exception;

    /**
     * Creates a table with auto-incrementing primary key column, which has the 
     * following schema:
     * <p>
     * auto( name:String; geom:Geometry; ) 
     * </p>
     * <p>
     * The table should be populated with the following data:
     *  "one" | NULL ; pkey = 1
     *  "two" | NULL ; pkey = 2
     *  "three" | NULL ; pkey = 3
     * </p>
     */
    protected abstract void createAutoGeneratedPrimaryKeyTable() throws Exception;
    
    /**
     * Creates a table with a primary key column with an associated sequence, which has the 
     * following schema:
     * <p>
     * seq( name:String; geom:Geometry; ) 
     * </p>
     * <p>
     * The table should be populated with the following data:
     *  "one" | NULL ; pkey = 1
     *  "two" | NULL ; pkey = 2
     *  "three" | NULL ; pkey = 3
     * </p>
     */
    protected abstract void createSequencedPrimaryKeyTable() throws Exception;
    
    /**
     * Creates a table with a non incrementing primary key column, which has the 
     * following schema:
     * <p>
     * noninc( name:String; geom:Geometry; ) 
     * </p>
     * <p>
     * The table should be populated with the following data:
     *  "one" | NULL ; pkey = 1
     *  "two" | NULL ; pkey = 2
     *  "three" | NULL ; pkey = 3
     * </p>
     */
    protected abstract void createNonIncrementingPrimaryKeyTable() throws Exception;
    
    /**
     * Creates a table with a primary key which is made up of multiple columns, which has the 
     * following schema:
     * <p>
     * multi( name:String, geom: Geometry );
     *  </p>
     *  <p>
     *  The table should be populated with the following data:
     *  "one" | NULL ; pkey1 = 1, pkey2 = 'x'
     *  "two" | NULL ; pkey1 = 1, pkey2 = 'y'
     *  "three" | NULL ; pkey1 = 1, pkey2 = 'z'
     *  </p>
     */
    protected abstract void createMultiColumnPrimaryKeyTable() throws Exception;

   
}
