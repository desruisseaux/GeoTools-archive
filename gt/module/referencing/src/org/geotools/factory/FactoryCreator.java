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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import javax.imageio.spi.ServiceRegistry; // For javadoc

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A {@linkplain FactoryRegistry factory registry} capable to creates factories if no appropriate
 * instance was found in the registry.
 * <p>
 * This class maintains a cache of previously created factories, as {@linkplain WeakReference
 * weak references}. Calls to {@link #getServiceProvider getServiceProvider} first check if a
 * previously created factory can fit.
 *
 * @source $URL$
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
     * List of factories already created. Used as a cache.
     */
    private final Map/*<Class, List<Reference>>*/ cache = new HashMap();

    /**
     * Objects under construction for each implementation class.
     * Used as a guard against infinite recursivity.
     */
    private final Set/*<Class>*/ underConstruction = new HashSet();

    /**
     * Constructs a new registry for the specified categories.
     *
     * @param categories The categories.
     */
    public FactoryCreator(final Collection categories) {
        super(categories);
    }

    /**
     * Returns the providers available in the cache. To be used by {@link FactoryRegistry}.
     */
    final List/*<Reference>*/ getCachedProviders(final Class category) {
        List c = (List) cache.get(category);
        if (c == null) {
            c = new LinkedList();
            cache.put(category, c);
        }
        return c;
    }

    /**
     * Caches the specified factory under the specified category.
     */
    private void cache(final Class category, final Object factory) {
        getCachedProviders(category).add(new WeakReference(factory));
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
     * @param  key      The key to use for looking for a user-provided instance in the hints, or
     *                  {@code null} if none.
     * @return A factory for the specified category and hints (never {@code null}).
     * @throws FactoryNotFoundException if no factory was found, and the specified hints don't
     *         provide suffisient information for creating a new factory.
     * @throws FactoryRegistryException if the factory can't be created for some other reason.
     */
    public Object getServiceProvider(final Class     category,
                                     final Filter    filter,
                                     final Hints     hints,
                                     final Hints.Key key)
            throws FactoryRegistryException
    {
        final FactoryNotFoundException notFound;
        try {
            return super.getServiceProvider(category, filter, hints, key);
        } catch (FactoryNotFoundException exception) {
            // Will be rethrown later in case of failure to create the factory.
            notFound = exception;
        }
        /*
         * No existing factory found. Creates one using reflection. First, we
         * check if an implementation class was explicitly specified by the user.
         */
        if (hints!=null && key!=null) {
            final Object hint = hints.get(key);
            if (hint != null) {
                final Class[] types;
                if (hint instanceof Class[]) {
                    types = (Class[]) hint;
                } else {
                    types = new Class[] {(Class) hint};
                    // Should not fails, since non-class argument should
                    // have been accepted by 'getServiceProvider(...)'.
                }
                final int length=types.length;
                Object candidate ;
                Class type;
                int modifiers;
                for (int i=0; i<length; i++) {
                    type = types[i];
                    if (type!=null && category.isAssignableFrom(type)) {
                        modifiers = type.getModifiers();
                        if (!Modifier.isAbstract(modifiers)) {
                            candidate = createSafe(category, type, hints);
                            if (isAcceptable(candidate, category, hints, filter)) {
                                cache(category, candidate);
                                return candidate;
                            }
                            dispose(candidate);
                        }
                    }
                }
            }
        }
        /*
         * No implementation hint provided. Search the first implementation
         * accepting a Hints argument. No-args constructor will be ignored.
         * Note: all Factory objects should be fully constructed by now,
         * since the super-class has already iterated over all factories.
         */
        for (final Iterator it=getServiceProviders(category); it.hasNext();) {
            final Object factory = it.next();
            if (filter!=null && !filter.filter(factory)) {
                continue;
            }
            final Class implementation = factory.getClass();
            final Object candidate;
            try {
                candidate = createSafe(category, implementation, hints);
            } catch (FactoryRegistryException exception) {
                if (exception.getCause() instanceof NoSuchMethodException) {
                    // No public constructor with the expected argument.
                    // Try an other implementation.
                    continue;
                } else {
                    // Other kind of error, probably unexpected.
                    // Let the exception propagates.
                    throw exception;
                }
            }
            if (FILTER.filter(candidate) && isAcceptable(candidate, category, hints, filter)) {
                cache(category, candidate);
                return candidate;
            }
            dispose(candidate);
        }
        throw notFound;
    }

    /**
     * Invokes {@link #createServiceProvider}, but checks against recursive calls.
     * It make debugging easier than inspecting a {@link StackOverflowError}.
     */
    private Object createSafe(final Class category,
                              final Class implementation,
                              final Hints hints)
    {
        if (!underConstruction.add(implementation)) {
            throw new FactoryRegistryException(Errors.format(ErrorKeys.RECURSIVE_CALL_$2,
                      Utilities.getShortName(implementation), Utilities.getShortName(category)));
        }
        try {
            return createServiceProvider(category, implementation, hints);
        } finally {
            if (!underConstruction.remove(implementation)) {
                throw new AssertionError();
            }
        }
    }

    /**
     * Creates a new instance of the specified factory using the specified hints.
     * The default implementation tries to instantiate the given implementation class
     * using the first of the following constructor found:
     * <p>
     * <ul>
     *   <li>Constructor with a single {@link Hints} argument.</li>
     *   <li>No-argument constructor.</li>
     * </ul>
     *
     * @param  category The category to instantiate.
     * @param  implementation The factory class to instantiate.
     * @param  hints The implementation hints.
     * @return The factory.
     * @throws FactoryRegistryException if the factory creation failed.
     */
    protected Object createServiceProvider(final Class category,
                                           final Class implementation,
                                           final Hints hints)
            throws FactoryRegistryException
    {
        Throwable cause;
        try {
            try {
                return implementation.getConstructor(HINTS_ARGUMENT).newInstance(new Object[]{hints});
            } catch (NoSuchMethodException exception) {
                // Constructor do not exists or is not public. We will fallback on the no-arg one.
                cause = exception;
            }
            try {
                return implementation.getConstructor((Class[])null).newInstance((Object[])null);
            } catch (NoSuchMethodException exception) {
                // No constructor accessible. Do not store the cause (we keep the one above).
            }
        } catch (IllegalAccessException exception) {
            cause = exception; // constructor is not public (should not happen)
        } catch (InstantiationException exception) {
            cause = exception; // The class is abstract
        } catch (InvocationTargetException exception) {
            cause = exception.getCause(); // Exception in constructor
            if (cause instanceof FactoryRegistryException) {
                throw (FactoryRegistryException) cause;
            }
        }
        throw new FactoryRegistryException(Errors.format(ErrorKeys.CANT_CREATE_FACTORY_$1,
                                           Utilities.getShortName(implementation)), cause);
    }

    /**
     * Dispose the specified factory after. This method is invoked when a factory has been
     * created, and then {@code FactoryCreator} determined that the factory doesn't meet
     * user's requirements.
     */
    private static void dispose(final Object factory) {
        // Empty for now. This method is merely a reminder for disposal in future Geotools versions.
    }
}
