/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.calculator;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;


/**
 * Provides a basic implementation ParamCalculators. {@link
 * AbstractParamCalculator} does not implements {@link #getMathTransform()}.
 *
 * @author jezekjan
 */
public abstract class AbstractParamCalculator {
    /** Set of source points */
    protected DirectPosition[] ptSrc;

    /** Set of destination points */
    protected DirectPosition[] ptDst;

    /** Coordinate Reference System of the points. */
    protected CoordinateReferenceSystem CRS;

    /**
     * Sets the {@link #ptSrc}
     *
     * @param ptSrc {@link #ptSrc}
     */
    public void setSourcePoints(DirectPosition[] ptSrc) {
        this.ptSrc = ptSrc;
    }

    /**
     * Sets the {@link #ptDst}
     *
     * @param ptDst {@link #ptDst}
     */
    public void setDestinationPoints(DirectPosition[] ptDst) {
        this.ptDst = ptDst;
    }

    /**
     * Check the {@link #ptSrc} and {@link #ptDst} whether they have:
     * Same dimesion, The number of {@link #ptSrc} and {@link #ptDst} is the
     * same, The number of {@link #ptSrc} and {@link #ptDst} is at least same
     * as number of necessary point that defines this transformation, The
     * Coordinate Reference System of {@link #ptSrc} and {@link #ptDst} is the
     * same or null.
     *
     * @param necessaryNumber The number of necessary point that defines this
     *        transformation.
     * @param dim The dimension that must {@link #ptSrc} and {@link #ptDst}
     *        have.
     *
     * @throws CalculationException
     * @throws CRSException CRSException
     */
    protected void checkPoints(int necessaryNumber, int dim)
        throws CalculationException, CRSException {
        // initalize the value of Coordinate refernce system
        CRS = ptSrc[0].getCoordinateReferenceSystem();

        if (ptDst.length != ptSrc.length) {
            throw new CalculationException(
                "Number of source and destination points must be the same ");
        }

        if (ptDst.length < necessaryNumber) {
            throw new CalculationException("Set at least " + necessaryNumber
                + " pairs of points");
        }

        for (int i = 0; i < ptDst.length; i++) {
            if ((ptDst[i].getDimension() != dim)
                    || (ptSrc[i].getDimension() != dim)) {
                throw new CalculationException(
                    "All points must have a dimension equal to " + dim + ".");
            }

            try {
                if (!ptDst[i].getCoordinateReferenceSystem()
                                 .equals(ptSrc[i].getCoordinateReferenceSystem())
                        && !ptDst[i].getCoordinateReferenceSystem().equals(CRS)) {
                    throw new CRSException("All points must have same CRS.");
                }
            } catch (NullPointerException e) {
                if (CRS != null) {
                    throw new CRSException("All points must have same CRS.");
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
     * @throws TransformException
     */
    public double getStandardDeviation() throws TransformException {
        double[] points = new double[ptSrc.length * ptSrc[0].getDimension()];
        double[] pointsDst = new double[ptSrc.length * ptSrc[0].getDimension()];
        double[] ptCalculated = new double[ptSrc.length * ptSrc[0].getDimension()];

        for (int i = 0; i < ptSrc.length; i++) {
            for (int j = 0; j < ptSrc[i].getDimension(); j++) {
                points[(i * ptSrc[0].getDimension()) + j] = ptSrc[i]
                    .getCoordinates()[j];
                pointsDst[(i * ptSrc[0].getDimension()) + j] = ptDst[i]
                    .getCoordinates()[j];
            }
        }

        getMathTransform().transform(points, 0, ptCalculated, 0, ptSrc.length);

        // calculation of the StandardDeviation
        double sum = 0;

        for (int i = 0; i < pointsDst.length; i++) {
            sum = sum
                + ((pointsDst[i] - ptCalculated[i]) * (pointsDst[i]
                - ptCalculated[i]));
        }

        double m = Math.sqrt(sum / (pointsDst.length / ptSrc[0].getDimension()));

        return m;
    }

    /**
     * Returns calculated MathTransform.
     *
     * @return MathTransform
     *
     * @throws TransformException
     */
    abstract public MathTransform getMathTransform() throws TransformException;
}
