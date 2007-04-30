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
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.NoSuchAuthorityCodeException;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.metadata.iso.citation.Citations;


/**
 * Looks up an object from an {@linkplain AuthorityFactory authority factory} which is
 * {@linkplain CRS#equalsIgnoreMetadata equals, ignoring metadata}, to the specified object.
 * The main purpose of this class is to get a fully {@linkplain IdentifiedObject identified
 * object} from an incomplete one, for example from an object without identifier or
 * "{@code AUTHORITY[...]}" element in <cite>Well Known Text</cite> terminology.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class IdentifiedObjectFinder {
    /**
     * The proxy for object creation.
     */
    private final AuthorityFactoryProxy proxy;

    /**
     * {@code true} for performing full scans, or {@code false} otherwise.
     */
    private boolean fullScan = true;

    /**
     * Creates a finder using the same proxy than the specified finder.
     */
    IdentifiedObjectFinder(final IdentifiedObjectFinder finder) {
        this.proxy = finder.proxy;
    }

    /**
     * Creates a finder using the specified factory. This constructor is
     * protected because instances of this class should not be created directly.
     * Use {@link AbstractAuthorityFactory#getIdentifiedObjectFinder} instead.
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
     * Returns the authority factory specified at construction time.
     */
    public AuthorityFactory getAuthorityFactory() {
        return proxy.getAuthorityFactory();
    }

    /**
     * If {@code true}, an exhaustive full scan against all registered objects
     * will be performed (may be slow). Otherwise only a fast lookup based on
     * embedded identifiers and names will be performed. The default value is
     * {@code true}.
     */
    public boolean isFullScanAllowed() {
        return fullScan;
    }

    /**
     * Set whatever an exhaustive scan against all registered objects is allowed.
     * The default value is {@code true}.
     */
    public void setFullScanAllowed(final boolean fullScan) {
        this.fullScan = fullScan;
    }

    /**
     * Looks up an object from the {@linkplain #getAuthorityFactory authority factory}
     * which is {@linkplain CRS#equalsIgnoreMetadata equals, ignoring metadata}, to the
     * specified object. The default implementation tries to instantiate some
     * {@linkplain IdentifiedObject identified objects} from the authority factory
     * specified at construction time, in the following order:
     * <p>
     * <ul>
     *   <li>If the specified object contains {@linkplain IdentifiedObject#getIdentifiers
     *       identifiers} associated to the same authority than the factory, then those
     *       identifiers are used for {@linkplain AuthorityFactory#createObject creating
     *       objects} to be tested.</li>
     *   <li>If the authority factory can create objects from their {@linkplain
     *       IdentifiedObject#getName name} in addition of identifiers, then the name and
     *       {@linkplain IdentifiedObject#getAlias aliases} are used for creating objects
     *       to be tested.</li>
     *   <li>If {@linkplain #isFullScanAllowed full scan is allowed}, then full
     *       {@linkplain #getCodeCandidates set of authority codes} are used for
     *       creating objects to be tested.</li>
     * </ul>
     * <p>
     * The first of the above created objects which is equals to the specified object in the
     * the sense of {@link CRS#equalsIgnoreMetadata equalsIgnoreMetadata} is returned.
     *
     * @param  object The object looked up.
     * @return The identified object, or {@code null} if not found.
     * @throws FactoryException if an error occured while creating an object.
     */
    public IdentifiedObject find(final IdentifiedObject object)
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
     * Returns the identifier of the specified object, or {@code null} if none. The default
     * implementation invokes <code>{@linkplain #find find}(object)</code> and extracts the
     * code from the returned {@linkplain IdentifiedObject identified object}.
     */
    public String findIdentifier(final IdentifiedObject object) throws FactoryException {
        final IdentifiedObject candidate = find(object);
        if (candidate != null) {
            final Citation authority = getAuthorityFactory().getAuthority();
            ReferenceIdentifier id = AbstractIdentifiedObject.getIdentifier(candidate, authority);
            if (id == null) {
                id = candidate.getName();
                // Should never be null past this point, since 'name' is a mandatory attribute.
            }
            return id.toString();
        }
        return null;
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
        final Set/*<String>*/ codes = getCodeCandidates(object);
        for (final Iterator it=codes.iterator(); it.hasNext();) {
            final String code = (String) it.next();
            IdentifiedObject candidate;
            try {
                candidate = create(code);
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
        final Citation authority = getAuthorityFactory().getAuthority();
        for (final Iterator it=object.getIdentifiers().iterator(); it.hasNext();) {
            final Identifier id = (Identifier) it.next();
            if (!Citations.identifierMatches(authority, id.getAuthority())) {
                // The identifier is not for this authority. Looks the other ones.
                continue;
            }
            IdentifiedObject candidate;
            try {
                candidate = create(id.getCode());
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
            candidate = create(object.getName().getCode());
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
                candidate = create(id.toString());
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
     * Creates an object for the specified code. This method will delegates to the most
     * specific {@code create} method from the authority factory for the type specified
     * at construction time.
     *
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    protected IdentifiedObject create(final String code) throws FactoryException {
        return proxy.create(code);
    }

    /**
     * Returns a set of authority codes that <strong>may</strong> identify the same object than
     * the specified one. The returned set must contains the code of every objects that are
     * {@linkplain CRS#equalsIgnoreMetadata equals, ignoring metadata}, to the specified one.
     * However the set is not required to contains only the codes of those objects; it may
     * conservatively contains the code for more objects if an exact search is too expensive.
     * <p>
     * This method is invoked by the default {@link #find find} method implementation. The caller
     * may iterates through every returned codes, instantiate the objects and compare them with
     * the specified one in order to determine which codes are really applicable.
     * <p>
     * The default implementation returns the same set than
     * <code>{@linkplain AuthorityFactory#getAuthorityCodes getAuthorityCodes}(type)</code>
     * where {@code type} is the interface specified at construction type. Subclasses should
     * override this method in order to return a smaller set, if they can.
     *
     * @param  object The object looked up.
     * @return A set of code candidates.
     * @throws FactoryException if an error occured while fetching the set of code candidates.
     */
    protected Set/*<String>*/ getCodeCandidates(final IdentifiedObject object) throws FactoryException {
        return proxy.getAuthorityCodes();
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
    IdentifiedObject getAcceptable(final IdentifiedObject candidate, final IdentifiedObject model)
            throws FactoryException
    {
        return CRS.equalsIgnoreMetadata(candidate, model) ? candidate : null;
    }
}
