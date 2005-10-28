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
package org.geotools.coverage;

// J2SE dependencies
import java.util.Arrays;
import java.util.Set;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.resources.LazySet;


/**
 * Defines static methods used to access the application's default
 * {@linkplain GridCoverageFactory factory} implementation.
 *
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class FactoryFinder {
    /**
     * The service registry for this manager.
     * Will be initialized only when first needed.
     */
    private static FactoryRegistry registry;

    /**
     * Do not allows any instantiation of this class.
     */
    private FactoryFinder() {
        // singleton
    }

    /**
     * Returns the service registry. The registry will be created the first
     * time this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(FactoryFinder.class);
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(new Class[] {
                    GridCoverageFactory.class}));
        }
        return registry;
    }

    /**
     * Returns the first implementation of {@link GridCoverageFactory} matching the specified hints.
     * If no implementation matches, a new one is created if possible or an exception is thrown
     * otherwise.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first grid coverage factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link GridCoverageFactory} interface.
     */
    public static synchronized GridCoverageFactory getGridCoverageFactory(final Hints hints)
            throws FactoryRegistryException
    {
        return (GridCoverageFactory) getServiceRegistry().getServiceProvider(
                GridCoverageFactory.class, null, hints, null);
    }

    /**
     * Returns a set of all available implementations for the {@link GridCoverageFactory}.
     *
     * @return Set of available grid coverage factory implementations.
     */
    public static synchronized Set getGridCoverageFactories() {
        return new LazySet(getServiceRegistry().getServiceProviders(GridCoverageFactory.class));
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
    public static void scanForPlugins() {
        if (registry != null) {
            registry.scanForPlugins();
        }
    }
}
