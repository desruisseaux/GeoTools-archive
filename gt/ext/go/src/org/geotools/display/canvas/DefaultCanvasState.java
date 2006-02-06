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
package org.geotools.display.canvas;

// J2SE dependencies
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.util.Cloneable;
import org.opengis.go.display.canvas.Canvas;        // For javadoc
import org.opengis.go.display.canvas.CanvasState;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.measure.CoordinateFormat;
import org.geotools.geometry.GeneralDirectPosition;


/**
 * Describes the current state of a {@link Canvas}. The information contained by instances
 * of this interface should only describe the viewing area or volume of the canvas and should
 * not contain any state information regarding the data contained within it.
 * <p>
 * When an instance of this class is returned from {@link Canvas} methods, a "snapshot" of the
 * current state of the canvas is taken and the values will never change (even if the canvas
 * changes state).
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultCanvasState implements CanvasState, Cloneable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8473318790311715748L;

    /**
     * The title of the canvas.
     */
    private final String title;

    /**
     * The position of the center pixel of the canvas.
     */
    private final GeneralDirectPosition center;

    /**
     * Creates a canvas state with the specified title and center position.
     *
     * @param title  The title of the canvas.
     * @param center The position of the center pixel of the canvas.
     */
    public DefaultCanvasState(final String title, final DirectPosition center) {
        this.title  = title;
        this.center = (center!=null) ? new GeneralDirectPosition(center) : null;
    }

    /**
     * Creates a canvas state with the specified title and envelope. The center position
     * is infered from the envelope.
     *
     * @param title    The title of the canvas.
     * @param envelope The canvas envelope.
     */
    public DefaultCanvasState(final String title, final Envelope envelope) {
        this.title = title;
        if (envelope != null) {
            final int dimension = envelope.getDimension();
            center = new GeneralDirectPosition(dimension);
            // TODO: for the next line, use a more direct way if GeoAPI 2.1 alows that.
            center.setCoordinateReferenceSystem(envelope.getLowerCorner().getCoordinateReferenceSystem());
            for (int i=0; i<dimension; i++) {
                center.ordinates[i] = envelope.getCenter(i);
            }
        } else {
            center = null;
        }
    }

    /**
     * Returns the title of the canvas.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the position of the center pixel of the canvas.
     */
    public DirectPosition getCenter() {
        return (center!=null) ? new GeneralDirectPosition(center) : center;
    }

    /**
     * Returns a hash code value for this canvas state.
     */
    public int hashCode() {
        int code = (int) serialVersionUID;
        if (title  != null) code ^= title .hashCode();
        if (center != null) code ^= center.hashCode();
        return code;
    }

    /**
     * Determines if the given object is the same type of canvas state object and has
     * values equal to this one.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final DefaultCanvasState that = (DefaultCanvasState) object;
            return Utilities.equals(this.title,  that.title ) &&
                   Utilities.equals(this.center, that.center);
        }
        return false;
    }

    /**
     * Returns a copy of this canvas state.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen, since we are cloneable.
            throw new AssertionError(e);
        }
    }

    /**
     * Returns a string representation of this canvas state.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        if (title != null) {
            buffer.append('"');
            buffer.append(title);
            buffer.append('"');
        }
        if (center != null) {
            buffer.append(", (");
            buffer.append(center);
            buffer.append(')');
        }
        buffer.append(']');
        return buffer.toString();
    }
}
