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
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

// Geotools dependencies
import org.geotools.resources.XMath;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Logging;
import org.geotools.util.Version;


/**
 * Static methods relative to the global Geotools configuration. GeoTools can be configured
 * in a system-wide basis through {@linkplain System#getProperties system properties}, some
 * of them are declared as {@link String} constants in this class.
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
     * Initialize GeoTools for use. This convenience method performs various tasks (more may
     * be added in the future), including setting up the {@linkplain java.util.logging Java
     * logging framework} in one of the following states:
     * <p>
     * <ul>
     *   <li>If the <A HREF="http://jakarta.apache.org/commons/logging/">commons-logging</A>
     *       framework is available, then every logging message in the {@code org.geotools}
     *       namespace sent to the Java {@linkplain java.util.logging.Logger logger} is
     *       redirected to commons-logging.</li>
     * 
     *   <li>Otherwise, the Java logging {@linkplain java.util.logging.Formatter formatter} for
     *       console output is replaced by a {@linkplain org.geotools.util.MonolineFormatter
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
     * Geotools.init(new Hints({@linkplain Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER}, Boolean.TRUE));
     * </pre></blockquote>
     * 
     * 
     * @see Logging#redirectToCommonsLogging
     * @see Logging#forceMonolineConsoleOutput
     * @see Hints#putSystemDefault
     * @see #getDefaultHints
     */
    public static void init(final Hints hints) {
        if (!Logging.isCommonsLoggingAvailable() || !Logging.GEOTOOLS.redirectToCommonsLogging()) {
            Logging.GEOTOOLS.forceMonolineConsoleOutput();
        }
        Hints.putSystemDefault(hints);
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
     * Reports the GeoTools {@linkplain #getVersion version} number to the
     * {@linkplain System#out standard output stream}.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        args = arguments.getRemainingArguments(0);
        arguments.out.print("GeoTools version ");
        arguments.out.println(getVersion());
    }
}
