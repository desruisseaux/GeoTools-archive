/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.lang.ref.Reference;
import java.awt.RenderingHints;
import javax.imageio.spi.ServiceRegistry;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;


/**
 * A registry for factories, organized by categories (usualy by <strong>interface</strong>).
 * For example <code>{@link org.opengis.referencing.crs.CRSFactory}.class</code> is a category,
 * and <code>{@link org.opengis.referencing.operation.MathTransformFactory}.class</code>
 * is an other category.
 * <p>
 * For each category, implementations are registered in a file placed in the
 * {@code META-INF/services/} directory, as specified in the {@link ServiceRegistry}
 * javadoc. Those files are usually bundled into the JAR file distributed by the vendor.
 * If the same {@code META-INF/services/} file appears many time in different JARs,
 * they are processed as if their content were merged.
 * <p>
 * Example use:
 * <blockquote><pre>
 * Set categories = Collections.singleton(new Class[] {MathTransformProvider.class});
 * FactoryRegistry registry = new FactoryRegistry(categories);
 * 
 * // get the providers
 * Iterator providers = registry.getProviders(MathTransformProvider.class)
 * </pre></blockquote>
 * <p>
 * <strong>NOTE: This class is not thread safe</strong>. Users are responsable
 * for synchronisation. This is usually done in an utility class wrapping this
 * service registry (e.g. {@link org.geotools.referencing.FactoryFinder}).
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Richard Gould
 * @author Jody Garnett
 *
 * @see org.geotools.referencing.FactoryFinder
 * @see org.geotools.coverage.FactoryFinder
 */
public class FactoryRegistry extends ServiceRegistry {
    /**
     * The logger for all events related to factory registry.
     */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.factory");

    /**
     * Alternative scanning methods. This is used by {@link #scanForPlugins(Collection,Class)}
     * in addition of the default lookup mechanism.
     */
    private final Collection/*<FactoryIteratorProvider>*/ iteratorProviders = new LinkedHashSet();

    /**
     * Categories under scanning. This is used by {@link #scanForPlugins(Collection,Class)}
     * as a guard against infinite recursivity (i.e. when a factory to be scanned request
     * an other dependency of the same category).
     */
    private final Set/*<Class>*/ scanningCategories = new HashSet();

