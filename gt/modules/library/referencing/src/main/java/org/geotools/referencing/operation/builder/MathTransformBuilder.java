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
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.*;       // Includes imports used only for javadoc.
import org.opengis.referencing.operation.*; // Includes imports used only for javadoc.
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.operation.DefaultOperationMethod;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
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
 * {@link #getMathTransform()} methods.
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
        final GeographicExtent ge = getValidArea(points);
        if (ge != null) {
            final ExtentImpl extent = new ExtentImpl();
            extent.getGeographicElements().add(ge);
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
     *
     * @todo Provides an error message.
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
            if (!CRS.equalsIgnoreMetadata(previousCRS, candidate)) {
                throw new MismatchedReferenceSystemException();
            }
        }
        return previousCRS;
    }

    /**
     * Sets the {@link #sourcePoints}
     * 
     * 
     * @param sourcePoints {@link #sourcePoints}
     * @throws MismatchedSizeException if the list doesn't have the expected number of points.
     * @throws MismatchedDimensionException if some points doesn't have the
     *         {@linkplain #getDimension expected number of dimensions}.
     * @throws MismatchedReferenceSystemException if CRS is not the same for all points.
     */
    public void setSourcePoints(final DirectPosition[] ptSrc)
            throws MismatchedSizeException, MismatchedDimensionException,
                   MismatchedReferenceSystemException
    {
        this.sourcePoints = ptSrc;
        sourceCRS = ensureValid(ptSrc, "sourcePoints");
    }

    /**
     * Sets the {@link #targetPoints}
     * 
     * 
     * @param targetPoints {@link #targetPoints}
     * @throws MismatchedSizeException if the list doesn't have the expected number of points.
     * @throws MismatchedDimensionException if some points doesn't have the
     *         {@linkplain #getDimension expected number of dimensions}.
     * @throws MismatchedReferenceSystemException if CRS is not the same for all points.
     */
    public void setTargetPoints(final DirectPosition[] ptDst)
            throws MismatchedSizeException, MismatchedDimensionException,
                   MismatchedReferenceSystemException
    {
        this.targetPoints = ptDst;
        targetCRS = ensureValid(ptDst, "targetPoints");
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
            // TODO: localize
            throw new MismatchedSizeException("Set at least " + necessaryNumber + " points");
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
            final CoordinateReferenceSystem candidate = point.getCoordinateReferenceSystem();
            if (candidate != null) {
                if (crs == null) {
                    crs = candidate;
                } else if (!crs.equals(candidate)) {
                    // TODO: localize
                    throw new MismatchedReferenceSystemException(
                            "All points must have same CRS.");
                }
            }
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
     * 
     * 
     * @param necessaryNumber The number of necessary point that defines this
     *        transformation.
     * @param dim The dimension that must {@link #sourcePoints} and {@link #targetPoints}
     *        have.
     * @throws MismatchedDimensionException if the number and dimension of {@link
     *         #sourcePoints} and {@link #targetPoints} is not valid.
     * @throws MismatchedReferenceSystemException if there is wrong Cordinate Reference System.
     *
     * @deprecated To be replaced by a no-args method.
     */
    protected void checkPoints(int necessaryNumber, int dim)
        throws MismatchedSizeException, MismatchedDimensionException, MismatchedReferenceSystemException {
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
     * @return standard deviation
     *
     * @throws FactoryException
     */
    public double getStandardDeviation() throws FactoryException, TransformException {
        final int dimension = sourcePoints[0].getDimension();
        double[] points       = new double[sourcePoints.length * dimension];
        double[] pointsDst    = new double[sourcePoints.length * dimension];
        double[] ptCalculated = new double[sourcePoints.length * dimension];

        for (int i = 0; i < sourcePoints.length; i++) {
            final int base = i * dimension;
            for (int j = 0; j < sourcePoints[i].getDimension(); j++) {
                points   [base + j] = sourcePoints[i].getOrdinate(j);
                pointsDst[base + j] = targetPoints[i].getOrdinate(j);
            }
        }
        getMathTransform().transform(points, 0, ptCalculated, 0, sourcePoints.length);

        // Calculation of the standard deviation
        double sum = 0;
        for (int i = 0; i < pointsDst.length; i++) {
            sum += (pointsDst[i] - ptCalculated[i]) * (pointsDst[i] - ptCalculated[i]);
        }
        return Math.sqrt(sum / (pointsDst.length / sourcePoints[0].getDimension()));
    }

    /**
     * Returns the calculated math transform.
     * 
     * 
     * 
     * @return The math transform from the set of {@link #sourcePoints} and {@link #targetPoints}.
     * @throws FactoryException
     */
    public abstract MathTransform getMathTransform() throws FactoryException;
}
