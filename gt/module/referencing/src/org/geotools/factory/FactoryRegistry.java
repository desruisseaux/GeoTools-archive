/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.factory;

// J2SE dependencies
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.awt.RenderingHints;
import javax.imageio.spi.ServiceRegistry;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;


/**
 * A registry for factories, organized by categories (usualy by <strong>interface</strong>).
 * <p>
 * For example <code>{@link org.opengis.referencing.crs.CRSFactory}.class</code> is a category,
 * and <code>{@link org.opengis.referencing.operation.MathTransformFactory}.class</code>
 * is an other category.
 * </p>
 * <p>
 * For each category, implementations are registered in a file placed in the
 * {@code META-INF/services/} directory, as specified in the {@link ServiceRegistry}
 * javadoc. Those files are usually bundled into the JAR file distributed by the vendor.
 * If the same {@code META-INF/services/} file appears many time in different JARs,
 * they are processed as if their content were merged.
 * </p>
 * <p>
 * Example use: <pre><code>
 * Set categories = Collections.singleton(new Class[] {MathTransformProvider.class});
 * FactoryRegistry registry = new FactoryRegistry(categories);
 * 
 * // get the providers
 * Iterator providers = registry.getProviders(MathTransformProvider.class)
 * </code></pre>
 * </p>
 * <p>
 * <strong>NOTE: This class is not thread safe</strong>. Users are responsable
 * for synchronisation. This is usually done in an utility class wrapping this
 * service registry (e.g. {@link org.geotools.referencing.FactoryFinder}).
 * </p>
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Richard Gould
 * @author Jody Garnett
 *
 * @see org.geotools.referencing.FactoryFinder
 */
public class FactoryRegistry extends ServiceRegistry {
    /**
     * Filters only the factories that are {@linkplain OptionalFactory#isReady ready}.
     */
    private static final Filter FILTER = new Filter() {
        public boolean filter(final Object provider) {
            return !(provider instanceof OptionalFactory) || ((OptionalFactory) provider).isReady();
        }
    };

    /**
     * The logger for all events related to factory registry.
     */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.factory");

    /**
     * Constructs a new registry for the specified categories.
     *
     * @param categories The categories.
     */
    public FactoryRegistry(final Collection categories) {
        // TODO: remove the cast when we will be allowed to compile for J2SE 1.5.
        super((Iterator) categories.iterator());
    }

    /**
     * Returns the providers in the registry for the specified category. Providers that are
     * not {@linkplain OptionalFactory#isReady ready} will be ignored. This method will
     * {@linkplain #scanForPlugins scan for plugins} the first time it is invoked for the
     * given category.
     *
     * @param category The category to look for. Usually an interface class
     *                 (not the actual implementation class).
     * @return Factories ready to use for the specified category.
     */
    public Iterator getServiceProviders(final Class category) {
        Iterator iterator = getServiceProviders(category, FILTER, true);
        if (!iterator.hasNext()) {
            /*
             * No plugin. This method is probably invoked the first time for the specified
             * category, otherwise we should have found at least the Geotools implementation.
             * Scans the plugin now, but for this category only.
             */
            for (final Iterator it=getClassLoaders().iterator(); it.hasNext();) {
                scanForPlugins((ClassLoader) it.next(), category);
            }
            iterator = getServiceProviders(category, FILTER, true);
        }
        return iterator;
    }

