/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
package org.geotools.referencing.operation.transform;

// J2SE dependencies and extensions
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import javax.units.Unit;

// JAI dependencies
import javax.media.jai.Warp;
import javax.media.jai.WarpPolynomial;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.Transformation;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.referencing.Identifier;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.metadata.citation.Citation;


/**
 * Wraps an arbitrary {@link Warp} object as a {@linkplain MathTransform2D two-dimensional transform}.
 * Calls to {@linkplain #transform(float[],int,float[],int,int) transform} methods are forwarded to
 * the {@link Warp#warpPoint(int,int,float[]) Warp.warpPoint} method. This implies that source
 * coordinates may be rounded to nearest integers before to the transformation is applied.
 * <p>
 * This transform is typically used with {@linkplain org.geotools.coverage.operation.Resampler2D
 * grid coverage "Resample" operation} for reprojecting an image. Source and destination coordinates
 * are usually pixel coordinates in source and target image, which is why this transform may use
 * integer arithmetic.
 * <p>
 * This math transform can be created alone (by invoking its public constructors directly), or it
 * can be created by a factory like {@link LocalizationGrid}.
 *
 * @version $Id$
 * @author Alessio Fabiani
 * @author Martin Desruisseaux
 *
 * @see LocalizationGrid#getMathTransform(int)
 * @see Warp
 * @see javax.media.jai.WarpOpImage
 * @see javax.media.jai.operator.WarpDescriptor
 */
