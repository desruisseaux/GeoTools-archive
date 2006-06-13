/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
public class AllAuthoritiesFactory extends AbstractAuthorityFactory implements
                DatumAuthorityFactory, CSAuthorityFactory, CRSAuthorityFactory,
                CoordinateOperationAuthorityFactory
{
    /**
     * The authority name for this factory.
     */
    private static final Citation AUTHORITY = (Citation)
            new CitationImpl(Vocabulary.format(VocabularyKeys.ALL)).unmodifiable();

    /**
     * An instance of {@code AllAuthoritiesFactory} with the
     * {@linkplain GenericName#DEFAULT_SEPARATOR default name separator} and no hints.
     */
    public static AllAuthoritiesFactory DEFAULT = new AllAuthoritiesFactory(null);

    /**
     * A set of user-specified factories to try before to delegate to {@link FactoryFinder},
     * or {@code null} if none.
     */
    private final Collection/*<AuthorityFactory>*/ factories;

    /**
     * The separator between the authority name and the code. The default value is {@code ':'}.
     */
    private final char separator;

    /**
     * User-supplied hints provided at construction time.
     * Its content may or may not be identical to {@link #hints}.
     */
    private final Hints userHints;

    /**
     * Creates a new factory using the specified hints and the
     * {@linkplain GenericName#DEFAULT_SEPARATOR default name separator}.
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
        this(hints, factories, GenericName.DEFAULT_SEPARATOR);
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
        int authorityCount=0;
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
     * Returns the organization or party responsible for definition and maintenance of the
     * database. The default implementation returns a citation named "All".
     */
    public Citation getAuthority() {
        return AUTHORITY;
    }

    /**
     * Returns the authority name for the specified code.
     *
     * @param  code The code to parse.
     * @return The authority name.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     */
    private String getAuthority(final String code) throws NoSuchAuthorityCodeException {
        ensureNonNull("code", code);
        final int split = code.indexOf(separator);
        if (split >= 0) {
            return code.substring(0, split).trim();
        }
        throw new NoSuchAuthorityCodeException(Errors.format(ErrorKeys.MISSING_AUTHORITY_$1, code),
                                               Vocabulary.format(VocabularyKeys.UNKNOW), code);
    }

    /**
     * Formats the exception to be throw when the user asked for a code from an unknown authority.
     *
     * @param  code  The code with an unknown authority.
     * @param  cause The cause for the exception to be formatted.
     * @return The formatted exception to be throw.
     */
    private NoSuchAuthorityCodeException noSuchAuthority(
            final String code, final FactoryRegistryException cause)
    {
        final String authority;
        try {
            authority = getAuthority(code);
        } catch (NoSuchAuthorityCodeException exception) {
            return exception;
        }
        final NoSuchAuthorityCodeException exception = new NoSuchAuthorityCodeException(
                Errors.format(ErrorKeys.UNKNOW_AUTHORITY_$1, authority), authority, code);
        exception.initCause(cause);
        return exception;
    }

    /**
     * Searchs for a user-supplied factory of the given type.
     *
     * @param  type      The interface to be implemented.
     * @param  authority The authority name.
     * @return The user factory, or {@code null} if none.
     */
    private AuthorityFactory getAuthorityFactory(final Class type, final String authority) {
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
        return null;
    }

    /**
     * Returns the datum authority factory for the specified {@code "AUTHORITY:NUMBER"} code.
     *
     * @param  code The code to parse.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     */
    private DatumAuthorityFactory getDatumAuthorityFactory(final String code)
            throws NoSuchAuthorityCodeException
    {
        final String authority = getAuthority(code);
        DatumAuthorityFactory factory = (DatumAuthorityFactory) // TODO: remove cast with J2SE 1.5.
                getAuthorityFactory(DatumAuthorityFactory.class, authority);
        if (factory == null) try {
            factory = FactoryFinder.getDatumAuthorityFactory(authority, userHints);
        } catch (FactoryRegistryException cause) {
            throw noSuchAuthority(code, cause);
        }
        return factory;
    }

    /**
     * Returns the CS authority factory for the specified {@code "AUTHORITY:NUMBER"} code.
     *
     * @param  code The code to parse.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     */
    private CSAuthorityFactory getCSAuthorityFactory(final String code)
            throws NoSuchAuthorityCodeException
    {
        final String authority = getAuthority(code);
        CSAuthorityFactory factory = (CSAuthorityFactory) // TODO: remove cast with J2SE 1.5.
                getAuthorityFactory(CSAuthorityFactory.class, authority);
        if (factory == null) try {
            factory = FactoryFinder.getCSAuthorityFactory(authority, userHints);
        } catch (FactoryRegistryException cause) {
            throw noSuchAuthority(code, cause);
        }
        return factory;
    }

    /**
     * Returns the CRS authority factory for the specified {@code "AUTHORITY:NUMBER"} code.
     *
     * @param  code The code to parse.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     */
    private CRSAuthorityFactory getCRSAuthorityFactory(final String code)
            throws NoSuchAuthorityCodeException
    {
        final String authority = getAuthority(code);
        CRSAuthorityFactory factory = (CRSAuthorityFactory) // TODO: remove cast with J2SE 1.5.
                getAuthorityFactory(CRSAuthorityFactory.class, authority);
        if (factory == null) try {
            factory = FactoryFinder.getCRSAuthorityFactory(authority, userHints);
        } catch (FactoryRegistryException cause) {
            throw noSuchAuthority(code, cause);
        }
        return factory;
    }

    /**
     * Returns the operation authority factory for the specified {@code "AUTHORITY:NUMBER"} code.
     *
     * @param  code The code to parse.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     */
    private CoordinateOperationAuthorityFactory getCoordinateOperationAuthorityFactory(final String code)
            throws NoSuchAuthorityCodeException
    {
        final String authority = getAuthority(code);
        CoordinateOperationAuthorityFactory factory = (CoordinateOperationAuthorityFactory) // TODO: remove cast with J2SE 1.5.
                getAuthorityFactory(CoordinateOperationAuthorityFactory.class, authority);
        if (factory == null) try {
            factory = FactoryFinder.getCoordinateOperationAuthorityFactory(authority, userHints);
        } catch (FactoryRegistryException cause) {
            throw noSuchAuthority(code, cause);
        }
        return factory;
    }

    /**
     * Returns an authority factory of the given type, where {@code type} is a number ranging
     * from {@code 0} inclusive to {@value #TYPE_COUNT} exclusive. This method is used when we
     * need to invoke a method available in more than one factory type.
     *
     * @param  code The code to parse.
     * @param  type The factory type as a number from {@code 0} inclusive to {@value #TYPE_COUNT}
     *         exclusive.
     * @return The authority factory.
     * @throws NoSuchAuthorityCodeException if no authority name has been found.
     */
    private AuthorityFactory getAuthorityFactory(final String code, final int type)
            throws NoSuchAuthorityCodeException
    {
        switch (type) {
            case 0:  return getCRSAuthorityFactory(code);
            case 1:  return getDatumAuthorityFactory(code);
            case 2:  return getCSAuthorityFactory(code);
            case 3:  return getCoordinateOperationAuthorityFactory(code);
            default: throw new IllegalArgumentException(String.valueOf(type));
        }
    }

    /**
     * The types to be recognized for the {@code factories} argument in constructors. While not
     * technically necessary, we keep this array consistent with {@link #getAuthorityFactory}.
     */
    private static final Class[] AUTHORIZED_TYPES = new Class[] {
        CRSAuthorityFactory.class,
        DatumAuthorityFactory.class,
        CSAuthorityFactory.class,
        CoordinateOperationAuthorityFactory.class
    };

    /**
     * The upper value (exclusive) allowed for {@link #getAuthorityFactory}. Usually
     * equals to the {@link #AUTHORIZED_TYPES} array length (but doesn't need to).
     */
    private static final int TYPE_COUNT = 4;

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
        final Set/*<String>*/ codes = new LinkedHashSet();
        final Set/*<AuthorityFactory>*/ done = new HashSet();
        done.add(this); // Safety for avoiding recursive calls.
        for (final Iterator it=FactoryFinder.getAuthorityNames().iterator(); it.hasNext();) {
            final String authority = (String) it.next();
            /*
             * Prepares a buffer with the "AUTHORITY:" part in "AUTHORITY:NUMBER".
             * We will reuse this buffer in order to prefix the authority name in
             * front of every codes.
             */
            final StringBuffer code = new StringBuffer(authority);
            code.append(separator);
            final int codeBase = code.length();
            code.append("all");
            final String dummyCode = code.toString();
            /*
             * Now scan over all factories. We will process a factory only if this particular
             * factory has not already been done in a previous iteration (some implementation
             * apply to more than one factory).
             */
            for (int i=0; i<TYPE_COUNT; i++) {
                final AuthorityFactory factory;
                try {
                    factory = getAuthorityFactory(dummyCode, i);
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
        for (int type=0; type<TYPE_COUNT; type++) {
            /*
             * Try all factories, starting with the CRS factory because it is the only one most
             * users care about. If the CRS factory doesn't know about the specified object, then
             * we will try the other factories (datum, CS, ...) before to rethrow the exception.
             */
            final AuthorityFactory factory;
            try {
                factory = getAuthorityFactory(code, type);
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
        for (int type=0; type<TYPE_COUNT; type++) {
            /*
             * Try all factories, starting with the CRS factory because it is the only one most
             * users care about. If the CRS factory doesn't know about the specified object, then
             * we will try the other factories (datum, CS, ...) before to rethrow the exception.
             */
            final AuthorityFactory factory;
            try {
                factory = getAuthorityFactory(code, type);
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

    /**
     * Returns an arbitrary {@linkplain Datum datum} from a code.
     *
     * @see #createGeodeticDatum
     * @see #createVerticalDatum
     * @see #createTemporalDatum
     */
    public Datum createDatum(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createDatum(code);
    }

    /**
     * Creates a {@linkplain EngineeringDatum engineering datum} from a code.
     *
     * @see #createEngineeringCRS
     */
    public EngineeringDatum createEngineeringDatum(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createEngineeringDatum(code);
    }

    /**
     * Creates a {@linkplain ImageDatum image datum} from a code.
     *
     * @see #createImageCRS
     */
    public ImageDatum createImageDatum(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createImageDatum(code);
    }

    /**
     * Creates a {@linkplain VerticalDatum vertical datum} from a code.
     *
     * @see #createVerticalCRS
     */
    public VerticalDatum createVerticalDatum(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createVerticalDatum(code);
    }

    /**
     * Creates a {@linkplain TemporalDatum temporal datum} from a code.
     *
     * @see #createTemporalCRS
     */
    public TemporalDatum createTemporalDatum(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createTemporalDatum(code);
    }

    /**
     * Returns a {@linkplain GeodeticDatum geodetic datum} from a code.
     *
     * @see #createEllipsoid
     * @see #createPrimeMeridian
     * @see #createGeographicCRS
     * @see #createProjectedCRS
     */
    public GeodeticDatum createGeodeticDatum(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createGeodeticDatum(code);
    }

    /**
     * Returns an {@linkplain Ellipsoid ellipsoid} from a code.
     *
     * @see #createGeodeticDatum
     */
    public Ellipsoid createEllipsoid(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createEllipsoid(code);
    }

    /**
     * Returns a {@linkplain PrimeMeridian prime meridian} from a code.
     *
     * @see #createGeodeticDatum
     */
    public PrimeMeridian createPrimeMeridian(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createPrimeMeridian(code);
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateSystem coordinate system} from a code.
     */
    public CoordinateSystem createCoordinateSystem(final String code) throws FactoryException {
        return getCSAuthorityFactory(code).createCoordinateSystem(code);
    }

    /**
     * Creates a cartesian coordinate system from a code.
     */
    public CartesianCS createCartesianCS(final String code) throws FactoryException {
        return getCSAuthorityFactory(code).createCartesianCS(code);
    }

    /**
     * Creates a polar coordinate system from a code.
     */
    public PolarCS createPolarCS(final String code) throws FactoryException {
        return getCSAuthorityFactory(code).createPolarCS(code);
    }

    /**
     * Creates a cylindrical coordinate system from a code.
     */
    public CylindricalCS createCylindricalCS(final String code) throws FactoryException {
        return getCSAuthorityFactory(code).createCylindricalCS(code);
    }

    /**
     * Creates a spherical coordinate system from a code.
     */
    public SphericalCS createSphericalCS(final String code) throws FactoryException {
        return getCSAuthorityFactory(code).createSphericalCS(code);
    }

    /**
     * Creates an ellipsoidal coordinate system from a code.
     */
    public EllipsoidalCS createEllipsoidalCS(final String code) throws FactoryException {
        return getCSAuthorityFactory(code).createEllipsoidalCS(code);
    }

    /**
     * Creates a vertical coordinate system from a code.
     */
    public VerticalCS createVerticalCS(final String code) throws FactoryException {
        return getCSAuthorityFactory(code).createVerticalCS(code);
    }

    /**
     * Creates a temporal coordinate system from a code.
     */
    public TimeCS createTimeCS(final String code) throws FactoryException {
        return getCSAuthorityFactory(code).createTimeCS(code);
    }

    /**
     * Returns a {@linkplain CoordinateSystemAxis coordinate system axis} from a code.
     */
    public CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        return getCSAuthorityFactory(code).createCoordinateSystemAxis(code);
    }

    /**
     * Returns an {@linkplain Unit unit} from a code.
     */
    public Unit createUnit(final String code) throws FactoryException {
        return getCSAuthorityFactory(code).createUnit(code);
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateReferenceSystem coordinate reference system}
     * from a code.
     *
     * @see #createGeographicCRS
     * @see #createProjectedCRS
     * @see #createVerticalCRS
     * @see #createTemporalCRS
     * @see #createCompoundCRS
     */
    public CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        return getCRSAuthorityFactory(code).createCoordinateReferenceSystem(code);
    }

    /**
     * Creates a 3D coordinate reference system from a code.
     */
    public CompoundCRS createCompoundCRS(final String code) throws FactoryException {
        return getCRSAuthorityFactory(code).createCompoundCRS(code);
    }

    /**
     * Creates a derived coordinate reference system from a code.
     */
    public DerivedCRS createDerivedCRS(final String code) throws FactoryException {
        return getCRSAuthorityFactory(code).createDerivedCRS(code);
    }
    
    /**
     * Creates a {@linkplain EngineeringCRS engineering coordinate reference system} from a code.
     */
    public EngineeringCRS createEngineeringCRS(final String code) throws FactoryException {
        return getCRSAuthorityFactory(code).createEngineeringCRS(code);
    }

    /**
     * Returns a {@linkplain GeographicCRS geographic coordinate reference system} from a code.
     *
     * @see #createGeodeticDatum
     */
    public GeographicCRS createGeographicCRS(final String code) throws FactoryException {
        return getCRSAuthorityFactory(code).createGeographicCRS(code);
    }

    /**
     * Returns a {@linkplain GeocentricCRS geocentric coordinate reference system} from a code.
     *
     * @see #createGeodeticDatum
     */
    public GeocentricCRS createGeocentricCRS(final String code) throws FactoryException {
        return getCRSAuthorityFactory(code).createGeocentricCRS(code);
    }

    /**
     * Creates a {@linkplain ImageCRS image coordinate reference system} from a code.
     */
    public ImageCRS createImageCRS(final String code) throws FactoryException {
        return getCRSAuthorityFactory(code).createImageCRS(code);
    }

    /**
     * Returns a {@linkplain ProjectedCRS projected coordinate reference system} from a code.
     *
     * @see #createGeodeticDatum
     */
    public ProjectedCRS createProjectedCRS(final String code) throws FactoryException {
        return getCRSAuthorityFactory(code).createProjectedCRS(code);
    }

    /**
     * Creates a {@linkplain TemporalCRS temporal coordinate reference system} from a code.
     *
     * @see #createTemporalDatum
     */
    public TemporalCRS createTemporalCRS(final String code) throws FactoryException {
        return getCRSAuthorityFactory(code).createTemporalCRS(code);
    }

    /**
     * Creates a {@linkplain VerticalCRS vertical coordinate reference system} from a code.
     *
     * @see #createVerticalDatum
     */
    public VerticalCRS createVerticalCRS(final String code) throws FactoryException {
        return getCRSAuthorityFactory(code).createVerticalCRS(code);
    }

    /**
     * Creates an operation from a single operation code. 
     */
    public CoordinateOperation createCoordinateOperation(final String code) throws FactoryException {
        return getCoordinateOperationAuthorityFactory(code).createCoordinateOperation(code);
    }

    /**
     * Creates an operation from coordinate reference system codes.
     */
    public Set/*<CoordinateOperation>*/ createFromCoordinateReferenceSystemCodes(
                                        final String sourceCode, final String targetCode)
            throws FactoryException
    {
        final String sourceAuthority = getAuthority(sourceCode);
        final String targetAuthority = getAuthority(targetCode);
        if (sourceAuthority.equals(targetAuthority)) {
            return getCoordinateOperationAuthorityFactory(sourceCode)
                   .createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
        }
        return super.createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
    }
}