    /**
     * Returns the first provider in the registry for the specified category, using the specified
     * map of hints (if any). This method may {@linkplain #scanForPlugins scan for plugins} the
     * first time it is invoked. Except as a result of this scan, no new provider instance is
     * created by the default implementation of this method. The {@link FactoryCreator} class
     * change this behavior however.
     *
     * @param  category The category to look for. Must be one of the categories declared to the
     *                  constructor. Usually an interface class (not the actual implementation
     *                  class).
     * @param  filter   An optional filter, or {@code null} if none.
     *                  This is used for example in order to select the first factory for some
     *                  {@linkplain org.opengis.referencing.AuthorityFactory#getAuthority authority}.
     * @param  hints    A {@linkplain Hints map of hints}, or {@code null} if none.
     * @param  key      The key to use for looking for a user-provided instance in the hints, or
     *                  {@code null} if none.
     * @return A factory {@linkplain OptionalFactory#isReady ready} to use for the specified
     *         category and hints. The returns type is {@code Object} instead of {@link Factory}
     *         because the factory implementation doesn't need to be a Geotools one.
     * @throws FactoryNotFoundException if no factory was found for the specified category, filter
     *         and hints.
     * @throws FactoryRegistryException if a factory can't be returned for some other reason.
     *
     * @see #getServiceProviders
     * @see FactoryCreator#getServiceProvider
     */
    public Object getServiceProvider(final Class     category,
                                     final Filter    filter,
                                     final Hints     hints,
                                     final Hints.Key key)
            throws FactoryRegistryException
    {
        Class implementation = null;
        if (key != null) {
            /*
             * Sanity check: make sure that the key class is appropriate for the category.
             */
            if (!category.isAssignableFrom(key.getValueClass())) {
                // TODO: localize this error message.
                throw new IllegalArgumentException("Illegal hint key: "+key);
            }
            if (hints!=null && !hints.isEmpty()) {
                final Object hint = hints.get(key);
                if (category.isInstance(hint)) {
                    /*
                     * The factory implementation was given explicitly by the user.
                     * Nothing to do; we are done.
                     */
                    return hint;
                }
                if (hint instanceof Class[]) {
                    /*
                     * The user accepts many implementation classes. Tries all of them in the
                     * preference order given by the user. The last class will be tried using
                     * the "normal" path (oustide the loop) in order to get the error message
                     * in case of failure.
                     */
                    final Class[] types = (Class[]) hint;
                    for (int i=0; i<types.length-1; i++) {
                        Object candidate = getServiceProvider(category, types[i], filter, hints);
                        if (candidate != null) {
                            return candidate;
                        }
                    }
                    if (types.length != 0) {
                        implementation = types[types.length-1]; // Last try to be done below.
                    }
                } else {
                    implementation = (Class) hint;
                }
            }
        }
        final Object candidate = getServiceProvider(category, implementation, filter, hints);
        if (candidate != null) {
            return candidate;
        }
        // TODO: provides localized messages.
        final String message;
        if (implementation == null) {
            message = "No factory found for category \"" + Utilities.getShortName(category) + "\".";
        } else {
            message = "No factory found for implementation \"" +
                      Utilities.getShortName(implementation) +"\".";
        }
        throw new FactoryNotFoundException(message);
    }

    /**
     * Search the first implementation in the registery matching the specified conditions.
     * This method do not creates new instance if no matching factory is found.
     *
     * @param  category       The category to look for. Usually an interface class.
     * @param  implementation The desired class for the implementation, or {@code null} if none.
     * @param  filter         An optional filter, or {@code null} if none.
     * @param  hints          A {@linkplain Hints map of hints}, or {@code null} if none.
     * @return A factory for the specified category and hints, or {@code null} if none.
     */
    private Object getServiceProvider(final Class category, final Class implementation,
                                      final Filter filter,  final Hints hints)
    {
        for (final Iterator it=getServiceProviders(category); it.hasNext();) {
            final Object candidate = it.next();
            if (filter!=null && !filter.filter(candidate)) {
                continue;
            }
            if (implementation!=null && !implementation.isInstance(candidate)) {
                continue;
            }
            if (hints != null) {
                if (candidate instanceof Factory) {
                    if (!isAcceptable((Factory) candidate, category, hints, null)) {
                        continue;
                    }
                }
                if (!isAcceptable(candidate, category, hints)) {
                    continue;
                }
            }
            return candidate;
        }
        return null;
    }

