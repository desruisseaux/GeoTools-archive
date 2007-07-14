/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

// OpenGIS dependencies
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.ReferencingFactoryFinder;


/**
 * An authority factory that delegates the object creation to an other factory
 * determined from the authority name in the code. This factory performs the same
 * work than its {@linkplain ManyAuthoritiesFactory super-class}, except that it
 * additionnally queries the authority factories provided by
 * <code>ReferencingFactoryFinder.{@linkplain ReferencingFactoryFinder#getCRSAuthorityFactory
 * getCRSAuthorityFactory}("EPSG", hints)</code>.
 * <p>
 * This class is not registered in {@link ReferencingFactoryFinder}. If this "authority" factory
 * is wanted, then users need to refer explicitly to the {@link #DEFAULT} constant or to create
 * their own instance.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AllAuthoritiesFactory extends ManyAuthoritiesFactory {
    /**
     * An instance of {@code AllAuthoritiesFactory} with the
     * {@linkplain GenericName#DEFAULT_SEPARATOR default name separator} and no hints.
     */
    public static AllAuthoritiesFactory DEFAULT = new AllAuthoritiesFactory(null);

    /**
     * User-supplied hints provided at construction time.
     * Its content may or may not be identical to {@link #hints}.
     */
    private final Hints userHints;

    /**
     * Creates a new factory using the specified hints.
     *
     * @param userHints An optional set of hints, or {@code null} if none.
     */
    public AllAuthoritiesFactory(final Hints userHints) {
        this(userHints, null);
    }

    /**
     * Creates a new factory using the specified hints and a set of user factories.
     * If {@code factories} is not null, then any call to a {@code createFoo(code)} method will
     * first scan the supplied factories in their iteration order. The first factory implementing
     * the appropriate interface and having the expected {@linkplain AuthorityFactory#getAuthority
     * authority name} will be used. Only if no suitable factory is found, then this class delegates
     * to {@link ReferencingFactoryFinder}.
     * <p>
     * If the {@code factories} collection contains more than one factory for the same authority
     * and interface, then all additional factories will be {@linkplain FallbackAuthorityFactory
     * fallbacks}, to be tried in iteration order only if the first acceptable factory failed to
     * create the requested object.
     *
     * @param userHints An optional set of hints, or {@code null} if none.
     * @param factories A set of user-specified factories to try before to delegate
     *        to {@link ReferencingFactoryFinder}, or {@code null} if none.
     */
    public AllAuthoritiesFactory(final Hints userHints,
                                 final Collection/*<? extends AuthorityFactory>*/ factories)
    {
        this(userHints, factories, (char) 0);
    }

    /**
     * Creates a new factory using the specified hints, user factories and name
     * separator. The optional {@code factories} collection is handled as in the
     * {@linkplain #AllAuthoritiesFactory(Hints, Collection) constructor above}.
     *
     * @param userHints An optional set of hints, or {@code null} if none.
     * @param factories A set of user-specified factories to try before to delegate
     *        to {@link ReferencingFactoryFinder}, or {@code null} if none.
     * @param separator The separator between the authority name and the code.
     *
     * @deprecated Override the {@link #getSeparator} method instead.
     */
    public AllAuthoritiesFactory(final Hints userHints,
                                 final Collection/*<? extends AuthorityFactory>*/ factories,
                                 final char separator)
    {
        super(userHints, factories, separator);
        this.userHints = new Hints(userHints);
    }

    /**
     * Returns the set of authority names.
     *
     * @since 2.4
     */
    //@Override
    public Set/*<String>*/ getAuthorityNames() {
        final Set names = super.getAuthorityNames();
        names.addAll(ReferencingFactoryFinder.getAuthorityNames());
        return names;
    }

    /**
     * Returns a factory for the specified authority, type and hints.
     */
    //@Override
    final AuthorityFactory fromFactoryRegistry(final String authority,
                                               final Class/*<? extends AuthorityFactory>*/ type)
            throws FactoryRegistryException
    {
        if (CRSAuthorityFactory.class.equals(type)) {
            return ReferencingFactoryFinder.getCRSAuthorityFactory(authority, userHints);
        } else if (CSAuthorityFactory.class.equals(type)) {
            return ReferencingFactoryFinder.getCSAuthorityFactory(authority, userHints);
        } else if (DatumAuthorityFactory.class.equals(type)) {
            return ReferencingFactoryFinder.getDatumAuthorityFactory(authority, userHints);
        } else if (CoordinateOperationAuthorityFactory.class.equals(type)) {
            return ReferencingFactoryFinder.getCoordinateOperationAuthorityFactory(authority, userHints);
        } else {
            return super.fromFactoryRegistry(authority, type);
        }
    }

    /**
     * Returns a copy of the hints specified by the user at construction time. It may or may
     * not be the same than the {@linkplain #getImplementationHints implementation hints} for
     * this class.
     */
    final Hints getUserHints() {
        return new Hints(userHints);
    }

    /**
     * Returns a finder which can be used for looking up unidentified objects.
     * The default implementation delegates the lookups to the underlying factories.
     *
     * @since 2.4
     */
    //@Override
    public IdentifiedObjectFinder getIdentifiedObjectFinder(final Class/*<? extends IdentifiedObject>*/ type)
            throws FactoryException
    {
        return new Finder(this, type);
    }

    /**
     * A {@link IdentifiedObjectFinder} which tests every factories.
     */
    private static final class Finder extends ManyAuthoritiesFactory.Finder {
        /**
         * Creates a finder for the specified type.
         */
        protected Finder(final ManyAuthoritiesFactory factory,
                         final Class/*<? extends IdentifiedObject>*/ type)
        {
            super(factory, type);
        }

        /**
         * Returns all factories to try.
         */
        private Set/*<AuthorityFactory>*/ fromFactoryRegistry() {
            final ManyAuthoritiesFactory factory = (ManyAuthoritiesFactory) getProxy().getAuthorityFactory();
            final Class/*<? extends AuthorityFactory>*/ type = getProxy().getType();
            final Set factories = new LinkedHashSet();
            for (final Iterator it=ReferencingFactoryFinder.getAuthorityNames().iterator(); it.hasNext();) {
                final String authority = (String) it.next();
                factory.fromFactoryRegistry(authority, type, factories);
            }
            // Removes the factories already tried by super-class.
            final Collection done = getFactories();
            if (done != null) {
                factories.removeAll(done);
            }
            return factories;
        }

        /**
         * Lookups for the specified object.
         */
        //@Override
        public IdentifiedObject find(final IdentifiedObject object) throws FactoryException {
            IdentifiedObject candidate = super.find(object);
            if (candidate != null) {
                return candidate;
            }
            IdentifiedObjectFinder finder;
            final Iterator it = fromFactoryRegistry().iterator();
            while ((finder = next(it)) != null) {
                candidate = finder.find(object);
                if (candidate != null) {
                    break;
                }
            }
            return candidate;
        }

        /**
         * Returns the identifier of the specified object, or {@code null} if none.
         */
        //@Override
        public String findIdentifier(final IdentifiedObject object) throws FactoryException {
            String candidate = super.findIdentifier(object);
            if (candidate != null) {
                return candidate;
            }
            IdentifiedObjectFinder finder;
            final Iterator it = fromFactoryRegistry().iterator();
            while ((finder = next(it)) != null) {
                candidate = finder.findIdentifier(object);
                if (candidate != null) {
                    break;
                }
            }
            return candidate;
        }
    }
}
