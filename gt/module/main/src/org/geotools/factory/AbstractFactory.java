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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Collections;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;

// Geotools dependencies
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Skeletal implementation of factories. This base classe provides no {@code createFoo} methods,
 * (they must be provided by subclasses), but provides two convenience features:
 * <br><br>
 * <ul>
 *   <li>An initially empty {@linkplain #hints map of hints} to be filled by subclasses
 *       constructors.</li>
 *   <li>An automatic {@linkplain ServiceRegistry#setOrdering ordering} applied
 *       on the basis of subclasses-provided {@linkplain #priority} rank.</li>
 * </ul>
 * <br><br>
 * When more than one factory implementation is {@linkplain ServiceRegistry#registerServiceProvider
 * registered} for the same category (i.e. they implement the same {@link Factory} sub-interface),
 * the actual instance to be used is selected according their
 * {@linkplain ServiceRegistry#setOrdering ordering} and user's {@linkplain Hints hints}.
 * Hints have precedence. If more than one factory matches the hints (including the common
 * case where the user doesn't provide any hint at all), then ordering matter.
 * <br><br>
 * The ordering is unspecified for every pairs of factories with the same {@linkplain #priority}.
 * This implies that the ordering is unspecified between all factories created with the
 * {@linkplain #AbstractFactory() default constructor}, since they all have the same
 * {@linkplain #NORMAL_PRIORITY default priority} level.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AbstractFactory implements Factory, RegisterableService {
    /**
     * The minimum priority for a factory. Factories with lowest priority
     * will be used only if there is no other factory in the same
     * {@linkplain ServiceRegistry#getCategories category}.
     *
     * @see #onRegistration
     */
    public static final int MINIMUM_PRIORITY = 1;

    /**
     * The default priority.
     *
     * @see #onRegistration
     */
    public static final int NORMAL_PRIORITY = 50;

    /**
     * The maximum priority for a factory. Factories with highest
     * priority will be preferred to any other factory in the same
     * {@linkplain ServiceRegistry#getCategories category}.
     *
     * @see #onRegistration
     */
    public static final int MAXIMUM_PRIORITY = 100;

    /**
     * The priority for this factory.
     */
    public final int priority;

    /**
     * The {@linkplain Factory#getImplementationHints implementation hints}. This map should be
     * filled by subclasses at construction time. Constructors should <strong>not</strong> copy
     * blindly all user-provided hints. The should select only the relevant hints and resolve them
     * as of {@linkplain Factory#getImplementationHints implementation hints} contract.
     *
     * Once the hints are accessibles to the user, this map should not change anymore.
     */
    protected final Map/*<RenderingHints.Key,Object>*/ hints = new LinkedHashMap();

    /**
     * An unmodifiable view of {@link #hints}. This is the actual map to be returned
     * by {@link #getImplementationHints}. Its content reflects the {@link #hints}
     * map even if the later is modified.
     */
    private final Map unmodifiableHints = Collections.unmodifiableMap(hints);
    
    /**
     * Creates a new factory with the {@linkplain #NORMAL_PRIORITY default priority}.
     */
    protected AbstractFactory() {
        this(NORMAL_PRIORITY);
    }

    /**
     * Constructs a factory with the specified priority.
     *
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY} and {@link #MAXIMUM_PRIORITY} inclusive.
     */
    protected AbstractFactory(final int priority) {
        this.priority = priority;
        if (priority<MINIMUM_PRIORITY || priority>MAXIMUM_PRIORITY) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "priority", new Integer(priority)));
        }
    }

    /**
     * Returns an {@linkplain java.util.Collections#unmodifiableMap unmodifiable} view of
     * {@linkplain #hints}.
     *
     * @return The map of hints, or an empty map if none.
     */
    public Map getImplementationHints() {
        return unmodifiableHints;
    }

    /**
     * Called when this factory is added to the given {@code category} of the given
     * {@code registry}. The factory may already be registered under another category
     * or categories.
     * <br><br>
     * This method is invoked automatically when this factory is registered as a plugin,
     * and should not be invoked directly by the user. The default implementation iterates
     * through all services under the same category that extends the {@code AbstractFactory}
     * class, and set the ordering according the priority given at construction time.
     *
     * @param registry A service registry where this factory has been registered.
     * @param category The registry category under which this object has been registered.
     *
     * @see #MINIMUM_PRIORITY
     * @see #MAXIMUM_PRIORITY
     */
    public void onRegistration(final ServiceRegistry registry, final Class category) {
        for (final Iterator it=registry.getServiceProviders(category, false); it.hasNext();) {
            final Object provider = it.next();
            if (provider instanceof AbstractFactory) {
                final AbstractFactory factory = (AbstractFactory) provider;
                if (priority > factory.priority) {
                    registry.setOrdering(category, this, factory);
                } else if (priority < factory.priority) {
                    registry.setOrdering(category, factory, this);
                }
            }
        }
    }

    /**
     * Called when this factory is removed from the given {@code category} of the given
     * {@code registry}. The object may still be registered under another category or categories.
     * <br><br>
     * This method is invoked automatically when this factory is no longer registered as a plugin,
     * and should not be invoked directly by the user.
     *
     * @param registry A service registry from which this object is being (wholly or partially)
     *                 deregistered.
     * @param category The registry category from which this object is being deregistered.
     */
    public void onDeregistration(final ServiceRegistry registry, final Class category) {
        // No action needed.
    }
}
