/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.factory;

// J2SE dependencies
import java.awt.RenderingHints;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

// Geotools dependencies
import org.geotools.resources.XMath;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.logging.CommonsLoggerFactory;
import org.geotools.util.logging.Log4JLoggerFactory;
import org.geotools.util.logging.LoggerFactory;
import org.geotools.util.logging.Logging;
import org.geotools.util.Version;


/**
 * Static methods relative to the global GeoTools configuration. GeoTools can be configured
 * in a system-wide basis through {@linkplain System#getProperties system properties}, some
 * of them are declared as {@link String} constants in this class.
 * <p>
 * There are two aspects to the configuration of GeoTools:
 * <ul>
 * <li>Default Settings: Are handled as the Hints returned by {@link getDefaultHints()}, the default values
 * can be provided by your code, or specified using system properties.
 * <li>Integration JNDI: Telling the GeoTools library about the facilities of your application, or application
 * container takes several forms. This class provides the {@link initContext( InitialContext ) } method
 * allowing you to tell GeoTools about the JNDI context you would like it to use.
 * <li>Intergration Plugins: If you are hosting GeoTools in a alternate plugin system such as Spring or OSGi you will need to hunt down the FactoryFinders and
 * register additional "FactoryIterators" you would like GeoTools to search using the {@link addFactoryIteratorProvider} method.
 * </ul>
 * <h3>JNDI Integration</h3>
 * This class provides a {@linkplain InitialContext initial context} for <cite>Java Naming and Directory
 * Interfaces</cite> (JNDI) in Geotools. This classes provides a central place where initial
 * context can been found for the Geotools library. This context is used for example by the
 * {@linkplain org.geotools.referencing.factory.epsg.ThreadedEpsgFactory EPSG factory} in order to
 * find connection parameters to an EPSG database. Using JNDI, such connection parameters can
 * be set in a J2EE environment.
 * 
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public final class GeoTools {
    /**
     * The current GeoTools version. The separator character must be the dot.
     */
    private static final Version VERSION = new Version("2.4.SNAPSHOT");

    /**
     * Object to inform about system-wide configuration changes.
     * We use the Swing utility listener list since it is lightweight and thread-safe.
     * Note that it doesn't involve any dependency to the remaining of Swing library.
     */
    private static final EventListenerList LISTENERS = new EventListenerList();

    /**
     * The bindings between {@linkplain System#getProperties system properties} and
     * a hint key. This field must be declared before any call to the {@link #bind}
     * method.
     */
    private static final Map/*<String, RenderingHints.Key>*/ BINDINGS = new HashMap();

    /**
     * The {@linkplain System#getProperty(String) system property} key for the default value to be
     * assigned to the {@link Hints#CRS_AUTHORITY_EXTRA_DIRECTORY CRS_AUTHORITY_EXTRA_DIRECTORY}
     * hint.
     *
     * @see Hints#CRS_AUTHORITY_EXTRA_DIRECTORY
     * @see #getDefaultHints
     */
    public static final String CRS_AUTHORITY_EXTRA_DIRECTORY =
            "org.geotools.referencing.crs-directory";
    static {
        bind(CRS_AUTHORITY_EXTRA_DIRECTORY, Hints.CRS_AUTHORITY_EXTRA_DIRECTORY);
    }

    /**
     * The {@linkplain System#getProperty(String) system property} key for the default
     * value to be assigned to the {@link Hints#EPSG_DATA_SOURCE EPSG_DATA_SOURCE} hint.
     *
     * @see Hints#EPSG_DATA_SOURCE
     * @see #getDefaultHints
     */
    public static final String EPSG_DATA_SOURCE =
            "org.geotools.referencing.epsg-datasource";
    static {
        bind(EPSG_DATA_SOURCE, Hints.EPSG_DATA_SOURCE);
    }

    /**
     * The {@linkplain System#getProperty(String) system property} key for the default
     * value to be assigned to the {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
     * FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint.
     *
     * This setting can provide a transition path for projects expecting a (<var>longitude</var>,
     * <var>latitude</var>) axis order on a system-wide level. Application developpers can set the
     * default value as below:
     *
     * <blockquote><pre>
     * System.setProperty(FORCE_LONGITUDE_FIRST_AXIS_ORDER, "true");
     * </pre></blockquote>
     *
     * Note that this system property applies mostly to the default EPSG factory. Most other
     * factories ({@code "CRS"}, {@code "AUTO"}, <cite>etc.</cite>) don't need this property
     * since they use (<var>longitude</var>, <var>latitude</var>) axis order by design.
     *
     * @see Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
     * @see #getDefaultHints
     */
    public static final String FORCE_LONGITUDE_FIRST_AXIS_ORDER =
            "org.geotools.referencing.forceXY";
    static {
        bind(FORCE_LONGITUDE_FIRST_AXIS_ORDER, Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER);
    }

    /**
     * The initial context. Will be created only when first needed.
     */
    private static InitialContext context;

    /**
     * Do not allow instantiation of this class.
     */
    private GeoTools() {
    }

    /**
     * Binds the specified {@linkplain System#getProperty(String) system property}
     * to the specified key. Only one key can be binded to a given system property.
     * However the same key can be binded to more than one system property names,
     * in which case the extra system property names are aliases.
     *
     * @param  property The system property.
     * @param  key The key to bind to the system property.
     * @throws IllegalArgumentException if an other key is already bounds
     *         to the given system property.
     */
    private static void bind(final String property, final RenderingHints.Key key) {
        synchronized (BINDINGS) {
            final RenderingHints.Key old = (RenderingHints.Key) BINDINGS.put(property, key);
            if (old == null) {
                return;
            }
            // Roll back
            BINDINGS.put(property, old);
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                "property", property));
    }

    /**
     * Reports back the version of GeoTools being used.
     */
    public static Version getVersion(){
         return VERSION;
    }

    /**
     * Initializes GeoTools for use. This convenience method performs various tasks (more may
     * be added in the future), including setting up the {@linkplain java.util.logging Java
     * logging framework} in one of the following states:
     * <p>
     * <ul>
     *   <li>If the <A HREF="http://jakarta.apache.org/commons/logging/">commons-logging</A>
     *       framework is available, then every logging message in the {@code org.geotools}
     *       namespace sent to the Java {@linkplain java.util.logging.Logger logger} are
     *       redirected to commons-logging.</li>
     * 
     *   <li>Otherwise, the Java logging {@linkplain java.util.logging.Formatter formatter} for
     *       console output is replaced by a {@linkplain org.geotools.util.logging.MonolineFormatter
     *       monoline formatter}.</li>
     * </ul>
     * <p>
     * In addition, the {@linkplain #getDefaultHints default hints} are initialized to the
     * specified {@code hints}.
     * <p>
     * Note that invoking this method is usually <strong>not</strong> needed for proper working
     * of the Geotools library. It is just a convenience method for overwriting some Java and
     * Geotools default settings in a way that seems to be common in server environment. Such
     * overwriting may not be wanted for every situations.
     * <p>
     * Example of typical invocation in a Geoserver environment:
     * 
     * <blockquote><pre>
     * Hints hints = new Hints(null);
     * hints.put({@linkplain Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER}, Boolean.TRUE);
     * hints.put({@linkplain Hints#FORCE_AXIS_ORDER_HONORING}, "http");
     * GeoTools.init(hints);
     * </pre></blockquote>
     * 
     * @see Logging#setLoggingFramework
     * @see Logging#forceMonolineConsoleOutput
     * @see Hints#putSystemDefault
     * @see #getDefaultHints
     */
    public static void init(final Hints hints) {
        final Logging log = Logging.GEOTOOLS;
        try {
            log.setLoggerFactory(CommonsLoggerFactory.getInstance());
        } catch(Exception commonsException) {
            try {
                log.setLoggerFactory(Log4JLoggerFactory.getInstance());
            } catch(Exception log4jException) {
                // nothing to do, we already tried our best
            }
        }
        // if java logging is used, force monoline console output
        if (log.getLoggerFactory() == null) {
            log.forceMonolineConsoleOutput();
        }
        Hints.putSystemDefault(hints);
    }

    /**
     * Set the global LogginFactory.
     * 
     * This method is the same as Logging.GEOTOOLS.setLoggerFactory( factory ), GeoTools
     * ships with support for commons logging and log4j. This method exists to allow you
     * supply your own implementation (this is sometimes required when using a GeoTools
     * application in an exotic environment like Eclipse, OC4J or your application).
     * 
     * @see LoggingFramework
     * @param factory
     */
    public void setLoggerFactory(LoggerFactory factory){
        final Logging log = Logging.GEOTOOLS;
        log.setLoggerFactory( factory );
    }
    
    /**
     * Forces the initial context for test cases, or as needed.
     * 
     * @see #getInitialContext
     *
     * @since 2.4
     */
    public static synchronized void init(final InitialContext applicationContext) {
        context = applicationContext;
    }

    /**
     * Scans {@linkplain System#getProperties system properties} for any property keys
     * defined in this class, and add their values to the specified map of hints. For
     * example if the {@value #FORCE_LONGITUDE_FIRST_AXIS_ORDER} system property is
     * defined, then the {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
     * FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint will be added to the set of hints.
     *
     * @return {@code true} if at least one hint changed as a result of this scan,
     *         or {@code false} otherwise.
     */
    static boolean scanForSystemHints(final Hints hints) {
        assert Thread.holdsLock(hints);
        boolean changed = false;
        synchronized (BINDINGS) {
            for (final Iterator it=BINDINGS.entrySet().iterator(); it.hasNext();) {
                final Map.Entry entry = (Map.Entry) it.next();
                final String propertyKey = (String) entry.getKey();
                final String property;
                try {
                    property = System.getProperty(propertyKey);
                } catch (SecurityException e) {
                    unexpectedException(e);
                    continue;
                }
                if (property != null) {
                    /*
                     * Converts the system property value from String to Object (java.lang.Boolean
                     * or java.lang.Number). We perform this conversion only if the key is exactly
                     * of kind Hints.Key,  not a subclass like ClassKey, in order to avoid useless
                     * class loading on  'getValueClass()'  method invocation (ClassKey don't make
                     * sense for Boolean and Number, which are the only types that we convert here).
                     */
                    Object value = property;
                    final RenderingHints.Key hintKey = (RenderingHints.Key) entry.getValue();
                    if (hintKey.getClass().equals(Hints.Key.class)) {
                        final Class type = ((Hints.Key) hintKey).getValueClass();
                        if (type.equals(Boolean.class)) {
                            value = Boolean.valueOf(property);
                        } else if (Number.class.isAssignableFrom(type)) try {
                            value = XMath.valueOf(type, property);
                        } catch (NumberFormatException e) {
                            unexpectedException(e);
                            continue;
                        }
                    }
                    final Object old;
                    try {
                        old = hints.put(hintKey, value);
                    } catch (IllegalArgumentException e) {
                        // The property value is illegal for this hint.
                        unexpectedException(e);
                        continue;
                    }
                    if (!changed && !Utilities.equals(old, value)) {
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Logs an exception as if it originated from {@link Hints#scanSystemProperties},
     * since it is the public API that may invokes this method.
     */
    private static void unexpectedException(final Exception exception) {
        Logging.unexpectedException("org.geotools.factory",
                Hints.class, "scanSystemProperties", exception);
    }

    /**
     * Returns the default set of hints used for the various utility classes.
     * This default set is determined by:
     * <p>
     * <ul>
     *   <li>The {@linplain System#getProperties system properties} available. Some property
     *       keys are enumerated in the {@link GeoTools} class.</li>
     *   <li>Any hints added by call to the {@link Hints#putSystemDefault}
     *       or {@link #init} method.</li>
     * </ul>
     * <p>
     * <b>Long term plan:</b>
     * We would like to transition the utility classes to being injected with their
     * required factories, either by taking Hints as part of their constructor, or
     * otherwise. Making this change would be a three step process 1) create instance
     * methods for each static final class method 2) create an singleton instance of the
     * class 3) change each static final class method into a call to the singleton. With
     * this in place we could then encourage client code to make use of utility class
     * instances before eventually retiring the static final methods.
     *
     * @return A copy of the default hints. It is safe to add to it.
     */
    public static Hints getDefaultHints() {
        return Hints.getDefaults();
    }

    /**
     * Returns the default initial context.
     *
     * @param  hints An optional set of hints, or {@code null} if none.
     * @return The initial context (never {@code null}).
     * @throws NamingException if the initial context can't be created.
     *
     * @see #init(InitialContext)
     *
     * @since 2.4
     */
    public static synchronized InitialContext getInitialContext(final Hints hints)
            throws NamingException
    {
        if (context == null) {
            context = new InitialContext();
        }
        return context;
    }

    /**
     * Adds an alternative way to search for factory implementations. {@link FactoryRegistry} has
     * a default mechanism bundled in it, which uses the content of all {@code META-INF/services}
     * directories found on the classpath. This {@code addFactoryIteratorProvider} method allows
     * to specify additional discovery algorithms. It may be useful in the context of some
     * frameworks that use the <cite>constructor injection</cite> pattern, like the
     * <a href="http://www.springframework.org/">Spring framework</a>.
     */
    public static void addFactoryIteratorProvider(final FactoryIteratorProvider provider) {
        Factories.addFactoryIteratorProvider(provider);
    }

    /**
     * Removes a provider that was previously {@linkplain #addFactoryIteratorProvider added}.
     * Note that factories already obtained from the specified provider will not be
     * {@linkplain FactoryRegistry#deregisterServiceProvider deregistered} by this method.
     */
    public static void removeFactoryIteratorProvider(final FactoryIteratorProvider provider) {
        Factories.removeFactoryIteratorProvider(provider);
    }

    /**
     * Adds the specified listener to the list of objects to inform when system-wide
     * configuration changed.
     */
    public static void addChangeListener(final ChangeListener listener) {
        removeChangeListener(listener); // Ensure singleton.
        LISTENERS.add(ChangeListener.class, listener);
    }

    /**
     * Removes the specified listener from the list of objects to inform when system-wide
     * configuration changed.
     */
    public static void removeChangeListener(final ChangeListener listener) {
        LISTENERS.remove(ChangeListener.class, listener);
    }

    /**
     * Informs every listeners that system-wide configuration changed.
     */
    public static void fireConfigurationChanged() {
        final ChangeEvent event = new ChangeEvent(GeoTools.class);
        final Object[] listeners = LISTENERS.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i+1]).stateChanged(event);
            }
        }
    }

    /**
     * Reports the GeoTools {@linkplain #getVersion version} number to the
     * {@linkplain System#out standard output stream}.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        args = arguments.getRemainingArguments(0);
        arguments.out.print("GeoTools version ");
        arguments.out.println(getVersion());
        final Hints hints = getDefaultHints();
        if (hints!=null && !hints.isEmpty()) {
            arguments.out.println(hints);
        }
    }
    
    public static String fixName( String name ) {
        try {
            return fixName( getInitialContext(null), name );
        } catch (NamingException e) {
            return name;
        }
    }
    /**
     * We need to that names defined for use in GeoTools end up being useful to the
     * InitialContext in question.
     * <p>
     * Names may be strung togehter in a varity of ways depending on the implementation
     * of InitialContext. In GeoTools we use "jdbc:EPSG" internally, but many implementaitons
     * use the form "jdbc/EPSG". Calling this method before use will set you right.
     * </p>
     * @param context
     * @param name Name of the form "jdbc:EPSG"
     * @return name fixed up with InitialContext.composeName( string, string )
     */
    public static String fixName( InitialContext context, String name ) {
        try {
            if( context == null || name == null ) {
                return name;
            }
            if( name.indexOf(':') != -1 ){
                String fixed = null;
                for( String part : name.split(":")){
                    if( fixed == null ){
                        fixed = part;
                    }
                    else {                   
                        fixed = context.composeName( fixed, part );                    
                    }
                }
                return fixed;
            }
            if( name.indexOf('/') != -1 ){
                String fixed = null;
                for( String part : name.split("/")){
                    if( fixed == null ){
                        fixed = part;
                    }
                    else {                   
                        fixed = context.composeName( fixed, part );                    
                    }
                }
                return fixed;
            }
            return name;
        } catch (NamingException e) {
            return name;
        }
    }
}
