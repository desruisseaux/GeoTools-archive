/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.util.Properties;
import java.util.logging.LogRecord;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.AbstractFactory;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.JDBC;


/**
 * Open a connection to an EPSG database using {@link DriverManager}. This implementation is not
 * suitable for uses with Java Naming and Directory (JNDI), but it provides a convenient base class
 * for connections determined from properties files.
 * <p>
 * The default implementation doesn't make any assumption about where the properties file is
 * located. The properties file location must be supplied at construction time. Because this
 * base class dosn't have a no-argument constructor, it can't be registered in the
 * {@code META-INF/services/} directory. If such a registration is wanted (for automatic detection
 * by {@link org.geotools.referencing.FactoryFinder}, then users must provide a subclass with a
 * no-argument constructor.
 * <p>
 * The properties file to be supplied at construction time shall contains the following:
 * <p>
 *
 * <TABLE BORDER="1">
 * <TR>
 *   <TH>Property</TH>
 *   <TH>Description</TH>
 *   <TH>Default</TH>
 * </TR>
 * <TR>
 *   <TD>{@code url}</TD>
 *   <TD>The URL to the EPSG database</TD>
 *   <TD>{@code jdbc:odbc:EPSG}</TD>
 * </TR>
 * <TR>
 *   <TD>{@code schema}</TD>
 *   <TD>The schema containing EPSG tables in the database</TD>
 *   <TD></TD>
 * </TR>
 * <TR>
 *   <TD>{@code user}</TD>
 *   <TD>User used to make database connections</TD>
 *   <TD></TD>
 * </TR>
 * <TR>
 *   <TD>{@code password}</TD>
 *   <TD>Password used to make database connections</TD>
 *   <TD></TD>
 * </TR>
 * <TR>
 *   <TD>{@code driver}</TD>
 *   <TD>The JDBC driver to load</TD>
 *   <TD>{@code sun.jdbc.odbc.JdbcOdbcDriver}</Td>
 * </TR>
 * <TR>
 *   <TD>{@code factory}</TD>
 *   <TD>The EPSG factory to instantiate</TD>
 *   <TD>{@code org.geotools.referencing.factory.epsg.FactoryUsingSQL}</TD>
 * </TR>
 * </TABLE>
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SimpleDataSource implements DataSource {
    /**
     * The properties used for etablishing the database connection. See the
     * {@linkplain SimpleDataSource class description} for the expected content.
     * This field is never {@code null}.
     */
    protected final Properties properties;

    /**
     * Constructs a data source with the specified properties.
     * The properties given to this constructor are not cloned.
     *
     * @param properties The properties, or {@code null} for an empty set.
     */
    public SimpleDataSource(final Properties properties) {
        this.properties = (properties!=null) ? properties : new Properties();
    }

    /**
     * Constructs a data source with properties loaded from the specified file.
     *
     * @param  file The properties file to load.
     * @throws IOException if an error occured while loading the properties file.
     */
    public SimpleDataSource(final File file) throws IOException {
        properties = new Properties();
        final InputStream in = new FileInputStream(file);
        properties.load(in);
        in.close();
    }

    /**
     * Retrieves the log writer. The default implementation delegates to the {@link DriverManager},
     * which returns a system-wide logger.
     */
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    /**
     * Sets the log writer. The default implementation delegates to the {@link DriverManager},
     * which have a system-wide effect.
     */
    public void setLogWriter(final PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    /**
     * Gets the maximum time in seconds that this data source can wait while attempting to
     * connect to a database.  The default implementation returns zero, which means that the
     * timeout is the default system timeout .
     */
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    /**
     * Sets the maximum time in seconds that this data source will wait while attempting to
     * connect to a database. The default implementation does nothing.
     */
    public void setLoginTimeout(final int seconds) throws SQLException {
    }

    /**
     * Returns the priority for this data source. The default implementation returns
     * {@link #NORMAL_PRIORITY NORMAL_PRIORITY}.
     */
    public int getPriority() {
        return NORMAL_PRIORITY;
    }

    /**
     * Returns the URL to the database.
     */
    private String getURL() {
        final LogRecord log;
        log = JDBC.loadDriver(properties.getProperty("driver", "sun.jdbc.odbc.JdbcOdbcDriver"));
        if (log != null) {
            log.setSourceClassName("SimpleDataSource");
            log.setSourceMethodName("getConnection");
            AbstractFactory.LOGGER.log(log);
        }
        return properties.getProperty("url", "jdbc:odbc:EPSG");
    }

    /**
     * Attempts to establish a connection with the data source.
     *
     * @return    A connection to the data source.
     * @exception SQLException if a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getURL(), properties);
    }

    /**
     * Attempts to establish a connection with the data source.
     *
     * @param     username The database user on whose behalf the connection is being made.
     * @param     password The user's password.
     * @return    A connection to the data source.
     * @exception SQLException if a database access error occurs.
     */
    public Connection getConnection(final String username, final String password) throws SQLException {
        return DriverManager.getConnection(getURL(), username, password);
    }

    /**
     * Opens a connection and creates an EPSG factory for it. The default implementation attempts
     * to create an instance of the class specified by the {@code "factory"} key. This class shall
     * have a constructor expecting the following arguments in that order: {@link Hints},
     * {@link Connection}.
     */
    public AbstractAuthorityFactory createFactory(final Hints hints) throws SQLException {
        final Connection connection = getConnection();
        String classname = properties.getProperty("factory");
        if (classname == null) {
            classname = "org.geotools.referencing.factory.epsg.FactoryUsingSQL";
            final String driver = properties.getProperty("driver");
            if (driver != null) {
                // TODO: 'contains' instead of 'indexOf' when we will be allowed to target J2SE 1.5.
                if (driver.indexOf(".postgresql.")>=0 || driver.indexOf(".mysql.")>=0) {
                    classname = "org.geotools.referencing.factory.epsg.FactoryUsingAnsiSQL";
                } else if (driver.startsWith("oracle.")) {
                    classname = "org.geotools.referencing.factory.epsg.FactoryUsingOracleSQL";
                } else if (driver.indexOf(".hsqldb.") >= 0) {
                    classname = "org.geotools.referencing.factory.epsg.FactoryUsingHSQL";
                }
            }
        }
        final AbstractAuthorityFactory factory;
        try {
            final Class  classe = Class.forName(classname);
            final Constructor c = classe.getConstructor(new Class[] {Hints.class, Connection.class});
            factory = (AbstractAuthorityFactory) c.newInstance(new Object[] {hints, connection});
        } catch (Exception exception) {
            final SQLException e;
            e = new SQLException(Errors.format(ErrorKeys.CANT_CONNECT_DATABASE_$1, getURL()));
            e.initCause(exception);
            throw e;
        }
        final String schema = properties.getProperty("schema");
        if (schema != null) {
            if (factory instanceof FactoryUsingAnsiSQL) {
                ((FactoryUsingAnsiSQL) factory).setSchema(schema);
            }
        }
        return factory;
    }
}
