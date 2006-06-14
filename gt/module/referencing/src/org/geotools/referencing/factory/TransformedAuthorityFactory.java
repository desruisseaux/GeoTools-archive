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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Comparator;
import java.util.LinkedHashSet;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
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
import org.geotools.factory.FactoryRegistryException;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.Utilities;


/**
 * An authority factory which returns modified {@linkplain CoordinateReferenceSystem CRS},
 * {@linkplain CoordinateSystem CS} or {@linkplain Datum datum} objects from other factory
 * implementations. This class provides a set of {@code replace(...)} methods to be overriden
 * by subclasses in order to replace some {@linkplain CoordinateReferenceSystem CRS},
 * {@linkplain CoordinateSystem CS} or {@linkplain Datum datum} objects by other ones.
 * The replacement rules are determined by the subclass being used. For example the
 * {@link OrderedAxisAuthorityFactory} subclass can replace {@linkplain CoordinateSystem
 * coordinate systems} using (<var>latitude</var>, <var>longitude</var>)
 * axis order by coordinate systems using (<var>longitude</var>, <var>latitude</var>) axis order.
 * <p>
 * All constructors are protected because this class must be subclassed in order to determine
 * which of the {@link DatumAuthorityFactory}, {@link CSAuthorityFactory} and
 * {@link CRSAuthorityFactory} interfaces to implement.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Use generic types for all {@code replace(...)} methods when we will be allowed
 *       to compile for J2SE 1.5, and remove casts in all {@code createXXX(...)} methods.
 */
public class TransformedAuthorityFactory extends AuthorityFactoryAdapter {
    /**
     * A set of default factories to be used if none were found if {@link #datumFactory},
     * {@link #csFactory}, {@link #crsFactory} or {@link #operationFactory}. Will be created
     * only when first needed.
     */
    private transient FactoryGroup factories;

    /**
     * The coordinate operation factory. Will be created only when first needed.
     */
    private transient CoordinateOperationFactory opFactory;

    /**
     * A pool of modified objects created up to date.
     */
    private final WeakHashSet pool = new WeakHashSet();

    /**
     * Creates a wrapper around the specified factory.
     *
     * @param factory The factory to wrap.
     */
    protected TransformedAuthorityFactory(final AuthorityFactory factory) {
        super(factory);
    }

