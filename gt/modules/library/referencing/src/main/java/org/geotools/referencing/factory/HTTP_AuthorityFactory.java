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
 */
package org.geotools.referencing.factory;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.util.GenericName;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * Wraps {@linkplain AllAuthoritiesFactory all factories} in a {@code "http://www.opengis.net/"}
 * name space. An exemple of complete URL is {@code "http://www.opengis.net/gml/srs/epsg.xml#4326"}.
 * <p>
 * Implementation note: this class requires some cooperation from the
 * {@link AllAuthoritiesFactory#getSeparator} method, since the separator is not the usual
 * {@value org.geotools.util.GenericName#DEFAULT_SEPARATOR} character.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class HTTP_AuthorityFactory extends AuthorityFactoryAdapter implements CRSAuthorityFactory,
        CSAuthorityFactory, DatumAuthorityFactory, CoordinateOperationAuthorityFactory
{
    /**
     * The base URL, which is {@value}.
     */
    public static final String BASE_URL = "http://www.opengis.net/gml/srs/";

    /**
     * Creates a default wrapper.
     */
    public HTTP_AuthorityFactory() {
        this(new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.FALSE));
    }

    /**
     * Creates a wrapper using the specified hints. For strict compliance with OGC
     * definition of CRS defined by URL, the supplied hints should contains at least the
     * {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint
     * with value {@link Boolean#FALSE FALSE}.
     *
     * @param hints The hints to be given to backing factories.
     */
    public HTTP_AuthorityFactory(final Hints hints) {
        this(new AllAuthoritiesFactory(hints));
    }

    /**
     * Creates a wrapper around the specified factory. The supplied factory is given unchanged
     * to the {@linkplain AuthorityFactoryAdapter#AuthorityFactoryAdapter(AuthorityFactory)
     * super class constructor}.
     */
    public HTTP_AuthorityFactory(final AllAuthoritiesFactory factory) {
        super(factory);
    }

    /**
     * Returns the authority, which contains the {@code "http://www.opengis.net"} identifier.
     */
    public Citation getAuthority() {
        return Citations.HTTP_OGC;
    }

    /**
     * Removes the URL base ({@value #BASE_URL}) from the specified code
     * before to pass it to the wrapped factories.
     *
     * @param  code The code given to this factory.
     * @return The code to give to the underlying factories.
     * @throws FactoryException if the code can't be converted.
     */
    protected String toBackingFactoryCode(String code) throws FactoryException {
        code = code.trim();
        final int length = BASE_URL.length();
        if (code.regionMatches(true, 0, BASE_URL, 0, length)) {
            code = code.substring(length);
            if (code.indexOf('/') < 0) {
                final int split = code.indexOf('#');
                if (split >= 0 && code.indexOf('#', split+1) < 0) {
                    code = code.substring(0, split).trim() + GenericName.DEFAULT_SEPARATOR +
                           code.substring(split + 1).trim();
                    return code;
                }
            }
        }
        throw new NoSuchAuthorityCodeException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                "code", code), BASE_URL, code);
    }
}
