/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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

// Geotools dependencies
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
     * Preference node for the {@linkplain DataSource data source} class name.
     */
    private static final String DATASOURCE_NODE = "DataSource";

    /**
     * The JDBC {@linkplain DataSource data source} name in JNDI.
     */
    public static final String DATASOURCE_NAME = "jdbc/EPSG";

    /**
     * The shutdown hook, or <code>null</code> if none.
     */
    private Thread shutdown;

    /**
     * Constructs an authority factory using the default set of
     * {@linkplain org.opengis.referencing.ObjectFactory object factories}.
     */
    public DefaultFactory() {
        super(new FactoryGroup(), MAX_PRIORITY-1);
        setTimeout(30*60*1000L); // Closes the connection after at least 30 minutes of inactivity.
    }

    /**
     * Returns the authority, which is {@link org.geotools.metadata.citation.Citation#EPSG EPSG}.
     */
    public Citation getAuthority() {
        return org.geotools.metadata.citation.Citation.EPSG;
    }

    /**
     * Creates a new data source for etablishing a connection to the EPSG database. This method is
     * invoked automatically by {@link #createBackingStore} if no data source were found in the
     * naming directory for the <code>{@value #DATASOURCE_NAME}</code> name. The default
     * implementation creates a data source for the JDBC-ODBC bridge. This data source
     * implementation is available and documented in Sun's distribution since J2SE 1.4. See
     *
     * <A HREF="http://java.sun.com/j2se/1.5/docs/guide/jdbc/bridge.html">New data source
     * implementations in the JDBC-ODBC bridge</A>.
     *
     * <br><br>
     * Subclasses should override this method if they know a more appropriate default
     * data source for their EPSG database.
     *
     * @throws FactoryException if this method failed to create a default data source.
     * @return The data source for the EPSG database.
     *
     * @todo Should we allows to select an alternative implementation of
     *       {@code "sun.jdbc.odbc.ee.DataSource"} through preference API?
     */
    protected DataSource createDataSource() throws FactoryException {
        /*
         * Use reflection in order to avoid direct dependency to Sun's internal class.
         * It also allows to select the default data source class as a user preference.
         */
        String implementation = "sun.jdbc.odbc.ee.DataSource";
        try {
            final Preferences prefs = Preferences.userNodeForPackage(DefaultFactory.class);
            implementation = prefs.get(DATASOURCE_NODE, implementation);
        } catch (SecurityException exception) {
            // We are not allowed to get preferences (for example we are
            // running in an applet). Try with the default implementation.
            final LogRecord record = new LogRecord(Level.WARNING,
                    "Can't read preferences for \""+DATASOURCE_NODE+"\".");  // TODO: Localize
            record.setThrown(exception);
            record.setSourceClassName(DefaultFactory.class.getName());
            record.setSourceMethodName("createDataSource");
            LOGGER.log(record);
        }
        final DataSource source;
        try {
            final Class classe = Class.forName(implementation);
            source = (DataSource) classe.newInstance();
            classe.getMethod("setDatabaseName", new Class[] {String.class})
                  .invoke(source, new Object[] {"EPSG"});
        } catch (Exception exception) {
            /*
             * Catching all exceptions is not really recommended,
             * but there is a lot of them to in the above lines:
             *
             *     ClassNotFoundException, InstantiationException, IllegalAccessException,
             *     ClassCastException, NoSuchMethodException, SecurityException,
             *     IllegalArgumentException and InvocationTargetException ...
             */
            throw new FactoryException("Can't create EPSG data source.", exception);
        }
        return source;
    }

    /**
     * Creates the backing store authority factory. This method try to connect to the EPSG
     * database from the <code>{@value #DATASOURCE_NAME}</code> data source. If no data
     * source were found for that name, {@link #createDataSource} is invoked in order to
     * get a default one.
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
        DataSource source;
        try {
            final InitialContext context = new InitialContext();
            try {
                source = (DataSource) context.lookup(DATASOURCE_NAME);
            } catch (NameNotFoundException exception) {
                source = createDataSource();
                context.bind(DATASOURCE_NAME, source);
                LOGGER.info("Created a \"" + DATASOURCE_NAME +
                            "\" entry in the naming system."); // TODO: localize
            }
        } catch (NoInitialContextException exception) {
            source = createDataSource();
            // TODO: localize
            LOGGER.config("Using default EPSG data source without naming system.");
        } catch (NamingException exception) {
            // TODO: localize
            throw new FactoryException("Failed to get the data source for name \"" +
                                       DATASOURCE_NAME + "\".", exception);
        }
        final Connection connection;
        final DatabaseMetaData info;
        final String product;
        final String url;
        try {
            connection = source.getConnection();
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
     *   <strong>{@code -datasource}</strong> <var>classname</var><br>
     *       Sets the default {@linkplain DataSource data source} class. This class is used for
     *       {@linkplain #createDataSource creating a default data source} if no data source was
     *       found in the naming system for the <code>{@value #DATASOURCE_NAME}</code> name. The
     *       specified classname is stored in {@linkplain Preferences#userRoot user preferences}
     *       for future execution. The "{@code default}" string reset the default value, which is
     *       "{@code sun.jdbc.odbc.ee.DataSource}".
     *       <br><br>
     *
     *   <strong>{@code -encoding} <var>charset</var></strong><br>
     *       Sets the console encoding for this application output. This value has
     *       no impact on the data exchanged with the EPSG database.
     * </blockquote>
     *
     * @param args A list of EPSG code to display.
     *             An arbitrary number of codes can be specified on the command line.
     */
    public static void main(String[] args) {
        MonolineFormatter.initGeotools(); // Use custom logger.
        final Arguments arguments = new Arguments(args);
        final String   datasource = arguments.getOptionalString("-datasource");
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        if (datasource != null) {
            final Preferences prefs = Preferences.userNodeForPackage(DefaultFactory.class);
            if (datasource.equalsIgnoreCase("default")) {
                prefs.remove(DATASOURCE_NODE);
            } else {
                prefs.put(DATASOURCE_NODE, datasource);
            }
        }
        try {
            AuthorityFactory factory = null;
            try {
                for (int i=0; i<args.length; i++) {
                    if (factory == null) {
                        factory = FactoryFinder.getCRSAuthorityFactory("EPSG");
                    }
                    arguments.out.println(factory.createObject(args[i]));
                }
            } finally {
                if (factory instanceof AbstractAuthorityFactory) {
                    ((AbstractAuthorityFactory) factory).dispose();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace(arguments.err);
        }
        arguments.out.flush();
    }
}
