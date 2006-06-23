/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.matrix.MatrixFactory;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
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
     * This math transform maps pixel center to "real world" coordinate using the
     * following line:
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
     * Constructs a new grid geometry from a {@linkplain MathTransform math transform}. This is
     * the most general constructor, the one that gives the maximal control on the grid geometry
     * to be created.
     *
     * @param gridRange The valid coordinate range of a grid coverage, or {@code null} if none.
     * @param gridToCRS The math transform which allows for the transformations from
     *        grid coordinates (pixel's <em>center</em>) to real world earth coordinates.
     *        May be {@code null} (but this is not recommanded).
     * @param crs The coordinate reference system for the "real world" coordinates, or {@code null}
     *        if unknown. This CRS is given to the {@linkplain #getEnvelope envelope}.
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
            /*
             * Checks arguments.
             */
            final int dimRange  = gridRange.getDimension();
            final int dimSource = gridToCRS.getSourceDimensions();
            final int dimTarget = gridToCRS.getTargetDimensions();
            if (dimRange != dimSource) {
                throw new MismatchedDimensionException(format(dimRange, dimSource));
            }
            if (dimRange != dimTarget) {
                throw new MismatchedDimensionException(format(dimRange, dimTarget));
            }
            /*
             * Computes the envelope.
             */
            GeneralEnvelope envelope = new GeneralEnvelope(dimSource);
            for (int i=0; i<dimSource; i++) {
                // According OpenGIS specification, GridGeometry maps pixel's center.
                // We want a bounding box for all pixels, not pixel's centers. Offset by
                // 0.5 (use -0.5 for maximum too, not +0.5, since maximum is exclusive).
                envelope.setRange(i, gridRange.getLower(i)-0.5, gridRange.getUpper(i)-0.5);
            }
            try {
                envelope = CRSUtilities.transform(gridToCRS, envelope);
            } catch (TransformException exception) {
                throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_TRANSFORM_$1,
                                        Utilities.getShortClassName(gridToCRS))/*, exception*/);
                // TODO: uncomment the exception cause when we will be allowed to target J2SE 1.5.
            }
            envelope.setCoordinateReferenceSystem(crs);
            this.envelope = envelope;
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
     * envelope using heuristic rules described below.
     * <p>
     * This convenience constructor assumes that axis order in the supplied {@code gridRange}
     * argument matches exactly axis order in the supplied {@code userRange} argument. In other
     * words, if axis order in the underlying image is (<var>column</var>, <var>row</var>) (which
     * is the case for a majority of images), then the envelope given to this constructor should
     * probably have a (<var>longitude</var>, <var>latitude</var>) or
     * (<var>easting</var>, <var>northing</var>) axis order.
     * <p>
     * An exception to the above rule applies for CRS using exactly the following axis order:
     * ({@link AxisDirection#NORTH NORTH}|{@link AxisDirection#SOUTH SOUTH},
     * {@link AxisDirection#EAST EAST}|{@link AxisDirection#WEST WEST}).
     * An example of such CRS is {@code EPSG:4326}. This convenience constructor will
     * interchange automatically the (<var>y</var>,<var>x</var>) axis for such CRS.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate. This rectangle must
     *                  contains entirely all pixels, i.e. the rectangle's upper left corner must
     *                  coincide with the upper left corner of the first pixel and the rectangle's
     *                  lower right corner must coincide with the lower right corner of the last
     *                  pixel.
     *
     * @throws MismatchedDimensionException if the grid range and the CRS doesn't have
     *         consistent dimensions.
     *
     * @since 2.2
     */
    public GeneralGridGeometry(final GridRange gridRange, final Envelope userRange)
            throws MismatchedDimensionException
    {
        this(gridRange, userRange, getCoordinateSystem(userRange));
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private GeneralGridGeometry(final GridRange gridRange, final Envelope userRange,
                                final CoordinateSystem cs)
            throws MismatchedDimensionException
    {
        this(gridRange, userRange, reverse(cs), swapXY(cs));
    }

    /**
     * Constructs a new grid geometry from an {@linkplain Envelope envelope}. An
     * {@linkplain AffineTransform affine transform} will be computed automatically from the
     * specified envelope. The two last arguments ({@code swapXY} and {@code reverse}) are hints
     * about the affine transform to be created: the {@code reverse} argument tells which (if any)
     * axis from <cite>user</cite> space (not grid space) should have their direction reversed, and
     * the {@code swapXY} argument tells if the two first axis should be interchanged.
     * <p>
     * This constructor is convenient when the following conditions are meet:
     * <p>
     * <ul>
     *   <li><p>Pixels coordinates (usually (<var>x</var>,<var>y</var>) integer values inside
     *       the rectangle specified by the {@code gridRange} argument) are expressed in some
     *       {@linkplain CoordinateReferenceSystem coordinate reference system} known at compile
     *       time. This is often the case; for example the CRS attached to {@link BufferedImage}
     *       has always ({@linkplain AxisDirection#COLUMN_POSITIVE column},
     *       {@linkplain AxisDirection#ROW_POSITIVE row}) axis, with the origin (0,0) in the upper
     *       left corner, and row values increasing down.</p></li>
     *
     *   <li><p>"Real world" coordinates (inside the envelope specified by the {@code userRange}
     *       argument) are expressed in arbitrary <em>horizontal</em> coordinate reference system.
     *       Axis directions may be ({@linkplain AxisDirection#NORTH North},
     *       {@linkplain AxisDirection#WEST West}), or ({@linkplain AxisDirection#EAST East},
     *       {@linkplain AxisDirection#NORTH North}), <cite>etc.</cite>.</p></li>
     * </ul>
     * <p>
     * In such case (and assuming that the image's CRS has the same characteristics than the
     * {@link BufferedImage}'s CRS described above):
     * <p>
     * <ul>
     *   <li><p>{@code swapXY} shall be set to {@code true} if the "real world" axis order is
     *       ({@linkplain AxisDirection#NORTH North}, {@linkplain AxisDirection#EAST East}) instead
     *       of ({@linkplain AxisDirection#EAST East}, {@linkplain AxisDirection#NORTH North}). This
     *       axis swapping is necessary for mapping the ({@linkplain AxisDirection#COLUMN_POSITIVE
     *       column}, {@linkplain AxisDirection#ROW_POSITIVE row}) axis order associated to the
     *       image CRS.</p></li>
     *
     *   <li><p>In addition, the "real world" axis directions shall be reversed (by setting
     *       {@code reverse[dimension]} to {@code true}) if their direction is
     *       {@link AxisDirection#WEST WEST} (<var>x</var> axis) or
     *       {@link AxisDirection#NORTH NORTH} (<var>y</var> axis), in order to get them oriented
     *       toward the {@link AxisDirection#EAST EAST} or {@link AxisDirection#SOUTH SOUTH}
     *       direction respectively. The later may seems unatural, but it reflects the fact
     *       that row values are increasing down in an {@link BufferedImage}'s CRS.</p></li>
     * </ul>
     * <p>
     * If this convenience constructor do not provides suffisient control on axis order or reversal,
     * then an affine transform shall be created explicitly and the grid geometry shall be created
     * using the {@linkplain #GeneralGridGeometry(GridRange,MathTransform) constructor expecting
     * a math transform} argument.
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
     * @throws MismatchedDimensionException if the grid range and the CRS doesn't have
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
        this.gridRange = gridRange;
        this.envelope  = new GeneralEnvelope(userRange);
        /*
         * Checks arguments validity. Grid range and envelope dimensions must match.
         * We are more tolerant for the coordinate system dimension (if any), since
         * it is only an optional hint for interchanging axis in a more common order.
         */
        final int dimension = gridRange.getDimension();
        final int userDim   = userRange.getDimension();
        if (userDim != dimension) {
            throw new MismatchedDimensionException(format(dimension, userDim));
        }
        if (reverse!=null && reverse.length!=dimension) {
            throw new MismatchedDimensionException(format(dimension, reverse.length));
        }
        /*
         * Setup the multi-dimensional affine transform for use with OpenGIS.
         * According OpenGIS specification, transforms must map pixel center.
         * This is done by adding 0.5 to grid coordinates.
         */
        final Matrix matrix = MatrixFactory.create(dimension+1);
        for (int i=0; i<dimension; i++) {
            // NOTE: i is a dimension in the 'gridRange' space (source coordinates).
            //       j is a dimension in the 'userRange' space (target coordinates).
            int j = i;
            if (swapXY && j<=1) {
                j = 1-j;
            }
            double scale = userRange.getLength(j) / gridRange.getLength(i);
            double offset;
            if (reverse==null || !reverse[j]) {
                offset = userRange.getMinimum(j);
            } else {
                scale  = -scale;
                offset = userRange.getMaximum(j);
            }
            offset -= scale * (gridRange.getLower(i)-0.5);
            matrix.setElement(j, j,         0.0   );
            matrix.setElement(j, i,         scale );
            matrix.setElement(j, dimension, offset);
        }
        gridToCRS = ProjectiveTransform.create(matrix);
    }

    /**
     * Returns the coordinate system in use with the specified envelope. This method
     * is invoked by the {@link #GeneralGridGeometry(GridRange,Envelope)} constructor.
     *
     * @todo Avoid the call to {@code getLowerCorner()} if we provide a
     *       {@code getCoordinateReferenceSystem()} directly in GeoAPI
     *       envelope interface.
     */
    static CoordinateSystem getCoordinateSystem(final Envelope envelope) {
        if (envelope != null) {
            final CoordinateReferenceSystem crs;
            crs = envelope.getLowerCorner().getCoordinateReferenceSystem();
            if (crs != null) {
                return crs.getCoordinateSystem();
            }
        }
        return null;
    }

    /**
     * Applies heuristic rules in order to determine which axis should be reversed. This
     * method is invoked by the {@link #GeneralGridGeometry(GridRange,Envelope)} constructor.
     */
    static boolean[] reverse(final CoordinateSystem cs) {
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
     * This method is invoked by the {@link #GeneralGridGeometry(GridRange,Envelope)} constructor.
     */
    static boolean swapXY(final CoordinateSystem cs) {
        boolean swapXY = false;
        if (cs!=null && cs.getDimension() >= 2) {
            swapXY = AxisDirection.NORTH.equals(cs.getAxis(0).getDirection().absolute()) &&
                     AxisDirection.EAST .equals(cs.getAxis(1).getDirection().absolute());
        }
        return swapXY;
    }

    /**
     * Formats an error message for mismatched dimension.
     */
    private static String format(final int dim1, final int dim2) {
        return Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$2, new Integer(dim1), new Integer(dim2));
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
     * Returns the math transform which allows for the transformations from grid coordinates to
     * real world earth coordinates. The transform is often an affine transformation. The coordinate
     * reference system of the real world coordinates is given by
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
     * @see GridGeometry2D#getGridToCoordinateSystem2D
     */
    public MathTransform getGridToCoordinateSystem() throws InvalidGridGeometryException {
        if (gridToCRS != null) {
            assert isDefined(GRID_TO_CRS);
            return gridToCRS;
        }
        assert !isDefined(GRID_TO_CRS);
        throw new InvalidGridGeometryException(Errors.format(ErrorKeys.UNSPECIFIED_TRANSFORM));
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
