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
package org.geotools.geometry;

// J2SE direct dependencies
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.Locale;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashSet;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.spi.RegisterableService;

// OpenGIS dependencies
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.Precision;
import org.opengis.geometry.aggregate.AggregateFactory;
import org.opengis.geometry.complex.ComplexFactory;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.Factory;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.MathTransformFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.GeoTools;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.resources.Arguments;
import org.geotools.resources.LazySet;


/**
 * Defines static methods used to access the application's default geometry factory implementations.
 * 
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett (Refractions Research)
 */
public class GeometryFactoryFinder {
    /**
     * The service registry for this manager. Will be initialized only when first needed.
     */
    private static FactoryRegistry registry;

    /**
     * Do not allows any instantiation of this class.
     */
    GeometryFactoryFinder() {
        // singleton
    }

    /**
     * Returns the service registry. The registry will be created the first
     * time this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(GeometryFactoryFinder.class);
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(new Class[] {
                    Precision.class,
                    PositionFactory.class,
                    GeometryFactory.class,
                    ComplexFactory.class,
                    AggregateFactory.class,
                    PrimitiveFactory.class}));
        }
        return registry;
    }

    /**
     * Add {@linkplain GeoTools#getDefaultHints defaults hints} to the specified user hints.
     * User hints have precedence.
     * <p>
     * <b>Note:</b> In a previous version, we fetched the {@linkplain Hints#getSystemDefault
     * default hints} on a case-by-case basis instead of fetching all default hints at once.
     * But it leads to significant complication in {@link FactoryRegistry} (hints comming from
     * two different sources, which introduced new bugs when "longitude first axis order" hint
     * is set). In addition, it may leads to synchronization issue if many hints are modified
     * one by one. It is safer to get all default hints in one synchronized snapshot and lets
     * {@link FactoryRegistry} assumes that the hints map really contains every hints it need
     * to care about.
     */
    private static Hints addDefaultHints(final Hints hints) {
        final Hints completed = GeoTools.getDefaultHints();
        if (hints != null) {
            completed.add(hints);
        }
        return completed;
    }
    
    public static Precision getPrecision(Hints hints) throws FactoryRegistryException {
        hints = addDefaultHints(hints);
        return (Precision) getServiceRegistry().getServiceProvider( Precision.class, null, hints, Hints.PRECISION );
    }
    
    public static PositionFactory getPositionFactory(CoordinateReferenceSystem crs, Hints hints) throws FactoryRegistryException {
        hints = addDefaultHints(hints);
        hints.put( Hints.CRS, crs );
        return (PositionFactory) getServiceRegistry().getServiceProvider( PositionFactory.class, null, hints, Hints.POSITION_FACTORY );
    }
    /**
     * An implementation of {@link GeometryFactory} for the provided crs.
     * <p>
     * The implementation returned is based on the provided hints. If no implementation matches, a
     * new one is created if possible or an exception is thrown otherwise. If more than one
     * implementation is registered and an {@linkplain #setVendorOrdering ordering is set}, then
     * the preferred implementation is returned. Otherwise an arbitrary one is selected.
     * 
     * @param crs CoordianteReferenceSystem for the resulting GeometryFactory
     * @param hints An optional map of hints, or {@code null} if none.
     * @return The first datum factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link DatumFactory} interface.
     */
    public static GeometryFactory getGeometryFactory(CoordinateReferenceSystem crs, Hints hints) throws FactoryRegistryException {
        hints = addDefaultHints(hints);
        hints.put( Hints.CRS, crs );
        return (GeometryFactory) getServiceRegistry().getServiceProvider( GeometryFactory.class, null, hints, Hints.GEOMETRY_FACTORY );
    }
    
}
