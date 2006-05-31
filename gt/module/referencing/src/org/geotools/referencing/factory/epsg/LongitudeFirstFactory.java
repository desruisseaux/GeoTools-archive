/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import javax.imageio.spi.ServiceRegistry;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.referencing.factory.OrderedAxisAuthorityFactory;
import org.geotools.metadata.iso.citation.Citations;

/**
 * An EPSG authority factory using (<var>longitude</var>,<var>latitude</var>) axis order.
 * This factory wraps a {@link DefaultFactory} into an {@link OrderedAxisAuthorityFactory}
 * when first needed.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @tutorial http://docs.codehaus.org/display/GEOTOOLS/The+axis+order+issue
 */
public class LongitudeFirstFactory extends DeferredAuthorityFactory
        implements CRSAuthorityFactory, CSAuthorityFactory
{
    /**
     * The service registry to use for fetching the {@linkplain DefaultFactory default factory}
     * instance, or {@code null} if not yet set.
     */
    private ServiceRegistry registry;

    /**
     * Number of time {@link #registry} was set during an {@link #onRegistration} method call.
     * Used in order to determine when to reset {@link #registry} to {@code null} during an
     * {@link #onDeregistration} method call.
     */
    private int counter;

    /**
     * Creates a default factory. The
     * {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER FORCE_LONGITUDE_FIRST_AXIS_ORDER},
     * {@link Hints#FORCE_STANDARD_AXIS_DIRECTIONS   FORCE_STANDARD_AXIS_DIRECTIONS} and
     * {@link Hints#FORCE_STANDARD_AXIS_UNITS        FORCE_STANDARD_AXIS_UNITS} hints
     * are set to {@link Boolean#TRUE TRUE}.
     */
    public LongitudeFirstFactory() {
        this(null);
    }

    /**
     * Creates a factory from the specified set of hints.
     *
     * @param hints An optional set of hints, or {@code null} for the default values.
     */
    public LongitudeFirstFactory(final Hints hints) {
        super(hints, DefaultFactory.PRIORITY - 10);
        put(null,  Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER);
        put(hints, Hints.FORCE_STANDARD_AXIS_DIRECTIONS);
        put(hints, Hints.FORCE_STANDARD_AXIS_UNITS);
    }

    /**
     * Stores a value from the specified hints.
     */
    private void put(final Hints source, final Hints.Key key) {
        Object value;
        if (source != null) {
            value = source.get(key);
            if (value == null) {
                value = Boolean.TRUE;
            }
        } else {
            value = Boolean.TRUE;
        }
        hints.put(key, value);
    }

    /**
     * Returns the authority for this EPSG database.
     * This authority will contains the database version in the {@linkplain Citation#getEdition
     * edition} attribute, together with the {@linkplain Citation#getEditionDate edition date}.
     */
    public Citation getAuthority() {
        final Citation authority = super.getAuthority();
        return (authority!=null) ? authority : Citations.EPSG;
    }

    /**
     * Returns the {@link DefaultFactory} instance to be used as the backing store. The default
     * implementation get the instance from the {@linkplain ServiceRegistry service registry}
     * declared {@linkplain #onRegistration on registration}.
     *
     * @throws FactoryException If no {@link DefaultFactory} instance was found.
     */
    protected AbstractAuthorityFactory createBackingStore() throws FactoryException {
        DefaultFactory factory = null;
        if (registry != null) {
            // TODO: remove the case when we will be allowed to compile for J2SE 1.5.
            factory = (DefaultFactory) registry.getServiceProviderByClass(DefaultFactory.class);
        }
        if (factory == null) try {
            factory = (DefaultFactory) FactoryFinder.getCRSAuthorityFactory("EPSG",
                        new Hints(Hints.CRS_AUTHORITY_FACTORY, DefaultFactory.class));
        } catch (FactoryRegistryException exception) {
            throw new FactoryException(exception);
        }
        return new OrderedAxisAuthorityFactory(factory, new Hints(hints), null);
    }

    /**
     * Called when this factory is added to the given {@code category} of the given
     * {@code registry}.
     */
    public void onRegistration(final ServiceRegistry registry, final Class category) {
        super.onRegistration(registry, category);
        if (this.registry == null) {
            this.registry = registry;
        } else if (this.registry != registry) {
            throw new IllegalStateException();
        }
        counter++;
    }

    /**
     * Called when this factory is removed from the given {@code category} of the given
     * {@code registry}.  The object may still be registered under another category.
     */
    public void onDeregistration(final ServiceRegistry registry, final Class category) {
        if (this.registry != registry) {
            throw new IllegalStateException();
        } else if (--counter == 0) {
            this.registry = null;
        }
        super.onDeregistration(registry, category);
    }
}
