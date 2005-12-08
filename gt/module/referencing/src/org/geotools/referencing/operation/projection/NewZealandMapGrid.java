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
package org.geotools.referencing.operation.projection;

// J2SE dependencies
import java.util.Collection;
import java.io.Serializable;
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.operation.transform.AbstractMathTransform;


/**
 * Implementation of the NZMG (New Zealand Map Grid) projection.
 * 
 * <p>
 * This is an implementation of algorithm published by  <a
 * href="http://www.govt.nz/record?recordid=28">Land Information New
 * Zealand.</a> The algorithm is documented <a
 * href="http://www.linz.govt.nz/rcs/linz/6137/">here.</a>
 * </p>
 *
 * @since 2.2
 * @version $Id$
 * @author Justin Deoliveira
 * @author Martin Desruisseaux
 */
public class NewZealandMapGrid extends MapProjection {
    /**
     * For compatibility with different versions during deserialization.
     */
	private static final long serialVersionUID = 8394817836243729133L;

    /**
     * Coefficients for forward and inverse projection.
     */
	private static final double[][] A = {
            {  0.7557853228,  0.0         },
            {  0.249204646,   0.003371507 },
            { -0.001541739,   0.041058560 },
            { -0.10162907,    0.01727609  },
            { -0.26623489,   -0.36249218  },
            { -0.6870983,    -1.1651967   }
        };

    /**
     * Coefficients for inverse projection.
     */
    private static final double[][] B = {
            {  1.3231270439,   0.0         },
            { -0.577245789,   -0.007809598 },
            {  0.508307513,   -0.112208952 },
            { -0.15094762,     0.18200602  },
            {  1.01418179,     1.64497696  },
            {  1.9660549,      2.5127645   }
        };

    /**
     * Coefficients for inverse projection.
     */
    private static final double[] tphi = new double[] {
            1.5627014243, .5185406398, -.03333098, -.1052906, -.0368594, .007317,
            .01220, .00394, -.0013
        };

    /**
     * Coefficients for forward projection.
     */
    private static final double[] tpsi = new double[] {
            .6399175073, -.1358797613, .063294409, -.02526853, .0117879,
            -.0055161, .0026906, -.001333, .00067, -.00034
        };

    /**
     * Constructs a new map projection with default parameter values.
     */
    protected NewZealandMapGrid() {
        this((ParameterValueGroup) Provider.PARAMETERS.createValue());
    }

    /**
     * Constructs a new map projection from the supplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected NewZealandMapGrid(final ParameterValueGroup parameters)
            throws ParameterNotFoundException
    {
        super(parameters, Provider.PARAMETERS.descriptors());
    }

    /**
     * {@inheritDoc}
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }

    /**
     * Must be overrided because {@link Provider} uses instances of
     * {@link ModifiedParameterDescriptor}. This hack was needed because the New Zeland map
     * projection uses particular default values for parameters like "False Easting", etc.
     */
    final boolean isExpectedParameter(final Collection expected, final ParameterDescriptor param) {
        return ModifiedParameterDescriptor.contains(expected, param);
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in {@code ptDst} (linear distance on a unit sphere).
     */
    protected Point2D transformNormalized(final double x, final double y, final Point2D ptDst)
            throws ProjectionException
    {
        final double dphi = (y - latitudeOfOrigin) * (180/Math.PI * 3600E-5);
        double dphi_pow_i = dphi;
        double dpsi       = 0;
        for (int i=0; i<10; i++) {
            dpsi += (tpsi[i] * dphi_pow_i);
            dphi_pow_i *= dphi;
        }

        final double[] theta = new double[] { dpsi, x };
        double[] z = multiply(A[0], theta);
        for (int i = 2; i <= 6; i++) {
            z = add(z, multiply(A[i - 1], power(theta, i)));
        }

        if (ptDst != null) {
            ptDst.setLocation(z[1], z[0]);
            return ptDst;
        }
        return new Point2D.Double(z[1], z[0]);
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
     * and stores the result in {@code ptDst}.
     */
    protected Point2D inverseTransformNormalized(final double x, final double y, final Point2D ptDst)
            throws ProjectionException
    {
        final double[] z = new double[] { y, x };
        double[] theta = multiply(B[0], z);

        for (int j = 2; j <= 6; j++) {
            theta = add(theta, multiply(B[j - 1], power(z, j)));
        }

        // increasing the number of iterations through this loop decreases
        // the error in the calculation, but 3 iterations gives 10-3 accuracy
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

        dphi = dphi / (180/Math.PI * 3600E-5) + latitudeOfOrigin;
        if (ptDst != null) {
            ptDst.setLocation(theta[1], dphi);
            return ptDst;
        }
        return new Point2D.Double(theta[1], dphi);
    }

    /**
     * Multplies two complex numbers.
     *
     * @param c1 DOCUMENT ME!
     * @param c2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static double[] multiply(final double[] c1, final double[] c2) {
        final double r = (c1[0] * c2[0]) - (c1[1] * c2[1]);
        final double i = (c1[1] * c2[0]) + (c1[0] * c2[1]);
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
    protected static double[] multiply(double[] c, double s) {
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
    protected static double[] divide(double[] c1, double[] c2) {
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
    protected static double[] add(double[] c1, double[] c2) {
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
    protected static double[] power(double[] c, int power) {
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

    /**
     * The provider for {@link NewZealandMapGrid}. This provider will construct transforms
     * from {@linkplain org.opengis.referencing.crs.GeographicCRS geographic} to
     * {@linkplain org.opengis.referencing.crs.ProjectedCRS projected} coordinate
     * reference systems.
     *
     * @since 2.2
     * @version $Id$
     * @author Justin Deoliveira
     */
    public static class Provider extends AbstractProvider {
        /**
         * For compatibility with different versions during deserialization.
         */
        private static final long serialVersionUID = -7716733400419275656L;

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                    new NamedIdentifier(Citations.OGC,  "New_Zealand_Map_Grid"),
                    new NamedIdentifier(Citations.EPSG, "New Zealand Map Grid"),
                    new NamedIdentifier(Citations.EPSG, "27200")
                },
                new ParameterDescriptor[] {
                    new ModifiedParameterDescriptor(SEMI_MAJOR,         6378388.0),
                    new ModifiedParameterDescriptor(SEMI_MINOR,         6378388.0),
                    new ModifiedParameterDescriptor(LATITUDE_OF_ORIGIN,     -41.0),
                    new ModifiedParameterDescriptor(CENTRAL_MERIDIAN,       173.0),
                    new ModifiedParameterDescriptor(FALSE_EASTING,      2510000.0),
                    new ModifiedParameterDescriptor(FALSE_NORTHING,     6023150.0)
                });

        /**
         * Constructs a new provider. 
         */
        public Provider() {
            super(PARAMETERS);
        }    

        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param  parameters The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup parameters)
                throws ParameterNotFoundException
        {
            final Collection descriptors = PARAMETERS.descriptors();
            return new NewZealandMapGrid(parameters);
        }
    }
}
