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

package org.geotools.data.oracle;

import oracle.jdbc.OracleConnection;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;


/**
 * Provides javax.sql.DataSource wrapper around an OracleConnection object.
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: cholmesny $
 * @version $Id: OracleConnectionFactory.java,v 1.1 2003/07/08 15:50:48 cholmesny Exp $
 */
public class OracleConnectionFactory implements DataSource {
    private static final String JDBC_PATH = "jdbc:oracle:thin:@";
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private String dbUrl;
    private String username = "";
    private String passwd = "";

    /**
     * Creates a new OracleConnection object that wraps a oracle.jdbc.driver.OracleConnection.
     *
     * @param host The host to connect to.
     * @param port The port number on the host
     * @param instance The instance name on the host
     */
    public OracleConnectionFactory(String host, String port, String instance) {
        dbUrl = JDBC_PATH + host + ":" + port + ":" + instance;
    }

    /**
     * Creates the real OracleConnection. Logs in to the Oracle Database and creates the
     * OracleConnection object.
     *
     * @param user The user name.
     * @param pass The password
     *
     * @return The real OracleConnection object.
     *
     * @throws SQLException
     */
    public OracleConnection getOracleConnection(String user, String pass)
        throws SQLException {
        OracleConnection conn = null;

        try {
            Class.forName(JDBC_DRIVER);
            conn = (OracleConnection) DriverManager.getConnection(dbUrl, user, pass);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle Driver Not Found");
        }

        return conn;
    }

    /**
     * Creates the real OracleConnection.  Logs into the database using the credentials provided by
     * setLogin
     *
     * @return
     *
     * @throws SQLException
     */
    public OracleConnection getOracleConnection() throws SQLException {
        return getOracleConnection(username, passwd);
    }

    /**
     * Creates a JDBC Connection object for this OracleConnection.
     *
     * @return A JDBC Connection for this DataSource.
     *
     * @throws SQLException If an error occurs when connecting.
     */
    public Connection getConnection() throws SQLException {
        return getOracleConnection(this.username, this.passwd);
    }

    /**
     * Creates a JDBC Connection object for this OracleConnection.
     *
     * @param username The username to login with.
     * @param password The password to login with.
     *
     * @return A JDBC Connection for this DataSource.
     *
     * @throws SQLException If an error occurs when connecting.
     */
    public Connection getConnection(String username, String password)
        throws SQLException {
        return getOracleConnection(username, password);
    }

    /**
     * Gets the Log writer.
     *
     * @return stout wrapped in a Printwriter.
     *
     * @throws SQLException Wont happen.
     */
    public PrintWriter getLogWriter() throws SQLException {
        return new PrintWriter(System.out);
    }

    /**
     * Sets the Log Writer.  A NO-OP
     *
     * @param out The new LogWriter
     *
     * @throws SQLException Wont happen.
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    /**
     * Sets the Login timeout.  A NO-OP
     *
     * @param seconds Ignored
     *
     * @throws SQLException Won't happen
     */
    public void setLoginTimeout(int seconds) throws SQLException {
    }

    /**
     * Gets the login timeout
     *
     * @return The login timeout - defaults to 10.
     *
     * @throws SQLException Wont happen
     */
    public int getLoginTimeout() throws SQLException {
        return 10;
    }

    /**
     * Sets the login credentials.
     *
     * @param user The username
     * @param pass The password
     */
    public void setLogin(String user, String pass) {
        this.username = user;
        this.passwd = pass;
    }
}
