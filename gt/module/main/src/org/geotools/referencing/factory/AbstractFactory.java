/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Iterator;
import java.util.logging.Logger;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.Factory;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.AuthorityFactory;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;  // For javadoc
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * Base class for all factories. Factories can be grouped in two categories:
 * <BR>
 * <UL>
 *   <LI>{@linkplain AuthorityFactory Authority factories} creates objects from
 *       a compact string defined by an authority.</LI>
 *   <LI>{@linkplain ObjectFactory Object factories} allows applications
 *       to make objects that cannot be created by an authority factory.
 *       This factory is very flexible, whereas the authority factory is
 *       easier to use.</LI>
 * </UL>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AbstractFactory implements Factory, org.geotools.factory.Factory, RegisterableService {
    /**
     * The logger for event related to Geotools's factories.
     */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.referencing.factory");

    /**
     * The minimum priority that a factory can have. Factories with lowest priority will be used
     * only if there is no other factory in the same {@linkplain ServiceRegistry#getCategories
     * category} for the same {@linkplain AuthorityFactory#getAuthority authority} and from the
     * same {@linkplain Factory#getVendor vendor}.
     *
     * @see #onRegistration
     */
    public static final int MIN_PRIORITY = 1;

    /**
     * The default priority.
     */
    public static final int NORM_PRIORITY = 5;

    /**
     * The maximum priority that a factory can have. Factories with highest priority will be
     * preferred to any other factory in the same {@linkplain ServiceRegistry#getCategories
     * category} for the same {@linkplain AuthorityFactory#getAuthority authority} and from
     * the same {@linkplain Factory#getVendor vendor}.
     *
     * @see #onRegistration
     */
    public static final int MAX_PRIORITY = 10;

    /**
     * The priority for this factory.
     */
    final int priority;

    /**
     * Constructs a factory with the default priority.
     */
    protected AbstractFactory() {
        this.priority = NORM_PRIORITY;
    }

    /**
     * Constructs a factory with the specified priority.
     *
     * @param priority The priority for this factory, as a number between
     *        {@link #MIN_PRIORITY} and {@link #MAX_PRIORITY} inclusive.
     */
    protected AbstractFactory(final int priority) {
        this.priority = priority;
    }

    /**
     * Returns the vendor responsible for creating this factory implementation. Many implementations
     * may be available for the same factory interface. The default implementation returns
     * {@linkplain org.geotools.metadata.citation.Citation#GEOTOOLS Geotools}.
     *
     * @return The vendor for this factory implementation.
     */
    public Citation getVendor() {
        return org.geotools.metadata.citation.Citation.GEOTOOLS;
    }

    /**
     * Makes sure that an argument is non-null. This is a
     * convenience method for subclass methods.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidParameterValueException if <code>object</code> is null.
     */
    protected static void ensureNonNull(final String name, final Object object)
        throws IllegalArgumentException
    {
        if (object == null) {
            throw new InvalidParameterValueException(Resources.format(
                        ResourceKeys.ERROR_NULL_ARGUMENT_$1, name), name, object);
        }
    }

    /**
     * Returns {@code true} if this factory is for the same authority than the specified
     * object. This method will be overriden by {@link AbstractAuthorityFactory}.
     */
    boolean sameAuthority(final Factory factory) {
        return !(factory instanceof AuthorityFactory);
    }

    /**
     * Called when this factory is added to the given <code>category</code> of the given
     * <code>registry</code>. The factory may already be registered under another category
     * or categories.
     * <br><br>
     * This method is invoked automatically when this factory is registered as a plugin,
     * and should not be invoked directly by the user. The default implementation iterates
     * through all services under the same category, for the same
     * {@linkplain AuthorityFactory#getAuthority authority} and from the same
     * {@linkplain Factory#getVendor vendor}, and set the ordering
     * according the priority given at construction time.
     *
     * @param registry a <code>ServiceRegistry</code> where this factory has been registered.
     * @param category a <code>Class</code> object indicating the registry category under which
     *                 this object has been registered.
     *
     * @see #MIN_PRIORITY
     * @see #MAX_PRIORITY
     * @see FactoryFinder
     */
    public void onRegistration(final ServiceRegistry registry, final Class category) {
        for (final Iterator it=registry.getServiceProviders(category, false); it.hasNext();) {
            final Object provider = it.next();
            if (provider instanceof AbstractFactory) {
                final AbstractFactory factory = (AbstractFactory) provider;
                final Citation vendor = getVendor();
                if (vendor!=null && vendor.equals(factory.getVendor())) {
                    if (sameAuthority(factory)) {
                        if (priority > factory.priority) {
                            registry.setOrdering(category, this, factory);
                        } else if (priority < factory.priority) {
                            registry.setOrdering(category, factory, this);
                        }
                    }
                }
            }
        }
    }

    /**
     * Called when this factory is removed from the given <code>category</code> of the given
     * <code>registry</code>.  The object may still be registered under another category or
     * categories.
     * <br><br>
     * This method is invoked automatically when this factory is no longer registered as a plugin,
     * and should not be invoked directly by the user.
     *
     * @param registry a <code>ServiceRegistry</code> from which this object is being
     *        (wholly or partially) deregistered.
     * @param category a <code>Class</code> object indicating the registry category from
     *        which this object is being deregistered.
     *
     * @see FactoryFinder
     */
    public void onDeregistration(final ServiceRegistry registry, final Class category) {
        // No action needed.
    }
}
