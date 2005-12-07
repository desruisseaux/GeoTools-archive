/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.transform;

import java.io.Serializable;

import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.referencing.operation.projection.MapProjection;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;


/**
 * Implementation of the nzmg (New Zealand Map Grid) projection.
 * 
 * <p>
 * This is an implementation of algorithm published by  <a
 * href="http://www.govt.nz/record?recordid=28">Land Information New
 * Zealand.</a> The algorithm is documented <a
 * href="http://www.linz.govt.nz/rcs/linz/6137/">here.</a>
 * </p>
 */
public class NZMGTransform extends AbstractMathTransform
    implements MathTransform2D, Serializable {
    
	private static final long serialVersionUID = 8394817836243729133L;
	
	public static double[][] A = new double[][] {
            { .7557853228, 0.0 },
            { .249204646, .003371507 },
            { -.001541739, .041058560 },
            { -.10162907, .01727609 },
            { -.26623489, -.36249218 },
            { -.6870983, -1.1651967 }
        };
    public static double[][] B = new double[][] {
            { 1.3231270439, 0.0 },
            { -0.577245789, -0.007809598 },
            { 0.508307513, -0.112208952 },
            { -0.15094762, 0.18200602 },
            { 1.01418179, 1.64497696 },
            { 1.9660549, 2.5127645 }
        };
    public static double[] tphi = new double[] {
            1.5627014243, .5185406398, -.03333098, -.1052906, -.0368594, .007317,
            .01220, .00394, -.0013
        };
    public static double[] tpsi = new double[] {
            .6399175073, -.1358797613, .063294409, -.02526853, .0117879,
            -.0055161, .0026906, -.001333, .00067, -.00034
        };

    /** lattitude of NZMG origin */
    double PHIo = -41d;

    /** longitude of NZMG origin */
    double LAMo = 173.0d;

    /** easting of NZMG origin */
    double Eo = 2510000d;

    /** northing of NZMG origin */
    double No = 6023150d;

    /** semi-major axis of the International spheroid */
    double a = 6378388d;

    public int getSourceDimensions() {
        return 2;
    }

    public int getTargetDimensions() {
        return 2;
    }

    public void setSemiMajorAxis(double value) {
        a = value;
    }

    public void setLatitudeOfOrigin(double value) {
        PHIo = value;
    }

    public void setCentralMeridian(double value) {
        LAMo = value;
    }

    public void setFalseEasting(double value) {
        Eo = value;
    }

    public void setNorthEasting(double value) {
        No = value;
    }

    public void transform(double[] srcPts, int srcOff, final double[] dstPts,
        int dstOff, int numPts) throws TransformException {
        for (int j = 0; j < numPts; j++) {
            double lam = srcPts[srcOff++];
            double phi = srcPts[srcOff++];

            double dphi = (phi - PHIo) * 3600E-5;
            double dpsi = 0;

            for (int i = 1; i <= 10; i++) {
                dpsi += (tpsi[i - 1] * Math.pow(dphi, i));
            }

            double dlam = ((lam - LAMo) * Math.PI) / 180d;

            double[] theta = new double[] { dpsi, dlam };
            double[] z = multiply(A[0], theta);

            for (int i = 2; i <= 6; i++) {
                z = add(z, multiply(A[i - 1], power(theta, i)));
            }

            dstPts[dstOff++] = (z[1] * a) + Eo;
            dstPts[dstOff++] = (z[0] * a) + No;
        }
    }

    public void inverseTransform(double[] srcPts, int srcOff,
        final double[] dstPts, int dstOff, int numPts) {
        for (int i = 0; i < numPts; i++) {
            double e = srcPts[srcOff++];
            double n = srcPts[srcOff++];

            double[] z = new double[] { (n - No) / a, (e - Eo) / a };
            double[] theta = multiply(B[0], z);

            for (int j = 2; j <= 6; j++) {
                theta = add(theta, multiply(B[j - 1], power(z, j)));
            }

            //increasing the number of iterations through this loop decreases
            //the error in the calculation, but 3 iterations gives 10-3 accuracy
            for (int j = 0; j < 2; j++) {
                double[] num = multiply(A[1], power(theta, 2));

                for (int k = 3; k <= 6; k++) {
                    num = add(num,
                            multiply(multiply(A[k - 1], power(theta, k)), k - 1));
                }

                num = add(z, num);

                double[] denom = multiply(A[0], power(theta, 0));

                for (int k = 2; k <= 6; k++) {
                    denom = add(denom,
                            multiply(multiply(A[k - 1], power(theta, k - 1)), k));
                }

                theta = divide(num, denom);
            }

            double dpsi = theta[0];
            double dphi = tphi[0] * dpsi;

            for (int j = 2; j <= 9; j++) {
                dphi += (tphi[j - 1] * Math.pow(dpsi, j));
            }

            dstPts[dstOff++] = PHIo + ((dphi * Math.pow(10, 5)) / 3600d);
            dstPts[dstOff++] = LAMo + ((theta[1] * 180d) / Math.PI);
        }
    }

    public MathTransform inverse() throws NoninvertibleTransformException {
        return new Inverse();
    }

    /**
     * Multplies two complex numbers.
     *
     * @param c1 DOCUMENT ME!
     * @param c2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected double[] multiply(double[] c1, double[] c2) {
        double r = (c1[0] * c2[0]) - (c1[1] * c2[1]);
        double i = (c1[1] * c2[0]) + (c1[0] * c2[1]);

        return new double[] { r, i };
    }

    /**
     * Multiplies a complex number by a scalar.
     *
     * @param c DOCUMENT ME!
     * @param s DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected double[] multiply(double[] c, double s) {
        return new double[] { c[0] * s, c[1] * s };
    }

    /**
     * Divides one complex number by another.
     *
     * @param c1 DOCUMENT ME!
     * @param c2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected double[] divide(double[] c1, double[] c2) {
        double denom = (c2[0] * c2[0]) + (c2[1] * c2[1]);
        double r = (c1[0] * c2[0]) + (c1[1] * c2[1]);
        double i = (c1[1] * c2[0]) - (c1[0] * c2[1]);

        return new double[] { r / denom, i / denom };
    }

    /**
     * Adds to complex numbers.
     *
     * @param c1 DOCUMENT ME!
     * @param c2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected double[] add(double[] c1, double[] c2) {
        double r = c1[0] + c2[0];
        double i = c1[1] + c2[1];

        return new double[] { r, i };
    }

    /**
     * Computes the integer power of a complex number up to 6.
     *
     * @param c DOCUMENT ME!
     * @param power DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    protected double[] power(double[] c, int power) {
        double x = c[0];
        double y = c[1];

        double r = Double.NaN;
        double i = Double.NaN;

        switch (power) {
        case 0:
            r = 1;
            i = 0;

            break;

        case 1:
            r = x;
            i = y;

            break;

        case 2:
            r = (x * x) - (y * y);
            i = 2 * x * y;

            break;

        case 3:
            r = (x * x * x) - (3 * x * y * y);
            i = (3 * x * x * y) - (y * y * y);

            break;

        case 4:
            r = (x * x * x * x) - (6 * x * x * y * y) + (y * y * y * y);
            i = (4 * x * x * x * y) - (4 * x * y * y * y);

            break;

        case 5:
            r = (x * x * x * x * x) - (10 * x * x * x * y * y)
                + (5 * x * y * y * y * y);
            i = (5 * x * x * x * x * y) - (10 * x * x * y * y * y)
                + (y * y * y * y * y);

            break;

        case 6:
            r = ((x * x * x * x * x * x) - (15 * x * x * x * x * y * y)
                + (15 * x * x * y * y * y * y)) - (y * y * y * y * y * y);
            i = (6 * x * x * x * x * x * y) - (20 * x * x * x * y * y * y)
                + (6 * x * y * y * y * y * y);

            break;

        default:
            throw new IllegalArgumentException();
        }

        return new double[] { r, i };
    }

    class Inverse extends AbstractMathTransform.Inverse implements Serializable {
        
    	private static final long serialVersionUID = 3815348650444311417L;

		public Inverse() {
            NZMGTransform.this.super();
        }

        public void transform(double[] src, int srcOff, double[] dst,
            int dstOff, int n) throws TransformException {
            NZMGTransform.this.inverseTransform(src, srcOff, dst, dstOff, n);
        }
    }

    /**
     * The provider for {@link NZMGTransform}. This provider will construct
     * transforms from {@linkPlain
     * org.geotools.referencing.crs.DefaultGeographicCRS geographic} to
     * {@linkPlain org.geotools.referencing.crs.DefaultGeographicCRS
     * geographic} coordinate reference systems.
     */
    public static class Provider extends MathTransformProvider {
    	
        private static final long serialVersionUID = -7716733400419275656L;
        
		public static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                    new NamedIdentifier(Citations.OGC, "New_Zealand_Map_Grid"),
                    new NamedIdentifier(Citations.EPSG,
                        "New Zealand Map Grid"),
                    new NamedIdentifier(Citations.EPSG, "27200")
                },
                new ParameterDescriptor[] {
                    MapProjection.AbstractProvider.SEMI_MAJOR,
                    MapProjection.AbstractProvider.SEMI_MINOR,
                    MapProjection.AbstractProvider.LATITUDE_OF_ORIGIN,
                    MapProjection.AbstractProvider.CENTRAL_MERIDIAN,
                    MapProjection.AbstractProvider.FALSE_EASTING,
                    MapProjection.AbstractProvider.FALSE_NORTHING
                });

        public Provider() {
            super(2, 2, PARAMETERS);
        }

        protected MathTransform createMathTransform(ParameterValueGroup values)
            throws InvalidParameterNameException, ParameterNotFoundException, 
                InvalidParameterValueException, FactoryException {
            NZMGTransform transform = new NZMGTransform();
            transform.setLatitudeOfOrigin(doubleValue(
                    MapProjection.AbstractProvider.LATITUDE_OF_ORIGIN, values));
            transform.setCentralMeridian(doubleValue(
                    MapProjection.AbstractProvider.CENTRAL_MERIDIAN, values));
            transform.setFalseEasting(doubleValue(
                    MapProjection.AbstractProvider.FALSE_EASTING, values));
            transform.setNorthEasting(doubleValue(
                    MapProjection.AbstractProvider.FALSE_NORTHING, values));
            transform.setSemiMajorAxis(doubleValue(
                    MapProjection.AbstractProvider.SEMI_MAJOR, values));

            return transform;
        }
    }
}
