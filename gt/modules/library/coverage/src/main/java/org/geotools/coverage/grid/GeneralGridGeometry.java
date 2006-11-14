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
package org.geotools.coverage.grid;

// J2SE dependencies
import java.awt.geom.AffineTransform;  // For javadoc
import java.awt.image.BufferedImage;   // For javadoc
import java.awt.image.RenderedImage;   // For javadoc
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Describes the valid range of grid coordinates and the math transform to transform grid
 * coordinates to real world coordinates. Grid geometries contains:
 * <p>
 * <ul>
 *   <li>An optional {@linkplain GridRange grid range}, usually inferred from the
 *       {@linkplain RenderedImage rendered image} size.</li>
 *   <li>An optional "grid to CRS" {@linkplain MathTransform transform}, which may be inferred
 *       from the grid range and the envelope.</li>
 *   <li>An optional {@linkplain Envelope envelope}, which may be inferred from the grid range
 *       and the "grid to CRS" transform.</li>
 *   <li>An optional {@linkplain CoordinateReferenceSystem coordinate reference system} to be
 *       given to the envelope.</li>
 * </ul>
 * <p>
 * All grid geometry attributes are optional because some of them may be inferred from a wider
 * context. For example a grid geometry know nothing about {@linkplain RenderedImage rendered
 * images}, but {@link GridCoverage2D} do. Consequently, the later may infer the {@linkplain
 * GridRange grid range} by itself.
 * <p>
 * By default, any request for an undefined attribute will thrown an
 * {@link InvalidGridGeometryException}. In order to check if an attribute is defined,
 * use {@link #isDefined}.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Alessio Fabiani
 */
public class GeneralGridGeometry implements GridGeometry, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 124700383873732132L;

    /**
     * A bitmask to specify the validity of the {@linkplain #getCoordinateReferenceSystem
     * coordinate reference system}. This is given as an argument to the {@link #isDefined}
     * method.
     *
     * @since 2.2
     */
    public static final int CRS = 1;

    /**
     * A bitmask to specify the validity of the {@linkplain #getEnvelope envelope}.
     * This is given as an argument to the {@link #isDefined} method.
     *
     * @since 2.2
     */
    public static final int ENVELOPE = 2;

    /**
     * A bitmask to specify the validity of the {@linkplain #getGridRange grid range}.
     * This is given as an argument to the {@link #isDefined} method.
     *
     * @since 2.2
     */
    public static final int GRID_RANGE = 4;

    /**
     * A bitmask to specify the validity of the {@linkplain #getGridToCoordinateSystem grid to CRS}
     * transform. This is given as an argument to the {@link #isDefined} method.
     *
     * @since 2.2
     */
    public static final int GRID_TO_CRS = 8;

    /**
     * A buffer of math transforms created by {@link #getHalfPixelTranslation}.
     * Each element in this array will be created when first needed.
     */
    private static final MathTransform[] translations = new MathTransform[8];

    /**
     * The valid coordinate range of a grid coverage, or {@code null} if none. The lowest valid
     * grid coordinate is zero for {@link BufferedImage}, but may be non-zero for arbitrary
     * {@link RenderedImage}. A grid with 512 cells can have a minimum coordinate of 0 and
     * maximum of 512, with 511 as the highest valid index.
     *
     * @see RenderedImage#getMinX
     * @see RenderedImage#getMinY
     * @see RenderedImage#getWidth
     * @see RenderedImage#getHeight
     */
    protected final GridRange gridRange;

    /**
     * The envelope, which is usually the {@linkplain #gridRange grid range}
     * {@linkplain #gridToCRS transformed} to real world coordinates. This
     * envelope contains the {@linkplain CoordinateReferenceSystem coordinate
     * reference system} of "real world" coordinates.
     * <p>
     * This field should be considered as private because envelopes are mutable, and we want to make
     * sure that envelopes are cloned before to be returned to the user. Only {@link GridGeometry2D}
     * and {@link GridCoverage2D} access directly to this field (read only) for performance reason.
     *
     * @since 2.2
     */
    final GeneralEnvelope envelope;

    /**
     * The math transform (usually an affine transform), or {@code null} if none.
     * This math transform maps {@linkplain PixelInCell#CELL_CENTER pixel center}
     * to "real world" coordinate using the following line:
     *
     * <pre>gridToCRS.transform(pixels, point);</pre>
     */
    protected final MathTransform gridToCRS;

    /**
     * Constructs a new grid geometry identical to the specified one except for the CRS.
     * Note that this constructor just defines the CRS; it does <strong>not</strong> reproject
     * the envelope. For this reason, this constructor should not be public. It is for internal
     * use by {@link GridCoverageFactory} only.
     */
    GeneralGridGeometry(final GeneralGridGeometry gm, final CoordinateReferenceSystem crs) {
        gridRange = gm.gridRange;
        gridToCRS = gm.gridToCRS;
        envelope  = new GeneralEnvelope(gm.envelope);
        envelope.setCoordinateReferenceSystem(crs);
    }

    /**
     * Constructs a new grid geometry from a {@linkplain MathTransform math transform}
     * mapping {@linkplain PixelInCell#CELL_CENTER pixel center}. This is the most general
     * constructor, the one that gives the maximal control over the grid geometry to be created.
     *
     * @param gridRange The valid coordinate range of a grid coverage, or {@code null} if none.
     * @param gridToCRS The math transform which allows for the transformations from grid
     *                  coordinates (pixel's <em>center</em>) to real world earth coordinates.
     *                  May be {@code null}, but this is not recommanded.
     * @param crs       The coordinate reference system for the "real world" coordinates, or
     *                  {@code null} if unknown. This CRS is given to the
     *                  {@linkplain #getEnvelope envelope}.
     *
     * @throws MismatchedDimensionException if the math transform or the CRS doesn't have
     *         consistent dimensions.
     * @throws IllegalArgumentException if the math transform can't transform coordinates
     *         in the domain of the specified grid range.
     *
     * @since 2.2
     */
    public GeneralGridGeometry(final GridRange           gridRange,
                               final MathTransform       gridToCRS,
                               final CoordinateReferenceSystem crs)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        this.gridRange = gridRange;
        this.gridToCRS = gridToCRS;
        if (gridRange!=null && gridToCRS!=null) {
            envelope = new GeneralEnvelope(gridRange, PixelInCell.CELL_CENTER, gridToCRS, crs);
        } else if (crs != null) {
            envelope = new GeneralEnvelope(crs);
            envelope.setToNull();
        } else {
            envelope = null;
        }
    }

    /**
     * Constructs a new grid geometry from an {@linkplain Envelope envelope}. An {@linkplain
     * AffineTransform affine transform} will be computed automatically from the specified
     * envelope using heuristic rules described in {@link GridToEnvelopeMapper} javadoc.
     * More specifically, heuristic rules are applied for:
     * <p>
     * <ul>
     *   <li>{@linkplain GridToEnvelopeMapper#getSwapXY axis swapping}</li>
     *   <li>{@linkplain GridToEnvelopeMapper#getReverseAxis axis reversal}</li>
     * </ul>
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate. This rectangle must
     *                  contains entirely all pixels, i.e. the rectangle's upper left corner must
     *                  coincide with the upper left corner of the first pixel and the rectangle's
     *                  lower right corner must coincide with the lower right corner of the last
     *                  pixel.
     *
     * @throws MismatchedDimensionException if the grid range and the envelope doesn't have
     *         consistent dimensions.
     *
     * @since 2.2
     */
    public GeneralGridGeometry(final GridRange gridRange, final Envelope userRange)
            throws MismatchedDimensionException
    {
        this(gridRange, userRange, null, false, true);
    }

    /**
     * Constructs a new grid geometry from an {@linkplain Envelope envelope}. This convenience
     * constructor delegates the work to {@link GridToEnvelopeMapper}; see its javadoc for details.
     * <p>
     * If this convenience constructor do not provides suffisient control on axis order or reversal,
     * then an affine transform shall be created explicitly and the grid geometry shall be created
     * using the {@linkplain #GeneralGridGeometry(GridRange,MathTransform,CoordinateReferenceSystem)
     * constructor expecting a math transform} argument.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate. This envelope must
     *                  contains entirely all pixels, i.e. the envelope's upper left corner must
     *                  coincide with the upper left corner of the first pixel and the envelope's
     *                  lower right corner must coincide with the lower right corner of the last
     *                  pixel.
     * @param reverse   Tells for each axis in <cite>user</cite> space whatever or not its direction
     *                  should be reversed. A {@code null} value reverse no axis. Callers will
     *                  typically set {@code reverse[1]} to {@code true} in order to reverse the
     *                  <var>y</var> axis direction.
     * @param swapXY    If {@code true}, then the two first axis will be interchanged. Callers will
     *                  typically set this argument to {@code true} when the geographic coordinate
     *                  system has axis in the (<var>y</var>,<var>x</var>) order. The {@code reverse}
     *                  parameter then apply to axis after the swap.
     *
     * @throws MismatchedDimensionException if the grid range and the envelope doesn't have
     *         consistent dimensions.
     *
     * @since 2.2
     */
    public GeneralGridGeometry(final GridRange gridRange,
                               final Envelope  userRange,
                               final boolean[] reverse,
                               final boolean   swapXY)
            throws MismatchedDimensionException
    {
        this(gridRange, userRange, reverse, swapXY, false);
    }

    /**
     * Implementation of heuristic constructors.
     */
    GeneralGridGeometry(final GridRange gridRange,
                        final Envelope  userRange,
                        final boolean[] reverse,
                        final boolean   swapXY,
                        final boolean   automatic)
            throws MismatchedDimensionException
    {
        this.gridRange = gridRange;
        this.envelope  = new GeneralEnvelope(userRange);
        final GridToEnvelopeMapper mapper = new GridToEnvelopeMapper(gridRange, userRange);
        if (!automatic) {
            mapper.setReverseAxis(reverse);
            mapper.setSwapXY(swapXY);
        }
        gridToCRS = mapper.createTransform();
    }

    /**
     * @deprecated Replaced by {@link GridToEnvelopeMapper}.
     *
     * @since 2.3
     */
    public static MathTransform getTransform(final GridRange gridRange, final Envelope userRange, 
            final boolean[] reverse, final boolean swapXY, final boolean halfPix)
            throws MismatchedDimensionException
    {
        final GridToEnvelopeMapper mapper = new GridToEnvelopeMapper(gridRange, userRange);
        mapper.setGridType(halfPix ? PixelInCell.CELL_CENTER : PixelInCell.CELL_CORNER);
        mapper.setReverseAxis(reverse);
        mapper.setSwapXY(swapXY);
        return mapper.createTransform();
    }

    /**
     * @deprecated Replaced by {@link GridToEnvelopeMapper}.
     *
     * @since 2.3
     */
    public static MathTransform getTransform(final GridRange gridRange,
                                             final Envelope  userRange,
                                             boolean halfPix)
            throws MismatchedDimensionException
    {
        final GridToEnvelopeMapper mapper = new GridToEnvelopeMapper(gridRange, userRange);
        mapper.setGridType(halfPix ? PixelInCell.CELL_CENTER : PixelInCell.CELL_CORNER);
        return mapper.createTransform();
    }

    /**
     * Applies heuristic rules in order to determine which axis should be reversed.
     *
     * @since 2.3
     *
     * @deprecated Use {@link org.geotools.referencing.cs.AbstractCS#swapAndScaleAxis} instead.
     */
    public static boolean[] reverse(final CoordinateSystem cs) {
        if (cs == null) {
            return null;
        }
        final int dimension = cs.getDimension();
        final boolean[] reverse = new boolean[dimension];
        for (int i=0; i<dimension; i++) {
            final AxisDirection direction = cs.getAxis(i).getDirection();
            final AxisDirection absolute  = direction.absolute();
            reverse[i] = direction.equals(absolute.opposite());
            if (AxisDirection.NORTH.equals(absolute)) {
                reverse[i] = !reverse[i]; // Reverses the 'row' axis.
            }
        }
        return reverse;
    }

    /**
     * Applies heuristic rules in order to determine if the two first axis should be interchanged.
     *
     * @since 2.3
     *
     * @deprecated Use {@link org.geotools.referencing.cs.AbstractCS#swapAndScaleAxis} instead
     *             or {@link org.geotools.referencing.operation.matrix.XMatrixTransform#getSwapXY}
     *             instead.
     */
    public static boolean swapXY(final CoordinateSystem cs) {
        boolean swapXY = false;
        if (cs!=null && cs.getDimension() >= 2) {
            swapXY = AxisDirection.NORTH.equals(cs.getAxis(0).getDirection().absolute()) &&
                     AxisDirection.EAST .equals(cs.getAxis(1).getDirection().absolute());
        }
        return swapXY;
    }

    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        if (gridToCRS != null) {
            return gridToCRS.getSourceDimensions();
        }
        return getGridRange().getDimension();
    }

    /**
     * Returns the "real world" coordinate reference system.
     *
     * @return The coordinate reference system (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no CRS (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #CRS})</code>
     *         returned {@code false}).
     *
     * @see GridGeometry2D#getCoordinateReferenceSystem2D
     *
     * @since 2.2
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
            throws InvalidGridGeometryException
    {
        if (envelope != null) {
            final CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();
            if (crs != null) {
                assert isDefined(CRS);
                return crs;
            }
        }
        assert !isDefined(CRS);
        throw new InvalidGridGeometryException(Errors.format(ErrorKeys.UNSPECIFIED_CRS));
    }

    /**
     * Returns the bounding box of "real world" coordinates for this grid geometry. This envelope is
     * the {@linkplain #getGridRange grid range} {@linkplain #getGridToCoordinateSystem transformed}
     * to the "real world" coordinate system.
     *
     * @return The bounding box in "real world" coordinates (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no envelope (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #ENVELOPE})</code>
     *         returned {@code false}).
     *
     * @see GridGeometry2D#getEnvelope2D
     */
    public Envelope getEnvelope() throws InvalidGridGeometryException {
        if (envelope!=null && !envelope.isNull()) {
            assert isDefined(ENVELOPE);
            return (Envelope) envelope.clone();
        }
        assert !isDefined(ENVELOPE);
        throw new InvalidGridGeometryException(Errors.format(gridToCRS == null ?
                    ErrorKeys.UNSPECIFIED_TRANSFORM : ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
    }

    /**
     * @deprecated Use {@link GeneralEnvelope(GridRange, PixelInCell, MathTransform,
     *             CoordinateReferenceSystem)} instead.
     *
     * @since 2.3
     */
    public static GeneralEnvelope getEnvelope(final GridRange gridRange,
            final MathTransform gridToCRS, final CoordinateReferenceSystem crs,
            final boolean halfPix) throws MismatchedDimensionException,
            IllegalArgumentException
    {
        return new GeneralEnvelope(gridRange, halfPix ? PixelInCell.CELL_CENTER :
                PixelInCell.CELL_CORNER, gridToCRS, crs);
    }

    /**
     * Returns the valid coordinate range of a grid coverage. The lowest valid grid coordinate is
     * zero for {@link BufferedImage}, but may be non-zero for arbitrary {@link RenderedImage}. A
     * grid with 512 cells can have a minimum coordinate of 0 and maximum of 512, with 511 as the
     * highest valid index.
     *
     * @return The grid range (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no grid range (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #GRID_RANGE})</code>
     *         returned {@code false}).
     *
     * @see GridGeometry2D#getGridRange2D
     */
    public GridRange getGridRange() throws InvalidGridGeometryException {
        if (gridRange != null) {
            assert isDefined(GRID_RANGE);
            return gridRange;
        }
        assert !isDefined(GRID_RANGE);
        throw new InvalidGridGeometryException(Errors.format(ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
    }

    /**
     * @deprecated Renamed as {@link #getGridToCRS()}.
     */
    public MathTransform getGridToCoordinateSystem() throws InvalidGridGeometryException {
        return getGridToCRS();
    }

    /**
     * Returns the transform from grid coordinates to real world earth coordinates.
     * The transform is often an affine transform. The coordinate reference system of the
     * real world coordinates is given by
     * {@link org.opengis.coverage.Coverage#getCoordinateReferenceSystem}.
     * <p>
     * <strong>Note:</strong> OpenGIS requires that the transform maps <em>pixel centers</em>
     * to real world coordinates. This is different from some other systems that map pixel's
     * upper left corner.
     *
     * @return The transform (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no transform (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #GRID_TO_CRS})</code>
     *         returned {@code false}).
     *
     * @see GridGeometry2D#getGridToCRS2D()
     *
     * @since 2.3
     */
    public MathTransform getGridToCRS() throws InvalidGridGeometryException {
        if (gridToCRS != null) {
            assert isDefined(GRID_TO_CRS);
            return gridToCRS;
        }
        assert !isDefined(GRID_TO_CRS);
        throw new InvalidGridGeometryException(Errors.format(ErrorKeys.UNSPECIFIED_TRANSFORM));
    }

    /**
     * Returns the transform from grid coordinates to real world earth coordinates.
     * This is similar to {@link #getGridToCRS()} except that the transform may maps
     * other parts than {@linkplain PixelInCell#CELL_CENTER pixel center}.
     *
     * @param  halfPixel The pixel part to map.
     * @return The transform (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no transform (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #GRID_TO_CRS})</code>
     *         returned {@code false}).
     *
     * @since 2.3
     */
    public MathTransform getGridToCRS(final PixelInCell halfPixel)
            throws InvalidGridGeometryException
    {
        final MathTransform gridToCRS = getGridToCoordinateSystem();
        if (PixelInCell.CELL_CENTER.equals(halfPixel)) {
            return gridToCRS;
        }
        if (!PixelInCell.CELL_CORNER.equals(halfPixel)) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "halfPixel", halfPixel));
        }
        return ConcatenatedTransform.create(
                getHalfPixelTranslation(gridToCRS.getSourceDimensions()), gridToCRS);
    }

    /**
     * Returns an affine transform holding a translation from the
     * {@linkplain PixelInCell#CELL_CENTER pixel center} to the
     * {@linkplain PixelInCell#CELL_CORNER pixel corner}. The
     * translation terms are set to exactly -0.5.
     *
     * @param dimension The dimension.
     */
    private static MathTransform getHalfPixelTranslation(final int dimension) {
        synchronized (translations) {
            if (dimension < translations.length) {
                final MathTransform candidate = translations[dimension];
                if (candidate != null) {
                    return candidate;
                }
            }
            final MathTransform mt = ProjectiveTransform.createTranslation(dimension, -0.5);
            if (dimension < translations.length) {
                translations[dimension] = mt;
            }
            return mt;
        }
    }

    /**
     * Returns {@code true} if all the parameters specified by the argument are set.
     *
     * @param  bitmask Any combinaison of {@link #CRS}, {@link #ENVELOPE}, {@link #GRID_RANGE}
     *         and {@link #GRID_TO_CRS}.
     * @return {@code true} if all specified attributes are defined (i.e. invoking the
     *         corresponding method will not thrown an {@link InvalidGridGeometryException}).
     * @throws IllegalArgumentException if the specified bitmask is not a combinaison of known
     *         masks.
     *
     * @since 2.2
     */
    public boolean isDefined(final int bitmask) throws IllegalArgumentException {
        if ((bitmask & ~(CRS | ENVELOPE | GRID_RANGE | GRID_TO_CRS)) != 0) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                                                             "bitmask", new Integer(bitmask)));
        }
        return ((bitmask & CRS)         == 0 || (envelope  != null && envelope.getCoordinateReferenceSystem()!=null))
            && ((bitmask & ENVELOPE)    == 0 || (envelope  != null && !envelope.isNull()))
            && ((bitmask & GRID_RANGE)  == 0 || (gridRange != null))
            && ((bitmask & GRID_TO_CRS) == 0 || (gridToCRS != null));
    }

    /**
     * Returns a hash value for this grid geometry. This value need not remain
     * consistent between different implementations of the same class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (gridToCRS != null) {
            code += gridToCRS.hashCode();
        }
        if (gridRange != null) {
            code += gridRange.hashCode();
        }
        // We do not check the envelope, since it usually has
        // a determinist relationship with other attributes.
        return code;
    }

    /**
     * Compares the specified object with this grid geometry for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final GeneralGridGeometry that = (GeneralGridGeometry) object;
            return Utilities.equals(this.gridRange, that.gridRange) &&
                   Utilities.equals(this.gridToCRS, that.gridToCRS) &&
                   Utilities.equals(this.envelope , that.envelope );
        }
        return false;
    }

    /**
     * Returns a string représentation of this grid geometry. The returned string
     * is implementation dependent. It is usually provided for debugging purposes.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        buffer.append(gridRange);
        buffer.append(", ");
        buffer.append(gridToCRS);
        buffer.append(']');
        return buffer.toString();
    }
}
