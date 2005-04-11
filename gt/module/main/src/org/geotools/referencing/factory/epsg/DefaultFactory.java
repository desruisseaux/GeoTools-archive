/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.prefs.Preferences;
import javax.imageio.spi.ServiceRegistry;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.sql.DataSource;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.OperationNotFoundException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.FactoryRegistry;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.Arguments;
import org.geotools.util.MonolineFormatter;


/**
 * The default EPSG factory to be registered in {@link FactoryFinder}. This factory works as a
 * proxy for 1) select an appropriate {@link EPSGFactory} subclass at runtime and 2) cache CRS
 * objects for better performances. The database connection is specified through a
 * {@link DataSource} binded to the <code>{@value #DATASOURCE_NAME}</code> name.
 * If no binding is found under that name, a default binding using the JDBC-ODBC bridge
 * is created. This default binding expects a "{@code EPSG}" database registered as an
 * ODBC data source. See the package javadoc for installation instructions.
 *
 * <P>The {@code EPSGFactory} subclass is selected on the basis of the
 * {@linkplain DatabaseMetaData#getDatabaseProductName database product name}:</P>
 *
 * <ul>
 *   <li>(...todo...)</li>
 *   <li>Otherwise, the default {@link EPSGFactory} implementation is used.
 *       This implementation is designed for the EPSG database in MS-Access
 *       format, which is the primary format distributed by EPSG.</li>
 * </ul>
 *
 * <P>Users should not creates instance of this class directly. They should invokes one of
 * <code>{@linkplain FactoryFinder}.getFooAuthorityFactory("EPSG")</code> methods instead.</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultFactory extends DeferredAuthorityFactory {
    /**
     * The JDBC {@linkplain DataSource data source} name in JNDI.
     */
    public static final String DATASOURCE_NAME = "jdbc/EPSG";

    /**
     * The factory registry for EPSG data sources. Will be created only when first needed.
     */
    private static FactoryRegistry datasources;

    /**
     * The data source, or {@code null} if the connection has not yet been etablished.
     */
    private DataSource datasource;

    /**
     * The shutdown hook, or <code>null</code> if none.
     */
    private Thread shutdown;

    /**
     * Constructs an authority factory using the default set of
     * {@linkplain org.opengis.referencing.ObjectFactory object factories}.
     */
    public DefaultFactory() {
        super(new FactoryGroup(), MAXIMUM_PRIORITY-10);
        setTimeout(30*60*1000L); // Closes the connection after at least 30 minutes of inactivity.
    }

    /**
     * Returns the authority, which is {@link org.geotools.metadata.citation.Citation#EPSG EPSG}.
     */
    public Citation getAuthority() {
        return org.geotools.metadata.citation.Citation.EPSG;
    }

    /**
     * Returns the data source for the EPSG database.
     * <strong>Note:</strong> invoking this method may force immediate connection to the EPSG
     * database.
     *
     * @return The data source.
     * @throws SQLException if the connection to the EPSG database failed.
     */
    public final synchronized DataSource getDataSource() throws SQLException {
        if (datasource == null) {
            // Gets the connection and close it. This is inneficient (unless the data
            // source pools connections), but will force the creation of DataSource.
            getConnection().close();
        }
        return datasource;
    }

    /**
     * Set the data source for the EPSG database. If an other EPSG database was already in use,
     * it will be disconnected. Users should not invoke this method on the factory returned by
     * {@link org.geotools.referencing.FactoryFinder}, since it would have a system-wide effect.
     *
     * @param  datasource The new datasource.
     * @throws SQLException if an error occured.
     */
    public synchronized void setDataSource(final DataSource datasource) throws SQLException {
        if (datasource != this.datasource) {
            try {
                dispose();
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof SQLException) {
                    throw (SQLException) cause;
                }
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                // Not really an SQL exception, but we should not reach this point anyway.
                final SQLException e = new SQLException(exception.getLocalizedMessage());
                e.initCause(exception);
                throw e;
            }
            this.datasource = datasource;
        }
    }

    /**
     * Returns a data source from the factory registry. This method is invoked by
     * {@link #createDataSource} only if no EPSG data source was found in the JNDI
     * (Java Naming and Directory). This method scans for plugins the first time it
     * is invoked, and add a default JDBC-ODBC bridge in addition.
     *
     * @return All registered data sources.
     */
    private static synchronized Iterator getDataSources() {
        final Class toLoad   = org.geotools.referencing.factory.epsg.DataSource.class;
        final Class category = DataSource.class;
        if (datasources == null) {
            datasources = new FactoryRegistry(Arrays.asList(new Class[] {category, toLoad}));
            for (final Iterator it=datasources.getServiceProviders(toLoad); it.hasNext();) {
                datasources.registerServiceProvider(it.next(), category);
            }
            /*
             * After scaning any user-specified data source, add the Sun's JDBC-ODBC one as the
             * default fallback. We use reflection in order to avoid direct dependency to Sun's
             * internal class. This JDBC-ODBC connection will be set as the preferred one since
             * 1) the "official" EPSG database is in Access format and  2) the HSQL database is
             * just a fallback to use if no "real" database was found.
             */
            try {
                final Class classe = Class.forName("sun.jdbc.odbc.ee.DataSource");
                final DataSource source = (DataSource) classe.newInstance();
                classe.getMethod("setDatabaseName", new Class[] {String.class})
                      .invoke(source, new Object[] {"EPSG"});
                datasources.registerServiceProvider(source, category);
                for (Iterator it=datasources.getServiceProviders(toLoad, false); it.hasNext();) {
                    final Object other = it.next();
                    if (other instanceof org.geotools.referencing.factory.epsg.DataSource &&
                        ((org.geotools.referencing.factory.epsg.DataSource) other).isPreferred())
                    {
                        datasources.setOrdering(category, other, source);
                    } else {
                        datasources.setOrdering(category, source, other);
                    }
                }
            } catch (Exception exception) {
                /*
                 * Catching all exceptions is not really recommended,
                 * but there is a lot of them in the previous lines:
                 *
                 *     ClassNotFoundException, InstantiationException, IllegalAccessException,
                 *     ClassCastException, NoSuchMethodException, SecurityException,
                 *     IllegalArgumentException and InvocationTargetException ...
                 */
                // TODO: localize
                final LogRecord record = new LogRecord(Level.CONFIG, "Can't instantiate JDBC-ODBC bridge.");
                record.setThrown(exception);
                record.setSourceClassName(DefaultFactory.class.getName());
                record.setSourceMethodName("createDataSource");
                LOGGER.log(record);
            }
        }
        return datasources.getServiceProviders(category, true);
    }

    /**
     * Gets the connection to the EPSG database. This method is invoked automatically by
     * {@link #createBackingStore}. The default implementation search for a {@link DataSource}
     * registered in JNDI (Java Naming Directory) for the <code>{@value #DATASOURCE_NAME}</code>
     * name. If no such data source is found, this method tries to create a data source for the
     * JDBC-ODBC bridge (available and documented in Sun's J2SE distribution since 1.4, see
     * <A HREF="http://java.sun.com/j2se/1.5/docs/guide/jdbc/bridge.html">New data source
     * implementations in the JDBC-ODBC bridge</A>), and any supplemental data source declared in
     * the following file:
     *
     * <blockquote><pre>
     * META-INF/services/org.geotools.referencing.factory.epsg.DataSource
     * </pre></blockquote>
     *
     * @return The connection to the EPSG database.
     * @throws SQLException if this method failed to etablish a connection.
     */
    private Connection getConnection() throws SQLException {
        assert Thread.holdsLock(this);
        /*
         * Try to gets the DataSource from JNDI. In case of success, it will be tried
         * for a connection before any DataSource declared in META-INF/services/.
         */
        InitialContext context  = null;
        DataSource     source   = null;
        boolean        register = false;
        try {
            context = new InitialContext();
            source = (DataSource) context.lookup(DATASOURCE_NAME);
        } catch (NoInitialContextException exception) {
            // Fall back on 'getDataSources()' below.
        } catch (NameNotFoundException exception) {
            register = (context != null);
            // Fall back on 'getDataSources()' below.
        } catch (NamingException exception) {
            SQLException e = new SQLException("Failed to get the data source for name \"" +
                                              DATASOURCE_NAME + "\"."); // TODO: localize
            e.initCause(exception);
            throw e;
        }
        /*
         * Iterate through all DataSources, begining with the one found in JNDI (if any).
         * We will retain the first successfull connection. If all DataSource fails, the
         * exception thrown by the first DataSource will be retrown, since it is usually
         * the main DataSource.
         */
        Iterator      sources = null;
        Connection connection = null;
        SQLException  failure = null;
        while (true) {
            if (source != null) try {
                connection = source.getConnection();
                break; // Found a successfull connection: stop the loop.
            } catch (SQLException exception) {
                // Keep only the exception from the first data source.
                if (failure == null) {
                    failure = exception;
                }
            }
            // Setup the iterator (if not already done) and test next DataSources.
            if (sources == null) {
                sources = getDataSources();
            }
            if (!sources.hasNext()) {
                if (failure == null) {
                    failure = new SQLException("No data source found."); // TODO: localize;
                }
                throw failure;
            }
            source = (DataSource) sources.next();
        };
        /*
         * We now have a working data source. If a naming directory is running but didn't contains
         * the "jdbc/EPSG" entry, add it now. In such case, a message is prepared and logged.
         */
        LogRecord record;
        if (register) {
            try {
                context.bind(DATASOURCE_NAME, source);
                record = new LogRecord(Level.INFO, "Created a \"" + DATASOURCE_NAME +
                                       "\" entry in the naming system."); // TODO: localize
            } catch (NamingException exception) {
                record = new LogRecord(Level.WARNING, "Failed to bind \"" + DATASOURCE_NAME +
                                       "\" entry"); // TODO: localize
                record.setThrown(exception);
            }
            record.setSourceMethodName(DefaultFactory.class.getName());
            record.setSourceMethodName("getConnection");
            LOGGER.log(record);
        }
        this.datasource = source;
        return connection;
    }

    /**
     * Creates the backing store authority factory. This method try to connect to the EPSG
     * database from the <code>{@value #DATASOURCE_NAME}</code> data source. If no data
     * source were found for that name, implementation declared in {@code META-INF/services/}
     * are tested. If none of them fit, the default JDBC-ODBC bridge is used.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the constructor failed to connect to the EPSG database.
     *         This exception usually has a {@link SQLException} as its cause.
     *
     * @todo Do may need some standard way (in Geotools) for fetching an {@link InitialContext}
     *       for the whole Geotools library?
     *
     * @todo Needs a better was to know which {@link EPSGFactory} implementation to use.
     *       Instead of relying on the product name (which is hardly generic), we should
     *       try a SQL statement like "SELECT id FROM epsg_sometable WHERE ID=0" (we don't
     *       mind if the result set contains no record) and use {@link FactoryForSQL} if
     *       the above didn't threw a SQLException.
     */
    protected AbstractAuthorityFactory createBackingStore() throws FactoryException {
        final Connection connection;
        final DatabaseMetaData info;
        final String product;
        final String url;
        try {
            connection = getConnection();
            info       = connection.getMetaData();
            product    = info.getDatabaseProductName();
            url        = info.getURL();
        } catch (SQLException exception) {
            // TODO: localize
            throw new FactoryException("Failed to connect to the EPSG database", exception);
        } catch (RuntimeException exception) {
            /*
             * May happen in some unpolished JDBC drivers. For example the JDBC-ODBC bridge on
             * Linux throws a NullPointerException when trying to log a warning to the tracer.
             * Log a message, since many application will catch the FactoryException without
             * looking further in its cause. Use a relatively low level for the warning, since
             * this error may not be of interest in some configurations.
             */
            // TODO: localize
            final LogRecord record = new LogRecord(Level.FINE,
                    "Unexpected exception in JDBC data source.");
            record.setSourceClassName(DefaultFactory.class.getName());
            record.setSourceMethodName("createBackingStore");
            record.setThrown(exception);
            LOGGER.log(record);
            throw new FactoryException("Failed to connect to the EPSG database", exception);
        }
        // TODO: Provide a localized message including the database version.
        LOGGER.config("Connected to EPSG database \"" + url + "\".");
        final EPSGFactory epsg;
        /*
         * TODO: Hard-coded product names for now. We will need to implement a
         *       better way later (see the @todo comment in the method javadoc).
         */
        if (product.equalsIgnoreCase("PostgreSQL") ||
            product.equalsIgnoreCase("MySQL"))
        {
            epsg = new FactoryForSQL(factories, connection);
        } else {
            epsg = new EPSGFactory(factories, connection);
        }
        epsg.buffered = this;
        return epsg;
    }

    /**
     * Called when this factory is added to the given <code>category</code> of the given
     * <code>registry</code>  The object may already be registered under another category.
     */
    public synchronized void onRegistration(final ServiceRegistry registry, final Class category) {
        super.onRegistration(registry, category);
        /*
         * Ensures that the database connection will be closed on JVM exit.
         * This code will be executed even if the JVM is terminated because
         * of an exception or with [Ctrl-C]. Note: we create this shutdown
         * hook only if this factory is registered as a service because it
         * will prevent this instance to be garbage collected until it is
         * deregistered.
         */
        if (shutdown == null) {
            shutdown = new Thread("EPSG factory shutdown") {
                public void run() {
                    try {
                        dispose();
                    } catch (FactoryException exception) {
                        // To late for logging, since the JVM is
                        // in process of shutting down. Ignore...
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdown);
        }
    }

    /**
     * Called when this factory is removed from the given <code>category</code> of the given
     * <code>registry</code>.  The object may still be registered under another category.
     */
    public synchronized void onDeregistration(final ServiceRegistry registry, final Class category) {
        if (shutdown != null) {
            if (registry.getServiceProviderByClass(getClass()) == null) {
                Runtime.getRuntime().removeShutdownHook(shutdown);
                shutdown = null;
            }
        }
        super.onDeregistration(registry, category);
    }

    /**
     * Constructs an object from the EPSG database and print its WKT (Well Know Text) to
     * the standard output. This method can be invoked from the command line. For example:
     *
     * <blockquote><pre>
     * java org.geotools.referencing.factory.epsg.DefaultFactory 4181
     * </pre></blockquote>
     *
     * Should print:
     *
     * <blockquote><pre>
     * GEOGCS["Luxembourg 1930", DATUM["Luxembourg 1930", <FONT face="Arial">etc...</FONT>
     * </pre></blockquote>
     *
     * The following optional arguments are supported:
     * <blockquote>
     *   <strong>{@code -encoding} <var>charset</var></strong><br>
     *       Sets the console encoding for this application output. This value has
     *       no impact on the data exchanged with the EPSG database.
     *
     *   <strong>{@code -transform}</strong><br>
     *       Output the math transforms between every pairs of CRS.
     * </blockquote>
     *
     * @param args A list of EPSG code to display.
     *             An arbitrary number of codes can be specified on the command line.
     */
    public static void main(String[] args) {
        final Hints hints = null;
        MonolineFormatter.initGeotools(); // Use custom logger.
        final Arguments arguments = new Arguments(args);
        final boolean     printMT = arguments.getFlag("-transform");
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        /*
         * Constructs and prints each object. In the process, keep all coordinate reference systems.
         * They will be used later for printing math transforms. This is usefull in order to check
         * if the EPSG database provides enough information that Geotools know about for creating
         * the coordinate operation.
         */
        int count = 0;
        final CoordinateReferenceSystem[] crs = new CoordinateReferenceSystem[args.length];
        try {
            AuthorityFactory factory = null;
            try {
                for (int i=0; i<args.length; i++) {
                    if (factory == null) {
                        factory = FactoryFinder.getCRSAuthorityFactory("EPSG", hints);
                    }
                    final Object object = factory.createObject(args[i]);
                    arguments.out.println(object);
                    arguments.out.println();
                    if (object instanceof CoordinateReferenceSystem) {
                        crs[count++] = (CoordinateReferenceSystem) object;
                    }
                }
            } finally {
                if (factory instanceof AbstractAuthorityFactory) {
                    ((AbstractAuthorityFactory) factory).dispose();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace(arguments.err);
        }
        /*
         * If the user asked for math transforms, prints them now.
         */
        if (printMT) {
            final CoordinateOperationFactory factory = FactoryFinder.getCoordinateOperationFactory(hints);
            for (int i=0; i<count; i++) {
                for (int j=i+1; j<count; j++) {
                    try {
                        arguments.out.println(factory.createOperation(crs[i], crs[j]).getMathTransform());
                    } catch (OperationNotFoundException exception) {
                        arguments.out.println(exception.getLocalizedMessage());
                    } catch (FactoryException exception) {
                        exception.printStackTrace(arguments.err);
                    }
                    arguments.out.println();
                }
            }
        }
        arguments.out.flush();
    }
}
