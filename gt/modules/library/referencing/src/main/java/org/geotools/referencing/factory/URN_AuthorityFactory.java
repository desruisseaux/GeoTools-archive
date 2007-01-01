/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

// Geotools dependencies
import org.geotools.util.Version;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;


/**
 * Wraps {@linkplain AllAuthoritiesFactory all factories} in a {@code "urn:ogc:def"} URN
 * name space. An exemple of complete URN is {@code "urn:ogc:def:crs:EPSG:6.8"}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Justin Deoliveira
 * @author Martin Desruisseaux
 *
 * @see <A HREF="https://portal.opengeospatial.org/files/?artifact_id=8814">URNs of definitions
 *      in OGC namespace</A>
 */
public class URN_AuthorityFactory extends AuthorityFactoryAdapter implements CRSAuthorityFactory,
        CSAuthorityFactory, DatumAuthorityFactory, CoordinateOperationAuthorityFactory
{
    /**
     * The backing factory. Will be used as a fallback if no object
     * is available for some specific version of an EPSG database.
     */
    private final AllAuthoritiesFactory factory;

    /**
     * The authority factories by versions. Factories will be created by
     * {@link #createVersionedFactory} when first needed.
     */
    private final SortedMap/*<Version,AbstractAuthorityFactory>*/ byVersions = new TreeMap();

    /**
     * The last code processed, or {@code null} if none.
     */
    private transient URN_Parser last;

    /**
     * Creates a default wrapper.
     */
    public URN_AuthorityFactory() {
        this(new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.FALSE));
    }

    /**
     * Creates a wrapper using the specified hints. For strict compliance with OGC definition
     * of {@code "urn:ogc:def"} namespace, the supplied hints should contains at least the
     * {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint
     * with value {@link Boolean#FALSE FALSE}.
     *
     * @param hints The hints to be given to backing factories.
     */
    public URN_AuthorityFactory(final Hints hints) {
        this(new AllAuthoritiesFactory(hints));
    }

    /**
     * Creates a wrapper around the specified factory. The supplied factory is given unchanged
     * to the {@linkplain AuthorityFactoryAdapter#AuthorityFactoryAdapter(AuthorityFactory)
     * super class constructor}.
     */
    public URN_AuthorityFactory(final AllAuthoritiesFactory factory) {
        super(factory);
        this.factory = factory;
    }

    /**
     * {@inheritDoc}
     */
    public Citation getAuthority() {
        return Citations.URN_OGC;
    }

    /**
     * Parses the specified code. For performance reason, returns the last result if applicable.
     *
     * @param  code The URN to parse.
     * @return parser The parser.
     * @throws NoSuchIdentifierException if the URN syntax is invalid.
     */
    private URN_Parser getParser(final String code) throws NoSuchIdentifierException {
        /*
         * Take a local copy of the field in order to protect against changes.
         * This avoid the need for synchronization (URN_Parsers are immutable,
         * so it doesn't matter if the 'last' reference is changed concurrently).
         */
        URN_Parser parser = last;
        if (parser == null || !parser.urn.equals(code)) {
            last = parser = new URN_Parser(code);
        }
        return parser;
    }

    /**
     * Returns an object factory for the specified code. This method invokes one of the
     * <code>get</code><var>Type</var><code>AuthorityFactory</code> methods where
     * <var>Type</var> is inferred from the code.
     *
     * @param  code The authority code given to this class.
     * @return A factory for the specified authority code (never {@code null}).
     * @throws FactoryException if no suitable factory were found.
     */
    protected AuthorityFactory getAuthorityFactory(final String code) throws FactoryException {
        if (code != null) {
            return getAuthorityFactory(code, getParser(code).type.type);
        } else {
            return super.getAuthorityFactory(code);
        }
    }

    /**
     * Returns the datum factory to use for the specified URN. If the URN contains a version
     * string, then this method will try to fetch a factory for that particular version. The
     * {@link #createVersionedFactory} method may be invoked for that purpose. If no factory
     * is provided for that specific version, then the
     * {@linkplain AuthorityFactoryAdapter#getDatumAuthorityFactory default one} is used.
     *
     * @param  code The URN given to this class.
     * @return A factory for the specified URN (never {@code null}).
     * @throws FactoryException if no datum factory is available.
     */
    protected DatumAuthorityFactory getDatumAuthorityFactory(final String code)
            throws FactoryException
    {
        if (code != null) {
            final URN_Parser parser = getParser(code);
            parser.logWarningIfTypeMismatch(DatumAuthorityFactory.class);
            final AuthorityFactory factory = getVersionedFactory(parser);
            if (factory instanceof DatumAuthorityFactory) {
                return (DatumAuthorityFactory) factory;
            }
        }
        return super.getDatumAuthorityFactory(code);
    }

    /**
     * Returns the coordinate system factory to use for the specified URN. If the URN contains a
     * version string, then this method will try to fetch a factory for that particular version.
     * The {@link #createVersionedFactory} method may be invoked for that purpose. If no factory
     * is provided for that specific version, then the
     * {@linkplain AuthorityFactoryAdapter#getCSAuthorityFactory default one} is used.
     *
     * @param  code The URN given to this class.
     * @return A factory for the specified URN (never {@code null}).
     * @throws FactoryException if no coordinate system factory is available.
     */
    protected CSAuthorityFactory getCSAuthorityFactory(final String code)
            throws FactoryException
    {
        if (code != null) {
            final URN_Parser parser = getParser(code);
            parser.logWarningIfTypeMismatch(CSAuthorityFactory.class);
            final AuthorityFactory factory = getVersionedFactory(parser);
            if (factory instanceof CSAuthorityFactory) {
                return (CSAuthorityFactory) factory;
            }
        }
        return super.getCSAuthorityFactory(code);
    }

    /**
     * Returns the coordinate reference system factory to use for the specified URN.
     * If the URN contains a version string, then this method will try to fetch a factory
     * for that particular version. The {@link #createVersionedFactory} method may be
     * invoked for that purpose. If no factory is provided for that specific version, then
     * the {@linkplain AuthorityFactoryAdapter#getCRSAuthorityFactory default one} is used.
     *
     * @param  code The URN given to this class.
     * @return A factory for the specified URN (never {@code null}).
     * @throws FactoryException if no coordinate reference system factory is available.
     */
    protected CRSAuthorityFactory getCRSAuthorityFactory(final String code)
            throws FactoryException
    {
        if (code != null) {
            final URN_Parser parser = getParser(code);
            parser.logWarningIfTypeMismatch(CRSAuthorityFactory.class);
            final AuthorityFactory factory = getVersionedFactory(parser);
            if (factory instanceof CRSAuthorityFactory) {
                return (CRSAuthorityFactory) factory;
            }
        }
        return super.getCRSAuthorityFactory(code);
    }

    /**
     * Returns the coordinate operation factory to use for the specified URN. If the URN
     * contains a version string, then this method will try to fetch a factory for that
     * particular version. The {@link #createVersionedFactory} method may be invoked for
     * that purpose. If no factory is provided for that specific version, then the
     * {@linkplain AuthorityFactoryAdapter#getCoordinateOperationAuthorityFactory default one}
     * is used.
     *
     * @param  code The URN given to this class.
     * @return A factory for the specified URN (never {@code null}).
     * @throws FactoryException if no coordinate operation factory is available.
     */
    protected CoordinateOperationAuthorityFactory getCoordinateOperationAuthorityFactory(final String code)
            throws FactoryException
    {
        if (code != null) {
            final URN_Parser parser = getParser(code);
            parser.logWarningIfTypeMismatch(CoordinateOperationAuthorityFactory.class);
            final AuthorityFactory factory = getVersionedFactory(parser);
            if (factory instanceof CoordinateOperationAuthorityFactory) {
                return (CoordinateOperationAuthorityFactory) factory;
            }
        }
        return super.getCoordinateOperationAuthorityFactory(code);
    }

    /**
     * Returns an authority factory for the specified version, or {@code null} if none.
     * This method invokes {@link #createVersionedFactory} the first time it is invoked
     * for a given version and cache the factory.
     *
     * @throws FactoryException if an error occured while creating the factory.
     */
    private AuthorityFactory getVersionedFactory(final URN_Parser parser)
            throws FactoryException
    {
        final Version version = parser.version;
        if (version == null) {
            return null;
        }
        AuthorityFactory factory;
        synchronized (byVersions) {
            factory = (AuthorityFactory) byVersions.get(version);
            if (factory == null) {
                factory = createVersionedFactory(version);
                if (factory != null) {
                    byVersions.put(version, factory);
                }
            }
        }
        return factory;
    }

    /**
     * Invoked when a factory is requested for a specific version. This method should create
     * a factory for the exact version specified by the argument, or return {@code null} if
     * no such factory is available. In the later case, this class will fallback on the factory
     * specified at {@linkplain #URN_AuthorityFactory(AuthorityFactory, String, Citation)
     * construction time}.
     *
     * @param  version The version for the factory to create.
     * @return The factory, of {@code null} if there is none for the specified version.
     * @throws FactoryException if an error occured while creating the factory.
     */
    protected AuthorityFactory createVersionedFactory(final Version version)
            throws FactoryException
    {
        final Hints hints = factory.getUserHints();
        hints.put(Hints.VERSION, version);
        final List factories = Arrays.asList(new AuthorityFactory[] {
            new AllAuthoritiesFactory(hints),
            factory
        });
        return FallbackAuthorityFactory.create(factories);
    }

    /**
     * Removes the {@code urnBase} from the specified code before to pass it to the wrapped
     * factories.
     *
     * @param  code The code given to this factory.
     * @return The code to give to the underlying factories.
     * @throws FactoryException if the code can't be converted.
     */
    protected String toBackingFactoryCode(final String code) throws FactoryException {
        return getParser(code).getAuthorityCode();
    }
}
