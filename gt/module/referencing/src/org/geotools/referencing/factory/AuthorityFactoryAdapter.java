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
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;

// Geotools dependencies
import org.geotools.util.WeakHashSet;
import org.geotools.factory.Hints;
import org.geotools.factory.Factory;
import org.geotools.factory.AbstractFactory;
import org.geotools.factory.OptionalFactory;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.Utilities;


/**
 * An authority factory which delegates {@linkplain CoordinateReferenceSystem CRS},
 * {@linkplain CoordinateSystem CS} or {@linkplain Datum datum} objects creation to
 * some other factory implementations. This class provides a set of {@code replace(...)}
 * methods to be overriden by subclasses in order to replace some
 * {@linkplain CoordinateReferenceSystem CRS}, {@linkplain CoordinateSystem CS} or
 * {@linkplain Datum datum} objects by other ones. The replacement rules are determined by the
 * subclass being used. For example the {@link OrderedAxisAuthorityFactory} subclass can replace
 * {@linkplain CoordinateSystem coordinate systems} using (<var>latitude</var>, <var>longitude</var>)
 * axis order by coordinate systems using (<var>longitude</var>, <var>latitude</var>) axis order.
 * <p>
 * All constructors are protected because this class must be subclassed in order to determine
 * which of the {@link DatumAuthorityFactory}, {@link CSAuthorityFactory} and
 * {@link CRSAuthorityFactory} interfaces to implement.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Use generic types for all {@code replace(...)} methods when we will be allowed
 *       to compile for J2SE 1.5, and remove casts in all {@code createXXX(...)} methods.
 */
public class AuthorityFactoryAdapter extends AbstractAuthorityFactory implements OptionalFactory {
    /**
     * A set of default factories to be used if none were found if {@link #datumFactory},
     * {@link #csFactory}, {@link #crsFactory} or {@link #operationFactory}. Will be created
     * only when first needed.
     */
    private transient FactoryGroup factories;

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
     * The coordinate operation factory. Will be created only when first needed.
     */
    private transient CoordinateOperationFactory opFactory;

    /**
     * A pool of modified objects created up to date.
     */
    final WeakHashSet pool = new WeakHashSet();

