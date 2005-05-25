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
import java.util.Comparator;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.imageio.spi.ServiceRegistry;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import java.io.PrintWriter;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.OperationNotFoundException;

// Geotools dependencies
import org.geotools.factory.JNDI;
import org.geotools.factory.Hints;
import org.geotools.factory.FactoryRegistry;
import org.geotools.metadata.iso.citation.CitationImpl;
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
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see DataSource
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
     * The hints for this factory. Null for now, but may be different in a future version.
     */
    private static final Hints HINTS = null;

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
     * Returns the authority for this EPSG database.
     * This authority will contains the database version in the {@linkplain Citation#getEdition
     * edition} attribute, together with the {@linkplain Citation#getEditionDate edition date}.
     */
    public Citation getAuthority() {
        final Citation authority = super.getAuthority();
        return (authority!=null) ? authority : CitationImpl.EPSG;
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
            if (!super.isReady()) {
                // Connection failed, but the exception is not available.
                throw new SQLException("No data source found."); // TODO: localize;
            }
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
                    return ((DataSource) f2).getPriority() -
                           ((DataSource) f1).getPriority();
                }
            });
        }
        return datasources.getServiceProviders(category, true);
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
     * @throws SQLException if this method failed to etablish a connection.
     */
    private AbstractAuthorityFactory createFactory() throws SQLException {
        assert Thread.holdsLock(this);
        if (datasource != null) {
            return datasource.createFactory(factories);
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
        Iterator                 sources = null;
        AbstractAuthorityFactory factory = null;
        SQLException             failure = null;
        while (true) {
            if (source != null) try {
                factory = source.createFactory(factories);
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
         * We now have a working connection. If a naming directory is running but didn't contains
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
            record.setSourceMethodName("createBackingStore"); // The public caller.
            LOGGER.log(record);
        }
        this.datasource = source;
        return factory;
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
        String product = "<unknow>"; // TODO: localize
        String url     = "<unknow>";
        try {
            factory = createFactory();
            if (factory instanceof EPSGFactory) {
                final DatabaseMetaData info = ((EPSGFactory) factory).connection.getMetaData();
                product = info.getDatabaseProductName();
                url     = info.getURL();
            }
        } catch (SQLException exception) {
            // TODO: localize
            throw new FactoryException("Failed to connect to the EPSG database", exception);
        }
        // TODO: Provide a localized message including the database version.
        LOGGER.config("Connected to EPSG database \"" + url + "\" on " + product + '.');
        if (factory instanceof EPSGFactory) {
            ((EPSGFactory) factory).buffered = this;
        }
        return factory;
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
        MonolineFormatter.initGeotools(); // Use custom logger.
        final Arguments arguments = new Arguments(args);
        final boolean     printMT = arguments.getFlag("-transform");
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        final PrintWriter out = arguments.out;
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
                        factory = FactoryFinder.getCRSAuthorityFactory("EPSG", HINTS);
                        if (factory instanceof AbstractAuthorityFactory) {
                            out.println(((AbstractAuthorityFactory) factory).getBackingStoreDescription());
                        }
                    }
                    final Object object = factory.createObject(args[i]);
                    out.println(object);
                    out.println();
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
            final CoordinateOperationFactory factory =
                    FactoryFinder.getCoordinateOperationFactory(HINTS);
            for (int i=0; i<count; i++) {
                for (int j=i+1; j<count; j++) {
                    try {
                        out.println(factory.createOperation(crs[i], crs[j]).getMathTransform());
                    } catch (OperationNotFoundException exception) {
                        out.println(exception.getLocalizedMessage());
                    } catch (FactoryException exception) {
                        exception.printStackTrace(arguments.err);
                    }
                    out.println();
                }
            }
        }
        out.flush();
    }
}
