/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *   
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.referencing.operation.transform;

// J2SE dependencies
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.prefs.Preferences;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.matrix.Matrix2;
import org.geotools.referencing.operation.matrix.Matrix3;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.referencing.wkt.Symbols;
import org.geotools.resources.Formattable;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.geometry.XAffineTransform;


/**
 * Transforms two-dimensional coordinate points using an {@link AffineTransform}.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class AffineTransform2D extends XAffineTransform
                           implements MathTransform2D, LinearTransform, Formattable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5299837898367149069L;
    
    /**
     * The inverse transform. This field
     * will be computed only when needed.
     */
    private transient AffineTransform2D inverse;
    
    /**
     * Constructs an affine transform.
     */
    protected AffineTransform2D(final AffineTransform transform) {
        super(transform);
    }
    
    /**
     * Throws an {@link UnsupportedOperationException} when a mutable method
     * is invoked, since {@code AffineTransform2D} must be immutable.
     */
    protected void checkPermission() {
        throw new UnsupportedOperationException(
                  Errors.format(ErrorKeys.UNMODIFIABLE_AFFINE_TRANSFORM));
    }

    /**
     * Returns the matrix elements as a group of parameters values. The number of parameters
     * depends on the matrix size. Only matrix elements different from their default value
     * will be included in this group.
     *
     * @return A copy of the parameter values for this math transform.
     */
    public ParameterValueGroup getParameterValues() {
        return ProjectiveTransform.getParameterValues(getMatrix());
    }

    /**
     * Gets the dimension of input points.
     *
     * @deprecated Renamed {@link #getSourceDimensions} for consistency with
     *             {@link org.opengis.referencing.operation.OperationMethod}.
     */
    public final int getDimSource() {
        return getSourceDimensions();
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getSourceDimensions() {
        return 2;
    }
    
    /**
     * Gets the dimension of output points.
     *
     * @deprecated Renamed {@link #getTargetDimensions} for consistency with
     *             {@link org.opengis.referencing.operation.OperationMethod}.
     */
    public final int getDimTarget() {
        return getTargetDimensions();
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getTargetDimensions() {
        return 2;
    }
    
    /**
     * Transforms the specified {@code ptSrc} and stores the result in {@code ptDst}.
     */
    public DirectPosition transform(final DirectPosition ptSrc, DirectPosition ptDst) {
        if (ptDst == null) {
            ptDst = new GeneralDirectPosition(2);
        } else {
            final int dimension = ptDst.getDimension();
            if (dimension != 2) {
                throw new MismatchedDimensionException(Errors.format(
                          ErrorKeys.MISMATCHED_DIMENSION_$3,
                          "ptDst", new Integer(dimension), new Integer(2)));
            }
        }
        final double[] array = ptSrc.getCoordinates();
        transform(array, 0, array, 0, 1);
        ptDst.setOrdinate(0, array[0]);
        ptDst.setOrdinate(1, array[1]);
        return ptDst;
    }
    
    /**
     * Returns this transform as an affine transform matrix.
     */
    public Matrix getMatrix() {
        return new Matrix3(this);
    }
    
    /**
     * Gets the derivative of this transform at a point. For an affine transform,
     * the derivative is the same everywhere.
     */
    public Matrix derivative(final Point2D point) {
        return new Matrix2(getScaleX(), getShearX(),
                           getShearY(), getScaleY());
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For an affine transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final DirectPosition point) {
        return derivative((Point2D) null);
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        if (inverse == null) {
            if (isIdentity()) {
                inverse = this;
            } else try {
                synchronized (this) {
                    inverse = new AffineTransform2D(createInverse());
                    inverse.inverse = this;
                }
            } catch (java.awt.geom.NoninvertibleTransformException exception) {
                throw new NoninvertibleTransformException(exception.getLocalizedMessage(), exception);
            }
        }
        return inverse;
    }

    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name.
     */
    public String formatWKT(final Formatter formatter) {
        final ParameterValueGroup parameters = getParameterValues();
        formatter.append(formatter.getName(parameters.getDescriptor()));
        formatter.append(parameters);
        return "PARAM_MT";
    }

    /**
     * Returns the WKT for this transform.
     */
    public String toWKT() {
        int indentation = 2;
        try {
            indentation = Preferences.userNodeForPackage(Formattable.class)
                                     .getInt("Indentation", indentation);
        } catch (SecurityException ignore) {
            // Ignore. Will fallback on the default indentation.
        }
        final Formatter formatter = new Formatter(Symbols.DEFAULT, indentation);
        formatter.append(this);
        return formatter.toString();
    }

    /**
     * Returns the WKT representation of this transform.
     */
    public String toString() {
        return toWKT();
    }
}
