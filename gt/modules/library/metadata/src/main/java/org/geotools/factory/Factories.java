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
import java.util.Set;
import java.util.LinkedHashSet;


/**
 * Utility methods that apply to all {@linkplain FactoryRegistry factory registries}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Factories {
    /**
     * Alternative scanning methods used by {@link FactoryRegistry#scanForPlugins(Collection,Class)}
     * in addition of the default lookup mechanism.
     *
     * @todo Replace by a concurrent hash set when we will be allowed to compile for J2SE 1.5,
     *       and remove the synchronization in this class and the clone in
     *       {@link #getIteratorProviders}.
     */
    private static final Set/*<FactoryIteratorProvider>*/ iteratorProviders = new LinkedHashSet();

    /**
     * Do not allow instantiation of this class.
     */
    private Factories() {
    }

    /**
     * Adds an alternative way to search for factory implementations. {@link FactoryRegistry} has
     * a default mechanism bundled in it, which uses the content of all {@code META-INF/services}
     * directories found on the classpath. This {@code addFactoryIteratorProvider} method allows
     * to specify additional discovery algorithms. It may be useful in the context of some
     * frameworks that use the <cite>constructor injection</cite> pattern, like the
     * <a href="http://www.springframework.org/">Spring framework</a>.
     */
    public static void addFactoryIteratorProvider(FactoryIteratorProvider provider) {
        synchronized (iteratorProviders) {
            iteratorProviders.add(provider);
        }
    }

    /**
     * Removes a provider that was previously {@linkplain #addFactoryIteratorProvider added}.
     * Note that factories already obtained from the specified provider will not be
     * {@linkplain FactoryRegistry#deregisterServiceProvider deregistered} by this method.
     */
    public static void removeFactoryIteratorProvider(FactoryIteratorProvider provider) {
        synchronized (iteratorProviders) {
            iteratorProviders.remove(provider);
        }
    }

    /**
     * Returns the iterator providers.
     *
     * @todo Remove the synchronization and the clone when we will be allowed to use
     *       a concurrent hash set with J2SE 1.5.
     */
    static Set/*<FactoryIteratorProvider>*/ getIteratorProviders() {
        synchronized (iteratorProviders) {
            return (Set) ((LinkedHashSet) iteratorProviders).clone();
        }
    }
}
