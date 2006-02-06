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

// OpenGIS dependencies
import org.opengis.go.display.primitive.Graphic;

// Geotools dependencies
import org.geotools.display.event.ReferencedEvent;
import org.geotools.display.primitive.ReferencedGraphic;


/**
 * Queries a graphic property. This is used by the following methods:
 * <p>
 * <ul>
 *   <li>{@link ReferencedCanvas#getToolTipText}</li>
 *   <li>{@link ReferencedCanvas#getAction}</li>
 *   <li>{@link ReferencedCanvas#format}</li>
 * </ul>
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Use generic type when we will be allowed to compile for J2SE 1.5.
 */
abstract class GraphicVisitor {
    /**
     * Visits the specified graphic for the specified event.
     */
    public abstract Object/*<T>*/ visit(Graphic graphic, ReferencedEvent event);

    /**
     * Visits the {@link ReferencedGraphic#getToolTipText} property.
     */
    static final class ToolTipText extends GraphicVisitor/*<String>*/ {
        public static final ToolTipText SHARED = new ToolTipText();

        public Object visit(final Graphic graphic, final ReferencedEvent event) {
            if (graphic instanceof ReferencedGraphic) {
                return ((ReferencedGraphic) graphic).getToolTipText(event);
            }
            return null;
        }
    }

    /**
     * Visits the {@link ReferencedGraphic#getAction} property.
     */
    static final class Action extends GraphicVisitor/*<Action>*/ {
        public static final Action SHARED = new Action();

        public Object visit(final Graphic graphic, final ReferencedEvent event) {
            if (graphic instanceof ReferencedGraphic) {
                return ((ReferencedGraphic) graphic).getAction(event);
            }
            return null;
        }
    }

    /**
     * Visits the {@link ReferencedGraphic#format} property.
     */
    static final class Format extends GraphicVisitor/*<Boolean>*/ {
        private final StringBuffer buffer;

        public Format(final StringBuffer buffer) {
            this.buffer = buffer;
        }

        public Object visit(final Graphic graphic, final ReferencedEvent event) {
            if (graphic instanceof ReferencedGraphic) {
                return Boolean.valueOf(((ReferencedGraphic) graphic).format(event, buffer));
            }
            return null;
        }
    }
}