    /**
     * Creates a wrapper around the specified factory. The {@link #priority priority} field
     * will be set to the same value than the specified factory. Subclasses should override
     * the {@link #getPriority()} method if they want to set a higher or lower priority for
     * this instance.
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
     * the {@link #getPriority()} method if they want to set a higher or lower priority for this
     * instance.
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

        store(Hints.               DATUM_AUTHORITY_FACTORY, this.    datumFactory = datumFactory);
        store(Hints.                  CS_AUTHORITY_FACTORY, this.       csFactory =    csFactory);
        store(Hints.                 CRS_AUTHORITY_FACTORY, this.      crsFactory =   crsFactory);
        store(Hints.COORDINATE_OPERATION_AUTHORITY_FACTORY, this.operationFactory =    opFactory);
    }

    /**
     * Creates a wrappers around the default factories for the specified authority.
     * The factories are fetched using {@link FactoryFinder}.
     * <p>
     * <strong>WARNING:</strong> Do not invoke this constructor from a subclass to be registered in
     * a {@code META-INF/services/} file for use by {@link FactoryFinder}. It may lead to recursive
     * calls until a {@link StackOverflowError} is thrown.
     *
     * @param  authority The authority to wraps (example: {@code "EPSG"}). If {@code null},
     *         then all authority factories must be explicitly specified in the set of hints.
     * @param  hints An optional set of hints, or {@code null} if none.
     * @throws FactoryRegistryException if at least one factory can not be obtained.
     */
    protected AuthorityFactoryAdapter(final String authority, final Hints hints)
            throws FactoryRegistryException
    {
        this(FactoryFinder.getCRSAuthorityFactory                (authority, hints),
             FactoryFinder.getCSAuthorityFactory                 (authority, hints),
             FactoryFinder.getDatumAuthorityFactory              (authority, hints),
             FactoryFinder.getCoordinateOperationAuthorityFactory(authority, hints));
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
     * Returns the implementation hint for the specified factory, or {@code null} if none.
     */
    private static Object getHint(final AuthorityFactory factory, final Hints.Key key) {
        if (factory instanceof Factory) {
            return ((Factory) factory).getImplementationHints().get(key);
        }
        return null;
    }

    /**
     * Returns the {@linkplain #hints hints} extented will all hints specified in dependencies.
     * This is used for fetching a new factory.
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
     * Suggests a factory group to use for the specified authority factory. The {@code factory}
     * parameter is usually one of {@link #datumFactory}, {@link #csFactory}, {@link #crsFactory}
     * or {@link #operationFactory} field. It may be {@code null}.
     */
    final FactoryGroup getFactoryGroup(final AuthorityFactory factory) {
        if (factory instanceof DirectAuthorityFactory) {
            return ((DirectAuthorityFactory) factory).factories;
        }
        if (factories == null) {
            factories = FactoryGroup.createInstance(hints());
        }
        return factories;
    }

    /**
     * Returns a coordinate operation factory for this adapter.
     */
    private final CoordinateOperationFactory getCoordinateOperationFactory() {
        if (opFactory == null) {
            final Object candidate = getHint(operationFactory, Hints.COORDINATE_OPERATION_FACTORY);
            if (candidate instanceof CoordinateOperationFactory) {
                opFactory = (CoordinateOperationFactory) candidate;
            } else {
                opFactory = FactoryFinder.getCoordinateOperationFactory(hints());
            }
        }
        return opFactory;
    }

    /**
     * Returns the priority for this factory. Priorities are used by {@link FactoryFinder} for
     * selecting a preferred factory when many are found for the same service. The default
     * implementation returns <code>{@linkplain #priority priority} + 1</code>, which implies
     * that this adapter has precedence over the wrapped factories. Subclasses should override
     * this method if they want a different priority order for this instance.
     *
     * @since 2.3
     */
    public int getPriority() {
        return priority + 1;
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
     * implementation checks if there is a {@linkplain #replace(Datum) datum replacement} or a
     * {@linkplain #replace(CoordinateSystem) coordinate system replacement}. If there is at
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
        CoordinateReferenceSystem modified;
        if (crs instanceof GeneralDerivedCRS) {
            final GeneralDerivedCRS         derivedCRS = (GeneralDerivedCRS) crs;
            final CoordinateReferenceSystem oldBaseCRS = derivedCRS.getBaseCRS();
            final CoordinateReferenceSystem    baseCRS = replace(oldBaseCRS);
            if (sameCS && Utilities.equals(baseCRS, oldBaseCRS)) {
                return crs;
            }
            final Map         properties = getProperties(crs);
            final FactoryGroup factories = getFactoryGroup(crsFactory);
            final CRSFactory  crsFactory = factories.getCRSFactory();
            Conversion          fromBase = derivedCRS.getConversionFromBase();
            fromBase = new DefiningConversion(getProperties(fromBase),
                           fromBase.getMethod(), fromBase.getParameterValues());
            if (crs instanceof ProjectedCRS) {
                modified = factories.createProjectedCRS(properties, (GeographicCRS) baseCRS,
                                                    fromBase, (CartesianCS) cs);
            } else {
                // TODO: Need a createDerivedCRS method.
                throw new FactoryException(Errors.format(ErrorKeys.UNSUPPORTED_CRS_$1,
                                           crs.getName().getCode()));
            }
        } else if (sameCS) {
            return crs;
        } else {
            final Map         properties = getProperties(crs);
            final FactoryGroup factories = getFactoryGroup(crsFactory);
            final CRSFactory  crsFactory = factories.getCRSFactory();
            if (crs instanceof GeographicCRS) {
                modified = crsFactory.createGeographicCRS(properties, (GeodeticDatum) datum, (EllipsoidalCS) cs);
            } else if (crs instanceof GeocentricCRS) {
                final GeodeticDatum gd = (GeodeticDatum) datum;
                if (cs instanceof CartesianCS) {
                    modified = crsFactory.createGeocentricCRS(properties, gd, (CartesianCS) cs);
                } else {
                    modified = crsFactory.createGeocentricCRS(properties, gd, (SphericalCS) cs);
                }
            } else if (crs instanceof VerticalCRS) {
                modified = crsFactory.createVerticalCRS(properties, (VerticalDatum) datum, (VerticalCS) cs);
            } else if (crs instanceof TemporalCRS) {
                modified = crsFactory.createTemporalCRS(properties, (TemporalDatum) datum, (TimeCS) cs);
            } else if (crs instanceof ImageCRS) {
                modified = crsFactory.createImageCRS(properties, (ImageDatum) datum, (AffineCS) cs);
            } else if (crs instanceof EngineeringCRS) {
                modified = crsFactory.createEngineeringCRS(properties, (EngineeringDatum) datum, cs);
            } else if (crs instanceof CompoundCRS) {
                final List/*<CoordinateReferenceSystem>*/ elements =
                        ((CompoundCRS) crs).getCoordinateReferenceSystems();
                final CoordinateReferenceSystem[] m = new CoordinateReferenceSystem[elements.size()];
                for (int i=0; i<m.length; i++) {
                    m[i] = replace((CoordinateReferenceSystem) elements.get(i));
                }
                modified = crsFactory.createCompoundCRS(properties, m);
            } else {
                throw new FactoryException(Errors.format(ErrorKeys.UNSUPPORTED_CRS_$1,
                                                         crs.getName().getCode()));
            }
        }
        modified = (CoordinateReferenceSystem) pool.canonicalize(modified);
        return modified;
    }

    /**
     * Replaces (if needed) the specified coordinate operation. The default implementation checks if
     * there is a source or target {@linkplain #replace(CoordinateReferenceSystem) CRS replacement}.
     * If there is at least one of those, then this method returns a new coordinate operation using
     * the new CRS.
     *
     * @param  operation The coordinate operation to replace.
     * @return A new operation, or {@code operation} if no change were needed.
     * @throws FactoryException if an error occured while creating the new operation object.
     */
    protected CoordinateOperation replace(final CoordinateOperation operation)
            throws FactoryException
    {
        final CoordinateReferenceSystem oldSrcCRS = operation.getSourceCRS();
        final CoordinateReferenceSystem oldTgtCRS = operation.getTargetCRS();
        final CoordinateReferenceSystem sourceCRS = replace(oldSrcCRS);
        final CoordinateReferenceSystem targetCRS = replace(oldTgtCRS);
        if (Utilities.equals(oldSrcCRS, sourceCRS) && Utilities.equals(oldTgtCRS, targetCRS)) {
            return operation;
        }
        CoordinateOperation modified;
        modified = getCoordinateOperationFactory().createOperation(sourceCRS, targetCRS);
        modified = (CoordinateOperation) pool.canonicalize(modified);
        return modified;
    }

    /**
     * Returns the properties to be given to an object replacing an original one. If the new object
     * keep the same authority, then all metadata are preserved. Otherwise (i.e. if a new authority
     * is given to the new object), then the old identifiers will be removed from the new object
     * metadata.
     *
     * @param  object The original object.
     * @return The properties to be given to the object created as a substitute of {@code object}.
     */
    final Map getProperties(final IdentifiedObject object) {
        final Citation authority = getAuthority();
        if (!Utilities.equals(authority, object.getName().getAuthority())) {
            return AbstractIdentifiedObject.getProperties(object, authority);
        } else {
            return AbstractIdentifiedObject.getProperties(object);
        }
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
     * Returns the vendor responsible for creating this factory implementation. The default
     * implementation delegates to the {@linkplain #crsFactory underlying CRS factory} with
     * no change.
     */
    public Citation getVendor() {
        return crsFactory.getVendor();
    }

    /**
     * Returns the organization or party responsible for definition and maintenance of the
     * database. The default implementation delegates to the {@linkplain #crsFactory underlying
     * CRS factory} with no change. Subclasses may consider to override this method if at least
     * one {@code replace(...)} methods performs a structural change, for example changing axis
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
     * {@linkplain #operationFactory underlying operation factory} with no change.
     */
    public ParameterDescriptor createParameterDescriptor(final String code) throws FactoryException {
        return getFactory("createParameterDescriptor").createParameterDescriptor(code);
    }

    /**
     * Creates an operation method from a code. The default implementation delegates to the
     * {@linkplain #operationFactory underlying operation factory} with no change.
     */
    public OperationMethod createOperationMethod(final String code) throws FactoryException {
        return getFactory("createOperationMethod").createOperationMethod(code);
    }

    /**
     * Creates an operation from a single operation code.
     * The default implementation first invokes the same method from the
     * {@linkplain #operationFactory underlying operation factory}, and next invokes
     * {@link #replace(CoordinateOperation) replace}.
     */
    public CoordinateOperation createCoordinateOperation(final String code) throws FactoryException {
        return replace(operationFactory.createCoordinateOperation(code));
    }

    /**
     * Creates an operation from coordinate reference system codes.
     * The default implementation first invokes the same method from the
     * {@linkplain #operationFactory underlying operation factory}, and next invokes
     * {@link #replace(CoordinateOperation) replace} for each operations.
     */
    public Set/*<CoordinateOperation>*/ createFromCoordinateReferenceSystemCodes(
                                        final String sourceCode, final String targetCode)
            throws FactoryException
    {
        final Set/*<CoordinateOperation>*/ operations, modified;
        operations = operationFactory.createFromCoordinateReferenceSystemCodes(sourceCode, targetCode);
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
     * Releases resources immediately instead of waiting for the garbage collector. This method do
     * <strong>not</strong> dispose the resources of wrapped factories (e.g. {@link #crsFactory}),
     * because they may still in use by other classes.
     */
    public void dispose() throws FactoryException {
        pool.clear();
        super.dispose();
    }
}
