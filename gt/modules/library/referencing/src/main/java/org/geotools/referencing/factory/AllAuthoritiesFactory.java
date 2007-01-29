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
import java.util.*;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.Factory;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.FactoryFinder;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.util.GenericName;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * An authority factory that delegates the object creation to an other factory determined from the
 * authority name in the code. This factory requires that every codes given to a {@code createFoo}
 * method are prefixed by the authority name, for example {@code "EPSG:4326"}. This is different
 * from using a factory from a known authority, in which case the authority part was optional (for
 * example when using the {@linkplain org.geotools.referencing.factory.epsg EPSG authority factory},
 * the {@code "EPSG:"} part in {@code "EPSG:4326"} is optional).
 * <p>
 * This class parses the authority name and delegates the work the corresponding factory. For
 * example if any {@code createFoo(...)} method in this class is invoked with a code starting
 * by {@code "EPSG:"}, then this class delegates the object creation to the authority factory
 * provided by <code>FactoryFinder.{@linkplain FactoryFinder#getCRSAuthorityFactory
 * getCRSAuthorityFactory}("EPSG", hints)</code>.
 * <p>
 * This class is not registered in {@link FactoryFinder}, because it is not a real authority
 * factory. There is not a single authority name associated to this factory, but rather a set
 * of names determined from all available authority factories. If this "authority" factory is
 * wanted, then users need to refer explicitly to the {@link #DEFAULT} constant or to create
 * their own instance.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AllAuthoritiesFactory extends AuthorityFactoryAdapter implements CRSAuthorityFactory,
        CSAuthorityFactory, DatumAuthorityFactory, CoordinateOperationAuthorityFactory
{
    /**
     * An instance of {@code AllAuthoritiesFactory} with the
     * {@linkplain GenericName#DEFAULT_SEPARATOR default name separator} and no hints.
     */
    public static AllAuthoritiesFactory DEFAULT = new AllAuthoritiesFactory(null);

    /**
     * The authority name for this factory.
     */
    private static final Citation AUTHORITY = (Citation)
            new CitationImpl(Vocabulary.format(VocabularyKeys.ALL)).unmodifiable();

    /**
     * The types to be recognized for the {@code factories} argument in constructors. Must be
     * consistent with the types expected by the {@link #getAuthorityFactory(Class, String)}
     * method.
     */
    private static final Class[] AUTHORIZED_TYPES = new Class[] {
        CRSAuthorityFactory.class,
        DatumAuthorityFactory.class,
        CSAuthorityFactory.class,
        CoordinateOperationAuthorityFactory.class
    };

    /**
     * A set of user-specified factories to try before to delegate to {@link FactoryFinder},
     * or {@code null} if none.
     */
    private final Collection/*<AuthorityFactory>*/ factories;

    /**
     * The separator between the authority name and the code.
     *
     * @deprecated Remove this field after we removed the deprecated constructor.
     */
    private final char separator;

    /**
     * Guard against infinite recursivity in {@link #getAuthorityCodes}.
     */
    private final ThreadLocal/*<Boolean>*/ inProgress = new ThreadLocal();

    /**
     * User-supplied hints provided at construction time.
     * Its content may or may not be identical to {@link #hints}.
     */
    private final Hints userHints;

    /**
     * Creates a new factory using the specified hints.
     *
     * @param hints An optional set of hints, or {@code null} if none.
     */
    public AllAuthoritiesFactory(final Hints hints) {
        this(hints, null);
    }

    /**
     * Creates a new factory using the specified hints and a set of user factories.
     * If {@code factories} is not null, then any call to a {@code createFoo(code)} method will
     * first scan the supplied factories in their iteration order. The first factory implementing
     * the appropriate interface and having the expected {@linkplain AuthorityFactory#getAuthority
     * authority name} will be used. Only if no suitable factory is found, then this class delegates
     * to {@link FactoryFinder}.
     * <p>
     * If the {@code factories} collection contains more than one factory for the same authority
     * and interface, then all additional factories will be {@linkplain FallbackAuthorityFactory
     * fallbacks}, to be tried in iteration order only if the first acceptable factory failed to
     * create the requested object.
     *
     * @param hints An optional set of hints, or {@code null} if none.
     * @param factories A set of user-specified factories to try before to delegate
     *        to {@link FactoryFinder}, or {@code null} if none.
     */
    public AllAuthoritiesFactory(final Hints hints,
                                 final Collection/*<? extends AuthorityFactory>*/ factories)
    {
        this(hints, factories, (char) 0);
    }

    /**
     * Creates a new factory using the specified hints, user factories and name
     * separator. The optional {@code factories} collection is handled as in the
     * {@linkplain #AllAuthoritiesFactory(Hints, Collection) constructor above}.
     *
     * @param hints An optional set of hints, or {@code null} if none.
     * @param factories A set of user-specified factories to try before to delegate
     *        to {@link FactoryFinder}, or {@code null} if none.
     * @param separator The separator between the authority name and the code.
     *
     * @deprecated Override the {@link #getSeparator} method instead.
     */
    public AllAuthoritiesFactory(final Hints hints,
                                 final Collection/*<? extends AuthorityFactory>*/ factories,
                                 final char separator)
    {
        super(NORMAL_PRIORITY);
        this.separator = separator;
        this.userHints = new Hints(hints);
        if (factories!=null && !factories.isEmpty()) {
            for (final Iterator it=factories.iterator(); it.hasNext();) {
                final Object factory = it.next();
                if (factory instanceof Factory) {
                    this.hints.putAll(((Factory) factory).getImplementationHints());
                }
            }
            this.factories = createFallbacks(factories);
        } else {
            this.factories = null;
        }
    }

    /**
     * If more than one factory is found for the same authority and interface,
     * then wraps them as a chain of {@link FallbackAuthorityFactory}.
     */
    private static Collection/*<AuthorityFactory>*/ createFallbacks(
            final Collection/*<? extends AuthorityFactory>*/ factories)
    {
        /*
         * 'authorities' Will contains the set of all authorities found without duplicate values
         * in the sense of Citations.identifierMatches(...). 'factoriesByType' will contains the
         * collection of factories for each (authority,type) pair.
         */
        int authorityCount = 0;
        final Citation[] authorities = new Citation[factories.size()];
        final List[] factoriesByType = new List[authorities.length * AUTHORIZED_TYPES.length];
        final Map/*<AuthorityFactory,Integer>*/ positions = new IdentityHashMap();
        for (final Iterator it=factories.iterator(); it.hasNext();) {
            final AuthorityFactory factory = (AuthorityFactory) it.next();
            Citation authority = factory.getAuthority();
            /*
             * Remember the factory position for later use, in order to preserve iteration order.
             * We take the opportunity for trimming duplicated factories (should not occur often).
             * Note: we store XORed values (~, not -) as a flag for use after the enclosing loop.
             */
            final Integer old = (Integer) positions.put(factory, new Integer(~positions.size()));
            if (old != null) {
                positions.put(factory, old);
                continue;
            }
            /*
             * Check if the authority has already been meet previously. If the authority is found
             * (no matter the type), then 'authorityIndex' is set to its index. Otherwise the new
             * authority is added to the 'authorities' list.
             */
            int authorityBase;
            for (authorityBase=0; authorityBase<authorityCount; authorityBase++) {
                final Citation candidate = authorities[authorityBase];
                if (Citations.identifierMatches(candidate, authority)) {
                    authority = candidate;
                    break;
                }
            }
            if (authorityBase == authorityCount) {
                authorities[authorityCount++] = authority;
            }
            /*
             * For each type, check if the factory implements the corresponding interface. If it
             * does, then the factory is added to the list of factories for this (authority,type)
             * pair. Otherwise it is silently ignored.
             */
            authorityBase *= AUTHORIZED_TYPES.length;
            for (int i=0; i<AUTHORIZED_TYPES.length; i++) {
                final Class type = AUTHORIZED_TYPES[i];
                if (type.isInstance(factory)) {
                    List forType = factoriesByType[authorityBase + i];
                    if (forType == null) {
                        factoriesByType[authorityBase + i] = forType = new ArrayList();
                    }
                    forType.add(factory);
                }
            }
        }
        /*
         * For each (authority,type) pair with two or more factories, chain those factories into
         * a FallbackAuthorityFactory object.  The definitive factories are stored into an array
         * (the order is significant) without duplicated values.
         */
        final ArrayList/*<AuthorityFactory,Integer>*/ result = new ArrayList();
        for (int i=0; i<factoriesByType.length; i++) {
            final List forType = factoriesByType[i];
            if (forType != null) {
                AuthorityFactory factory = (AuthorityFactory) forType.get(0);
                Integer position = (Integer) positions.get(factory);
                if (forType.size() != 1) {
                    final Class type = AUTHORIZED_TYPES[i % AUTHORIZED_TYPES.length];
                    factory = FallbackAuthorityFactory.create(type, forType);
                    if (position.intValue() < 0) {
                        position = new Integer(~position.intValue());
                    }
                    if (positions.put(factory, position) != null) {
                        throw new AssertionError(factory); // Should never happen.
                    }
                } else if (position.intValue() >= 0) {
                    // Factory already added to the list.
                    continue;
                } else {
                    // Factory without fallback, and not yet added to the list.
                    positions.put(factory, new Integer(~position.intValue()));
                }
                result.add(factory);
            }
        }
        /*
         * Sort the factories in iteration order (i.e. in the same order than the user-supplied
         * factories).
         */
        result.trimToSize();
        Collections.sort(result, new Comparator/*<AuthorityFactory>*/() {
            public int compare(final Object f1, final Object f2) {
                final int p1 = ((Integer) positions.get(f1)).intValue();
                final int p2 = ((Integer) positions.get(f2)).intValue();
                assert p1 >= 0 : p1;
                assert p2 >= 0 : p2;
                return p1 - p2;
            }
        });
        return result;
    }

    /**
     * Returns the character separator for the specified code. The default implementation returns
     * the {@linkplain GenericName#DEFAULT_SEPARATOR default name separator} {@code ':'}, except
     * if the code looks like a URL (e.g. {@code "http://www.opengis.net/"}), in which case this
     * method returns {@code '/'}.
     * <p>
     * In the current implementation, "looks like a URL" means that the first
     * non-{@linkplain Character#isLetterOrDigit(char) aplhanumeric} characters
     * are {@code "://"}. But this heuristic rule may change in future implementations.
     *
     * @since 2.4
     */
    protected char getSeparator(String code) {
        if (separator != 0) {
            // Remove this block after we removed the deprecated separator field.
            return separator;
        }
        code = code.trim();
        final int length = code.length();
        for (int i=0; i<length; i++) {
            if (!Character.isLetterOrDigit(code.charAt(i))) {
                if (code.regionMatches(i, "://", 0, 3)) {
                    return '/';
                }
                break;
            }
        }
        return GenericName.DEFAULT_SEPARATOR;
    }

    /**
     * Returns {@code true} if the specified code can be splitted in a (<cite>authority</code>,
     * <cite>code</code>) pair at the specified index. The default implementation returns
     * {@code true} if the first non-whitespace character on each side are valid Java identifiers.
     * <p>
     * We may consider to turn this method into a protected one if the users need to override it.
     */
    private static boolean canSeparateAt(final String code, final int index) {
        char c;
        int i = index;
        do {
            if (--i < 0) {
                return false;
            }
            c = code.charAt(i);
        } while (Character.isWhitespace(c));
        if (!Character.isJavaIdentifierPart(c)) {
            return false;
        }
        final int length = code.length();
        i = index;
        do {
            if (++i >= length) {
                return false;
            }
            c = code.charAt(i);
        } while (Character.isWhitespace(c));
        return Character.isJavaIdentifierPart(c);
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
     * Returns the vendor responsible for creating this factory implementation.
     * The default implementation returns {@linkplain Citations#GEOTOOLS Geotools}.
     */
    public Citation getVendor() {
        return Citations.GEOTOOLS;
    }

    /**
     * Returns the organization or party responsible for definition and maintenance of the
     * database. The default implementation returns a citation named "All".
     */
    public Citation getAuthority() {
        return AUTHORITY;
    }

    /**
     * Searchs for a factory of the given type. This method first search in user-supplied
     * factories. If no user factory is found, then this method request for a factory using
     * {@link FactoryFinder}. The authority name is inferred from the specified code.
     *
     * @param  type The interface to be implemented.
     * @param  code The code of the object to create.
     * @return The factory.
     * @throws NoSuchAuthorityCodeException if no suitable factory were found.
     */
    private AuthorityFactory getAuthorityFactory(final Class/*<T extends AuthorityFactory>*/ type,
                                                 final String code)
            throws NoSuchAuthorityCodeException
    {
        ensureNonNull("code", code);
        String authority = null;
        FactoryRegistryException cause = null;
        final char separator = getSeparator(code);
        for (int split = code.lastIndexOf(separator); split >= 0;
                 split = code.lastIndexOf(separator, split-1))
        {
            if (!canSeparateAt(code, split)) {
                continue;
            }
            /*
             * Try all possible authority names, begining with the most specific ones.
             * For example if the code is "urn:ogc:def:crs:EPSG:6.8:4326", then we will
             * try "urn:ogc:def:crs:EPSG:6.8" first, "urn:ogc:def:crs:EPSG" next, etc.
             * until a suitable factory is found (searching into user-supplied factories
             * first).
             */
            authority = code.substring(0, split).trim();
            if (factories != null) {
                for (final Iterator it=factories.iterator(); it.hasNext();) {
                    final AuthorityFactory factory = (AuthorityFactory) it.next();
                    if (type.isAssignableFrom(factory.getClass())) {
                        if (Citations.identifierMatches(factory.getAuthority(), authority)) {
                            return factory;
                        }
                    }
                }    
            }
            /*
             * No suitable user-supplied factory. Now query FactoryFinder.
             */
            final AuthorityFactory factory;
            try {
                if (CRSAuthorityFactory.class.equals(type)) {
                    factory = FactoryFinder.getCRSAuthorityFactory(authority, userHints);
                } else if (CSAuthorityFactory.class.equals(type)) {
                    factory = FactoryFinder.getCSAuthorityFactory(authority, userHints);
                } else if (DatumAuthorityFactory.class.equals(type)) {
                    factory = FactoryFinder.getDatumAuthorityFactory(authority, userHints);
                } else if (CoordinateOperationAuthorityFactory.class.equals(type)) {
                    factory = FactoryFinder.getCoordinateOperationAuthorityFactory(authority, userHints);
                } else {
                    continue;
                }
            } catch (FactoryRegistryException exception) {
                cause = exception;
                continue;
            }
            return /*type.cast*/(factory);
            // TODO: uncomment when we will be allowed to compile for J2SE 1.5.
        }
        /*
         * No factory found. Creates an error message from the most global authority name
         * (for example "urn" if the code was "urn:ogc:def:crs:EPSG:6.8:4326") and the
         * corresponding cause. Both the authority and cause may be null if the code didn't
         * had any authority part.
         */
        throw noSuchAuthority(code, authority, cause);
    }

    /**
     * Formats the exception to be throw when the user asked for a code from an unknown authority.
     *
     * @param  code      The code with an unknown authority.
     * @param  authority The authority, or {@code null} if none.
     * @param  cause     The cause for the exception to be formatted, or {@code null} if none.
     * @return The formatted exception to be throw.
     */
    private NoSuchAuthorityCodeException noSuchAuthority(final String code, String authority,
                                                         final FactoryRegistryException cause)
    {
        final String message;
        if (authority == null) {
            authority = Vocabulary.format(VocabularyKeys.UNKNOW);
            message   = Errors.format(ErrorKeys.MISSING_AUTHORITY_$1, code);
        } else {
            message = Errors.format(ErrorKeys.UNKNOW_AUTHORITY_$1, authority);
        }
        final NoSuchAuthorityCodeException exception;
        exception = new NoSuchAuthorityCodeException(message, authority, code);
        exception.initCause(cause);
        return exception;
    }

    /**
     * Returns a generic object authority factory for the specified {@code "AUTHORITY:NUMBER"}
     * code.
     * <p>
     * <b>Note:</b> this method is defined for safety, but should not be used since
     * most methods that may invoke it in {@link AuthorityFactoryAdapter} are overridden
     * in {@code AllAuthoritiesFactory}.
     *
     * @param  code The code to parse.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     *
     * @since 2.4
     */
    protected AuthorityFactory getAuthorityFactory(final String code)
            throws NoSuchAuthorityCodeException
    {
        return getAuthorityFactory(AuthorityFactory.class, code);
    }

    /**
     * Returns the datum authority factory for the specified {@code "AUTHORITY:NUMBER"} code.
     *
     * @param  code The code to parse.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     *
     * @since 2.4
     */
    protected DatumAuthorityFactory getDatumAuthorityFactory(final String code)
            throws NoSuchAuthorityCodeException
    {
        return (DatumAuthorityFactory) // TODO: remove cast with J2SE 1.5.
                getAuthorityFactory(DatumAuthorityFactory.class, code);
    }

    /**
     * Returns the CS authority factory for the specified {@code "AUTHORITY:NUMBER"} code.
     *
     * @param  code The code to parse.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     *
     * @since 2.4
     */
    protected CSAuthorityFactory getCSAuthorityFactory(final String code)
            throws NoSuchAuthorityCodeException
    {
        return (CSAuthorityFactory) // TODO: remove cast with J2SE 1.5.
                getAuthorityFactory(CSAuthorityFactory.class, code);
    }

    /**
     * Returns the CRS authority factory for the specified {@code "AUTHORITY:NUMBER"} code.
     *
     * @param  code The code to parse.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     *
     * @since 2.4
     */
    protected CRSAuthorityFactory getCRSAuthorityFactory(final String code)
            throws NoSuchAuthorityCodeException
    {
        return (CRSAuthorityFactory) // TODO: remove cast with J2SE 1.5.
                getAuthorityFactory(CRSAuthorityFactory.class, code);
    }

    /**
     * Returns the operation authority factory for the specified {@code "AUTHORITY:NUMBER"} code.
     *
     * @param  code The code to parse.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     *
     * @since 2.4
     */
    protected CoordinateOperationAuthorityFactory getCoordinateOperationAuthorityFactory(final String code)
            throws NoSuchAuthorityCodeException
    {
        return (CoordinateOperationAuthorityFactory) // TODO: remove cast with J2SE 1.5.
                getAuthorityFactory(CoordinateOperationAuthorityFactory.class, code);
    }

    /**
     * Returns the set of authority codes of the given type.
     *
     * @param  type The spatial reference objects type (may be {@code Object.class}).
     * @return The set of authority codes for spatial reference objects of the given type.
     *         If this factory doesn't contains any object of the given type, then this method
     *         returns an {@linkplain java.util.Collections#EMPTY_SET empty set}.
     * @throws FactoryException if access to the underlying database failed.
     */
    public Set/*<String>*/ getAuthorityCodes(final Class type) throws FactoryException {
        if (Boolean.TRUE.equals(inProgress.get())) {
            /*
             * 'getAuthorityCodes' is invoking itself (indirectly). Returns an empty set in order
             * to avoid infinite recursivity. Note that the end result (the output of the caller)
             * will usually not be empty.
             */
            return Collections.EMPTY_SET;
        }
        final Set/*<String>*/ codes = new LinkedHashSet();
        final Set/*<AuthorityFactory>*/ done = new HashSet();
        done.add(this); // Safety for avoiding recursive calls.
        inProgress.set(Boolean.TRUE);
        try {
            for (final Iterator it=FactoryFinder.getAuthorityNames().iterator(); it.hasNext();) {
                final String authority = ((String) it.next()).trim();
                final char separator = getSeparator(authority);
                /*
                 * Prepares a buffer with the "AUTHORITY:" part in "AUTHORITY:NUMBER".
                 * We will reuse this buffer in order to prefix the authority name in
                 * front of every codes.
                 */
                final StringBuffer code = new StringBuffer(authority);
                int codeBase = code.length();
                if (codeBase != 0 && code.charAt(codeBase - 1) != separator) {
                    code.append(separator);
                    codeBase = code.length();
                }
                code.append("all");
                final String dummyCode = code.toString();
                /*
                 * Now scan over all factories. We will process a factory only if this particular
                 * factory has not already been done in a previous iteration (some implementation
                 * apply to more than one factory).
                 */
                for (int i=0; i<AUTHORIZED_TYPES.length; i++) {
                    final AuthorityFactory factory;
                    try {
                        factory = getAuthorityFactory(AUTHORIZED_TYPES[i], dummyCode);
                    } catch (NoSuchAuthorityCodeException e) {
                        continue;
                    }
                    if (done.add(factory)) {
                        for (final Iterator it2=factory.getAuthorityCodes(type).iterator(); it2.hasNext();) {
                            String candidate = ((String) it2.next()).trim();
                            if (candidate.length() < codeBase ||
                                Character.isLetterOrDigit (candidate.charAt(codeBase-1)) ||
                               !authority.equalsIgnoreCase(candidate.substring(0, codeBase-1)))
                            {
                                // Prepend the authority code if it was not already presents.
                                code.setLength(codeBase);
                                code.append(candidate);
                                candidate = code.toString();
                            }
                            codes.add(candidate);
                        }
                    }
                }
            }
        } finally {
            inProgress.set(Boolean.FALSE);
        }
        return codes;
    }

    /**
     * Gets a description of the object corresponding to a code.
     *
     * @param  code Value allocated by authority.
     * @return A description of the object, or {@code null} if the object
     *         corresponding to the specified {@code code} has no description.
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the query failed for some other reason.
     */
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        final Set/*<AuthorityFactory>*/ done = new HashSet();
        done.add(this); // Safety for avoiding recursive calls.
        FactoryException failure = null;
        for (int type=0; type<AUTHORIZED_TYPES.length; type++) {
            /*
             * Try all factories, starting with the CRS factory because it is the only one most
             * users care about. If the CRS factory doesn't know about the specified object, then
             * we will try the other factories (datum, CS, ...) before to rethrow the exception.
             */
            final AuthorityFactory factory;
            try {
                factory = getAuthorityFactory(AUTHORIZED_TYPES[type], code);
            } catch (NoSuchAuthorityCodeException exception) {
                if (failure == null) {
                    failure = exception;
                }
                continue;
            }
            if (done.add(factory)) try {
                return factory.getDescriptionText(code);
            } catch (FactoryException exception) {
                /*
                 * Failed to creates an object using the current factory.  We will retain only the
                 * first exception and discart all other ones, except if the first exceptions were
                 * due to unknown authority (we will prefer exception due to unknown code instead).
                 * The first exception is usually thrown by the CRS factory, which is the only
                 * factory most users care about.
                 */
                if (failure==null || failure.getCause() instanceof FactoryRegistryException) {
                    failure = exception;
                }
            }
        }
        if (failure == null) {
            failure = noSuchAuthorityCode(IdentifiedObject.class, code);
        }
        throw failure;
    }

    /**
     * Returns an arbitrary object from a code.
     *
     * @see #createCoordinateReferenceSystem
     * @see #createDatum
     * @see #createEllipsoid
     * @see #createUnit
     */
    public IdentifiedObject createObject(final String code) throws FactoryException {
        final Set/*<AuthorityFactory>*/ done = new HashSet();
        done.add(this); // Safety for avoiding recursive calls.
        FactoryException failure = null;
        for (int type=0; type<AUTHORIZED_TYPES.length; type++) {
            /*
             * Try all factories, starting with the CRS factory because it is the only one most
             * users care about. If the CRS factory doesn't know about the specified object, then
             * we will try the other factories (datum, CS, ...) before to rethrow the exception.
             */
            final AuthorityFactory factory;
            try {
                factory = getAuthorityFactory(AUTHORIZED_TYPES[type], code);
            } catch (NoSuchAuthorityCodeException exception) {
                if (failure == null) {
                    failure = exception;
                }
                continue;
            }
            if (done.add(factory)) try {
                return factory.createObject(code);
            } catch (FactoryException exception) {
                /*
                 * Failed to creates an object using the current factory.  We will retain only the
                 * first exception and discart all other ones, except if the first exceptions were
                 * due to unknown authority (we will prefer exception due to unknown code instead).
                 * The first exception is usually thrown by the CRS factory, which is the only
                 * factory most users care about.
                 */
                if (failure==null || failure.getCause() instanceof FactoryRegistryException) {
                    failure = exception;
                }
            }
        }
        if (failure == null) {
            failure = noSuchAuthorityCode(IdentifiedObject.class, code);
        }
        throw failure;
    }
}
