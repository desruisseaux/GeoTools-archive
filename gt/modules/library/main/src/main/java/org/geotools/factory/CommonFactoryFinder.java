/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
import java.util.Arrays;
import java.util.Set;

// Geotools dependencies
import org.geotools.styling.StyleFactory;
import org.geotools.resources.LazySet;

// OpenGIS dependencies
import org.opengis.filter.FilterFactory;


/**
 * Defines static methods used to access the application's default implementation for some
 * common factories. Those "common" factories comprise the {@linkplain StyleFactory style}
 * and {@linkplain FilterFactory filter} factories. Note that some specialized factories
 * finder like {@linkplain org.geotools.referencing.FactoryFinder referencing} and
 * {@linkplain org.geotools.coverage.FactoryFinder coverage} are defined in specialized
 * classes.
 * <p>
 * <b>Tip:</b> The {@link BasicFactories} classes provides an other way to access the various
 * factories from a central point.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CommonFactoryFinder {
    /**
     * The service registry for this manager.
     * Will be initialized only when first needed.
     */
    private static FactoryRegistry registry;

    /**
     * Do not allows any instantiation of this class.
     */
    private CommonFactoryFinder() {
        // singleton
    }

    /**
     * Returns the service registry. The registry will be created the first
     * time this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(CommonFactoryFinder.class);
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(new Class[] {
                    StyleFactory.class,
                    FilterFactory.class}));
        }
        return registry;
    }

    /**
     * Returns the first implementation of {@link StyleFactory} matching the specified hints.
     * If no implementation matches, a new one is created if possible or an exception is thrown
     * otherwise.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first style factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link StyleFactory} interface.
     *
     * @see Hints#STYLE_FACTORY
     */
    public static synchronized StyleFactory getStyleFactory(final Hints hints)
            throws FactoryRegistryException
    {
        return (StyleFactory) getServiceRegistry().getServiceProvider(
                StyleFactory.class, null, hints, Hints.STYLE_FACTORY);
    }

    /**
     * Returns a set of all available implementations for the {@link StyleFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available style factory implementations.
     */
    public static synchronized Set getStyleFactories(final Hints hints) {
        return new LazySet(getServiceRegistry().getServiceProviders(
                StyleFactory.class, null, hints));
    }

    /**
     * Returns the first implementation of {@link FilterFactory} matching the specified hints.
     * If no implementation matches, a new one is created if possible or an exception is thrown
     * otherwise.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first filter factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link FilterFactory} interface.
     *
     * @see Hints#FILTER_FACTORY
     */
    public static synchronized FilterFactory getFilterFactory(final Hints hints)
            throws FactoryRegistryException
    {
        return (FilterFactory) getServiceRegistry().getServiceProvider(
                FilterFactory.class, null, hints, Hints.FILTER_FACTORY);
    }

    /**
     * Returns a set of all available implementations for the {@link FilterFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available filter factory implementations.
     */
    public static synchronized Set getFilterFactories(final Hints hints) {
        return new LazySet(getServiceRegistry().getServiceProviders(
                FilterFactory.class, null, hints));
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
    public static synchronized void scanForPlugins() {
        if (registry != null) {
            registry.scanForPlugins();
        }
    }

    /**
     * Adds an alternative way to search for factory implementations. {@link FactoryRegistry} has
     * a default mechanism bundled in it, which uses the content of all {@code META-INF/services}
     * directories found on the classpath. This {@code addFactoryIteratorProvider} method allows
     * to specify additional discovery algorithms. It may be useful in the context of some
     * frameworks that use the <cite>constructor injection</cite> pattern, like the
     * <a href="http://www.springframework.org/">Spring framework</a>.
     */
    public static synchronized void addFactoryIteratorProvider(FactoryIteratorProvider provider) {
        getServiceRegistry().addFactoryIteratorProvider(provider);
    }

    /**
     * Removes a provider that was previously {@linkplain #addFactoryIteratorProvider added}.
     * Note that factories already obtained from the specified provider will not be
     * {@linkplain FactoryRegistry#deregisterServiceProvider deregistered} by this method.
     */
    public static synchronized void removeFactoryIteratorProvider(FactoryIteratorProvider provider) {
        getServiceRegistry().removeFactoryIteratorProvider(provider);
    }
}
