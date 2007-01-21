/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.imageio.spi.ServiceRegistry;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.JNDI;
import org.geotools.factory.Hints;
import org.geotools.factory.FactoryRegistry;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.referencing.factory.FactoryNotFoundException;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * Base class for EPSG factories to be registered in {@link FactoryFinder}. Various subclasses
 * are defined for different database backend (Access, PostgreSQL, HSQL, <cite>etc.</cite>).
 * The main purpose of this class is to {@linkplain #createDataSource create a data source}
 * connecting to the EPSG database, and an appropriate {@link FactoryUsingSQL} instance capable
 * to speak that database syntax.
 * <p>
 * Users should not creates instance of this class directly. They should invoke one of
 * <code>{@linkplain FactoryFinder}.getFooAuthorityFactory("EPSG")</code> methods instead.
 * <p>
 * Subclasses should override the following methods:
 * <ul>
 *   <li>{@link #createDataSource()}</li>
 *   <li>{@link #createBackingStore(Hints)}</li>
 * </ul>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultFactory extends DeferredAuthorityFactory
        implements CRSAuthorityFactory, CSAuthorityFactory, DatumAuthorityFactory,
                   CoordinateOperationAuthorityFactory
{
    /**
     * The JDBC {@linkplain DataSource data source} name in JNDI.
     *
     * @see #createDataSource
     */
    public static final String DATASOURCE_NAME = "jdbc/EPSG";

    /**
     * The factory registry for EPSG data sources. Will be created only when first needed.
     *
     * @deprecated To remove when other deprecated methods will have been removed.
     */
    private static FactoryRegistry datasources;

    /**
     * The default priority level for this factory.
     */
    static final int PRIORITY = MAXIMUM_PRIORITY - 10;

    /**
     * The factories to be given to the backing store.
     */
    private final FactoryGroup factories;

    /**
     * The context where to register {@link #datasource}, or {@code null} if it should
     * not be registered. This is used only as a way to pass "hiden" return value between
     * {@link #createDataSource()} and {@link #createBackingStore()}.
     */
    private transient InitialContext registerInto;

    /**
     * The data source, or {@code null} if the connection has not yet been etablished.
     */
    private DataSource datasource;

    /**
     * The shutdown hook, or {@code null} if none.
     */
    private Thread shutdown;

    /**
     * Constructs an authority factory using the default set of factories.
     */
    public DefaultFactory() {
        this(null);
    }

    /**
     * Constructs an authority factory with the default priority.
     */
    public DefaultFactory(final Hints hints) {
        this(hints, PRIORITY);
    }

    /**
     * Constructs an authority factory using a set of factories created from the specified hints.
     * This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS},
     * {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM}
     * {@code FACTORY} hints.
     *
     * @param hints An optional set of hints, or {@code null} if none.
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     *
     * @since 2.4
     */
    public DefaultFactory(final Hints hints, final int priority) {
        super(hints, priority);
        factories = FactoryGroup.createInstance(hints);
        setTimeout(30*60*1000L); // Close the connection after at least 30 minutes of inactivity.
    }

    /**
     * Returns the authority for this EPSG database.
     * This authority will contains the database version in the {@linkplain Citation#getEdition
     * edition} attribute, together with the {@linkplain Citation#getEditionDate edition date}.
     */
    public Citation getAuthority() {
        final Citation authority = super.getAuthority();
        return (authority!=null) ? authority : Citations.EPSG;
    }

    /**
     * Returns the data source for the EPSG database. If no data source has been previously
     * {@linkplain #setDataSource set}, then this method invokes {@link #createDataSource}.
     * <strong>Note:</strong> invoking this method may force immediate connection to the EPSG
     * database.
     *
     * @return The data source.
     * @throws SQLException if the connection to the EPSG database failed.
     *
     * @see #setDataSource
     * @see #createDataSource
     */
    public final synchronized DataSource getDataSource() throws SQLException {
        if (datasource == null) {
            // Force the creation of the underlying backing store. It will invokes
            // (indirectly) createBackingStore, which will fetch the DataSource.
            if (!super.isAvailable()) {
                // Connection failed, but the exception is not available.
                datasource = null;
                throw new SQLException(Errors.format(ErrorKeys.NO_DATA_SOURCE));
            }
        }
        return datasource;
    }

    /**
     * Set the data source for the EPSG database. If an other EPSG database was already in use,
     * it will be disconnected. Users should not invoke this method on the factory returned by
     * {@link FactoryFinder}, since it could have a system-wide effect.
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
     * {@link #createBackingStore()} only if no EPSG data source was found in the
     * JNDI (Java Naming and Directory). This method scans for plugins the first
     * time it is invoked.
     *
     * @return All registered data sources.
     *
     * @deprecated To be deleted after we removed the {@code org.geotools...DataSource} interface.
     */
    private static synchronized Iterator getDataSources() {
        final Class category = org.geotools.referencing.factory.epsg.DataSource.class;
        if (datasources == null) {
            datasources = new FactoryRegistry(Collections.singleton(category));
            datasources.scanForPlugins();
            datasources.setOrdering(category, new Comparator() {
                public int compare(final Object f1, final Object f2) {
                    return ((org.geotools.referencing.factory.epsg.DataSource) f1).getPriority() -
                           ((org.geotools.referencing.factory.epsg.DataSource) f2).getPriority();
                }
            });
        }
        return datasources.getServiceProviders(category, true);
    }

    /**
     * Setup a data source for a connection to the EPSG database. This method is invoked by
     * {@link #getDataSource()} when no data source has been {@linkplain #setDataSource
     * explicitly set}. The default implementation searchs for a {@link DataSource} instance
     * binded to the <code>{@value #DATASOURCE_NAME}</code> name using <cite>Java Naming and
     * Directory Interfaces</cite> (JNDI). If no data source were found, then this method
     * returns {@code null}.
     * <p>
     * Subclasses override this method in order to initialize a default data source when none were
     * found with JNDI. For example {@code plugin/epsg-access} defines a default data source using
     * the JDBC-ODBC bridge, which expects an "{@code EPSG}" database registered as an ODBC data
     * source (see the {@linkplain org.geotools.referencing.factory.epsg package javadoc} for
     * installation instructions). Example for a PostgreSQL data source:
     *
     * <blockquote><pre>
     * protected DataSource createDataSource() throws SQLException {
     *     DataSource candidate = super.createDataSource();
     *     if (candidate instanceof Jdbc3SimpleDataSource) {
     *         return candidate;
     *     }
     *     Jdbc3SimpleDataSource ds = new Jdbc3SimpleDataSource();
     *     ds.setServerName("localhost");
     *     ds.setDatabaseName("EPSG");
     *     ds.setUser("postgre");
     *     return ds;
     * }
     * </pre></blockquote>
     *
     * If a JNDI environment is running and no <code>{@value #DATASOURCE_NAME}</code> data source
     * was previously bound, then the {@code Jdbc3SimpleDataSource} created by the above code will
     * be automatically binded by {@link DefaultFactory}.
     *
     * @return The EPSG data source, or {@code null} if none where found.
     * @throws SQLException if an error occured while creating the data source.
     *
     * @since 2.4
     */
    protected DataSource createDataSource() throws SQLException {
        InitialContext context = null;
        DataSource     source  = null;
        try {
            context = JNDI.getInitialContext(new Hints(hints));
            source = (DataSource) context.lookup(DATASOURCE_NAME);
        } catch (NoInitialContextException exception) {
            // Fall back on 'return null' below.
        } catch (NameNotFoundException exception) {
            registerInto = context;
            // Fall back on 'return null' below.
        } catch (NamingException exception) {
            SQLException e = new SQLException(Errors.format(ErrorKeys.CANT_GET_DATASOURCE_$1,
                                              DATASOURCE_NAME));
            e.initCause(exception);
            throw e;
        }
        return source;
    }

    /**
     * Creates the backing store for the specified data source. This method usually
     * returns a new instance of {@link FactoryUsingSQL} or {@link FactoryUsingAnsiSQL}.
     * Subclasses may override this method in order to returns an instance tuned for the
     * SQL syntax of the underlying database. Example for a PostgreSQL data source:
     *
     * <blockquote><pre>
     * protected AbstractAuthorityFactory createBackingStore(Hints hints) throws SQLException {
     *     return new FactoryUsingAnsiSQL(hints, getDataSource().getConnection());
     * }
     * </pre></blockquote>
     *
     * @param  hints A map of hints, including the low-level factories to use for CRS creation.
     *         This argument should be given unchanged to {@code FactoryUsingSQL} constructor.
     * @return The {@linkplain FactoryUsingSQL EPSG factory} using SQL queries appropriate
     *         for this data source.
     * @throws SQLException if connection to the database failed.
     *
     * @since 2.4
     */
    protected AbstractAuthorityFactory createBackingStore(final Hints hints) throws SQLException {
        final DataSource source = getDataSource();
        // The next two lines will be removed after org.geotools...DataSource interface removal.
        if (source instanceof org.geotools.referencing.factory.epsg.DataSource) {
            return ((org.geotools.referencing.factory.epsg.DataSource) source).createFactory(hints);
        }
        final Connection connection = source.getConnection();
        final String quote = connection.getMetaData().getIdentifierQuoteString();
        if (quote.equals("\"")) {
            /*
             * PostgreSQL quotes the indentifiers with "..." while MS-Access quotes the
             * identifiers with [...], so we use the identifier quote string metadata as
             * a way to distinguish the two cases. However I'm not sure that it is a robust
             * criterion. Subclasses should always override as a safety.
             */
            return new FactoryUsingAnsiSQL(hints, connection);
        }
        return new FactoryUsingSQL(hints, connection);
    }

    /**
     * Gets the EPSG factory implementation connected to the database. This method is invoked
     * automatically by {@link #createBackingStore()}.
     *
     * @return The connection to the EPSG database.
     * @throws FactoryException if no data source were found.
     * @throws SQLException if this method failed to etablish a connection.
     *
     * @todo Inline this method into {@link #createBackingStore()} after we removed the
     *       deprecated code.
     */
    private AbstractAuthorityFactory createBackingStore0() throws FactoryException, SQLException {
        assert Thread.holdsLock(this);
        final Hints sourceHints = new Hints(hints);
        sourceHints.putAll(factories.getImplementationHints());
        if (datasource != null) {
            return createBackingStore(sourceHints);
        }
        /*
         * Try to gets the DataSource from JNDI. In case of success, it will be tried
         * for a connection before any DataSource declared in META-INF/services/.
         */
        DataSource source;
        final InitialContext context;
        try {
            source  = createDataSource();
            context = registerInto;
        } finally {
            registerInto = null;
        }
        /*
         * Iterate through all DataSources, begining with the one found in JNDI (if any).
         * We will retain the first successfull connection. If all DataSource fails, the
         * exception thrown by the first DataSource will be retrown, since it is usually
         * the main DataSource.
         */
        if (false) {
            /*
             * TODO: All the block after // --- Begin deprecated code ----
             *       should be replaced by this code after we deleted the
             *       deprecated org.geotools...DataSource interface:
             */
            if (source == null) {
                throw new FactoryNotFoundException(Errors.format(ErrorKeys.NO_DATA_SOURCE));
            }
            final AbstractAuthorityFactory factory;
            try {
                datasource = source;
                factory = createBackingStore(sourceHints);
            } finally {
                datasource = null;
            }
        }
        // ---- Begin deprecated code to delete (up to the next block comment) --------------------
        Iterator                 sources = null;
        AbstractAuthorityFactory factory = null;
        SQLException             failure = null;
        while (true) {
            if (source != null) try {
                try {
                    datasource = source;
                    factory = createBackingStore(sourceHints);
                } finally {
                    datasource = null;
                }
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
                if (failure != null) {
                    throw failure;
                }
                throw new FactoryNotFoundException(Errors.format(ErrorKeys.NO_DATA_SOURCE));
            }
            source = (DataSource) sources.next();
        };
        /*
         * We now have a working connection. If a naming directory is running but didn't contains
         * the "jdbc/EPSG" entry, add it now. In such case, a message is prepared and logged.
         */
        LogRecord record;
        if (context != null) {
            try {
                context.bind(DATASOURCE_NAME, source);
                record = Logging.format(Level.INFO, LoggingKeys.CREATED_DATASOURCE_ENTRY_$1,
                                        DATASOURCE_NAME);
            } catch (NamingException exception) {
                record = Logging.format(Level.WARNING, LoggingKeys.CANT_BIND_DATASOURCE_$1,
                                        DATASOURCE_NAME);
                record.setThrown(exception);
            }
            log(record);
        }
        this.datasource = source;  // Stores the data source only after success.
        return factory;
    }

    /**
     * Creates the backing store authority factory.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the constructor failed to connect to the EPSG database.
     *         This exception usually has a {@link SQLException} as its cause.
     */
    protected AbstractAuthorityFactory createBackingStore() throws FactoryException {
        final AbstractAuthorityFactory factory;
        String product = '<' + Vocabulary.format(VocabularyKeys.UNKNOW) + '>';
        String url     = product;
        try {
            factory = createBackingStore0();
            if (factory instanceof FactoryUsingSQL) {
                final DatabaseMetaData info = ((FactoryUsingSQL) factory).connection.getMetaData();
                product = info.getDatabaseProductName();
                url     = info.getURL();
            }
        } catch (SQLException exception) {
            throw new FactoryException(Errors.format(ErrorKeys.CANT_CONNECT_DATABASE_$1, "EPSG"),
                                       exception);
        }
        log(Logging.format(Level.CONFIG, LoggingKeys.CONNECTED_EPSG_DATABASE_$2, url, product));
        if (factory instanceof FactoryUsingSQL) {
            ((FactoryUsingSQL) factory).buffered = this;
        }
        return factory;
    }

    /**
     * For internal use by {@link #createFactory()} and {@link #createBackingStore()} only.
     */
    private static void log(final LogRecord record) {
        record.setSourceClassName(DefaultFactory.class.getName());
        record.setSourceMethodName("createBackingStore"); // The public caller.
        LOGGER.log(record);
    }

    /**
     * Returns {@code true} if the backing store can be disposed now. This method is invoked
     * automatically after the amount of time specified by {@link #setTimeout} if the factory
     * were not used during that time.
     *
     * @param backingStore The backing store in process of being disposed.
     */
    protected boolean canDisposeBackingStore(final AbstractAuthorityFactory backingStore) {
        if (backingStore instanceof FactoryUsingSQL) {
            return ((FactoryUsingSQL) backingStore).canDispose();
        }
        return super.canDisposeBackingStore(backingStore);
    }

    /**
     * Ensures that the database connection will be closed on JVM exit. This code will be executed
     * even if the JVM is terminated because of an exception or with [Ctrl-C]. Note: we create this
     * shutdown hook only if this factory is registered as a service because it will prevent this
     * instance to be garbage collected until it is deregistered.
     */
    private final class ShutdownHook extends Thread {
        public ShutdownHook() {
            super(FactoryUsingSQL.SHUTDOWN_THREAD);
        }

        public void run() {
            synchronized (DefaultFactory.this) {
                try {
                    dispose();
                } catch (FactoryException exception) {
                    // To late for logging, since the JVM is
                    // in process of shutting down. Ignore...
                }
            }
        }
    }

    /**
     * Called when this factory is added to the given {@code category} of the given
     * {@code registry}. The object may already be registered under another category.
     */
    public synchronized void onRegistration(final ServiceRegistry registry, final Class category) {
        super.onRegistration(registry, category);
        if (shutdown == null) {
            shutdown = new ShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdown);
        }
    }

    /**
     * Called when this factory is removed from the given {@code category} of the given
     * {@code registry}.  The object may still be registered under another category.
     */
    public synchronized void onDeregistration(final ServiceRegistry registry, final Class category) {
        if (shutdown != null) {
            if (registry.getServiceProviderByClass(getClass()) == null) {
                // Remove the shutdown hook only if this instance is not
                // anymore registered as a service under any category.
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
     *
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
    public static void main(final String[] args) {
        Console.main(args);
    }
}
