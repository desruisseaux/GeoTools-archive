/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005 Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.referencing.operation.transform;

// J2SE dependencies
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A math transform which delegates part of its work to an other math transform. This is used
 * as a starting point for subclass wanting to modifies only some aspect of an existing math
 * transform, or to attach additional informations to it. The default implementation delegates
 * all method calls to the {@linkplain #transform underlying transform}. Subclasses typically
 * override some of those methods.
 * <p>
 * This class is serializable if the {@linkplain #transform underlying transform} is serializable
 * too.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MathTransformProxy implements MathTransform, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8844242705205498128L;

    /**
     * The math transform on which to delegate the work.
     */
    public final MathTransform transform;
    
    /**
     * Creates a new proxy which delegates its work to the specified math transform.
     */
    protected MathTransformProxy(final MathTransform transform) {
        this.transform = transform;
    }

    /**
     * Gets the dimension of input points.
     */
    public int getSourceDimensions() {
        return transform.getTargetDimensions();
    }

    /**
     * Gets the dimension of input points.
     *
     * @deprecated Renamed {@link #getSourceDimensions}.
     */
    public final int getDimSource() {
        return getSourceDimensions();
    }

    /**
     * Gets the dimension of output points.
     */
    public int getTargetDimensions() {
        return transform.getSourceDimensions();
    }
    
    /**
     * Gets the dimension of output points.
     *
     * @deprecated Renamed {@link #getTargetDimensions}.
     */
    public final int getDimTarget() {
        return getTargetDimensions();
    }
    
    /**
     * Transforms the specified {@code ptSrc} and stores the result in {@code ptDst}.
     */
    public DirectPosition transform(final DirectPosition ptSrc, final DirectPosition ptDst)
            throws MismatchedDimensionException, TransformException
    {
        return transform.transform(ptSrc, ptDst);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, final int srcOff,
                          final double[] dstPts, final int dstOff,
                          final int numPts) throws TransformException
    {
        transform.transform(srcPts, srcOff, dstPts, dstOff, numPts);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, final int srcOff,
                          final float[] dstPts, final int dstOff,
                          final int numPts) throws TransformException
    {
        transform.transform(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    /**
     * Gets the derivative of this transform at a point.
     */
    public Matrix derivative(final DirectPosition point) throws TransformException {
        return transform.derivative(point);
    }

    /**
     * Returns the inverse of this math transform.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        return transform.inverse();
    }

    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        return transform.isIdentity();
    }

    /**
     * Returns a <cite>Well Known Text</cite> (WKT) for this transform.
     */
    public String toWKT() throws UnsupportedOperationException {
        return transform.toWKT();
    }

    /**
     * Returns a string representation for this transform.
     */
    public String toString() {
        return transform.toString();
    }

    /**
     * Compares the specified object with this inverse math transform for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final MathTransformProxy that = (MathTransformProxy) object;
            return Utilities.equals(this.transform, that.transform);
        }
        return false;
    }

    /**
     * Returns a hash code value for this math transform.
     */
    public int hashCode() {
        return transform.hashCode() ^ (int)serialVersionUID;
    }
}
