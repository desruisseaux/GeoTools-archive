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
package org.geotools.display.canvas;

// J2SE dependencies
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.go.display.primitive.Graphic;

// Geotools dependencies
import org.geotools.display.event.ReferencedEvent;


/**
 * Queries a graphic property. This is used by the following methods:
 * <p>
 * <ul>
 *   <li>{@link ReferencedCanvas2D#getToolTipText}</li>
 *   <li>{@link ReferencedCanvas2D#getAction}</li>
 *   <li>{@link ReferencedCanvas2D#format}</li>
 * </ul>
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Use generic type when we will be allowed to compile for J2SE 1.5.
 */
abstract class GraphicVisitor2D extends GraphicVisitor/*<T>*/ {
    /**
     * The display coordinates.
     */
    private final double x, y;

    /**
     * Creates a visitor for the specified event.
     */
    public GraphicVisitor2D(final ReferencedEvent event) {
        final Point2D point = event.getDisplayPoint2D();
        x = point.getX();
        y = point.getY();
    }

    /**
     * Returns {@code true} if the event may applies to the specified graphic.
     */
    protected final boolean into(final ReferencedGraphic2D graphic) {
        return graphic.getDisplayBounds().contains(x,y);
    }

    /**
     * Visits the {@link ReferencedGraphic#getToolTipText} property.
     */
    static final class ToolTipText extends GraphicVisitor2D/*<String>*/ {
        public ToolTipText(final ReferencedEvent event) {
            super(event);
        }

        public Object visit(final Graphic graphic, final ReferencedEvent event) {
            if (graphic instanceof ReferencedGraphic2D) {
                if (!into((ReferencedGraphic2D) graphic)) {
                    return null;
                }
            } else if (!(graphic instanceof ReferencedGraphic)) {
                return null;
            }
            return ((ReferencedGraphic) graphic).getToolTipText(event);
        }
    }

    /**
     * Visits the {@link ReferencedGraphic#getAction} property.
     */
    static final class Action extends GraphicVisitor2D/*<Action>*/ {
        public Action(final ReferencedEvent event) {
            super(event);
        }

        public Object visit(final Graphic graphic, final ReferencedEvent event) {
            if (graphic instanceof ReferencedGraphic2D) {
                if (!into((ReferencedGraphic2D) graphic)) {
                    return null;
                }
            } else if (!(graphic instanceof ReferencedGraphic)) {
                return null;
            }
            return ((ReferencedGraphic) graphic).getAction(event);
        }
    }

    /**
     * Visits the {@link ReferencedGraphic#format} property.
     */
    static final class Format extends GraphicVisitor2D/*<Boolean>*/ {
        protected final StringBuffer buffer;

        public Format(final ReferencedEvent event, final StringBuffer buffer) {
            super(event);
            this.buffer = buffer;
        }

        public Object visit(final Graphic graphic, final ReferencedEvent event) {
            if (graphic instanceof ReferencedGraphic2D) {
                if (!into((ReferencedGraphic2D) graphic)) {
                    return null;
                }
            } else if (!(graphic instanceof ReferencedGraphic)) {
                return null;
            }
            return Boolean.valueOf(((ReferencedGraphic) graphic).format(event, buffer));
        }
    }
}
