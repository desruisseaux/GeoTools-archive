/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.util;

// J2SE dependencies
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.imageio.spi.ServiceRegistry;

// Geotools dependencies
import org.geotools.resources.Utilities;



/**
 * A utility class that uses a pre-configured {@link ServiceRegistry} to search for
 * providers and catagories.
 * 
 * Example use:
 * <pre>
 * ServiceRegistry registry = new ServiceRegistry((Iterator) Arrays.asList(new Class[] {MathTransformProvider.class}).iterator());
 * 
 * // get the providers
 * ClassFinder.getProviders(registry, MathTransformProvider.class)
 * </pre>
 *
 * @author rgould
 *
 * @deprecated Use {@link org.geotools.factory.FactoryRegistry} instead.
 */
public class ClassFinder {
    /**
     * Scans for factory plug-ins on the application class path. This method is
     * needed because the application class path can theoretically change, or
     * additional plug-ins may become available. Rather than re-scanning the
     * classpath on every invocation of the API, the class path is scanned
     * automatically only on the first invocation. Clients can call this
     * method to prompt a re-scan. Thus this method need only be invoked by
     * sophisticated applications which dynamically make new plug-ins
     * available at runtime.
     *
     * @param registry DOCUMENT ME!
     */
    private static void scanForPlugins(ServiceRegistry registry) {
        /*
         * Note: if the registry was not yet initialized, then there is no need to scan for
         * plug-ins now, since they will be scanned the first time a service provider will
         * be required.
         */
        if (registry != null) {
            for (final Iterator categories = registry.getCategories();
                    categories.hasNext();) {
                scanForPlugins(registry, (Class) categories.next());
            }
        }
    }

    private static void scanForPlugins(ServiceRegistry registry, Class class1) {
        ClassLoader[] loaders = org.geotools.factory.FactoryFinder
            .findClassLoaders();

        for (int i = 0; i < loaders.length; i++) {
            scanForPlugins(registry, loaders[i], class1);
        }
    }

    /**
     * Scans for factory plug-ins of the given category.
     *
     * @param registry DOCUMENT ME!
     * @param loader The class loader to use.
     * @param category The category to scan for plug-ins.
     *
     * @todo localize log messages and group them together.
     */
    private static void scanForPlugins(ServiceRegistry registry,
        final ClassLoader loader, final Class category) {
        //System.out.append("...scanning "+loader );
        final Logger logger = Logger.getLogger("org.opengis");
        final Iterator iter = ServiceRegistry.lookupProviders(category, loader);
        final String classname = Utilities.getShortName(category);

        while (iter.hasNext()) {
            Object factory = iter.next();

            //System.out.println(" ...found "+factory.getClass().toString() );
            if (true) {
                /*
                 * If the factory implements more than one interface and an instance were
                 * already registered, reuse the same instance instead of duplicating it.
                 */
                Object replacement = registry.getServiceProviderByClass(factory
                        .getClass());

                if (replacement != null) {
                    factory = replacement;
                }
            }

            final String operation = registry.registerServiceProvider(factory,
                    category) ? "Register " : "Replace  ";
            final LogRecord log = new LogRecord(Level.CONFIG,
                    operation + factory.getClass().getName() + " as "
                    + classname);
            log.setSourceClassName("org.opengis.go.FactoryFinder");
            log.setSourceMethodName("scanForPlugins");
            logger.log(log);
        }
    }

    /**
     * Returns the providers in the registry for the specified category. This method will
     * scan for plugins the first time it is invoked.
     *
     * @param registry DOCUMENT ME!
     * @param category DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @todo revisit
     */
    public static Iterator getProviders(ServiceRegistry registry,
        final Class category) {
        assert Thread.holdsLock(ClassFinder.class);

        Iterator iterator = registry.getServiceProviders(category, false);

        if (!iterator.hasNext()) {
            /*
             * No plugin. This method is probably invoked the first time for the specified
             * category, otherwise we should have found at least the Geotools implementation.
             * Scans the plugin now, but for this category only.
             */

            //System.out.println("No "+category.toString()+ " found ... scanning for plugins");
            scanForPlugins(registry, category);
            iterator = registry.getServiceProviders(category, false);
        }

        return iterator;
    }
}
