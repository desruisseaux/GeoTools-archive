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
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage.grid;

// J2SE dependencies
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
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
 * Describes the valid range of grid coordinates and the math
 * transform to transform grid coordinates to real world coordinates.
 *
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeneralGridGeometry implements GridGeometry, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3133087291728297383L;

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
     * The math transform (usually an affine transform), or {@code null} if none.
     * This math transform maps pixel center to "real world" coordinate using the
     * following line:
     *
     * <pre>gridToCoordinateSystem.transform(pixels, point);</pre>
     */
    protected final MathTransform gridToCoordinateSystem;

    /**
     * Constructs a new grid geometry from a math transform.
     *
     * @param gridRange The valid coordinate range of a grid coverage, or {@code null} if none.
     * @param gridToCoordinateSystem The math transform which allows for the transformations
     *        from grid coordinates (pixel's <em>center</em>) to real world earth coordinates.
     */
    public GeneralGridGeometry(final GridRange gridRange, final MathTransform gridToCoordinateSystem) {
        this.gridRange              = gridRange;
        this.gridToCoordinateSystem = gridToCoordinateSystem;
        if (gridRange!=null && gridToCoordinateSystem!=null) {
            final int dimRange  = gridRange.getDimension();
            final int dimSource = gridToCoordinateSystem.getSourceDimensions();
            final int dimTarget = gridToCoordinateSystem.getTargetDimensions();
            if (dimRange != dimSource) {
                throw new MismatchedDimensionException(format(dimRange, dimSource));
            }
            if (dimRange != dimTarget) {
                throw new MismatchedDimensionException(format(dimRange, dimTarget));
            }
        }
    }

    /**
     * Constructs a new grid geometry from an envelope. An affine transform will be computed
     * automatically from the specified envelope. This constructor does not reverse or swap any
     * axis.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate. This rectangle must
     *                  contains entirely all pixels, i.e. the rectangle's upper left corner must
     *                  coincide with the upper left corner of the first pixel and the rectangle's
     *                  lower right corner must coincide with the lower right corner of the last
     *                  pixel.
     *
     * @since 2.2
     */
    public GeneralGridGeometry(final GridRange gridRange, final Envelope userRange) {
        this(gridRange, userRange, null, false);
    }

    /**
     * Constructs a new grid geometry from an envelope.
     *
     * @deprecated Replaced by {@code GeneralGridGeometry(gridRange, userRange, reverse, false)}.
     *             Users just need to append the {@code false} argument value, so this constructor
     *             will be removed in a future version in order to keep the API lighter.
     */
    public GeneralGridGeometry(final GridRange gridRange,
                               final Envelope  userRange,
                               final boolean[] reverse)
    {
        this(gridRange, userRange, reverse, false);
    }

    /**
     * Constructs a new grid geometry from an envelope. An affine transform will be computed
     * automatically from the specified envelope. The two last arguments are hints about the
     * affine transform to be created: the {@code reverse} argument tells which (if any) axis
     * should be reversed, and the {@code swapXY} tells if the two first axis should be
     * interchanged.
     * <p>
     * If this convenience constructor do not provides suffisient control on axis order or reversal,
     * then an affine transform shall be created outside and the grid geometry shall be created
     * using the {@linkplain #GeneralGridGeometry(GridRange,MathTransform) constructor expecting
     * a math transform} argument.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate. This rectangle must
     *                  contains entirely all pixels, i.e. the rectangle's upper left corner must
     *                  coincide with the upper left corner of the first pixel and the rectangle's
     *                  lower right corner must coincide with the lower right corner of the last
     *                  pixel.
     * @param reverse   Tells for each axis whatever or not it should be reversed. A {@code null}
     *                  value reverse no axis. Callers will typically set {@code reverse[1]} to 
     *                  {@code true} in order to reverse the <var>y</var> axis direction.
     * @param swapXY    If {@code true}, then the two first axis will be interchanged. Callers will
     *                  typically set this argument to {@code true} when the geographic coordinate
     *                  system has axis in the (<var>y</var>,<var>x</var>) order. The {@code reverse}
     *                  parameter then apply to axis after the swap.
     *
     * @since 2.2
     */
    public GeneralGridGeometry(final GridRange gridRange,
                               final Envelope  userRange,
                               final boolean[] reverse,
                               final boolean   swapXY)
    {
        this.gridRange = gridRange;
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
            int j = i;
            if (swapXY && i<=1) {
                j = 1-j;
            }
            double scale = userRange.getLength(i) / gridRange.getLength(i);
            double offset;
            if (reverse==null || !reverse[j]) {
                offset = userRange.getMinimum(i);
            } else {
                scale  = -scale;
                offset = userRange.getMaximum(i);
            }
            offset -= scale * (gridRange.getLower(i)-0.5);
            matrix.setElement(j, i,         scale );
            matrix.setElement(j, dimension, offset);
        }
        gridToCoordinateSystem = ProjectiveTransform.create(matrix);
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
        if (gridToCoordinateSystem != null) {
            return gridToCoordinateSystem.getSourceDimensions();
        }
        return getGridRange().getDimension();
    }

    /**
     * Returns the bounding box of "real world" coordinates for this grid geometry.
     * This envelope is the {@linkplain #getGridRange grid range}
     * {@linkplain #getGridToCoordinateSystem transformed} to the "real world" coordinate system.
     *
     * @return The bounding box in "real world" coordinates.
     * @throws InvalidGridGeometryException if the envelope can't be computed.
     *
     * @see #getGridRange
     * @see #getGridToCoordinateSystem
     */
    public Envelope getEnvelope() throws InvalidGridGeometryException {
        final int dimension = getDimension();
        final GeneralEnvelope envelope = new GeneralEnvelope(dimension);
        for (int i=0; i<dimension; i++) {
            // According OpenGIS specification, GridGeometry maps pixel's center.
            // We want a bounding box for all pixels, not pixel's centers. Offset by
            // 0.5 (use -0.5 for maximum too, not +0.5, since maximum is exclusive).
            envelope.setRange(i, gridRange.getLower(i)-0.5, gridRange.getUpper(i)-0.5);
        }
        final MathTransform gridToCoordinateSystem = getGridToCoordinateSystem();
        try {
            return CRSUtilities.transform(gridToCoordinateSystem, envelope);
        } catch (TransformException exception) {
            throw new InvalidGridGeometryException(Errors.format(ErrorKeys.BAD_TRANSFORM_$1,
                    Utilities.getShortClassName(gridToCoordinateSystem)), exception);
        }
    }

    /**
     * Returns the valid coordinate range of a grid coverage. The lowest valid grid coordinate is
     * zero for {@link BufferedImage}, but may be non-zero for arbitrary {@link RenderedImage}. A
     * grid with 512 cells can have a minimum coordinate of 0 and maximum of 512, with 511 as the
     * highest valid index.
     *
     * @return The grid range (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no grid range.
     *
     * @see RenderedImage#getMinX
     * @see RenderedImage#getMinY
     * @see RenderedImage#getWidth
     * @see RenderedImage#getHeight
     */
    public GridRange getGridRange() throws InvalidGridGeometryException {
        if (gridRange != null) {
            return gridRange;
        }
        throw new InvalidGridGeometryException(Errors.format(ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
    }

    /**
     * Returns the math transform which allows for the transformations from grid coordinates to
     * real world earth coordinates. The transform is often an affine transformation. The coordinate
     * reference system of the real world coordinates is given by
     * {@link org.opengis.coverage.Coverage#getCoordinateReferenceSystem}.
     * <br><br>
     * <strong>Note:</strong> OpenGIS requires that the transform maps <em>pixel centers</em>
     * to real world coordinates. This is different from some other systems that map pixel's
     * upper left corner.
     *
     * @return The transform (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no transform.
     */
    public MathTransform getGridToCoordinateSystem() throws InvalidGridGeometryException {
        if (gridToCoordinateSystem != null) {
            return gridToCoordinateSystem;
        }
        throw new InvalidGridGeometryException(); // TODO: provide a localized message.
    }

    /**
     * Returns a hash value for this grid geometry. This value need not remain
     * consistent between different implementations of the same class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (gridToCoordinateSystem != null) {
            code += gridToCoordinateSystem.hashCode();
        }
        if (gridRange != null) {
            code += gridRange.hashCode();
        }
        return code;
    }

    /**
     * Compares the specified object with this grid geometry for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final GeneralGridGeometry that = (GeneralGridGeometry) object;
            return Utilities.equals(this.gridRange,              that.gridRange) &&
                   Utilities.equals(this.gridToCoordinateSystem, that.gridToCoordinateSystem);
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
        buffer.append(gridToCoordinateSystem);
        buffer.append(']');
        return buffer.toString();
    }
}
