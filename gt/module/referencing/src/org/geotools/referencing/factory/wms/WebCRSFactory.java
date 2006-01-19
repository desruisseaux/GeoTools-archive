/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005 Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.wms;

// J2SE dependencies and extensions
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.util.SimpleInternationalString;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.factory.AbstractAuthorityFactory;


/**
 * The factory for {@linkplain CoordinateReferenceSystem coordinate reference systems}
 * in the {@code CRS} space.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class WebCRSFactory extends AbstractAuthorityFactory {
    /**
     * The map of pre-defined CRS.
     */
    private final Map crsMap = new TreeMap();

    /**
     * Constructs a default factory for the {@code CRS} authority.
     *
     * @throws FactoryException if this constructor failed to register the default set of CRS
     *         using the default factories.
     */
    public WebCRSFactory() throws FactoryException {
        this(null);
    }

    /**
     * Constructs a factory for the {@code CRS} authority using the specified hints.
     *
     * @throws FactoryException if this constructor failed to register the default set of CRS
     *         using the factories provided in the specified hints.
     */
    public WebCRSFactory(final Hints hints) throws FactoryException {
        super(hints, NORMAL_PRIORITY);
        add(84, "WGS84", DefaultEllipsoid.WGS84      );
        add(83, "NAD83", DefaultEllipsoid.GRS80      );
        add(27, "NAD27", DefaultEllipsoid.CLARKE_1866);
    }

    /**
     * Adds a geographic CRS from the specified parameters.
     *
     * @param code      The CRS code.
     * @param name      The CRS and datum name.
     * @param ellipsoid The ellipsoid.
     *
     * @throws FactoryException if factories failed to creates the CRS.
     */
    private void add(final int       code,
                     final String    name,
                     final Ellipsoid ellipsoid)
            throws FactoryException
    {
        final Map      properties = new HashMap();
        final Citation authority  = getAuthority();
        final String   text       = String.valueOf(code);
        properties.put(IdentifiedObject.NAME_KEY, name);
        properties.put(Identifier.AUTHORITY_KEY, authority);
        final GeodeticDatum datum = factories.getDatumFactory().createGeodeticDatum(
                                    properties, ellipsoid, DefaultPrimeMeridian.GREENWICH);

        properties.put(IdentifiedObject.IDENTIFIERS_KEY, new NamedIdentifier(authority, text));
        final CoordinateReferenceSystem crs = factories.getCRSFactory().createGeographicCRS(
                                    properties, datum, DefaultEllipsoidalCS.GEODETIC_2D);
        if (crsMap.put(new Integer(code), crs) != null) {
            throw new IllegalArgumentException(text);
        }
    }

    /**
     * Returns the authority for this factory, which is {@link Citations#CRS CRS}.
     */
    public Citation getAuthority() {
        return Citations.CRS;
    }

    /**
     * Provides a complete set of the known codes provided by this authority. The returned set
     * contains only numeric identifiers like {@code "84"}, {@code "27"}, <cite>etc</cite>.
     * The authority name ({@code "CRS"}) is not included. This is consistent with the
     * {@linkplain org.geotools.referencing.factory.epsg.FactoryUsingSQL#getAuthorityCodes
     * codes returned by the EPSG factory} and avoid duplication, since the authority is the
     * same for every codes returned by this factory. It also make it easier for clients to
     * prepend whatever authority name they wish, as for example in the
     * {@linkplain org.geotools.referencing.factory.AllAuthoritiesFactory#getAuthorityCodes
     * all authorities factory}.
     */
    public Set getAuthorityCodes(final Class type) throws FactoryException {
        final Set set = new LinkedHashSet();
        for (final Iterator it=crsMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final CoordinateReferenceSystem crs = (CoordinateReferenceSystem) entry.getValue();
            if (type.isAssignableFrom(crs.getClass())) {
                final Integer code = (Integer) entry.getKey();
                set.add(String.valueOf(code));
            }
        }
        return set;
    }

    /**
     * Returns the CRS name for the given code.
     */
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        return new SimpleInternationalString(createObject(code).getName().getCode());
    }

    /**
     * Creates an object from the specified code. The default implementation delegates to
     * <code>{@linkplain #createCoordinateReferenceSystem createCoordinateReferenceSystem}(code)</code>.
     */
    public IdentifiedObject createObject(final String code) throws FactoryException {
        return createCoordinateReferenceSystem(code);
    }

    /**
     * Creates a coordinate reference system from the specified code.
     */
    public CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        final String c = trimAuthority(code);
        final int i;
        try {
            i = Integer.parseInt(c);
        } catch (NumberFormatException exception) {
            // If a number can't be parsed, then this is an invalid authority code.
            NoSuchAuthorityCodeException e = noSuchAuthorityCode(CoordinateReferenceSystem.class, code);
            e.initCause(exception);
            throw e;
        }
        final CoordinateReferenceSystem crs = (CoordinateReferenceSystem) crsMap.get(new Integer(i));
        if (crs != null) {
            return crs;
        }
        throw noSuchAuthorityCode(CoordinateReferenceSystem.class, code);
    }
}
