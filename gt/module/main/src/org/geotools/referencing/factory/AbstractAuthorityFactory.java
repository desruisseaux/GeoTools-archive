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
import java.util.Iterator;
import javax.units.Unit;
import javax.imageio.spi.ServiceRegistry;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.Factory;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSAuthorityFactory;
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
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.cs.CylindricalCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.PolarCS;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.ImageDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.TemporalDatum;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.resources.Utilities;
import org.geotools.util.NameFactory;


/**
 * Base class for authority factories. An <cite>authority</cite> is an
 * organization that maintains definitions of authority codes. An <cite>authority
 * code</cite> is a compact string defined by an authority to reference a particular
 * spatial reference object. For example the
 * <A HREF="http://www.epsg.org">European Petroleum Survey Group (EPSG)</A> maintains
 * a database of coordinate systems, and other spatial referencing objects, where each
 * object has a code number ID. For example, the EPSG code for a WGS84 Lat/Lon coordinate
 * system is '4326'.
 * <br><br>
 * The default implementation for all <code>createFoo</code> methods ultimately invokes
 * {@link #createObject}, which may be the only method that a subclass need to override.
 * However, other methods may be overriden as well for better performances.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractAuthorityFactory extends AbstractFactory
        implements DatumAuthorityFactory, CSAuthorityFactory, CRSAuthorityFactory
{
    /**
     * The underlying factories used for objects creation.
     */
    protected final FactoryGroup factories;

    /**
     * Constructs an instance using the specified set of factories.
     *
     * @param factories The factories to use.
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     */
    protected AbstractAuthorityFactory(final FactoryGroup factories, final int priority) {
        super(priority);
        this.factories = factories;
        ensureNonNull("factories", factories);
    }

    /**
     * Returns {@code true} if this factory is ready. The default implementation may
     * returns {@code false} for example if a connection to the EPSG database failed.
     *
     * @todo Consider moving this method in GeoAPI interfaces. However, we need to decide
     *       first if there is a need for some general API for discovering factory capabilities.
     */
    boolean isReady() {
        return true;
    }

    /**
     * Returns the organization or party responsible for definition and maintenance of the
     * database.
     */
    public abstract Citation getAuthority();

    /**
     * Returns a description of the underlying backing store, or {@code null} if unknow.
     * This is for example the database software used for storing the data.
     * The default implementation returns always {@code null}.
     *
     * @throws FactoryException if a failure occured while fetching the engine description.
     */
    public String getBackingStoreDescription() throws FactoryException {
        return null;
    }

    /**
     * Returns the low-level {@linkplain ObjectFactory object factory} used for
     * {@linkplain #createObject object creation}.
     *
     * @deprecated Will be removed.
     */
    public final org.opengis.referencing.ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an arbitrary object from a code. The returned object will typically be an instance
     * of {@link Datum}, {@link CoordinateSystem}, {@link CoordinateReferenceSystem} or
     * {@link CoordinateOperation}. The default implementation always throw an exception.
     * Subclasses should override this method if they are capable to automatically detect
     * the object type from its code. 
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see #createCoordinateReferenceSystem
     * @see #createDatum
     * @see #createEllipsoid
     * @see #createUnit
     */
    public IdentifiedObject createObject(final String code) throws FactoryException {
        ensureNonNull("code", code);
        throw noSuchAuthorityCode(IdentifiedObject.class, code);
    }

    /**
     * Returns an arbitrary {@linkplain Datum datum} from a code.
     * The default implementation invokes <code>{@linkplain #createObject createObject}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see #createGeodeticDatum
     * @see #createVerticalDatum
     * @see #createTemporalDatum
     */
    public Datum createDatum(final String code) throws FactoryException {
        final IdentifiedObject object = createObject(code);
        try {
            return (Datum) object;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(Datum.class, code, exception);
        }
    }

    /**
     * Creates a {@linkplain EngineeringDatum engineering datum} from a code.
     * The default implementation invokes <code>{@linkplain #createDatum createDatum}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see #createEngineeringCRS
     */
    public EngineeringDatum createEngineeringDatum(final String code) throws FactoryException {
        final Datum datum = createDatum(code);
        try {
            return (EngineeringDatum) datum;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(EngineeringDatum.class, code, exception);
        }
    }

    /**
     * Creates a {@linkplain ImageDatum image datum} from a code.
     * The default implementation invokes <code>{@linkplain #createDatum createDatum}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see #createImageCRS
     */
    public ImageDatum createImageDatum(final String code) throws FactoryException {
        final Datum datum = createDatum(code);
        try {
            return (ImageDatum) datum;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(ImageDatum.class, code, exception);
        }
    }

    /**
     * Creates a {@linkplain VerticalDatum vertical datum} from a code.
     * The default implementation invokes <code>{@linkplain #createDatum createDatum}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see #createVerticalCRS
     */
    public VerticalDatum createVerticalDatum(final String code) throws FactoryException {
        final Datum datum = createDatum(code);
        try {
            return (VerticalDatum) datum;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(VerticalDatum.class, code, exception);
        }
    }

    /**
     * Creates a {@linkplain TemporalDatum temporal datum} from a code.
     * The default implementation invokes <code>{@linkplain #createDatum createDatum}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see #createTemporalCRS
     */
    public TemporalDatum createTemporalDatum(final String code) throws FactoryException {
        final Datum datum = createDatum(code);
        try {
            return (TemporalDatum) datum;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(TemporalDatum.class, code, exception);
        }
    }

    /**
     * Returns a {@linkplain GeodeticDatum geodetic datum} from a code.
     * The default implementation invokes <code>{@linkplain #createDatum createDatum}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see #createEllipsoid
     * @see #createPrimeMeridian
     * @see #createGeographicCRS
     * @see #createProjectedCRS
     */
    public GeodeticDatum createGeodeticDatum(final String code) throws FactoryException {
        final Datum datum = createDatum(code);
        try {
            return (GeodeticDatum) datum;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(GeodeticDatum.class, code, exception);
        }
    }

    /**
     * Returns an {@linkplain Ellipsoid ellipsoid} from a code.
     * The default implementation invokes <code>{@linkplain #createObject createObject}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see #createGeodeticDatum
     */
    public Ellipsoid createEllipsoid(final String code) throws FactoryException {
        final IdentifiedObject object = createObject(code);
        try {
            return (Ellipsoid) object;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(Ellipsoid.class, code, exception);
        }
    }

    /**
     * Returns a {@linkplain PrimeMeridian prime meridian} from a code.
     * The default implementation invokes <code>{@linkplain #createObject createObject}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see #createGeodeticDatum
     */
    public PrimeMeridian createPrimeMeridian(final String code) throws FactoryException {
        final IdentifiedObject object = createObject(code);
        try {
            return (PrimeMeridian) object;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(PrimeMeridian.class, code, exception);
        }
    }

    /**
     * Returns a {@linkplain Extent extent} (usually an area of validity) from a code.
     * The default implementation invokes <code>{@linkplain #createObject createObject}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public Extent createExtent(final String code) throws FactoryException {
        final IdentifiedObject object = createObject(code);
        try {
            return (Extent) object;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(Extent.class, code, exception);
        }
    }

    /**
     * Gets the Geoid code from a WKT name. 
     * In the OGC definition of WKT horizontal datums, the geoid is
     * referenced by a quoted string, which is used as a key value.  This
     * method converts the key value string into a code recognized by this
     * authority.
     *
     * @param wkt Name of geoid defined by OGC (e.g. "European_Datum_1950").
     *
     * @see #createGeodeticDatum
     *
     * @deprecated We should try to rely on the alias mechanism available in all
     *             {@link IdentifiedObject} instead. If this method is really
     *             needed, we can reinsert it later.
     */
    public String geoidFromWktName(String wkt) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the WKT name of a Geoid. 
     * In the OGC definition of WKT horizontal datums, the geoid is
     * referenced by a quoted string, which is used as a key value.
     * This method gets the OGC WKT key value from a geoid code.
     *
     * @param geoid Code value for geoid allocated by authority.
     *
     * @see #createGeodeticDatum
     *
     * @deprecated We should try to rely on the alias mechanism available in all
     *             {@link IdentifiedObject} instead. If this method is really
     *             needed, we can reinsert it later.
     */
    public String wktFromGeoidName(String geoid) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateSystem coordinate system} from a code.
     * The default implementation invokes <code>{@linkplain #createObject createObject}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public CoordinateSystem createCoordinateSystem(final String code) throws FactoryException {
        final IdentifiedObject object = createObject(code);
        try {
            return (CoordinateSystem) object;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(CoordinateSystem.class, code, exception);
        }
    }

    /**
     * Creates a cartesian coordinate system from a code.
     * The default implementation invokes
     * <code>{@linkplain #createCoordinateSystem createCoordinateSystem}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public CartesianCS createCartesianCS(final String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        try {
            return (CartesianCS) cs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(CartesianCS.class, code, exception);
        }
    }

    /**
     * Creates a polar coordinate system from a code.
     * The default implementation invokes
     * <code>{@linkplain #createCoordinateSystem createCoordinateSystem}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public PolarCS createPolarCS(final String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        try {
            return (PolarCS) cs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(PolarCS.class, code, exception);
        }
    }

    /**
     * Creates a cylindrical coordinate system from a code.
     * The default implementation invokes
     * <code>{@linkplain #createCoordinateSystem createCoordinateSystem}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public CylindricalCS createCylindricalCS(final String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        try {
            return (CylindricalCS) cs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(CylindricalCS.class, code, exception);
        }
    }

    /**
     * Creates a spherical coordinate system from a code.
     * The default implementation invokes
     * <code>{@linkplain #createCoordinateSystem createCoordinateSystem}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public SphericalCS createSphericalCS(final String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        try {
            return (SphericalCS) cs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(SphericalCS.class, code, exception);
        }
    }

    /**
     * Creates an ellipsoidal coordinate system from a code.
     * The default implementation invokes
     * <code>{@linkplain #createCoordinateSystem createCoordinateSystem}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public EllipsoidalCS createEllipsoidalCS(final String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        try {
            return (EllipsoidalCS) cs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(EllipsoidalCS.class, code, exception);
        }
    }

    /**
     * Creates a vertical coordinate system from a code.
     * The default implementation invokes
     * <code>{@linkplain #createCoordinateSystem createCoordinateSystem}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public VerticalCS createVerticalCS(final String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        try {
            return (VerticalCS) cs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(VerticalCS.class, code, exception);
        }
    }

    /**
     * Creates a temporal coordinate system from a code.
     * The default implementation invokes
     * <code>{@linkplain #createCoordinateSystem createCoordinateSystem}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public TimeCS createTimeCS(final String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        try {
            return (TimeCS) cs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(TimeCS.class, code, exception);
        }
    }

    /**
     * Returns a {@linkplain CoordinateSystemAxis coordinate system axis} from a code.
     * The default implementation invokes <code>{@linkplain #createObject createObject}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        final IdentifiedObject object = createObject(code);
        try {
            return (CoordinateSystemAxis) object;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(CoordinateSystemAxis.class, code, exception);
        }
    }

    /**
     * Returns an {@linkplain Unit unit} from a code.
     * The default implementation invokes <code>{@linkplain #createObject createObject}(code)</code>.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public Unit createUnit(final String code) throws FactoryException {
        final IdentifiedObject object = createObject(code);
        try {
            return (Unit) object;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(Unit.class, code, exception);
        }
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateReferenceSystem coordinate reference system}
     * from a code. If the coordinate reference system type is know at compile time, it is
     * recommended to invoke the most precise method instead of this one (for example
     * <code>&nbsp;{@linkplain #createGeographicCRS createGeographicCRS}(code)&nbsp;</code>
     * instead of <code>&nbsp;createCoordinateReferenceSystem(code)&nbsp;</code> if the caller
     * know he is asking for a {@linkplain GeographicCRS geographic coordinate reference system}).
     *
     * @param code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
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
        final IdentifiedObject object = createObject(code);
        try {
            return (CoordinateReferenceSystem) object;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(CoordinateReferenceSystem.class, code, exception);
        }
    }

    /**
     * Creates a 3D coordinate reference system from a code.
     *
     * @param code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public CompoundCRS createCompoundCRS(final String code) throws FactoryException {
        final CoordinateReferenceSystem crs = createCoordinateReferenceSystem(code);
        try {
            return (CompoundCRS) crs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(CompoundCRS.class, code, exception);
        }
    }

    /**
     * Creates a derived coordinate reference system from a code.
     *
     * @param code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public DerivedCRS createDerivedCRS(final String code) throws FactoryException {
        final CoordinateReferenceSystem crs = createCoordinateReferenceSystem(code);
        try {
            return (DerivedCRS) crs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(DerivedCRS.class, code, exception);
        }
    }
    
    /**
     * Create a {@linkplain EngineeringCRS engineering coordinate reference system} from a code.
     *
     * @param code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public EngineeringCRS createEngineeringCRS(final String code) throws FactoryException {
        final CoordinateReferenceSystem crs = createCoordinateReferenceSystem(code);
        try {
            return (EngineeringCRS) crs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(EngineeringCRS.class, code, exception);
        }
    }

    /**
     * Returns a {@linkplain GeographicCRS geographic coordinate reference system} from a code.
     *
     * @param code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see org.opengis.referencing.datum.DatumAuthorityFactory#createGeodeticDatum
     */
    public GeographicCRS createGeographicCRS(final String code) throws FactoryException {
        final CoordinateReferenceSystem crs = createCoordinateReferenceSystem(code);
        try {
            return (GeographicCRS) crs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(GeographicCRS.class, code, exception);
        }
    }

    /**
     * Returns a {@linkplain GeocentricCRS geocentric coordinate reference system} from a code.
     *
     * @param code Value allocated by authority.
     * @throws FactoryException if the object creation failed.
     *
     * @see org.opengis.referencing.datum.DatumAuthorityFactory#createGeodeticDatum
     */
    public GeocentricCRS createGeocentricCRS(final String code) throws FactoryException {
        final CoordinateReferenceSystem crs = createCoordinateReferenceSystem(code);
        try {
            return (GeocentricCRS) crs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(GeocentricCRS.class, code, exception);
        }
    }

    /**
     * Create a {@linkplain ImageCRS image coordinate reference system} from a code.
     *
     * @param code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public ImageCRS createImageCRS(final String code) throws FactoryException {
        final CoordinateReferenceSystem crs = createCoordinateReferenceSystem(code);
        try {
            return (ImageCRS) crs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(ImageCRS.class, code, exception);
        }
    }

    /**
     * Returns a {@linkplain ProjectedCRS projected coordinate reference system} from a code.
     *
     * @param code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see org.opengis.referencing.datum.DatumAuthorityFactory#createGeodeticDatum
     */
    public ProjectedCRS createProjectedCRS(final String code) throws FactoryException {
        final CoordinateReferenceSystem crs = createCoordinateReferenceSystem(code);
        try {
            return (ProjectedCRS) crs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(ProjectedCRS.class, code, exception);
        }
    }

    /**
     * Create a {@linkplain TemporalCRS temporal coordinate reference system} from a code.
     *
     * @param code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see org.opengis.referencing.datum.DatumAuthorityFactory#createTemporalDatum
     */
    public TemporalCRS createTemporalCRS(final String code) throws FactoryException {
        final CoordinateReferenceSystem crs = createCoordinateReferenceSystem(code);
        try {
            return (TemporalCRS) crs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(TemporalCRS.class, code, exception);
        }
    }

    /**
     * Create a {@linkplain VerticalCRS vertical coordinate reference system} from a code.
     *
     * @param code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified <code>code</code> was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     *
     * @see org.opengis.referencing.datum.DatumAuthorityFactory#createVerticalDatum
     */
    public VerticalCRS createVerticalCRS(final String code) throws FactoryException {
        final CoordinateReferenceSystem crs = createCoordinateReferenceSystem(code);
        try {
            return (VerticalCRS) crs;
        } catch (ClassCastException exception) {
            throw noSuchAuthorityCode(VerticalCRS.class, code, exception);
        }
    }

    /**
     * Releases resources immediately instead of waiting for the garbage collector.
     * Once a factory has been disposed, further <code>create(...)</code> invocations
     * may throw a {@link FactoryException}. Disposing a previously-disposed factory,
     * however, has no effect.
     *
     * @throws FactoryException if an error occured while disposing the factory.
     */
    public void dispose() throws FactoryException {
        // To be overriden by subclasses.
    }

    /**
     * Creates an exception for an unknow authority code. This convenience method is provided
     * for implementation of <code>createXXX</code> methods.
     *
     * @param  type  The GeoAPI interface that was to be created
     *               (e.g. <code>CoordinateReferenceSystem.class</code>).
     * @param  code  The unknow authority code.
     * @param  cause The cause of this error, or <code>null</code>.
     * @return An exception initialized with an error message built
     *         from the specified informations.
     */
    private NoSuchAuthorityCodeException noSuchAuthorityCode(final Class              type,
                                                             final String             code,
                                                             final ClassCastException cause)
    {
        final NoSuchAuthorityCodeException exception = noSuchAuthorityCode(type, code);
        exception.initCause(cause);
        return exception;
    }

    /**
     * Trim the authority scope, if present. For example if this factory is an EPSG authority
     * factory and the specified code start with the "EPSG:" prefix, then the prefix is removed.
     * Otherwise, if a prefix is present but unrecognized, then an exception is thrown.
     *
     * @param  code The code to trim.
     * @return The code without the authority scope.
     * @throws NoSuchAuthorityCodeException if the specified code as a scope and the scope
     *         is not the one expected by this factory.
     *
     * @todo Localize the error message.
     */
    protected String trimAuthority(String code) throws NoSuchAuthorityCodeException {
        code = code.trim();
        final GenericName name  = NameFactory.create(code);
        final GenericName scope = name.getScope();
        if (scope == null) {
            return code;
        }
        if (CitationImpl.titleMatches(getAuthority(), scope.toString())) {
            return name.asLocalName().toString();
        }
        final InternationalString authority = getAuthority().getTitle();
        throw new NoSuchAuthorityCodeException("\"" + scope.toInternationalString() + 
                "\" is outside the scope of " + authority + "factory.", authority.toString(), code);
    }

    /**
     * Creates an exception for an unknow authority code. This convenience method is provided
     * for implementation of <code>createXXX</code> methods.
     *
     * @param  type  The GeoAPI interface that was to be created
     *               (e.g. <code>CoordinateReferenceSystem.class</code>).
     * @param  code  The unknow authority code.
     * @return An exception initialized with an error message built
     *         from the specified informations.
     *
     * @todo Localize the error message.
     */
    protected final NoSuchAuthorityCodeException noSuchAuthorityCode(final Class  type,
                                                                     final String code)
    {
        final String authority = getAuthority().getTitle().toString();
        return new NoSuchAuthorityCodeException(
                "No code \""+code+"\" from the authority \""+authority+
                "\" was found for object of type "+Utilities.getShortName(type)+".",
                authority, code);
    }

    /**
     * Called when this factory is added to the given <code>category</code> of the given
     * <code>registry</code>. The factory may already be registered under another category
     * or categories.
     * <br><br>
     * This method is invoked automatically when this factory is registered as a plugin,
     * and should not be invoked directly by the user. The default implementation iterates
     * through all services under the same category and for the same
     * {@linkplain AuthorityFactory#getAuthority authority}, and set the ordering
     * according the priority given at construction time.
     *
     * @param registry a <code>ServiceRegistry</code> where this factory has been registered.
     * @param category a <code>Class</code> object indicating the registry category under which
     *                 this object has been registered.
     *
     * @see #MINIMUM_PRIORITY
     * @see #MAXIMUM_PRIORITY
     * @see org.geotools.referencing.FactoryFinder
     */
    public void onRegistration(final ServiceRegistry registry, final Class category) {
        for (final Iterator it=registry.getServiceProviders(category, false); it.hasNext();) {
            final Object provider = it.next();
            if (provider instanceof AbstractAuthorityFactory) {
                final AbstractAuthorityFactory factory = (AbstractAuthorityFactory) provider;
                final Citation authority = getAuthority();
                if (authority!=null && authority.equals(factory.getAuthority())) {
                    if (priority > factory.priority) {
                        registry.setOrdering(category, this, factory);
                    } else if (priority < factory.priority) {
                        registry.setOrdering(category, factory, this);
                    }
                }
            }
        }
    }
}
