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
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.io.IOException;
import java.util.prefs.Preferences;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.imageio.spi.ServiceRegistry;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;

// Geotools dependencies
import org.geotools.io.TableWriter;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.BufferedAuthorityFactory;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.Arguments;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.util.MonolineFormatter;


/**
 * The default EPSG factory to be registered in {@link FactoryFinder}. This factory work as a proxy
 * for 1) selecting an appropriate {@link EPSGFactory} subclass at runtime and 2) cache objects for
 * better performances. The database URL (as well as the JDBC driver to use) are stored in the
 * system preferences. The default values are:
 *
 * <table>
 *   <tr><td>JDBC driver:&nbsp;</td><td><code>{@value #DEFAULT_DRIVER}</code></td></tr>
 *   <tr><td>URL:&nbsp;</td><td><code>{@value #DEFAULT_CONNECTION}</code></td></tr>
 * </table>
 *
 * <P>Those default connection parameters can be changed by invoking the {@link #main} method
 * from the command line. For example:</P>
 *
 * <blockquote><pre>
 * java org.geotools.referencing.espg.DefaultFactory -driver=[my driver] -connection=[my url]
 * </pre></blockquote>
 *
 * <P>The {@code EPSGFactory} subclass is selected on the basis of the URL:</P>
 *
 * <ul>
 *   <li>(...todo...)</li>
 *   <li>Otherwise, the default {@link EPSGFactory} implementation is used.
 *       This implementation is designed for the EPSG database in MS-Access
 *       format, which is the primary format distributed by EPSG.</li>
 * </ul>
 *
 * Users should not creates instance of this class themself. It is public mainly for the purpose of
 * registering in {@link FactoryFinder}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class DefaultFactory extends BufferedAuthorityFactory {
    /** Preference node for the JDBC driver class name. */
    private static final String DRIVER = "JDBC driver";

    /** Default value for the JDBC driver class name. */
    public static final String DEFAULT_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";

    /** Preference node for the EPSG database connection string. */
    private static final String CONNECTION = "EPSG connection";

    /** Preference node for the EPSG database connection string. */
    public static final String DEFAULT_CONNECTION = "jdbc:odbc:EPSG";

    /**
     * The shutdown hook, or <code>null</code> if none.
     */
    private Thread shutdown;

    /**
     * <code>true</code> if system preferences was used (or is to be used) instead of user
     * preferences.
     */
    private boolean system;

    /**
     * Constructs an authority factory using the default set of
     * {@linkplain org.opengis.referencing.ObjectFactory object factories} and the
     * default connection parameters to the EPSG database.
     */
    public DefaultFactory() {
        super(new FactoryGroup(), MAX_PRIORITY);
    }

    /**
     * Returns the authority, which is {@link org.geotools.metadata.citation.Citation#EPSG EPSG}.
     */
    public Citation getAuthority() {
        return org.geotools.metadata.citation.Citation.EPSG;
    }

    /**
     * Returns the URL to use for the connection to the EPSG database.
     * Subclasses may override this method in order to specify a different URL.
     */
    protected String getURL() {
        return getPreference(CONNECTION, DEFAULT_CONNECTION, system);
    }

    /**
     * Returns the driver to load before any attempt to connect to the EPSG database,
     * or <code>null</code> if none.
     * Subclasses may override this method in order to specify a different driver.
     */
    protected String getDriver() {
        return getPreference(DRIVER, DEFAULT_DRIVER, system);
    }

    /**
     * Returns the value for the specified preference node. This method try on user preferences
     * first, and on the system preferences next if no user preferences was set. If both of them
     * fails, the default value is returned.
     */
    private static String getPreference(final String node, final String defaultValue, boolean system) {
        do {
            final Preferences prefs;
            try {
                prefs = getPreferences(system);
            } catch (SecurityException exception) {
                // We are not allowed to read those preferences.
                // Try the other preference node or the default.
                continue;
            }
            final String value = prefs.get(node, null);
            if (value != null) {
                return value;
            }
        } while ((system = !system) == true);
        return defaultValue;
    }

    /**
     * Returns the user or system preferences.
     *
     * @param  system <code>true</code> for returning {@linkplain Preferences#systemRoot system
     *         preferences} instead of {@linkplain Preferences#userRoot user preferences}.
     * @return The preferences.
     */
    private static Preferences getPreferences(final boolean system) {
        return system ? Preferences.systemNodeForPackage(DefaultFactory.class)
                      : Preferences.  userNodeForPackage(DefaultFactory.class);
    }

    /**
     * Returns the backing store authority factory. This method try to connect to the EPSG
     * database using the default connection parameters the first time it is invoked.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the constructor failed to connect to the EPSG database.
     *         This exception usually has a {@link SQLException} as its cause.
     */
    protected AbstractAuthorityFactory getBackingStore() throws FactoryException {
        if (backingStore == null) try {
            /*
             * TODO: Infer the EPSGFactory subclass from the URL here.
             */
            final EPSGFactory epsg = new EPSGFactory(factories, getURL(), getDriver());
            epsg.buffered = this;
            backingStore = epsg;
        } catch (SQLException exception) {
            // TODO: localize
            throw new FactoryException("Failed to connect to the EPSG database", exception);
        }
        return backingStore;
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
     * java org.geotools.referencing.epsg.DefaultFactory 4181
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
     *   <strong>{@code -system}</strong><br>
     *       Instructs that {@code -connection} and {@code -driver} arguments below should
     *       set the {@linkplain Preferences#systemRoot system preferences} instead of the
     *       {@linkplain Preferences#userRoot user preferences}. Use this flag if you are
     *       a system administrator and want every users on a machine connect to the specified
     *       EPSG database.
     *       <br><br>
     *
     *   <strong>{@code -connection}</strong> <var>URL</var><br>
     *       Set the EPSG database URL. The URL must conform to
     *       {@link DriverManager#getConnection(String)} specification. The default value
     *       is <code>{@value #DEFAULT_CONNECTION}</code>. The specified URL is stored in
     *       system preferences and will become the default URL every time an EPSG
     *       {@code DefaultFactory} is created without explicit URL. The
     *       "{@code default}" string reset the default URL.
     *       <br><br>
     *
     *   <strong>{@code -driver}</strong> <var>classname</var><br>
     *       Set the driver class. The default value is <code>{@value #DEFAULT_DRIVER}</code>.
     *       The specified classname is stored in system preferences and will become the default
     *       driver every time an EPSG {@code DefaultFactory} is created without explicit
     *       driver. The "{@code default}" string reset the default driver.
     *       <br><br>
     *
     *   <strong>{@code -encoding} <var>charset</var></strong><br>
     *       Set the console encoding for this application output. This value has
     *       no impact on the data exchanged with the EPSG database.
     * </blockquote>
     *
     * If this method is run without any EPSG code in argument, it prints the current configuration
     * to the standard output.
     *
     * @param args A list of EPSG code to display.
     *             An arbitrary number of code can be specified on the command line.
     */
    public static void main(String[] args) {
        MonolineFormatter.initGeotools(); // Use custom logger.
        final Arguments arguments = new Arguments(args);
        final String       driver = arguments.getOptionalString("-driver");
        final String   connection = arguments.getOptionalString("-connection");
        final boolean     system = arguments.getFlag("-system");
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        /*
         * If a driver or connection parameters were set, update the preferences.
         */
        Preferences prefs = getPreferences(system);
        if (driver != null) {
            if (driver.equalsIgnoreCase("default")) {
                prefs.remove(DRIVER);
            } else {
                prefs.put(DRIVER, driver);
            }
        }
        if (connection != null) {
            if (connection.equalsIgnoreCase("default")) {
                prefs.remove(CONNECTION);
            } else {
                prefs.put(CONNECTION, connection);
            }
        }
        /*
         * Run without argument other than the above.
         * Prints the current configuration and exit.
         */
        if (args.length == 0) try {
            final Resources resources = Resources.getResources(arguments.locale);
            final TableWriter table = new TableWriter(arguments.out, " \u2502 ");
            table.writeHorizontalSeparator();
            table.nextColumn(); table.write("URL");    // TODO: localize
            table.nextColumn(); table.write("Driver"); // TODO: localize
            table.nextLine();
            table.writeHorizontalSeparator();
            for (int i=0; i<=2; i++) {
                final String title, url, pilote;
                if (i != 2) {
                    prefs  = getPreferences(i!=0);
                    title  = (i!=0) ? "System preferences" : "User preferences"; // TODO: localize.
                    url    = prefs.get(CONNECTION, null);
                    pilote = prefs.get(DRIVER,     null);
                } else {
                    title  = resources.getString(ResourceKeys.DEFAULT_VALUE);
                    url    = DEFAULT_CONNECTION;
                    pilote = DEFAULT_DRIVER;
                }
                if (title  != null) table.write(title);  table.nextColumn();
                if (url    != null) table.write(url);    table.nextColumn();
                if (pilote != null) table.write(pilote); table.nextLine();
            }
            table.writeHorizontalSeparator();
            table.flush();
        } catch (IOException exception) {
            // Should not happen, since we are writting to a PrintWriter.
            exception.printStackTrace(arguments.err);
            return;
        }
        /*
         * Run with at least one EPSG code provided in argument.
         * Fetch objects from the database and format as WKT.
         */
        try {
            DefaultFactory factory = null;
            try {
                factory = new DefaultFactory();
                factory.system = system;
                for (int i=0; i<args.length; i++) {
                    arguments.out.println(factory.createObject(args[i]));
                }
            } finally {
                if (factory != null) {
                    factory.dispose();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace(arguments.err);
        }
        arguments.out.flush();
    }
}
