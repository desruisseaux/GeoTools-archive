/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.builder;

// J2SE and extensions
import java.util.Map;
import java.util.HashMap;
import javax.vecmath.MismatchedSizeException;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.*;       // Includes imports used only for javadoc.
import org.opengis.referencing.operation.*; // Includes imports used only for javadoc.
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.quality.EvaluationMethodType;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.operation.DefaultOperationMethod;
import org.geotools.referencing.operation.DefaultTransformation;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.metadata.iso.quality.PositionalAccuracyImpl;
import org.geotools.metadata.iso.quality.QuantitativeResultImpl;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;


/**
 * Provides a basic implementation for {@linkplain MathTransform math transform} builders.
 * Math transform builders create {@link MathTransform} objects for transforming coordinates
 * from a source CRS ({@linkplain CoordinateReferenceSystem Coordinate Reference System}) to
 * a target CRS using empirical parameters. Usually, one of those CRS is a
 * {@linkplain GeographicCRS geographic} or {@linkplain ProjectedCRS projected}
 * one with a well known relationship to the earth. The other CRS is often an
 * {@linkplain EngineeringCRS engineering} or {@linkplain ImageCRS image} one
 * tied to some ship. For example a remote sensing image <em>before</em>
 * georectification may be referenced by an {@linkplain ImageCRS image CRS}.
 *
 * <blockquote><p><font size=-1><strong>Design note:</strong>
 * It is technically possible to reference such remote sensing images with a
 * {@linkplain DerivedCRS CRS derived} from the geographic or projected CRS,
 * where the {@linkplain DerivedCRS#getConversionFromBase conversion from base}
 * is the math transform {@linkplain #getMathTransform computed by this builder}.
 * Such approach is advantageous for {@linkplain CoordinateOperationFactory
 * coordinate operation factory} implementations, since they can determine the
 * operation just by inspection of the {@link DerivedCRS} instance. However this
 * is conceptually incorrect since {@link DerivedCRS} can be related to an other
 * CRS only through {@linkplain Conversion conversions}, which by definition are
 * accurate up to rounding errors. The operations created by math transform
 * builders are rather {@linkplain Transformation transformations}, which can't
 * be used for {@link DerivedCRS} creation.
 * </font></p></blockquote>
 *
 * The math transform from {@linkplain #getSourceCRS source CRS} to {@linkplain
 * #getTargetCRS target CRS} is calculated by {@code MathTransformBuilder} from
 * a set of {@linkplain #getSourcePoints points in source CRS} and a set of
 * {@linkplain #getDestinationPoints points in target CRS}.
 * <p>
 * Subclasses must implement at least the {@link #getMinimumPointCount()} and
 * {@link #computeMathTransform()} methods.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Jan Jezek
 * @author Martin Desruisseaux
 */
public abstract class MathTransformBuilder {
    /**
     * Set of source points.
     *
     * @todo Try to make this field a private one, and maybe a List.
     */
    protected DirectPosition[] sourcePoints;

    /**
     * Set of destination points.
     *
     * @todo Try to make this field a private one, and maybe a List.
     */
    protected DirectPosition[] targetPoints;

    /**
     * Coordinate Reference System of the source and target points,
     * or {@code null} if unknown.
     */
    private CoordinateReferenceSystem sourceCRS, targetCRS;

    /**
     * The math transform. Will be computed only when first needed.
     */
    private transient MathTransform transform;

    /**
     * The transformation. Will be computed only when first needed.
     */
    private transient Transformation transformation;

    /**
     * The factory to use for creating {@link MathTransform math transform} instances.
     */
    protected final MathTransformFactory mtFactory;

    /**
     * The CRS factory to use for creating {@link EngineeringCRS} instances.
     */
    private final CRSFactory crsFactory;

    /**
     * The datum factory to use for creating {@link EngineeringCRS} instances.
     */
    private final DatumFactory datumFactory;

    /**
     * Creates a builder with the default factories.
     */
    public MathTransformBuilder() {
        this(null);
    }

    /**
     * Creates a builder from the specified hints.
     */
    public MathTransformBuilder(final Hints hints) {
        mtFactory    = FactoryFinder.getMathTransformFactory(hints);
        crsFactory   = FactoryFinder.getCRSFactory          (hints);
        datumFactory = FactoryFinder.getDatumFactory        (hints);
    }

