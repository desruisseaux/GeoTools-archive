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
import java.util.Map;
import java.util.logging.Logger;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;
import javax.vecmath.GMatrix;
import javax.vecmath.SingularMatrixException;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSFactory;
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
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.TemporalDatum;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.OperationNotFoundException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.DefaultIdentifiedObject;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


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
 */
public class CoordinateOperationFactory extends AbstractCoordinateOperationFactory {
    /**
     * A unit of one millisecond.
     */
    private static final Unit MILLISECOND = SI.MILLI(SI.SECOND);

    /**
     * The operation to use by {@link #createTransformationStep(GeographicCRS,GeographicCRS)} for
     * datum shift. This string can have one of the following values:
     * <p>
     * <ul>
     *   <li><code>"Abridged_Molodenski"</code> for the abridged Molodenski transformation.</li>
     *   <li><code>"Molodenski"</code> for the Molodenski transformation.</li>
     *   <li><code>null</code> for performing datum shifts is geocentric coordinates.</li>
     * </ul>
     */
    private final String molodenskiMethod;

    /**
     * {@code true} if datum shift are allowed even if no Bursa Wolf parameters is available.
     */
    private final boolean lenientDatumShift;

    /**
     * Constructs a coordinate operation factory using the default factories.
     */
    public CoordinateOperationFactory() {
        this((Hints) null);
    }

    /**
     * Constructs a coordinate operation factory using the specified hints.
     */
    public CoordinateOperationFactory(final Hints hints) {
        super(hints);
        //
        // Default hints values
        //
        String  molodenskiMethod  = "Molodenski"; // Alternative: "Abridged_Molodenski"
        boolean lenientDatumShift = false;
        //
        // Fetchs the user-supplied hints
        //
        if (hints != null) {
            Object candidate = hints.get(Hints.DATUM_SHIFT_METHOD);
            if (candidate != null) {
                molodenskiMethod = (String) candidate;
                if (molodenskiMethod.trim().equalsIgnoreCase("Geocentric")) {
                    molodenskiMethod = null;
                }
            }
            candidate = hints.get(Hints.LENIENT_DATUM_SHIFT);
            if (candidate != null) {
                lenientDatumShift = ((Boolean) candidate).booleanValue();
            }
        }
        //
        // Stores the retained hints
        //
        this.molodenskiMethod  = molodenskiMethod;
        this.lenientDatumShift = lenientDatumShift;
        this.hints.put(Hints.DATUM_SHIFT_METHOD,  molodenskiMethod);
        this.hints.put(Hints.LENIENT_DATUM_SHIFT, Boolean.valueOf(lenientDatumShift));
    }

    /**
     * Constructs a coordinate operation factory from the specified math transform factory.
     *
     * @param mtFactory The math transform factory to use.
     *
     * @deprecated Use {@link #CoordinateOperationFactory(Hints)} instead.
     */
    public CoordinateOperationFactory(final MathTransformFactory mtFactory) {
        super(mtFactory);
        molodenskiMethod  = "Molodenski";
        lenientDatumShift = false;
    }

