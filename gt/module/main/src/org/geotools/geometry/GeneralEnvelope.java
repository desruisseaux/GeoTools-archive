/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.geometry;

// J2SE dependencies
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Arrays;

import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.geometry.XRectangle2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * A minimum bounding box or rectangle. Regardless of dimension, an <code>Envelope</code> can
 * be represented without ambiguity as two direct positions (coordinate points). To encode an
 * <code>Envelope</code>, it is sufficient to encode these two points. This is consistent with
 * all of the data types in this specification, their state is represented by their publicly
 * accessible attributes.
 * <br><br>
 * This particular implementation of <code>Envelope</code> is said "General" because it
 * uses coordinates of an arbitrary dimension.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeneralEnvelope implements Envelope, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1752330560227688940L;
    
    /**
     * Minimum and maximum ordinate values. The first half contains minimum
     * ordinates, while the last half contains maximum ordinates. Consider
     * this reference as final; it is modified by {@link #clone} only.
     */
    private double[] ordinates;

    /**
     * The coordinate reference system, or <code>null</code>.
     */
    private CoordinateReferenceSystem crs;
    
    /**
     * Construct a new envelope with the same data than the specified envelope.
     */
    public GeneralEnvelope(final Envelope envelope) {
        if (envelope instanceof GeneralEnvelope) {
            final GeneralEnvelope e = (GeneralEnvelope) envelope;
            ordinates = (double[]) e.ordinates.clone();
            crs = e.crs;
        } else {
            // TODO: See if we can simplify this code with GeoAPI 1.1
            final DirectPosition lower, upper;
            lower = envelope.getLowerCorner();
            upper = envelope.getUpperCorner();
            crs   = lower.getCoordinateReferenceSystem();
            final int dimension = lower.getDimension();
            if (!Utilities.equals(crs, upper.getCoordinateReferenceSystem()) ||
                dimension != upper.getDimension())
            {
                // TODO: provides a localized message.
                throw new IllegalArgumentException("Malformed envelope");
            }
            ordinates = new double[2*dimension];
            for (int i=0; i<dimension; i++) {
                ordinates[i]           = lower.getOrdinate(i);
                ordinates[i+dimension] = upper.getOrdinate(i);
            }
            checkCoherence();
        }
    }
    
    /**
     * Construct an empty envelope of the specified dimension.
     * All ordinates are initialized to 0.
     */
    public GeneralEnvelope(final int dimension) {
        ordinates = new double[dimension*2];
    }
    
    /**
     * Construct one-dimensional envelope defined by a range of values.
     *
     * @param min The minimal value.
     * @param max The maximal value.
     */
    public GeneralEnvelope(final double min, final double max) {
        ordinates = new double[] {min, max};
        checkCoherence();
    }
    
    /**
     * Construct a envelope defined by two positions.
     *
     * @param  minCP Minimum ordinate values.
     * @param  maxCP Maximum ordinate values.
     * @throws MismatchedDimensionException if the two positions don't have the same dimension.
     * @throws IllegalArgumentException if an ordinate value in the minimum point is not
     *         less than or equal to the corresponding ordinate value in the maximum point.
     */
    public GeneralEnvelope(final double[] minCP, final double[] maxCP)
        throws MismatchedDimensionException
    {
        if (minCP.length != maxCP.length) {
            throw new MismatchedDimensionException(Resources.format(
                                ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                                new Integer(minCP.length), new Integer(maxCP.length)));
        }
        ordinates = new double[minCP.length + maxCP.length];
        System.arraycopy(minCP, 0, ordinates, 0,            minCP.length);
        System.arraycopy(maxCP, 0, ordinates, minCP.length, maxCP.length);
        checkCoherence();
    }
    
    /**
     * Construct a envelope defined by two positions.
     *
     * @param  minCP Point containing minimum ordinate values.
     * @param  maxCP Point containing maximum ordinate values.
     * @throws MismatchedDimensionException if the two positions don't have the same dimension.
     * @throws IllegalArgumentException if an ordinate value in the minimum point is not
     *         less than or equal to the corresponding ordinate value in the maximum point.
     */
    public GeneralEnvelope(final GeneralDirectPosition minCP, final GeneralDirectPosition maxCP)
            throws MismatchedDimensionException
    {
        this(minCP.ordinates, maxCP.ordinates);
    }
    
    /**
     * Construct two-dimensional envelope defined by a {@link Rectangle2D}.
     */
    public GeneralEnvelope(final Rectangle2D rect) {
        ordinates = new double[] {
            rect.getMinX(), rect.getMinY(),
            rect.getMaxX(), rect.getMaxY()
        };
        checkCoherence();
    }
    
    /**
     * Check if ordinate values in the minimum point are less than or
     * equal to the corresponding ordinate value in the maximum point.
     *
     * @throws IllegalArgumentException if an ordinate value in the minimum
     *         point is not less than or equal to the corresponding ordinate
     *         value in the maximum point.
     */
    private void checkCoherence() throws IllegalArgumentException {
        final int dimension = ordinates.length/2;
        for (int i=0; i<dimension; i++) {
            if (!(ordinates[i] <= ordinates[dimension+i])) { // Use '!' in order to catch 'NaN'.
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_ILLEGAL_ENVELOPE_ORDINATE_$1, new Integer(i)));
            }
        }
    }

    /**
     * Returns the coordinate reference system in which the coordinates are given.
     *
     * @return The coordinate reference system, or <code>null</code>.
     */
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Set the coordinate reference system in which the coordinate are given.
     *
     * @param crs The new coordinate reference system, or <code>null</code>.
     */
    public void setCoordinateReferenceSystem(final CoordinateReferenceSystem crs) {
        GeneralDirectPosition.checkCoordinateReferenceSystemDimension(crs, getDimension());
        this.crs = crs;
    }
    
    /**
     * Returns the number of dimensions.
     */
    public final int getDimension() {
        return ordinates.length/2;
    }

    /**
     * A coordinate position consisting of all the maximal ordinates for each
     * dimension for all points within the <code>Envelope</code>.
     *
     * @return The upper corner.
     */
    public DirectPosition getUpperCorner() {
        final int dim = ordinates.length/2;
        final GeneralDirectPosition position = new GeneralDirectPosition(dim);
        System.arraycopy(ordinates, dim, position.ordinates, 0, dim);
        position.setCoordinateReferenceSystem(crs);
        return position;
    }

    /**
     * A coordinate position consisting of all the minimal ordinates for each
     * dimension for all points within the <code>Envelope</code>.
     *
     * @return The lower corner.
     */
    public DirectPosition getLowerCorner() {
        final int dim = ordinates.length/2;
        final GeneralDirectPosition position = new GeneralDirectPosition(dim);
        System.arraycopy(ordinates, 0, position.ordinates, 0, dim);
        position.setCoordinateReferenceSystem(crs);
        return position;
    }
    
    /**
     * Returns the minimal ordinate along the specified dimension.
     */
    public final double getMinimum(final int dimension) {
        if (dimension < ordinates.length/2) {
            return ordinates[dimension];
        } else {
            throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Returns the maximal ordinate along the specified dimension.
     */
    public final double getMaximum(final int dimension) {
        if (dimension >= 0) {
            return ordinates[dimension + ordinates.length/2];
        } else {
            throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Returns the center ordinate along the specified dimension.
     */
    public final double getCenter(final int dimension) {
        return 0.5*(ordinates[dimension] + ordinates[dimension+ordinates.length/2]);
    }
    
    /**
     * Returns the envelope length along the specified dimension.
     * This length is equals to the maximum ordinate minus the
     * minimal ordinate.
     */
    public final double getLength(final int dimension) {
        return ordinates[dimension+ordinates.length/2] - ordinates[dimension];
    }
    
    /**
     * Set the envelope's range along the specified dimension.
     *
     * @param dimension The dimension to set.
     * @param minimum   The minimum value along the specified dimension.
     * @param maximum   The maximum value along the specified dimension.
     */
    public void setRange(final int dimension, double minimum, double maximum) {
        if (minimum > maximum) {
            // Make an empty envelope (min==max)
            // while keeping it legal (min<=max).
            minimum = maximum = 0.5*(minimum+maximum);
        }
        if (dimension >= 0) {
            // Do not make any change if 'dimension' is out of range.
            ordinates[dimension+ordinates.length/2] = maximum;
            ordinates[dimension                   ] = minimum;
        } else {
            throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Adds a point to this envelope. The resulting envelope
     * is the smallest envelope that contains both the original envelope and the
     * specified point. After adding a point, a call to {@link #contains} with the
     * added point as an argument will return <code>true</code>, except if one of
     * the point's ordinates was {@link Double#NaN} (in which case the corresponding
     * ordinate have been ignored).
     *
     * @param  position The point to add.
     * @throws MismatchedDimensionException if the specified point doesn't have
     *         the expected dimension.
     */
    public void add(final GeneralDirectPosition position) throws MismatchedDimensionException {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("position", position.getDimension(), dim);
        for (int i=0; i<dim; i++) {
            final double value = position.getOrdinate(i);
            if (value < ordinates[i    ]) ordinates[i    ]=value;
            if (value > ordinates[i+dim]) ordinates[i+dim]=value;
        }
    }
    
    /**
     * Adds an envelope object to this envelope.
     * The resulting envelope is the union of the
     * two <code>Envelope</code> objects.
     *
     * @param  envelope the <code>Envelope</code> to add to this envelope.
     * @throws MismatchedDimensionException if the specified envelope doesn't
     *         have the expected dimension.
     */
    public void add(final GeneralEnvelope envelope) throws MismatchedDimensionException {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        for (int i=0; i<dim; i++) {
            final double min = envelope.ordinates[i    ];
            final double max = envelope.ordinates[i+dim];
            if (min < ordinates[i    ]) ordinates[i    ]=min;
            if (max > ordinates[i+dim]) ordinates[i+dim]=max;
        }
    }
    
    /**
     * Tests if a specified coordinate is inside the boundary of this envelope.
     *
     * @param  position The point to text.
     * @return <code>true</code> if the specified coordinates are inside the boundary
     *         of this envelope; <code>false</code> otherwise.
     * @throws MismatchedDimensionException if the specified point doesn't have
     *         the expected dimension.
     */
    public boolean contains(final GeneralDirectPosition position) throws MismatchedDimensionException {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("point", position.getDimension(), dim);
        for (int i=0; i<dim; i++) {
            final double value = position.getOrdinate(i);
            if (!(value >= ordinates[i    ])) return false;
            if (!(value <= ordinates[i+dim])) return false;
            // Use '!' in order to take 'NaN' in account.
        }
        return true;
    }
    
    /**
     * Set this envelope to the intersection if this envelope with the specified one.
     *
     * @param  envelope the <code>Envelope</code> to intersect to this envelope.
     * @throws MismatchedDimensionException if the specified envelope doesn't
     *         have the expected dimension.
     */
    public void intersect(final GeneralEnvelope envelope) throws MismatchedDimensionException {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        for (int i=0; i<dim; i++) {
            double min = Math.max(ordinates[i    ], envelope.ordinates[i    ]);
            double max = Math.min(ordinates[i+dim], envelope.ordinates[i+dim]);
            if (min > max) {
                // Make an empty envelope (min==max)
                // while keeping it legal (min<=max).
                min = max = 0.5*(min+max);
            }
            ordinates[i    ] = min;
            ordinates[i+dim] = max;
        }
    }
    
    /**
     * Determines whether or not this envelope is empty.
     * An envelope is non-empty only if it has a length
     * greater that 0 along all dimensions.
     */
    public boolean isEmpty() {
        final int dimension = ordinates.length/2;
        for (int i=0; i<dimension; i++) {
            if (!(ordinates[i] < ordinates[i+dimension])) { // Use '!' in order to catch NaN
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns a new envelope that encompass only some dimensions of this envelope.
     * This method copy this envelope's ordinates into a new envelope, beginning at
     * dimension <code>lower</code> and extending to dimension <code>upper-1</code>.
     * Thus the dimension of the subenvelope is <code>upper-lower</code>.
     *
     * @param  lower The first dimension to copy, inclusive.
     * @param  upper The last  dimension to copy, exclusive.
     * @return The subenvelope.
     * @throws IndexOutOfBoundsException if an index is out of bounds.
     */
    public GeneralEnvelope getSubEnvelope(final int lower, final int upper)
            throws IndexOutOfBoundsException
    {
        final int curDim = ordinates.length/2;
        final int newDim = upper-lower;
        if (lower<0 || lower>curDim) {
            throw new IndexOutOfBoundsException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "lower", new Integer(lower)));
        }
        if (newDim<0 || upper>curDim) {
            throw new IndexOutOfBoundsException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "upper", new Integer(upper)));
        }
        final GeneralEnvelope envelope = new GeneralEnvelope(newDim);
        System.arraycopy(ordinates, lower,        envelope.ordinates, 0,      newDim);
        System.arraycopy(ordinates, lower+curDim, envelope.ordinates, newDim, newDim);
        return envelope;
    }

    /**
     * Returns a new envelope with the same values than this envelope minus the
     * specified range of dimensions.
     *
     * @param  lower The first dimension to omit, inclusive.
     * @param  upper The last  dimension to omit, exclusive.
     * @return The subenvelope.
     * @throws IndexOutOfBoundsException if an index is out of bounds.
     */
    public GeneralEnvelope getReducedEnvelope(final int lower, final int upper)
            throws IndexOutOfBoundsException
    {
        final int curDim = ordinates.length/2;
        final int rmvDim = upper-lower;
        if (lower<0 || lower>curDim) {
            throw new IndexOutOfBoundsException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "lower", new Integer(lower)));
        }
        if (rmvDim<0 || upper>curDim) {
            throw new IndexOutOfBoundsException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "upper", new Integer(upper)));
        }
        final GeneralEnvelope envelope = new GeneralEnvelope(curDim - rmvDim);
        System.arraycopy(ordinates, 0,     envelope.ordinates, 0,            lower);
        System.arraycopy(ordinates, lower, envelope.ordinates, upper, curDim-upper);
        return envelope;
    }
    
    /**
     * Returns a {@link Rectangle2D} with the same bounds as this <code>Envelope</code>.
     * This is a convenience method for interoperability with Java2D.
     *
     * @throws IllegalStateException if this envelope is not two-dimensional.
     */
    public Rectangle2D toRectangle2D() throws IllegalStateException {
        if (ordinates.length == 4) {
            return XRectangle2D.createFromExtremums(ordinates[0], ordinates[1],
                                                    ordinates[2], ordinates[3]);
        } else {
            throw new IllegalStateException(Resources.format(
                    ResourceKeys.ERROR_NOT_TWO_DIMENSIONAL_$1, new Integer(getDimension())));
        }
    }
    
    /**
     * Returns a string representation of this envelope.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes.
     */
    public String toString() {
        return GeneralDirectPosition.toString(this, ordinates);
    }
    
    /**
     * Returns a hash value for this envelope.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return GeneralDirectPosition.hashCode(ordinates) ^ (int)serialVersionUID;
    }
    
    /**
     * Compares the specified object with
     * this envelope for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final GeneralEnvelope that = (GeneralEnvelope) object;
            return Arrays.equals(this.ordinates, that.ordinates);
        }
        return false;
    }
    
    /**
     * Returns a deep copy of this envelope.
     */
    public Object clone() {
        try {
            GeneralEnvelope e = (GeneralEnvelope) super.clone();
            e.ordinates = (double[]) e.ordinates.clone();
            return e;
        } catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable.
            throw new AssertionError(exception);
        }
    }
}