    /**
     * Returns the name for the {@linkplain Transformation transformation} to
     * be created by this builder.
     *
     * @todo Consider making this method public, and provide some way for the
     *       user to specify a name. Also need to find a better default name.
     */
    private String getName() {
        return Utilities.getShortClassName(this) + " fit";
    }

    /**
     * Returns the minimum number of points required by this builder. This minimum depends on the
     * algorithm used. For example {@linkplain AffineTransformBuilder affine transform builders}
     * require at least 3 points, while {@linkplain SimilarParamCalculator similar transform
     * builders} requires only 2 points.
     */
    public abstract int getMinimumPointCount();

    /**
     * Returns the dimension for {@linkplain #getSourceCRS source} and
     * {@link #getTargetCRS target} CRS. The default value is 2.
     */
    public int getDimension() {
        return 2;
    }

    /**
     * Sets the {@linkplain #sourcePoints source points}.
     *
     * @param  sourcePoints The source points.
     * @throws MismatchedSizeException if the list doesn't have the expected number of points.
     * @throws MismatchedDimensionException if some points doesn't have the
     *         {@linkplain #getDimension expected number of dimensions}.
     * @throws MismatchedReferenceSystemException if CRS is not the same for all points.
     */
    public void setSourcePoints(final DirectPosition[] points)
            throws MismatchedSizeException, MismatchedDimensionException,
                   MismatchedReferenceSystemException
    {
        // Set the points only after we checked them.
        sourceCRS    = ensureValid(points, "sourcePoints");
        sourcePoints = (DirectPosition[]) points.clone();
        transform    = null;
    }

    /**
     * Sets the {@linkplain #targetPoints target points}.
     *
     * @param  points The target points.
     * @throws MismatchedSizeException if the list doesn't have the expected number of points.
     * @throws MismatchedDimensionException if some points doesn't have the
     *         {@linkplain #getDimension expected number of dimensions}.
     * @throws MismatchedReferenceSystemException if CRS is not the same for all points.
     */
    public void setTargetPoints(final DirectPosition[] points)
            throws MismatchedSizeException, MismatchedDimensionException,
                   MismatchedReferenceSystemException
    {
        // Set the points only after we checked them.
        targetCRS    = ensureValid(points, "targetPoints");
        targetPoints = (DirectPosition[]) points.clone();
        transform    = null;
    }

    /**
     * Returns the coordinate reference system for the {@link #sourcePoints source points}.
     * This method determines the CRS as below:
     * <p>
     * <ul>
     *   <li>If at least one {@linkplain #sourcePoints source points} has a
     *       CRS, then this CRS is selected as the source one and returned.</li>
     *   <li>If no source point has a CRS, then this method creates an
     *       {@linkplain EngineeringCRS engineering CRS} using the same
     *       {@linkplain CoordinateSystem coordinate system} than the one used
     *       by the {@linkplain #getTargetCRS target CRS}.</li>
     * </ul>
     *
     * @throws FactoryException if the CRS can't be created.
     */
    public CoordinateReferenceSystem getSourceCRS() throws FactoryException {
        if (sourceCRS == null) {
            sourceCRS = createEngineeringCRS(targetCRS, sourcePoints);
        }
        assert sourceCRS.getCoordinateSystem().getDimension() == getDimension();
        return sourceCRS;
    }

    /**
     * Returns the coordinate reference system for the {@link #targetPoints source points}.
     * This method determines the CRS as below:
     * <p>
     * <ul>
     *   <li>If at least one {@linkplain #targetPoints target points} has a
     *       CRS, then this CRS is selected as the source one and returned.</li>
     *   <li>If no target point has a CRS, then this method creates an
     *       {@linkplain EngineeringCRS engineering CRS} using the same
     *       {@linkplain CoordinateSystem coordinate system} than the one used
     *       by the {@linkplain #getSourceCRS source CRS}.</li>
     * </ul>
     *
     * @throws FactoryException if the CRS can't be created.
     */
    public CoordinateReferenceSystem getTargetCRS() throws FactoryException {
        if (targetCRS == null) {
            targetCRS = createEngineeringCRS(sourceCRS, targetPoints);
        }
        assert targetCRS.getCoordinateSystem().getDimension() == getDimension();
        return targetCRS;
    }

