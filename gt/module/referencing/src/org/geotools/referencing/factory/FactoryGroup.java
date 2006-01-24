/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.units.ConversionException;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.parameter.Parameters;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.operation.DefiningConversion;  // For javadoc
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.matrix.MatrixFactory;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.cs.AbstractCS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.XArray;
import org.geotools.util.Singleton;


/**
 * A set of utilities methods working on factories. Many of those methods requires more than
 * one factory. Consequently, they can't be a method in a single factory. Furthermore, since
 * they are helper methods and somewhat implementation-dependent, they are not part of GeoAPI.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FactoryGroup {
    /**
     * Hint for the {@code FactoryGroup} instance to use. This hint is a somewhat low-level details
     * of Geotools implementation, which is why this hint is not listed in the {@link Hints} class.
     * The {@link Hints#CRS_FACTORY CRS_FACTORY} hint and its friend should be suffisient in most
     * cases.
     *
     * @see #createInstance
     *
     * @since 2.2
     */
    public static final Hints.Key HINT_KEY = new Hints.Key(FactoryGroup.class);

    /**
     * The {@linkplain Datum datum} factory.
     * If null, then a default factory will be created only when first needed.
     */
    private DatumFactory datumFactory;

    /**
     * The {@linkplain CoordinateSystem coordinate system} factory.
     * If null, then a default factory will be created only when first needed.
     */
    private CSFactory csFactory;

    /**
     * The {@linkplain CoordinateReferenceSystem coordinate reference system} factory.
     * If null, then a default factory will be created only when first needed.
     */
    private CRSFactory crsFactory;

    /**
     * The {@linkplain CoordinateOperation coordinate operation} factory.
     * If null, then a default factory will be created only when first needed.
     *
     * @since 2.2
     */
    private CoordinateOperationFactory opFactory;

    /**
     * The {@linkplain MathTransform math transform} factory.
     * If null, then a default factory will be created only when first needed.
     */
    private MathTransformFactory mtFactory;

    /**
     * Constructs an instance using the default factories.
     * Default factories are:
     *
     * <blockquote><pre>
     * FactoryFinder.{@linkplain FactoryFinder#getDatumFactory         getDatumFactory}(null);
     * FactoryFinder.{@linkplain FactoryFinder#getCSFactory            getCSFactory}(null);
     * FactoryFinder.{@linkplain FactoryFinder#getCRSFactory           getCRSFactory}(null);
     * FactoryFinder.{@linkplain FactoryFinder#getMathTransformFactory MathTransformFactory}(null);
     * </pre></blockquote>
     */
    public FactoryGroup() {
    }

    /**
     * Constructs an instance using the specified factories. If any factory is null,
     * a default instance will be created by {@link FactoryFinder} when first needed.
     *
     * @param datumFactory The {@linkplain Datum datum} factory.
     * @param    csFactory The {@linkplain CoordinateSystem coordinate system} factory.
     * @param   crsFactory The {@linkplain CoordinateReferenceSystem coordinate reference system}
     *                     factory.
     * @param    mtFactory The {@linkplain MathTransform math transform} factory.
     */
    public FactoryGroup(final DatumFactory      datumFactory,
                        final CSFactory            csFactory,
                        final CRSFactory          crsFactory,
                        final MathTransformFactory mtFactory)
    {
        this.datumFactory = datumFactory;
        this.csFactory    =    csFactory;
        this.crsFactory   =   crsFactory;
        this.mtFactory    =    mtFactory;
    }

    /**
     * Constructs an instance using the factories initialized with the specified hints.
     *
     * @see #createInstance
     */
    public FactoryGroup(Hints hints) {
        /*
         * If hints are provided, we will fetch factory immediately (instead of storing the hints
         * in an inner field) because most factories will retain few hints, while the Hints map
         * may contains big objects. If no hints were provided, we will construct factories only
         * when first needed.
         */
        if (hints!=null && !hints.isEmpty()) {
            if (!hints.containsKey(HINT_KEY)) {
                hints = new Hints(hints);
                hints.putAll(hints());
            }
            datumFactory = FactoryFinder.getDatumFactory              (hints);
            csFactory    = FactoryFinder.getCSFactory                 (hints);
            crsFactory   = FactoryFinder.getCRSFactory                (hints);
            opFactory    = FactoryFinder.getCoordinateOperationFactory(hints);
            mtFactory    = FactoryFinder.getMathTransformFactory      (hints);
        }
    }

    /**
     * Creates an instance from the specified hints. If the hints contains a {@link #HINT_KEY}
     * value, then the specified factory group will be returned. Otherwise, a new one will be
     * created.
     *
     * @param  hints The hints, or {@code null} if none.
     * @return A factory group created from the specified set of hints.
     *
     * @since 2.2
     */
    public static FactoryGroup createInstance(final Hints hints) {
        if (hints != null) {
            final FactoryGroup candidate = (FactoryGroup) hints.get(HINT_KEY);
            if (candidate != null) {
                return candidate;
            }
        }
        return new FactoryGroup(hints);
    }

    /**
     * Copies in the specified map all values for the {@link Hints#CRS_FACTORY CRS},
     * {@link Hints#CS_FACTORY CS}, {@link Hints#DATUM_FACTORY DATUM} and
     * {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM} {@code FACTORY} hints.
     * A {@link #HINT_KEY} is put in addition for this {@code FactoryGroup} instance,
     * but it should be considered as low-level detail. This method is provided as a helper
     * for implementation of {@link org.geotools.factory.Factory#getImplementationHints}
     * methods.
     *
     * @param hints The map to put hints into.
     *
     * @since 2.2
     */
    public void getHints(final Map hints) {
        hints.put(Hints.                 CRS_FACTORY, getCRSFactory());
        hints.put(Hints.                  CS_FACTORY, getCSFactory());
        hints.put(Hints.               DATUM_FACTORY, getDatumFactory());
        hints.put(Hints.COORDINATE_OPERATION_FACTORY, getCoordinateOperationFactory());
        hints.put(Hints.      MATH_TRANSFORM_FACTORY, getMathTransformFactory());
        hints.putAll(hints());
    }

    /**
     * Returns the hints to be used for lazy creation of <em>default</em> factories in various
     * {@code getFoo} methods. This is different from {@link #getHints} because the later may
     * returns non-default factories. The hint provided by this method is needed in order to
     * avoid never-ending loop when a factory creation requires a {@code FactoryGroup} instance
     * (for example in {@link DefaultCoordinateOperationFactory}).
     */
    private Hints hints() {
        return new Hints(HINT_KEY, this);
    }

    /**
     * Returns the {@linkplain Datum datum} factory.
     */
    public DatumFactory getDatumFactory() {
        if (datumFactory == null) {
            datumFactory = FactoryFinder.getDatumFactory(hints());
        }
        return datumFactory;
    }

    /**
     * Returns the {@linkplain CoordinateSystem coordinate system} factory.
     */
    public CSFactory getCSFactory() {
        if (csFactory == null) {
            csFactory = FactoryFinder.getCSFactory(hints());
        }
        return csFactory;
    }

    /**
     * Returns the {@linkplain CoordinateReferenceSystem coordinate reference system} factory.
     */
    public CRSFactory getCRSFactory() {
        if (crsFactory == null) {
            crsFactory = FactoryFinder.getCRSFactory(hints());
        }
        return crsFactory;
    }

    /**
     * Returns the {@linkplain CoordinateOperation coordinate operation} factory.
     *
     * @since 2.2
     */
    public CoordinateOperationFactory getCoordinateOperationFactory() {
        if (opFactory == null) {
            opFactory = FactoryFinder.getCoordinateOperationFactory(hints());
        }
        return opFactory;
    }

    /**
     * Returns the {@linkplain MathTransform math transform} factory.
     */
    public MathTransformFactory getMathTransformFactory() {
        if (mtFactory == null) {
            mtFactory = FactoryFinder.getMathTransformFactory(hints());
        }
        return mtFactory;
    }

    /**
     * Returns the operation method for the specified name. This method scans all operations
     * registered in the {@linkplain #getMathTransformFactory current math transform factory}.
     *
     * @param  method The case insensitive {@linkplain Identifier#getCode identifier code}
     *         of the operation method to search for (e.g. {@code "Transverse_Mercator"}).
     * @return The operation method.
     * @throws NoSuchIdentifierException if there is no operation method registered for the
     *         specified name.
     *
     * @see DefaultMathTransformFactory#getOperationMethod
     *
     * @since 2.2
     */
    public OperationMethod getOperationMethod(final String name)
            throws NoSuchIdentifierException
    {
        final MathTransformFactory mtFactory = getMathTransformFactory();
        if (mtFactory instanceof DefaultMathTransformFactory) {
            // Special processing for Geotools implementation.
            return ((DefaultMathTransformFactory) mtFactory).getOperationMethod(name);
        }
        // Not a geotools implementation. Scan all methods.
        final Set operations = mtFactory.getAvailableMethods(Operation.class);
        for (final Iterator it=operations.iterator(); it.hasNext();) {
            final OperationMethod method = (OperationMethod) it.next();
            if (AbstractIdentifiedObject.nameMatches(method, name)) {
                return method;
            }
        }
        throw new NoSuchIdentifierException(Errors.format(
                  ErrorKeys.NO_TRANSFORM_FOR_CLASSIFICATION_$1, name), name);
    }

    /**
     * Creates a transform from a group of parameters and add the method used to a list.
     * This variant of {@code createParameterizedTransform(...)} provide a way for
     * the client to keep trace of any {@linkplain OperationMethod operation method}
     * used by this factory. 
     *
     * @param  parameters The parameter values.
     * @param  methods A collection where to add the operation method that apply to the transform,
     *                 or {@code null} if none.
     * @return The parameterized transform.
     * @throws NoSuchIdentifierException if there is no transform registered for the method.
     * @throws FactoryException if the object creation failed. This exception is thrown
     *         if some required parameter has not been supplied, or has illegal value.
     *
     * @see MathTransformFactory#createParameterizedTransform
     */
    public MathTransform createParameterizedTransform(ParameterValueGroup parameters,
                                                      Collection          methods)
            throws NoSuchIdentifierException, FactoryException
    {
        final MathTransformFactory mtFactory = getMathTransformFactory();
        if (methods == null) {
            return mtFactory.createParameterizedTransform(parameters);
        }
        final MathTransform transform;
        if (mtFactory instanceof DefaultMathTransformFactory) {
            // Special processing for Geotools implementation.
            transform = ((DefaultMathTransformFactory) mtFactory)
                        .createParameterizedTransform(parameters, methods);
        } else {
            // Not a geotools implementation. Try to guess the method.
            transform = mtFactory.createParameterizedTransform(parameters);
            final Set operations = mtFactory.getAvailableMethods(Operation.class);
            final String classification = parameters.getDescriptor().getName().getCode();
            for (final Iterator it=operations.iterator(); it.hasNext();) {
                final OperationMethod method = (OperationMethod) it.next();
                if (AbstractIdentifiedObject.nameMatches(method.getParameters(), classification)) {
                    methods.add(method);
                    break;
                }
            }
        }
        return transform;
    }

    /**
     * Creates a {@linkplain #createParameterizedTransform parameterized transform} from a base
     * CRS to a derived CS. If the <code>"semi_major"</code> and <code>"semi_minor"</code>
     * parameters are not explicitly specified, they will be inferred from the
     * {@linkplain Ellipsoid ellipsoid} and added to {@code parameters}.
     * In addition, this method performs axis switch as needed. 
     *
     * @param  baseCRS The source coordinate reference system.
     * @param  parameters The parameter values for the transform.
     * @param  derivedCS the target coordinate system.
     * @param  methods A collection where to add the operation method that apply to the transform,
     *                 or {@code null} if none.
     * @return The parameterized transform.
     * @throws NoSuchIdentifierException if there is no transform registered for the method.
     * @throws FactoryException if the object creation failed. This exception is thrown
     *         if some required parameter has not been supplied, or has illegal value.
     */
    public MathTransform createBaseToDerived(final CoordinateReferenceSystem baseCRS,
                                             final ParameterValueGroup       parameters,
                                             final CoordinateSystem          derivedCS,
                                             final Collection                methods)
            throws NoSuchIdentifierException, FactoryException
    {
        /*
         * If the user's parameter do not contains semi-major and semi-minor axis length, infers
         * them from the ellipsoid. This is a convenience service since the user often omit those
         * parameters (because they duplicate datum information).
         */
        final Ellipsoid ellipsoid = CRSUtilities.getHeadGeoEllipsoid(baseCRS);
        if (ellipsoid != null) {
            final Unit axisUnit = ellipsoid.getAxisUnit();
            Parameters.ensureSet(parameters, "semi_major", ellipsoid.getSemiMajorAxis(), axisUnit, false);
            Parameters.ensureSet(parameters, "semi_minor", ellipsoid.getSemiMinorAxis(), axisUnit, false);
        }
        /*
         * Computes matrix for swapping axis and performing units conversion.
         * There is one matrix to apply before projection on (longitude,latitude)
         * coordinates, and one matrix to apply after projection on (easting,northing)
         * coordinates.
         */
        final CoordinateSystem sourceCS = baseCRS.getCoordinateSystem();
        final Matrix swap1, swap3;
        try {
            swap1 = AbstractCS.swapAndScaleAxis(sourceCS, AbstractCS.standard(sourceCS));
            swap3 = AbstractCS.swapAndScaleAxis(AbstractCS.standard(derivedCS), derivedCS);
        } catch (IllegalArgumentException cause) {
            // User-specified axis don't match.
            throw new FactoryException(cause);
        } catch (ConversionException cause) {
            // A Unit conversion is non-linear.
            throw new FactoryException(cause);
        }
        /*
         * Prepares the concatenation of the matrix computed above and the projection.
         * Note that at this stage, the dimensions between each step may not be compatible.
         * For example the projection (step2) is usually two-dimensional while the source
         * coordinate system (step1) may be three-dimensional if it has a height.
         */
        MathTransformFactory  mtFactory = getMathTransformFactory();
        MathTransform step1 = mtFactory.createAffineTransform(swap1);
        MathTransform step2 = createParameterizedTransform(parameters, methods);
        MathTransform step3 = mtFactory.createAffineTransform(swap3);
        /*
         * If the target coordinate system has a height, instructs the projection to pass
         * the height unchanged from the base CRS to the target CRS. After this block, the
         * dimensions of 'step2' and 'step3' should match.
         */
        final int numTrailingOrdinates = step3.getSourceDimensions() - step2.getTargetDimensions();
        if (numTrailingOrdinates > 0) {
            step2 = mtFactory.createPassThroughTransform(0, step2, numTrailingOrdinates);
        }
        /*
         * If the source CS has a height but the target CS doesn't, drops the extra coordinates.
         * After this block, the dimensions of 'step1' and 'step2' should match.
         */
        final int sourceDim = step1.getTargetDimensions();
        final int targetDim = step2.getSourceDimensions();
        if (sourceDim > targetDim) {
            final Matrix drop = MatrixFactory.create(targetDim+1, sourceDim+1);
            drop.setElement(targetDim, sourceDim, 1);
            step1 = mtFactory.createConcatenatedTransform(
                    mtFactory.createAffineTransform(drop), step1);
        }
        return mtFactory.createConcatenatedTransform(
               mtFactory.createConcatenatedTransform(step1, step2), step3);
    }

    /**
     * Creates a projected coordinate reference system from a conversion.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  baseCRS Geographic coordinate reference system to base projection on.
     * @param  conversionFromBase The {@linkplain DefiningConversion defining conversion}.
     * @param  derivedCS The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     *
     * @todo Current implementation creates directly a Geotools implementation, because there
     *       is not yet a suitable method in GeoAPI interfaces.
     */
    public ProjectedCRS createProjectedCRS(final Map           properties,
                                           final GeographicCRS baseCRS,
                                           final Conversion    conversionFromBase,
                                           final CartesianCS   derivedCS)
            throws FactoryException
    {
        final MathTransform mt;
        mt = createBaseToDerived(baseCRS, conversionFromBase.getParameterValues(), derivedCS, null);
        return new org.geotools.referencing.crs.DefaultProjectedCRS(
                    properties, conversionFromBase, baseCRS, mt, derivedCS);
    }

    /**
     * Creates a projected coordinate reference system from a set of parameters. If the
     * {@code "semi_major"} and {@code "semi_minor"} parameters are not explicitly specified,
     * they will be inferred from the {@linkplain Ellipsoid ellipsoid} and added to the
     * {@code parameters}. This method also checks for axis order and unit conversions.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  baseCRS Geographic coordinate reference system to base projection on.
     * @param  method The operation method, or {@code null} for a default one.
     * @param  parameters The parameter values to give to the projection.
     * @param  derivedCS The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     */
    public ProjectedCRS createProjectedCRS(Map                 properties,
                                           GeographicCRS          baseCRS,
                                           OperationMethod         method,
                                           ParameterValueGroup parameters,
                                           CartesianCS          derivedCS)
            throws FactoryException
    {
        final Singleton methods = (method==null) ? new Singleton() : null;
        final MathTransform mt = createBaseToDerived(baseCRS, parameters, derivedCS, methods);
        if (method == null) {
            method = (OperationMethod) methods.get();
        }
        return getCRSFactory().createProjectedCRS(properties, method, baseCRS, mt, derivedCS);
    }

    /**
     * Converts a 2D&nbsp;+&nbsp;1D compound CRS into a 3D CRS, if possible. More specifically,
     * if the specified {@linkplain CompoundCRS compound CRS} is made of a
     * {@linkplain GeographicCRS geographic} (or {@linkplain ProjectedCRS projected}) and a
     * {@linkplain VerticalCRS vertical} CRS, and if the vertical CRS datum type is
     * {@linkplain VerticalDatumType#ELLIPSOIDAL height above the ellipsoid}, then this method
     * converts the compound CRS in a single 3D CRS. Otherwise, the {@code crs} argument is
     * returned unchanged.
     *
     * @param  crs The compound CRS to converts in a 3D geographic or projected CRS.
     * @return The 3D geographic or projected CRS, or {@code crs} if the change can't be applied.
     * @throws FactoryException if the object creation failed.
     */
    public CoordinateReferenceSystem toGeodetic3D(final CompoundCRS crs) throws FactoryException {
        final SingleCRS[] components = DefaultCompoundCRS.getSingleCRS(crs);
        SingleCRS   horizontal = null;
        VerticalCRS vertical   = null;
        int hi=0, vi=0;
        for (int i=0; i<components.length; i++) {
            final SingleCRS candidate = components[i];
            if (candidate instanceof VerticalCRS) {
                if (vertical == null) {
                    vertical = (VerticalCRS) candidate;
                    if (VerticalDatumType.ELLIPSOIDAL.equals( // TODO: remove cast with J2SE 1.5.
                        ((VerticalDatum) vertical.getDatum()).getVerticalDatumType()))
                    {
                        vi = i;
                        continue;
                    }
                }
                return crs;
            }
            if (candidate instanceof GeographicCRS ||
                candidate instanceof  ProjectedCRS)
            {
                if (horizontal == null) {
                    horizontal = (SingleCRS) candidate;
                    if (horizontal.getCoordinateSystem().getDimension() == 2) {
                        hi = i;
                        continue;
                    }
                }
                return crs;
            }
        }
        if (horizontal!=null && vertical!=null && Math.abs(vi-hi)==1) {
            /*
             * Exactly one horizontal and one vertical CRS has been found, and those two CRS are
             * consecutives. Constructs the new 3D CS. If the two above-cited components are the
             * only one, the result is returned directly. Otherwise, a new compound CRS is created.
             */
            final boolean classic = (hi < vi);
            final SingleCRS single = toGeodetic3D(components.length == 2 ? crs : null,
                                                  horizontal, vertical, classic);
            if (components.length == 2) {
                return single;
            }
            final CoordinateReferenceSystem[] c=new CoordinateReferenceSystem[components.length-1];
            final int i = classic ? hi : vi;
            System.arraycopy(components, 0, c, 0, i);
            c[i] = single;
            System.arraycopy(components, i+2, c, i+1, components.length-(i+2));
            return crsFactory.createCompoundCRS(AbstractIdentifiedObject.getProperties(crs), c);
        }
        return crs;
    }

    /**
     * Implementation of {@link #toGeodetic3D(CompoundCRS)} invoked after the horizontal and
     * vertical parts have been identified. This method may invokes itself recursively if the
     * horizontal CRS is a derived one.
     *
     * @param  crs        The compound CRS to converts in a 3D geographic CRS, or {@code null}.
     *                    Used only in order to infer the name properties of objects to create.
     * @param  horizontal The horizontal component of {@code crs}.
     * @param  vertical   The vertical   component of {@code crs}.
     * @param  classic    {@code true} if the horizontal component appears before the vertical
     *                    component, or {@code false} for the converse.
     * @return The 3D geographic or projected CRS.
     * @throws FactoryException if the object creation failed.
     */
    private SingleCRS toGeodetic3D(final CompoundCRS crs,
                                   final SingleCRS   horizontal,
                                   final VerticalCRS vertical,
                                   final boolean     classic) throws FactoryException
    {
        final CoordinateSystemAxis[] axis = new CoordinateSystemAxis[3];
        final CoordinateSystem cs = horizontal.getCoordinateSystem();
        axis[classic ? 0 : 1] = cs.getAxis(0);
        axis[classic ? 1 : 2] = cs.getAxis(1);
        axis[classic ? 2 : 0] = vertical.getCoordinateSystem().getAxis(0);
        final Map csName, crsName;
        if (crs != null) {
            csName  = AbstractIdentifiedObject.getProperties(crs.getCoordinateSystem());
            crsName = AbstractIdentifiedObject.getProperties(crs);
        } else {
            csName  = getTemporaryName(cs);
            crsName = getTemporaryName(horizontal);
        }
        final  CSFactory  csFactory = getCSFactory();
        final CRSFactory crsFactory = getCRSFactory();
        final SingleCRS single;
        if (horizontal instanceof GeographicCRS) {
            /*
             * Merges a 2D geographic CRS with the vertical CRS.
             */
            single = crsFactory.createGeographicCRS(crsName, (GeodeticDatum) horizontal.getDatum(),
                      csFactory.createEllipsoidalCS(csName, axis[0], axis[1], axis[2]));
        } else if (horizontal instanceof ProjectedCRS) {
            /*
             * Merges a 2D projected CRS with the vertical CRS.
             */
            final ProjectedCRS projected = (ProjectedCRS) horizontal;
            GeographicCRS baseCRS = (GeographicCRS) projected.getBaseCRS();
            baseCRS = (GeographicCRS) toGeodetic3D(null, baseCRS, vertical, classic);
            final Conversion projection  = projected.getConversionFromBase();
            single = createProjectedCRS(crsName, baseCRS, projection,
                     csFactory.createCartesianCS(csName, axis[0], axis[1], axis[2]));
        } else {
            // Should never happen.
            throw new AssertionError(horizontal);
        }
        return single;
    }

    /**
     * Returns a new coordinate reference system with only the specified dimension.
     * This method is used for example in order to get a component of a
     * {@linkplain CompoundCRS compound CRS}.
     *
     * @param  crs The original (usually compound) CRS.
     * @param  dimensions The dimensions to keep.
     * @return The CRS with only the specified dimensions.
     */
    public CoordinateReferenceSystem separate(final CoordinateReferenceSystem crs,
                                              final int[] dimensions)
            throws FactoryException
    {
        final int length = dimensions.length;
        final int crsDimension = crs.getCoordinateSystem().getDimension();
        if (length==0 || dimensions[0]<0 || dimensions[length-1]>=crsDimension ||
            !XArray.isStrictlySorted(dimensions))
        {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$1,
                                               "dimension"));
        }
        if (length == crsDimension) {
            return crs;
        }
        /*
         * If the CRS is a compound one, separate each components independently.
         * For each component, we search the sub-array of 'dimensions' that apply
         * to this component and invoke 'separate' recursively.
         */
        if (crs instanceof CompoundCRS) {
            int count=0, lowerDimension=0, lowerIndex=0;
            final List/*<CoordinateReferenceSystem>*/ sources;
            final CoordinateReferenceSystem[] targets;
            sources = ((CompoundCRS) crs).getCoordinateReferenceSystems();
            targets = new CoordinateReferenceSystem[sources.size()];
search:     for (final Iterator it=sources.iterator(); it.hasNext();) {
                final CoordinateReferenceSystem source = (CoordinateReferenceSystem) it.next();
                final int upperDimension = lowerDimension + source.getCoordinateSystem().getDimension();
                /*
                 * 'source' CRS applies to dimension 'lowerDimension' inclusive to 'upperDimension'
                 * exclusive. Now search the smallest range in the user-specified 'dimensions' that
                 * cover the [lowerDimension .. upperDimension] range.
                 */
                if (lowerIndex == dimensions.length) {
                    break search;
                }
                while (dimensions[lowerIndex] < lowerDimension) {
                    if (++lowerIndex == dimensions.length) {
                        break search;
                    }
                }
                int upperIndex = lowerIndex;
                while (dimensions[upperIndex] < upperDimension) {
                    if (++upperIndex == dimensions.length) {
                        break;
                    }
                }
                if (lowerIndex != upperIndex) {
                    final int[] sub = new int[upperIndex - lowerIndex];
                    for (int j=0; j<sub.length; j++) {
                        sub[j] = dimensions[j+lowerIndex] - lowerDimension;
                    }
                    targets[count++] = separate(source, sub);
                }
                lowerDimension = upperDimension;
                lowerIndex     = upperIndex;
            }
            if (count == 1) {
                return targets[0];
            }
            return getCRSFactory().createCompoundCRS(getTemporaryName(crs),
                    (CoordinateReferenceSystem[]) XArray.resize(targets, count));
        }
        /*
         * TODO: Implement other cases here (3D-GeographicCRS, etc.).
         *       It may requires the creation of new CoordinateSystem objects,
         *       which is why this method live in 'FactoryGroup'.
         */
        throw new FactoryException(Errors.format(ErrorKeys.CANT_SEPARATE_CRS_$1,
                                   crs.getName().getCode()));
    }

    /**
     * Returns a temporary name for object derived from the specified one.
     */
    private static Map getTemporaryName(final IdentifiedObject source) {
        return Collections.singletonMap(IdentifiedObject.NAME_KEY,
                                        source.getName().getCode() + " (3D)");
    }
}
