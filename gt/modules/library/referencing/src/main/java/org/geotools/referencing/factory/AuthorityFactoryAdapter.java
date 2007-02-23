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
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.Factory;
import org.geotools.factory.AbstractFactory;
import org.geotools.factory.OptionalFactory;
import org.geotools.referencing.FactoryFinder;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.Utilities;


/**
 * An authority factory which delegates {@linkplain CoordinateReferenceSystem CRS},
 * {@linkplain CoordinateSystem CS} or {@linkplain Datum datum} objects creation to
 * some other factory implementations.
 * <p>
 * All constructors are protected because this class must be subclassed in order to determine
 * which of the {@link DatumAuthorityFactory}, {@link CSAuthorityFactory} and
 * {@link CRSAuthorityFactory} interfaces to implement.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AuthorityFactoryAdapter extends AbstractAuthorityFactory implements OptionalFactory {
    /**
     * The underlying {@linkplain Datum datum} authority factory,
     * or {@code null} if none.
     *
     * @deprecated Use {@link #getDatumAuthorityFactory} instead.
     *             This field will become private in Geotools 2.5.
     */
    protected final DatumAuthorityFactory datumFactory;

    /**
     * The underlying {@linkplain CoordinateSystem coordinate system} authority factory,
     * or {@code null} if none.
     *
     * @deprecated Use {@link #getCSAuthorityFactory} instead.
     *             This field will become private in Geotools 2.5.
     */
    protected final CSAuthorityFactory csFactory;

    /**
     * The underlying {@linkplain CoordinateReferenceSystem coordinate reference system}
     * authority factory, or {@code null} if none.
     *
     * @deprecated Use {@link #getCRSAuthorityFactory} instead.
     *             This field will become private in Geotools 2.5.
     */
    protected final CRSAuthorityFactory crsFactory;

    /**
     * The underlying {@linkplain CoordinateOperation coordinate operation} authority factory,
     * or {@code null} if none.
     *
     * @deprecated Use {@link #getCoordinateOperationAuthorityFactory} instead.
     *             This field will become private in Geotools 2.5.
     */
    protected final CoordinateOperationAuthorityFactory operationFactory;

    /**
     * A set of low-level factories to be used if none were found in {@link #datumFactory},
     * {@link #csFactory}, {@link #crsFactory} or {@link #operationFactory}. Will be created
     * only when first needed.
     *
     * @see #getFactoryGroup
     */
    private transient FactoryGroup factories;

    /**
     * Creates a wrapper around no factory. This constructor should never be used except by
     * subclasses overriding the <code>get</code><var>Foo</var><code>AuthorityFactory</code>
     * methods.
     *
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     */
    AuthorityFactoryAdapter(final int priority) {
        super(priority);
        datumFactory     = null;
        csFactory        = null;
        crsFactory       = null;
        operationFactory = null;
    }

    /**
     * Creates a wrapper around the specified factory. The {@link #priority priority} field
     * will be set to the same value than the specified factory. Subclasses should override
     * the {@link #getPriority() getPriority()} method if they want to set a higher or lower
     * priority for this instance.
     *
     * @param factory The factory to wrap.
     */
    protected AuthorityFactoryAdapter(final AuthorityFactory factory) {
        this((factory instanceof   CRSAuthorityFactory) ? (  CRSAuthorityFactory) factory : null,
             (factory instanceof    CSAuthorityFactory) ? (   CSAuthorityFactory) factory : null,
             (factory instanceof DatumAuthorityFactory) ? (DatumAuthorityFactory) factory : null,
             (factory instanceof CoordinateOperationAuthorityFactory) ?
                                (CoordinateOperationAuthorityFactory) factory : null);
    }

    /**
     * Creates a wrapper around the specified factories. The {@link #priority priority} field will
     * be set to the highest priority found in the specified factories. Subclasses should override
     * the {@link #getPriority() getPriority()} method if they want to set a higher or lower
     * priority for this instance.
     *
     * @param crsFactory   The {@linkplain CoordinateReferenceSystem coordinate reference system}
     *                     authority factory, or {@code null}.
     * @param csFactory    The {@linkplain CoordinateSystem coordinate system} authority factory,
     *                     or {@code null}.
     * @param datumFactory The {@linkplain Datum datum} authority factory, or {@code null}.
     * @param opFactory    The {@linkplain CoordinateOperation coordinate operation} authority
     *                     factory, or {@code null}.
     */
    protected AuthorityFactoryAdapter(final CRSAuthorityFactory                crsFactory,
                                      final CSAuthorityFactory                  csFactory,
                                      final DatumAuthorityFactory            datumFactory,
                                      final CoordinateOperationAuthorityFactory opFactory)
    {
        super(Math.max(getPriority(datumFactory),
              Math.max(getPriority(   csFactory),
              Math.max(getPriority(  crsFactory),
                       getPriority(   opFactory)))));

        if (this instanceof CRSAuthorityFactory) {
            ensureNonNull("crsFactory", crsFactory);
        }
        if (this instanceof CSAuthorityFactory) {
            ensureNonNull("csFactory", csFactory);
        }
        if (this instanceof DatumAuthorityFactory) {
            ensureNonNull("datumFactory", datumFactory);
        }
        if (this instanceof CoordinateOperationAuthorityFactory) {
            ensureNonNull("opFactory", opFactory);
        }
        store(Hints.               DATUM_AUTHORITY_FACTORY, this.    datumFactory = datumFactory);
        store(Hints.                  CS_AUTHORITY_FACTORY, this.       csFactory =    csFactory);
        store(Hints.                 CRS_AUTHORITY_FACTORY, this.      crsFactory =   crsFactory);
        store(Hints.COORDINATE_OPERATION_AUTHORITY_FACTORY, this.operationFactory =    opFactory);
    }

    /**
     * Returns the priority of the specified factory, or {@link #NORMAL_PRIORITY} if unknown.
     */
    private static int getPriority(final AuthorityFactory factory) {
        return (factory instanceof AbstractFactory) ?
            ((AbstractFactory) factory).getPriority() : NORMAL_PRIORITY;
    }

    /**
     * Adds the specified factory to the set of hints, if non null.
     */
    private void store(final Hints.Key key, final AuthorityFactory factory) {
        if (factory != null) {
            if (hints.put(key, factory) != null) {
                throw new AssertionError(key);
            }
        }
    }

    /**
     * Returns the {@linkplain #hints hints} extented will all hints specified in dependencies.
     */
    private Hints hints() {
        final Hints extended = new Hints(hints);
        addAll(operationFactory, extended);
        addAll(    datumFactory, extended);
        addAll(       csFactory, extended);
        addAll(      crsFactory, extended);
        extended.putAll(hints); // Give precedences to the hints from this class.
        return extended;
    }

    /**
     * Adds all hints from the specified factory into the specified set of hints.
     */
    private static void addAll(final AuthorityFactory factory, final Hints hints) {
        if (factory instanceof Factory) {
            hints.putAll(((Factory) factory).getImplementationHints());
        }
    }

    /**
     * Returns {@code true} if this factory is ready for use. This default implementation
     * checks the availability of CRS, CS, datum and operation authority factories specified
     * at construction time.
     */
    public boolean isAvailable() {
        return isAvailable(      crsFactory) &&
               isAvailable(       csFactory) &&
               isAvailable(    datumFactory) &&
               isAvailable(operationFactory);
    }

    /**
     * Checks the availability of the specified factory.
     */
    private static boolean isAvailable(final AuthorityFactory factory) {
        return !(factory instanceof OptionalFactory) || ((OptionalFactory) factory).isAvailable();
    }

    /**
     * Replaces the specified unit, if applicable.
     * To be overridden with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    Unit replace(Unit units) throws FactoryException {
        return units;
    }

    /**
     * Replaces (if needed) the specified axis by a new one.
     * To be overridden with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    CoordinateSystemAxis replace(CoordinateSystemAxis axis) throws FactoryException {
        return axis;
    }

    /**
     * Replaces (if needed) the specified coordinate system by a new one.
     * To be overridden with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    CoordinateSystem replace(CoordinateSystem cs) throws FactoryException {
        return cs;
    }

    /**
     * Replaces (if needed) the specified datum by a new one.
     * To be overridden with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    Datum replace(Datum datum) throws FactoryException {
        return datum;
    }

    /**
     * Replaces (if needed) the specified coordinate reference system.
     * To be overridden with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    CoordinateReferenceSystem replace(CoordinateReferenceSystem crs) throws FactoryException {
        return crs;
    }

    /**
     * Replaces (if needed) the specified coordinate operation.
     * To be overridden with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    CoordinateOperation replace(CoordinateOperation operation) throws FactoryException {
        return operation;
    }

    /**
     * Delegates the work to an appropriate {@code replace} method for the given object.
     */
    private IdentifiedObject replaceObject(final IdentifiedObject object) throws FactoryException {
        if (object instanceof CoordinateReferenceSystem) {
            return replace((CoordinateReferenceSystem) object);
        }
        if (object instanceof CoordinateSystem) {
            return replace((CoordinateSystem) object);
        }
        if (object instanceof CoordinateSystemAxis) {
            return replace((CoordinateSystemAxis) object);
        }
        if (object instanceof Datum) {
            return replace((Datum) object);
        }
        if (object instanceof CoordinateOperation) {
            return replace((CoordinateOperation) object);
        }
        return object;
    }

    /**
     * Returns one of the underlying factories as an instance of the Geotools implementation.
     * If there is none of them, then returns {@code null} or throws an exception if {@code caller}
     * is not null.
     */
    private AbstractAuthorityFactory getGeotoolsFactory(final String caller, final String code)
            throws FactoryException
    {
        final AuthorityFactory candidate = getAuthorityFactory(code);
        if (candidate instanceof AbstractAuthorityFactory) {
            return (AbstractAuthorityFactory) candidate;
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
        final AbstractAuthorityFactory factory = getGeotoolsFactory(null, null);
        return (factory != null) ? factory.getBackingStoreDescription() : null;
    }

    /**
     * Returns the vendor responsible for creating this factory implementation.
     */
    public Citation getVendor() {
        return getAuthorityFactory().getVendor();
    }

    /**
     * Returns the organization or party responsible for definition and maintenance of the
     * database.
     */
    public Citation getAuthority() {
        return getAuthorityFactory().getAuthority();
    }

    /**
     * Returns the set of authority code for the specified type.
     * 
     * @todo We should returns the union of authority codes from all underlying factories.
     */
    public Set/*<String>*/ getAuthorityCodes(final Class type) throws FactoryException {
        return getAuthorityFactory(null).getAuthorityCodes(type);
    }

    /**
     * Returns a description for the object identified by the specified code.
     */
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        return getAuthorityFactory(code).getDescriptionText(toBackingFactoryCode(code));
    }

    /**
     * Returns an arbitrary object from a code.
     *
     * @see #createCoordinateReferenceSystem
     * @see #createDatum
     * @see #createEllipsoid
     * @see #createUnit
     */
    public IdentifiedObject createObject(final String code) throws FactoryException {
        return replaceObject(getAuthorityFactory(code).createObject(toBackingFactoryCode(code)));
    }

    /**
     * Returns an arbitrary {@linkplain Datum datum} from a code.
     *
     * @see #createGeodeticDatum
     * @see #createVerticalDatum
     * @see #createTemporalDatum
     */
    public Datum createDatum(final String code) throws FactoryException {
        return replace(getDatumAuthorityFactory(code).createDatum(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain EngineeringDatum engineering datum} from a code.
     *
     * @see #createEngineeringCRS
     */
    public EngineeringDatum createEngineeringDatum(final String code) throws FactoryException {
        return (EngineeringDatum) replace(getDatumAuthorityFactory(code).
                createEngineeringDatum(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain ImageDatum image datum} from a code.
     *
     * @see #createImageCRS
     */
    public ImageDatum createImageDatum(final String code) throws FactoryException {
        return (ImageDatum) replace(getDatumAuthorityFactory(code).
                createImageDatum(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain VerticalDatum vertical datum} from a code.
     *
     * @see #createVerticalCRS
     */
    public VerticalDatum createVerticalDatum(final String code) throws FactoryException {
        return (VerticalDatum) replace(getDatumAuthorityFactory(code).
                createVerticalDatum(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain TemporalDatum temporal datum} from a code.
     *
     * @see #createTemporalCRS
     */
    public TemporalDatum createTemporalDatum(final String code) throws FactoryException {
        return (TemporalDatum) replace(getDatumAuthorityFactory(code).
                createTemporalDatum(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain GeodeticDatum geodetic datum} from a code.
     *
     * @see #createEllipsoid
     * @see #createPrimeMeridian
     * @see #createGeographicCRS
     * @see #createProjectedCRS
     */
    public GeodeticDatum createGeodeticDatum(final String code) throws FactoryException {
        return (GeodeticDatum) replace(getDatumAuthorityFactory(code).
                createGeodeticDatum(toBackingFactoryCode(code)));
    }

    /**
     * Returns an {@linkplain Ellipsoid ellipsoid} from a code.
     *
     * @see #createGeodeticDatum
     */
    public Ellipsoid createEllipsoid(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createEllipsoid(toBackingFactoryCode(code));
    }

    /**
     * Returns a {@linkplain PrimeMeridian prime meridian} from a code.
     *
     * @see #createGeodeticDatum
     */
    public PrimeMeridian createPrimeMeridian(final String code) throws FactoryException {
        return getDatumAuthorityFactory(code).createPrimeMeridian(toBackingFactoryCode(code));
    }

    /**
     * Returns a {@linkplain Extent extent} (usually an area of validity) from a code.
     */
    public Extent createExtent(final String code) throws FactoryException {
        return getGeotoolsFactory("createExtent", code).createExtent(toBackingFactoryCode(code));
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateSystem coordinate system} from a code.
     */
    public CoordinateSystem createCoordinateSystem(final String code) throws FactoryException {
        return replace(getCSAuthorityFactory(code).
                createCoordinateSystem(toBackingFactoryCode(code)));
    }

    /**
     * Creates a cartesian coordinate system from a code.
     */
    public CartesianCS createCartesianCS(final String code) throws FactoryException {
        return (CartesianCS) replace(getCSAuthorityFactory(code).
                createCartesianCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a polar coordinate system from a code.
     */
    public PolarCS createPolarCS(final String code) throws FactoryException {
        return (PolarCS) replace(getCSAuthorityFactory(code).
                createPolarCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a cylindrical coordinate system from a code.
     */
    public CylindricalCS createCylindricalCS(final String code) throws FactoryException {
        return (CylindricalCS) replace(getCSAuthorityFactory(code).
                createCylindricalCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a spherical coordinate system from a code.
     */
    public SphericalCS createSphericalCS(final String code) throws FactoryException {
        return (SphericalCS) replace(getCSAuthorityFactory(code).
                createSphericalCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates an ellipsoidal coordinate system from a code.
     */
    public EllipsoidalCS createEllipsoidalCS(final String code) throws FactoryException {
        return (EllipsoidalCS) replace(getCSAuthorityFactory(code).
                createEllipsoidalCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a vertical coordinate system from a code.
     */
    public VerticalCS createVerticalCS(final String code) throws FactoryException {
        return (VerticalCS) replace(getCSAuthorityFactory(code).
                createVerticalCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a temporal coordinate system from a code.
     */
    public TimeCS createTimeCS(final String code) throws FactoryException {
        return (TimeCS) replace(getCSAuthorityFactory(code).
                createTimeCS(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain CoordinateSystemAxis coordinate system axis} from a code.
     */
    public CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        return replace(getCSAuthorityFactory(code).
                createCoordinateSystemAxis(toBackingFactoryCode(code)));
    }

    /**
     * Returns an {@linkplain Unit unit} from a code.
     */
    public Unit createUnit(final String code) throws FactoryException {
        return replace(getCSAuthorityFactory(code).
                createUnit(toBackingFactoryCode(code)));
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateReferenceSystem coordinate reference system}
     * from a code.
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
        return replace(getCRSAuthorityFactory(code).
                createCoordinateReferenceSystem(toBackingFactoryCode(code)));
    }

    /**
     * Creates a 3D coordinate reference system from a code.
     */
    public CompoundCRS createCompoundCRS(final String code) throws FactoryException {
        return (CompoundCRS) replace(getCRSAuthorityFactory(code).
                createCompoundCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a derived coordinate reference system from a code.
     */
    public DerivedCRS createDerivedCRS(final String code) throws FactoryException {
        return (DerivedCRS) replace(getCRSAuthorityFactory(code).
                createDerivedCRS(toBackingFactoryCode(code)));
    }
    
    /**
     * Creates a {@linkplain EngineeringCRS engineering coordinate reference system} from a code.
     */
    public EngineeringCRS createEngineeringCRS(final String code) throws FactoryException {
        return (EngineeringCRS) replace(getCRSAuthorityFactory(code).
                createEngineeringCRS(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain GeographicCRS geographic coordinate reference system} from a code.
     */
    public GeographicCRS createGeographicCRS(final String code) throws FactoryException {
        return (GeographicCRS) replace(getCRSAuthorityFactory(code).
                createGeographicCRS(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain GeocentricCRS geocentric coordinate reference system} from a code.
     */
    public GeocentricCRS createGeocentricCRS(final String code) throws FactoryException {
        return (GeocentricCRS) replace(getCRSAuthorityFactory(code).
                createGeocentricCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain ImageCRS image coordinate reference system} from a code.
     */
    public ImageCRS createImageCRS(final String code) throws FactoryException {
        return (ImageCRS) replace(getCRSAuthorityFactory(code).
                createImageCRS(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain ProjectedCRS projected coordinate reference system} from a code.
     */
    public ProjectedCRS createProjectedCRS(final String code) throws FactoryException {
        return (ProjectedCRS) replace(getCRSAuthorityFactory(code).
                createProjectedCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain TemporalCRS temporal coordinate reference system} from a code.
     */
    public TemporalCRS createTemporalCRS(final String code) throws FactoryException {
        return (TemporalCRS) replace(getCRSAuthorityFactory(code).
                createTemporalCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain VerticalCRS vertical coordinate reference system} from a code.
     */
    public VerticalCRS createVerticalCRS(final String code) throws FactoryException {
        return (VerticalCRS) replace(getCRSAuthorityFactory(code).
                createVerticalCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a parameter descriptor from a code.
     */
    public ParameterDescriptor createParameterDescriptor(final String code) throws FactoryException {
        return getGeotoolsFactory("createParameterDescriptor", code).
                createParameterDescriptor(toBackingFactoryCode(code));
    }

    /**
     * Creates an operation method from a code.
     */
    public OperationMethod createOperationMethod(final String code) throws FactoryException {
        return getGeotoolsFactory("createOperationMethod", code).
                createOperationMethod(toBackingFactoryCode(code));
    }

    /**
     * Creates an operation from a single operation code.
     */
    public CoordinateOperation createCoordinateOperation(final String code) throws FactoryException {
        return replace(getCoordinateOperationAuthorityFactory(code).
                createCoordinateOperation(toBackingFactoryCode(code)));
    }

    /**
     * Creates an operation from coordinate reference system codes.
     */
    public Set/*<CoordinateOperation>*/ createFromCoordinateReferenceSystemCodes(
                                        final String sourceCode, final String targetCode)
            throws FactoryException
    {
        final CoordinateOperationAuthorityFactory factory, check;
        factory = getCoordinateOperationAuthorityFactory(sourceCode);
        check   = getCoordinateOperationAuthorityFactory(targetCode);
        if (factory != check) {
            /*
             * No coordinate operation because of mismatched factories. This is not
             * illegal - the result is an empty set - but it is worth to notify the
             * user since this case has some chances to be an user error.
             */
            final LogRecord record = Logging.format(Level.WARNING,
                    LoggingKeys.MISMATCHED_COORDINATE_OPERATION_FACTORIES_$2, sourceCode, targetCode);
            record.setSourceMethodName("createFromCoordinateReferenceSystemCodes");
            record.setSourceClassName(AuthorityFactoryAdapter.class.getName());
            LOGGER.log(record);
            return Collections.EMPTY_SET;
        }
        return factory.createFromCoordinateReferenceSystemCodes(
                toBackingFactoryCode(sourceCode), toBackingFactoryCode(targetCode));
    }

    /**
     * Looks up an object from this authority factory which is
     * {@linkplain org.geotools.referencing.CRS#equalsIgnoreMetadata equals, ignoring metadata},
     * to the specified object. The default implementation delegates to the
     * {@linkplain #getAuthorityFactory(String) generic authority factory}, if possible.
     *
     * @since 2.4
     */
    //@Override
    public IdentifiedObject find(IdentifiedObject object, final boolean fullScan)
            throws FactoryException
    {
        final AbstractAuthorityFactory factory = getGeotoolsFactory(null, null);
        if (factory != null) {
            object = factory.find(object, fullScan);
        } else {
            object = super.find(object, fullScan);
        }
        return replaceObject(object);
    }

    /**
     * Creates an exception for a missing factory.
     */
    private static FactoryException missingFactory(final Class category) {
        return new FactoryException(Errors.format(ErrorKeys.FACTORY_NOT_FOUND_$1,
                  Utilities.getShortName(category)));
    }

    /**
     * For internal use by {@link #getAuthority} and {@link #getVendor} only. Its only purpose
     * is to catch the {@link FactoryException} for methods that don't allow it. The protected
     * method should be used instead when this exception is allowed.
     */
    private AuthorityFactory getAuthorityFactory() {
        try {
            return getAuthorityFactory(null);
        } catch (FactoryException cause) {
            IllegalStateException e = new IllegalStateException(
                    Errors.format(ErrorKeys.UNDEFINED_PROPERTY));
            e.initCause(cause); // TODO: inline when we will be allowed to compile for J2SE 1.5.
            throw e;
        }
    }

    /**
     * Returns an authority factory of the specified type. This method delegates to:
     * <ul>
     *   <li>{@link #getCRSAuthorityFactory} if {@code type} is
     *       {@code CRSAuthorityFactory.class};</li>
     *   <li>{@link #getCSAuthorityFactory} if {@code type} is
     *       {@code CSAuthorityFactory.class};</li>
     *   <li>{@link #getDatumAuthorityFactory} if {@code type} is
     *       {@code DatumAuthorityFactory.class};</li>
     *   <li>{@link #CoordinateOperationAuthorityFactory} if {@code type} is
     *       {@code CoordinateOperationAuthorityFactory.class};</li>
     * </ul>
     *
     * @throws IllegalArgumentException if the specified {@code type} is invalid.
     * @throws FactoryException if no suitable factory were found.
     */
    AuthorityFactory getAuthorityFactory(final Class/*<T extends AuthorityFactory>*/ type,
                                         final String code)
            throws FactoryException
    {
        if (CRSAuthorityFactory.class.equals(type)) {
            return getCRSAuthorityFactory(code);
        }
        if (CSAuthorityFactory.class.equals(type)) {
            return getCSAuthorityFactory(code);
        }
        if (DatumAuthorityFactory.class.equals(type)) {
            return getDatumAuthorityFactory(code);
        }
        if (CoordinateOperationAuthorityFactory.class.equals(type)) {
            return getCoordinateOperationAuthorityFactory(code);
        }
        throw new IllegalArgumentException(
                Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2, "type", type));
    }

    /**
     * Returns a generic object factory to use for the specified code. The default implementation
     * returns one of the factory specified at construction time. Subclasses can override
     * this method in order to select a different factory implementation depending on the
     * code value.
     * <p>
     * <strong>Note:</strong> The value of the {@code code} argument given to this
     * method may be {@code null} when a factory is needed for some global task,
     * like {@link #getAuthorityCodes} method execution.
     *
     * @param  code The authority code given to this class. Note that the code to be given
     *         to the returned factory {@linkplain #toBackingFactoryCode may be different}.
     * @return A factory for the specified authority code (never {@code null}).
     * @throws FactoryException if no suitable factory were found.
     *
     * @since 2.4
     */
    protected AuthorityFactory getAuthorityFactory(final String code) throws FactoryException {
        if (      crsFactory != null) return       crsFactory;
        if (       csFactory != null) return        csFactory;
        if (    datumFactory != null) return     datumFactory;
        if (operationFactory != null) return operationFactory;
        throw missingFactory(AuthorityFactory.class);
    }

    /**
     * Returns the datum factory to use for the specified code. The default implementation
     * always returns the factory specified at construction time. Subclasses can override
     * this method in order to select a different factory implementation depending on the
     * code value.
     *
     * @param  code The authority code given to this class. Note that the code to be given
     *         to the returned factory {@linkplain #toBackingFactoryCode may be different}.
     * @return A factory for the specified authority code (never {@code null}).
     * @throws FactoryException if no datum factory were specified at construction time.
     *
     * @since 2.4
     */
    protected DatumAuthorityFactory getDatumAuthorityFactory(final String code)
            throws FactoryException
    {
        if (datumFactory == null) {
            throw missingFactory(DatumAuthorityFactory.class);
        }
        return datumFactory;
    }

    /**
     * Returns the coordinate system factory to use for the specified code. The default
     * implementation always returns the factory specified at construction time. Subclasses
     * can override this method in order to select a different factory implementation
     * depending on the code value.
     *
     * @param  code The authority code given to this class. Note that the code to be given
     *         to the returned factory {@linkplain #toBackingFactoryCode may be different}.
     * @return A factory for the specified authority code (never {@code null}).
     * @throws FactoryException if no coordinate system factory were specified at construction time.
     *
     * @since 2.4
     */
    protected CSAuthorityFactory getCSAuthorityFactory(final String code)
            throws FactoryException
    {
        if (csFactory == null) {
            throw missingFactory(CSAuthorityFactory.class);
        }
        return csFactory;
    }

    /**
     * Returns the coordinate reference system factory to use for the specified code. The default
     * implementation always returns the factory specified at construction time. Subclasses can
     * override this method in order to select a different factory implementation depending on
     * the code value.
     *
     * @param  code The authority code given to this class. Note that the code to be given
     *         to the returned factory {@linkplain #toBackingFactoryCode may be different}.
     * @return A factory for the specified authority code (never {@code null}).
     * @throws FactoryException if no coordinate reference system factory were specified
     *         at construction time.
     *
     * @since 2.4
     */
    protected CRSAuthorityFactory getCRSAuthorityFactory(final String code)
            throws FactoryException
    {
        if (crsFactory == null) {
            throw missingFactory(CRSAuthorityFactory.class);
        }
        return crsFactory;
    }

    /**
     * Returns the coordinate operation factory to use for the specified code. The default
     * implementation always returns the factory specified at construction time. Subclasses can
     * override this method in order to select a different factory implementation depending on
     * the code value.
     *
     * @param  code The authority code given to this class. Note that the code to be given
     *         to the returned factory {@linkplain #toBackingFactoryCode may be different}.
     * @return A factory for the specified authority code (never {@code null}).
     * @throws FactoryException if no coordinate operation factory were specified
     *         at construction time.
     *
     * @since 2.4
     */
    protected CoordinateOperationAuthorityFactory getCoordinateOperationAuthorityFactory(final String code)
            throws FactoryException
    {
        if (operationFactory == null) {
            throw missingFactory(CoordinateOperationAuthorityFactory.class);
        }
        return operationFactory;
    }

    /**
     * Returns a coordinate operation factory for this adapter. This method will try to fetch
     * this information from the coordinate operation authority factory, or will returns the
     * default one if no explicit factory were found.
     */
    final CoordinateOperationFactory getCoordinateOperationFactory() throws FactoryException {
        if (operationFactory instanceof Factory) {
            final Factory factory   = (Factory) operationFactory;
            final Map     hints     = factory.getImplementationHints();
            final Object  candidate = hints.get(Hints.COORDINATE_OPERATION_FACTORY);
            if (candidate instanceof CoordinateOperationFactory) {
                return (CoordinateOperationFactory) candidate;
            }
        }
        return FactoryFinder.getCoordinateOperationFactory(hints());
    }

    /**
     * Suggests a low-level factory group. If {@code crs} is {@code true}, then this method will
     * try to fetch the factory group from the CRS authority factory. Otherwise it will try to
     * fetch the factory group from the CS authority factory. This is used by subclasses like
     * {@link TransformedAuthorityFactory} that need low-level access to factories. Do not change
     * this method into a public one; we would need a better API before to do such thing.
     */
    final FactoryGroup getFactoryGroup(final boolean crs) {
        final AuthorityFactory factory;
        if (crs) {
            factory = crsFactory;
        } else {
            factory = csFactory;
        }
        if (factory instanceof DirectAuthorityFactory) {
            return ((DirectAuthorityFactory) factory).factories;
        }
        // No predefined factory group. Create one.
        if (factories == null) {
            factories = FactoryGroup.createInstance(hints());
        }
        return factories;
    }

    /**
     * Returns the code to be given to the wrapped factories. This method is automatically
     * invoked by all {@code create} methods before to forward the code to the
     * {@linkplain #getCRSAuthorityFactory CRS}, {@linkplain #getCSAuthorityFactory CS},
     * {@linkplain #getDatumAuthorityFactory datum} or {@linkplain #operationFactory operation}
     * factory. The default implementation returns the {@code code} unchanged.
     *
     * @param  code The code given to this factory.
     * @return The code to give to the underlying factories.
     * @throws FactoryException if the code can't be converted.
     *
     * @since 2.4
     */
    protected String toBackingFactoryCode(final String code) throws FactoryException {
        return code;
    }
}