    /**
     * Creates a wrapper around the specified factories.
     *
     * @param crsFactory   The {@linkplain CoordinateReferenceSystem coordinate reference system}
     *                     authority factory, or {@code null}.
     * @param csFactory    The {@linkplain CoordinateSystem coordinate system} authority factory,
     *                     or {@code null}.
     * @param datumFactory The {@linkplain Datum datum} authority factory, or {@code null}.
     * @param opFactory    The {@linkplain CoordinateOperation coordinate operation} authority
     *                     factory, or {@code null}.
     */
    protected TransformedAuthorityFactory(final CRSAuthorityFactory                crsFactory,
                                          final CSAuthorityFactory                  csFactory,
                                          final DatumAuthorityFactory            datumFactory,
                                          final CoordinateOperationAuthorityFactory opFactory)
    {
        super(crsFactory, csFactory, datumFactory, opFactory);
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
    TransformedAuthorityFactory(final String authority, final Hints hints)
            throws FactoryRegistryException
    {
        super(FactoryFinder.getCRSAuthorityFactory                (authority, hints),
              FactoryFinder.getCSAuthorityFactory                 (authority, hints),
              FactoryFinder.getDatumAuthorityFactory              (authority, hints),
              FactoryFinder.getCoordinateOperationAuthorityFactory(authority, hints));
    }

    /**
     * Suggests a factory group to use for the specified authority factory. The {@code factory}
     * parameter is usually one of {@link #datumFactory}, {@link #csFactory}, {@link #crsFactory}
     * or {@link #operationFactory} field. It may be {@code null}.
     */
    private FactoryGroup getFactoryGroup(final AuthorityFactory factory) {
        if (factory instanceof DirectAuthorityFactory) {
            return ((DirectAuthorityFactory) factory).factories;
        }
        if (factories == null) {
            factories = FactoryGroup.createInstance(hints());
        }
        return factories;
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
     */
    public int getPriority() {
        return priority + 1;
    }

    /**
     * Replaces the specified unit, if applicable. This method is invoked automatically by the
     * {@link #replace(CoordinateSystem)} method. The default implementation returns the unit
     * unchanged.
     *
     * @param  units The units to replace.
     * @return The new units, or {@code units} if no change were needed.
     * @throws FactoryException if an error occured while creating the new units.
     */
    //@Override
    protected Unit replace(final Unit units) throws FactoryException {
        return units;
    }

    /**
     * Replaces the specified direction, if applicable. This method is invoked automatically by the
     * {@link #replace(CoordinateSystem)} method. The default implementation returns the axis
     * direction unchanged.
     *
     * @param  cs The axis direction to replace.
     * @return The new direction, or {@code direction} if no change were needed.
     * @throws FactoryException if an error occured while creating the new axis direction.
     */
    protected AxisDirection replace(final AxisDirection direction) throws FactoryException {
        return direction;
    }

    /**
     * Replaces (if needed) the specified axis by a new one. The default implementation
     * invokes {@link #replace(Unit)} and {@link #replace(AxisDirection)}.
     *
     * @param  cs The coordinate system axis to replace.
     * @return The new coordinate system axis, or {@code axis} if no change were needed.
     * @throws FactoryException if an error occured while creating the new coordinate system axis.
     */
    //@Override
    protected CoordinateSystemAxis replace(CoordinateSystemAxis axis) throws FactoryException {
        final AxisDirection oldDirection = axis.getDirection();
        final AxisDirection newDirection = replace(oldDirection);
        final Unit          oldUnits     = axis.getUnit();
        final Unit          newUnits     = replace(oldUnits);
        if (!oldDirection.equals(newDirection) || !oldUnits.equals(newUnits)) {
            if (factories == null) {
                factories = getFactoryGroup(csFactory);
            }
            final CSFactory csFactory = factories.getCSFactory();
            final Map properties = getProperties(axis);
            axis = csFactory.createCoordinateSystemAxis(properties,
                    axis.getAbbreviation(), newDirection, newUnits);
            axis = (CoordinateSystemAxis) pool.canonicalize(axis);
        }
        return axis;
    }

    /**
     * Replaces (if needed) the specified coordinate system by a new one. The default implementation
     * invokes {@link #replace(CoordinateSystemAxis) replace} for each axis. In addition, axis are
     * sorted if this factory implements the {@link Comparator} interface.
     *
     * @param  cs The coordinate system to replace.
     * @return The new coordinate system, or {@code cs} if no change were needed.
     * @throws FactoryException if an error occured while creating the new coordinate system.
     */
    //@Override
    protected CoordinateSystem replace(final CoordinateSystem cs) throws FactoryException {
        final int dimension = cs.getDimension();
        final CoordinateSystemAxis[] orderedAxis = new CoordinateSystemAxis[dimension];
        FactoryGroup factories = null;
        for (int i=0; i<dimension; i++) {
            orderedAxis[i] = replace(cs.getAxis(i));
        }
        if (this instanceof Comparator) {
            Arrays.sort(orderedAxis, (Comparator) this);
        }
        for (int i=0; i<dimension; i++) {
            if (!orderedAxis[i].equals(cs.getAxis(i))) {
                CoordinateSystem modified = createCS(cs.getClass(), getProperties(cs), orderedAxis);
                assert Utilities.sameInterfaces(cs.getClass(), modified.getClass(), CoordinateSystem.class);
                modified = (CoordinateSystem) pool.canonicalize(modified);
                return modified;
            }
        }
        // All axis are identical - the CS was actually not changed.
        return cs;
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
    //@Override
    protected Datum replace(final Datum datum) throws FactoryException {
        return super.replace(datum);
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
    //@Override
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
    //@Override
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
     * Creates a new coordinate system of the specified kind. This method is invoked automatically
     * by {@link #replace(CoordinateSystem)} after it determined that the axis need to be changed.
     *
     * @param  type The coordinate system type to create.
     * @param  properties The properties to gives to the new coordinate system.
     * @param  axis The axis to give to the new coordinate system. Subclasses are allowed to write
     *              directly in this array (no need to copy it).
     * @return A new coordinate system of the specified kind with the specified axis.
     * @throws FactoryException if the coordinate system can't be created.
     */
    private CoordinateSystem createCS(final Class/*<CoordinateSystem>*/ type,
                                      final Map properties,
                                      final CoordinateSystemAxis[] axis)
            throws FactoryException
    {
        final int          dimension = axis.length;
        final FactoryGroup factories = getFactoryGroup(csFactory);
        final CSFactory    csFactory = factories.getCSFactory();
        if (CartesianCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 2: return csFactory.createCartesianCS(properties, axis[0], axis[1]);
                case 3: return csFactory.createCartesianCS(properties, axis[0], axis[1], axis[2]);
            }
        } else
        if (EllipsoidalCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 2: return csFactory.createEllipsoidalCS(properties, axis[0], axis[1]);
                case 3: return csFactory.createEllipsoidalCS(properties, axis[0], axis[1], axis[2]);
            }
        } else
        if (SphericalCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 3: return csFactory.createSphericalCS(properties, axis[0], axis[1], axis[2]);
            }
        } else
        if (CylindricalCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 3: return csFactory.createCylindricalCS(properties, axis[0], axis[1], axis[2]);
            }
        } else
        if (PolarCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 2: return csFactory.createPolarCS(properties, axis[0], axis[1]);
            }
        } else
        if (VerticalCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 1: return csFactory.createVerticalCS(properties, axis[0]);
            }
        } else
        if (TimeCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 1: return csFactory.createTimeCS(properties, axis[0]);
            }
        } else
        if (LinearCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 1: return csFactory.createLinearCS(properties, axis[0]);
            }
        } else
        if (UserDefinedCS.class.isAssignableFrom(type)) {
            switch (dimension) {
                case 2: return csFactory.createUserDefinedCS(properties, axis[0], axis[1]);
                case 3: return csFactory.createUserDefinedCS(properties, axis[0], axis[1], axis[2]);
            }
        }
        throw new FactoryException(Errors.format(ErrorKeys.UNSUPPORTED_COORDINATE_SYSTEM_$1,
                                   Utilities.getShortName(type)));
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
    private Map getProperties(final IdentifiedObject object) {
        final Citation authority = getAuthority();
        if (!Utilities.equals(authority, object.getName().getAuthority())) {
            return AbstractIdentifiedObject.getProperties(object, authority);
        } else {
            return AbstractIdentifiedObject.getProperties(object);
        }
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
     * <strong>not</strong> dispose the resources of wrapped factories (e.g. {@link #crsFactory
     * crsFactory}), because they may still in use by other classes.
     */
    public synchronized void dispose() throws FactoryException {
        pool.clear();
        super.dispose();
    }
}
