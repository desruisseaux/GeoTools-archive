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
import java.util.LinkedHashMap;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.CylindricalCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.PolarCS;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.ImageDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.TemporalDatum;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.Utilities;


/**
 * An authority factory that caches all objects created by an other factory. All
 * {@code createFoo(String)} methods first looks if a previously created object
 * exists for the given code. If such an object exists, it is returned. Otherwise,
 * the object creation is delegated to the {@linkplain AbstractAuthorityFactory authority factory}
 * specified at creation time, and the result is cached in this buffered factory. 
 * <br><br>
 * Objects are cached by strong references, up to the amount of objects specified at
 * construction time. If a greater amount of objects are cached, the oldest ones will
 * be retained through a {@linkplain WeakReference weak reference} instead of a strong
 * one. This means that this buffered factory will continue to returns them as long as
 * they are in use somewhere else in the Java virtual machine, but will be discarted
 * (and recreated on the fly if needed) otherwise.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BufferedAuthorityFactory extends AbstractAuthorityFactory {
    /**
     * The default value for {@link #maxStrongReferences}.
     */
    static final int DEFAULT_MAX = 20;

    /**
     * The underlying authority factory. This field may be <code>null</code> if this object was
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
     *
     * @param factory The factory to cache. Can not be <code>null</code>.
     */
    public BufferedAuthorityFactory(final AbstractAuthorityFactory factory) {
        this(factory, DEFAULT_MAX);
    }

    /**
     * Constructs an instance wrapping the specified factory. The <code>maxStrongReferences</code>
     * argument specify the maximum number of objects to keep by strong reference. If a greater
     * amount of objects are created, then the strong references for the oldest ones are replaced
     * by weak references.
     *
     * @param factory The factory to cache. Can not be <code>null</code>.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     */
    public BufferedAuthorityFactory(AbstractAuthorityFactory factory,
                                    final int maxStrongReferences)
    {
        super(factory.factories, factory.priority);
        while (factory instanceof BufferedAuthorityFactory) {
            factory = ((BufferedAuthorityFactory) factory).backingStore;
        }
        this.backingStore        = factory;
        this.maxStrongReferences = maxStrongReferences;
    }

    /**
     * Constructs an instance without initial backing store. This constructor is for subclass
     * constructors only. Subclasses are responsible for creating an appropriate backing store
     * when the {@link DeferredAuthorityFactory#createBackingStore} method is invoked.
     *
     * @param factories The factories to use.
     * @param priority The priority for this factory, as a number between
     *        {@link #MIN_PRIORITY MIN_PRIORITY} and {@link #MAX_PRIORITY MAX_PRIORITY} inclusive.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     *
     * @see DeferredAuthorityFactory#createBackingStore
     */
    BufferedAuthorityFactory(final FactoryGroup factories,
                             final int          priority,
                             final int maxStrongReferences)
    {
        super(factories, priority);
        this.maxStrongReferences = maxStrongReferences;
    }

    /**
     * Returns the backing store authority factory.
     *
     * @return The backing store to uses in {@code createXXX(...)} methods.
     * @throws FactoryException if the creation of backing store failed.
     */
    AbstractAuthorityFactory getBackingStore() throws FactoryException {
        if (backingStore == null) {
            throw new FactoryException("The factory has been disposed."); // TODO: localize.
        }
        return backingStore;
    }

    /**
     * Returns {@code true} if this factory is ready. The default implementation returns
     * {@code false} if no backing store were setup and
     * {@link DeferredAuthorityFactory#createBackingStore} throws an exception.
     *
     * @todo Localize the logging message.
     */
    synchronized boolean isReady() {
        try {
            return getBackingStore().isReady();
        } catch (FactoryException exception) {
            final Citation citation = getAuthority();
            final LogRecord record = new LogRecord(Level.FINE, "Unavailable factory: "+citation.getTitle());
            record.setSourceClassName(Utilities.getShortClassName(this));
            record.setSourceMethodName("isReady");
            record.setThrown(exception);
            LOGGER.log(record);
            return false;
        }
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
     * Returns the set of authority codes of the given type. The <code>type</code>
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
     * @return A description of the object, or <code>null</code> if the object
     *         corresponding to the specified <code>code</code> has no description.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
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
        final Object cached = get(code);
        if (cached instanceof IdentifiedObject) {
            object = (IdentifiedObject) cached;
        } else {
            object = getBackingStore().createObject(code);
        }
        put(code, object);
        return object;
    }

    /**
     * Returns an arbitrary {@linkplain Datum datum} from a code.
     */
    public synchronized Datum createDatum(final String code)
            throws FactoryException
    {
        final Datum datum;
        final Object cached = get(code);
        if (cached instanceof Datum) {
            datum = (Datum) cached;
        } else {
            datum = getBackingStore().createDatum(code);
        }
        put(code, datum);
        return datum;
    }

    /**
     * Creates a {@linkplain EngineeringDatum engineering datum} from a code.
     */
    public synchronized EngineeringDatum createEngineeringDatum(final String code)
            throws FactoryException
    {
        final EngineeringDatum datum;
        final Object cached = get(code);
        if (cached instanceof EngineeringDatum) {
            datum = (EngineeringDatum) cached;
        } else {
            datum = getBackingStore().createEngineeringDatum(code);
        }
        put(code, datum);
        return datum;
    }

    /**
     * Creates a {@linkplain ImageDatum image datum} from a code.
     */
    public synchronized ImageDatum createImageDatum(final String code)
            throws FactoryException
    {
        final ImageDatum datum;
        final Object cached = get(code);
        if (cached instanceof ImageDatum) {
            datum = (ImageDatum) cached;
        } else {
            datum = getBackingStore().createImageDatum(code);
        }
        put(code, datum);
        return datum;
    }

    /**
     * Creates a {@linkplain VerticalDatum vertical datum} from a code.
     */
    public synchronized VerticalDatum createVerticalDatum(final String code)
            throws FactoryException
    {
        final VerticalDatum datum;
        final Object cached = get(code);
        if (cached instanceof VerticalDatum) {
            datum = (VerticalDatum) cached;
        } else {
            datum = getBackingStore().createVerticalDatum(code);
        }
        put(code, datum);
        return datum;
    }

    /**
     * Creates a {@linkplain TemporalDatum temporal datum} from a code.
     */
    public synchronized TemporalDatum createTemporalDatum(final String code)
            throws FactoryException
    {
        final TemporalDatum datum;
        final Object cached = get(code);
        if (cached instanceof TemporalDatum) {
            datum = (TemporalDatum) cached;
        } else {
            datum = getBackingStore().createTemporalDatum(code);
        }
        put(code, datum);
        return datum;
    }

    /**
     * Returns a {@linkplain GeodeticDatum geodetic datum} from a code.
     */
    public synchronized GeodeticDatum createGeodeticDatum(final String code)
            throws FactoryException
    {
        final GeodeticDatum datum;
        final Object cached = get(code);
        if (cached instanceof GeodeticDatum) {
            datum = (GeodeticDatum) cached;
        } else {
            datum = getBackingStore().createGeodeticDatum(code);
        }
        put(code, datum);
        return datum;
    }

    /**
     * Returns an {@linkplain Ellipsoid ellipsoid} from a code.
     */
    public synchronized Ellipsoid createEllipsoid(final String code)
            throws FactoryException
    {
        final Ellipsoid ellipsoid;
        final Object cached = get(code);
        if (cached instanceof Ellipsoid) {
            ellipsoid = (Ellipsoid) cached;
        } else {
            ellipsoid = getBackingStore().createEllipsoid(code);
        }
        put(code, ellipsoid);
        return ellipsoid;
    }

    /**
     * Returns a {@linkplain PrimeMeridian prime meridian} from a code.
     */
    public synchronized PrimeMeridian createPrimeMeridian(final String code)
            throws FactoryException
    {
        final PrimeMeridian meridian;
        final Object cached = get(code);
        if (cached instanceof PrimeMeridian) {
            meridian = (PrimeMeridian) cached;
        } else {
            meridian = getBackingStore().createPrimeMeridian(code);
        }
        put(code, meridian);
        return meridian;
    }

    /**
     * Returns an {@linkplain Extent extent} (usually an area of validity) from a code.
     */
    public synchronized Extent createExtent(final String code)
            throws FactoryException
    {
        final Extent extent;
        final Object cached = get(code);
        if (cached instanceof Extent) {
            extent = (Extent) cached;
        } else {
            extent = getBackingStore().createExtent(code);
        }
        put(code, extent);
        return extent;
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateSystem coordinate system} from a code.
     */
    public synchronized CoordinateSystem createCoordinateSystem(final String code)
            throws FactoryException
    {
        final CoordinateSystem cs;
        final Object cached = get(code);
        if (cached instanceof CoordinateSystem) {
            cs = (CoordinateSystem) cached;
        } else {
            cs = getBackingStore().createCoordinateSystem(code);
        }
        put(code, cs);
        return cs;
    }

    /**
     * Creates a cartesian coordinate system from a code.
     */
    public synchronized CartesianCS createCartesianCS(final String code)
            throws FactoryException
    {
        final CartesianCS cs;
        final Object cached = get(code);
        if (cached instanceof CartesianCS) {
            cs = (CartesianCS) cached;
        } else {
            cs = getBackingStore().createCartesianCS(code);
        }
        put(code, cs);
        return cs;
    }

    /**
     * Creates a polar coordinate system from a code.
     */
    public synchronized PolarCS createPolarCS(final String code)
            throws FactoryException
    {
        final PolarCS cs;
        final Object cached = get(code);
        if (cached instanceof PolarCS) {
            cs = (PolarCS) cached;
        } else {
            cs = getBackingStore().createPolarCS(code);
        }
        put(code, cs);
        return cs;
    }

    /**
     * Creates a cylindrical coordinate system from a code.
     */
    public synchronized CylindricalCS createCylindricalCS(final String code)
            throws FactoryException
    {
        final CylindricalCS cs;
        final Object cached = get(code);
        if (cached instanceof CylindricalCS) {
            cs = (CylindricalCS) cached;
        } else {
            cs = getBackingStore().createCylindricalCS(code);
        }
        put(code, cs);
        return cs;
    }

    /**
     * Creates a spherical coordinate system from a code.
     */
    public synchronized SphericalCS createSphericalCS(final String code)
            throws FactoryException
    {
        final SphericalCS cs;
        final Object cached = get(code);
        if (cached instanceof SphericalCS) {
            cs = (SphericalCS) cached;
        } else {
            cs = getBackingStore().createSphericalCS(code);
        }
        put(code, cs);
        return cs;
    }

    /**
     * Creates an ellipsoidal coordinate system from a code.
     */
    public synchronized EllipsoidalCS createEllipsoidalCS(final String code)
            throws FactoryException
    {
        final EllipsoidalCS cs;
        final Object cached = get(code);
        if (cached instanceof EllipsoidalCS) {
            cs = (EllipsoidalCS) cached;
        } else {
            cs = getBackingStore().createEllipsoidalCS(code);
        }
        put(code, cs);
        return cs;
    }

    /**
     * Creates a vertical coordinate system from a code.
     */
    public synchronized VerticalCS createVerticalCS(final String code)
            throws FactoryException
    {
        final VerticalCS cs;
        final Object cached = get(code);
        if (cached instanceof VerticalCS) {
            cs = (VerticalCS) cached;
        } else {
            cs = getBackingStore().createVerticalCS(code);
        }
        put(code, cs);
        return cs;
    }

    /**
     * Creates a temporal coordinate system from a code.
     */
    public synchronized TimeCS createTimeCS(final String code)
            throws FactoryException
    {
        final TimeCS cs;
        final Object cached = get(code);
        if (cached instanceof TimeCS) {
            cs = (TimeCS) cached;
        } else {
            cs = getBackingStore().createTimeCS(code);
        }
        put(code, cs);
        return cs;
    }

    /**
     * Returns a {@linkplain CoordinateSystemAxis coordinate system axis} from a code.
     */
    public synchronized CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        final CoordinateSystemAxis axis;
        final Object cached = get(code);
        if (cached instanceof CoordinateSystemAxis) {
            axis = (CoordinateSystemAxis) cached;
        } else {
            axis = getBackingStore().createCoordinateSystemAxis(code);
        }
        put(code, axis);
        return axis;
    }

    /**
     * Returns an {@linkplain Unit unit} from a code.
     */
    public synchronized Unit createUnit(final String code)
            throws FactoryException
    {
        final Unit unit;
        final Object cached = get(code);
        if (cached instanceof Unit) {
            unit = (Unit) cached;
        } else {
            unit = getBackingStore().createUnit(code);
        }
        put(code, unit);
        return unit;
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateReferenceSystem coordinate reference system} from a code. If the
     */
    public synchronized CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        final CoordinateReferenceSystem crs;
        final Object cached = get(code);
        if (cached instanceof CoordinateReferenceSystem) {
            crs = (CoordinateReferenceSystem) cached;
        } else {
            crs = getBackingStore().createCoordinateReferenceSystem(code);
        }
        put(code, crs);
        return crs;
    }

    /**
     * Creates a 3D coordinate reference system from a code.
     */
    public synchronized CompoundCRS createCompoundCRS(final String code)
            throws FactoryException
    {
        final CompoundCRS crs;
        final Object cached = get(code);
        if (cached instanceof CompoundCRS) {
            crs = (CompoundCRS) cached;
        } else {
            crs = getBackingStore().createCompoundCRS(code);
        }
        put(code, crs);
        return crs;
    }

    /**
     * Creates a derived coordinate reference system from a code.
     */
    public synchronized DerivedCRS createDerivedCRS(final String code)
            throws FactoryException
    {
        final DerivedCRS crs;
        final Object cached = get(code);
        if (cached instanceof DerivedCRS) {
            crs = (DerivedCRS) cached;
        } else {
            crs = getBackingStore().createDerivedCRS(code);
        }
        put(code, crs);
        return crs;
    }
    
    /**
     * Create a {@linkplain EngineeringCRS engineering coordinate reference system} from a code.
     */
    public synchronized EngineeringCRS createEngineeringCRS(final String code)
            throws FactoryException
    {
        final EngineeringCRS crs;
        final Object cached = get(code);
        if (cached instanceof EngineeringCRS) {
            crs = (EngineeringCRS) cached;
        } else {
            crs = getBackingStore().createEngineeringCRS(code);
        }
        put(code, crs);
        return crs;
    }

    /**
     * Returns a {@linkplain GeographicCRS geographic coordinate reference system} from a code.
     */
    public synchronized GeographicCRS createGeographicCRS(final String code)
            throws FactoryException
    {
        final GeographicCRS crs;
        final Object cached = get(code);
        if (cached instanceof GeographicCRS) {
            crs = (GeographicCRS) cached;
        } else {
            crs = getBackingStore().createGeographicCRS(code);
        }
        put(code, crs);
        return crs;
    }

    /**
     * Returns a {@linkplain GeocentricCRS geocentric coordinate reference system} from a code.
     */
    public synchronized GeocentricCRS createGeocentricCRS(final String code)
            throws FactoryException
    {
        final GeocentricCRS crs;
        final Object cached = get(code);
        if (cached instanceof GeocentricCRS) {
            crs = (GeocentricCRS) cached;
        } else {
            crs = getBackingStore().createGeocentricCRS(code);
        }
        put(code, crs);
        return crs;
    }

    /**
     * Create a {@linkplain ImageCRS image coordinate reference system} from a code.
     */
    public synchronized ImageCRS createImageCRS(final String code)
            throws FactoryException
    {
        final ImageCRS crs;
        final Object cached = get(code);
        if (cached instanceof ImageCRS) {
            crs = (ImageCRS) cached;
        } else {
            crs = getBackingStore().createImageCRS(code);
        }
        put(code, crs);
        return crs;
    }

    /**
     * Returns a {@linkplain ProjectedCRS projected coordinate reference system} from a code.
     */
    public synchronized ProjectedCRS createProjectedCRS(final String code)
            throws FactoryException
    {
        final ProjectedCRS crs;
        final Object cached = get(code);
        if (cached instanceof ProjectedCRS) {
            crs = (ProjectedCRS) cached;
        } else {
            crs = getBackingStore().createProjectedCRS(code);
        }
        put(code, crs);
        return crs;
    }

    /**
     * Create a {@linkplain TemporalCRS temporal coordinate reference system} from a code.
     */
    public synchronized TemporalCRS createTemporalCRS(final String code)
            throws FactoryException
    {
        final TemporalCRS crs;
        final Object cached = get(code);
        if (cached instanceof TemporalCRS) {
            crs = (TemporalCRS) cached;
        } else {
            crs = getBackingStore().createTemporalCRS(code);
        }
        put(code, crs);
        return crs;
    }

    /**
     * Create a {@linkplain VerticalCRS vertical coordinate reference system} from a code.
     */
    public synchronized VerticalCRS createVerticalCRS(final String code)
            throws FactoryException
    {
        final VerticalCRS crs;
        final Object cached = get(code);
        if (cached instanceof VerticalCRS) {
            crs = (VerticalCRS) cached;
        } else {
            crs = getBackingStore().createVerticalCRS(code);
        }
        put(code, crs);
        return crs;
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
    private Object get(final String code) {
        Object object = pool.get(code);
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
    private void put(final String code, final Object object) {
        pool.put(code, object);
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
