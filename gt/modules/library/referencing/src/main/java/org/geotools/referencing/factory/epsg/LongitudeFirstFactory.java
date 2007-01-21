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
package org.geotools.referencing.factory.epsg;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.referencing.factory.OrderedAxisAuthorityFactory;
import org.geotools.metadata.iso.citation.Citations;


/**
 * An EPSG authority factory using (<var>longitude</var>, <var>latitude</var>) axis order.
 * This factory wraps a {@link DefaultFactory} into an {@link OrderedAxisAuthorityFactory}
 * when first needed.
 * <p>
 * Users don't need to create explicitly an instance of this class. Instead, one can get
 * an instance using the following code:
 *
 * <blockquote><pre>
 * Hints hints = new Hints({@linkplain Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER}, Boolean.TRUE);
 * CRSAuthorityFactory factory = {@linkplain FactoryFinder}.getCRSAuthorityFactory("EPSG", hints);
 * </pre></blockquote>
 * 
 * This factory will have a {@linkplain #priority priority} lower than the
 * {@linkplain DefaultFactory default factory} priority, <u>except</u> if the
 * <code>{@value #SYSTEM_DEFAULT_KEY}</code> {@linkplain System#getProperty(String) system
 * property} is set to {@code true}. This means that when the
 * {@code FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint is not specified, the system-wide default is
 * (<var>longitude</var>, <var>latitude</var>) order if the above-cited system property is set
 * to {@code true}, and the EPSG (<var>latitude</var>, <var>longitude</var>) order otherwise.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see OrderedAxisAuthorityFactory
 * @see Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
 * @tutorial http://docs.codehaus.org/display/GEOTOOLS/The+axis+order+issue
 */
public class LongitudeFirstFactory extends DeferredAuthorityFactory implements CRSAuthorityFactory,
        CSAuthorityFactory, CoordinateOperationAuthorityFactory, DatumAuthorityFactory
{
    /*
     * Implementation note: in theory the DatumAuthorityFactory interface is useless here, since
     * "axis order" doesn't make any sense for them. However if we do not register this class for
     * the DatumAuthorityFactory as well, user will get a FactoryNotFoundException when asking for
     * a factory with the FORCE_LONGITUDE_FIRST_AXIS_ORDER hint set.
     */

    /**
     * The {@linkplain System#getProperty(String) system property} key for setting the default
     * {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint value.
     * This setting can provide a transition path for projects expecting a (<var>longitude</var>,
     * <var>latitude</var>) axis order on a system-wide level. Application developpers can set the
     * default value as below:
     *
     * <blockquote><pre>
     * System.setProperty(SYSTEM_DEFAULT_KEY, "true");
     * </pre></blockquote>
     *
     * Note that this system property applies mostly to the default EPSG factory. Most other
     * factories ({@code "CRS"}, {@code "AUTO"}, <cite>etc.</cite>) don't need this property
     * since they use (<var>longitude</var>, <var>latitude</var>) axis order by design.
     *
     * @see Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
     */
    public static final String SYSTEM_DEFAULT_KEY = "org.geotools.referencing.forceXY";

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
        super(hints, DefaultFactory.PRIORITY + relativePriority());
        put(null,  Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER);
        put(hints, Hints.FORCE_STANDARD_AXIS_DIRECTIONS);
        put(hints, Hints.FORCE_STANDARD_AXIS_UNITS);
        // Note: the above hints are also listed in FactoryFinder.UGLY_HACK
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
     * Returns the priority to use relative to the {@link DefaultFactory} priority. The default
     * priority should be lower, except if the <code>{@value #SYSTEM_DEFAULT_KEY}</code> system
     * property is set to {@code true}.
     */
    private static int relativePriority() {
        try {
            if (Boolean.getBoolean(SYSTEM_DEFAULT_KEY)) {
                return +10;
            }
        } catch (SecurityException e) {
            // Fall back on default value.
        }
        return -10;
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
     * Returns the {@link DefaultFactory} instance to be used as the backing store.
     *
     * @throws FactoryException If no {@link DefaultFactory} instance was found.
     */
    protected AbstractAuthorityFactory createBackingStore() throws FactoryException {
        final DefaultFactory factory;
        try {
            factory = (DefaultFactory) FactoryFinder.getCRSAuthorityFactory("EPSG",
                        new Hints(Hints.CRS_AUTHORITY_FACTORY, DefaultFactory.class));
        } catch (FactoryNotFoundException exception) {
            throw new org.geotools.referencing.factory.FactoryNotFoundException(exception);
        } catch (FactoryRegistryException exception) {
            throw new FactoryException(exception);
        }
        return new OrderedAxisAuthorityFactory(factory, new Hints(hints), null);
    }
}
