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
 */
package org.geotools.display.event;

// J2SE dependencies
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.spatialschema.geometry.DirectPosition;


/**
 * Common interface for events corresponding in some geographic location. They are typically mouse
 * events with {@linkplain org.geotools.display.canvas.ReferencedCanvas#getDisplayToObjectiveTransform
 * display to objective transform} capabilities.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface ReferencedEvent {
    /**
     * Returns the mouse's position in terms of
     * {@linkplain org.geotools.display.canvas.ReferencedCanvas#getDisplayCRS display CRS}.
     * This method is similar to {@link java.awt.event.MouseEvent#getPoint()} except that
     * the mouse location is corrected for deformations caused by some artifacts like the
     * {@linkplain org.geotools.gui.swing.ZoomPane#setMagnifierVisible magnifying glass}.
     */
    DirectPosition getDisplayPosition();

    /**
     * Returns the mouse's position in terms of
     * {@linkplain org.geotools.display.canvas.ReferencedCanvas#getObjectiveCRS objective CRS}.
     */
    DirectPosition getObjectivePosition();

    /**
     * Returns the {@linkplain #getDisplayPosition display position} as a two-dimensional point.
     * If the display position has more than two dimensions, only the two first ones are returned.
     */
    Point2D getDisplayPoint2D();

    /**
     * Returns the {@linkplain #getObjectivePosition objective position} as a two-dimensional point.
     * If the objective position has more than two dimensions, only the two first ones are returned.
     */
    Point2D getObjectivePoint2D();
}