    /**
     * Creates an engineering CRS using the same {@linkplain CoordinateSystem
     * coordinate system} than the specified CRS, and an area of validity
     * determined from the specified points. This method is used for creating
     * a {@linkplain #getTargetCRS target CRS} from the
     * {@linkplain #getSourceCRS source CRS}, or conversely.
     *
     * @param oppositeCRS A CRS to use as template, or {@code null} if none.
     * @param points The points inside the valid area, or {@code null} if none.
     * @throws FactoryException if the CRS can't be created.
     */
    private EngineeringCRS createEngineeringCRS(final CoordinateReferenceSystem oppositeCRS,
                                                final DirectPosition[] points)
            throws FactoryException
    {
        final Map properties = new HashMap(4);
        properties.put(CoordinateReferenceSystem.NAME_KEY, Vocabulary.format(VocabularyKeys.UNKNOW));
        final GeographicExtent validArea = getValidArea(points);
        if (validArea != null) {
            final ExtentImpl extent = new ExtentImpl();
            extent.getGeographicElements().add(validArea);
            properties.put(CoordinateReferenceSystem.VALID_AREA_KEY, extent.unmodifiable());
        }
        final CoordinateSystem cs;
        if (oppositeCRS != null) {
            cs = oppositeCRS.getCoordinateSystem();
        } else {
            switch (getDimension()) {
                case 2: cs = DefaultCartesianCS.GENERIC_2D; break;
                case 3: cs = DefaultCartesianCS.GENERIC_3D; break;
                default: throw new FactoryException(Errors.format(ErrorKeys.UNSPECIFIED_CRS));
            }
        }
        return crsFactory.createEngineeringCRS(properties,
                datumFactory.createEngineeringDatum(properties), cs);
    }

    /**
     * Returns a geographic extent that contains fully all the specified points.
     * If the envelope can't be calculated, then this method returns {@code null}.
     */
    private static GeographicBoundingBox getValidArea(final DirectPosition[] points) {
        GeneralEnvelope envelope = null;
        CoordinateReferenceSystem crs = null;
        for (int i=0; i<points.length; i++) {
            final DirectPosition point = points[i];
            if (point != null) {
                if (envelope == null) {
                    final double[] coordinates = point.getCoordinates();
                    envelope = new GeneralEnvelope(coordinates, coordinates);
                } else {
                    envelope.add(point);
                }
                crs = getCoordinateReferenceSystem(point, crs);
            }
        }
        if (envelope != null) try {
            envelope.setCoordinateReferenceSystem(crs);
            return new GeographicBoundingBoxImpl(envelope);
        } catch (TransformException exception) {
            /*
             * Can't transform the envelope. Do not rethrown this exception. We don't
             * log it neither (at least not at the warning level) because this method
             * is optional.
             */
        }
        return null;
    }

    /**
     * Returns the CRS of the specified point. If the CRS of the previous point is known,
     * it can be specified. This method will then ensure that the two CRS are compatibles.
     */
    private static CoordinateReferenceSystem getCoordinateReferenceSystem(
            final DirectPosition point, CoordinateReferenceSystem previousCRS)
            throws MismatchedReferenceSystemException
    {
        final CoordinateReferenceSystem candidate = point.getCoordinateReferenceSystem();
        if (candidate != null) {
            if (previousCRS == null) {
                return candidate;
            }
            /*
             * We use strict 'equals' instead of 'equalsIgnoreCase' because if the metadata
             * are not identical, we have no easy way to choose which CRS is the "main" one.
             */
            if (!previousCRS.equals(candidate)) {
                throw new MismatchedReferenceSystemException(
                        Errors.format(ErrorKeys.MISMATCHED_COORDINATE_REFERENCE_SYSTEM));
            }
        }
        return previousCRS;
    }

