/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.display.canvas.map;

// J2SE dependencies
import java.awt.Rectangle;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.go.display.canvas.Canvas;  // For javadoc
import org.opengis.go.display.canvas.map2d.Map2DState;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.display.canvas.DefaultCanvasState;


/**
 * The properties necessary to define a rectangular, 2D canvas.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultMapState extends DefaultCanvasState implements Map2DState {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1753033112812208638L;

    /**
     * The visible geographic map boundary, in objective CRS.
     */
    private final GeneralEnvelope envelope;

    /**
     * The widget bounds, in display CRS.
     */
    private final int width, height;

    /**
     * The scale of the {@link Canvas} represented by this map state.
     */
    private final double scale;

    /**
     * Creates a canvas state with the specified title and envelope. The center position
     * is infered from the envelope.
     *
     * @param title    The title of the canvas.
     * @param envelope The visible geographic map boundary, in objective CRS.
     * @param bounds   The widget bounds, in display CRS.
     * @param scale    The scale of the canvas.
     */
    public DefaultMapState(final String title, final Envelope envelope, final Rectangle bounds, final double scale) {
        super(title, envelope);
        this.envelope = new GeneralEnvelope(envelope);
        this.width    = bounds.width;
        this.height   = bounds.height;
        this.scale    = scale;
    }

    /**
     * Gets the pixel width of the {@link Canvas} represented by this map state.
     */             
    public int getPixelWidth() {
        return width;
    }

    /**
     * Gets the pixel height of the {@link Canvas} represented by this map state.
     */
    public int getPixelHeight() {
        return height;
    }

    /**
     * Gets the map width of the {@code Canvas} represented by this map state, in terms of the
     * given units.
     *
     * @param  unit The unit for the return value.
     * @return The width in terms of the given unit.
     */
    public double getMapWidth(final Unit unit) {
        return envelope.getLength(0, unit);
    }

    /**
     * Gets the map height of the {@code Canvas} represented by this map state, in terms of the
     * given units.
     *
     * @param  unit The unit for the return value.
     * @return The width in terms of the given unit.
     */
    public double getMapHeight(final Unit unit) {
        return envelope.getLength(1, unit);
    }

    /**
     * Gets the scale of the {@link Canvas} represented by this map state.
     */
    public double getScale() {
        return scale;
    }
    
    /**
     * Gets the envelope of the {@link Canvas} represented by this map state. This is the
     * <em>visible</em> geographic map boundary, in objective CRS. This envelope is scale
     * dependent.
     *
     * @see org.geotools.display.canvas.ReferencedCanvas#getEnvelope
     */
    public Envelope getEnvelope() {
        return (Envelope) envelope.clone();
    }

    /**
     * Determines if the given object is the same type of canvas state object and has
     * values equal to this one.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final DefaultMapState that = (DefaultMapState) object;
            return                  this.width  == that.width     &&
                                    this.height == that.height    &&
                   Utilities.equals(this.envelope, that.envelope) &&
                   Double.doubleToLongBits(this.scale) ==
                   Double.doubleToLongBits(that.scale);
        }
        return false;
    }
}