    /**
     * Constructs a coordinate operation factory from a group of factories.
     *
     * @param factories The factories to use.
     *
     * @deprecated Use {@link #CoordinateOperationFactory(Hints)} instead.
     */
    public CoordinateOperationFactory(final FactoryGroup factories) {
        super(factories);
        molodenskiMethod  = "Molodenski";
        lenientDatumShift = false;
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
        ensureNonNull("sourceCRS", sourceCRS);
        ensureNonNull("targetCRS", targetCRS);
        if (equalsIgnoreMetadata(sourceCRS, targetCRS)) {
            final int dim  = getDimension(sourceCRS);
            assert    dim == getDimension(targetCRS) : dim;
            return createFromAffineTransform(IDENTITY, sourceCRS, targetCRS,
                                             new GeneralMatrix(dim+1));
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
            if (targetCRS instanceof VerticalCRS) {
                final VerticalCRS target = (VerticalCRS) targetCRS;
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
            step1 = createFromMathTransform(INVERSE_OPERATION, sourceCRS, base, transform);
            return concatenate(step1, step2);
        }
        ////////////////////////////////////////////
        ////                                    ////
        ////     Compound  -->  various CRS     ////
        ////                                    ////
        ////////////////////////////////////////////
        if (sourceCRS instanceof CompoundCRS) {
            final CompoundCRS source = (CompoundCRS) sourceCRS;
            if (targetCRS instanceof CompoundCRS) {
                final CompoundCRS target = (CompoundCRS) targetCRS;
                return createOperationStep(source, target);
            }
            if (targetCRS instanceof SingleCRS) {
                final SingleCRS target = (SingleCRS) targetCRS;
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
            final int dimSource = getDimension(sourceCRS);
            final int dimTarget = getDimension(targetCRS);
            if (dimTarget == dimSource) {
                final Matrix matrix = new GeneralMatrix(dimTarget+1, dimSource+1);
                return createFromAffineTransform(IDENTITY, sourceCRS, targetCRS, matrix);
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
    ////////////               N O R M A L I Z A T I O N S               ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Makes sure that the specified geocentric CRS uses standard axis,
     * prime meridian and the specified datum.
     * If <code>crs</code> already meets all those conditions, then it is
     * returned unchanged. Otherwise, a new normalized geocentric CRS is
     * created and returned.
     *
     * @param  crs The geocentric coordinate reference system to normalize.
     * @param  datum The expected datum.
     * @return The normalized coordinate reference system.
     * @throws FactoryException if the construction of a new CRS was needed but failed.
     */
    private GeocentricCRS normalize(final GeocentricCRS crs,
                                    final GeodeticDatum datum)
            throws FactoryException
    {
        final CartesianCS STANDARD = org.geotools.referencing.cs.CartesianCS.GEOCENTRIC;
        final GeodeticDatum candidate = (GeodeticDatum) crs.getDatum();
        // TODO: Remove cast once we are allowed to compile against J2SE 1.5.
        if (equalsIgnorePrimeMeridian(candidate, datum)) {
            if (getGreenwichLongitude(candidate.getPrimeMeridian()) ==
                getGreenwichLongitude(datum    .getPrimeMeridian()))
            {
                if (hasStandardAxis(crs.getCoordinateSystem(), STANDARD)) {
                    return crs;
                }
            }
        }
        return factories.getCRSFactory().createGeocentricCRS(getTemporaryName(crs), datum, STANDARD);
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
     * @throws FactoryException if the construction of a new CRS was needed but failed.
     */
    private GeographicCRS normalize(final GeographicCRS      crs,
                                    final boolean forceGreenwich)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
              GeodeticDatum datum = (GeodeticDatum) crs.getDatum();
        final EllipsoidalCS cs    = (EllipsoidalCS) crs.getCoordinateSystem();
        final EllipsoidalCS STANDARD = (cs.getDimension() <= 2) ?
                org.geotools.referencing.cs.EllipsoidalCS.GEODETIC_2D :
                org.geotools.referencing.cs.EllipsoidalCS.GEODETIC_3D;
        if (forceGreenwich && getGreenwichLongitude(datum.getPrimeMeridian()) != 0) {
            datum = new TemporaryDatum(datum);
        } else if (hasStandardAxis(cs, STANDARD)) {
            return crs;
        }
        /*
         * The specified geographic coordinate system doesn't use standard axis
         * (EAST, NORTH) or the greenwich meridian. Create a new one meeting those criterions.
         */
        return factories.getCRSFactory().createGeographicCRS(getTemporaryName(crs), datum, STANDARD);
    }

    /**
     * A datum identical to the specified datum except for the prime meridian, which is replaced
     * by Greenwich. This datum is processed in a special way by {@link #equalsIgnorePrimeMeridian}.
     */
    private static final class TemporaryDatum extends org.geotools.referencing.datum.GeodeticDatum {
        /** The wrapped datum. */
        private final GeodeticDatum datum;

        /** Wrap the specified datum. */
        public TemporaryDatum(final GeodeticDatum datum) {
            super(getTemporaryName(datum), datum.getEllipsoid(), DefaultPrimeMeridian.GREENWICH);
            this.datum = datum;
        }

        /** Unwrap the datum. */
        public static GeodeticDatum unwrap(GeodeticDatum datum) {
            while (datum instanceof TemporaryDatum) {
                datum = ((TemporaryDatum) datum).datum;
            }
            return datum;
        }

        /** Compares this datum with the specified object for equality. */
        public boolean equals(final DefaultIdentifiedObject object,
                              final boolean compareMetadata)
        {
            if (super.equals(object, compareMetadata)) {
                final GeodeticDatum other = ((TemporaryDatum) object).datum;
                return compareMetadata ? datum.equals(other) : equalsIgnoreMetadata(datum, other);
            }
            return false;
        }
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
     * Returns the longitude value relative to the Greenwich Meridian, expressed in degrees.
     */
    private static double getGreenwichLongitude(final PrimeMeridian pm) {
        return getGreenwichLongitude(pm, NonSI.DEGREE_ANGLE);
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
     *
     * @todo Delete and replace by a static import when we
     *       will be allowed to compile against J2SE 1.5.
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
        return createFromAffineTransform(AXIS_CHANGES, sourceCRS, targetCRS, matrix);
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
        final VerticalCS  sourceCS = (VerticalCS) sourceCRS.getCoordinateSystem();
        final VerticalCS  targetCS = (VerticalCS) targetCRS.getCoordinateSystem();
        final Matrix      matrix   = swapAndScaleAxis(sourceCS, targetCS);
        return createFromAffineTransform(AXIS_CHANGES, sourceCRS, targetCRS, matrix);
    }

    /**
     * Creates an operation between a geographic and a vertical coordinate reference systems.
     * The default implementation accepts the conversion only if the geographic CRS is a tri
     * dimensional one and the vertical CRS is for {@linkplain VerticalDatumType#ELLIPSOIDAL
     * height above the ellipsoid}. More elaborated operation, like transformation from
     * ellipsoidal to geoidal height, should be implemented here.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     *
     * @todo Implement GEOT-352 here.
     */
    protected CoordinateOperation createOperationStep(final GeographicCRS sourceCRS,
                                                      final VerticalCRS   targetCRS)
            throws FactoryException
    {
        // TODO: remove cast when we will be allowed to compile for J2SE 1.5.
        if (VerticalDatumType.ELLIPSOIDAL.equals(((VerticalDatum) targetCRS.getDatum()).getVerticalDatumType())) {
            final Matrix matrix = swapAndScaleAxis(sourceCRS.getCoordinateSystem(),
                                                   targetCRS.getCoordinateSystem());
            return createFromAffineTransform(AXIS_CHANGES, sourceCRS, targetCRS, matrix);
        }
        throw new OperationNotFoundException(getErrorMessage(sourceCRS, targetCRS));
    }

    /**
     * Creates an operation between two geographic coordinate reference systems. The default
     * implementation can adjust axis order and orientation (e.g. transforming from
     * <code>(NORTH,WEST)</code> to <code>(EAST,NORTH)</code>), performs units conversion
     * and apply datum shifts if needed.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     *
     * @todo When rotating the prime meridian, we should ensure that
     *       transformed longitudes stay in the range [-180..+180°].
     */
    protected CoordinateOperation createOperationStep(final GeographicCRS sourceCRS,
                                                      final GeographicCRS targetCRS)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final EllipsoidalCS sourceCS    = (EllipsoidalCS) sourceCRS.getCoordinateSystem();
        final EllipsoidalCS targetCS    = (EllipsoidalCS) targetCRS.getCoordinateSystem();
        final GeodeticDatum sourceDatum = (GeodeticDatum) sourceCRS.getDatum();
        final GeodeticDatum targetDatum = (GeodeticDatum) targetCRS.getDatum();
        final PrimeMeridian sourcePM    = sourceDatum.getPrimeMeridian();
        final PrimeMeridian targetPM    = targetDatum.getPrimeMeridian();
        if (equalsIgnorePrimeMeridian(sourceDatum, targetDatum)) {
            /*
             * If both geographic CRS use the same datum, then there is no need for a datum shift.
             * Just swap axis order, and rotate the longitude coordinate if prime meridians are
             * different. Note: this special block is mandatory for avoiding never-ending loop,
             * since it is invoked by 'createOperationStep(GeocentricCRS...)'.
             *
             * TODO: We should ensure that longitude is in range [-180..+180°].
             */
            final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS, sourcePM, targetPM);
            return createFromAffineTransform(AXIS_CHANGES, sourceCRS, targetCRS, matrix);
        }
        /*
         * The two geographic CRS use different datum. If Molodenski transformations
         * are allowed, try them first. Note that is some case if the datum shift can't
         * be performed in a single Molodenski transformation step (i.e. if we need to
         * go through at least one intermediate datum), then we will use the geocentric
         * transform below instead: it allows to concatenates many Bursa Wolf parameters
         * in a single affine transform.
         */
        if (molodenskiMethod != null) {
            Identifier          identifier = DATUM_SHIFT;
            BursaWolfParameters bursaWolf  = null;
            if (sourceDatum instanceof org.geotools.referencing.datum.GeodeticDatum) {
                bursaWolf = ((org.geotools.referencing.datum.GeodeticDatum) sourceDatum)
                             .getBursaWolfParameters(targetDatum);
            }
            if (bursaWolf==null && lenientDatumShift) {
                /*
                 * No BursaWolf parameters available, but the user want us to performs the
                 * datum shift anyway. We will notify the users through positional accuracy.
                 */
                bursaWolf  = new BursaWolfParameters(targetDatum);
                identifier = ELLIPSOID_SHIFT;
            }
            /*
             * Apply the Molodenski transformation now. Note: in current parameters, we can't
             * specify a different input and output dimension. However, our Molodenski transform
             * allows that. We should expand the parameters block for this case (TODO).
             */
            if (bursaWolf!=null && bursaWolf.isTranslation()) {
                final Ellipsoid sourceEllipsoid = sourceDatum.getEllipsoid();
                final Ellipsoid targetEllipsoid = targetDatum.getEllipsoid();
                if (bursaWolf.isIdentity() && equalsIgnoreMetadata(sourceEllipsoid, targetEllipsoid)) {
                    final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS, sourcePM, targetPM);
                    return createFromAffineTransform(ELLIPSOID_SHIFT, sourceCRS, targetCRS, matrix);
                }
                final int sourceDim = getDimension(sourceCRS);
                final int targetDim = getDimension(targetCRS);
                final ParameterValueGroup parameters = mtFactory.getDefaultParameters(molodenskiMethod);
                parameters.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis());
                parameters.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis());
                parameters.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis());
                parameters.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis());
                parameters.parameter("dx")            .setValue(bursaWolf.dx);
                parameters.parameter("dy")            .setValue(bursaWolf.dy);
                parameters.parameter("dz")            .setValue(bursaWolf.dz);
                parameters.parameter("dim")           .setValue(sourceDim);
                if (sourceDim == targetDim) {
                    final CoordinateOperation step1, step2, step3;
                    final GeographicCRS normSourceCRS = normalize(sourceCRS, true);
                    final GeographicCRS normTargetCRS = normalize(targetCRS, true);
                    step1 = createOperationStep(sourceCRS, normSourceCRS);
                    step2 = createFromParameters(identifier, normSourceCRS, normTargetCRS, parameters);
                    step3 = createOperationStep(normTargetCRS, targetCRS);
                    return concatenate(step1, step2, step3);
                } else {
                    // TODO: Need some way to pass 'targetDim' to Molodenski.
                    //       Fallback on geocentric transformations for now.
                }
            }
        }
        /*
         * If the two geographic CRS use different datum, transform from the
         * source to target datum through the geocentric coordinate system.
         * The transformation chain is:
         *
         *     source geographic CRS                                               -->
         *     geocentric CRS with a preference for datum using Greenwich meridian -->
         *     target geographic CRS
         */
        final CartesianCS STANDARD = org.geotools.referencing.cs.CartesianCS.GEOCENTRIC;
        final GeocentricCRS stepCRS;
        final CRSFactory crsFactory = factories.getCRSFactory();
        if (getGreenwichLongitude(targetPM) == 0) {
            stepCRS = crsFactory.createGeocentricCRS(
                      getTemporaryName(targetCRS), targetDatum, STANDARD);
        } else {
            stepCRS = crsFactory.createGeocentricCRS(
                      getTemporaryName(sourceCRS), sourceDatum, STANDARD);
        }
        final CoordinateOperation step1 = createOperationStep(sourceCRS, stepCRS);
        final CoordinateOperation step2 = createOperationStep(stepCRS, targetCRS);
        return concatenate(step1, step2);
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
            return createFromAffineTransform(AXIS_CHANGES, sourceCRS, targetCRS, linear);
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
     *
     * @todo Provides a non-null method.
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
        step1 = createFromMathTransform(INVERSE_OPERATION, sourceCRS, base, transform);
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
     * @todo Transformation version set to "(unknow)". We should search this information somewhere.
     */
    protected CoordinateOperation createOperationStep(final GeocentricCRS sourceCRS,
                                                      final GeocentricCRS targetCRS)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final GeodeticDatum sourceDatum = (GeodeticDatum) sourceCRS.getDatum();
        final GeodeticDatum targetDatum = (GeodeticDatum) targetCRS.getDatum();
        final CoordinateSystem sourceCS = sourceCRS.getCoordinateSystem();
        final CoordinateSystem targetCS = targetCRS.getCoordinateSystem();
        final double sourcePM, targetPM;
        sourcePM = getGreenwichLongitude(sourceDatum.getPrimeMeridian());
        targetPM = getGreenwichLongitude(targetDatum.getPrimeMeridian());
        if (equalsIgnorePrimeMeridian(sourceDatum, targetDatum)) {
            if (sourcePM == targetPM) {
                /*
                 * If both CRS use the same datum and the same prime meridian,
                 * then the transformation is probably just axis swap or unit
                 * conversions.
                 */
                final Matrix matrix = swapAndScaleAxis(sourceCS, targetCS);
                return createFromAffineTransform(AXIS_CHANGES, sourceCRS, targetCRS, matrix);
            }
            // Prime meridians are differents. Performs the full transformation.
        }
        if (sourcePM != targetPM) {
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
        Identifier identifier = DATUM_SHIFT;
        try {
            Matrix datumShift = org.geotools.referencing.datum.GeodeticDatum.
                                      getAffineTransform(TemporaryDatum.unwrap(sourceDatum),
                                                         TemporaryDatum.unwrap(targetDatum));
            if (!(datumShift instanceof GMatrix)) {
                if (lenientDatumShift) {
                    datumShift = new GeneralMatrix(4); // Identity transform.
                    identifier = ELLIPSOID_SHIFT;
                } else {
                    throw new OperationNotFoundException(Resources.format(
                                ResourceKeys.BURSA_WOLF_PARAMETERS_REQUIRED));
                }
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
        return createFromAffineTransform(identifier, sourceCRS, targetCRS, matrix);
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
        final ParameterValueGroup   param = mtFactory.getDefaultParameters("Ellipsoid_To_Geocentric");
        final MathTransform     transform;
        param.parameter("semi_major").setValue(ellipsoid.getSemiMajorAxis(), unit);
        param.parameter("semi_minor").setValue(ellipsoid.getSemiMinorAxis(), unit);
        param.parameter("dim")       .setValue(getDimension(normSourceCRS));

        final CoordinateOperation step1, step2, step3;
        step1 = createOperationStep (sourceCRS, normSourceCRS);
        step2 = createFromParameters(GEOCENTRIC_CONVERSION, normSourceCRS, normTargetCRS, param);
        step3 = createOperationStep (normTargetCRS, targetCRS);
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
        final ParameterValueGroup   param = mtFactory.getDefaultParameters("Geocentric_To_Ellipsoid");
        final MathTransform     transform;
        param.parameter("semi_major").setValue(ellipsoid.getSemiMajorAxis(), unit);
        param.parameter("semi_minor").setValue(ellipsoid.getSemiMinorAxis(), unit);
        param.parameter("dim")       .setValue(getDimension(normTargetCRS));

        final CoordinateOperation step1, step2, step3;
        step1 = createOperationStep (sourceCRS, normSourceCRS);
        step2 = createFromParameters(GEOCENTRIC_CONVERSION, normSourceCRS, normTargetCRS, param);
        step3 = createOperationStep (normTargetCRS, targetCRS);
        return concatenate(step1, step2, step3);
    }

    /**
     * Creates an operation from a compound to a single coordinate reference systems.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     *
     * @todo (GEOT-401) This method work for some simple cases (e.g. no datum change), and give up
     *       otherwise. Before to give up at the end of this method, we should try the following:
     *       <ul>
     *         <li>Maybe <code>sourceCRS</code> uses a non-ellipsoidal height. We should replace
     *             the non-ellipsoidal height by an ellipsoidal one, create a transformation step
     *             for that (to be concatenated), and then try again this operation step.</li>
     *
     *         <li>Maybe <code>sourceCRS</code> contains some extra axis, like a temporal CRS.
     *             We should revisit this code in other to lets supplemental ordinates to be
     *             pass through or removed.</li>
     *       </ul>
     */
    protected CoordinateOperation createOperationStep(final CompoundCRS sourceCRS,
                                                      final SingleCRS   targetCRS)
            throws FactoryException
    {
        final SingleCRS[] sources = org.geotools.referencing.crs.CompoundCRS.getSingleCRS(sourceCRS);
        if (sources.length == 1) {
            return createOperation(sources[0], targetCRS);
        }
        if (!needsGeodetic3D(sources, targetCRS)) {
            // No need for a datum change (see 'needGeodetic3D' javadoc).
            final SingleCRS[] targets = new SingleCRS[] {targetCRS};
            return createOperationStep(sourceCRS, sources, targetCRS, targets);
        }
        /*
         * There is a change of datum.  It may be a vertical datum change (for example from
         * ellipsoidal to geoidal height), in which case geographic coordinates are usually
         * needed. It may also be a geodetic datum change, in which case the height is part
         * of computation. Try to convert the source CRS into a 3D-geodetic CRS.
         */
        final CoordinateReferenceSystem source3D = factories.toGeodetic3D(sourceCRS);
        if (source3D != sourceCRS) {
            return createOperation(source3D, targetCRS);
        }
        /*
         * TODO: Search for non-ellipsoidal height, and lets supplemental axis (e.g. time)
         *       pass through. See javadoc comments above.
         */
        throw new OperationNotFoundException(getErrorMessage(sourceCRS, targetCRS));
    }
    
    /**
     * Creates an operation from a single to a compound coordinate reference system.
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
        final SingleCRS[] targets = org.geotools.referencing.crs.CompoundCRS.getSingleCRS(targetCRS);
        if (targets.length == 1) {
            return createOperation(sourceCRS, targets[0]);
        }
        /*
         * This method has almost no chance to succeed (we can't invent ordinate values!) unless
         * 'sourceCRS' is a 3D-geodetic CRS and 'targetCRS' is a 2D + 1D one. Test for this case.
         * Otherwise, the 'createOperationStep' invocation will throws the appropriate exception.
         */
        final CoordinateReferenceSystem target3D = factories.toGeodetic3D(targetCRS);
        if (target3D != targetCRS) {
            return createOperation(sourceCRS, target3D);
        }
        final SingleCRS[] sources = new SingleCRS[] {sourceCRS};
        return createOperationStep(sourceCRS, sources, targetCRS, targets);
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
        final SingleCRS[] sources = org.geotools.referencing.crs.CompoundCRS.getSingleCRS(sourceCRS);
        final SingleCRS[] targets = org.geotools.referencing.crs.CompoundCRS.getSingleCRS(targetCRS);
        if (targets.length == 1) {
            return createOperation(sourceCRS, targets[0]);
        }
        if (sources.length == 1) { // After 'targets' because more likely to fails to transform.
            return createOperation(sources[0], targetCRS);
        }
        /*
         * If the source CRS contains both a geodetic and a vertical CRS, then we can process
         * only if there is no datum change. If at least one of those CRS appears in the target
         * CRS with a different datum, then the datum shift must be applied on the horizontal and
         * vertical components together.
         */
        for (int i=0; i<targets.length; i++) {
            if (needsGeodetic3D(sources, targets[i])) {
                final CoordinateReferenceSystem source3D = factories.toGeodetic3D(sourceCRS);
                final CoordinateReferenceSystem target3D = factories.toGeodetic3D(targetCRS);
                if (source3D!=sourceCRS || target3D!=targetCRS) {
                    return createOperation(source3D, target3D);
                }
                /*
                 * TODO: Search for non-ellipsoidal height, and lets supplemental axis pass through.
                 *       See javadoc comments for createOperation(CompoundCRS, SingleCRS).
                 */
                throw new OperationNotFoundException(getErrorMessage(sourceCRS, targetCRS));
            }
        }
        // No need for a datum change (see 'needGeodetic3D' javadoc).
        return createOperationStep(sourceCRS, sources, targetCRS, targets);
    }

    /**
     * Implementation of transformation step on compound CRS.
     *
     * <strong>NOTE:</strong>
     * If there is a horizontal (geographic or projected) CRS together with a vertical CRS,
     * then we can't performs the transformation since the vertical value has an impact on
     * the horizontal value, and this impact is not taken in account if the horizontal and
     * vertical components are not together in a 3D geographic CRS.  This case occurs when
     * the vertical CRS is not a height above the ellipsoid. It must be checked by the
     * caller before this method is invoked.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  sources   The source CRS components.
     * @param  targetCRS Output coordinate reference system.
     * @param  targets   The target CRS components.
     * @return A coordinate operation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws FactoryException If the operation can't be constructed.
     */
    private CoordinateOperation createOperationStep(final CoordinateReferenceSystem sourceCRS,
                                                    final SingleCRS[]               sources,
                                                    final CoordinateReferenceSystem targetCRS,
                                                    final SingleCRS[]               targets)
            throws FactoryException
    {
        /*
         * Try to find operations from source CRSs to target CRSs. All pairwise combinaisons are
         * tried, but the preference is given to CRS in the same order (source[0] with target[0],
         * source[1] with target[1], etc.). Operations found are stored in 'steps', but are not
         * yet given to pass through transforms. We need to know first if some ordinate values
         * need reordering (for matching the order of target CRS) if any ordinates reordering and
         * source ordinates drops are required.
         */
        final CoordinateReferenceSystem[] ordered = new CoordinateReferenceSystem[targets.length];
        final CoordinateOperation[]       steps   = new CoordinateOperation      [targets.length];
        final boolean[]                   done    = new boolean                  [sources.length];
        final int[]                       indices = new int[getDimension(sourceCRS)];
        int count=0, dimensions=0;
search: for (int j=0; j<targets.length; j++) {
            int lower, upper=0;
            final CoordinateReferenceSystem target = targets[j];
            OperationNotFoundException cause = null;
            for (int i=0; i<sources.length; i++) {
                final CoordinateReferenceSystem source = sources[i];
                lower  = upper;
                upper += getDimension(source);
                if (done[i]) continue;
                try {
                    steps[count] = createOperation(source, target);
                } catch (OperationNotFoundException exception) {
                    // No operation path for this pair.
                    // Search for an other pair.
                    if (cause==null || i==j) {
                        cause = exception;
                    }
                    continue;
                }
                ordered[count++] = source;
                while (lower < upper) {
                    indices[dimensions++] = lower++;
                }
                done[i] = true;
                continue search;
            }
            /*
             * No source CRS was found for current target CRS.
             * Concequently, we can't get a transformation path.
             */
            throw new OperationNotFoundException(getErrorMessage(sourceCRS, targetCRS), cause);
        }
        /*
         * A transformation has been found for every source and target CRS pairs.
         * Some reordering of ordinate values may be needed. Prepare it now as an
         * affine transform. This transform also drop source dimensions not used
         * for any target coordinates.
         */
        assert count == targets.length : count;
        while (count!=0 && steps[--count].getMathTransform().isIdentity());
        CoordinateOperation  operation = null;
        CoordinateReferenceSystem sourceStepCRS = sourceCRS;
        final GeneralMatrix select = new GeneralMatrix(dimensions+1, indices.length+1);
        select.setZero();
        select.setElement(dimensions, indices.length, 1);
        for (int j=0; j<dimensions; j++) {
            select.setElement(j, indices[j], 1);
        }
        if (!select.isIdentity()) {
            if (ordered.length == 1) {
                sourceStepCRS = ordered[0];
            } else {
                sourceStepCRS = factories.getCRSFactory().createCompoundCRS(
                                    getTemporaryName(sourceCRS), ordered);
            }
            operation = createFromAffineTransform(AXIS_CHANGES, sourceCRS, sourceStepCRS, select);
        }
        /*
         * Now creates the pass through transforms for each transformation steps found above.
         * We get (or construct temporary) source and target CRS for this step. They will be
         * given to the constructor of the pass through operation, after the construction of
         * pass through transform.
         */
        int lower, upper=0;
        for (int i=0; i<targets.length; i++) {
            CoordinateOperation step = steps[i];
            final Map properties = getProperties(step);
            final CoordinateReferenceSystem source = ordered[i];
            final CoordinateReferenceSystem target = targets[i];
            final CoordinateReferenceSystem targetStepCRS;
            ordered[i] = target; // Used for the construction of targetStepCRS.
            MathTransform mt = step.getMathTransform();
            if (i >= count) {
                targetStepCRS = targetCRS;
            } else if (mt.isIdentity()) {
                targetStepCRS = sourceStepCRS;
            } else if (ordered.length == 1) {
                targetStepCRS = ordered[0];
            } else {
                targetStepCRS = factories.getCRSFactory().createCompoundCRS(
                                    getTemporaryName(target), ordered);
            }
            lower  = upper;
            upper += getDimension(source);
            if (lower!=0 || upper!=dimensions) {
                /*
                 * Constructs the pass through transform only if there is at least one ordinate to
                 * pass. Actually, the code below would give an acceptable result even if this check
                 * was not performed, except that the exception below could be unnecessary thrown.
                 */
                if (!(step instanceof Operation)) {
                    // TODO
                    throw new OperationNotFoundException("Concatenated operation not supported.");
                }
                mt   = mtFactory.createPassThroughTransform(lower, mt, dimensions-upper);
                step = new PassThroughOperation(properties, sourceStepCRS, targetStepCRS,
                                               (Operation) step, mt);
            }
            operation     = (operation==null) ? step : concatenate(operation, step);
            sourceStepCRS = targetStepCRS;
        }
        assert upper == dimensions : upper;
        return operation;
    }

    /**
     * Returns <code>true</code> if a transformation path from <code>sourceCRS</code> to
     * <code>targetCRS</code> is likely to requires a tri-dimensional geodetic CRS as an
     * intermediate step. More specifically, this method returns <code>false</code> if at
     * lest one of the following conditions is meet:
     *
     * <ul>
     *   <li>The target datum is not a vertical or geodetic one (the two datum that must work
     *       together). Consequently, a potential datum change is not the caller's business.
     *       It will be handled by the generic method above.</li>
     *
     *   <li>The target datum is vertical or geodetic, but there is no datum change. It is
     *       better to not try to create 3D-geodetic CRS, since they are more difficult to
     *       separate in the generic method above.</li>
     *
     *   <li>A datum change is required, but source CRS doesn't have both a geodetic
     *       and a vertical CRS, so we can't apply a 3D datum shift anyway.</li>
     * </ul>
     */
    private static boolean needsGeodetic3D(final SingleCRS[] sourceCRS, final SingleCRS targetCRS) {
        final boolean targetGeodetic;
        final Datum targetDatum = targetCRS.getDatum();
        if (targetDatum instanceof GeodeticDatum) {
            targetGeodetic = true;
        } else if (targetDatum instanceof VerticalDatum) {
            targetGeodetic = false;
        } else {
            return false;
        }
        boolean horizontal = false;
        boolean vertical   = false;
        boolean shift      = false;
        for (int i=0; i<sourceCRS.length; i++) {
            final Datum sourceDatum = sourceCRS[i].getDatum();
            final boolean sourceGeodetic;
            if (sourceDatum instanceof GeodeticDatum) {
                horizontal     = true;
                sourceGeodetic = true;
            } else if (sourceDatum instanceof VerticalDatum) {
                vertical       = true;
                sourceGeodetic = false;
            } else {
                continue;
            }
            if (!shift && sourceGeodetic==targetGeodetic) {
                shift = !equalsIgnoreMetadata(sourceDatum, targetDatum);
                assert Utilities.sameInterfaces(sourceDatum.getClass(),
                                                targetDatum.getClass(),
                                                Datum.class);
            }
        }
        return shift && horizontal && vertical;
    }
    




    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    ////////////                                                         ////////////
    ////////////                M I S C E L L A N E O U S                ////////////
    ////////////                                                         ////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Compare the specified datum for equality, except the prime meridian.
     *
     * @param  object1 The first object to compare (may be null).
     * @param  object2 The second object to compare (may be null).
     * @return <code>true</code> if both objects are equals.
     */
    private static boolean equalsIgnorePrimeMeridian(GeodeticDatum object1,
                                                     GeodeticDatum object2)
    {
        object1 = TemporaryDatum.unwrap(object1);
        object2 = TemporaryDatum.unwrap(object2);
        if (equalsIgnoreMetadata(object1.getEllipsoid(), object2.getEllipsoid())) {
            return nameMatches(object1, object2.getName().getCode()) ||
                   nameMatches(object2, object1.getName().getCode());
        }
        return false;
    }

    /**
     * Returns <code>true</code> if either the primary name or at least
     * one alias matches the specified string.
     *
     * @param  object The object to check.
     * @param  name The name.
     * @return <code>true</code> if the primary name of at least one alias
     *         matches the specified <code>name</code>.
     *
     * @todo Delete and replace by a static import when we
     *       will be allowed to compile against J2SE 1.5.
     */
    private static boolean nameMatches(final IdentifiedObject object, final String name) {
        return DefaultIdentifiedObject.nameMatches(object, name);
    }
}
