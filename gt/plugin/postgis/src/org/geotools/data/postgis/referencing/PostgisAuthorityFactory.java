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
package org.geotools.data.postgis.referencing;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.jdbc.referencing.JDBCAuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author jeichar
 */
public class PostgisAuthorityFactory extends JDBCAuthorityFactory {

    private String TABLE_NAME="SPATIAL_REF_SYS";
    private String WKT_COLUMN="SRTEXT";
    private String SRID_COLUMN="SRID";

    /**
     * Construct <code>PostgisAuthorityFactory</code>.
     *
     * @param pool
     */
    public PostgisAuthorityFactory( ConnectionPool pool ) {
        super(pool);
    }
    
    CoordinateReferenceSystem createCRS(int srid) throws FactoryException{
        Connection dbConnection = null;

        try {
            String sqlStatement = "SELECT "+WKT_COLUMN+" FROM "+TABLE_NAME+" WHERE "+SRID_COLUMN+" = "+srid;
            dbConnection = getConnection(Transaction.AUTO_COMMIT);

            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                String wkt = result.getString("srid");
                JDBCUtils.close(statement);

                return factory.createFromWKT(wkt);
            } else {
                String mesg = "No wkt column row for srid in table: "+ TABLE_NAME;
                throw new FactoryException(mesg);
            }
        } catch (SQLException sqle) {
            String message = sqle.getMessage();

            throw new FactoryException(message, sqle);
        } catch (IOException e) {
            throw new FactoryException(e.getMessage(), e);
        } finally {
            JDBCUtils.close(dbConnection, Transaction.AUTO_COMMIT, null);
        }
    }
    


}