    /**
     * Factories under testing for availablity. This is used by {@link #isAvailable}
     * as a guard against infinite recursivity.
     */
    private final Set/*<Class<? extends OptionalFactory>>*/ testingFactories = new HashSet();

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
     * not {@linkplain OptionalFactory#isAvailable available} will be ignored. This method
     * will {@linkplain #scanForPlugins() scan for plugins} the first time it is invoked for
     * the given category.
     *
     * @param category The category to look for. Usually an interface class
     *                 (not the actual implementation class).
     * @return Factories ready to use for the specified category.
     *
     * @deprecated Replaced by {@link #getServiceProviders(Class, Filter, Hints)}.
     */
    public Iterator getServiceProviders(final Class category) {
        return getServiceProviders(category, null, null);
    }

    /**
     * Returns the providers in the registry for the specified category, filter and hints.
     * Providers that are not {@linkplain OptionalFactory#isAvailable available} will be
     * ignored. This method will {@linkplain #scanForPlugins() scan for plugins} the first
     * time it is invoked for the given category.
     *
     * @param category The category to look for. Usually an interface class
     *                 (not the actual implementation class).
     * @param filter   The optional filter, or {@code null}.
     * @param hints    The optional user requirements, or {@code null}.
     * @return Factories ready to use for the specified category, filter and hints.
     *
     * @since 2.3
     */
    public Iterator getServiceProviders(final Class category, final Filter filter, final Hints hints) {
        /*
         * The implementation of this method is very similar to the 'getUnfilteredProviders'
         * one except for filter handling. See the comments in 'getUnfilteredProviders' for
         * more implementation details.
         */
        if (scanningCategories.contains(category)) {
            throw new RecursiveSearchException(category);
        }
        final Filter hintsFilter = new Filter() {
            public boolean filter(final Object provider) {
                return isAcceptable(provider, category, hints, filter);
            }
        };
        Iterator iterator = getServiceProviders(category, hintsFilter, true);
        if (!iterator.hasNext()) {
            scanForPlugins(getClassLoaders(), category);
            iterator = getServiceProviders(category, hintsFilter, true);
        }
        return iterator;
    }

    /**
     * Implementation of {@link #getServiceProviders(Class, Filter, Hints)} without the filtering
     * applied by the {@link #isAcceptable(Object, Class, Hints, Filter)} method. If this filtering
     * is not already presents in the filter given to this method, then it must be applied on the
     * elements returned by the iterator. The later is preferrable when:
     * <p>
     * <ul>
     *   <li>There is some cheaper tests to perform before {@code isAcceptable}.</li>
     *   <li>We don't want a restrictive filter in order to avoid trigging a classpath
     *       scan if this method doesn't found any element to iterate.</li>
     * </ul>
     */
    final Iterator getUnfilteredProviders(final Class category) {
        if (!scanningCategories.isEmpty()) {
            /*
             * The 'scanningCategories' map is almost always empty, so we use the above 'isEmpty()'
             * check because it is fast. If the map is not empty, then this mean that a scanning is
             * under progress, i.e. 'scanForPlugins' is currently being executed. This is okay as
             * long as the user is not asking for one of the categories under scanning. Otherwise,
             * the answer returned by 'getServiceProviders' would be incomplete because not all
             * plugins have been found yet. This can lead to some bugs hard to spot because this
             * methoud could complete normally but return the wrong plugin. It is safer to thrown
             * an exception so the user is advised that something is wrong.
             */
            if (scanningCategories.contains(category)) {
                throw new RecursiveSearchException(category);
            }
        }
        Iterator iterator = getServiceProviders(category, true);
        if (!iterator.hasNext()) {
            /*
             * No plugin. This method is probably invoked the first time for the specified
             * category, otherwise we should have found at least the Geotools implementation.
             * Scans the plugin now, but for this category only.
             */
            scanForPlugins(getClassLoaders(), category);
            iterator = getServiceProviders(category, true);
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
     * @return A factory {@linkplain OptionalFactory#isAvailable available} for use for the
     *         specified category and hints. The returns type is {@code Object} instead of
     *         {@link Factory} because the factory implementation doesn't need to be a Geotools one.
     * @throws FactoryNotFoundException if no factory was found for the specified category, filter
     *         and hints.
     * @throws FactoryRegistryException if a factory can't be returned for some other reason.
     *
     * @see #getServiceProviders(Class, Filter, Hints)
     * @see FactoryCreator#getServiceProvider
     */
    public Object getServiceProvider(final Class category, final Filter filter,
                                     Hints hints, final Hints.Key key)
            throws FactoryRegistryException
    {
        Class implementation = null;
        if (key != null) {
            /*
             * Sanity check: make sure that the key class is appropriate for the category.
             */
            if (!category.isAssignableFrom(key.getValueClass())) {
                throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_KEY_$1, key));
            }
            if (hints!=null && !hints.isEmpty()) {
                final Object hint = hints.get(key);
                if (hint != null) {
                    if (category.isInstance(hint)) {
                        /*
                         * The factory implementation was given explicitly by the user.
                         * Nothing to do; we are done.
                         */
                        return hint;
                    }
                    /*
                     * Before to pass the hints to the private 'getServiceImplementation' method,
                     * remove the hint for the user-supplied key.  This is because this hint has
                     * been processed by this public 'getServiceProvider' method, and the policy
                     * is to remove the processed hints before to pass them to child dependencies
                     * (see the "Check recursively in factory dependencies" comment elswhere in
                     * this class).
                     *
                     * Use case: DefaultDataSourceTest invokes indirectly 'getServiceProvider'
                     * with a "CRS_AUTHORITY_FACTORY = DefaultFactory.class" hint. However
                     * DefaultFactory (in the org.geotools.referencing.factory.epsg package)
                     * is a wrapper around FactoryUsingSQL, and defines this dependency through
                     * a "CRS_AUTHORITY_FACTORY = FactoryUsingSQL.class" hint. There is no way
                     * to match this hint for both factories in same time. Since we must choose
                     * one, we assume that the user is interrested in the most top level one and
                     * discart this particular hint for the dependencies.
                     */
                    hints = new Hints(hints);
                    if (hints.remove(key) != hint) {
                        // Should never happen except on concurrent modification in an other thread.
                        throw new AssertionError(key);
                    }
                    /*
                     * If the user accepts many implementation classes, then try all of them in
                     * the preference order given by the user. The last class (or the singleton
                     * if the hint was not an array) will be tried using the "normal" path
                     * (oustide the loop) in order to get the error message in case of failure.
                     */
                    if (hint instanceof Class[]) {
                        final Class[] types = (Class[]) hint;
                        final int length = types.length;
                        for (int i=0; i<length-1; i++) {
                            final Object candidate =
                                    getServiceImplementation(category, types[i], filter, hints);
                            if (candidate != null) {
                                return candidate;
                            }
                        }
                        if (length != 0) {
                            implementation = types[length-1]; // Last try to be done below.
                        }
                    } else {
                        implementation = (Class) hint;
                    }
                }
            }
        }
        final Object candidate = getServiceImplementation(category, implementation, filter, hints);
        if (candidate != null) {
            return candidate;
        }
        throw new FactoryNotFoundException(Errors.format(ErrorKeys.FACTORY_NOT_FOUND_$1,
                  Utilities.getShortName(implementation!=null ? implementation : category)));
    }

    /**
     * Search the first implementation in the registery matching the specified conditions.
     * This method is invoked only by the {@link #getServiceProvider(Class, Filter, Hints,
     * Hints.Key)} public method above; there is no recursivity there. This method do not
     * creates new instance if no matching factory is found.
     *
     * @param  category       The category to look for. Usually an interface class.
     * @param  implementation The desired class for the implementation, or {@code null} if none.
     * @param  filter         An optional filter, or {@code null} if none.
     * @param  hints          A {@linkplain Hints map of hints}, or {@code null} if none.
     * @return A factory for the specified category and hints, or {@code null} if none.
     */
    private Object getServiceImplementation(final Class category, final Class implementation,
                                            final Filter filter,  final Hints hints)
    {
        for (final Iterator/*<Object>*/ it=getUnfilteredProviders(category); it.hasNext();) {
            final Object candidate = it.next();
            // Implementation class must be tested before 'isAcceptable'
            // in order to avoid StackOverflowError in some situations.
            if (implementation!=null && !implementation.isInstance(candidate)) {
                continue;
            }
            if (!isAcceptable(candidate, category, hints, filter)) {
                continue;
            }
            return candidate;
        }
        final List/*<Reference>*/ cached = getCachedProviders(category);
        if (cached != null) {
            /*
             * Checks if a factory previously created by FactoryCreator could fit. This
             * block should never be executed if this instance is not a FactoryCreator.
             */
            for (final Iterator/*<Reference>*/ it=cached.iterator(); it.hasNext();) {
                final Object candidate = ((Reference) it.next()).get();
                if (candidate == null) {
                    it.remove();
                    continue;
                }
                if (implementation!=null && !implementation.isInstance(candidate)) {
                    continue;
                }
                if (!isAcceptable(candidate, category, hints, filter)) {
                    continue;
                }
                return candidate;
            }
        }
        return null;
    }

    /**
     * Returns the providers available in the cache, or {@code null} if none.
     * To be overridden by {@link FactoryCreator} only.
     */
    List/*<Reference>*/ getCachedProviders(final Class category) {
        return null;
    }

    /**
     * Returns {@code true} is the specified {@code factory} meets the requirements specified by
     * a map of {@code hints} and the filter. This method is the entry point for the following
     * public methods:
     * <ul>
     *   <li>Singleton {@link #getServiceProvider (Class category, Filter, Hints, Hints.Key)}</li>
     *   <li>Iterator  {@link #getServiceProviders(Class category, Filter, Hints)}</li>
     * </ul>
     *
     * @param candidate The factory to checks.
     * @param category  The factory category. Usually an interface.
     * @param hints     The optional user requirements, or {@code null}.
     * @param filter    The optional filter, or {@code null}.
     * @return {@code true} if the {@code factory} meets the user requirements.
     */
    final boolean isAcceptable(final Object candidate,
                               final Class  category,
                               final Hints  hints,
                               final Filter filter)
    {
        if (filter!=null && !filter.filter(candidate)) {
            return false;
        }
        /*
         * Note: isAvailable(...) must be tested before checking the hints, because in current
         * Geotools implementation (especially DeferredAuthorityFactory), some hints computation
         * are deferred until a connection to the database is etablished (which 'isAvailable'
         * does in order to test the connection).
         */
        if (!isAvailable(candidate)) {
            return false;
        }
        if (hints != null) {
            if (candidate instanceof Factory) {
                if (!usesAcceptableHints((Factory) candidate, category, hints, (Set) null)) {
                    return false;
                }
            }
        }
        /*
         * Checks for optional user conditions supplied in FactoryRegistry subclasses.
         */
        return isAcceptable(candidate, category, hints);
    }

    /**
     * Returns {@code true} is the specified {@code factory} meets the requirements specified
     * by a map of {@code hints}. This method checks only the hints; it doesn't check the
     * {@link Filter}, the {@linkplain OptionalFactory#isAvailable availability} or the
     * user-overrideable {@link #isAcceptable(Object, Class, Hints)} method. This method
     * invokes itself recursively.
     *
     * @param factory     The factory to checks.
     * @param category    The factory category. Usually an interface.
     * @param hints       The user requirements ({@code null} not allowed).
     * @param alreadyDone Should be {@code null} except on recursive calls (for internal use only).
     * @return {@code true} if the {@code factory} meets the hints requirements.
     */
    private boolean usesAcceptableHints(final Factory factory,
                                        final Class   category,
                                        final Hints   hints,
                                        Set/*<Factory>*/ alreadyDone)
    {
        Hints remaining = null;
        final Map implementationHints = factory.getImplementationHints();
        for (final Iterator it=implementationHints.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final Object    key   = entry.getKey();
            final Object    value = entry.getValue();
            final Object expected = hints.get(key);
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
             * Check recursively in factory dependencies, if any. Note that the dependencies
             * will be checked against a subset of user's hints. More specifically, all hints
             * processed by the current pass will NOT be passed to the factories dependencies.
             * This is because the same hint may appears in the "parent" factory and a "child"
             * dependency with different value. For example the FORCE_LONGITUDE_FIRST_AXIS_ORDER
             * hint has the value TRUE in OrderedAxisAuthorityFactory, but the later is basically
             * a wrapper around the EPSG DefaultFactory (typically), which has the value FALSE
             * for the same hint.
             *
             * Additional note: The 'alreadyDone' set is a safety against cyclic dependencies,
             * in order to protect ourself against never-ending loops.
             */
            if (value instanceof Factory) {
                final Factory dependency = (Factory) value;
                if (alreadyDone == null) {
                    alreadyDone = new HashSet();
                }
                if (!alreadyDone.contains(dependency)) {
                    alreadyDone.add(factory);
                    if (remaining == null) {
                        remaining = new Hints(hints);
                        remaining.keySet().removeAll(implementationHints.keySet());
                    }
                    final Class type;
                    if (key instanceof Hints.Key) {
                        type = ((Hints.Key) key).getValueClass();
                    } else {
                        type = Factory.class; // Kind of unknown factory type...
                    }
                    // Recursive call to this method for scanning dependencies.
                    if (!usesAcceptableHints(dependency, type, remaining, alreadyDone)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the specified {@code provider} meets the requirements specified by
     * a map of {@code hints}. The default implementation always returns {@code true}. There is no
     * need to override this method for {@link AbstractFactory} implementations, since their hints
     * are automatically checked. Override this method for non-Geotools implementations.
     * For example a JTS geometry factory finder may overrides this method in order to check
     * if a {@link com.vividsolutions.jts.geom.GeometryFactory} uses the required
     * {@link com.vividsolutions.jts.geom.CoordinateSequenceFactory}. Such method should be
     * implemented as below, since this method may be invoked for various kind of objects:
     *
     * <blockquote><pre>
     * if (provider instanceof GeometryFactory) {
     *     // ... Check the GeometryFactory state here.
     * }
     * </pre></blockquote>
     *
     * @param provider The provider to checks.
     * @param category The factory category. Usually an interface.
     * @param hints    The user requirements, or {@code null} if none.
     * @return {@code true} if the {@code provider} meets the user requirements.
     */
    protected boolean isAcceptable(final Object provider, final Class category, final Hints hints) {
        return true;
    }

    /**
     * Returns {@code true} if the specified factory is available.
     */
    private boolean isAvailable(final Object provider) {
        if (!(provider instanceof OptionalFactory)) {
            return true;
        }
        final OptionalFactory factory = (OptionalFactory) provider;
        final Class type = factory.getClass();
        if (!testingFactories.add(type)) {
            throw new RecursiveSearchException(type);
        }
        try {
            return factory.isAvailable();
        } finally {
            if (!testingFactories.remove(type)) {
                throw new AssertionError(type);
            }
        }
    }

    /**
     * Returns all class loaders to be used for scanning plugins. Current implementation
     * returns the following class loaders:
     * <p>
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
        loaders.remove(null);
        /*
         * We now have a set of class loaders with duplicated object already removed
         * (e.g. system classloader == context classloader). However, we may still
         * have an other form of redundancie. A class loader may be the parent of an
         * other one. Try to remove those dependencies.
         */
        final ClassLoader[] asArray = (ClassLoader[]) loaders.toArray(new ClassLoader[loaders.size()]);
        final int length = asArray.length;
        for (int i=0; i<length; i++) {
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
            scanForPlugins(loaders, category);
        }
    }

    /**
     * Scans for factory plug-ins of the given category, with guard against recursivities.
     * The recursivity check make debugging easier than inspecting a {@link StackOverflowError}.
     *
     * @param loader The class loader to use.
     * @param category The category to scan for plug-ins.
     */
    private void scanForPlugins(final Collection/*<ClassLoader>*/ loaders, final Class category) {
        if (!scanningCategories.add(category)) {
            throw new RecursiveSearchException(category);
        }
        try {
            final StringBuffer message = getLogHeader(category);
            boolean newServices = false;
            /*
             * First, query the user-provider iterators, if any.
             */
            for (final Iterator ip=iteratorProviders.iterator(); ip.hasNext();) {
                final Iterator it = ((FactoryIteratorProvider) ip.next()).iterator(category);
                if (it != null) {
                    newServices |= register(it, category, message);
                }
            }
            /*
             * Next, scan META-INF/services directories (the default mechanism).
             */
            for (final Iterator it=loaders.iterator(); it.hasNext();) {
                final ClassLoader loader = (ClassLoader) it.next();
                newServices |= register(lookupProviders(category, loader), category, message);
                newServices |= registerFromSystemProperty(loader, category, message);
            }
            /*
             * Finally, log the list of registered factories.
             */
            if (newServices) {
                log("scanForPlugins", message);
            }
        } finally {
            if (!scanningCategories.remove(category)) {
                throw new AssertionError(category);
            }
        }
    }

    /**
     * {@linkplain #registerServiceProvider Registers} all service providers given by the
     * supplied iterator.
     *
     * @param factories The service providers to register.
     * @param category  the category under which to register the providers.
     * @param message   A buffer where to write the logging message.
     * @return {@code true} if at least one service provider has been registered.
     */
    private boolean register(final Iterator factories, final Class category,
                             final StringBuffer message)
    {
        boolean newServices = false;
        final String lineSeparator = System.getProperty("line.separator", "\n");
        while (factories.hasNext()) {
            Object factory;
            try {
                factory = factories.next();
            } catch (OutOfMemoryError error) {
                // Makes sure that we don't try to handle this error.
                throw error;
            } catch (NoClassDefFoundError error) {
                /*
                 * A provider can't be registered because of some missing dependencies.
                 * This occurs for example when trying to register the WarpTransform2D
                 * math transform on a machine without JAI installation. Since the service
                 * may not be essential (this is the case of WarpTransform2D), just skip it.
                 */
                loadingFailure(category, error, false);
                continue;
            } catch (Error error) {
                if (!Utilities.getShortClassName(error).equals("ServiceConfigurationError")) {
                    // We want to handle sun.misc.ServiceConfigurationError only. Unfortunatly, we
                    // need to rely on reflection because this error class is not a commited API.
                    throw error;
                }
                /*
                 * Failed to register a service for a reason probably related to the plugin
                 * initialisation. It may be some service-dependent missing resources.
                 */
                loadingFailure(category, error, true);
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
                // Need to register anyway, because the category may not be the same.
            }
            if (registerServiceProvider(factory, category)) {
                /*
                 * The factory is now registered. Add it to the message to be logged. We will log
                 * all factories together in a single log event because some registration (e.g.
                 * MathTransformProviders) would be otherwise quite verbose.
                 */
                message.append(lineSeparator);
                message.append("  ");
                message.append(factoryClass.getName());
                newServices = true;
            }
        }
        return newServices;
    }

    /**
     * If a system property was setup, load the class (if not already registered)
     * and move it in front of any other factory. This is done for compatibility
     * with legacy {@code FactoryFinder} implementation.
     *
     * @param loader   The class loader to use.
     * @param category The category to scan for plug-ins.
     * @param message  A buffer where to write the logging message.
     * @return {@code true} if at least one service provider has been registered.
     */
    private boolean registerFromSystemProperty(final ClassLoader loader, final Class category,
                                               final StringBuffer message)
    {
        boolean newServices = false;
        try {
            final String classname = System.getProperty(category.getName());
            if (classname != null) try {
                final Class factoryClass = loader.loadClass(classname);
                Object factory = getServiceProviderByClass(factoryClass);
                if (factory == null) try {
                    factory = factoryClass.newInstance();
                    if (registerServiceProvider(factory, category)) {
                        message.append(System.getProperty("line.separator", "\n"));
                        message.append("  ");
                        message.append(factoryClass.getName());
                        newServices = true;
                    }
                } catch (IllegalAccessException exception) {
                    throw new FactoryRegistryException(Errors.format(
                            ErrorKeys.CANT_CREATE_FACTORY_$1, classname), exception);
                } catch (InstantiationException exception) {
                    throw new FactoryRegistryException(Errors.format(
                            ErrorKeys.CANT_CREATE_FACTORY_$1, classname), exception);
                }
                /*
                 * Put this factory in front of every other factories (including the ones loaded
                 * in previous class loaders, which is why we don't inline this ordering in the
                 * 'register' loop). Note: if some factories were not yet registered, they will
                 * not be properly ordered. Since this code exists more for compatibility reasons
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
        return newServices;
    }

    /**
     * Invoked when a service can't be loaded. Log a warning, but do not stop the process.
     */
    private static void loadingFailure(final Class category, final Throwable error,
                                       final boolean showStackTrace)
    {
        final String        name = Utilities.getShortName(category);
        final StringBuffer cause = new StringBuffer(Utilities.getShortClassName(error));
        final String     message = error.getLocalizedMessage();
        if (message != null) {
            cause.append(": ");
            cause.append(message);
        }
        final LogRecord record = Logging.format(Level.WARNING,
                LoggingKeys.CANT_LOAD_SERVICE_$2, name, cause.toString());
        if (showStackTrace) {
            record.setThrown(error);
        }
        record.setSourceClassName(FactoryRegistry.class.getName());
        record.setSourceMethodName("scanForPlugins");
        LOGGER.log(record);
    }

    /**
     * Prepares a message to be logged if any provider has been registered.
     */
    private static StringBuffer getLogHeader(final Class category) {
        return new StringBuffer(Logging.getResources(null).getString(
                LoggingKeys.FACTORY_IMPLEMENTATIONS_$1, Utilities.getShortName(category)));
    }

    /**
     * Log the specified message after all provider for a given category have been registered.
     */
    private static void log(final String method, final StringBuffer message) {
        final LogRecord record = new LogRecord(Level.CONFIG, message.toString());
        record.setSourceClassName(FactoryRegistry.class.getName());
        record.setSourceMethodName(method);
        LOGGER.log(record);
    }

    /**
     * Adds an alternative way to search for factory implementations. {@code FactoryRegistry} has
     * a default mechanism bundled in it, which uses the content of all {@code META-INF/services}
     * directories found on the classpath. This {@code addFactoryIteratorProvider} method allows
     * to specify additional discovery algorithms. It may be useful in the context of some
     * frameworks that use the <cite>constructor injection</cite> pattern, like the
     * <a href="http://www.springframework.org/">Spring framework</a>.
     *
     * @since 2.4
     */
    public void addFactoryIteratorProvider(final FactoryIteratorProvider provider) {
        iteratorProviders.add(provider);
        for (final Iterator categories=getCategories(); categories.hasNext();) {
            final Class category = (Class) categories.next();
            if (getServiceProviders(category, false).hasNext()) {
                /*
                 * Register immediately the factories only if some other factories were already
                 * registered for this category,  because in such case 'scanForPlugin' will not
                 * be invoked automatically. If no factory are registered for this category, do
                 * nothing - we will rely on the lazy invocation of 'scanForPlugins' when first
                 * needed. We perform this check because getServiceProviders(category).hasNext()
                 * is the criterion used by FactoryRegistry in order to decide if it should invoke
                 * automatically scanForPlugins.
                 */
                final Iterator it = provider.iterator(category);
                if (it != null) {
                    final StringBuffer message = getLogHeader(category);
                    if (register(it, category, message)) {
                        log("addFactoryIteratorProvider", message);
                    }
                }
            }
        }
    }

    /**
     * Removes a provider that was previously {@linkplain #addFactoryIteratorProvider added}.
     * Note that factories already obtained from the specified provider will not be
     * {@linkplain #deregisterServiceProvider deregistered} by this method.
     *
     * @since 2.4
     */
    public void removeFactoryIteratorProvider(final FactoryIteratorProvider provider) {
        iteratorProviders.remove(provider);
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
     * @param service1 Filter for the preferred service.
     * @param service2 Filter for the service to which {@code service1} is preferred.
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
                for (final Iterator it=getServiceProviders(category, false); it.hasNext();) {
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
