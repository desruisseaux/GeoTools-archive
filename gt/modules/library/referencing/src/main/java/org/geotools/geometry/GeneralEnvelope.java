/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.geometry;

// J2SE dependencies
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Arrays;
import javax.units.Unit;
import javax.units.ConversionException;

// OpenGIS dependencies
import org.opengis.util.Cloneable;
import org.opengis.coverage.grid.GridRange;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;


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
 * @author Simone Giannecchini
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
     * Constructs an empty envelope of the specified dimension.
     * All ordinates are initialized to 0 and the coordinate reference
     * system is undefined.
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
     * @param  minDP Minimum ordinate values.
     * @param  maxDP Maximum ordinate values.
     * @throws MismatchedDimensionException if the two positions don't have the same dimension.
     * @throws IllegalArgumentException if an ordinate value in the minimum point is not
     *         less than or equal to the corresponding ordinate value in the maximum point.
     */
    public GeneralEnvelope(final double[] minDP, final double[] maxDP)
            throws IllegalArgumentException
    {
        ensureNonNull("minDP", minDP);
        ensureNonNull("maxDP", maxDP);
        ensureSameDimension(minDP.length, maxDP.length);
        ordinates = new double[minDP.length + maxDP.length];
        System.arraycopy(minDP, 0, ordinates, 0,            minDP.length);
        System.arraycopy(maxDP, 0, ordinates, minDP.length, maxDP.length);
        checkCoherence();
    }

    /**
     * Constructs a envelope defined by two positions. The coordinate
     * reference system is inferred from the supplied direct position.
     *
     * @param  minDP Point containing minimum ordinate values.
     * @param  maxDP Point containing maximum ordinate values.
     * @throws MismatchedDimensionException if the two positions don't have the same dimension.
     * @throws MismatchedReferenceSystemException if the two positions don't use the same CRS.
     * @throws IllegalArgumentException if an ordinate value in the minimum point is not
     *         less than or equal to the corresponding ordinate value in the maximum point.
     */
    public GeneralEnvelope(final GeneralDirectPosition minDP, final GeneralDirectPosition maxDP)
            throws IllegalArgumentException
    {
//  Uncomment next lines if Sun fixes RFE #4093999
//      ensureNonNull("minDP", minDP);
//      ensureNonNull("maxDP", maxDP);
        this(minDP.ordinates, maxDP.ordinates);
        crs = getCoordinateReferenceSystem(minDP, maxDP);
        GeneralDirectPosition.checkCoordinateReferenceSystemDimension(crs, ordinates.length/2);
    }

    /**
     * Constructs an empty envelope with the specified coordinate reference system.
     * All ordinates are initialized to 0.
     *
     * @since 2.2
     */
    public GeneralEnvelope(final CoordinateReferenceSystem crs) {
//  Uncomment next line if Sun fixes RFE #4093999
//      ensureNonNull("envelope", envelope);
        this(crs.getCoordinateSystem().getDimension());
        this.crs = crs;
    }

    /**
     * Constructs a new envelope with the same data than the specified envelope.
     */
    public GeneralEnvelope(final Envelope envelope) {
        ensureNonNull("envelope", envelope);
        if (envelope instanceof GeneralEnvelope) {
            final GeneralEnvelope e = (GeneralEnvelope) envelope;
            ordinates = (double[]) e.ordinates.clone();
            crs = e.crs;
        } else {
            crs = envelope.getCoordinateReferenceSystem();
            final int dimension = envelope.getDimension();
            ordinates = new double[2*dimension];
            for (int i=0; i<dimension; i++) {
                ordinates[i]           = envelope.getMinimum(i);
                ordinates[i+dimension] = envelope.getMaximum(i);
            }
            checkCoherence();
        }
    }

    /**
     * Constructs a new envelope with the same data than the specified
     * geographic bounding box. The coordinate reference system is set
     * to {@linkplain DefaultGeographicCRS#WGS84 WGS84}.
     *
     * @since 2.4
     */
    public GeneralEnvelope(final GeographicBoundingBox box) {
        ensureNonNull("box", box);
        ordinates = new double[] {
            box.getWestBoundLongitude(),
            box.getSouthBoundLatitude(),
            box.getEastBoundLongitude(),
            box.getNorthBoundLatitude()
        };
        crs = DefaultGeographicCRS.WGS84;
    }

    /**
     * Constructs two-dimensional envelope defined by a {@link Rectangle2D}.
     * The coordinate reference system is initially undefined.
     */
    public GeneralEnvelope(final Rectangle2D rect) {
        ensureNonNull("rect", rect);
        ordinates = new double[] {
            rect.getMinX(), rect.getMinY(),
            rect.getMaxX(), rect.getMaxY()
        };
        checkCoherence();
    }

    /**
     * Creates an envelope for a grid range transformed using the specified math transform.
     *
     * @param gridRange The grid range.
     * @param gridType  Whatever grid range coordinates map to pixel center or pixel corner.
     * @param gridToCRS The transform (usually affine) from grid range to the envelope CRS.
     * @param crs       The envelope CRS, or {@code null} if unknow.
     *
     * @throws MismatchedDimensionException If one of the supplied object doesn't have
     *         a dimension compatible with the other objects.
     * @throws IllegalArgumentException if an argument is illegal for some other reason,
     *         including failure to use the provided math transform.
     *
     * @since 2.3
     */
    public GeneralEnvelope(final GridRange           gridRange,
                           final PixelInCell         gridType,
                           final MathTransform       gridToCRS,
                           final CoordinateReferenceSystem crs)
            throws IllegalArgumentException
    {
        ensureNonNull("gridRange", gridRange);
        ensureNonNull("gridToCRS", gridToCRS);
        final int dimRange  = gridRange.getDimension();
        final int dimSource = gridToCRS.getSourceDimensions();
        final int dimTarget = gridToCRS.getTargetDimensions();
        ensureSameDimension(dimRange, dimSource);
        ensureSameDimension(dimRange, dimTarget);
        ordinates = new double[dimSource*2];
        final double offset;
        if (PixelInCell.CELL_CENTER.equals(gridType)) {
            offset = 0.5;
        } else if (PixelInCell.CELL_CORNER.equals(gridType)) {
            offset = 0.0;
        } else {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "gridType", gridType));
        }
        for (int i=0; i<dimSource; i++) {
            // According OpenGIS specification, GridGeometry maps pixel's center.
            // We want a bounding box for all pixels, not pixel's centers. Offset by
            // 0.5 (use -0.5 for maximum too, not +0.5, since maximum is exclusive).
            setRange(i, gridRange.getLower(i)-offset, gridRange.getUpper(i)-offset);
        }
        final GeneralEnvelope transformed;
        try {
            transformed = CRS.transform(gridToCRS, this);
        } catch (TransformException exception) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_TRANSFORM_$1,
                                    Utilities.getShortClassName(gridToCRS))/*, exception*/);
            // TODO: uncomment the exception cause when we will be allowed to target J2SE 1.5.
        }
        assert transformed.ordinates.length == this.ordinates.length;
        System.arraycopy(transformed.ordinates, 0, this.ordinates, 0, ordinates.length);
        setCoordinateReferenceSystem(crs);
    }

    /**
     * Makes sure an argument is non-null.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidParameterValueException if {@code object} is null.
     */
    private static void ensureNonNull(final String name, final Object object)
            throws IllegalArgumentException
    {
        if (object == null) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NULL_ARGUMENT_$1, name));
        }
    }

    /**
     * Make sure the specified dimensions are identical.
     */
    private static void ensureSameDimension(final int dim1, final int dim2)
            throws MismatchedDimensionException
    {
        if (dim1 != dim2) {
            throw new MismatchedDimensionException(Errors.format(
                    ErrorKeys.MISMATCHED_DIMENSION_$2, new Integer(dim1), new Integer(dim2)));
        }
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
     * Returns the common CRS of specified points.
     *
     * @param  minDP The first position.
     * @param  maxDP The second position.
     * @return Their common CRS, or {@code null} if none.
     * @throws MismatchedReferenceSystemException if the two positions don't use the same CRS.
     */
    static CoordinateReferenceSystem getCoordinateReferenceSystem(final DirectPosition minDP,
            final DirectPosition maxDP) throws MismatchedReferenceSystemException
    {
        final CoordinateReferenceSystem crs1 = minDP.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem crs2 = maxDP.getCoordinateReferenceSystem();
        if (crs1 == null) {
            return crs2;
        } else {
            if (crs2!=null && !crs1.equals(crs2)) {
                throw new MismatchedReferenceSystemException(
                          Errors.format(ErrorKeys.MISMATCHED_COORDINATE_REFERENCE_SYSTEM));
            }
            return crs1;
        }
    }

    /**
     * Returns the coordinate reference system from an arbitrary envelope, or {@code null}
     * if unknown. This method performs some sanity checking for ensuring that the envelope
     * CRS is consistent.
     *
     * @deprecated Use {@link Envelope#getCoordinateReferenceSystem()} instead.
     *
     * @since 2.3
     */
    public static CoordinateReferenceSystem getCoordinateReferenceSystem(final Envelope envelope) {
        if (envelope == null) {
            return null;
        }
        if (envelope instanceof GeneralEnvelope) {
            return ((GeneralEnvelope) envelope).getCoordinateReferenceSystem();
        }
        if (envelope instanceof Envelope2D) {
            return ((Envelope2D) envelope).getCoordinateReferenceSystem();
        }
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
        return ordinates.length / 2;
    }

    /**
     * A coordinate position consisting of all the {@linkplain #getMinimum minimal ordinates}
     * for each dimension for all points within the {@code Envelope}.
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
     * A coordinate position consisting of all the {@linkplain #getMaximum maximal ordinates}
     * for each dimension for all points within the {@code Envelope}.
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
     * A coordinate position consisting of all the {@linkplain #getCenter(int) middle ordinates}
     * for each dimension for all points within the {@code Envelope}.
     *
     * @since 2.3
     */
    public DirectPosition getCenter() {
        final GeneralDirectPosition position = new GeneralDirectPosition(ordinates.length/2);
        for (int i=position.ordinates.length; --i>=0;) {
            position.ordinates[i] = getCenter(i);
        }
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
        ensureNonNull("envelope", envelope);
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), getDimension());
        System.arraycopy(envelope.ordinates, 0, ordinates, 0, ordinates.length);
        if (envelope.crs != null) {
            crs = envelope.crs;
            assert crs.getCoordinateSystem().getDimension() == getDimension() : crs;
            assert !envelope.getClass().equals(getClass()) || equals(envelope) : envelope;
        }
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
     * is greater than 0 along all dimensions. Note that an empty envelope is always {@linkplain
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
        return crs1==null || crs2==null || CRS.equalsIgnoreMetadata(crs1, crs2);
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
        ensureNonNull("position", position);
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
        ensureNonNull("envelope", envelope);
        final int dim = ordinates.length / 2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, envelope.getCoordinateReferenceSystem()) : envelope;
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
        ensureNonNull("position", position);
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
     * <p>
     * This method assumes that the specified envelope uses the same CRS than this envelope.
     * For performance reason, it will no be verified unless J2SE assertions are enabled.
     *
     * @param  envelope The envelope to test for inclusion.
     * @param  edgesInclusive {@code true} if this envelope edges are inclusive.
     * @return {@code true} if this envelope completly encloses the specified one.
     * @throws MismatchedDimensionException if the specified envelope doesn't have
     *         the expected dimension.
     *
     * @see #intersects(Envelope, boolean)
     * @see #equals(Envelope, double)
     *
     * @since 2.2
     */
    public boolean contains(final Envelope envelope, final boolean edgesInclusive)
            throws MismatchedDimensionException
    {
        ensureNonNull("envelope", envelope);
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, envelope.getCoordinateReferenceSystem()) : envelope;
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
     * <p>
     * This method assumes that the specified envelope uses the same CRS than this envelope.
     * For performance reason, it will no be verified unless J2SE assertions are enabled.
     *
     * @param  envelope The envelope to test for intersection.
     * @param  edgesInclusive {@code true} if this envelope edges are inclusive.
     * @return {@code true} if this envelope intersects the specified one.
     * @throws MismatchedDimensionException if the specified envelope doesn't have
     *         the expected dimension.
     *
     * @see #contains(Envelope, boolean)
     * @see #equals(Envelope, double)
     *
     * @since 2.2
     */
    public boolean intersects(final Envelope envelope, final boolean edgesInclusive)
            throws MismatchedDimensionException
    {
        ensureNonNull("envelope", envelope);
        final int dim = ordinates.length/2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, envelope.getCoordinateReferenceSystem()) : envelope;
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
        ensureNonNull("envelope", envelope);
        final int dim = ordinates.length / 2;
        GeneralDirectPosition.ensureDimensionMatch("envelope", envelope.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, envelope.getCoordinateReferenceSystem()) : envelope;
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
     * Compares to the specified envelope for equality up to the specified relative tolerance value.
     * The tolerance value {@code eps} is relative to the {@linkplain #getLength envelope length}
     * along each dimension. More specifically, the actual tolerance value for a given dimension
     * <var>i</var> is {@code eps}&times;{@code length} where {@code length} is the maximum of
     * {@linkplain #getLength this envelope length} and the specified envelope length along
     * dimension <var>i</var>.
     * <p>
     * Relative tolerance value (as opposed to absolute tolerance value) help to workaround the
     * fact that tolerance value are CRS dependent. For example the tolerance value need to be
     * smaller for geographic CRS than for UTM projections, because the former typically has a
     * range of -180 to 180° while the later can have a range of thousands of meters.
     * <p>
     * This method assumes that the specified envelope uses the same CRS than this envelope.
     * For performance reason, it will no be verified unless J2SE assertions are enabled.
     *
     * @see #contains(Envelope, boolean)
     * @see #intersects(Envelope, boolean)
     *
     * @since 2.3
     *
     * @deprecated Use {@link #equals(Envelope, double, boolean)} instead.
     */
    public boolean equals(final Envelope envelope, final double eps) {
    	return equals(envelope, eps, true);
    }

    /**
     * Compares to the specified envelope for equality up to the specified tolerance value.
     * The tolerance value {@code eps} can be either relative to the {@linkplain #getLength 
     * envelope length} along each dimension or can be an absolute value (as for example some
     * ground resolution of a {@linkplain org.opengis.coverage.grid.GridCoverage grid coverage}).
     * <p>
     * If {@code relativeToLength} is set to {@code true}, the actual tolerance value for a given
     * dimension <var>i</var> is {@code eps}&times;{@code length} where {@code length} is the
     * maximum of {@linkplain #getLength this envelope length} and the specified envelope length
     * along dimension <var>i</var>.
     * <p>
     * If {@code relativeToLength} is set to {@code false}, the actual tolerance value for a
     * given dimension <var>i</var> is {@code eps}.
     * <p>
     * Relative tolerance value (as opposed to absolute tolerance value) help to workaround the
     * fact that tolerance value are CRS dependent. For example the tolerance value need to be
     * smaller for geographic CRS than for UTM projections, because the former typically has a
     * range of -180 to 180° while the later can have a range of thousands of meters.
     * <p>
     * This method assumes that the specified envelope uses the same CRS than this envelope.
     * For performance reason, it will no be verified unless J2SE assertions are enabled.
     *
     * @param envelope The envelope to compare with.
     * @param eps The tolerance value to use for numerical comparaisons.
     * @param relativeToLength {@code true} if the tolerance value should be relative to
     *        axis length, or {@code false} if it is an absolute value.
     *
     * @see #contains(Envelope, boolean)
     * @see #intersects(Envelope, boolean)
     *
     * @since 2.4
     */
    public boolean equals(final Envelope envelope, final double eps,
                          final boolean relativeToLength)
    {
        ensureNonNull("envelope", envelope);
        final int dimension = getDimension();
        if (envelope.getDimension() != dimension) {
            return false;
        }
        assert equalsIgnoreMetadata(crs, envelope.getCoordinateReferenceSystem()) : envelope;
        for (int i=0; i<dimension; i++) {
        	double epsilon;
        	if(relativeToLength){
	            epsilon = Math.max(getLength(i), envelope.getLength(i));
	            epsilon = (epsilon>0 && epsilon<Double.POSITIVE_INFINITY) ? epsilon*eps : eps;
        	}
        	else
        		epsilon=eps;
            // Comparaison below uses '!' in order to catch NaN values.
            if (!(Math.abs(getMinimum(i) - envelope.getMinimum(i)) <= epsilon &&
                  Math.abs(getMaximum(i) - envelope.getMaximum(i)) <= epsilon))
            {
                return false;
            }
        }
        return true;
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
