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
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;


/**
 * Provides a URN syntax view over an existing authority factory. For example this adapter
 * can provides a view in the {@code "urn:ogc:def:crs:EPSG:6.8"} namespace for an existing
 * authority factory in the {@code "EPSG"} namespace.
 * <p>
 * All constructors are protected because this class must be subclassed in order to determine
 * which of the {@link DatumAuthorityFactory}, {@link CSAuthorityFactory} and
 * {@link CRSAuthorityFactory} interfaces to implement.
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
public class URN_AuthorityFactory extends AuthorityFactoryAdapter {
    /**
     * The begining parts of the URN, typically {@code "urn:ogc:def:"} and {@code "urn:x-ogc:def:"}.
     * All elements in the array are treated as synonymous. Those parts are up to, but do not
     * include, de type part ({@code "crs"}, {@code "cs"}, {@code "datum"}, <cite>etc.</cite>).
     * They must include a trailing (@value URN_Parser#SEPARATOR} character.
     */
    private final String[] urnBases;

    /**
     * The authority for this factory, to be returned by {@link #getAuthority}.
     */
    private final Citation authority;

    /**
     * The last code processed, or {@code null} if none.
     */
    private transient URN_Parser last;

    /**
     * Creates a wrapper around the specified factory. The supplied factory is given unchanged
     * to the {@linkplain AuthorityFactoryAdapter#AuthorityFactoryAdapter(AuthorityFactory)
     * super class constructor}.
     * <p>
     * A default authority citation will be created for this factory. The new
     * citation will be a copy of the wrapped {@code factory} citation, with all
     * {@linkplain Citation#getIdentifiers() identifiers} replaced by the following ones:
     *
     * <blockquote>
     * <var>urnBase</var>{@code :}<var>type</var>{@code :}<var>name</var>{@code :}<var>version</var>
     * </blockquote>
     *
     * where:
     * <ul>
     *   <li><var>urnBase</var> is the argument supplied to this constructor.</li>
     *   <li><var>type</var> may be {@code "crs"}, {@code "cs"}, {@code "datum"},
     *       <cite>etc</cite>.</li>
     *   <li><var>name</var> is the authority name of the wrapped {@code factory}.</li>
     *   <li><var>version</var> is inferred from the wrapped {@code factory}.</li>
     * </ul>
     *
     * @param factory   The factory to wrap.
     * @param urnBase   The begining part of the URN, typically {@code "urn:ogc:def"}.
     *                  This part is up to, but do not include, de type part ({@code "crs"},
     *                  {@code "cs"}, {@code "datum"}, <cite>etc.</cite>).
     */
    protected URN_AuthorityFactory(final AuthorityFactory factory, final String urnBase) {
        this(factory, new String[] {urnBase}, URN_Parser.getAuthority(factory, urnBase));
    }

    /**
     * Creates a wrapper around the specified factory using the authority name explicitly given.
     *
     * @param factory
     *          The factory to wrap.
     * @param urnBases
     *          The begining parts of the URN, typically {@code "urn:ogc:def:"} and
     *          {@code "urn:x-ogc:def:"}. All elements in the array are treated as synonymous.
     *          Those parts are up to, but do not include, de type part ({@code "crs"},
     *          {@code "cs"}, {@code "datum"}, <cite>etc.</cite>).
     * @param authority
     *          The authority citation for this factory.
     */
    protected URN_AuthorityFactory(final AuthorityFactory factory, String[] urnBases,
                                   final Citation authority)
    {
        super(factory);
        ensureNonNull("authority", authority);
        ensureNonNull("urnBases",  urnBases);
        this.authority = authority;
        this.urnBases = urnBases = (String[]) urnBases.clone();
        for (int i=0; i<urnBases.length; i++) {
            String urnBase = urnBases[i];
            ensureNonNull("urnBase", urnBase);
            urnBases[i] = URN_Parser.addSeparator(urnBase);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Citation getAuthority() {
        return authority;
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
            last = parser = new URN_Parser(urnBases, code);
        }
        return parser;
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

    /**
     * A URN syntax view in the {@code "urn:ogc:def"} name space for
     * {@linkplain AllAuthoritiesFactory all factories}.
     *
     * @since 2.4
     * @source $URL$
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class OGC extends URN_AuthorityFactory implements CRSAuthorityFactory,
            CSAuthorityFactory, DatumAuthorityFactory, CoordinateOperationAuthorityFactory
    {
        /**
         * Creates a default view.
         */
        public OGC() {
            this(new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.FALSE));
        }

        /**
         * Creates a view using the specified hints.
         *
         * @param hints The hints to be given to backing factories.
         */
        public OGC(final Hints hints) {
            super(new AllAuthoritiesFactory(hints),
                    new String[] {"urn:ogc:def:", "urn:x-ogc:def:"}, Citations.URN_OGC);
        }
    }
}
