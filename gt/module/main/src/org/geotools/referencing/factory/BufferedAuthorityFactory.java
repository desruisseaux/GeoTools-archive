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
import javax.units.Unit;

// OpenGIS dependencies
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
     * The underlying factory.
     */
    private final AbstractAuthorityFactory factory;

    /**
     * The pool of cached objects.
     */
    private final LinkedHashMap pool = new LinkedHashMap();

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
     * @param factory The factory to cache.
     */
    public BufferedAuthorityFactory(final AbstractAuthorityFactory factory) {
        this(factory, 20);
    }

    /**
     * Constructs an instance wrapping the specified factory. The <code>maxStrongReferences</code>
     * argument specify the maximum number of objects to keep by strong reference. If a greater
     * amount of objects are created, then the strong references for the oldest ones are replaced
     * by weak references.
     *
     * @param factory The factory to cache.
     * @param maxStrongReferences The maximum number of objects to keep by strong reference.
     */
    public BufferedAuthorityFactory(AbstractAuthorityFactory factory,
                                    final int maxStrongReferences)
    {
        super(factory.factories, factory.priority);
        while (factory instanceof BufferedAuthorityFactory) {
            factory = ((BufferedAuthorityFactory) factory).factory;
        }
        this.factory = factory;
        this.maxStrongReferences = maxStrongReferences;
    }

    /**
     * Returns the vendor responsible for creating the underlying factory implementation.
     */
    public Citation getVendor() {
        return factory.getVendor();
    }

    /**
     * Returns the organization or party responsible for definition and maintenance of the
     * underlying database.
     */
    public Citation getAuthority() {
        return factory.getAuthority();
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
    public Set getAuthorityCodes(final Class type) throws FactoryException {
        return factory.getAuthorityCodes(type);
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
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        return factory.getDescriptionText(code);
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
            object = factory.createObject(code);
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
            datum = factory.createDatum(code);
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
            datum = factory.createEngineeringDatum(code);
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
            datum = factory.createImageDatum(code);
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
            datum = factory.createVerticalDatum(code);
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
            datum = factory.createTemporalDatum(code);
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
            datum = factory.createGeodeticDatum(code);
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
            ellipsoid = factory.createEllipsoid(code);
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
            meridian = factory.createPrimeMeridian(code);
        }
        put(code, meridian);
        return meridian;
    }

    /**
     * Gets the Geoid code from a WKT name.
     *
     * @deprecated May be replaced by an alias mechanism.
     */
    public String geoidFromWktName(final String wkt) {
        return factory.geoidFromWktName(wkt);
    }

    /**
     * Gets the WKT name of a Geoid. 
     *
     * @deprecated May be replaced by an alias mechanism.
     */
    public String wktFromGeoidName(final String geoid) {
        return factory.wktFromGeoidName(geoid);
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
            cs = factory.createCoordinateSystem(code);
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
            cs = factory.createCartesianCS(code);
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
            cs = factory.createPolarCS(code);
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
            cs = factory.createCylindricalCS(code);
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
            cs = factory.createSphericalCS(code);
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
            cs = factory.createEllipsoidalCS(code);
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
            cs = factory.createVerticalCS(code);
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
            cs = factory.createTimeCS(code);
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
            axis = factory.createCoordinateSystemAxis(code);
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
            unit = factory.createUnit(code);
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
            crs = factory.createCoordinateReferenceSystem(code);
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
            crs = factory.createCompoundCRS(code);
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
            crs = factory.createDerivedCRS(code);
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
            crs = factory.createEngineeringCRS(code);
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
            crs = factory.createGeographicCRS(code);
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
            crs = factory.createGeocentricCRS(code);
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
            crs = factory.createImageCRS(code);
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
            crs = factory.createProjectedCRS(code);
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
            crs = factory.createTemporalCRS(code);
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
            crs = factory.createVerticalCRS(code);
        }
        put(code, crs);
        return crs;
    }

    /**
     * Releases resources immediately instead of waiting for the garbage collector.
     */
    public void dispose() throws FactoryException {
        factory.dispose();
        super.dispose();
    }

    /**
     * Returns an object from the pool for the specified code. If the object was retained as a
     * {@linkplain Reference weak reference}, the {@link Reference#get referent} is returned.
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