public class WarpTransform2D extends AbstractMathTransform implements MathTransform2D, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7949539694656719923L;

    /**
     * The maximal polynomial degree allowed.
     */
    static final int MAX_DEGREE = 7;

    /**
     * The warp object. Transformations will be applied using the
     * {@link Warp#warpPoint(int,int,float[]) warpPoint} method or something equivalent.
     */
    private final Warp warp;

    /**
     * The inverse math transform.
     */
    private final WarpTransform2D inverse;

    /**
     * Constructs a warp transform that approximatively maps the given source coordinates to the
     * given destination coordinates. The transformation is performed using some polynomial warp
     * with the degree supplied in argument.
     *
     * @param srcBounds Bounding box of source coordinates, or {@code null} if unknow.
     * @param srcCoords Source coordinates.
     * @param srcOffset The inital entry of {@code srcCoords} to be used.
     * @param dstBounds Bounding box of destination coordinates, or {@code null} if unknow.
     * @param dstCoords Destination coordinates.
     * @param dstOffset The inital entry of {@code destCoords} to be used.
     * @param numCoords The number of coordinates from {@code srcCoords} and {@code destCoords}
     *                  to be used.
     * @param degree    The desired degree of the warp polynomials.
     */
    public WarpTransform2D(final Rectangle2D srcBounds, final Point2D[] srcCoords, final int srcOffset,
                           final Rectangle2D dstBounds, final Point2D[] dstCoords, final int dstOffset,
                           final int numCoords, final int degree)
    {
        this(srcBounds, toFloat(srcCoords, srcOffset, numCoords), 0,
             dstBounds, toFloat(dstCoords, dstOffset, numCoords), 0, numCoords, degree);
    }

    /**
     * Converts an array of points into an array of floats.
     * This is used internally for the above constructor only.
     */
    private static float[] toFloat(final Point2D[] points, int offset, final int numCoords) {
        final float[] array = new float[numCoords * 2];
        for (int i=0; i<array.length;) {
            final Point2D point = points[offset++];
            array[i++] = (float) point.getX();
            array[i++] = (float) point.getY();
        }
        return array;
    }

    /**
     * Constructs a warp transform that approximatively maps the given source coordinates to the
     * given destination coordinates. The transformation is performed using some polynomial warp
     * with the degree supplied in argument.
     *
     * @param srcBounds Bounding box of source coordinates, or {@code null} if unknow.
     * @param srcCoords Source coordinates with <var>x</var> and <var>y</var> alternating.
     * @param srcOffset The inital entry of {@code srcCoords} to be used.
     * @param dstBounds Bounding box of destination coordinates, or {@code null} if unknow.
     * @param dstCoords Destination coordinates with <var>x</var> and <var>y</var> alternating.
     * @param dstOffset The inital entry of {@code destCoords} to be used.
     * @param numCoords The number of coordinates from {@code srcCoords} and {@code destCoords}
     *                  to be used.
     * @param degree    The desired degree of the warp polynomials.
     */
    public WarpTransform2D(final Rectangle2D srcBounds, final float[] srcCoords, final int srcOffset,
                           final Rectangle2D dstBounds, final float[] dstCoords, final int dstOffset,
                           final int numCoords,     final int degree)
    {
        final float preScaleX, preScaleY, postScaleX, postScaleY;
        if (srcBounds != null) {
            preScaleX = (float) srcBounds.getWidth();
            preScaleY = (float) srcBounds.getHeight();
        } else {
            preScaleX = getWidth(srcCoords, srcOffset  , numCoords);
            preScaleY = getWidth(srcCoords, srcOffset+1, numCoords);
        }
        if (dstBounds != null) {
            postScaleX = (float) dstBounds.getWidth();
            postScaleY = (float) dstBounds.getHeight();
        } else {
            postScaleX = getWidth(dstCoords, dstOffset  , numCoords);
            postScaleY = getWidth(dstCoords, dstOffset+1, numCoords);
        }
        /*
         * Note: Warp semantic (transforms coordinates from destination to source) is the
         *       opposite of MathTransform semantic (transforms coordinates from source to
         *       destination). We have to interchange source and destination arrays for the
         *       direct transform.
         */
        warp = WarpPolynomial.createWarp(dstCoords, dstOffset, srcCoords, srcOffset, numCoords,
                                         1/preScaleX, 1/preScaleY, postScaleX, postScaleY, degree);
        inverse = new WarpTransform2D(
               WarpPolynomial.createWarp(srcCoords, srcOffset, dstCoords, dstOffset, numCoords,
                                         1/postScaleX, 1/postScaleY, preScaleX, preScaleY, degree),
               this);
    }

    /**
     * Returns the maximum minus the minimum ordinate int the specifie array.
     * This is used internally for the above constructor only.
     */
    private static float getWidth(final float[] array, int offset, int num) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        while (--num >= 0) {
            float value = array[offset];
            if (value < min) min = value;
            if (value > max) max = value;
            offset += 2;
        }
        return max - min;
    }

    /**
     * Constructs a transform using the specified warp object. Transformations will be applied
     * using the {@link Warp#warpPoint(int,int,float[]) warpPoint} method or something equivalent.
     *
     * @param warp    The image warp to wrap into a math transform.
     * @param inverse An image warp to uses for the {@linkplain #inverse inverse transform},
     *                or {@code null} in none.
     */
    protected WarpTransform2D(final Warp warp, final Warp inverse) {
        ensureNonNull("warp", warp);
        this.warp    = warp;
        this.inverse = (inverse!=null) ? new WarpTransform2D(inverse, this) : null;
    }

    /**
     * Constructs a transform using the specified warp object. This private constructor is used
     * for the construction of {@link #inverse} transform only.
     */
    private WarpTransform2D(final Warp warp, final WarpTransform2D inverse) {
        this.warp    = warp;
        this.inverse = inverse;
    }

    /**
     * Returns a transform using the specified warp object. Transformations will be applied
     * using the {@link Warp#warpPoint(int,int,float[]) warpPoint} method or something equivalent.
     *
     * @param warp The image warp to wrap into a math transform.
     */
    public static MathTransform2D create(final Warp warp) {
        if (warp instanceof WarpAdapter) {
            return ((WarpAdapter) warp).getTransform();
        }
        return new WarpTransform2D(warp, (Warp)null);
    }

    /**
     * Returns a {@linkplain Warp image warp} for the specified transform. The
     * {@link Warp#warpPoint(int,int,float[]) Warp.warpPoint} method transforms coordinates from
     * source to target CRS. Note that JAI's {@linkplain javax.media.jai.operator.WarpDescriptor
     * warp operation} needs a warp object with the opposite semantic (i.e. the image warp must
     * transforms coordinates from target to source CRS). Consequently, consider invoking
     * <code>getWarp(transform.inverse())</code> if the warp object is going to be used in an
     * image reprojection.
     *
     * @param name      The image or {@linkplain org.geotools.coverage.Coverage coverage} name.
     *                  Used only for formatting error message if needed.
     * @param transform The transform to returns as an image warp.
     */
    public static Warp getWarp(final InternationalString name, final MathTransform2D transform) {
        if (transform instanceof WarpTransform2D) {
            return ((WarpTransform2D) transform).getWarp();
        }
        return new WarpAdapter(name, transform);
    }

    /**
     * Returns {@linkplain Warp image warp} wrapped by this transform. The
     * {@link Warp#warpPoint(int,int,float[]) Warp.warpPoint} method transforms coordinates from
     * source to target CRS. Note that JAI's {@linkplain javax.media.jai.operator.WarpDescriptor
     * warp operation} needs a warp object with the opposite semantic (i.e. the image warp must
     * transforms coordinates from target to source CRS). Consequently, consider invoking
     * <code>{@linkplain #inverse}.getWarp()</code> if the warp object is going to be used in an
     * image reprojection.
     */
    public Warp getWarp() {
        return warp;
    }
    
    /**
     * Returns the parameter descriptors for this math transform.
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        if (warp instanceof WarpPolynomial) {
            return Provider.PARAMETERS;
        } else {
            return super.getParameterDescriptors();
        }
    }

    /**
     * Returns the parameter values for this math transform.
     */
    public ParameterValueGroup getParameterValues() {
        if (warp instanceof WarpPolynomial) {
            final ParameterValueGroup parameters = (ParameterValueGroup) getParameterDescriptors().createValue();
            parameters.parameter("degree").setValue(((WarpPolynomial) warp).getDegree());
            return parameters;
        } else {
            return super.getParameterValues();
        }
    }

    /**
     * Returns the dimension of input points.
     */
    public int getSourceDimensions() {
        return 2;
    }

    /**
     * Returns the dimension of output points.
     */
    public int getTargetDimensions() {
        return 2;
    }

    /**
     * Tests if this transform is the identity transform.
     */
    public boolean isIdentity() {
        return false;
    }

    /**
     * Transforms source coordinates (usually pixel indices) into destination coordinates
     * (usually "real world" coordinates).
     *
     * @param ptSrc the specified coordinate point to be transformed.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              {@code ptSrc}, or {@code null}.
     * @return the coordinate point after transforming {@code ptSrc} and storing the result in
     *         {@code ptDst}.
     */
    public Point2D transform(final Point2D ptSrc, final Point2D ptDst) {
        final Point2D result = warp.mapDestPoint(ptSrc);
        if (ptDst == null) {
            return result;
        } else {
            ptDst.setLocation(result);
            return result;
        }
    }

    /**
     * Transforms source coordinates (usually pixel indices) into destination coordinates
     * (usually "real world" coordinates).
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        final int postIncrement;
        if (srcPts == dstPts  &&  srcOff < dstOff) {
            srcOff += (numPts-1)*2;
            dstOff += (numPts-1)*2;
            postIncrement = -4;
        } else {
            postIncrement = 0;
        }
        final float[] dstCoords = new float[2];
        while (--numPts >= 0) {
            final float xi = srcPts[srcOff++];
            final float yi = srcPts[srcOff++];
            warp.warpPoint(Math.round(xi), Math.round(yi), dstCoords);
            dstPts[dstOff++] = dstCoords[0];
            dstPts[dstOff++] = dstCoords[1];
            dstOff += postIncrement;
        }
    }

    /**
     * Transforms source coordinates (usually pixel indices) into destination coordinates
     * (usually "real world" coordinates).
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        final int postIncrement;
        if (srcPts == dstPts  &&  srcOff < dstOff) {
            srcOff += (numPts-1)*2;
            dstOff += (numPts-1)*2;
            postIncrement = -4;
        } else {
            postIncrement = 0;
        }
        final float[] dstCoords = new float[2];
        while (--numPts >= 0) {
            final double xi = srcPts[srcOff++];
            final double yi = srcPts[srcOff++];
            warp.warpPoint((int)Math.round(xi), (int)Math.round(yi), dstCoords);
            dstPts[dstOff++] = dstCoords[0];
            dstPts[dstOff++] = dstCoords[1];
            dstOff += postIncrement;
        }
    }

    /**
     * Returns the inverse transform.
     *
     * @throws NoninvertibleTransformException if no inverse warp were specified at construction time.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        if (inverse != null) {
            return inverse;
        } else {
            return super.inverse();
        }
    }

    /**
     * Returns a hash value for this transform.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode() ^ warp.hashCode();
    }

    /**
     * Compares this transform with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final WarpTransform2D that = (WarpTransform2D) object;
            return Utilities.equals(this.warp, that.warp);
        }
        return false;
    }
    
    /**
     * The provider for the {@link WarpTransform2D}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     *
     * @todo Not yet fully implemented.
     */
    private static class Provider extends MathTransformProvider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -7949539694656719923L;

        /**
         * The operation parameter descriptor for the {@link WarpPolynomial#getDegree degree}
         * parameter value.
         */
        public static final ParameterDescriptor DEGREE = new org.geotools.parameter.ParameterDescriptor(
                "degree", 2, 1, MAX_DEGREE, Unit.ONE);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new Identifier[] {
                new Identifier(Citation.GEOTOOLS, "WarpPolynomial")
            }, new ParameterDescriptor[] {
                DEGREE
            });

        /**
         * Create a provider for warp transforms.
         */
        public Provider() {
            super(2, 2, PARAMETERS);
        }

        /**
         * Returns the operation type.
         */
        protected Class getOperationType() {
            return Transformation.class;
        }

        /**
         * Creates a warp transform from the specified group of parameter values.
         *
         * @param  values The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup values)
                throws ParameterNotFoundException
        {
            throw new UnsupportedOperationException("Not yet implemented.");
        }
    }
}
