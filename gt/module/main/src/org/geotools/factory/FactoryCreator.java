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
import javax.imageio.spi.ServiceRegistry; // For javadoc
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A {@linkplain FactoryRegistry factory registry} capable to creates factories if no appropriate
 * instance was found in the registry. Factory created "on the fly" are not cached; all invocation
 * to {@link #getServiceProvider getServiceProvider(...)} will creates them again if no registered
 * factory matches the requirements ({@linkplain javax.imageio.spi.ServiceRegistry.Filter filter}
 * and/or {@linkplain Hints hints}). If caching is wanted, the instances to cache should be declared
 * likes all other services in the {@code META-INF/services/} directory. For the caching to be
 * effective, their no-argument constructor shall setup the factory with
 * {@linkplain Factory#getImplementationHints implementation hints} matching the hints that the
 * application is expected to ask for. It is preferable that such custom implementation
 * {@linkplain ServiceRegistry#setOrdering order} itself after the default implementations.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 */
public class FactoryCreator extends FactoryRegistry {
    /**
     * The array of classes for searching the one-argument constructor.
     */
    private static final Class[] HINTS_ARGUMENT = new Class[] {Hints.class};

    /**
     * Constructs a new registry for the specified categories.
     *
     * @param categories The categories.
     */
    public FactoryCreator(final Collection categories) {
        super(categories);
    }

    /**
     * Returns a provider for the specified category, using the specified map of hints (if any).
     * If a provider matching the requirements is found in the registry, it is returned. Otherwise,
     * a new provider is created and returned. This creation step is the only difference between
     * this method and the {@linkplain FactoryRegistry#getServiceProvider super-class method}.
     *
     * @param  category The category to look for.
     * @param  filter   An optional filter, or {@code null} if none.
     * @param  hints    A {@linkplain Hints map of hints}, or {@code null} if none.
     * @return A factory for the specified category and hints (never {@code null}).
     * @throws FactoryNotFoundException if no factory was found, and the specified hints don't
     *         provide suffisient information for creating a new factory.
     * @throws FactoryRegistryException if the factory can't be created for some other reason.
     */
    public Object getServiceProvider(final Class category, final Filter filter, final Hints hints)
            throws FactoryRegistryException
    {
        final FactoryNotFoundException notFound;
        try {
            return super.getServiceProvider(category, filter, hints);
        } catch (FactoryNotFoundException exception) {
            // Will be rethrown later in case of failure to create the factory.
            notFound = exception;
        }
        /*
         * No existing factory found. Creates one using reflection.
         */
        if (hints != null) {
            final Hints.Key key = Hints.Key.getKeyForCategory(category);
            if (key != null) {
                final Object hint = hints.get(key);
                if (hint != null) {
                    final Class type;
                    if (hint instanceof Class[]) {
                        type = ((Class[]) hint)[0];
                        // Should not fails, since Hints.isCompatibleValue(Object)
                        // do not allows empty array.
                    } else {
                        type = (Class) hint;
                        // Should not fails, since non-class argument should
                        // have been accepted by 'getServiceProvider(...)'.
                    }
                    if (type!=null && category.isAssignableFrom(type)) {
                        return createServiceProvider(type, hints);
                    }
                }
            }
        }
        /*
         * No implementation hint provided. Search the first implementation
         * accepting a Hints argument. No-args constructor will be ignored.
         */
        for (final Iterator it=getServiceProviders(category); it.hasNext();) {
            final Class implementation = it.next().getClass();
            try {
                implementation.getConstructor(HINTS_ARGUMENT);
            } catch (NoSuchMethodException exception) {
                // No public constructor with the expected argument.
                continue;
            }
            return createServiceProvider(implementation, hints);
        }
        throw notFound;
    }

    /**
     * Creates a new instance of the specified factory using the specified hints.
     * The default implementation try to instantiate the given implementation class
     * the first of the following constructor found:
     * <ul>
     *   <li>Constructor with a single {@link Hints} argument.</li>
     *   <li>No-argument constructor.</li>
     * </ul>
     *
     * @param  implementation The factory class to instantiate.
     * @param  hints The implementation hints.
     * @return The factory.
     * @throws FactoryRegistryException if the factory creation failed.
     */
    protected Object createServiceProvider(final Class implementation, final Hints hints)
            throws FactoryRegistryException
    {
        Throwable cause;
        try {
            try {
                return implementation.getConstructor(HINTS_ARGUMENT).newInstance(new Object[]{hints});
            } catch (NoSuchMethodException exception) {
                // Constructor do not exists or is not public. We will fallback on the no-arg one.
            }
            try {
                return implementation.getConstructor((Class[])null).newInstance((Object[])null);
            } catch (NoSuchMethodException exception) {
                cause = exception; // No constructor accessible
            }
        } catch (IllegalAccessException exception) {
            cause = exception; // constructor is not public (should not happen)
        } catch (InstantiationException exception) {
            cause = exception; // The class is abstract
        } catch (InvocationTargetException exception) {
            cause = exception.getCause(); // Exception in constructor
        }
        // TODO: localize
        throw new FactoryRegistryException("Can't creates \"" +
                Utilities.getShortName(implementation) + "\" factory.", cause);
    }
}
