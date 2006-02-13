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
package org.geotools.display.style;

// J2SE dependencies
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.RenderingHints;

// OpenGIS dependencies
import org.opengis.go.display.primitive.Graphic;  // For javadoc
import org.opengis.go.display.style.GraphicStyle;
import org.opengis.go.display.style.event.GraphicStyleListener;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.display.canvas.DisplayObject;


/**
 * Base classe for the collection of drawing attributes that are applied to a {@link Graphic}.
 * Subclasses provide attributes for specifying SLD-based line symbolizer, polygon symbolizer,
 * point symbolizer, text symbolizer.  Attributes common to all types of geometry, related to
 * viewability, editability, and highlighting, are contained in {@code Graphic}.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DefaultGraphicStyle extends DisplayObject implements GraphicStyle {
    /**
     * Map of the implementation-specific hint identified by a rendering hint key.
     */
    private Hints hints;

    /**
     * List of the registered graphic style listeners.
     */
    private List graphicStyleListeners;
    
    /**
     * Creates a default instance of graphic style.
     */
    public DefaultGraphicStyle() {
        hints                   = new Hints(null);
        graphicStyleListeners   = new ArrayList();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Object getRenderingHint(final RenderingHints.Key key) {
        return hints.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setRenderingHint(final RenderingHints.Key key, final Object value) {
        if (value != null) {
            hints.put(key, value);
        } else {
            hints.remove(key);
        }
    }

    /**
     * Registers the given object as a listener to receive events when the
     * properties of this style have changed.
     */
    public synchronized void addGraphicStyleListener(final GraphicStyleListener listener) {
        graphicStyleListeners.add(listener);
    }
    
    /**
     * For a listener that was previously added using the {@link #addGraphicStyleListener
     * addGraphicStyleListener} method, de-registers it so that it will no longer receive
     * events when the properties of this style have changed.
     */
    public synchronized void removeGraphicStyleListener(final GraphicStyleListener listener) {
        graphicStyleListeners.remove(listener);
    }

    /**
     * Ensures that the color alpha channel has the specified opacity.
     */
    static Color fixAlphaChannel(Color color, final float opacity) {
        final int alpha = Math.max(0, Math.min(255, Math.round(256*opacity))) << 24;
        final int RGB   = color.getRGB();
        if ((RGB & 0xFF000000) != alpha) {
            color = new Color((RGB & 0x00FFFFFF) | alpha, true);
        }
        return color;
    }

    /**
     * Sets the properties of this {@code GraphicStyle} from the properties of the specified
     * {@code GraphicStyle}.  May throw an exception if the given object is not the same type
     * as this one.
     * <p>
     * The default implementation do not set the listeners neither the implementation hints.
     */
    public void setPropertiesFrom(final GraphicStyle graphicStyle) {
    }

    /**
     * Returns a shallow copy of this object. This means that all of the subordinate objects
     * referenced by this object will also be referenced by the result. These objects include
     * the values for {@linkplain #getImplHint implementation hints}, <cite>etc.</cite>
     */
    public Object clone() {
        final DefaultGraphicStyle clone;
        try {
            clone = (DefaultGraphicStyle) super.clone();
        } catch (CloneNotSupportedException exception) {
            // Should never happen since we are cloneable.
            throw new AssertionError(exception);
        }
        clone.hints = (Hints) hints.clone();
        clone.graphicStyleListeners = new ArrayList(graphicStyleListeners);
        return clone;
    }
}