    /**
     * Ensures that the specified list of points is valid, and returns their CRS.
     *
     * @param points The points to check.
     * @param label  The argument name, used for formatting error message only.
     *
     * @throws MismatchedSizeException if the list doesn't have the expected number of points.
     * @throws MismatchedDimensionException if some points doesn't have the
     *         {@linkplain #getDimension expected number of dimensions}.
     * @throws MismatchedReferenceSystemException if CRS is not the same for all points.
     * @return The CRS used for the specified points, or {@code null} if unknown.
     */
    private CoordinateReferenceSystem ensureValid(final DirectPosition[] points,
                                                  final String label)
            throws MismatchedSizeException, MismatchedDimensionException,
                   MismatchedReferenceSystemException
    {
        final int necessaryNumber = getMinimumPointCount();
        if (points.length < necessaryNumber) {
            throw new MismatchedSizeException(Errors.format(ErrorKeys.INSUFFICIENT_POINTS_$2,
                        new Integer(points.length), new Integer(necessaryNumber)));
        }
        CoordinateReferenceSystem crs = null;
        final int dimension = getDimension();
        for (int i=0; i<points.length; i++) {
            final DirectPosition point = points[i];
            final int pointDim = point.getDimension();
            if (pointDim != dimension) {
                throw new MismatchedDimensionException(Errors.format(
                        ErrorKeys.MISMATCHED_DIMENSION_$3, label + '[' + i + ']',
                        new Integer(pointDim), new Integer(dimension)));
            }
            crs = getCoordinateReferenceSystem(point, crs);
        }
        return crs;
    }

    /**
     * Check the {@link #sourcePoints} and {@link #targetPoints} whether they have:
     * Same dimension, The number of {@link #sourcePoints} and {@link #targetPoints} is the
     * same, The number of {@link #sourcePoints} and {@link #targetPoints} is at least same
     * as number of necessary point that defines this transformation, The
     * Coordinate Reference System of {@link #sourcePoints} and {@link #targetPoints} is the
     * same or null.
     *
     * @throws MismatchedDimensionException if the number and dimension of {@link
     *         #sourcePoints} and {@link #targetPoints} is not valid.
     * @throws MismatchedReferenceSystemException if there is wrong Cordinate Reference System.
     *
     * @deprecated Should not be needed anymore.
     */
    final void checkPoints() throws MismatchedSizeException,
            MismatchedDimensionException, MismatchedReferenceSystemException
    {
        final int necessaryNumber = getMinimumPointCount();
        final int dim = getDimension();
        // initalize the value of Coordinate refernce system
        sourceCRS = sourcePoints[0].getCoordinateReferenceSystem();

        if (targetPoints.length != sourcePoints.length) {
            throw new MismatchedSizeException(
                "Number of source and destination points must be the same ");
        }

        if (targetPoints.length < necessaryNumber) {
            throw new MismatchedSizeException("Set at least " + necessaryNumber
                + " pairs of points");
        }

        for (int i = 0; i < targetPoints.length; i++) {
            if ((targetPoints[i].getDimension() != dim)
                    || (sourcePoints[i].getDimension() != dim)) {
                throw new MismatchedDimensionException(
                    "All points must have a dimension equal to " + dim + ".");
            }

            try {
                if (!targetPoints[i].getCoordinateReferenceSystem()
                                 .equals(sourcePoints[i].getCoordinateReferenceSystem())
                        && !targetPoints[i].getCoordinateReferenceSystem().equals(sourceCRS)) {
                    throw new MismatchedReferenceSystemException("All points must have same CRS.");
                }
            } catch (NullPointerException e) {
                if (sourceCRS != null) {
                    throw new MismatchedReferenceSystemException("All points must have same CRS.");
                }

                // CRS can be null but for all points.
            }
        }
    }

    /**
     * Returns standard deviation of this transformation method.
     *
     * @throws FactoryException If the math transform can't be created or used.
     */
    public double getStandardDeviation() throws FactoryException {
        final int dimension = getDimension();
        final double[] points = new double[sourcePoints.length * dimension];
        /*
         * Copies the source points in an array to be transformed. No need to copy the
         * target points; we will loop directly over them later, which avoid the cost
         * of creating a temporary array.
         */
        for (int i=0; i<sourcePoints.length; i++) {
            final int base = i * dimension;
            final DirectPosition point = sourcePoints[i];
            for (int j=0; j<dimension; j++) {
                points[base + j] = point.getOrdinate(j);
            }
        }
        /*
         * Transforms the source point using the math transform calculated by this class.
         * If the transform can't be applied, then we consider this failure as if it was
         * a factory error rather than a transformation error. This simplify the exception
         * declaration, but also has some sense on a conceptual point of view. We are
         * transforming the exact same points than the one used for creating the math
         * transform. If one of those points can't be transformed, then there is probably
         * something wrong with the transform we just created.
         */
        try {
            getMathTransform().transform(points, 0, points, 0, sourcePoints.length);
        } catch (TransformException e) {
            throw new FactoryException(Errors.format(ErrorKeys.CANT_TRANSFORM_VALID_POINTS), e);
        }
        // Calculation of the standard deviation
        double sum = 0;
        for (int i=0; i<targetPoints.length; i++) {
            final int base = i * dimension;
            final DirectPosition point = targetPoints[i];
            for (int j=0; j<dimension; j++) {
                final double delta = point.getOrdinate(j) - points[base + j];
                sum += delta*delta;
            }
        }
        return Math.sqrt(sum / targetPoints.length);
    }

