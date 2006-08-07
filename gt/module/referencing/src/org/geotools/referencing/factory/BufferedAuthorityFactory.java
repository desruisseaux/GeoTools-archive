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
 *    
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory;

// J2SE dependencies and extensions
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.Factory;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;


/**
 * An authority factory that caches all objects created by an other factory. All
 * {@code createFoo(String)} methods first looks if a previously created object
 * exists for the given code. If such an object exists, it is returned. Otherwise,
 * the object creation is delegated to the {@linkplain AbstractAuthorityFactory authority factory}
 * specified at creation time, and the result is cached in this buffered factory. 
 * <p>
 * Objects are cached by strong references, up to the amount of objects specified at
 * construction time. If a greater amount of objects are cached, the oldest ones will
 * be retained through a {@linkplain WeakReference weak reference} instead of a strong
 * one. This means that this buffered factory will continue to returns them as long as
 * they are in use somewhere else in the Java virtual machine, but will be discarted
 * (and recreated on the fly if needed) otherwise.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BufferedAuthorityFactory extends AbstractAuthorityFactory {
    /**
     * The default value for {@link #maxStrongReferences}.
     */
    static final int DEFAULT_MAX = 20;

    /**
     * The underlying authority factory. This field may be {@code null} if this object was
     * created by the {@linkplain #BufferedAuthorityFactory(FactoryGroup,int) package protected
     * constructor}. In this case, the subclass is responsible for creating the backing store
     * when {@link DeferredAuthorityFactory#createBackingStore} is invoked.
     *
     * @see #getBackingStore
     * @see DeferredAuthorityFactory#createBackingStore
     */
    AbstractAuthorityFactory backingStore;

    /**
     * The pool of cached objects.
     */
    private final LinkedHashMap pool = new LinkedHashMap(32, 0.75f, true);

    /**
     * The maximum number of objects to keep by strong reference. If a greater amount of
     * objects are created, then the strong references for the oldest ones are replaced by
     * weak references.
     */
    private final int maxStrongReferences;

    /**
     * Constructs an instance wrapping the specified factory with a default number
     * of entries to keep by strong reference.
     * <p>
     * This constructor is protected because subclasses must declare which of the
     * {@link DatumAuthorityFactory}, {@link CSAuthorityFactory}, {@link CRSAuthorityFactory}
     * and {@link CoordinateOperationAuthorityFactory} interfaces they choose to implement.
     *
     * @param factory The factory to cache. Can not be {@code null}.
     */
    protected BufferedAuthorityFactory(final AbstractAuthorityFactory factory) {
        this(factory, DEFAULT_MAX);
    }

    /**
     * Constructs an instance wrapping the specified factory. The {@code maxStrongReferences}
     * argument specify the maximum number of objects to keep by strong reference. If a greater
     * amount of objects are created, then the strong references for the oldest ones are replaced
     * by weak references.
     * <p>
     * This constructor is protected because subclasses must declare which of the
     * {@link DatumAuthorityFactory}, {@link CSAuthorityFactory}, {@link CRSAuthorityFactory}
     * and {@link CoordinateOperationAuthorityFactory} interfaces they choose to implement.
     *
     * @param factory The factory to cache. Can not be {@code null}.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     */
    protected BufferedAuthorityFactory(AbstractAuthorityFactory factory,
                                       final int maxStrongReferences)
    {
        super(factory.getPriority());
        while (factory instanceof BufferedAuthorityFactory) {
            factory = ((BufferedAuthorityFactory) factory).backingStore;
        }
        this.backingStore        = factory;
        this.maxStrongReferences = maxStrongReferences;
        completeHints();
    }

    /**
     * Constructs an instance without initial backing store. This constructor is for subclass
     * constructors only. Subclasses are responsible for creating an appropriate backing store
     * when the {@link DeferredAuthorityFactory#createBackingStore} method is invoked.
     *
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     *
     * @see DeferredAuthorityFactory#createBackingStore
     */
    BufferedAuthorityFactory(final int priority, final int maxStrongReferences) {
        super(priority);
        this.maxStrongReferences = maxStrongReferences;
        // completeHints() will be invoked by DeferredAuthorityFactory.getBackingStore()
    }

    /**
     * Completes the set of hints according the value currently set in this object. This method
     * is invoked by {@code BufferedAuthorityFactory} or by {@code DeferredAuthorityFactory} at
     * backing store creation time.
     *
     * DON'T FORGET to set those hints to {@code null} when {@link DeferredAuthorityFactory}
     * dispose the backing store.
     */
    final void completeHints() {
        if (backingStore instanceof DatumAuthorityFactory) {
            hints.put(Hints.DATUM_AUTHORITY_FACTORY, backingStore);
        }
        if (backingStore instanceof CSAuthorityFactory) {
            hints.put(Hints.CS_AUTHORITY_FACTORY, backingStore);
        }
        if (backingStore instanceof CRSAuthorityFactory) {
            hints.put(Hints.CRS_AUTHORITY_FACTORY, backingStore);
        }
        if (backingStore instanceof CoordinateOperationAuthorityFactory) {
            hints.put(Hints.COORDINATE_OPERATION_AUTHORITY_FACTORY, backingStore);
        }
    }

    /**
     * Returns the backing store authority factory.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the creation of backing store failed.
     */
    AbstractAuthorityFactory getBackingStore() throws FactoryException {
        if (backingStore == null) {
            throw new FactoryException(Errors.format(ErrorKeys.DISPOSED_FACTORY));
        }
        return backingStore;
    }

    /**
     * Returns {@code true} if this factory is available. The default implementation returns
     * {@code false} if no backing store were setup and
     * {@link DeferredAuthorityFactory#createBackingStore} throws an exception.
     */
    synchronized boolean isAvailable() {
        try {
            return getBackingStore().isAvailable();
        } catch (FactoryNotFoundException exception) {
            /*
             * The factory is not available. This is error may be normal; it happens
             * for example if no gt2-epsg-hsql.jar (or similar JAR) are found in the
             * classpath, which is the case for example in GeoServer 1.3. Do not log
             * any stack trace,  since stack traces suggest more serious errors than
             * what we really have here.
             */
        } catch (FactoryException exception) {
            /*
             * The factory creation failed for an other reason, which may be more
             * serious. Now it is time to log a warning with a stack trace.
             */
            final Citation   citation = getAuthority();
            final Collection   titles = citation.getAlternateTitles();
            InternationalString title = citation.getTitle();
            if (titles != null) {
                for (final Iterator it=titles.iterator(); it.hasNext();) {
                    /*
                     * Uses the longuest title instead of the main one. In Geotools
                     * implementation, the alternate title may contains usefull informations
                     * like the EPSG database version number and the database engine.
                     */
                    final InternationalString candidate = (InternationalString) it.next();
                    if (candidate.length() > title.length()) {
                        title  = candidate;
                    }
                }
            }
            final LogRecord record = Logging.format(Level.WARNING,
                    LoggingKeys.UNAVAILABLE_AUTHORITY_FACTORY_$1, title);
            record.setSourceClassName(Utilities.getShortClassName(this));
            record.setSourceMethodName("isAvailable");
            record.setThrown(exception);
            LOGGER.log(record);
        }
        return false;
    }

    /**
     * Returns the vendor responsible for creating the underlying factory implementation.
     */
    public synchronized Citation getVendor() {
        return (backingStore!=null) ? backingStore.getVendor() : super.getVendor();
    }

    /**
     * Returns the organization or party responsible for definition and maintenance of the
     * underlying database.
     */
    public synchronized Citation getAuthority() {
        return (backingStore!=null) ? backingStore.getAuthority() : null;
    }

    /**
     * Returns a description of the underlying backing store, or {@code null} if unknow.
     * This is for example the database software used for storing the data.
     *
     * @throws FactoryException if a failure occured while fetching the engine description.
     */
    public synchronized String getBackingStoreDescription() throws FactoryException {
        return getBackingStore().getBackingStoreDescription();
    }

    /**
     * Returns the set of authority codes of the given type. The {@code type}
     * argument specify the base class.
     *
     * @param  type The spatial reference objects type.
     * @return The set of authority codes for spatial reference objects of the given type.
     *         If this factory doesn't contains any object of the given type, then this method
     *         returns an {@linkplain java.util.Collections#EMPTY_SET empty set}.
     * @throws FactoryException if access to the underlying database failed.
     */
    public synchronized Set getAuthorityCodes(final Class type)
            throws FactoryException
    {
        return getBackingStore().getAuthorityCodes(type);
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
    public synchronized InternationalString getDescriptionText(final String code)
            throws FactoryException
    {
        return getBackingStore().getDescriptionText(code);
    }

    /**
     * Returns an arbitrary object from a code.
     */
    public synchronized IdentifiedObject createObject(final String code)
            throws FactoryException
    {
        final IdentifiedObject object;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof IdentifiedObject) {
            object = (IdentifiedObject) cached;
        } else {
            object = getBackingStore().createObject(code);
        }
        put(key, object);
        return object;
    }

    /**
     * Returns an arbitrary datum from a code.
     */
    public synchronized Datum createDatum(final String code)
            throws FactoryException
    {
        final Datum datum;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof Datum) {
            datum = (Datum) cached;
        } else {
            datum = getBackingStore().createDatum(code);
        }
        put(key, datum);
        return datum;
    }

    /**
     * Returns an engineering datum from a code.
     */
    public synchronized EngineeringDatum createEngineeringDatum(final String code)
            throws FactoryException
    {
        final EngineeringDatum datum;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof EngineeringDatum) {
            datum = (EngineeringDatum) cached;
        } else {
            datum = getBackingStore().createEngineeringDatum(code);
        }
        put(key, datum);
        return datum;
    }

    /**
     * Returns an image datum from a code.
     */
    public synchronized ImageDatum createImageDatum(final String code)
            throws FactoryException
    {
        final ImageDatum datum;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof ImageDatum) {
            datum = (ImageDatum) cached;
        } else {
            datum = getBackingStore().createImageDatum(code);
        }
        put(key, datum);
        return datum;
    }

    /**
     * Returns a vertical datum from a code.
     */
    public synchronized VerticalDatum createVerticalDatum(final String code)
            throws FactoryException
    {
        final VerticalDatum datum;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof VerticalDatum) {
            datum = (VerticalDatum) cached;
        } else {
            datum = getBackingStore().createVerticalDatum(code);
        }
        put(key, datum);
        return datum;
    }

    /**
     * Returns a temporal datum from a code.
     */
    public synchronized TemporalDatum createTemporalDatum(final String code)
            throws FactoryException
    {
        final TemporalDatum datum;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof TemporalDatum) {
            datum = (TemporalDatum) cached;
        } else {
            datum = getBackingStore().createTemporalDatum(code);
        }
        put(key, datum);
        return datum;
    }

    /**
     * Returns a geodetic datum from a code.
     */
    public synchronized GeodeticDatum createGeodeticDatum(final String code)
            throws FactoryException
    {
        final GeodeticDatum datum;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof GeodeticDatum) {
            datum = (GeodeticDatum) cached;
        } else {
            datum = getBackingStore().createGeodeticDatum(code);
        }
        put(key, datum);
        return datum;
    }

    /**
     * Returns an ellipsoid from a code.
     */
    public synchronized Ellipsoid createEllipsoid(final String code)
            throws FactoryException
    {
        final Ellipsoid ellipsoid;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof Ellipsoid) {
            ellipsoid = (Ellipsoid) cached;
        } else {
            ellipsoid = getBackingStore().createEllipsoid(code);
        }
        put(key, ellipsoid);
        return ellipsoid;
    }

    /**
     * Returns a prime meridian from a code.
     */
    public synchronized PrimeMeridian createPrimeMeridian(final String code)
            throws FactoryException
    {
        final PrimeMeridian meridian;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof PrimeMeridian) {
            meridian = (PrimeMeridian) cached;
        } else {
            meridian = getBackingStore().createPrimeMeridian(code);
        }
        put(key, meridian);
        return meridian;
    }

    /**
     * Returns an extent (usually an area of validity) from a code.
     */
    public synchronized Extent createExtent(final String code)
            throws FactoryException
    {
        final Extent extent;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof Extent) {
            extent = (Extent) cached;
        } else {
            extent = getBackingStore().createExtent(code);
        }
        put(key, extent);
        return extent;
    }

    /**
     * Returns an arbitrary coordinate system from a code.
     */
    public synchronized CoordinateSystem createCoordinateSystem(final String code)
            throws FactoryException
    {
        final CoordinateSystem cs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof CoordinateSystem) {
            cs = (CoordinateSystem) cached;
        } else {
            cs = getBackingStore().createCoordinateSystem(code);
        }
        put(key, cs);
        return cs;
    }

    /**
     * Returns a cartesian coordinate system from a code.
     */
    public synchronized CartesianCS createCartesianCS(final String code)
            throws FactoryException
    {
        final CartesianCS cs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof CartesianCS) {
            cs = (CartesianCS) cached;
        } else {
            cs = getBackingStore().createCartesianCS(code);
        }
        put(key, cs);
        return cs;
    }

    /**
     * Returns a polar coordinate system from a code.
     */
    public synchronized PolarCS createPolarCS(final String code)
            throws FactoryException
    {
        final PolarCS cs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof PolarCS) {
            cs = (PolarCS) cached;
        } else {
            cs = getBackingStore().createPolarCS(code);
        }
        put(key, cs);
        return cs;
    }

    /**
     * Returns a cylindrical coordinate system from a code.
     */
    public synchronized CylindricalCS createCylindricalCS(final String code)
            throws FactoryException
    {
        final CylindricalCS cs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof CylindricalCS) {
            cs = (CylindricalCS) cached;
        } else {
            cs = getBackingStore().createCylindricalCS(code);
        }
        put(key, cs);
        return cs;
    }

    /**
     * Returns a spherical coordinate system from a code.
     */
    public synchronized SphericalCS createSphericalCS(final String code)
            throws FactoryException
    {
        final SphericalCS cs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof SphericalCS) {
            cs = (SphericalCS) cached;
        } else {
            cs = getBackingStore().createSphericalCS(code);
        }
        put(key, cs);
        return cs;
    }

    /**
     * Returns an ellipsoidal coordinate system from a code.
     */
    public synchronized EllipsoidalCS createEllipsoidalCS(final String code)
            throws FactoryException
    {
        final EllipsoidalCS cs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof EllipsoidalCS) {
            cs = (EllipsoidalCS) cached;
        } else {
            cs = getBackingStore().createEllipsoidalCS(code);
        }
        put(key, cs);
        return cs;
    }

    /**
     * Returns a vertical coordinate system from a code.
     */
    public synchronized VerticalCS createVerticalCS(final String code)
            throws FactoryException
    {
        final VerticalCS cs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof VerticalCS) {
            cs = (VerticalCS) cached;
        } else {
            cs = getBackingStore().createVerticalCS(code);
        }
        put(key, cs);
        return cs;
    }

    /**
     * Returns a temporal coordinate system from a code.
     */
    public synchronized TimeCS createTimeCS(final String code)
            throws FactoryException
    {
        final TimeCS cs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof TimeCS) {
            cs = (TimeCS) cached;
        } else {
            cs = getBackingStore().createTimeCS(code);
        }
        put(key, cs);
        return cs;
    }

    /**
     * Returns a coordinate system axis from a code.
     */
    public synchronized CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        final CoordinateSystemAxis axis;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof CoordinateSystemAxis) {
            axis = (CoordinateSystemAxis) cached;
        } else {
            axis = getBackingStore().createCoordinateSystemAxis(code);
        }
        put(key, axis);
        return axis;
    }

    /**
     * Returns an unit from a code.
     */
    public synchronized Unit createUnit(final String code)
            throws FactoryException
    {
        final Unit unit;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof Unit) {
            unit = (Unit) cached;
        } else {
            unit = getBackingStore().createUnit(code);
        }
        put(key, unit);
        return unit;
    }

    /**
     * Returns an arbitrary coordinate reference system from a code.
     */
    public synchronized CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        final CoordinateReferenceSystem crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof CoordinateReferenceSystem) {
            crs = (CoordinateReferenceSystem) cached;
        } else {
            crs = getBackingStore().createCoordinateReferenceSystem(code);
        }
        put(key, crs);
        return crs;
    }

    /**
     * Returns a 3D coordinate reference system from a code.
     */
    public synchronized CompoundCRS createCompoundCRS(final String code)
            throws FactoryException
    {
        final CompoundCRS crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof CompoundCRS) {
            crs = (CompoundCRS) cached;
        } else {
            crs = getBackingStore().createCompoundCRS(code);
        }
        put(key, crs);
        return crs;
    }

    /**
     * Returns a derived coordinate reference system from a code.
     */
    public synchronized DerivedCRS createDerivedCRS(final String code)
            throws FactoryException
    {
        final DerivedCRS crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof DerivedCRS) {
            crs = (DerivedCRS) cached;
        } else {
            crs = getBackingStore().createDerivedCRS(code);
        }
        put(key, crs);
        return crs;
    }
    
    /**
     * Returns an engineering coordinate reference system from a code.
     */
    public synchronized EngineeringCRS createEngineeringCRS(final String code)
            throws FactoryException
    {
        final EngineeringCRS crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof EngineeringCRS) {
            crs = (EngineeringCRS) cached;
        } else {
            crs = getBackingStore().createEngineeringCRS(code);
        }
        put(key, crs);
        return crs;
    }

    /**
     * Returns a geographic coordinate reference system from a code.
     */
    public synchronized GeographicCRS createGeographicCRS(final String code)
            throws FactoryException
    {
        final GeographicCRS crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof GeographicCRS) {
            crs = (GeographicCRS) cached;
        } else {
            crs = getBackingStore().createGeographicCRS(code);
        }
        put(key, crs);
        return crs;
    }

    /**
     * Returns a geocentric coordinate reference system from a code.
     */
    public synchronized GeocentricCRS createGeocentricCRS(final String code)
            throws FactoryException
    {
        final GeocentricCRS crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof GeocentricCRS) {
            crs = (GeocentricCRS) cached;
        } else {
            crs = getBackingStore().createGeocentricCRS(code);
        }
        put(key, crs);
        return crs;
    }

    /**
     * Returns an image coordinate reference system from a code.
     */
    public synchronized ImageCRS createImageCRS(final String code)
            throws FactoryException
    {
        final ImageCRS crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof ImageCRS) {
            crs = (ImageCRS) cached;
        } else {
            crs = getBackingStore().createImageCRS(code);
        }
        put(key, crs);
        return crs;
    }

    /**
     * Returns a projected coordinate reference system from a code.
     */
    public synchronized ProjectedCRS createProjectedCRS(final String code)
            throws FactoryException
    {
        final ProjectedCRS crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof ProjectedCRS) {
            crs = (ProjectedCRS) cached;
        } else {
            crs = getBackingStore().createProjectedCRS(code);
        }
        put(key, crs);
        return crs;
    }

    /**
     * Returns a temporal coordinate reference system from a code.
     */
    public synchronized TemporalCRS createTemporalCRS(final String code)
            throws FactoryException
    {
        final TemporalCRS crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof TemporalCRS) {
            crs = (TemporalCRS) cached;
        } else {
            crs = getBackingStore().createTemporalCRS(code);
        }
        put(key, crs);
        return crs;
    }

    /**
     * Returns a vertical coordinate reference system from a code.
     */
    public synchronized VerticalCRS createVerticalCRS(final String code)
            throws FactoryException
    {
        final VerticalCRS crs;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof VerticalCRS) {
            crs = (VerticalCRS) cached;
        } else {
            crs = getBackingStore().createVerticalCRS(code);
        }
        put(key, crs);
        return crs;
    }

    /**
     * Returns a parameter descriptor from a code. 
     *
     * @since 2.2
     */
    public synchronized ParameterDescriptor createParameterDescriptor(final String code)
            throws FactoryException
    {
        final ParameterDescriptor parameter;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof ParameterDescriptor) {
            parameter = (ParameterDescriptor) cached;
        } else {
            parameter = getBackingStore().createParameterDescriptor(code);
        }
        put(key, parameter);
        return parameter;
    }

    /**
     * Returns an operation method from a code. 
     *
     * @since 2.2
     */
    public synchronized OperationMethod createOperationMethod(final String code)
            throws FactoryException
    {
        final OperationMethod method;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof OperationMethod) {
            method = (OperationMethod) cached;
        } else {
            method = getBackingStore().createOperationMethod(code);
        }
        put(key, method);
        return method;
    }

    /**
     * Returns an operation from a single operation code. 
     *
     * @since 2.2
     */
    public synchronized CoordinateOperation createCoordinateOperation(final String code)
            throws FactoryException
    {
        final CoordinateOperation operation;
        final String key = trimAuthority(code);
        final Object cached = get(key);
        if (cached instanceof CoordinateOperation) {
            operation = (CoordinateOperation) cached;
        } else {
            operation = getBackingStore().createCoordinateOperation(code);
        }
        put(key, operation);
        return operation;
    }

    /**
     * Returns an operation from coordinate reference system codes.
     *
     * @since 2.2
     */
    public synchronized Set/*<CoordinateOperation>*/ createFromCoordinateReferenceSystemCodes(
                        final String sourceCode, final String targetCode)
            throws FactoryException
    {
        final Set/*<CoordinateOperation>*/ operations;
        final CodePair key = new CodePair(trimAuthority(sourceCode), trimAuthority(targetCode));
        final Object cached = get(key);
        if (cached instanceof CoordinateOperation) {
            operations = (Set/*<CoordinateOperation>*/) cached;
        } else {
            operations = Collections.unmodifiableSet(getBackingStore()
                         .createFromCoordinateReferenceSystemCodes(sourceCode, targetCode));
        }
        put(key, operations);
        return operations;
    }

    /**
     * A pair of codes for operations to cache with
     * {@link #createFromCoordinateReferenceSystemCodes}.
     */
    private static final class CodePair {
        private final String source, target;

        public CodePair(final String source, final String target) {
            this.source = source;
            this.target = target;
        }

        public int hashCode() {
            int code = 0;
            if (source!=null) code  = source.hashCode();
            if (target!=null) code += target.hashCode() * 37;
            return code;
        }

        public boolean equals(final Object other) {
            if (other instanceof CodePair) {
                final CodePair that = (CodePair) other;
                return Utilities.equals(this.source, that.source) &&
                       Utilities.equals(this.target, that.target);
            }
            return false;
        }

        public String toString() {
            return source + " \u21E8 " + target;
        }
    }

    /**
     * Releases resources immediately instead of waiting for the garbage collector.
     */
    public synchronized void dispose() throws FactoryException {
        if (backingStore != null) {
            backingStore.dispose();
            backingStore = null;
        }
        pool.clear();
        super.dispose();
    }

    /**
     * Returns an object from the pool for the specified code. If the object was retained as a
     * {@linkplain Reference weak reference}, the {@link Reference#get referent} is returned.
     *
     * @todo Consider logging a message here to the finer or finest level.
     */
    private Object get(final Object key) {
        Object object = pool.get(key);
        if (object instanceof Reference) {
            object = ((Reference) object).get();
        }
        return object;
    }

    /**
     * Put an element in the pool. This method is invoked everytime a {@code createFoo(...)}
     * method is invoked, even if an object was already in the pool for the given code, for
     * the following reasons: 1) Replaces weak reference by strong reference (if applicable)
     * and 2) Alters the linked hash set order, so that this object is declared as the last
     * one used.
     */
    private void put(final Object key, final Object object) {
        pool.put(key, object);
        int toReplace = maxStrongReferences - pool.size();
        if (toReplace > 0) {
            for (final Iterator it=pool.entrySet().iterator(); it.hasNext();) {
                final Map.Entry entry = (Map.Entry) it.next();
                final Object value = entry.getValue();
                if (value instanceof Reference) {
                    if (((Reference) value).get() == null) {
                        it.remove();
                    }
                    continue;
                }
                entry.setValue(new WeakReference(value));
                if (--toReplace == 0) {
                    break;
                }
            }
        }
    }
}
