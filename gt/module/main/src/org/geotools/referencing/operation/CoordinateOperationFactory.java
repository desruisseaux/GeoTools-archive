/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation;

// J2SE dependencies and extensions
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.units.ConversionException;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;
import javax.vecmath.GMatrix;
import javax.vecmath.SingularMatrixException;

import org.geotools.referencing.Factory;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.geotools.util.WeakHashSet;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeneralDerivedCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.TemporalDatum;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.Transformation;
import org.opengis.util.InternationalString;


/**
 * Creates {@linkplain CoordinateOperation coordinate operations}. This factory is capable to find
 * coordinate {@linkplain Transformation transformations} or {@linkplain Conversion conversions}
 * between two {@linkplain CoordinateReferenceSystem coordinate reference systems}. It delegates
 * most of its work to one or many of <code>createOperationStep</code> methods. Subclasses can
 * override those methods in order to extend the factory capability to some more CRS.
 *
 * @version $Id$
 * @author <A HREF="http://www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 *
 * @see org.opengis.ct.CT_CoordinateTransformationFactory
 */
public class CoordinateOperationFactory extends Factory
        implements org.opengis.referencing.operation.CoordinateOperationFactory
{
    /**
     * A unit of one millisecond.
     */
    private static final Unit MILLISECOND = SI.MILLI(SI.SECOND);

    /**
     * The identifier for temporary objects created.
     */
    private static final Identifier TEMPORARY_NAME =
            new org.geotools.referencing.Identifier(null, "Temporary");

    /**
     * The underlying math transform factory.
     */
    private final MathTransformFactory factory;

    /**
     * A pool of coordinate operation. This pool is used in order
     * to returns instance of existing operations when possible.
     */
    private final WeakHashSet pool = new WeakHashSet();

    /**
     * Constructs a coordinate operation factory using the default factories.
     */
    public CoordinateOperationFactory() {
        this(FactoryFinder.getMathTransformFactory());
    }

    /**
     * Constructs a coordinate operation factory.
     *
     * @param factory The math transform factory to use.
     */
    public CoordinateOperationFactory(final MathTransformFactory factory) {
        this.factory = factory;
        ensureNonNull("factory", factory);
    }

    /**
     * Returns the underlying math transform factory. This factory
     * is used for constructing {@link MathTransform} objects for
     * all {@linkplain CoordinateOperation coordinate operations}.
     */
    public final MathTransformFactory getMathTransformFactory() {
        return factory;
    }

    /**
     * Returns an operation for conversion or transformation between two coordinate reference
     * systems. If an operation exists, it is returned. If more than one operation exists, the
     * default is returned. If no operation exists, then the exception is thrown.
     *
     * <P>The default implementation inspects the CRS and delegates the work to one or
     * many <code>createOperationStep(...)</code> methods. This method fails if no path
     * between the CRS is found.</P>
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws OperationNotFoundException if no operation path was found from <code>sourceCRS</code>
     *         to <code>targetCRS</code>.
     * @throws FactoryException if the operation creation failed for some other reason.
     */
    public CoordinateOperation createOperation(final CoordinateReferenceSystem sourceCRS,
                                               final CoordinateReferenceSystem targetCRS)
            throws OperationNotFoundException, FactoryException
    {
        if (equalsIgnoreMetadata(sourceCRS, targetCRS)) {
            final int dim = sourceCRS.getCoordinateSystem().getDimension();
            assert   dim == targetCRS.getCoordinateSystem().getDimension() : dim;
            return createFromMathTransform(sourceCRS, targetCRS,
                   factory.createAffineTransform(new GeneralMatrix(dim+1)));
        }
        /////////////////////////////////////////////////////////////////////
        ////                                                             ////
        ////     Geographic  -->  Geographic, Projected or Geocentric    ////
        ////                                                             ////
        /////////////////////////////////////////////////////////////////////
        if (sourceCRS instanceof GeographicCRS) {
            final GeographicCRS source = (GeographicCRS) sourceCRS;
            if (targetCRS instanceof GeographicCRS) {
                final GeographicCRS target = (GeographicCRS) targetCRS;
                return createOperationStep(source, target);
            }
            if (targetCRS instanceof ProjectedCRS) {
                final ProjectedCRS target = (ProjectedCRS) targetCRS;
                return createOperationStep(source, target);
            }
            if (targetCRS instanceof GeocentricCRS) {
                final GeocentricCRS target = (GeocentricCRS) targetCRS;
                return createOperationStep(source, target);
            }
        }
        /////////////////////////////////////////////////////////
        ////                                                 ////
        ////     Projected  -->  Projected or Geographic     ////
        ////                                                 ////
        /////////////////////////////////////////////////////////
        if (sourceCRS instanceof ProjectedCRS) {
            final ProjectedCRS source = (ProjectedCRS) sourceCRS;
            if (targetCRS instanceof ProjectedCRS) {
                final ProjectedCRS target = (ProjectedCRS) targetCRS;
                return createOperationStep(source, target);
            }
            if (targetCRS instanceof GeographicCRS) {
                final GeographicCRS target = (GeographicCRS) targetCRS;
                return createOperationStep(source, target);
            }
        }
        //////////////////////////////////////////////////////////
        ////                                                  ////
        ////     Geocentric  -->  Geocentric or Geographic    ////
        ////                                                  ////
        //////////////////////////////////////////////////////////
        if (sourceCRS instanceof GeocentricCRS) {
            final GeocentricCRS source = (GeocentricCRS) sourceCRS;
            if (targetCRS instanceof GeocentricCRS) {
                final GeocentricCRS target = (GeocentricCRS) targetCRS;
                return createOperationStep(source, target);
            }
            if (targetCRS instanceof GeographicCRS) {
                final GeographicCRS target = (GeographicCRS) targetCRS;
                return createOperationStep(source, target);
            }
        }
        /////////////////////////////////////////
        ////                                 ////
        ////     Vertical  -->  Vertical     ////
        ////                                 ////
        /////////////////////////////////////////
        if (sourceCRS instanceof VerticalCRS) {
            final VerticalCRS source = (VerticalCRS) sourceCRS;
            if (targetCRS instanceof VerticalCRS) {
                final VerticalCRS target = (VerticalCRS) targetCRS;
                return createOperationStep(source, target);
            }
        }
        /////////////////////////////////////////
        ////                                 ////
        ////     Temporal  -->  Temporal     ////
        ////                                 ////
        /////////////////////////////////////////
        if (sourceCRS instanceof TemporalCRS) {
            final TemporalCRS source = (TemporalCRS) sourceCRS;
            if (targetCRS instanceof TemporalCRS) {
                final TemporalCRS target = (TemporalCRS) targetCRS;
                return createOperationStep(source, target);
            }
        }
        //////////////////////////////////////////////////////////////////
        ////                                                          ////
        ////     Any coordinate reference system -->  Derived CRS     ////
        ////                                                          ////
        //////////////////////////////////////////////////////////////////
        if (targetCRS instanceof GeneralDerivedCRS) {
            // Note: this code is identical to 'createOperationStep(GeographicCRS, ProjectedCRS)'
            //       except that the later invokes directly the right method for 'step1' instead
            //       of invoking 'createOperation' recursively.
            final GeneralDerivedCRS  target = (GeneralDerivedCRS) targetCRS;
            final CoordinateReferenceSystem base = target.getBaseCRS();
            final CoordinateOperation step1 = createOperation(sourceCRS, base);
            final CoordinateOperation step2 = target.getConversionFromBase();
            return concatenate(step1, step2);
        }
        //////////////////////////////////////////////////////////////////
        ////                                                          ////
        ////     Derived CRS -->  Any coordinate reference system     ////
        ////                                                          ////
        //////////////////////////////////////////////////////////////////
        if (sourceCRS instanceof GeneralDerivedCRS) {
            // Note: this code is identical to 'createOperationStep(ProjectedCRS, GeographicCRS)'
            //       except that the later invokes directly the right method for 'step2' instead
            //       of invoking 'createOperation' recursively.
            final GeneralDerivedCRS       source = (GeneralDerivedCRS) sourceCRS;
            final CoordinateReferenceSystem base = source.getBaseCRS();
            final CoordinateOperation      step2 = createOperation(base, targetCRS);
            CoordinateOperation            step1 = source.getConversionFromBase();
            MathTransform              transform = step1.getMathTransform();
            try {
                transform = transform.inverse();
            } catch (NoninvertibleTransformException exception) {
                throw new OperationNotFoundException(getErrorMessage(sourceCRS, base), exception);
            }
            step1 = createFromMathTransform(sourceCRS, base, transform);
            return concatenate(step1, step2);
        }
        ////////////////////////////////////////////
        ////                                    ////
        ////     Compound  -->  various CRS     ////
        ////                                    ////
        ////////////////////////////////////////////
        if (sourceCRS instanceof CompoundCRS) {
            final CompoundCRS source = (CompoundCRS) sourceCRS;
            if (targetCRS instanceof SingleCRS) {
                final SingleCRS target = (SingleCRS) targetCRS;
                return createOperationStep(source, target);
            }
            if (targetCRS instanceof CompoundCRS) {
                final CompoundCRS target = (CompoundCRS) targetCRS;
                return createOperationStep(source, target);
            }
        }
        if (targetCRS instanceof CompoundCRS) {
            final CompoundCRS target = (CompoundCRS) targetCRS;
            if (sourceCRS instanceof SingleCRS) {
                final SingleCRS source = (SingleCRS) sourceCRS;
                return createOperationStep(source, target);
            }
        }
        /////////////////////////////////////////
        ////                                 ////
        ////     Generic  -->  various CS    ////
        ////     Various CS --> Generic      ////
        ////                                 ////
        /////////////////////////////////////////
        if (sourceCRS == org.geotools.referencing.crs.EngineeringCRS.GENERIC_2D ||
            targetCRS == org.geotools.referencing.crs.EngineeringCRS.GENERIC_2D ||
            sourceCRS == org.geotools.referencing.crs.EngineeringCRS.GENERIC_3D ||
            targetCRS == org.geotools.referencing.crs.EngineeringCRS.GENERIC_3D)
        {
            final int dimSource = sourceCRS.getCoordinateSystem().getDimension();
            final int dimTarget = targetCRS.getCoordinateSystem().getDimension();
            if (dimTarget == dimSource) {
                final Matrix  matrix    = new GeneralMatrix(dimTarget+1, dimSource+1);
                MathTransform transform = factory.createAffineTransform(matrix);
                return createFromMathTransform(sourceCRS, targetCRS, transform);
            }
        }
        throw new OperationNotFoundException(getErrorMessage(sourceCRS, targetCRS));
    }

    /**
     * Returns an operation using a particular method for conversion or transformation
     * between two coordinate reference systems.
     * If the operation exists on the implementation, then it is returned.
     * If the operation does not exist on the implementation, then the implementation has the option
     * of inferring the operation from the argument objects.
     * If for whatever reason the specified operation will not be returned, then the exception is
     * thrown.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @param  method the algorithmic method for conversion or transformation
     * @throws OperationNotFoundException if no operation path was found from <code>sourceCRS</code>
     *         to <code>targetCRS</code>.
     * @throws FactoryException if the operation creation failed for some other reason.
     *
     * @deprecated Current implementation ignore the <code>method</code> argument.
     */
    public CoordinateOperation createOperation(final CoordinateReferenceSystem sourceCRS,
                                               final CoordinateReferenceSystem targetCRS,
                                               final OperationMethod           method)
            throws OperationNotFoundException, FactoryException
    {
        return createOperation(sourceCRS, targetCRS);
    }





    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////               C O N C A T E N A T I O N S               ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a concatenated operation from a sequence of operations.
     *
     * @param  properties Set of properties. Should contains at least <code>"name"</code>.
     * @param  operations The sequence of operations.
     * @return The concatenated operation.
     * @throws FactoryException if the object creation failed.
     */
    public CoordinateOperation createConcatenatedOperation(Map properties,
                                                           CoordinateOperation[] operations)
            throws FactoryException
    {
        CoordinateOperation operation;
        operation = new org.geotools.referencing.operation.ConcatenatedOperation(
                        properties, operations, factory);
        operation = (CoordinateOperation) pool.canonicalize(operation);
        return operation;
    }

    /**
     * Concatenate two operation steps.
     *
     * @param  step1 The first  step, or <code>null</code> for the identity operation.
     * @param  step2 The second step, or <code>null</code> for the identity operation.
     * @return A concatenated operation, or <code>null</code> if all arguments was nul.
     * @throws FactoryException if the operation can't be constructed.
     */
    private CoordinateOperation concatenate(final CoordinateOperation step1,
                                            final CoordinateOperation step2)
            throws FactoryException
    {
        if (step1==null) return step2;
        if (step2==null) return step1;
        if (false) {
            // Note: we sometime get this assertion failure if the user provided CRS with two
            //       different ellipsoids but an identical TOWGS84 conversion infos (which is
            //       wrong).
            assert equalsIgnoreMetadata(step1.getTargetCRS(), step2.getSourceCRS()) :
                   "CRS 1 =" + step1.getTargetCRS() + '\n' +
                   "CRS 2 =" + step2.getSourceCRS();
        }
        final MathTransform mt1 = step1.getMathTransform();
        final CoordinateReferenceSystem sourceCRS = step1.getSourceCRS();
        if (mt1.isIdentity() && equalsIgnoreMetadata(sourceCRS, step2.getSourceCRS())) {
            return step2;
        }
        final MathTransform mt2 = step2.getMathTransform();
        final CoordinateReferenceSystem targetCRS = step2.getTargetCRS();
        if (mt2.isIdentity() && equalsIgnoreMetadata(step1.getTargetCRS(), targetCRS)) {
            return step1;
        }
        return createConcatenatedOperation(getTemporaryName(sourceCRS, targetCRS),
                                           new CoordinateOperation[] {step1, step2});
    }

    /**
     * Concatenate three transformation steps.
     *
     * @param  step1 The first  step, or <code>null</code> for the identity operation.
     * @param  step2 The second step, or <code>null</code> for the identity operation.
     * @param  step3 The third  step, or <code>null</code> for the identity operation.
     * @return A concatenated operation, or <code>null</code> if all arguments were null.
     * @throws FactoryException if the operation can't be constructed.
     */
    private CoordinateOperation concatenate(final CoordinateOperation step1,
                                            final CoordinateOperation step2,
                                            final CoordinateOperation step3)
            throws FactoryException
    {
        if (step1==null) return concatenate(step2, step3);
        if (step2==null) return concatenate(step1, step3);
        if (step3==null) return concatenate(step1, step2);
        assert equalsIgnoreMetadata(step1.getTargetCRS(), step2.getSourceCRS()) : step1;
        assert equalsIgnoreMetadata(step2.getTargetCRS(), step3.getSourceCRS()) : step3;

        final MathTransform mt1 = step1.getMathTransform();
        final CoordinateReferenceSystem sourceCRS = step1.getSourceCRS();
        if (mt1.isIdentity() && equalsIgnoreMetadata(sourceCRS, step2.getSourceCRS())) {
            return concatenate(step2, step3);
        }
        final MathTransform mt2 = step2.getMathTransform();
        if (mt2.isIdentity()) {
            return concatenate(step1, step3);
        }
        final MathTransform mt3 = step3.getMathTransform();
        final CoordinateReferenceSystem targetCRS = step3.getTargetCRS();
        if (mt3.isIdentity() && equalsIgnoreMetadata(step2.getTargetCRS(), targetCRS)) {
            return concatenate(step1, step2);
        }
        return createConcatenatedOperation(getTemporaryName(sourceCRS, targetCRS),
                                           new CoordinateOperation[] {step1, step2, step3});
    }

    /**
     * Creates a coordinate operation from a math transform.
     * If the specified math transform is already a coordinate operation, and if source
     * and target CRS match, then <code>transform</code> is returned with no change.
     * Otherwise, a new coordinate operation is created.
     *
     * @param  sourceCRS The source coordinate reference system.
     * @param  targetCRS The destination coordinate reference system.
     * @param  transform The math transform.
     * @return A coordinate transform using the specified math transform.
     * @throws FactoryException if the operation can't be constructed.
     */
    private CoordinateOperation createFromMathTransform(
                                  final CoordinateReferenceSystem sourceCRS,
                                  final CoordinateReferenceSystem targetCRS,
                                  final MathTransform             transform)
            throws FactoryException
    {
        CoordinateOperation operation;
        if (transform instanceof CoordinateOperation) {
            operation = (CoordinateOperation) transform;
            if (Utilities.equals(operation.getSourceCRS(), sourceCRS) &&
                Utilities.equals(operation.getTargetCRS(), targetCRS))
            {
                return operation;
            }
        }
        final String name = getName(sourceCRS) + " \u21E8 " + getName(targetCRS);
        operation = createFromMathTransform(Collections.singletonMap(
                    org.geotools.referencing.IdentifiedObject.NAME_PROPERTY, name),
                    sourceCRS, targetCRS, transform);
        return operation;
    }

    /**
     * Constructs a coordinate operation from a set of properties. Subclasses can override this
     * method in order to control the operation creation process.
     *
     * @param properties Set of properties. Should contains at least <code>"name"</code>.
     * @param sourceCRS The source CRS, or <code>null</code> if not available.
     * @param targetCRS The target CRS, or <code>null</code> if not available.
     * @param transform Transform from positions in the source coordinate reference system
     *                  to positions in the target coordinate reference system.
     * @throws FactoryException if the operation can't be constructed.
     *
     * @see org.geotools.referencing.operation.CoordinateOperation#CoordinateOperation(Map,
     *      CoordinateReferenceSystem, CoordinateReferenceSystem, MathTransform)
     *
     * @todo Constructs {@link Conversion} or {@link Transformation} when possible.
     */
    protected CoordinateOperation createFromMathTransform(final Map                      properties,
                                                          final CoordinateReferenceSystem sourceCRS,
                                                          final CoordinateReferenceSystem targetCRS,
                                                          final MathTransform             transform)
            throws FactoryException
    {
        CoordinateOperation operation;
        operation = new org.geotools.referencing.operation.SingleOperation(
                        properties, sourceCRS, targetCRS, transform);
        operation = (CoordinateOperation) pool.canonicalize(operation);
        return operation;
    }




    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////               N O R M A L I Z A T I O N S               ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Makes sure that the specified geocentric CRS uses standard axis
     * and the specified datum.
     * If <code>crs</code> already meets all those conditions, then it is
     * returned unchanged. Otherwise, a new normalized geocentric CRS is
     * created and returned.
     *
     * @param  crs The geocentric coordinate reference system to normalize.
     * @param  datum The expected datum.
     * @return The normalized coordinate reference system.
     */
    private static GeocentricCRS normalize(final GeocentricCRS crs,
                                           final GeodeticDatum datum)
    {
        final CartesianCS STANDARD = org.geotools.referencing.cs.CartesianCS.GEOCENTRIC;
        if (equalsIgnoreMetadata(crs.getDatum(), datum) &&
            hasStandardAxis(crs.getCoordinateSystem(), STANDARD))
        {
            return crs;
        }
        return new org.geotools.referencing.crs.GeocentricCRS(
                   getTemporaryName(crs), datum, STANDARD);
    }

    /**
     * Makes sure that the specified geographic CRS uses standard axis
     * (longitude and latitude in degrees). Optionally, this method can
     * also make sure that the CRS use the Greenwich prime meridian.
     * Other datum properties are left unchanged.
     * If <code>crs</code> already meets all those conditions, then it is
     * returned unchanged. Otherwise, a new normalized geographic CRS is
     * created and returned.
     *
     * @param  crs The geographic coordinate reference system to normalize.
     * @param  forceGreenwich <code>true</code> for forcing the Greenwich prime meridian.
     * @return The normalized coordinate reference system.
     */
    private static GeographicCRS normalize(final GeographicCRS      crs,
                                           final boolean forceGreenwich)
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
              GeodeticDatum datum = (GeodeticDatum) crs.getDatum();
        final EllipsoidalCS cs    = (EllipsoidalCS) crs.getCoordinateSystem();
        final EllipsoidalCS STANDARD = (cs.getDimension() <= 2) ?
                org.geotools.referencing.cs.EllipsoidalCS.GEODETIC_2D :
                org.geotools.referencing.cs.EllipsoidalCS.GEODETIC_3D;
        if (forceGreenwich &&
            getGreenwichLongitude(datum.getPrimeMeridian(), NonSI.DEGREE_ANGLE)!=0)
        {
            final Map name = getTemporaryName(datum);
            final Ellipsoid ellipsoid = datum.getEllipsoid();
            datum = new org.geotools.referencing.datum.GeodeticDatum(name, ellipsoid,
                        org.geotools.referencing.datum.PrimeMeridian.GREENWICH);
        } else if (hasStandardAxis(cs, STANDARD)) {
            return crs;
        }
        /*
         * The specified geographic coordinate system doesn't use standard axis
         * (EAST, NORTH) or the greenwich meridian.
         */
        return new org.geotools.referencing.crs.GeographicCRS(
                   getTemporaryName(crs), datum, STANDARD);
    }

    /**
     * Returns <code>true</code> if the specified coordinate system
     * use standard axis and units.
     *
     * @param crs  The coordinate system to test.
     * @param standard The coordinate system that defines the standard. Usually
     *        {@link org.geotools.referencing.cs.EllipsoidalCS#GEODETIC_2D} or
     *        {@link org.geotools.referencing.cs.CartesianCS#PROJECTED}.
     */
    private static boolean hasStandardAxis(final CoordinateSystem cs,
                                           final CoordinateSystem standard)
    {
        final int dimension = standard.getDimension();
        if (cs.getDimension() != dimension) {
            return false;
        }
        for (int i=0; i<dimension; i++) {
            final CoordinateSystemAxis a1 =       cs.getAxis(i);
            final CoordinateSystemAxis a2 = standard.getAxis(i);
            if (!a1.getDirection().equals(a2.getDirection()) ||
                !a1.getUnit()     .equals(a2.getUnit()))
            {
                return false;
            }
        }
        return true;
    }




    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////            A X I S   O R I E N T A T I O N S            ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns an affine transform between two coordinate systems. Only units and
     * axis order (e.g. transforming from (NORTH,WEST) to (EAST,NORTH)) are taken
     * in account.
     * <br><br>
     * Example: If coordinates in <code>sourceCS</code> are (x,y) pairs in metres and
     * coordinates in <code>targetCS</code> are (-y,x) pairs in centimetres, then the
     * transformation can be performed as below:
     *
     * <pre><blockquote>
     *          [-y(cm)]   [ 0  -100    0 ] [x(m)]
     *          [ x(cm)] = [ 100   0    0 ] [y(m)]
     *          [ 1    ]   [ 0     0    1 ] [1   ]
     * </blockquote></pre>
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @return The transformation from <code>sourceCS</code> to <code>targetCS</code> as
     *         an affine transform. Only axis orientation and units are taken in account.
     * @throws OperationNotFoundException If the affine transform can't be constructed.
     */
    private Matrix swapAndScaleAxis(final CoordinateSystem sourceCS,
                                    final CoordinateSystem targetCS)
            throws OperationNotFoundException
    {
        try {
            return org.geotools.referencing.cs.CoordinateSystem.swapAndScaleAxis(sourceCS,targetCS);
        } catch (IllegalArgumentException exception) {
            throw new OperationNotFoundException(getErrorMessage(sourceCS, targetCS), exception);
        } catch (ConversionException exception) {
            throw new OperationNotFoundException(getErrorMessage(sourceCS, targetCS), exception);
        }
        // No attempt to catch ClassCastException: it would be a programming error.
    }

    /**
     * Returns an affine transform between two ellipsoidal coordinate systems. Only
     * units, axis order (e.g. transforming from (NORTH,WEST) to (EAST,NORTH)) and
     * prime meridian are taken in account. Other attributes (especially the datum)
     * must be checked before invoking this method.
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @param  sourcePM The source prime meridian.
     * @param  targetPM The target prime meridian.
     * @return The transformation from <code>sourceCS</code> to <code>targetCS</code> as
     *         an affine transform. Only axis orientation, units and prime meridian are
     *         taken in account.
     * @throws OperationNotFoundException If the affine transform can't be constructed.
     */
    private Matrix swapAndScaleAxis(final EllipsoidalCS sourceCS,
                                    final EllipsoidalCS targetCS,
                                    final PrimeMeridian sourcePM,
                                    final PrimeMeridian targetPM)
            throws OperationNotFoundException
    {
        final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS);
        for (int i=targetCS.getDimension(); --i>=0;) {
            final CoordinateSystemAxis axis = targetCS.getAxis(i);
            final AxisDirection direction = axis.getDirection();
            if (AxisDirection.EAST.equals(direction.absolute())) {
                /*
                 * A longitude ordinate has been found (i.e. the axis is oriented toward EAST or
                 * WEST). Compute the amount of angle to add to the source longitude in order to
                 * get the destination longitude. This amount is measured in units of the target
                 * axis.  The affine transform is then updated in order to take this rotation in
                 * account. Note that the resulting longitude may be outside the usual [-180..180°]
                 * range.
                 */
                final Unit              unit = axis.getUnit();
                final double sourceLongitude = getGreenwichLongitude(sourcePM, unit);
                final double targetLongitude = getGreenwichLongitude(targetPM, unit);
                final int   lastMatrixColumn = matrix.getNumCol()-1;
                double rotate = sourceLongitude - targetLongitude;
                if (AxisDirection.WEST.equals(direction)) {
                    rotate = -rotate;
                }
                rotate += matrix.getElement(i, lastMatrixColumn);
                matrix.setElement(i, lastMatrixColumn, rotate);
            }
        }
        return matrix;
    }

    /**
     * Returns the longitude value relative to the Greenwich Meridian,
     * expressed in the specified units.
     */
    private static double getGreenwichLongitude(final PrimeMeridian pm, final Unit unit) {
        return pm.getAngularUnit().getConverterTo(unit).convert(pm.getGreenwichLongitude());
    }

    /**
     * Returns a conversion from a source to target projected CRS, if this conversion
     * is representable as an affine transform. More specifically, if all projection
     * parameters are identical except the following ones:
     * <BR>
     * <UL>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#SCALE_FACTOR   scale_factor}</LI>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#SEMI_MAJOR     semi_major}</LI>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#SEMI_MINOR     semi_minor}</LI>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#FALSE_EASTING  false_easting}</LI>
     *   <LI>{@link org.geotools.referencing.operation.projection.MapProjection.Provider#FALSE_NORTHING false_northing}</LI>
     * </UL>
     *
     * <P>Then the conversion between two projected CRS can sometime be represented as a linear
     * conversion. For example if only false easting/northing differ, than the coordinate conversion
     * is simply a translation. If no linear conversion has been found between the two CRS, then
     * this method returns <code>null</code>.</P>
     *
     * @param  sourceCRS The source coordinate reference system.
     * @param  targetCRS The target coordinate reference system.
     * @return The conversion from <code>sourceCRS</code> to <code>targetCRS</code> as an
     *         affine transform, or <code>null</code> if no linear transform has been found.
     */
    private static Matrix createLinearConversion(final ProjectedCRS sourceCRS,
                                                 final ProjectedCRS targetCRS)
    {
        return org.geotools.referencing.crs.ProjectedCRS.
               createLinearConversion(sourceCRS, targetCRS, 1E-12);
    }




    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////        T R A N S F O R M A T I O N S   S T E P S        ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an operation between two temporal coordinate reference systems.
     * The default implementation checks if both CRS use the same datum, and
     * then adjusts for axis direction, units and epoch.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final TemporalCRS sourceCRS,
                                                      final TemporalCRS targetCRS)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final TemporalDatum sourceDatum = (TemporalDatum) sourceCRS.getDatum();
        final TemporalDatum targetDatum = (TemporalDatum) targetCRS.getDatum();
        if (!equalsIgnoreMetadata(sourceDatum, targetDatum)) {
            throw new OperationNotFoundException(getErrorMessage(sourceDatum, targetDatum));
        }
        /*
         * Compute the epoch shift.  The epoch is the time "0" in a particular coordinate
         * reference system. For example, the epoch for java.util.Date object is january 1,
         * 1970 at 00:00 UTC.  We compute how much to add to a time in 'sourceCRS' in order
         * to get a time in 'targetCRS'. This "epoch shift" is in units of 'targetCRS'.
         */
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final TimeCS sourceCS = (TimeCS) sourceCRS.getCoordinateSystem();
        final TimeCS targetCS = (TimeCS) targetCRS.getCoordinateSystem();
        final Unit targetUnit = targetCS.getAxis(0).getUnit();
        double epochShift = sourceDatum.getOrigin().getTime() -
                            targetDatum.getOrigin().getTime();
        epochShift = MILLISECOND.getConverterTo(targetUnit).convert(epochShift);
        /*
         * Check axis orientation.  The method 'swapAndScaleAxis' should returns a matrix
         * of size 2x2. The element at index (0,0) may be 1 if sourceCRS and targetCRS axis
         * are in the same direction, or -1 if there are in opposite direction (e.g.
         * "PAST" vs "FUTURE"). This number may be something else than -1 or +1 if a unit
         * conversion was applied too,  for example 60 if time in 'sourceCRS' was in hours
         * while time in 'targetCRS' was in minutes.
         *
         * The "epoch shift" previously computed is a translation.
         * Consequently, it is added to element (0,1).
         */
        final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS);
        final int translationColumn = matrix.getNumCol()-1;
        if (translationColumn >= 0) { // Paranoiac check: should always be 1.
            final double translation = matrix.getElement(0, translationColumn);
            matrix.setElement(0, translationColumn, translation+epochShift);
        }
        final MathTransform transform = factory.createAffineTransform(matrix);
        return createFromMathTransform(sourceCRS, targetCRS, transform);
    }
    
    /**
     * Creates an operation between two vertical coordinate reference systems.
     * The default implementation checks if both CRS use the same datum, and
     * then adjusts for axis direction and units.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final VerticalCRS sourceCRS,
                                                      final VerticalCRS targetCRS)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final VerticalDatum sourceDatum = (VerticalDatum) sourceCRS.getDatum();
        final VerticalDatum targetDatum = (VerticalDatum) targetCRS.getDatum();
        if (!equalsIgnoreMetadata(sourceDatum, targetDatum)) {
            throw new OperationNotFoundException(getErrorMessage(sourceDatum, targetDatum));
        }
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final VerticalCS    sourceCS  = (VerticalCS) sourceCRS.getCoordinateSystem();
        final VerticalCS    targetCS  = (VerticalCS) targetCRS.getCoordinateSystem();
        final Matrix        matrix    = swapAndScaleAxis(sourceCS, targetCS);
        final MathTransform transform = factory.createAffineTransform(matrix);
        return createFromMathTransform(sourceCRS, targetCRS, transform);
    }

    /**
     * Creates an operation between two geographic coordinate reference systems. The default
     * implementation can adjust axis order and orientation (e.g. transforming from
     * <code>(NORTH,WEST)</code> to <code>(EAST,NORTH)</code>), performs units conversion
     * and apply Bursa Wolf transformation if needed.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     *
     * @todo When rotating the prime meridian, we should ensure that
     *       transformed longitudes stay in the range [-180..+180°].
     *
     * @todo We should use Molodenski transforms when applicable.
     */
    protected CoordinateOperation createOperationStep(final GeographicCRS sourceCRS,
                                                      final GeographicCRS targetCRS)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final GeodeticDatum sourceDatum = (GeodeticDatum) sourceCRS.getDatum();
        final GeodeticDatum targetDatum = (GeodeticDatum) targetCRS.getDatum();
        if (equalsIgnoreMetadata(sourceDatum, targetDatum)) {
            /*
             * If both geographic CRS use the same datum, then there is no need for a datum shift.
             * Just swap axis order, and rotate the longitude coordinate if prime meridians are
             * different. Note: this special block is mandatory for avoiding never-ending loop,
             * since it is invoked by 'createOperationStep(GeocentricCRS...)'.
             *
             * TODO: We should ensure that longitude is in range [-180..+180°].
             */
            // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
            final EllipsoidalCS sourceCS = (EllipsoidalCS) sourceCRS.getCoordinateSystem();
            final EllipsoidalCS targetCS = (EllipsoidalCS) targetCRS.getCoordinateSystem();
            final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS,
                                                   sourceDatum.getPrimeMeridian(),
                                                   targetDatum.getPrimeMeridian());
            MathTransform transform = factory.createAffineTransform(matrix);
            return createFromMathTransform(sourceCRS, targetCRS, transform);
        }
        /*
         * If the two geographic CRS use different datum, transform from the
         * source to target datum through the geocentric coordinate system.
         * The transformation chain is:
         *
         *     source geographic CRS             -->
         *     geocentric CRS with source datum  -->
         *     geocentric CRS with target datum  -->
         *     target geographic CRS
         */
        final CartesianCS STANDARD = org.geotools.referencing.cs.CartesianCS.GEOCENTRIC;
        final GeocentricCRS  gcrs1 = new org.geotools.referencing.crs.GeocentricCRS(
                                     getTemporaryName(sourceCRS), sourceDatum, STANDARD);
        final GeocentricCRS  gcrs3 = new org.geotools.referencing.crs.GeocentricCRS(
                                     getTemporaryName(targetCRS), targetDatum, STANDARD);
        final CoordinateOperation step1 = createOperationStep(sourceCRS, gcrs1);
        final CoordinateOperation step2 = createOperationStep(gcrs1,     gcrs3);
        final CoordinateOperation step3 = createOperationStep(gcrs3, targetCRS);
        return concatenate(step1, step2, step3);
    }

    /**
     * Creates an operation between two projected coordinate reference systems.
     * The default implementation can adjust axis order and orientation. It also
     * performs units conversion if it is the only extra change needed. Otherwise,
     * it performs three steps:
     *
     * <ul>
     *   <li>Unproject from <code>sourceCRS</code> to its base
     *       {@linkplain GeographicCRS geographic CRS}.</li>
     *   <li>Convert the source to target base geographic CRS.</li>
     *   <li>Project from the base {@linkplain GeographicCRS geographic CRS}
     *       to the <code>targetCRS</code>.</li>
     * </ul>
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final ProjectedCRS sourceCRS,
                                                      final ProjectedCRS targetCRS)
            throws FactoryException
    {
        /*
         * First, check if a linear path exists from sourceCRS to targetCRS.
         * If both projected CRS use the same projection and the same horizontal datum,
         * then only axis orientation and units may have been changed. We do not need
         * to perform the tedious  ProjectedCRS --> GeographicCRS --> ProjectedCRS  chain.
         * We can apply a much shorter conversion using only an affine transform.
         *
         * This shorter path is essential for proper working of 
         * createOperationStep(GeographicCRS,ProjectedCRS).
         */
        final Matrix linear = createLinearConversion(sourceCRS, targetCRS);
        if (linear != null) {
            final MathTransform transform = factory.createAffineTransform(linear);
            return createFromMathTransform(sourceCRS, targetCRS, transform);
        }
        /*
         * Apply the transformation in 3 steps (the 3 arrows below):
         *
         *     source projected CRS   --(unproject)-->
         *     source geographic CRS  --------------->
         *     target geographic CRS  ---(project)--->
         *     target projected CRS
         */
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final GeographicCRS   sourceGeo = (GeographicCRS) sourceCRS.getBaseCRS();
        final GeographicCRS   targetGeo = (GeographicCRS) targetCRS.getBaseCRS();
        final CoordinateOperation step1 = createOperationStep(sourceCRS, sourceGeo);
        final CoordinateOperation step2 = createOperationStep(sourceGeo, targetGeo);
        final CoordinateOperation step3 = createOperationStep(targetGeo, targetCRS);
        return concatenate(step1, step2, step3);
    }

    /**
     * Creates an operation from a geographic to a projected coordinate reference system.
     * The default implementation constructs the following operation chain:
     *
     * <blockquote><pre>
     * sourceCRS  &rarr;  {@linkplain ProjectedCRS#getBaseCRS baseCRS}  &rarr;  targetCRS
     * </pre></blockquote>
     *
     * where the conversion from <code>baseCRS</code> to <code>targetCRS</code> is obtained
     * from <code>targetCRS.{@linkplain ProjectedCRS#getConversionFromBase
     * getConversionFromBase()}</code>.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final GeographicCRS sourceCRS,
                                                      final ProjectedCRS  targetCRS)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final GeographicCRS       base  = (GeographicCRS) targetCRS.getBaseCRS();
        final CoordinateOperation step1 = createOperationStep(sourceCRS, base);
        final CoordinateOperation step2 = targetCRS.getConversionFromBase();
        return concatenate(step1, step2);
    }
    
    /**
     * Creates an operation from a projected to a geographic coordinate reference system.
     * The default implementation constructs the following operation chain:
     *
     * <blockquote><pre>
     * sourceCRS  &rarr;  {@linkplain ProjectedCRS#getBaseCRS baseCRS}  &rarr;  targetCRS
     * </pre></blockquote>
     *
     * where the conversion from <code>sourceCRS</code> to <code>baseCRS</code> is obtained
     * from the inverse of
     * <code>sourceCRS.{@linkplain ProjectedCRS#getConversionFromBase
     * getConversionFromBase()}</code>.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final ProjectedCRS  sourceCRS,
                                                      final GeographicCRS targetCRS)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final GeographicCRS base  = (GeographicCRS) sourceCRS.getBaseCRS();
        CoordinateOperation step2 = createOperationStep(base, targetCRS);
        CoordinateOperation step1 = sourceCRS.getConversionFromBase();
        MathTransform   transform = step1.getMathTransform();
        try {
            transform = transform.inverse();
        } catch (NoninvertibleTransformException exception) {
            throw new OperationNotFoundException(getErrorMessage(sourceCRS, base), exception);
        }
        step1 = createFromMathTransform(sourceCRS, base, transform);
        return concatenate(step1, step2);
    }

    /**
     * Creates an operation between two geocentric coordinate reference systems.
     * The default implementation can adjust for axis order and orientation,
     * performs units conversion and apply Bursa Wolf transformation if needed.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     *
     * @todo Rotation of prime meridian not yet implemented.
     */
    protected CoordinateOperation createOperationStep(final GeocentricCRS sourceCRS,
                                                      final GeocentricCRS targetCRS)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final GeodeticDatum sourceDatum = (GeodeticDatum) sourceCRS.getDatum();
        final GeodeticDatum targetDatum = (GeodeticDatum) targetCRS.getDatum();
        final PrimeMeridian    sourcePM = sourceDatum.getPrimeMeridian();
        final PrimeMeridian    targetPM = targetDatum.getPrimeMeridian();
        final CoordinateSystem sourceCS = sourceCRS.getCoordinateSystem();
        final CoordinateSystem targetCS = targetCRS.getCoordinateSystem();
        if (equalsIgnoreMetadata(sourceDatum, targetDatum)) {
            if (equalsIgnoreMetadata(sourcePM, targetPM)) {
                /*
                 * If both CRS use the same datum and the same prime meridian,
                 * then the transformation is probably just axis swap or unit
                 * conversions.
                 */
                final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS);
                final MathTransform transform = factory.createAffineTransform(matrix);
                return createFromMathTransform(sourceCRS, targetCRS, transform);
            }
            // If prime meridians are not the same, performs the full transformation.
        }
        if (getGreenwichLongitude(sourcePM, NonSI.DEGREE_ANGLE) != 0 ||
            getGreenwichLongitude(targetPM, NonSI.DEGREE_ANGLE) != 0)
        {
            throw new OperationNotFoundException("Rotation of prime meridian not yet implemented");
        }
        /*
         * Transform between differents ellipsoids using Bursa Wolf parameters.
         * The Bursa Wolf parameters are used with "standard" geocentric CS, i.e.
         * with x axis towards the prime meridian, y axis towards East and z axis
         * toward North. The following steps are applied:
         *
         *     source CRS                      -->
         *     standard CRS with source datum  -->
         *     standard CRS with target datum  -->
         *     target CRS
         */
        final CartesianCS STANDARD = org.geotools.referencing.cs.CartesianCS.GEOCENTRIC;
        final GeneralMatrix matrix;
        try {
            final Matrix datumShift = org.geotools.referencing.datum.GeodeticDatum.
                                      getAffineTransform(sourceDatum, targetDatum);
            if (!(datumShift instanceof GMatrix)) {
                throw new OperationNotFoundException(Resources.format(
                            ResourceKeys.BURSA_WOLF_PARAMETERS_REQUIRED));
            }
            final Matrix normalizeSource = swapAndScaleAxis(sourceCS, STANDARD);
            final Matrix normalizeTarget = swapAndScaleAxis(STANDARD, targetCS);
            /*
             * Since all steps are matrix, we can multiply them into a single matrix operation.
             * Note: GMatrix.mul(GMatrix) is equivalents to AffineTransform.concatenate(...):
             *       First transform by the supplied transform and then transform the result
             *       by the original transform.
             *
             * We compute: matrix = normalizeTarget * datumShift * normalizeSource
             */
            matrix = (GeneralMatrix) normalizeTarget;
            matrix.mul((GMatrix) datumShift);
            matrix.mul((GMatrix) normalizeSource);
        } catch (SingularMatrixException cause) {
            throw new OperationNotFoundException(getErrorMessage(sourceDatum, targetDatum), cause);
        }
        final MathTransform transform = factory.createAffineTransform(matrix);
        return createFromMathTransform(sourceCRS, targetCRS, transform);
    }

    /**
     * Creates an operation from a geographic to a geocentric coordinate reference systems.
     * If the source CRS doesn't have a vertical axis, height above the ellipsoid will be
     * assumed equals to zero everywhere. The default implementation use the
     * <code>"Ellipsoid_To_Geocentric"</code> math transform.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final GeographicCRS sourceCRS,
                                                      final GeocentricCRS targetCRS)
            throws FactoryException
    {
        /*
         * This transformation is a 3 steps process:
         *
         *    source     geographic CRS  -->
         *    normalized geographic CRS  -->
         *    normalized geocentric CRS  -->
         *    target     geocentric CRS
         *
         * "Normalized" means that axis point toward standards direction (East, North, etc.),
         * units are metres or degrees, prime meridian is Greenwich and height is measured
         * above the ellipsoid. However, the horizontal datum is preserved.
         */
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.

        final GeographicCRS normSourceCRS = normalize(sourceCRS, true);
        final GeodeticDatum datum         = (GeodeticDatum) normSourceCRS.getDatum();
        final GeocentricCRS normTargetCRS = normalize(targetCRS, datum);
        final Ellipsoid         ellipsoid = datum.getEllipsoid();
        final Unit                   unit = ellipsoid.getAxisUnit();
        final ParameterValueGroup   param = factory.getDefaultParameters("Ellipsoid_To_Geocentric");
        param.parameter("semi_major").setValue(ellipsoid.getSemiMajorAxis(), unit);
        param.parameter("semi_minor").setValue(ellipsoid.getSemiMinorAxis(), unit);
        param.parameter("dim").setValue(normSourceCRS.getCoordinateSystem().getDimension());
        final MathTransform transform = factory.createParameterizedTransform(param);

        final CoordinateOperation step1, step2, step3;
        step1 = createOperationStep    (    sourceCRS, normSourceCRS);
        step2 = createFromMathTransform(normSourceCRS, normTargetCRS, transform);
        step3 = createOperationStep    (normTargetCRS,     targetCRS);
        return concatenate(step1, step2, step3);
    }

    /**
     * Creates an operation from a geocentric to a geographic coordinate reference systems.
     * The default implementation use the <code>"Geocentric_To_Ellipsoid"</code> math transform.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final GeocentricCRS sourceCRS,
                                                      final GeographicCRS targetCRS)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.

        final GeographicCRS normTargetCRS = normalize(targetCRS, true);
        final GeodeticDatum datum         = (GeodeticDatum) normTargetCRS.getDatum();
        final GeocentricCRS normSourceCRS = normalize(sourceCRS, datum);
        final Ellipsoid         ellipsoid = datum.getEllipsoid();
        final Unit                   unit = ellipsoid.getAxisUnit();
        final ParameterValueGroup   param = factory.getDefaultParameters("Geocentric_To_Ellipsoid");
        param.parameter("semi_major").setValue(ellipsoid.getSemiMajorAxis(), unit);
        param.parameter("semi_minor").setValue(ellipsoid.getSemiMinorAxis(), unit);
        param.parameter("dim").setValue(normTargetCRS.getCoordinateSystem().getDimension());
        final MathTransform transform = factory.createParameterizedTransform(param);

        final CoordinateOperation step1, step2, step3;
        step1 = createOperationStep    (    sourceCRS, normSourceCRS);
        step2 = createFromMathTransform(normSourceCRS, normTargetCRS, transform);
        step3 = createOperationStep    (normTargetCRS,     targetCRS);
        return concatenate(step1, step2, step3);
    }

    /**
     * Creates an operation from a compound to a single coordinate reference systems.
     * The default implementation try to keep only one CRS from the compound one, and
     * drop all the others. For example, <code>sourceCRS</code> may be a {@link GeographicCRS} +
     * {@link TemporalCRS}, while <code>targetCRS</code> is a single {@link GeographicCRS}.
     * This implementation try to invoke
     * <code>{@linkplain #createOperation(CoordinateReferenceSystem, CoordinateReferenceSystem)
     * createOperation}(crs[i], targetCRS)</code> successively for all CRS in the compound
     * <code>sourceCRS</code>, and keep the operation that worked for the largest (in
     * {@linkplain CoordinateSystem#getDimension dimension}) CRS.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final CompoundCRS sourceCRS,
                                                      final SingleCRS   targetCRS)
            throws FactoryException
    {
        int lower = 0;
        int upper = 0;
        int index = 0;
        CoordinateOperation         operation = null;
        CoordinateReferenceSystem   singleCRS = null;
        final CoordinateReferenceSystem[] crs = sourceCRS.getCoordinateReferenceSystems();
        for (int i=0; i<crs.length; i++) {
            final CoordinateReferenceSystem candidate = crs[i];
            final int dimension = candidate.getCoordinateSystem().getDimension();
            /*
             * Try to create the operation only if the candidate CRS has a dimension
             * greater than the last successfully created operation, otherwise it is
             * not worth to create the operation since we would discart it anyway.
             */
            if (dimension > (upper-lower)) try {
                operation = createOperation(candidate, targetCRS);
                singleCRS = candidate; // Creation successfull
                lower     = index;
                upper     = lower + dimension;
            } catch (FactoryException exception) {
                // Creation failed. Try the next one.
            }
            index += dimension;
        }
        assert index == sourceCRS.getCoordinateSystem().getDimension() : index;
        if (singleCRS == null) {
            throw new OperationNotFoundException(getErrorMessage(sourceCRS, targetCRS));
        }
        /*
         * A coordinate operation from a part of 'sourceCRS' has been successfully
         * contructed. Now, build a projective transform that will select only the
         * corresponding ordinates from input arrays, and pass them to the transform.
         */
        final int[] indices = new int[upper-lower];
        for (int i=0; i<indices.length; i++) {
            indices[i] = lower+i;
        }
        final Matrix        select = ProjectiveTransform.createSelectMatrix(index, indices);
        final MathTransform filter = factory.createAffineTransform(select);
        return concatenate(createFromMathTransform(sourceCRS, singleCRS, filter), operation);
    }
    
    /**
     * Creates an operation from a single to a compound coordinate reference system.
     * The default implementation returns the inverse of
     * <code>{@linkplain #createOperationStep(CompoundCRS,SingleCRS)
     * createOperationStep}(targetCRS, sourceCRS)</code>. Note that this inversion will
     * fails for most default implementation of the later, since it create non-invertible
     * operation. Subclasses should override this method if they know how to create this
     * operation.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final SingleCRS   sourceCRS,
                                                      final CompoundCRS targetCRS)
            throws FactoryException
    {
        final CoordinateOperation operation;
        try {
            operation = createOperationStep(targetCRS, sourceCRS);
        } catch (FactoryException exception) {
            throw new OperationNotFoundException(getErrorMessage(sourceCRS, targetCRS), exception);
        }
        MathTransform transform = operation.getMathTransform();
        try {
            transform = transform.inverse();
        } catch (NoninvertibleTransformException exception) {
            throw new OperationNotFoundException(getErrorMessage(sourceCRS, targetCRS), exception);
        }
        return createFromMathTransform(sourceCRS, targetCRS, transform);
    }

    /**
     * Creates an operation between two compound coordinate reference systems.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    protected CoordinateOperation createOperationStep(final CompoundCRS sourceCRS,
                                                      final CompoundCRS targetCRS)
            throws FactoryException
    {
        // TODO: not yet implemented.
        throw new OperationNotFoundException(getErrorMessage(sourceCRS, targetCRS));
    }




    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////                M I S C E L L A N E O U S                ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the dimension of the specified coordinate system,
     * or <code>0</code> if the coordinate system is null.
     */
    private static int getDimension(final CoordinateReferenceSystem crs) {
        return (crs!=null) ? crs.getCoordinateSystem().getDimension() : 0;
    }

    /**
     * Returns the name of the specified object.
     */
    private static String getName(final IdentifiedObject object) {
        if (object != null) {
            final Identifier id = object.getName();
            if (id != null) {
                return Utilities.getShortClassName(object) + '(' + id.getCode() + ')';
            }
            return Utilities.getShortClassName(object);
        }
        return null;
    }
    
    /**
     * Returns a temporary name for object derived from the specified one.
     *
     * @param source The CRS to base name on, or <code>null</code> if none.
     *
     * @todo Find better names, and localize.
     */
    private static Map getTemporaryName(final IdentifiedObject source) {
        final Map properties = new HashMap(4);
        properties.put(org.geotools.referencing.IdentifiedObject.NAME_PROPERTY, TEMPORARY_NAME);
        final StringBuffer remarks = new StringBuffer("Derived from \"");
        remarks.append(getName(source));
        remarks.append('"');
        final InternationalString previous = source.getRemarks();
        if (previous != null) {
            remarks.append(System.getProperty("line.separator", "\n"));
            remarks.append(previous);
        }
        properties.put(org.geotools.referencing.IdentifiedObject.REMARKS_PROPERTY,
                       remarks.toString());
        return properties;
    }

    /**
     * Returns a temporary name for object derived from a concatenation.
     *
     * @param source The CRS to base name on, or <code>null</code> if none.
     */
    private static Map getTemporaryName(final CoordinateReferenceSystem source,
                                        final CoordinateReferenceSystem target)
    {
        final String name = getName(source) + " \u21E8 " + getName(target);
        return Collections.singletonMap(
               org.geotools.referencing.IdentifiedObject.NAME_PROPERTY, name);
    }

    /**
     * Returns an error message for "No path found from sourceCRS to targetCRS".
     */
    private static String getErrorMessage(final IdentifiedObject source,
                                          final IdentifiedObject target)
    {
        return Resources.format(ResourceKeys.ERROR_NO_TRANSFORMATION_PATH_$2,
                                getName(source), getName(target));
    }

    /**
     * Compare the specified objects for equality. If both objects are Geotools
     * implementations of {@linkplain org.geotools.referencing.IdentifiedObject},
     * then this method will ignore the metadata during the comparaison.
     *
     * @param  object1 The first object to compare (may be null).
     * @param  object2 The second object to compare (may be null).
     * @return <code>true</code> if both objects are equals.
     */
    private static boolean equalsIgnoreMetadata(final IdentifiedObject object1,
                                                final IdentifiedObject object2)
    {
        return CRSUtilities.equalsIgnoreMetadata(object1, object2);
    }

    /**
     * Returns <code>true</code> if either the primary name or at least
     * one alias matches the specified string.
     *
     * @param  object The object to check.
     * @param  name The name.
     * @return <code>true</code> if the primary name of at least one alias
     *         matches the specified <code>name</code>.
     */
    private static boolean nameMatches(final IdentifiedObject object, final String name) {
        return org.geotools.referencing.IdentifiedObject.nameMatches(object, name);
    }

    /**
     * Makes sure an argument is non-null.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws IllegalArgumentException if <code>object</code> is null.
     */
    private static void ensureNonNull(final String name, final Object object)
            throws IllegalArgumentException
    {
        if (object == null) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NULL_ARGUMENT_$1, name));
        }
    }
}