    /**
     * Calculates the math transform immediately.
     *
     * @return The math transform from the set of {@link #sourcePoints} and {@link #targetPoints}.
     * @throws FactoryException if the math transform can't be created.
     */
    protected abstract MathTransform computeMathTransform() throws FactoryException;

    /**
     * Returns the calculated math transform. This method {@linkplain #computes the math
     * transform} the first time it is requested.
     *
     * @return The math transform from the set of {@link #sourcePoints} and {@link #targetPoints}.
     * @throws FactoryException if the math transform can't be created.
     */
    public final MathTransform getMathTransform() throws FactoryException {
        if (transform == null) {
            transform = computeMathTransform();
        }
        return transform;
    }

    /**
     * Returns the coordinate operation wrapping the {@linkplain #getMathTransform() calculated
     * math transform}. The {@linkplain Transformation#getPositionalAccuracy positional
     * accuracy} will be set to the Root Mean Square (RMS) of the differences between the
     * source points transformed to the target CRS, and the expected target points.
     */
    public Transformation getTransformation() throws FactoryException {
        if (transformation == null) {
            final Map properties = new HashMap();
            properties.put(Transformation.NAME_KEY, getName());
            /*
             * Set the valid area as the intersection of source CRS and target CRS valid area.
             */
            final CoordinateReferenceSystem sourceCRS = getSourceCRS();
            final CoordinateReferenceSystem targetCRS = getTargetCRS();
            final GeographicBoundingBox sourceBox = CRS.getGeographicBoundingBox(sourceCRS);
            final GeographicBoundingBox targetBox = CRS.getGeographicBoundingBox(targetCRS);
            final GeographicBoundingBox validArea;
            if (sourceBox == null) {
                validArea = targetBox;
            } else if (targetBox == null) {
                validArea = sourceBox;
            } else {
                final GeneralEnvelope area = new GeneralEnvelope(sourceBox);
                area.intersect(new GeneralEnvelope(sourceBox));
                try {
                    validArea = new GeographicBoundingBoxImpl(area);
                } catch (TransformException e) {
                    // Should never happen, because we know that 'area' CRS is WGS84.
                    throw new AssertionError(e);
                }
            }
            if (validArea != null) {
                final ExtentImpl extent = new ExtentImpl();
                extent.getGeographicElements().add(validArea);
                properties.put(Transformation.VALID_AREA_KEY, extent.unmodifiable());
            }
            /*
             * Computes the positional accuracy as the RMS value of differences
             * between the computed target points and the supplied target points.
             */
            final double error = getStandardDeviation();
            if (!Double.isNaN(error)) {
                final InternationalString description =
                        Vocabulary.formatInternational(VocabularyKeys.ROOT_MEAN_SQUARED_ERROR);
                final QuantitativeResultImpl result = new QuantitativeResultImpl();
                result.setValues(new double[] {error});
                result.setValueType(Double.TYPE);
                result.setValueUnit(CRSUtilities.getUnit(targetCRS.getCoordinateSystem()));
                result.setErrorStatistic(description);
                final PositionalAccuracyImpl accuracy = new PositionalAccuracyImpl(result);
                accuracy.setEvaluationMethodType(EvaluationMethodType.DIRECT_INTERNAL);
                accuracy.setEvaluationMethodDescription(description);
                properties.put(Transformation.POSITIONAL_ACCURACY_KEY, accuracy.unmodifiable());
            }
            /*
             * Now creates the transformation.
             */
            final MathTransform transform = getMathTransform();
            transformation = new DefaultTransformation(properties, sourceCRS, targetCRS, transform,
                             new DefaultOperationMethod(transform));
        }
        return transformation;
    }
}
