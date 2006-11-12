/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
 */
package org.geotools.referencing.operation.matrix;

// J2SE dependencies
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// Geotools dependencies
import org.geotools.resources.XMath;


/**
 * Utility methods for affine transforms. This class provides two kind of services:
 *
 * <ul>
 *   <li><p>A set of public static methods working on any {@link AffineTransform}.</p></li>
 *   <li><p>An abstract base class that override all mutable {@link AffineTransform} methods
 *       in order to check for permission before changing the transform's state.
 *       If {@link #checkPermission} is defined to always throw an exception,
 *       then {@code XAffineTransform} is immutable.</p></li>
 * </ul>
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class XAffineTransform extends AffineTransform {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5215291166450556451L;

    /**
     * Tolerance value for floating point comparisons.
     */
    private static final double EPS = 1E-6;

    /**
     * Constructs a new {@code XAffineTransform} that is a
     * copy of the specified {@code AffineTransform} object.
     */
    protected XAffineTransform(final AffineTransform tr) {
        super(tr);
    }

    /**
     * Check if the caller is allowed to change this {@code XAffineTransform} state.
     * If this method is defined to thrown an exception in all case, then this
     * {@code XAffineTransform} is immutable.
     */
    protected abstract void checkPermission();

    /**
     * Check for {@linkplain #checkPermission permission} before translating this transform.
     */
    public void translate(double tx, double ty) {
        checkPermission();
        super.translate(tx, ty);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before rotating this transform.
     */
    public void rotate(double theta) {
        checkPermission();
        super.rotate(theta);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before rotating this transform.
     */
    public void rotate(double theta, double x, double y) {
        checkPermission();
        super.rotate(theta, x, y);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before scaling this transform.
     */
    public void scale(double sx, double sy) {
        checkPermission();
        super.scale(sx, sy);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before shearing this transform.
     */
    public void shear(double shx, double shy) {
        checkPermission();
        super.shear(shx, shy);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before setting this transform.
     */
    public void setToIdentity() {
        checkPermission();
        super.setToIdentity();
    }

    /**
     * Check for {@linkplain #checkPermission permission} before setting this transform.
     */
    public void setToTranslation(double tx, double ty) {
        checkPermission();
        super.setToTranslation(tx, ty);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before setting this transform.
     */
    public void setToRotation(double theta) {
        checkPermission();
        super.setToRotation(theta);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before setting this transform.
     */
    public void setToRotation(double theta, double x, double y) {
        checkPermission();
        super.setToRotation(theta, x, y);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before setting this transform.
     */
    public void setToScale(double sx, double sy) {
        checkPermission();
        super.setToScale(sx, sy);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before setting this transform.
     */
    public void setToShear(double shx, double shy) {
        checkPermission();
        super.setToShear(shx, shy);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before setting this transform.
     */
    public void setTransform(AffineTransform Tx) {
        checkPermission();
        super.setTransform(Tx);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before setting this transform.
     */
    public void setTransform(double m00, double m10,
                             double m01, double m11,
                             double m02, double m12) {
        checkPermission();
        super.setTransform(m00, m10, m01, m11, m02, m12);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before concatenating this transform.
     */
    public void concatenate(AffineTransform Tx) {
        checkPermission();
        super.concatenate(Tx);
    }

    /**
     * Check for {@linkplain #checkPermission permission} before concatenating this transform.
     */
    public void preConcatenate(AffineTransform Tx) {
        checkPermission();
        super.preConcatenate(Tx);
    }

    /**
     * Returns a rectangle which entirely contains the direct
     * transform of {@code bounds}. This operation is equivalent to:
     *
     * <blockquote><code>
     * {@linkplain #createTransformedShape createTransformedShape}(bounds).{@linkplain
     * Rectangle2D#getBounds2D() getBounds2D()}
     * </code></blockquote>
     *
     * @param transform Affine transform to use.
     * @param bounds    Rectangle to transform. This rectangle will not be modified.
     * @param dest      Rectangle in which to place the result.  If null, a new
     *                  rectangle will be created.
     *
     * @return The direct transform of the {@code bounds} rectangle.
     */
    public static Rectangle2D transform(final AffineTransform transform,
                                        final Rectangle2D     bounds,
                                        final Rectangle2D     dest)
    {
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        final Point2D.Double point = new Point2D.Double();
        for (int i=0; i<4; i++) {
            point.x = (i&1)==0 ? bounds.getMinX() : bounds.getMaxX();
            point.y = (i&2)==0 ? bounds.getMinY() : bounds.getMaxY();
            transform.transform(point, point);
            if (point.x < xmin) xmin = point.x;
            if (point.x > xmax) xmax = point.x;
            if (point.y < ymin) ymin = point.y;
            if (point.y > ymax) ymax = point.y;
        }
        if (dest != null) {
            dest.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
            return dest;
        }
        return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
    }

    /**
     * Returns a rectangle which entirely contains the inverse
     * transform of {@code bounds}. This operation is equivalent to:
     *
     * <blockquote><code>
     * {@linkplain #createInverse() createInverse()}.{@linkplain
     * #createTransformedShape createTransformedShape}(bounds).{@linkplain
     * Rectangle2D#getBounds2D() getBounds2D()}
     * </code></blockquote>
     *
     * @param transform Affine transform to use.
     * @param bounds    Rectangle to transform. This rectangle will not be modified.
     * @param dest      Rectangle in which to place the result.  If null, a new
     *                  rectangle will be created.
     *
     * @return The inverse transform of the {@code bounds} rectangle.
     * @throws NoninvertibleTransformException if the affine transform can't be inverted.
     */
    public static Rectangle2D inverseTransform(final AffineTransform transform,
                                               final Rectangle2D     bounds,
                                               final Rectangle2D     dest)
            throws NoninvertibleTransformException
    {
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        final Point2D.Double point = new Point2D.Double();
        for (int i=0; i<4; i++) {
            point.x = (i&1)==0 ? bounds.getMinX() : bounds.getMaxX();
            point.y = (i&2)==0 ? bounds.getMinY() : bounds.getMaxY();
            transform.inverseTransform(point, point);
            if (point.x < xmin) xmin = point.x;
            if (point.x > xmax) xmax = point.x;
            if (point.y < ymin) ymin = point.y;
            if (point.y > ymax) ymax = point.y;
        }
        if (dest != null) {
            dest.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
            return dest;
        }
        return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
    }

    /**
     * Calculates the inverse affine transform of a point without without
     * applying the translation components.
     *
     * @param transform Affine transform to use.
     * @param source    Point to transform. This rectangle will not be modified.
     * @param dest      Point in which to place the result.  If {@code null}, a
     *                  new point will be created.
     *
     * @return The inverse transform of the {@code source} point.
     * @throws NoninvertibleTransformException if the affine transform can't be inverted.
     */
    public static Point2D inverseDeltaTransform(final AffineTransform transform,
                                                final Point2D         source,
                                                final Point2D         dest)
            throws NoninvertibleTransformException
    {
        final double m00 = transform.getScaleX();
        final double m11 = transform.getScaleY();
        final double m01 = transform.getShearX();
        final double m10 = transform.getShearY();
        final double det = m00*m11 - m01*m10;
        if (!(Math.abs(det) > Double.MIN_VALUE)) {
            return transform.createInverse().deltaTransform(source, dest);
        }
        final double x0 = source.getX();
        final double y0 = source.getY();
        final double x = (x0*m11 - y0*m01) / det;
        final double y = (y0*m00 - x0*m10) / det;
        if (dest != null) {
            dest.setLocation(x, y);
            return dest;
        }
        return new Point2D.Double(x, y);
    }

    /**
     * Returns an estimation about whatever the specified transform swaps <var>x</var>
     * and <var>y</var> axis. This method assumes that the specified affine transform
     * is built from arbitrary translations, scales or rotations, but no shear. It
     * returns {@code +1} if the (<var>x</var>, <var>y</var>) axis order seems to be
     * preserved, {@code -1} if the transform seems to swap axis to the (<var>y</var>,
     * <var>x</var>) axis order, or {@code 0} if this method can not make a decision.
     */
    public static int getSwapXY(final AffineTransform tr) {
        final int flip = getFlip(tr);
        if (flip != 0) {
            final double scaleX = getScaleX0(tr);
            final double scaleY = getScaleY0(tr) * flip;
            final double y = Math.abs(tr.getShearY()/scaleY - tr.getShearX()/scaleX);
            final double x = Math.abs(tr.getScaleY()/scaleY + tr.getScaleX()/scaleX);
            if (x > y) return +1;
            if (x < y) return -1;
            // At this point, we may have (x == y) or some NaN value.
        }
        return 0;
    }

    /**
     * Returns an estimation of the rotation angle in radians. This method assumes that the
     * specified affine transform is built from arbitrary translations, scales or rotations,
     * but no shear. If a flip has been applied, then this method assumes that the flipped
     * axis is the <var>y</var> one in <cite>source CRS</cite> space. For a <cite>grid to
     * world CRS</cite> transform, this is the row number in grid coordinates.
     *
     * @param  tr The affine transform to inspect.
     * @return An estimation of the rotation angle in radians, or {@link Double#NaN NaN}
     *         if the angle can not be estimated.
     */
    public static double getRotation(final AffineTransform tr) {
        final int flip = getFlip(tr);
        if (flip != 0) {
            final double scaleX = getScaleX0(tr);
            final double scaleY = getScaleY0(tr) * flip;
            return Math.atan2(tr.getShearY()/scaleY - tr.getShearX()/scaleX,
                              tr.getScaleY()/scaleY + tr.getScaleX()/scaleX);
        }
        return Double.NaN;
    }

    /**
     * Returns +1 if one axis has been flipped, -1 if none axis has been flipped, or 0 if
     * unknown. A flipped axis in an axis with direction reversed (typically the <var>y</var>
     * axis). This method assumes that the specified affine transform is built from arbitrary
     * translations, scales or rotations, but no shear. Note that it is not possible to
     * determine which of the <var>x</var> or <var>y</var> axis has been flipped.
     * <p>
     * This method is similar to the following code, except that this method
     * distinguish between "unflipped" and "unknow" states.
     *
     * <blockquote><code>
     * boolean flipped = (tr.{@linkplain #getType() getType()} & {@linkplain #TYPE_FLIP}) != 0;
     * </code></blockquote>
     */
    public static int getFlip(final AffineTransform tr) {
        final int scaleX = XMath.sgn(tr.getScaleX());
        final int scaleY = XMath.sgn(tr.getScaleY());
        final int shearX = XMath.sgn(tr.getShearX());
        final int shearY = XMath.sgn(tr.getShearY());
        if (scaleX ==  scaleY && shearX == -shearY) return +1;
        if (scaleX == -scaleY && shearX ==  shearY) return -1;
        return 0;
    }

    /**
     * Returns the magnitude of scale factor <var>x</var> by cancelling the
     * effect of eventual flip and rotation. This factor is calculated by
     * <IMG src="{@docRoot}/org/geotools/display/canvas/doc-files/scaleX0.png">.
     */
    public static double getScaleX0(final AffineTransform tr) {
        return XMath.hypot(tr.getScaleX(), tr.getShearX());
    }

    /**
     * Returns the magnitude of scale factor <var>y</var> by cancelling the
     * effect of eventual flip and rotation. This factor is calculated by
     * <IMG src="{@docRoot}/org/geotools/display/canvas/doc-files/scaleY0.png">.
     */
    public static double getScaleY0(final AffineTransform tr) {
        return XMath.hypot(tr.getScaleY(), tr.getShearY());
    }

    /**
     * Returns a global scale factor for the specified affine transform.
     * This scale factor will combines {@link #getScaleX0} and {@link #getScaleY0}.
     * The way to compute such a "global" scale is somewhat arbitrary and may change
     * in a future version.
     */
    public static double getScale(final AffineTransform tr) {
        return 0.5 * (getScaleX0(tr) + getScaleY0(tr));
    }

    /**
     * Returns an affine transform representing a zoom carried out around a
     * central point (<var>x</var>, <var>y</var>). The transforms will leave
     * the specified (<var>x</var>, <var>y</var>) coordinate unchanged.
     *
     * @param sx Scale along <var>x</var> axis.
     * @param sy Scale along <var>y</var> axis.
     * @param  x <var>x</var> coordinates of the central point.
     * @param  y <var>y</var> coordinates of the central point.
     * @return   Affine transform of a zoom which leaves the
     *          (<var>x</var>,<var>y</var>) coordinate unchanged.
     */
    public static AffineTransform getScaleInstance(final double sx, final double sy,
                                                   final double  x, final double  y) {
        return new AffineTransform(sx, 0, 0, sy, (1-sx)*x, (1-sy)*y);
    }

    /**
     * Checks whether the matrix coefficients are close to whole numbers.
     * If this is the case, these coefficients will be rounded up to the
     * nearest whole numbers. This rounding up is useful, for example, for
     * speeding up image displays.  Above all, it is efficient when we know that
     * a matrix has a chance of being close to the similarity matrix.
     */
    public static void round(final AffineTransform tr) {
        double r;
        final double m00, m01, m10, m11;
        if (Math.abs((m00 = Math.rint(r = tr.getScaleX())) - r) <= EPS &&
            Math.abs((m01 = Math.rint(r = tr.getShearX())) - r) <= EPS &&
            Math.abs((m11 = Math.rint(r = tr.getScaleY())) - r) <= EPS &&
            Math.abs((m10 = Math.rint(r = tr.getShearY())) - r) <= EPS)
        {
            if ((m00!=0 || m01!=0) && (m10!=0 || m11!=0)) {
                double m02=Math.rint(r=tr.getTranslateX()); if (!(Math.abs(m02-r)<=EPS)) m02=r;
                double m12=Math.rint(r=tr.getTranslateY()); if (!(Math.abs(m12-r)<=EPS)) m12=r;
                tr.setTransform(m00, m10, m01, m11, m02, m12);
            }
        }
    }
}
