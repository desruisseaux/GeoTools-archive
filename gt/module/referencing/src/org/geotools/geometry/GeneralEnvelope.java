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
import javax.units.Unit;
import javax.units.ConversionException;

// OpenGIS dependencies
import org.opengis.util.Cloneable;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.geometry.XRectangle2D;


/**
 * A minimum bounding box or rectangle. Regardless of dimension, an {@code Envelope} can
 * be represented without ambiguity as two {@linkplain DirectPosition direct positions}
 * (coordinate points). To encode an {@code Envelope}, it is sufficient to encode these
 * two points.
 * <p>
 * This particular implementation of {@code Envelope} is said "General" because it
 * uses coordinates of an arbitrary dimension.
 * <p>
 * <strong>Tip:</strong> The metadata package provides a
 * {@link org.opengis.metadata.extent.GeographicBoundingBox}, which can be used as
 * a kind of envelope with a coordinate reference system fixed to WGS 84 (EPSG:4326).
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Envelope2D
 * @see org.geotools.geometry.jts.ReferencedEnvelope
 * @see org.opengis.metadata.extent.GeographicBoundingBox
 */
public class GeneralEnvelope implements Envelope, Cloneable, Serializable {
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
     * The coordinate reference system, or {@code null}.
     */
    private CoordinateReferenceSystem crs;

    /**
     * Constructs a new envelope with the same data than the specified envelope.
     */
    public GeneralEnvelope(final Envelope envelope) {
        if (envelope instanceof GeneralEnvelope) {
            final GeneralEnvelope e = (GeneralEnvelope) envelope;
            ordinates = (double[]) e.ordinates.clone();
            crs = e.crs;
        } else {
            final DirectPosition lower = envelope.getLowerCorner();
            final DirectPosition upper = envelope.getUpperCorner();
            crs = getCoordinateReferenceSystem(envelope);
            final int dimension = crs.getCoordinateSystem().getDimension();
            ordinates = new double[2*dimension];
            for (int i=0; i<dimension; i++) {
                ordinates[i]           = lower.getOrdinate(i);
                ordinates[i+dimension] = upper.getOrdinate(i);
            }
            checkCoherence();
        }
    }

    /**
     * Constructs an empty envelope with the specified coordinate reference system.
     * All ordinates are initialized to 0.
     *
     * @since 2.2
     */
    public GeneralEnvelope(final CoordinateReferenceSystem crs) {
        this(crs.getCoordinateSystem().getDimension());
        this.crs = crs;
    }

    /**
     * Constructs an empty envelope of the specified dimension.
     * All ordinates are initialized to 0.
     */
    public GeneralEnvelope(final int dimension) {
        ordinates = new double[dimension*2];
    }

    /**
     * Constructs one-dimensional envelope defined by a range of values.
     *
     * @param min The minimal value.
     * @param max The maximal value.
     */
    public GeneralEnvelope(final double min, final double max) {
        ordinates = new double[] {min, max};
        checkCoherence();
    }

    /**
     * Constructs a envelope defined by two positions.
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
            throw new MismatchedDimensionException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$2,
                                new Integer(minCP.length), new Integer(maxCP.length)));
        }
        ordinates = new double[minCP.length + maxCP.length];
        System.arraycopy(minCP, 0, ordinates, 0,            minCP.length);
        System.arraycopy(maxCP, 0, ordinates, minCP.length, maxCP.length);
        checkCoherence();
    }

    /**
     * Constructs a envelope defined by two positions.
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
        final CoordinateReferenceSystem crs1 = minCP.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem crs2 = maxCP.getCoordinateReferenceSystem();
        if (crs1 == null) {
            crs = crs2;
        } else {
            crs = crs1;
            if (crs2!=null && !CRSUtilities.equalsIgnoreMetadata(crs1, crs2)) {
                throw new IllegalArgumentException(
                          Errors.format(ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM));
            }
        }
        GeneralDirectPosition.checkCoordinateReferenceSystemDimension(crs, ordinates.length/2);
    }

    /**
     * Constructs two-dimensional envelope defined by a {@link Rectangle2D}.
     */
    public GeneralEnvelope(final Rectangle2D rect) {
        ordinates = new double[] {
            rect.getMinX(), rect.getMinY(),
            rect.getMaxX(), rect.getMaxY()
        };
        checkCoherence();
    }

