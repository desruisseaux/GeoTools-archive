/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.factory;

// J2SE dependencies
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.imageio.spi.ServiceRegistry;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A registry for factories. Factories are registered by categories, which are identified
 * by <strong>interface</strong> class (<U>not</U> implementation class). For example
 * <code>{@link org.opengis.referencing.crs.CRSFactory}.class</code> is a category,
 * and <code>{@link org.opengis.referencing.operation.MathTransformFactory}.class</code>
 * is an other category.
 * <BR><BR>
 * For each category, implementations are registered in a file placed in the
 * <code>META-INF/services/</code> directory, as specified in the {@link ServiceRegistry}
 * javadoc. Those files are usually bundled into the JAR file distributed by the vendor.
 * If the same <code>META-INF/services/</code> file appears many time in different JARs,
 * they are processed as if their content were merged.
 * <BR><BR>
 * 
 * Example use:
 * <pre>
 * Set categories = Collections.singleton(new Class[] {MathTransformProvider.class});
 * FactoryRegistry registry = new FactoryRegistry(categories);
 * 
 * // get the providers
 * registry.getProviders(MathTransformProvider.class)
 * </pre>
 *
 * <P><strong>NOTE: This class is not thread safe</strong>. Users are responsable
 * for synchronisation. This is usually done in an utility class wrapping this
 * service registry (e.g. {@link org.geotools.referencing.FactoryFinder}).</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Richard Gould
 */
public class FactoryRegistry extends ServiceRegistry {
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
     * Returns the providers in the registry for the specified category.
     * This method will scan for plugins the first time it is invoked.
     *
     * @param category The category to look for. Must be an interface class
     *                 (not the actual implementation class).
     * @return All factories for the specified category.
     */
    public Iterator getServiceProviders(final Class category) {
        Iterator iterator = getServiceProviders(category, true);
        if (!iterator.hasNext()) {
            /*
             * No plugin. This method is probably invoked the first time for the specified
             * category, otherwise we should have found at least the Geotools implementation.
             * Scans the plugin now, but for this category only.
             */
            for (final Iterator it=getClassLoaders().iterator(); it.hasNext();) {
                scanForPlugins((ClassLoader) it.next(), category);
            }
            iterator = getServiceProviders(category, true);
        }
        return iterator;
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
     * @todo localize log messages.
     */
    private void scanForPlugins(final ClassLoader loader, final Class category) {
        final Iterator   factories = ServiceRegistry.lookupProviders(category, loader);
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringBuffer message = new StringBuffer();
        message.append("Scan for '");
        message.append(Utilities.getShortName(category));
        message.append("' implementations:");
        message.append(lineSeparator);
        boolean newServices = false;
        while (factories.hasNext()) {
            Object factory = factories.next();
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
                message.append("  Register ");
                message.append(factoryClass.getName());
                message.append(lineSeparator);
                newServices = true;
            }
        }
        if (newServices) {
            final LogRecord record = new LogRecord(Level.CONFIG, message.toString());
            record.setSourceClassName(FactoryRegistry.class.getName());
            record.setSourceMethodName("scanForPlugins");
            Logger.getLogger("org.opengis.factory").log(record);
        }
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
            Logger.getLogger("org.geotools.factory").warning("No class loaders available");
        }
        return loaders;
    }

    /**
     * Sets or unsets a pairwise ordering between all services meeting a criterion. For example
     * in the CRS framework ({@link org.geotools.referencing.FactoryFinder}), this is used for
     * setting ordering between all services provided by two vendors, or for two authorities.
     * If one or both services are not currently registered, or if the desired ordering is
     * already set/unset, nothing happens and false is returned.
     *
     * @param base     The base category. Only categories {@linkplain Class#isAssignableFrom
     *                 assignable} to <code>base</code> will be processed.
     * @param set      <code>true</code> for setting the ordering, or <code>false</code> for
     *                 unsetting.
     * @param service1 filter for the preferred service.
     * @param service2 filter for the service to which <code>service1</code> is preferred.
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
