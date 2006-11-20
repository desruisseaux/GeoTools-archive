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
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


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
     */
    protected final DatumAuthorityFactory datumFactory;

    /**
     * The underlying {@linkplain CoordinateSystem coordinate system} authority factory,
     * or {@code null} if none.
     */
    protected final CSAuthorityFactory csFactory;

    /**
     * The underlying {@linkplain CoordinateReferenceSystem coordinate reference system}
     * authority factory, or {@code null} if none.
     */
    protected final CRSAuthorityFactory crsFactory;

    /**
     * The underlying {@linkplain CoordinateOperation coordinate operation} authority factory,
     * or {@code null} if none.
     */
    protected final CoordinateOperationAuthorityFactory operationFactory;

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
     * This is used by subclasses for fetching a new factory.
     */
    final Hints hints() {
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
     * Returns {@code true} if this factory is ready for use. This method checks the
     * availability of {@link #crsFactory}, {@link #csFactory}, {@link #datumFactory}
     * and {@link #operationFactory}.
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
     * To be overrided with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    Unit replace(Unit units) throws FactoryException {
        return units;
    }

    /**
     * Replaces (if needed) the specified axis by a new one.
     * To be overrided with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    CoordinateSystemAxis replace(CoordinateSystemAxis axis) throws FactoryException {
        return axis;
    }

    /**
     * Replaces (if needed) the specified coordinate system by a new one.
     * To be overrided with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    CoordinateSystem replace(CoordinateSystem cs) throws FactoryException {
        return cs;
    }

    /**
     * Replaces (if needed) the specified datum by a new one.
     * To be overrided with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    Datum replace(Datum datum) throws FactoryException {
        return datum;
    }

    /**
     * Replaces (if needed) the specified coordinate reference system.
     * To be overrided with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    CoordinateReferenceSystem replace(CoordinateReferenceSystem crs) throws FactoryException {
        return crs;
    }

    /**
     * Replaces (if needed) the specified coordinate operation.
     * To be overrided with {@code protected} access by {@link TransformedAuthorityFactory}.
     */
    CoordinateOperation replace(CoordinateOperation operation) throws FactoryException {
        return operation;
    }

    /**
     * Returns the first non-null authority factory.
     */
    private AuthorityFactory getAuthorityFactory() {
        if (      crsFactory != null) return       crsFactory;
        if (       csFactory != null) return        csFactory;
        if (    datumFactory != null) return     datumFactory;
        if (operationFactory != null) return operationFactory;
        return null;
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
        if (operationFactory instanceof AbstractAuthorityFactory) {
            return (AbstractAuthorityFactory) operationFactory;
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
    public Set/*<String>*/ getAuthorityCodes(Class type) throws FactoryException {
        return getAuthorityFactory().getAuthorityCodes(type);
    }

    /**
     * Returns a description for the object identified by the specified code.
     */
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        return getAuthorityFactory().getDescriptionText(toBackingFactoryCode(code));
    }

    /**
     * Returns an arbitrary object from a code.
     */
    public IdentifiedObject createObject(final String code) throws FactoryException {
        IdentifiedObject object = getAuthorityFactory().createObject(toBackingFactoryCode(code));
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
     */
    public Datum createDatum(final String code) throws FactoryException {
        return replace(datumFactory.createDatum(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain EngineeringDatum engineering datum} from a code.
     */
    public EngineeringDatum createEngineeringDatum(final String code) throws FactoryException {
        return (EngineeringDatum) replace(datumFactory.createEngineeringDatum(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain ImageDatum image datum} from a code.
     */
    public ImageDatum createImageDatum(final String code) throws FactoryException {
        return (ImageDatum) replace(datumFactory.createImageDatum(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain VerticalDatum vertical datum} from a code.
     */
    public VerticalDatum createVerticalDatum(final String code) throws FactoryException {
        return (VerticalDatum) replace(datumFactory.createVerticalDatum(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain TemporalDatum temporal datum} from a code.
     */
    public TemporalDatum createTemporalDatum(final String code) throws FactoryException {
        return (TemporalDatum) replace(datumFactory.createTemporalDatum(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain GeodeticDatum geodetic datum} from a code.
     */
    public GeodeticDatum createGeodeticDatum(final String code) throws FactoryException {
        return (GeodeticDatum) replace(datumFactory.createGeodeticDatum(toBackingFactoryCode(code)));
    }

    /**
     * Returns an {@linkplain Ellipsoid ellipsoid} from a code.
     */
    public Ellipsoid createEllipsoid(final String code) throws FactoryException {
        return datumFactory.createEllipsoid(toBackingFactoryCode(code));
    }

    /**
     * Returns a {@linkplain PrimeMeridian prime meridian} from a code.
     */
    public PrimeMeridian createPrimeMeridian(final String code) throws FactoryException {
        return datumFactory.createPrimeMeridian(toBackingFactoryCode(code));
    }

    /**
     * Returns a {@linkplain Extent extent} (usually an area of validity) from a code.
     */
    public Extent createExtent(final String code) throws FactoryException {
        return getFactory("createExtent").createExtent(toBackingFactoryCode(code));
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateSystem coordinate system} from a code.
     */
    public CoordinateSystem createCoordinateSystem(final String code) throws FactoryException {
        return replace(csFactory.createCoordinateSystem(toBackingFactoryCode(code)));
    }

    /**
     * Creates a cartesian coordinate system from a code.
     */
    public CartesianCS createCartesianCS(final String code) throws FactoryException {
        return (CartesianCS) replace(csFactory.createCartesianCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a polar coordinate system from a code.
     */
    public PolarCS createPolarCS(final String code) throws FactoryException {
        return (PolarCS) replace(csFactory.createPolarCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a cylindrical coordinate system from a code.
     */
    public CylindricalCS createCylindricalCS(final String code) throws FactoryException {
        return (CylindricalCS) replace(csFactory.createCylindricalCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a spherical coordinate system from a code.
     */
    public SphericalCS createSphericalCS(final String code) throws FactoryException {
        return (SphericalCS) replace(csFactory.createSphericalCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates an ellipsoidal coordinate system from a code.
     */
    public EllipsoidalCS createEllipsoidalCS(final String code) throws FactoryException {
        return (EllipsoidalCS) replace(csFactory.createEllipsoidalCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a vertical coordinate system from a code.
     */
    public VerticalCS createVerticalCS(final String code) throws FactoryException {
        return (VerticalCS) replace(csFactory.createVerticalCS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a temporal coordinate system from a code.
     */
    public TimeCS createTimeCS(final String code) throws FactoryException {
        return (TimeCS) replace(csFactory.createTimeCS(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain CoordinateSystemAxis coordinate system axis} from a code.
     */
    public CoordinateSystemAxis createCoordinateSystemAxis(final String code)
            throws FactoryException
    {
        return replace(csFactory.createCoordinateSystemAxis(toBackingFactoryCode(code)));
    }

    /**
     * Returns an {@linkplain Unit unit} from a code.
     */
    public Unit createUnit(final String code) throws FactoryException {
        return replace(csFactory.createUnit(toBackingFactoryCode(code)));
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateReferenceSystem coordinate reference system}
     * from a code.
     */
    public CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        return replace(crsFactory.createCoordinateReferenceSystem(toBackingFactoryCode(code)));
    }

    /**
     * Creates a 3D coordinate reference system from a code.
     */
    public CompoundCRS createCompoundCRS(final String code) throws FactoryException {
        return (CompoundCRS) replace(crsFactory.createCompoundCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a derived coordinate reference system from a code.
     */
    public DerivedCRS createDerivedCRS(final String code) throws FactoryException {
        return (DerivedCRS) replace(crsFactory.createDerivedCRS(toBackingFactoryCode(code)));
    }
    
    /**
     * Creates a {@linkplain EngineeringCRS engineering coordinate reference system} from a code.
     */
    public EngineeringCRS createEngineeringCRS(final String code) throws FactoryException {
        return (EngineeringCRS) replace(crsFactory.createEngineeringCRS(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain GeographicCRS geographic coordinate reference system} from a code.
     */
    public GeographicCRS createGeographicCRS(final String code) throws FactoryException {
        return (GeographicCRS) replace(crsFactory.createGeographicCRS(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain GeocentricCRS geocentric coordinate reference system} from a code.
     */
    public GeocentricCRS createGeocentricCRS(final String code) throws FactoryException {
        return (GeocentricCRS) replace(crsFactory.createGeocentricCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain ImageCRS image coordinate reference system} from a code.
     */
    public ImageCRS createImageCRS(final String code) throws FactoryException {
        return (ImageCRS) replace(crsFactory.createImageCRS(toBackingFactoryCode(code)));
    }

    /**
     * Returns a {@linkplain ProjectedCRS projected coordinate reference system} from a code.
     */
    public ProjectedCRS createProjectedCRS(final String code) throws FactoryException {
        return (ProjectedCRS) replace(crsFactory.createProjectedCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain TemporalCRS temporal coordinate reference system} from a code.
     */
    public TemporalCRS createTemporalCRS(final String code) throws FactoryException {
        return (TemporalCRS) replace(crsFactory.createTemporalCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a {@linkplain VerticalCRS vertical coordinate reference system} from a code.
     */
    public VerticalCRS createVerticalCRS(final String code) throws FactoryException {
        return (VerticalCRS) replace(crsFactory.createVerticalCRS(toBackingFactoryCode(code)));
    }

    /**
     * Creates a parameter descriptor from a code.
     */
    public ParameterDescriptor createParameterDescriptor(final String code) throws FactoryException {
        return getFactory("createParameterDescriptor").createParameterDescriptor(toBackingFactoryCode(code));
    }

    /**
     * Creates an operation method from a code.
     */
    public OperationMethod createOperationMethod(final String code) throws FactoryException {
        return getFactory("createOperationMethod").createOperationMethod(toBackingFactoryCode(code));
    }

    /**
     * Creates an operation from a single operation code.
     */
    public CoordinateOperation createCoordinateOperation(final String code) throws FactoryException {
        return replace(operationFactory.createCoordinateOperation(toBackingFactoryCode(code)));
    }

    /**
     * Creates an operation from coordinate reference system codes.
     */
    public Set/*<CoordinateOperation>*/ createFromCoordinateReferenceSystemCodes(
                                        final String sourceCode, final String targetCode)
            throws FactoryException
    {
        return operationFactory.createFromCoordinateReferenceSystemCodes(
                toBackingFactoryCode(sourceCode), toBackingFactoryCode(targetCode));
    }

    /**
     * Returns the code to be given to the wrapped factories. This method is automatically
     * invoked by all {@code create} methods before to forward the code to the
     * {@linkplain crsFactory CRS}, {@linkplain csFactory CS}, {@linkplain datumFactory datum}
     * or {@linkplain opFactory operation} factory. The default implementation returns the
     * {@code code} unchanged.
     *
     * @param code The code given to this factory.
     * @return The code to give to the underlying factories.
     *
     * @since 2.4
     */
    protected String toBackingFactoryCode(final String code) {
        return code;
    }
}
