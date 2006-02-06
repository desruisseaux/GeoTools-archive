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
import java.util.Locale;
import java.util.logging.Logger;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

// OpenGIS dependencies
import org.opengis.go.display.canvas.Canvas;


/**
 * The base class for {@linkplain AbstractCanvas canvas} and {@linkplain AbstractGraphic graphic}
 * primitives. This base class provides support for {@linkplain PropertyChangeListener property
 * change listeners}.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DisplayObject {
    /**
     * The logger for the GO implementation module.
     */
    static final Logger LOGGER = Logger.getLogger("org.geotools.display");

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain org.geotools.display.primitive.AbstractGraphic#getName graphic name} changed.
     */
    public static final String NAME_PROPERTY = "name";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractCanvas#getTitle canvas title} changed.
     */
    public static final String TITLE_PROPERTY = "title";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas#getObjectiveCRS objective CRS} changed.
     */
    public static final String OBJECTIVE_CRS_PROPERTY = "objectiveCRS";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas#getDisplayCRS device CRS} changed.
     */
    public static final String DISPLAY_CRS_PROPERTY = "displayCRS";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas#getDeviceCRS device CRS} changed.
     */
    public static final String DEVICE_CRS_PROPERTY = "deviceCRS";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractCanvas#getGraphics set of graphics} in this canvas changed.
     */
    public static final String GRAPHICS_PROPERTY = "graphics";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain org.geotools.display.primitive.AbstractGraphic#getVisible graphic visibility}
     * changed.
     */
    public static final String VISIBLE_PROPERTY = "visible";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain org.geotools.display.primitive.AbstractGraphic#getZOrderHint z order hint}
     * changed.
     */
    public static final String Z_ORDER_HINT_PROPERTY = "zOrderHint";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain ReferencedCanvas#getEnvelope canvas envelope} or
     * {@linkplain ReferencedGraphic#getEnvelope graphic envelope} changed.
     */
    public static final String ENVELOPE_PROPERTY = "envelope";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas#getScale canvas scale} changed.
     */
    public static final String SCALE_PROPERTY = "scale";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas2D#getDisplayBounds display bounds} changed.
     */
    public static final String DISPLAY_BOUNDS_PROPERTY = "displayBounds";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain org.geotools.display.primitive.AbstractGraphic#getParent graphic parent} changed.
     */
    public static final String PARENT_PROPERTY = "parent";

    /**
     * Listeners to be notified about any changes in this canvas properties.
     */
    protected final PropertyChangeSupport listeners;

    /**
     * The canvas that own this graphic, or {@code null} if none. This field is set automatically
     * by {@link AbstractCanvas#add}. Users should not modify this field directly.
     */
    Canvas owner;

    /**
     * {@code true} if this canvas or graphic has {@value #SCALE_PROPERTY} properties listeners.
     * Used in order to reduce the amount of {@link PropertyChangeEvent} objects created in the
     * common case where no listener have interest in this property. This optimisation may be
     * worth since a {@value #SCALE_PROPERTY} property change event is sent for every graphics
     * everytime a zoom change.
     * <p>
     * This field is read only by {@link ReferencedCanvas#setScale}.
     *
     * @see #listenersChanged
     */
    boolean hasScaleListeners;

    /**
     * Creates a new instance of display object.
     */
    public DisplayObject() {
        this.listeners = new PropertyChangeSupport(this);
    }

    /**
     * Adds a property change listener to the listener list. The listener is registered
     * for all properties. For example, {@linkplain AbstractCanvas#add adding} or
     * {@linkplain AbstractCanvas#remove removing} graphics in a canvas may fire
     * {@value #GRAPHICS_PROPERTY} change events and, indirectly, some other side-effect
     * events like {@value #ENVELOPE_PROPERTY}.
     *
     * @param listener The property change listener to be added
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        synchronized (listeners) {
            listeners.addPropertyChangeListener(listener);
            listenersChanged();
        }
    }

    /**
     * Adds a property change listener for a specific property.
     * The listener will be invoked only when that specific property changes.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The property change listener to be added.
     */
    public void addPropertyChangeListener(final String propertyName,
                                          final PropertyChangeListener listener)
    {
        synchronized (listeners) {
            listeners.addPropertyChangeListener(propertyName, listener);
            listenersChanged();
        }
    }

    /**
     * Removes a property change listener from the listener list. This removes a listener
     * that was registered for all properties.
     *
     * @param listener The property change listener to be removed
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        synchronized (listeners) {
            listeners.removePropertyChangeListener(listener);
            listenersChanged();
        }
    }

    /**
     * Remove a property change listener for a specific property.
     *
     * @param propertyName The name of the property that was listened on.
     * @param listener     The property change listener to be removed.
     */
    public void removePropertyChangeListener(final String propertyName,
                                             final PropertyChangeListener listener)
    {
        synchronized (listeners) {
            listeners.removePropertyChangeListener(propertyName, listener);
            listenersChanged();
        }
    }

    /**
     * Invoked when a property change listener has been {@linkplain #addPropertyChangeListener
     * added} or {@linkplain #removePropertyChangeListener removed}.
     */
    protected void listenersChanged() {
        hasScaleListeners = listeners.hasListeners(SCALE_PROPERTY);
    }

    /**
     * Returns the locale for this object.
     */
    public Locale getLocale() {
        final Canvas owner = getCanvas();
        if (owner instanceof DisplayObject) {
            return ((DisplayObject) owner).getLocale();
        }
        return Locale.getDefault();
    }

    /**
     * Returns the logger for all messages to be logged by Geotools implementation of GO-1.
     */
    protected Logger getLogger() {
        final Canvas owner = getCanvas();
        if (owner instanceof DisplayObject) {
            return ((DisplayObject) owner).getLogger();
        }
        return LOGGER;
    }

    /**
     * Returns the lock for synchronisation.
     */
    protected final Object getTreeLock() {
        final Canvas owner = this.owner;
        return (owner!=null) ? (Object)owner : (Object)this;
    }

    /**
     * If this display object is part of a canvas, returns the canvas that own it.
     * Otherwise, returns {@code null}.
     */
    protected final Canvas getCanvas() {
        return owner;
    }

    /**
     * Creates a new {@code DisplayObject} of the same type as this object.
     * <p>
     * By default, {@code DisplayObject} are not cloneable. Subclasses need to implement the
     * {@link Cloneable} interface if they support cloning.
     *
     * @return The cloned object.
     * @throws CloneNotSupportedException if this object is not cloneable.
     */
    protected Object clone() throws CloneNotSupportedException {
        assert Thread.holdsLock(getTreeLock());
        final DisplayObject clone = (DisplayObject) super.clone();
        clone.owner = null;
        return clone;
    }

    /**
     * Clears all cached data. Invoking this method may help to release some resources for other
     * applications. It should be invoked when we know that the map is not going to be rendered
     * for a while. For example it may be invoked from {@link java.applet.Applet#stop}. Note
     * that this method doesn't changes the renderer setting; it will just slow down the first
     * rendering after this method call.
     *
     * @see #dispose
     */
    protected void clearCache() {
    }

    /**
     * Method that can be called when an object is no longer needed. Implementations may use
     * this method to release resources, if needed. Implementations may also implement this
     * method to return an object to an object pool. It is an error to reference a
     * {@code Graphic} or {@code Canvas} in any way after its dispose method has been called.
     */
    public void dispose() {
        synchronized (getTreeLock()) {
            clearCache();
            final PropertyChangeListener[] list = listeners.getPropertyChangeListeners();
            for (int i=list.length; --i>=0;) {
                listeners.removePropertyChangeListener(list[i]);
            }
        }
    }
}
