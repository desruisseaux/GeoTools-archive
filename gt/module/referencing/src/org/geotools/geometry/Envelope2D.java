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

// OpenGIS dependencies
import org.opengis.util.Cloneable;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A two-dimensional envelope.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public class Envelope2D extends Rectangle2D.Double implements Envelope, Cloneable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3319231220761419350L;
    
    /**
     * The coordinate reference system, or {@code null}.
     */
    private CoordinateReferenceSystem crs;

    /**
     * Constructs two-dimensional envelope defined by an other {@link Envelope}.
     */
    public Envelope2D(final Envelope envelope) {
        super(envelope.getMinimum(0), envelope.getMinimum(1),
              envelope.getLength (0), envelope.getLength (1));

        // TODO: check below should be first, if only Sun could fix RFE #4093999.
        final int dimension = envelope.getDimension();
        if (dimension != 2) {
            throw new IllegalStateException(Errors.format(ErrorKeys.NOT_TWO_DIMENSIONAL_$1,
                                            new Integer(dimension)));
        }
        // TODO: Code below would be simplier if 'getCoordinateReferenceSystem()'
        //       method was defined right into the 'Envelope' interface.
        if (envelope instanceof Envelope2D) {
            crs = ((Envelope2D) envelope).getCoordinateReferenceSystem();
        } else if (envelope instanceof GeneralEnvelope) {
            crs = ((GeneralEnvelope) envelope).getCoordinateReferenceSystem();
        } else {
            crs = envelope.getLowerCorner().getCoordinateReferenceSystem();
        }
        setCoordinateReferenceSystem(crs); // Paranoiac check.
    }

    /**
     * Constructs two-dimensional envelope defined by an other {@link Rectangle2D}.
     */
    public Envelope2D(final CoordinateReferenceSystem crs, final Rectangle2D rect) {
        super(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        setCoordinateReferenceSystem(crs);
    }

    /**
     * Constructs two-dimensional envelope defined by the specified coordinates.
     */
    public Envelope2D(final CoordinateReferenceSystem crs,
                      final double x, final double y, final double width, final double height)
    {
        super(x, y, width, height);
        setCoordinateReferenceSystem(crs);
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
        return 2;
    }

    /**
     * A coordinate position consisting of all the minimal ordinates for each
     * dimension for all points within the <code>Envelope</code>.
     *
     * @return The lower corner.
     */
    public DirectPosition getLowerCorner() {
        return new DirectPosition2D(crs, getMinX(), getMinY());
    }

    /**
     * A coordinate position consisting of all the maximal ordinates for each
     * dimension for all points within the <code>Envelope</code>.
     *
     * @return The upper corner.
     */
    public DirectPosition getUpperCorner() {
        return new DirectPosition2D(crs, getMaxX(), getMaxY());
    }
    
    /**
     * Returns the minimal ordinate along the specified dimension.
     */
    public final double getMinimum(final int dimension) {
        switch (dimension) {
            case 0:  return getMinX();
            case 1:  return getMinY();
            default: throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Returns the maximal ordinate along the specified dimension.
     */
    public final double getMaximum(final int dimension) {
        switch (dimension) {
            case 0:  return getMaxX();
            case 1:  return getMaxY();
            default: throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Returns the center ordinate along the specified dimension.
     */
    public final double getCenter(final int dimension) {
        switch (dimension) {
            case 0:  return getCenterX();
            case 1:  return getCenterY();
            default: throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Returns the envelope length along the specified dimension.
     * This length is equals to the maximum ordinate minus the
     * minimal ordinate.
     */
    public final double getLength(final int dimension) {
        switch (dimension) {
            case 0:  return getWidth ();
            case 1:  return getHeight();
            default: throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Returns a hash value for this envelope.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code = super.hashCode() ^ (int)serialVersionUID;
        if (crs != null) {
            code ^= crs.hashCode();
        }
        return code;
    }
    
    /**
     * Compares the specified object with this envelope for equality.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final Envelope2D that = (Envelope2D) object;
            return Utilities.equals(this.crs, that.crs);
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code this} envelope bounds is equals to {@code that} envelope
     * bounds in two specified dimensions. The coordinate reference system is not compared, since
     * it doesn't need to have the same number of dimensions.
     *
     * @param that The envelope to compare to.
     * @param xDim The dimension of {@code that} envelope to compare to the <var>x</var> dimension
     *             of {@code this} envelope.
     * @param yDim The dimension of {@code that} envelope to compare to the <var>y</var> dimension
     *             of {@code this} envelope.
     * @param eps  A small tolerance number for floating point number comparaisons. This value will
     *             be scaled according this envelope {@linkplain #width width} and
     *             {@linkplain #height height}.
     * @return {@code true} if the envelope bounds are the same (up to the specified tolerance
     *         level) in the specified dimensions, or {@code false} otherwise.
     */
    public boolean boundsEquals(final Envelope that, final int xDim, final int yDim, double eps) {
        eps *= 0.5*(width + height);
        for (int i=0; i<4; i++) {
            final int dim2D = (i & 1);
            final int dimND = (dim2D == 0) ? xDim : yDim;
            final double value2D, valueND;
            if ((i & 2) == 0) {
                value2D = this.getMinimum(dim2D);
                valueND = that.getMinimum(dimND);
            } else {
                value2D = this.getMaximum(dim2D);
                valueND = that.getMaximum(dimND);
            }
            // Use '!' for catching NaN values.
            if (!(Math.abs(value2D - valueND) <= eps)) {
                return false;
            }
        }
        return true;
    }
}