    /**
     * Checks if ordinate values in the minimum point are less than or
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
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.ILLEGAL_ENVELOPE_ORDINATE_$1, new Integer(i)));
            }
        }
    }

    /**
     * Returns the coordinate reference system from an arbitrary envelope. This method performs
     * some sanity checking for ensuring that the envelope CRS is consistent.
     *
     * @todo See if we can simplify this code with GeoAPI 2.1.
     */
    static CoordinateReferenceSystem getCoordinateReferenceSystem(final Envelope envelope) {
        final DirectPosition lower = envelope.getLowerCorner();
        final DirectPosition upper = envelope.getUpperCorner();
        if (lower.getDimension() == upper.getDimension()) {
            final CoordinateReferenceSystem crs = lower.getCoordinateReferenceSystem();
            if (Utilities.equals(crs, upper.getCoordinateReferenceSystem())) {
                return crs;
            }
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.MALFORMED_ENVELOPE));
    }

    /**
     * Returns the coordinate reference system in which the coordinates are given.
     *
     * @return The coordinate reference system, or {@code null}.
     */
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        assert crs==null || crs.getCoordinateSystem().getDimension() == getDimension();
        return crs;
    }

    /**
     * Set the coordinate reference system in which the coordinate are given.
     * Note: this method <strong>do not</strong> reproject the envelope.
     *
     * @param  crs The new coordinate reference system, or {@code null}.
     * @throws MismatchedDimensionException if the specified CRS doesn't have the expected
     *         number of dimensions.
     */
    public void setCoordinateReferenceSystem(final CoordinateReferenceSystem crs)
            throws MismatchedDimensionException
    {
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
     * A coordinate position consisting of all the minimal ordinates for each
     * dimension for all points within the {@code Envelope}.
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
     * A coordinate position consisting of all the maximal ordinates for each
     * dimension for all points within the {@code Envelope}.
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
     * Returns the envelope length along the specified dimension, in terms of the given units.
     *
     * @param  unit The unit for the return value.
     * @return The length in terms of the given unit.
     * @throws ConversionException if the length can't be converted to the specified units.
     *
     * @since 2.2
     */
    public double getLength(final int dimension, final Unit unit) throws ConversionException {
        double value = getLength(dimension);
        if (crs != null) {
            final Unit source = crs.getCoordinateSystem().getAxis(dimension).getUnit();
            if (source != null) {
                value = source.getConverterTo(unit).convert(value);
            }
        }
        return value;
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
            // An exception will be thrown before any change if 'dimension' is out of range.
            ordinates[dimension+ordinates.length/2] = maximum;
            ordinates[dimension                   ] = minimum;
        } else {
            throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }

    /**
     * Set this envelope to the same coordinate values than the specified envelope.
     *
     * @param  envelope The new envelope to copy coordinates from.
     * @throws MismatchedDimensionException if the specified envelope doesn't have the expected
     *         number of dimensions.
     *
     * @since 2.2
     */
    public void setEnvelope(final GeneralEnvelope envelope) throws MismatchedDimensionException {
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), getDimension());
        if (envelope.crs != null) {
            crs = envelope.crs;
            assert crs.getCoordinateSystem().getDimension() == getDimension();
        }
        System.arraycopy(ordinates, 0, envelope.ordinates, 0, ordinates.length);
        assert equals(envelope);
    }

    /**
     * Sets the lower corner to {@linkplain Double#NEGATIVE_INFINITY negative infinity}
     * and the upper corner to {@linkplain Double#POSITIVE_INFINITY positive infinity}.
     * The {@linkplain #getCoordinateReferenceSystem coordinate reference system} (if any)
     * stay unchanged.
     *
     * @since 2.2
     */
    public void setToInfinite() {
        final int mid = ordinates.length/2;
        Arrays.fill(ordinates, 0,   mid,              Double.NEGATIVE_INFINITY);
        Arrays.fill(ordinates, mid, ordinates.length, Double.POSITIVE_INFINITY);
        assert isInfinite() : this;
    }

    /**
     * Returns {@code true} if at least one ordinate has an
     * {@linkplain Double#isInfinite infinite} value.
     *
     * @since 2.2
     */
    public boolean isInfinite() {
        for (int i=0; i<ordinates.length; i++) {
            if (Double.isInfinite(ordinates[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets all ordinate values to {@linkplain Double#NaN NaN}. The
     * {@linkplain #getCoordinateReferenceSystem coordinate reference system} (if any) stay
     * unchanged.
     *
     * @since 2.2
     */
    public void setToNull() {
        Arrays.fill(ordinates, Double.NaN);
        assert isNull() : this;
    }

    /**
     * Returns {@code false} if at least one ordinate value is not {@linkplain Double#NaN NaN}. The
     * {@code isNull()} check is a little bit different than {@link #isEmpty()} since it returns
     * {@code false} for a partially initialized envelope, while {@code isEmpty()} returns
     * {@code false} only after all dimensions have been initialized. More specifically, the
     * following rules apply:
     * <p>
     * <ul>
     *   <li>If <code>isNull() == true</code>, then <code>{@linkplain #isEmpty()} == true</code></li>
     *   <li>If <code>{@linkplain #isEmpty()} == false</code>, then <code>isNull() == false</code></li>
     *   <li>The converse of the above-cited rules are not always true.</li>
     * </ul>
     *
     * @since 2.2
     */
    public boolean isNull() {
        for (int i=0; i<ordinates.length; i++) {
            if (!Double.isNaN(ordinates[i])) {
                return false;
            }
        }
        assert isEmpty() : this;
        return true;
    }

    /**
     * Determines whether or not this envelope is empty. An envelope is non-empty only if it has
     * at least one {@linkplain #getDimension dimension}, and the {@linkplain #getLength length}
     * is greater that 0 along all dimensions. Note that an empty envelope is always {@linkplain
     * #isNull null}, but the converse is not always true.
     */
    public boolean isEmpty() {
        final int dimension = ordinates.length/2;
        if (dimension == 0) {
            return true;
        }
        for (int i=0; i<dimension; i++) {
            if (!(ordinates[i] < ordinates[i+dimension])) { // Use '!' in order to catch NaN
                return true;
            }
        }
        assert !isNull() : this;
        return false;
    }

    /**
     * Returns {@code true} if at least one of the specified CRS is null, or both CRS are equals.
     * This special processing for {@code null} values is different from the usual contract of an
     * {@code equals} method, but allow to handle the case where the CRS is unknown.
     */
    private static boolean equalsIgnoreMetadata(final CoordinateReferenceSystem crs1,
                                                final CoordinateReferenceSystem crs2)
    {
        return crs1==null || crs2==null || CRSUtilities.equalsIgnoreMetadata(crs1, crs2);
    }

    /**
     * Adds a point to this envelope. The resulting envelope is the smallest envelope that
     * contains both the original envelope and the specified point. After adding a point,
     * a call to {@link #contains} with the added point as an argument will return {@code true},
     * except if one of the point's ordinates was {@link Double#NaN} (in which case the
     * corresponding ordinate have been ignored).
     * <p>
     * This method assumes that the specified point uses the same CRS than this envelope.
     * For performance reason, it will no be verified unless J2SE assertions are enabled.
     *
     * @param  position The point to add.
     * @throws MismatchedDimensionException if the specified point doesn't have
     *         the expected dimension.
     */
    public void add(final DirectPosition position) throws MismatchedDimensionException {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("position", position.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, position.getCoordinateReferenceSystem()) : position;
        for (int i=0; i<dim; i++) {
            final double value = position.getOrdinate(i);
            if (value < ordinates[i    ]) ordinates[i    ]=value;
            if (value > ordinates[i+dim]) ordinates[i+dim]=value;
        }
        assert isEmpty() || contains(position);
    }

    /**
     * Adds an envelope object to this envelope. The resulting envelope is the union of the
     * two {@code Envelope} objects.
     * <p>
     * This method assumes that the specified envelope uses the same CRS than this envelope.
     * For performance reason, it will no be verified unless J2SE assertions are enabled.
     *
     * @param  envelope the {@code Envelope} to add to this envelope.
     * @throws MismatchedDimensionException if the specified envelope doesn't
     *         have the expected dimension.
     */
    public void add(final Envelope envelope) throws MismatchedDimensionException {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, getCoordinateReferenceSystem(envelope)) : envelope;
        for (int i=0; i<dim; i++) {
            final double min = envelope.getMinimum(i);
            final double max = envelope.getMaximum(i);
            if (min < ordinates[i    ]) ordinates[i    ]=min;
            if (max > ordinates[i+dim]) ordinates[i+dim]=max;
        }
        assert isEmpty() || contains(envelope, true);
    }

    /**
     * Tests if a specified coordinate is inside the boundary of this envelope.
     * <p>
     * This method assumes that the specified point uses the same CRS than this envelope.
     * For performance reason, it will no be verified unless J2SE assertions are enabled.
     *
     * @param  position The point to text.
     * @return {@code true} if the specified coordinates are inside the boundary
     *         of this envelope; {@code false} otherwise.
     * @throws MismatchedDimensionException if the specified point doesn't have
     *         the expected dimension.
     */
    public boolean contains(final DirectPosition position) throws MismatchedDimensionException {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("point", position.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, position.getCoordinateReferenceSystem()) : position;
        for (int i=0; i<dim; i++) {
            final double value = position.getOrdinate(i);
            if (!(value >= ordinates[i    ])) return false;
            if (!(value <= ordinates[i+dim])) return false;
            // Use '!' in order to take 'NaN' in account.
        }
        return true;
    }

    /**
     * Returns {@code true} if this envelope completly encloses the specified envelope.
     * If one or more edges from the specified envelope coincide with an edge from this
     * envelope, then this method returns {@code true} only if {@code edgesInclusive}
     * is {@code true}.
     *
     * @param  envelope The envelope to test for inclusion.
     * @param  edgesInclusive {@code true} if this envelope edges are inclusive.
     * @return {@code true} if this envelope completly encloses the specified one.
     * @throws MismatchedDimensionException if the specified envelope doesn't have
     *         the expected dimension.
     *
     * @since 2.2
     */
    public boolean contains(final Envelope envelope, final boolean edgesInclusive)
            throws MismatchedDimensionException
    {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, getCoordinateReferenceSystem(envelope)) : envelope;
        for (int i=0; i<dim; i++) {
            double inner = envelope.getMinimum(i);
            double outer = ordinates[i];
            if (!(edgesInclusive ? inner >= outer : inner > outer)) { // ! is for catching NaN.
                return false;
            }
            inner = envelope.getMaximum(i);
            outer = ordinates[i+dim];
            if (!(edgesInclusive ? inner <= outer : inner < outer)) { // ! is for catching NaN.
                return false;
            }
        }
        assert intersects(envelope, edgesInclusive);
        return true;
    }

    /**
     * Returns {@code true} if this envelope intersects the specified envelope.
     * If one or more edges from the specified envelope coincide with an edge from this
     * envelope, then this method returns {@code true} only if {@code edgesInclusive}
     * is {@code true}.
     *
     * @param  envelope The envelope to test for intersection.
     * @param  edgesInclusive {@code true} if this envelope edges are inclusive.
     * @return {@code true} if this envelope intersects the specified one.
     * @throws MismatchedDimensionException if the specified envelope doesn't have
     *         the expected dimension.
     *
     * @since 2.2
     */
    public boolean intersects(final Envelope envelope, final boolean edgesInclusive)
            throws MismatchedDimensionException
    {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, getCoordinateReferenceSystem(envelope)) : envelope;
        for (int i=0; i<dim; i++) {
            double inner = envelope.getMaximum(i);
            double outer = ordinates[i];
            if (!(edgesInclusive ? inner >= outer : inner > outer)) { // ! is for catching NaN.
                return false;
            }
            inner = envelope.getMinimum(i);
            outer = ordinates[i+dim];
            if (!(edgesInclusive ? inner <= outer : inner < outer)) { // ! is for catching NaN.
                return false;
            }
        }
        return true;
    }

    /**
     * Sets this envelope to the intersection if this envelope with the specified one.
     * <p>
     * This method assumes that the specified envelope uses the same CRS than this envelope.
     * For performance reason, it will no be verified unless J2SE assertions are enabled.
     *
     * @param  envelope the {@code Envelope} to intersect to this envelope.
     * @throws MismatchedDimensionException if the specified envelope doesn't
     *         have the expected dimension.
     */
    public void intersect(final Envelope envelope) throws MismatchedDimensionException {
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, getCoordinateReferenceSystem(envelope)) : envelope;
        for (int i=0; i<dim; i++) {
            double min = Math.max(ordinates[i    ], envelope.getMinimum(i));
            double max = Math.min(ordinates[i+dim], envelope.getMaximum(i));
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
            throw new IndexOutOfBoundsException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "lower", new Integer(lower)));
        }
        if (newDim<0 || upper>curDim) {
            throw new IndexOutOfBoundsException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "upper", new Integer(upper)));
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
            throw new IndexOutOfBoundsException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "lower", new Integer(lower)));
        }
        if (rmvDim<0 || upper>curDim) {
            throw new IndexOutOfBoundsException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "upper", new Integer(upper)));
        }
        final GeneralEnvelope envelope = new GeneralEnvelope(curDim - rmvDim);
        System.arraycopy(ordinates, 0,     envelope.ordinates, 0,            lower);
        System.arraycopy(ordinates, lower, envelope.ordinates, upper, curDim-upper);
        return envelope;
    }

    /**
     * Returns a {@link Rectangle2D} with the same bounds as this {@code Envelope}.
     * This is a convenience method for interoperability with Java2D.
     *
     * @throws IllegalStateException if this envelope is not two-dimensional.
     */
    public Rectangle2D toRectangle2D() throws IllegalStateException {
        if (ordinates.length == 4) {
            return XRectangle2D.createFromExtremums(ordinates[0], ordinates[1],
                                                    ordinates[2], ordinates[3]);
        } else {
            throw new IllegalStateException(Errors.format(
                    ErrorKeys.NOT_TWO_DIMENSIONAL_$1, new Integer(getDimension())));
        }
    }

    /**
     * Returns a string representation of this envelope. The default implementation formats the
     * {@linkplain #getLowerCorner lower} and {@linkplain #getUpperCorner upper} corners using a
     * shared instance of {@link org.geotools.measure.CoordinateFormat}. This is okay for occasional
     * formatting (for example for debugging purpose). But if there is a lot of positions to format,
     * users will get better performance and more control by using their own instance of
     * {@link org.geotools.measure.CoordinateFormat}.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append('[');
        buffer.append(GeneralDirectPosition.toString(getLowerCorner()));
        buffer.append(" , ");
        buffer.append(GeneralDirectPosition.toString(getUpperCorner()));
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Returns a hash value for this envelope. This value need not remain
     * consistent between different implementations of the same class.
     */
    public int hashCode() {
        int code = GeneralDirectPosition.hashCode(ordinates) ^ (int)serialVersionUID;
        if (crs != null) {
            code ^= crs.hashCode();
        }
        return code;
    }

    /**
     * Compares the specified object with this envelope for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final GeneralEnvelope that = (GeneralEnvelope) object;
            return Arrays.equals(this.ordinates, that.ordinates) &&
                   Utilities.equals(this.crs, that.crs);
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
