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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
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
 * The default EPSG factory to be registered in {@link FactoryFinder}. This factory works as a
 * proxy for 1) select an appropriate {@link FactoryUsingSQL} subclass at runtime and 2) cache CRS
 * objects for better performances. The database connection is specified through a
 * {@link DataSource} binded to the <code>{@value #DATASOURCE_NAME}</code> name in <cite>Java
 * Naming and Directory Interfaces</cite> (JNDI). If no binding is found under that name, data
 * sources are searched in the following directory:
 *
 * <BLOCKQUOTE><PRE>META-INF/services/org.geotools.referencing.factory.epsg.DataSource</PRE></BLOCKQUOTE>
 *
 * The {@code plugin/epsg-access} contains a default binding using the JDBC-ODBC bridge. This
 * default binding expects an "{@code EPSG}" database registered as an ODBC data source. See
 * the {@linkplain org.geotools.referencing.factory.epsg package javadoc} for installation
 * instructions.
 * <p>
 * Users should not creates instance of this class directly. They should invoke one of
 * <code>{@linkplain FactoryFinder}.getFooAuthorityFactory("EPSG")</code> methods instead.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see DataSource
 */
public class DefaultFactory extends DeferredAuthorityFactory
        implements CRSAuthorityFactory, CSAuthorityFactory, DatumAuthorityFactory,
                   CoordinateOperationAuthorityFactory
{
    /**
     * The JDBC {@linkplain DataSource data source} name in JNDI.
     */
    public static final String DATASOURCE_NAME = "jdbc/EPSG";

    /**
     * The factory registry for EPSG data sources. Will be created only when first needed.
     */
    private static FactoryRegistry datasources;

    /**
     * The priority level for this factory.
     */
    static final int PRIORITY = MAXIMUM_PRIORITY - 10;

    /**
     * The hints for this factory. Null for now, but may be different in a future version.
     */
    private static final Hints HINTS = null;

    /**
     * The factories to be given to the backing store.
     */
    private final FactoryGroup factories;

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
     * Constructs an authority factory using a set of factories created from the specified hints.
     * This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS},
     * {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM}
     * {@code FACTORY} hints.
     */
    public DefaultFactory(final Hints hints) {
        super(hints, PRIORITY);
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
     * Returns the data source for the EPSG database.
     * <strong>Note:</strong> invoking this method may force immediate connection to the EPSG
     * database.
     *
     * @return The data source.
     * @throws SQLException if the connection to the EPSG database failed.
     */
    public final synchronized DataSource getDataSource() throws SQLException {
        if (datasource == null) {
            // Force the creation of the underlying backing store. It will invokes
            // (indirectly) createBackingStore, which will fetch the DataSource.
            if (!super.isAvailable()) {
                // Connection failed, but the exception is not available.
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
     * {@link #createFactory} only if no EPSG data source was found in the JNDI
     * (Java Naming and Directory). This method scans for plugins the first time it
     * is invoked, and add a default JDBC-ODBC bridge in addition.
     *
     * @return All registered data sources.
     */
    private static synchronized Iterator getDataSources() {
        final Class category = DataSource.class;
        if (datasources == null) {
            datasources = new FactoryRegistry(Collections.singleton(category));
            datasources.scanForPlugins();
            datasources.setOrdering(category, new Comparator() {
                public int compare(final Object f1, final Object f2) {
                    return ((DataSource) f1).getPriority() -
                           ((DataSource) f2).getPriority();
                }
            });
        }
        return datasources.getServiceProviders(category, true);
    }

    /**
     * Invokes {@link DataSource#createFactory} with all hints reserved for it.
     *
     * <strong>Note:</strong>   {@link #hints} is usually empty (the super class do not retains
     * any hint except the authority factories after the backing store is created), but we take
     * it just for the principle. The potentially relevant hints are the one specified in
     * {@link #factories}.
     */
    private AbstractAuthorityFactory createFactory(final DataSource source) throws SQLException {
        final Hints sourceHints = new Hints(hints);
        sourceHints.putAll(factories.getImplementationHints());
        return source.createFactory(sourceHints);
    }

    /**
     * Gets the EPSG factory implementation connected to the database. This method is invoked
     * automatically by {@link #createBackingStore}. The default implementation search for a
     * {@link DataSource} registered in JNDI (Java Naming Directory) for the
     * <code>{@value #DATASOURCE_NAME}</code> name. If no such data source is found, this method
     * tries to create a data source for the JDBC-ODBC bridge, together with any data source
     * declared in the following file:
     *
     * <blockquote><pre>META-INF/services/org.geotools.referencing.factory.epsg.DataSource</pre></blockquote>
     *
     * @return The connection to the EPSG database.
     * @throws FactoryException if no data source were found.
     * @throws SQLException if this method failed to etablish a connection.
     */
    private AbstractAuthorityFactory createFactory() throws FactoryException, SQLException {
        assert Thread.holdsLock(this);
        if (datasource != null) {
            return createFactory(datasource);
        }
        /*
         * Try to gets the DataSource from JNDI. In case of success, it will be tried
         * for a connection before any DataSource declared in META-INF/services/.
         */
        InitialContext context  = null;
        DataSource     source   = null;
        boolean        register = false;
        try {
            context = JNDI.getInitialContext(HINTS);
            source = (DataSource) context.lookup(DATASOURCE_NAME);
        } catch (NoInitialContextException exception) {
            // Fall back on 'getDataSources()' below.
        } catch (NameNotFoundException exception) {
            register = (context != null);
            // Fall back on 'getDataSources()' below.
        } catch (NamingException exception) {
            SQLException e = new SQLException(Errors.format(ErrorKeys.CANT_GET_DATASOURCE_$1,
                                              DATASOURCE_NAME));
            e.initCause(exception);
            throw e;
        }
        /*
         * Iterate through all DataSources, begining with the one found in JNDI (if any).
         * We will retain the first successfull connection. If all DataSource fails, the
         * exception thrown by the first DataSource will be retrown, since it is usually
         * the main DataSource.
         */
        Iterator                 sources = null;
        AbstractAuthorityFactory factory = null;
        SQLException             failure = null;
        while (true) {
            if (source != null) try {
                factory = createFactory(source);
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
        if (register) {
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
        this.datasource = source;
        return factory;
    }

    /**
     * For internal use by {@link #createFactory} and {@link #createBackingStore} only.
     */
    private static void log(final LogRecord record) {
        record.setSourceMethodName(DefaultFactory.class.getName());
        record.setSourceMethodName("createBackingStore"); // The public caller.
        LOGGER.log(record);
    }

    /**
     * Creates the backing store authority factory. This method try to connect to the EPSG
     * database from the <code>{@value #DATASOURCE_NAME}</code> data source. If no data
     * source were found for that name, implementation declared in {@code META-INF/services/}
     * are tested.
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
            factory = createFactory();
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
