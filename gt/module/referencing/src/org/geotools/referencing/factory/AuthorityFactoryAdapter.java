/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    replace it under the terms of the GNU Lesser General Public
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
package org.geotools.referencing.factory;

// J2SE dependencies and extensions
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Iterator;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.feature.BackingStoreException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.operation.*;

// Geotools dependencies
import org.geotools.util.WeakValueHashMap;
import org.geotools.resources.Utilities;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * An authority factory which delegates {@linkplain CoordinateReferenceSystem CRS},
 * {@linkplain CoordinateSystem CS} or {@linkplain Datum datum} objects creation to
 * some other factory implementations. This adapter may be used as a bridge between
 * different implementations that do not rely exclusively on GeoAPI interfaces (not
 * recommanded, but this case happens sometime). This adapter may also be used when
 * some slight changes to the created objects are desired.
 * <p>
 * This class provides a set of {@code replace(...)} methods to be overriden by subclasses in order
 * to replace some {@linkplain CoordinateReferenceSystem CRS}, {@linkplain CoordinateSystem CS} or
 * {@linkplain Datum datum} objects by other ones. The replacement rules are determined by the
 * subclass being used. For example the {@link OrderedAxisAuthorityFactory} subclass can replace
 * {@linkplain CoordinateSystem coordinate systems} using (<var>latitude</var>, <var>longitude</var>)
 * axis order by coordinate systems using (<var>longitude</var>, <var>latitude</var>) axis order.
 * <p>
 * In the case that a {@code replace} method returns an object with a different structure than the
 * original one (for example the axis order change described above), then this modified factory
 * should <strong>not</strong> declares the same {@linkplain #getAuthority authority code} than
 * the {@linkplain #crsFactory original factory}. For example an EPSG factory with all coordinate
 * systems forced to (<var>longitude</var>, <var>latitude</var>) axis order is not an EPSG
 * authority factory anymore.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Use generic types for all {@code replace(...)} methods when we will be allowed
 *       to compile for J2SE 1.5, and remove casts in all {@code createXXX(...)} methods.
 */
public class AuthorityFactoryAdapter extends AbstractAuthorityFactory {
    /**
     * The underlying {@linkplain Datum datum} authority factory.
     */
    protected final DatumAuthorityFactory datumFactory;

    /**
     * The underlying {@linkplain CoordinateSystem coordinate system} authority factory.
     */
    protected final CSAuthorityFactory csFactory;

    /**
     * The underlying {@linkplain CoordinateReferenceSystem coordinate reference system}
     * authority factory.
     */
    protected final CRSAuthorityFactory crsFactory;

    /**
     * The underlying {@linkplain CoordinateOperation coordinate operation} authority factory.
     */
    protected final CoordinateOperationAuthorityFactory opFactory;

    /**
     * A pool of modified objects created up to date. Will be created only when
     * first needed.
     */
    private transient Map pool;

    /**
     * Creates a factory wrapping the specified one. The priority level will be equals to the
     * specified {@linkplain AbstractAuthorityFactory#priority factory's priority} plus one.
     */
    public AuthorityFactoryAdapter(final AbstractAuthorityFactory factory) {
        super(factory.factories, factory.priority + 1);
        this.datumFactory = factory;
        this.   csFactory = factory;
        this.  crsFactory = factory;
        this.   opFactory = factory;
    }

    /**
     * Creates a factory wrapping the specified ones.
     *
     * @param datumFactory The {@linkplain Datum datum} authority factory.
     * @param csFactory    The {@linkplain CoordinateSystem coordinate system} authority factory.
     * @param crsFactory   The {@linkplain CoordinateReferenceSystem coordinate reference system}
     *                     authority factory.
     * @param opFactory    The {@linkplain CoordinateOperation coordinate operation} authority factory.
     * @param factories    The low-level factories to use.
     * @param priority     The priority for this factory, as a number between
     *                     {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *                     {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     */
    public AuthorityFactoryAdapter(final DatumAuthorityFactory               datumFactory,
                                   final CSAuthorityFactory                     csFactory,
                                   final CRSAuthorityFactory                   crsFactory,
                                   final CoordinateOperationAuthorityFactory    opFactory,
                                   final FactoryGroup                           factories,
                                   final int                                     priority)
    {
        super(factories, priority);
        this.datumFactory = datumFactory;
        this.   csFactory =    csFactory;
        this.  crsFactory =   crsFactory;
        this.   opFactory =    opFactory;
    }

    /**
     * Cache a modified object in the internal pool.
     * @todo Use generic types once we will be allowed to compile for J2SE 1.5.
     */
    final void cache(final IdentifiedObject original, final IdentifiedObject modified) {
        if (pool == null) {
            pool = new WeakValueHashMap();
        }
        pool.put(original, modified);
    }

    /**
     * Returns a modified object from the pool.
     * @todo Use generic types once we will be allowed to compile for J2SE 1.5.
     */
    final IdentifiedObject getFromCache(final IdentifiedObject original) {
        return (pool!=null) ? (IdentifiedObject) pool.get(original) : null;
    }

    /**
     * Replaces (if needed) the specified datum by a new one. The default implementation returns
     * the datum unchanged. Subclasses should override this method if some datum replacements are
     * desired.
     *
     * @param  datum The datum to replace.
     * @return The new datum, or {@code datum} if no change were needed.
     * @throws FactoryException if an error occured while creating the new datum.
     */
    protected Datum replace(final Datum datum) throws FactoryException {
        return datum;
    }

    /**
     * Replaces (if needed) the specified coordinate system by a new one. The default
     * implementation returns the coordinate system unchanged. Subclasses should override
     * this method if some coordinate system replacements are desired.
     *
     * @param  cs The coordinate system to replace.
     * @return A new coordinate system, or {@code cs} if no change were needed.
     * @throws FactoryException if an error occured while creating the new coordinate system.
     */
    protected CoordinateSystem replace(final CoordinateSystem cs) throws FactoryException {
        return cs;
    }

    /**
     * Replaces (if needed) the specified coordinate reference system. The default
     * implementation checks if there is a {@linplain #replace(Datum) datum replacement} or a
     * {@linplain #replace(CoordinateSystem) coordinate system replacement}. If there is at
     * least one of those, then this method returns a new coordinate reference system using
     * the new datum and coordinate system.
     *
     * @param  crs The coordinate reference system to replace.
     * @return A new CRS, or {@code crs} if no change were needed.
     * @throws FactoryException if an error occured while creating the new CRS object.
     */
    protected CoordinateReferenceSystem replace(final CoordinateReferenceSystem crs)
            throws FactoryException
    {
        CoordinateReferenceSystem candidate = (CoordinateReferenceSystem) getFromCache(crs);
        if (candidate != null) {
            return candidate;
        }
        /*
         * Gets the replaced coordinate system and datum, and checks if there is any change.
         */
        final CoordinateSystem oldCS = crs.getCoordinateSystem();
        final CoordinateSystem    cs = replace(oldCS);
        final Datum oldDatum, datum;
        if (crs instanceof SingleCRS) {
            oldDatum = ((SingleCRS) crs).getDatum();
            datum = replace(oldDatum);
        } else {
            datum = oldDatum = null;
        }
        final boolean sameCS = Utilities.equals(cs, oldCS) && Utilities.equals(datum, oldDatum);
        /*
         * Creates a new coordinate reference system using the same properties than the
         * original CRS, except for the coordinate system, datum and authority code.
         */
        if (crs instanceof GeneralDerivedCRS) {
            final GeneralDerivedCRS         derivedCRS = (GeneralDerivedCRS) crs;
            final CoordinateReferenceSystem oldBaseCRS = derivedCRS.getBaseCRS();
            final CoordinateReferenceSystem    baseCRS = replace(oldBaseCRS);
            if (sameCS && Utilities.equals(baseCRS, oldBaseCRS)) {
                return crs;
            }
            final Map properties = getProperties(crs);
            final CRSFactory crsFactory = factories.getCRSFactory();
            Conversion fromBase = derivedCRS.getConversionFromBase();
            fromBase = new DefiningConversion(getProperties(fromBase),
                           fromBase.getMethod(), fromBase.getParameterValues());
            if (crs instanceof ProjectedCRS) {
                candidate = factories.createProjectedCRS(properties, (GeographicCRS) baseCRS,
                                                    fromBase, (CartesianCS) cs);
            } else {
                // TODO: Need a createDerivedCRS method.
                throw new FactoryException(Errors.format(ErrorKeys.UNSUPPORTED_CRS_$1,
                                           crs.getName().getCode()));
            }
        } else if (sameCS) {
            return crs;
        } else {
            final Map properties = getProperties(crs);
            final CRSFactory crsFactory = factories.getCRSFactory();
            if (crs instanceof GeographicCRS) {
                candidate = crsFactory.createGeographicCRS(properties, (GeodeticDatum) datum, (EllipsoidalCS) cs);
            } else if (crs instanceof GeocentricCRS) {
                final GeodeticDatum gd = (GeodeticDatum) datum;
                if (cs instanceof CartesianCS) {
                    candidate = crsFactory.createGeocentricCRS(properties, gd, (CartesianCS) cs);
                } else {
                    candidate = crsFactory.createGeocentricCRS(properties, gd, (SphericalCS) cs);
                }
            } else if (crs instanceof VerticalCRS) {
                candidate = crsFactory.createVerticalCRS(properties, (VerticalDatum) datum, (VerticalCS) cs);
            } else if (crs instanceof TemporalCRS) {
                candidate = crsFactory.createTemporalCRS(properties, (TemporalDatum) datum, (TimeCS) cs);
            } else if (crs instanceof ImageCRS) {
                candidate = crsFactory.createImageCRS(properties, (ImageDatum) datum, (AffineCS) cs);
            } else if (crs instanceof EngineeringCRS) {
                candidate = crsFactory.createEngineeringCRS(properties, (EngineeringDatum) datum, cs);
            } else if (crs instanceof CompoundCRS) {
                final List/*<CoordinateReferenceSystem>*/ elements =
                        ((CompoundCRS) crs).getCoordinateReferenceSystems();
                final CoordinateReferenceSystem[] modified = new CoordinateReferenceSystem[elements.size()];
                for (int i=0; i<modified.length; i++) {
                    modified[i] = replace((CoordinateReferenceSystem) elements.get(i));
                }
                candidate = crsFactory.createCompoundCRS(properties, modified);
            } else {
                throw new FactoryException(Errors.format(ErrorKeys.UNSUPPORTED_CRS_$1,
                                                         crs.getName().getCode()));
            }
        }
        cache(crs, candidate);
        return candidate;
    }

    /**
     * Replaces (if needed) the specified coordinate operation. The default implementation checks
     * if there is a source or target {@linplain #replace(CRS) CRS replacement}. If there is at
     * least one of those, then this method returns a new coordinate operation using the new CRS.
     *
     * @param  operation The coordinate operation to replace.
     * @return A new operation, or {@code operation} if no change were needed.
     * @throws FactoryException if an error occured while creating the new operation object.
     */
    protected CoordinateOperation replace(final CoordinateOperation operation)
            throws FactoryException
    {
        CoordinateOperation candidate = (CoordinateOperation) getFromCache(operation);
        if (candidate == null) {
            final CoordinateReferenceSystem oldSrcCRS = operation.getSourceCRS();
            final CoordinateReferenceSystem oldTgtCRS = operation.getTargetCRS();
            final CoordinateReferenceSystem sourceCRS = replace(oldSrcCRS);
            final CoordinateReferenceSystem targetCRS = replace(oldTgtCRS);
            if (Utilities.equals(oldSrcCRS, sourceCRS) && Utilities.equals(oldTgtCRS, targetCRS)) {
                return operation;
            }
            candidate = factories.getCoordinateOperationFactory().createOperation(sourceCRS, targetCRS);
            cache(operation, candidate);
        }
        return candidate;
    }

    /**
     * Returns the properties to be given to an object replacing an original one.
     *
     * @param  object The original object.
     * @return The properties to be given to the object created as a substitute of {@code object}.
     */
    final Map getProperties(final IdentifiedObject object) {
        return AbstractIdentifiedObject.getProperties(object, getAuthority());
    }

    /**
     * Returns one of the underlying factories as an instance of the Geotools implementation.
     * If there is none of them, then returns {@code null} or throws an exception if {@code caller}
     * is not null.
     */
    private AbstractAuthorityFactory getFactory(final String caller) throws FactoryException {
        if (crsFactory instanceof AbstractAuthorityFactory) {
            return (AbstractAuthorityFactory) crsFactory;
        }
        if (csFactory instanceof AbstractAuthorityFactory) {
            return (AbstractAuthorityFactory) csFactory;
        }
        if (datumFactory instanceof AbstractAuthorityFactory) {
            return (AbstractAuthorityFactory) datumFactory;
        }
        if (opFactory instanceof AbstractAuthorityFactory) {
            return (AbstractAuthorityFactory) opFactory;
        }
        if (caller == null) {
            return null;
        }
        throw new FactoryException(Errors.format(
                    ErrorKeys.GEOTOOLS_EXTENSION_REQUIRED_$1, caller));
    }

    /**
     * Returns a description of the underlying backing store, or {@code null} if unknow.
     *
     * @throws FactoryException if a failure occured while fetching the engine description.
     */
    public String getBackingStoreDescription() throws FactoryException {
        final AbstractAuthorityFactory factory = getFactory(null);
        return (factory!=null) ? factory.getBackingStoreDescription() : null;
    }

    /**
     * Returns the organization or party responsible for definition and maintenance of the
     * database. The default implementation delegates to the {@linkplain #crsFactory underlying
     * CRS factory} with no change. Subclasses should override this method if at least one
     * {@code replace(...)} methods performs a structural change, for example changing axis
     * order from (<var>latitude</var>, <var>longitude</var>) to (<var>longitude</var>,
     * <var>latitude</var>).
     */
    public Citation getAuthority() {
        return crsFactory.getAuthority();
    }

    /**
     * Returns the set of authority code for the specified type. The default implementation
     * delegates to the {@linkplain #crsFactory underlying CRS factory} with no change.
     */
    public Set/*<String>*/ getAuthorityCodes(Class type) throws FactoryException {
        return crsFactory.getAuthorityCodes(type);
    }

    /**
     * Returns a description for the object identified by the specified code. The default
     * implementation delegates to the {@linkplain #crsFactory underlying CRS factory} with
     * no change.
     */
    public InternationalString getDescriptionText(String code) throws FactoryException {
        return crsFactory.getDescriptionText(code);
    }

    /**
     * Returns an arbitrary object from a code. The default implementation first creates an object
     * using the {@linkplain #crsFactory underlying CRS factory}, and then modifies it using one of
     * the {@code replace(...)} methods, if applicable.
     */
    public IdentifiedObject createObject(final String code) throws FactoryException {
        IdentifiedObject object = crsFactory.createObject(code);
        if (object instanceof Datum) {
            object = replace((Datum) object);
        } else if (object instanceof CoordinateSystem) {
            object = replace((CoordinateSystem) object);
        } else if (object instanceof CoordinateReferenceSystem) {
            object = replace((CoordinateReferenceSystem) object);
        }
        return object;
    }

    /**
     * Returns an arbitrary {@linkplain Datum datum} from a code.
     * The default implementation first invokes the same method from the {@linkplain #datumFactory
     * underlying datum factory}, and next invokes {@link #replace(Datum) replace}.
     */
    public Datum createDatum(final String code) throws FactoryException {
        return replace(datumFactory.createDatum(code));
    }

    /**
     * Creates a {@linkplain EngineeringDatum engineering datum} from a code.
     * The default implementation first invokes the same method from the {@linkplain #datumFactory
     * underlying datum factory}, and next invokes {@link #replace(Datum) replace}.
     */
    public EngineeringDatum createEngineeringDatum(final String code) throws FactoryException {
        return (EngineeringDatum) replace(datumFactory.createEngineeringDatum(code));
    }

    /**
     * Creates a {@linkplain ImageDatum image datum} from a code.
     * The default implementation first invokes the same method from the {@linkplain #datumFactory
     * underlying datum factory}, and next invokes {@link #replace(Datum) replace}.
     */
    public ImageDatum createImageDatum(final String code) throws FactoryException {
        return (ImageDatum) replace(datumFactory.createImageDatum(code));
    }

    /**
     * Creates a {@linkplain VerticalDatum vertical datum} from a code.
     * The default implementation first invokes the same method from the {@linkplain #datumFactory
     * underlying datum factory}, and next invokes {@link #replace(Datum) replace}.
     */
    public VerticalDatum createVerticalDatum(final String code) throws FactoryException {
        return (VerticalDatum) replace(datumFactory.createVerticalDatum(code));
    }

    /**
     * Creates a {@linkplain TemporalDatum temporal datum} from a code.
     * The default implementation first invokes the same method from the {@linkplain #datumFactory
     * underlying datum factory}, and next invokes {@link #replace(Datum) replace}.
     */
    public TemporalDatum createTemporalDatum(final String code) throws FactoryException {
        return (TemporalDatum) replace(datumFactory.createTemporalDatum(code));
    }

    /**
     * Returns a {@linkplain GeodeticDatum geodetic datum} from a code.
     * The default implementation first invokes the same method from the {@linkplain #datumFactory
     * underlying datum factory}, and next invokes {@link #replace(Datum) replace}.
     */
    public GeodeticDatum createGeodeticDatum(final String code) throws FactoryException {
        return (GeodeticDatum) replace(datumFactory.createGeodeticDatum(code));
    }

    /**
     * Returns an {@linkplain Ellipsoid ellipsoid} from a code. The default implementation
     * delegates to the {@linkplain #datumFactory underlying datum factory} with no change.
     */
    public Ellipsoid createEllipsoid(final String code) throws FactoryException {
        return datumFactory.createEllipsoid(code);
    }

    /**
     * Returns a {@linkplain PrimeMeridian prime meridian} from a code. The default implementation
     * delegates to the {@linkplain #datumFactory underlying datum factory} with no change.
     */
    public PrimeMeridian createPrimeMeridian(final String code) throws FactoryException {
        return datumFactory.createPrimeMeridian(code);
    }

    /**
     * Returns a {@linkplain Extent extent} (usually an area of validity) from a code.
     * The default implementation delegates to the {@linkplain #crsFactory underlying CRS factory}
     * with no change.
     */
    public Extent createExtent(final String code) throws FactoryException {
        return getFactory("createExtent").createExtent(code);
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateSystem coordinate system} from a code.
     * The default implementation first invokes the same method from the {@linkplain #csFactory
     * underlying CS factory}, and next invokes {@link #replace(CoordinateSystem) replace}.
     */
    public CoordinateSystem createCoordinateSystem(final String code) throws FactoryException {
        return replace(csFactory.createCoordinateSystem(code));
    }

    /**
     * Creates a cartesian coordinate system from a code.
     * The default implementation first invokes the same method from the {@linkplain #csFactory
     * underlying CS factory}, and next invokes {@link #replace(CoordinateSystem) replace}.
     */
    public CartesianCS createCartesianCS(final String code) throws FactoryException {
        return (CartesianCS) replace(csFactory.createCartesianCS(code));
    }

    /**
     * Creates a polar coordinate system from a code.
     * The default implementation first invokes the same method from the {@linkplain #csFactory
     * underlying CS factory}, and next invokes {@link #replace(CoordinateSystem) replace}.
     */
    public PolarCS createPolarCS(final String code) throws FactoryException {
        return (PolarCS) replace(csFactory.createPolarCS(code));
    }

    /**
     * Creates a cylindrical coordinate system from a code.
     * The default implementation first invokes the same method from the {@linkplain #csFactory
     * underlying CS factory}, and next invokes {@link #replace(CoordinateSystem) replace}.
     */
    public CylindricalCS createCylindricalCS(final String code) throws FactoryException {
        return (CylindricalCS) replace(csFactory.createCylindricalCS(code));
    }

    /**
     * Creates a spherical coordinate system from a code.
     * The default implementation first invokes the same method from the {@linkplain #csFactory
     * underlying CS factory}, and next invokes {@link #replace(CoordinateSystem) replace}.
     */
    public SphericalCS createSphericalCS(final String code) throws FactoryException {
        return (SphericalCS) replace(csFactory.createSphericalCS(code));
    }

    /**
     * Creates an ellipsoidal coordinate system from a code.
     * The default implementation first invokes the same method from the {@linkplain #csFactory
     * underlying CS factory}, and next invokes {@link #replace(CoordinateSystem) replace}.
     */
    public EllipsoidalCS createEllipsoidalCS(final String code) throws FactoryException {
        return (EllipsoidalCS) replace(csFactory.createEllipsoidalCS(code));
    }

    /**
     * Creates a vertical coordinate system from a code.
     * The default implementation first invokes the same method from the {@linkplain #csFactory
     * underlying CS factory}, and next invokes {@link #replace(CoordinateSystem) replace}.
     */
    public VerticalCS createVerticalCS(final String code) throws FactoryException {
        return (VerticalCS) replace(csFactory.createVerticalCS(code));
    }

    /**
     * Creates a temporal coordinate system from a code.
     * The default implementation first invokes the same method from the {@linkplain #csFactory
     * underlying CS factory}, and next invokes {@link #replace(CoordinateSystem) replace}.
     */
    public TimeCS createTimeCS(final String code) throws FactoryException {
        return (TimeCS) replace(csFactory.createTimeCS(code));
    }

    /**
     * Returns a {@linkplain CoordinateSystemAxis coordinate system axis} from a code. The
     * default implementation delegates to the {@linkplain #csFactory underlying CS factory}
     * with no change.
     */
    public CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        return csFactory.createCoordinateSystemAxis(code);
    }

    /**
     * Returns an {@linkplain Unit unit} from a code. The default implementation
     * delegates to the {@linkplain #csFactory underlying CS factory} with no change.
     */
    public Unit createUnit(final String code) throws FactoryException {
        return csFactory.createUnit(code);
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateReferenceSystem coordinate reference system}
     * from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        return replace(crsFactory.createCoordinateReferenceSystem(code));
    }

    /**
     * Creates a 3D coordinate reference system from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public CompoundCRS createCompoundCRS(final String code) throws FactoryException {
        return (CompoundCRS) replace(crsFactory.createCompoundCRS(code));
    }

    /**
     * Creates a derived coordinate reference system from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public DerivedCRS createDerivedCRS(final String code) throws FactoryException {
        return (DerivedCRS) replace(crsFactory.createDerivedCRS(code));
    }
    
    /**
     * Creates a {@linkplain EngineeringCRS engineering coordinate reference system} from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public EngineeringCRS createEngineeringCRS(final String code) throws FactoryException {
        return (EngineeringCRS) replace(crsFactory.createEngineeringCRS(code));
    }

    /**
     * Returns a {@linkplain GeographicCRS geographic coordinate reference system} from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public GeographicCRS createGeographicCRS(final String code) throws FactoryException {
        return (GeographicCRS) replace(crsFactory.createGeographicCRS(code));
    }

    /**
     * Returns a {@linkplain GeocentricCRS geocentric coordinate reference system} from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public GeocentricCRS createGeocentricCRS(final String code) throws FactoryException {
        return (GeocentricCRS) replace(crsFactory.createGeocentricCRS(code));
    }

    /**
     * Creates a {@linkplain ImageCRS image coordinate reference system} from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public ImageCRS createImageCRS(final String code) throws FactoryException {
        return (ImageCRS) replace(crsFactory.createImageCRS(code));
    }

    /**
     * Returns a {@linkplain ProjectedCRS projected coordinate reference system} from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public ProjectedCRS createProjectedCRS(final String code) throws FactoryException {
        return (ProjectedCRS) replace(crsFactory.createProjectedCRS(code));
    }

    /**
     * Creates a {@linkplain TemporalCRS temporal coordinate reference system} from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public TemporalCRS createTemporalCRS(final String code) throws FactoryException {
        return (TemporalCRS) replace(crsFactory.createTemporalCRS(code));
    }

    /**
     * Creates a {@linkplain VerticalCRS vertical coordinate reference system} from a code.
     * The default implementation first invokes the same method from the
     * {@linkplain #crsFactory underlying CRS factory}, and next invokes
     * {@link #replace(CoordinateReferenceSystem) replace}.
     */
    public VerticalCRS createVerticalCRS(final String code) throws FactoryException {
        return (VerticalCRS) replace(crsFactory.createVerticalCRS(code));
    }

    /**
     * Creates a parameter descriptor from a code. The default implementation delegates to the
     * {@linkplain #opFactory underlying operation factory} with no change.
     */
    public ParameterDescriptor createParameterDescriptor(final String code) throws FactoryException {
        return getFactory("createParameterDescriptor").createParameterDescriptor(code);
    }

    /**
     * Creates an operation method from a code. The default implementation delegates to the
     * {@linkplain #opFactory underlying operation factory} with no change.
     */
    public OperationMethod createOperationMethod(final String code) throws FactoryException {
        return getFactory("createOperationMethod").createOperationMethod(code);
    }

    /**
     * Creates an operation from a single operation code.
     * The default implementation first invokes the same method from the
     * {@linkplain #opFactory underlying operation factory}, and next invokes
     * {@link #replace(CoordinateOperation) replace}.
     */
    public CoordinateOperation createCoordinateOperation(final String code) throws FactoryException {
        return replace(opFactory.createCoordinateOperation(code));
    }

    /**
     * Creates an operation from coordinate reference system codes.
     * The default implementation first invokes the same method from the
     * {@linkplain #opFactory underlying operation factory}, and next invokes
     * {@link #replace(CoordinateOperation) replace} for each operations.
     */
    public Set/*<CoordinateOperation>*/ createFromCoordinateReferenceSystemCodes(
                                        final String sourceCode, final String targetCode)
            throws FactoryException
    {
        final Set/*<CoordinateOperation>*/ operations, modified;
        operations = opFactory.createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
        modified   = new LinkedHashSet((int) (operations.size()/0.75f) + 1);
        for (final Iterator it=operations.iterator(); it.hasNext();) {
            final CoordinateOperation operation;
            try {
                operation = (CoordinateOperation) it.next();
            } catch (BackingStoreException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof FactoryException) {
                    throw (FactoryException) cause;
                } else {
                    throw exception;
                }
            }
            modified.add(replace(operation));
        }
        return modified;
    }

    /**
     * Releases resources immediately instead of waiting for the garbage collector.
     */
    public void dispose() throws FactoryException {
        if (   opFactory instanceof AbstractAuthorityFactory) ((AbstractAuthorityFactory)    opFactory).dispose();
        if (   csFactory instanceof AbstractAuthorityFactory) ((AbstractAuthorityFactory)    csFactory).dispose();
        if (  crsFactory instanceof AbstractAuthorityFactory) ((AbstractAuthorityFactory)   crsFactory).dispose();
        if (datumFactory instanceof AbstractAuthorityFactory) ((AbstractAuthorityFactory) datumFactory).dispose();
    }
}