    /**
     * Returns {@code true} is the specified {@code factory} meets the requirements specified by a
     * map of {@code hints}.
     *
     * @param factory     The factory to checks.
     * @param category    The factory's category. Usually be an interface class.
     * @param hints       The user requirements.
     * @param alreadyDone Should be {@code null} except on recursive calls (for internal use only).
     * @return {@code true} if the {@code factory} meets the user requirements.
     */
    private boolean isAcceptable(final Factory factory,
                                 final Class   category,
                                 final Hints   hints,
                                       Set     alreadyDone)
    {
        for (final Iterator it=factory.getImplementationHints().entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final Object    value = entry.getValue();
            final Object expected = hints.get(entry.getKey());
            if (expected != null) {
                /*
                 * We have found a hint that matter. Check if the
                 * available factory meets the user's criterions.
                 */
                if (expected instanceof Class) {
                    if (!((Class) expected).isInstance(value)) {
                        return false;
                    }
                } else if (expected instanceof Class[]) {
                    final Class[] types = (Class[]) expected;
                    int i=0;
                    do if (i >= types.length) return false;
                    while (!types[i++].isInstance(value));
                } else if (!expected.equals(value)) {
                    return false;
                }
            }
            // User check (overridable).
            if (!isAcceptable(value, category, hints)) {
                return false;
            }
            /*
             * Check recursively in factory depencies, if any. The 'alreadyDone' set is a safety
             * against cyclic dependencies, in order to protect ourself against never-ending loops.
             */
            if (value instanceof Factory) {
                if (alreadyDone == null) {
                    alreadyDone = new HashSet();
                }
                if (!alreadyDone.contains(value)) {
                    alreadyDone.add(factory);
                    if (!isAcceptable((Factory) value, category, hints, alreadyDone)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the specified {@code provider} meets the requirements specified by a
     * map of {@code hints}. This method is invoked automatically when the {@code provider} is known
     * to meets standard requirements.
     * <br><br>
     * The default implementation always returns {@code true}. Override this method if
     * more checks are needed, typically for non-Geotools implementation. For example a
     * JTS geometry factory finder may overrides this method in order to check if a
     * {@link com.vividsolutions.jts.geom.GeometryFactory} uses the required
     * {@link com.vividsolutions.jts.geom.CoordinateSequenceFactory}.
     *
     * @param provider The provider to checks.
     * @param category The factory's category. Usually an interface class.
     * @param hints    The user requirements.
     * @return {@code true} if the {@code provider} meets the user requirements.
     */
    protected boolean isAcceptable(final Object provider, final Class category, final Hints hints) {
        return true;
    }

    /**
     * Returns all class loaders to be used for scanning plugins. Current implementation
     * returns the following class loaders:
     *
     * <ul>
     *   <li>{@linkplain Class#getClassLoader This object class loader}</li>
     *   <li>{@linkplain Thread#getContextClassLoader The thread context class loader}</li>
     *   <li>{@linkplain ClassLoader#getSystemClassLoader The system class loader}</li>
     * </ul>
     *
     * The actual number of class loaders may be smaller if redundancies was found.
     * If some more classloaders should be scanned, they shall be added into the code
     * of this method.
     */
    public final Set getClassLoaders() {
        final Set loaders = new HashSet();
        for (int i=0; i<4; i++) {
            final ClassLoader loader;
            try {
                switch (i) {
                    case 0:  loader = getClass().getClassLoader();                    break;
                    case 1:  loader = FactoryRegistry.class.getClassLoader();         break;
                    case 2:  loader = Thread.currentThread().getContextClassLoader(); break;
                    case 3:  loader = ClassLoader.getSystemClassLoader();             break;
                    // Add any supplementary class loaders here, if needed.
                    default: throw new AssertionError(i); // Should never happen.
                }
            } catch (SecurityException exception) {
                // We are not allowed to get a class loader.
                // Continue; some other class loader may be available.
                continue;
            }
            loaders.add(loader);
        }
        /*
         * We now have a set of class loaders with duplicated object already removed
         * (e.g. system classloader == context classloader). However, we may still
         * have an other form of redundancie. A class loader may be the parent of an
         * other one. Try to remove those dependencies.
         */
        final ClassLoader[] asArray = (ClassLoader[]) loaders.toArray(new ClassLoader[loaders.size()]);
        for (int i=0; i<asArray.length; i++) {
            ClassLoader loader = asArray[i];
            try {
                while ((loader=loader.getParent()) != null) {
                    loaders.remove(loader);
                }
            } catch (SecurityException exception) {
                // We are not allowed to fetch the parent class loader.
                // Ignore (some redundancies may remains).
            }
        }
        if (loaders.isEmpty()) {
            LOGGER.warning("No class loaders available");
        }
        return loaders;
    }

    /**
     * Scans for factory plug-ins on the application class path. This method is
     * needed because the application class path can theoretically change, or
     * additional plug-ins may become available. Rather than re-scanning the
     * classpath on every invocation of the API, the class path is scanned
     * automatically only on the first invocation. Clients can call this
     * method to prompt a re-scan. Thus this method need only be invoked by
     * sophisticated applications which dynamically make new plug-ins
     * available at runtime.
     */
    public void scanForPlugins() {
        final Set loaders = getClassLoaders();
        for (final Iterator categories=getCategories(); categories.hasNext();) {
            final Class category = (Class) categories.next();
            for (final Iterator it=loaders.iterator(); it.hasNext();) {
                final ClassLoader loader = (ClassLoader) it.next();
                    scanForPlugins(loader, category);
            }
        }
    }

    /**
     * Scans for factory plug-ins of the given category.
     *
     * @param loader The class loader to use.
     * @param category The category to scan for plug-ins.
     *
     * @todo localize log and error messages.
     */
    private void scanForPlugins(final ClassLoader loader, final Class category) {
        final Iterator   factories = ServiceRegistry.lookupProviders(category, loader);
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringBuffer message = new StringBuffer();
        message.append("Scan for '");
        message.append(Utilities.getShortName(category));
        message.append("' implementations:");
        boolean newServices = false;
        while (factories.hasNext()) {
            Object factory;
            try {
                factory = factories.next();
            } catch (NoClassDefFoundError error) {
                /*
                 * A provider can't be registered because of some missing dependencies.
                 * This occurs for example when trying to register the WarpTransform2D
                 * math transform on a machine without JAI installation. Since the service
                 * may not be essential (this is the case of WarpTransform2D), just skip it.
                 */
                loadingFailure(category, error);
                continue;
            } catch (RuntimeException error) {
                /*
                 * Failed to register a service for a reason not related to Java Virtual Machine
                 * errors. It may be some service-dependent missing resources.
                 */
                loadingFailure(category, error);
                continue;
            }
            final Class factoryClass = factory.getClass();
            /*
             * If the factory implements more than one interface and an instance were
             * already registered, reuse the same instance instead of duplicating it.
             */
            final Object replacement = getServiceProviderByClass(factoryClass);
            if (replacement != null) {
                factory = replacement;
            }
            if (registerServiceProvider(factory, category)) {
                /*
                 * The factory is now registered. Add it to the message to be logged
                 * at the end of this method. We log all factories together in order
                 * to produces only one log entry, since some registration (e.g.
                 * MathTransformProviders) may be quite extensive.
                 */
                message.append(lineSeparator);
                message.append("  Register ");
                message.append(factoryClass.getName());
                newServices = true;
            }
        }
        /*
         * If a system property was setup, load the class (if not already registered)
         * and move it in front of any other factory. This is done for compatibility
         * with legacy FactoryFinder implementation.
         */
        try {
            final String classname = System.getProperty(category.getName());
            if (classname != null) try {
                final Class factoryClass = loader.loadClass(classname);
                Object factory = getServiceProviderByClass(factoryClass);
                if (factory == null) try {
                    factory = factoryClass.newInstance();
                    if (registerServiceProvider(factory, category)) {
                        message.append(lineSeparator);
                        message.append("  Register ");
                        message.append(factoryClass.getName());
                        message.append(" from system property");
                        newServices = true;
                    }
                } catch (IllegalAccessException exception) {
                    // TODO: localize
                    throw new FactoryRegistryException("Can't create factory for the \"" +
                                classname + "\" system property.", exception);
                } catch (InstantiationException exception) {
                    // TODO: localize
                    throw new FactoryRegistryException("Can't create factory for the \"" +
                                classname + "\" system property.", exception);
                }
                /*
                 * Put this factory in front of every other factories (including the ones loaded
                 * in previous class loaders, which is why we don't inline this ordering in the
                 * above loop). Note: if some factories were not yet registered, they will not
                 * be properly ordered. Since this code exists more for compatibility reasons
                 * than as a commited API, we ignore this short comming for now.
                 */
                for (final Iterator it=getServiceProviders(category, false); it.hasNext();) {
                    final Object other = it.next();
                    if (other != factory) {
                        setOrdering(category, factory, other);
                    }
                }
            } catch (ClassNotFoundException exception) {
                // The class has not been found, maybe because we are not using the appropriate
                // class loader. Ignore (do not thrown an exception), in order to give a chance
                // to the caller to invokes this method again with a different class loader.
            }
        } catch (SecurityException exception) {
            // We are not allowed to read property, probably
            // because we are running in an applet. Ignore...
        }
        /*
         * Log the list of registered factories.
         */
        if (newServices) {
            final LogRecord record = new LogRecord(Level.CONFIG, message.toString());
            record.setSourceClassName(FactoryRegistry.class.getName());
            record.setSourceMethodName("scanForPlugins");
            LOGGER.log(record);
        }
    }

    /**
     * Invoked when a service can't be loaded. Log a warning, but do not stop the process.
     */
    private static void loadingFailure(final Class category, final Throwable error) {
        final LogRecord record = Logging.format(Level.WARNING,
                LoggingKeys.CANT_LOAD_SERVICE_$1, Utilities.getShortName(category));
        record.setSourceClassName("FactoryRegistry");
        record.setSourceMethodName("scanForPlugins");
        record.setThrown(error);
        LOGGER.log(record);
    }

    /**
     * Set pairwise ordering between all services according a comparator. Calls to
     * <code>{@linkplain Comparator#compare compare}(factory1, factory2)</code> should returns:
     * <ul>
     *   <li>{@code -1} if {@code factory1} is preferred to {@code factory2}</li>
     *   <li>{@code +1} if {@code factory2} is preferred to {@code factory1}</li>
     *   <li>{@code 0} if there is no preferred order between {@code factory1} and
     *       {@code factory2}</li>
     * </ul>
     *
     * @param  category   The category to set ordering.
     * @param  comparator The comparator to use for ordering.
     * @return {@code true} if at least one ordering setting has been modified as a consequence
     *         of this call.
     */
    public boolean setOrdering(final Class category, final Comparator comparator) {
        boolean set = false;
        final List previous = new ArrayList();
        for (final Iterator it=getServiceProviders(category, false); it.hasNext();) {
            final Object f1 = it.next();
            for (int i=previous.size(); --i>=0;) {
                final Object f2 = previous.get(i);
                final int c;
                try {
                    c = comparator.compare(f1, f2);
                } catch (ClassCastException exception) {
                    /*
                     * This exception is expected if the user-supplied comparator follows strictly
                     * the java.util.Comparator specification and has determined that it can't
                     * compare the supplied factories. From ServiceRegistry point of view, it just
                     * means that the ordering between those factories will stay undeterminated.
                     */
                    continue;
                }
                if (c > 0) {
                    set |= setOrdering(category, f1, f2);
                } else if (c < 0) {
                    set |= setOrdering(category, f2, f1);
                }
            }
            previous.add(f1);
        }
        return set;
    }

    /**
     * Sets or unsets a pairwise ordering between all services meeting a criterion. For example
     * in the CRS framework ({@link org.geotools.referencing.FactoryFinder}), this is used for
     * setting ordering between all services provided by two vendors, or for two authorities.
     * If one or both services are not currently registered, or if the desired ordering is
     * already set/unset, nothing happens and false is returned.
     *
     * @param base     The base category. Only categories {@linkplain Class#isAssignableFrom
     *                 assignable} to {@code base} will be processed.
     * @param set      {@code true} for setting the ordering, or {@code false} for unsetting.
     * @param service1 filter for the preferred service.
     * @param service2 filter for the service to which {@code service1} is preferred.
     */
    public boolean setOrdering(final Class  base,
                               final boolean set,
                               final Filter service1,
                               final Filter service2)
    {
        boolean done = false;
        for (final Iterator categories=getCategories(); categories.hasNext();) {
            final Class category = (Class) categories.next();
            if (base.isAssignableFrom(category)) {
                Object impl1 = null;
                Object impl2 = null;
                for (final Iterator it=getServiceProviders(category); it.hasNext();) {
                    final Object factory = it.next();
                    if (service1.filter(factory)) impl1 = factory;
                    if (service2.filter(factory)) impl2 = factory;
                    if (impl1!=null && impl2!=null && impl1!=impl2) {
                        if (set) done |=   setOrdering(category, impl1, impl2);
                        else     done |= unsetOrdering(category, impl1, impl2);
                    }
                }
            }
        }
        return done;
    }
}
