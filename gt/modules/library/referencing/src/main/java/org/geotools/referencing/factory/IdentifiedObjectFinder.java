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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Set;
import java.util.Iterator;

// OpenGIS dependencies
import org.opengis.util.GenericName;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.metadata.iso.citation.Citations;


/**
 * Looks up an object from an authority factory which is {@linkplain CRS#equalsIgnoreMetadata
 * equals, ignoring metadata}, to the specified object.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
class IdentifiedObjectFinder {
    /**
     * The proxy for object creation.
     */
    private final AuthorityFactoryProxy proxy;

    /**
     * Creates a finder using the specified factory.
     *
     * @param factory The factory to scan for the identified objects.
     * @param type    The type of objects to lookup.
     */
    protected IdentifiedObjectFinder(final AuthorityFactory factory,
                                     final Class/*<? extends IdentifiedObject>*/ type)
    {
        proxy = AuthorityFactoryProxy.getInstance(factory, type);
    }

    /**
     * Looks up an object from this authority factory which is
     * {@linkplain CRS#equalsIgnoreMetadata equals, ignoring metadata},
     * to the specified object.
     */
    public IdentifiedObject find(final IdentifiedObject object, final boolean fullScan)
            throws FactoryException
    {
        /*
         * First check if one of the identifiers can be used to spot directly an
         * identified object (and check it's actually equal to one in the factory).
         */
        IdentifiedObject candidate = createFromIdentifiers(object);
        if (candidate != null) {
            return candidate;
        }
        /*
         * We are unable to find the object from its identifiers. Try a quick name lookup.
         * Some implementations like the one backed by the EPSG database are capable to find
         * an object from its name.
         */
        candidate = createFromNames(object);
        if (candidate != null) {
            return candidate;
        }
        /*
         * Here we exhausted the quick paths. Bail out if the user does not want a full scan.
         */
        return fullScan ? createEquivalent(object) : null;
    }

    /**
     * Creates an object {@linkplain CRS#equalsIgnoreMetadata equals, ignoring metadata}, to the
     * specified object. This method scans the {@linkplain #getAuthorityCodes authority codes},
     * {@linkplain #create create} the objects and returns the first one which is equals to the
     * specified object in the sense of {@link CRS#equalsIgnoreMetadata equalsIgnoreMetadata}.
     * <p>
     * This method may be used in order to get a fully {@linkplain IdentifiedObject identified
     * object} from an object without {@linkplain IdentifiedObject#getIdentifiers identifiers}.
     * <p>
     * Scaning the whole set of authority codes may be slow. Users should try
     * <code>{@linkplain #createFromIdentifiers createFromIdentifiers}(object)</code> and/or
     * <code>{@linkplain #createFromNames createFromNames}(object)</code> before to fallback
     * on this method.
     *
     * @param  object The object looked up.
     * @return The identified object, or {@code null} if not found.
     * @throws FactoryException if an error occured while scanning through authority codes.
     *
     * @see #createFromIdentifiers
     * @see #createFromNames
     */
    final IdentifiedObject createEquivalent(final IdentifiedObject object) throws FactoryException {
        Set/*<String>*/ codes = null;
        final AuthorityFactory factory = proxy.getAuthorityFactory();
        if (factory instanceof AbstractAuthorityFactory) {
            codes = ((AbstractAuthorityFactory) factory).getCodeCandidates(object);
        }
        if (codes == null) {
            codes = proxy.getAuthorityCodes();
        }
        for (final Iterator it=codes.iterator(); it.hasNext();) {
            final String code = (String) it.next();
            IdentifiedObject candidate;
            try {
                candidate = proxy.create(code);
            } catch (FactoryException e) {
                // Some object cannot be created properly.
                continue;
            }
            candidate = getAcceptable(candidate, object);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Creates an object {@linkplain CRS#equalsIgnoreMetadata equals, ignoring metadata}, to the
     * specified object using only the {@linkplain IdentifiedObject#getIdentifiers identifiers}.
     * If no such object is found, returns {@code null}.
     * <p>
     * This method may be used in order to get a fully identified object from a partially
     * identified one.
     *
     * @param  object The object looked up.
     * @return The identified object, or {@code null} if not found.
     * @throws FactoryException if an error occured while creating an object.
     *
     * @see #createEquivalent
     * @see #createFromNames
     */
    final IdentifiedObject createFromIdentifiers(final IdentifiedObject object) throws FactoryException {
        final Citation authority = proxy.getAuthorityFactory().getAuthority();
        for (final Iterator it=object.getIdentifiers().iterator(); it.hasNext();) {
            final Identifier id = (Identifier) it.next();
            if (!Citations.identifierMatches(authority, id.getAuthority())) {
                // The identifier is not for this authority. Looks the other ones.
                continue;
            }
            IdentifiedObject candidate;
            try {
                candidate = proxy.create(id.getCode());
            } catch (NoSuchAuthorityCodeException e) {
                // The identifier was not recognized. No problem, let's go on.
                continue;
            }
            candidate = getAcceptable(candidate, object);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Creates an object {@linkplain CRS#equalsIgnoreMetadata equals, ignoring metadata}, to
     * the specified object using only the {@linkplain IdentifiedObject#getName name} and
     * {@linkplain IdentifiedObject#getAlias aliases}. If no such object is found, returns
     * {@code null}.
     * <p>
     * This method may be used with some {@linkplain AuthorityFactory authority factory}
     * implementations like the one backed by the EPSG database, which are capable to find
     * an object from its name when the identifier is unknown.
     *
     * @param  object The object looked up.
     * @return The identified object, or {@code null} if not found.
     * @throws FactoryException if an error occured while creating an object.
     *
     * @see #createEquivalent
     * @see #createFromIdentifiers
     */
    final IdentifiedObject createFromNames(final IdentifiedObject object) throws FactoryException {
        IdentifiedObject candidate;
        try {
            candidate = proxy.create(object.getName().getCode());
            candidate = getAcceptable(candidate, object);
            if (candidate != null) {
                return candidate;
            }
        } catch (FactoryException e) {
            /*
             * The identifier was not recognized. No problem, let's go on.
             * Note: we catch a more generic exception than NoSuchAuthorityCodeException
             *       because this attempt may fail for various reasons (character string
             *       not supported by the underlying database for primary key, duplicated
             *       name found, etc.).
             */
        }
        for (final Iterator it=object.getAlias().iterator(); it.hasNext();) {
            final GenericName id = (GenericName) it.next();
            try {
                candidate = proxy.create(id.toString());
            } catch (FactoryException e) {
                // The name was not recognized. No problem, let's go on.
                continue;
            }
            candidate = getAcceptable(candidate, object);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Returns {@code candidate}, or an object derived from {@code candidate}, if it is
     * {@linkplain CRS#equalsIgnoreMetadata equals ignoring metadata} to the specified
     * model. Otherwise returns {@code null}.
     * <p>
     * This method is overriden by factories that may test many flavors of
     * {@code candidate}, for example {@link TransformedAuthorityFactory}.
     *
     * @throws FactoryException if an error occured while creating an object.
     */
    protected IdentifiedObject getAcceptable(final IdentifiedObject candidate,
                                             final IdentifiedObject model)
            throws FactoryException
    {
        return CRS.equalsIgnoreMetadata(candidate, model) ? candidate : null;
    }
}
