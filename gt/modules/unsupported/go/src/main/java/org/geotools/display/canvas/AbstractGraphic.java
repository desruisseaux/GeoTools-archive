/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */

package org.geotools.display.canvas;

// J2SE dependencies
import java.util.Locale;
import java.util.logging.Logger;
import java.text.NumberFormat;
import java.text.FieldPosition;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// OpenGIS dependencies
import org.opengis.go.display.canvas.Canvas;
import org.opengis.go.display.primitive.Graphic;
import org.opengis.go.display.event.GraphicEvent;
import org.opengis.go.display.event.GraphicListener;
import org.opengis.go.display.style.GraphicStyle;
import org.opengis.go.display.style.Symbology;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * The root abstraction of a graphic object taxonomy, specifying the methods common to a
 * lightweight set of graphic objects.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractGraphic extends DisplayObject implements Graphic {
    /**
     * The default {@linkplain #getZOrderHint z-order}.
     */
    private static final double DEFAULT_Z_ORDER = Double.POSITIVE_INFINITY;

    /**
     * The format used during the last call to {@link #getName}. We use only one instance for
     * all graphics, since an application is likely to use only one locale. However, more locales
     * are allowed; it will just be slower.
     */
    private static Format format;

    /**
     * Convenience class for {@link RenderedLayer#getName}.
     * This class should be immutable and thread-safe.
     */
    private static final class Format {
        /** The locale of the {@link #format}. */
        public final Locale locale;

        /** The format in the {@link #locale}. */
        public final NumberFormat format;

        /** Construct a format for the given locale. */
        public Format(final Locale locale) {
            this.locale = locale;
            this.format = NumberFormat.getNumberInstance(locale);
        }
    }

    /**
     * The canvas that own this graphic, or {@code null} if none.
     */
    private Canvas canvas;

    /**
     * The name assigned to this graphic.
     */
    private String name;

    /**
     * The parent of this graphic, or {@code null} if none.
     */
    private Graphic parent;

    /**
     * Tells if this graphic is visible.
     *
     * @see #setVisible
     */
    private boolean visible = true;

    /**
     * The z value for this graphic.
     *
     * @see #getZOrderHint
     * @see #setZOrderHint
     */
    private double zOrder = DEFAULT_Z_ORDER;

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
     * Creates a new graphic. The {@linkplain #getZOrderHint z-order} default to positive infinity
     * (i.e. this graphic is drawn on top of everything else). Subclasses should invokes setters
     * methods in order to define properly this graphic properties.
     */
    protected AbstractGraphic() {
    }

    /**
     * If this display object is contained in a canvas, returns the canvas that own it.
     * Otherwise, returns {@code null}.
     */
    final Canvas getCanvas() {
        return canvas;
    }

    /**
     * Set the canvas to the specified value. Used by {@link AbstractCanvas} only.
     */
    final void setCanvas(final Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Returns the locale for this object. If this graphic is contained in a
     * {@linkplain AbstractCanvas canvas}, then the default implementation returns the canvas
     * locale. Otherwise, this method returns the {@linkplain Locale#getDefault system locale}.
     */
    public Locale getLocale() {
        final Canvas canvas = getCanvas();
        if (canvas instanceof DisplayObject) {
            return ((DisplayObject) canvas).getLocale();
        }
        return super.getLocale();
    }

    /**
     * Returns the name assigned to this {@code Graphic}. If no name were
     * {@linkplain #setName explicitly set}, then this method returns a default
     * name built from the {@linkplain #getZOrderHint z order}.
     */
    public String getName() {
        final String name = this.name;  // Avoid the need for synchronization.
        if (name != null) {
            return name;
        }
        final Locale locale = getLocale();
        Format f = format; // Avoid the need for synchronization.
        if (f==null || !f.locale.equals(locale)) {
            format = f = new Format(locale);
        }
        final StringBuffer buffer = new StringBuffer("z=");
        return f.format.format(getZOrderHint(), buffer, new FieldPosition(0)).toString();
    }

    /**
     * Sets the name of this {@code Graphic} to the given value.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#NAME_PROPERTY}
     * property change event.
     */
    public void setName(final String name) {
        final String old;
        synchronized (getTreeLock()) {
            old = this.name;
            this.name = name;
            listeners.firePropertyChange(NAME_PROPERTY, old, name);
        }
    }

    /**
     * Returns the parent of this {@code Graphic}, or {@code null} if none. Usually, only 
     * {@link org.opengis.go.display.primitive.AggregateGraphic}s have {@code Graphic} children.
     */
    public Graphic getParent() {
        return parent;
    }

    /**
     * Sets the parent of this {@code Graphic}.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#PARENT_PROPERTY}
     * property change event.
     */
    public void setParent(final Graphic parent) {
        final Graphic old;
        synchronized (getTreeLock()) {
            old = this.parent;
            this.parent = parent;
            listeners.firePropertyChange(PARENT_PROPERTY, old, name);
        }
    }

    /**
     * Sets the graphic style for this {@code Graphic}.
     * {@code Graphic}s may share style property objects with other {@code Graphic}s.
     *
     * @todo Not yet implemented.
     */
    public void setGraphicStyle(final GraphicStyle style) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the graphic style for this {@code Graphic}. Implementations should return a reference
     * to their internal {@code GraphicStyle} object, so that users may modify this
     * {@code Graphic}'s style properties directly through the style object.
     *
     * @todo Not yet implemented.
     */
    public GraphicStyle getGraphicStyle() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the value of the property with the specified key. Only properties added with
     * {@link #putClientProperty putClientProperty} will return a non-null value.
     *
     * @return the value of this property or null
     * @see #putClientProperty
     *
     * @todo Not yet implemented.
     */
    public Object getClientProperty(Object key) {
        return null;
    }

    /**
     * Adds an arbitrary key/value "client property" to this {@code Graphic}. The
     * {@code get}/{@code putClientProperty} methods provide access to a small
     * per-instance table. Callers can use {@code get}/{@code putClientProperty} to
     * annotate {@code Graphics} that were created by another module.
     * <p>
     * If value is null this method will remove the property. Changes to client properties are
     * reported with {@link PropertyChangeEvent}s. The name of the property (for the sake of
     * {@link PropertyChangeEvent}s) is {@code key.toString()}. The {@code clientProperty}
     * dictionary is not intended to support large scale extensions to {@code Graphic} nor
     * should be it considered an alternative to subclassing when designing a new component.
     *
     * @param key the Object containing the key string.
     * @param value the Object that is the client data.
     * @see #getClientProperty
     *
     * @todo Not yet implemented.
     */
    public void putClientProperty(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a boolean indicating whether mouse events on this {@code Graphic} should be passed to
     * the parent {@code Graphic} in addition to being passed to any listeners on this object.
     * The default is false, indicating that events will not be passed to the parent.  If the
     * boolean is true, then the event will be passed to the parent after having been passed
     * to the listeners on this object.
     *
     * @param passToParent {@code true} if events should be passed to the
     *        parent graphic, {@code false} if they should not.
     *
     * @todo Not yet implemented.
     */
    public void setPassingEventsToParent(boolean passToParent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a boolean indicating whether mouse events on this {@code Graphic} will
     * be passed to the parent {@code Graphic} in addition to being passed to any
     * listeners on this object.  The default is {@code false}, indicating that events
     * will not be passed to the parent.  If the boolean is {@code true}, then the
     * event will be passed to the parent after having been passed to the
     * listeners on this object.
     *
     * @return {@code true} if this graphic pass the events to the parent graphic.
     *
     * @todo Not yet implemented.
     */
    public boolean isPassingEventsToParent() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a boolean flag specifying whether this object is to show its edit handles.
     * Edit handles are the small boxes that appear on the end of a line segment or on
     * the four corners of a box that a users selects to edit this object.
     *
     * @param showingHandles {@code true} if this object show its edit handles.
     *
     * @todo Not yet implemented.
     */
    public void setShowingEditHandles(final boolean showingHandles) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the boolean flag that specifies whether this object is showing
     * its edit handles.
     *
     * @return {@code true} means it is showing its handles.
     *
     * @todo Not yet implemented.
     */
    public boolean isShowingEditHandles() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a boolean flag indicating whether this object is to show anchor handles.
     * Anchor handles allow the object to be moved in the display.
     *
     * @todo Not yet implemented.
     */
    public void setShowingAnchorHandles(boolean showingHandles) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the boolean flag that indicates whether this object is showing anchor handles.
     * Anchor handles allow the object to be moved in the display.
     *
     * @todo Not yet implemented.
     */
    public boolean isShowingAnchorHandles() {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds the given {@code GraphicListener} to this {@code Graphic}'s list of listeners.
     * {@code GraphicListener}s are notified of key, mouse, and change events that affect
     * this {@code Graphic}.
     *
     * @todo Not yet implemented.
     */
    public void addGraphicListener(GraphicListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the given {@code GraphicListener} from this {@code Graphic}'s list of listeners.  
     *
     * @todo Not yet implemented.
     */
    public void removeGraphicListener(GraphicListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Calls the graphic event method of all {@code GraphicListener}s in this {@code Graphic}'s
     * list of listeners. The listeners need to determine which subclassed event is called and
     * what event-specific action was taken.
     *
     * @todo Not yet implemented.
     * @todo Usually, this kind of method is a protected one in the implementation class,
     *       not a public method in the interface...
     */
    public void fireGraphicEvent(final GraphicEvent event) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the auto edit value.
     *
     * @todo Not yet implemented.
     */
    public boolean getAutoEdit() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the auto edit value.
     *
     * @todo Not yet implemented.
     */
    public void setAutoEdit(boolean autoEdit) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the drag selectable value.
     *
     * @todo Not yet implemented.
     */
    public boolean getDragSelectable() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the drag selectable value.
     *
     * @todo Not yet implemented.
     */
    public void setDragSelectable(boolean dragSelectable) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the pickable value.
     *
     * @todo Not yet implemented.
     */
    public boolean getPickable() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the pickable value.
     *
     * @todo Not yet implemented.
     */
    public void setPickable(boolean pickable) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the selected value.
     *
     * @todo Not yet implemented.
     */
    public boolean getSelected() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the selected value.
     *
     * @todo Not yet implemented.
     */
    public void setSelected(boolean selected) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the blinking value.
     *
     * @todo Not yet implemented.
     */
    public boolean getBlinking() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the blinking value.
     *
     * @todo Not yet implemented.
     */
    public void setBlinking(boolean blinking) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the blink pattern value.
     *
     * @todo Not yet implemented.
     */
    public float[] getBlinkPattern() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the blink pattern value.
     *
     * @todo Not yet implemented.
     */
    public void setBlinkPattern(float[] blinkPattern) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a symbology object that will be used to override some, if not
     * all, of the graphical properties of {@code Graphics} using this style.
     * This will be null by default.
     *
     * @todo Not yet implemented.
     */
    public Symbology getSymbology() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the symbology object that will be used to override some, if not
     * all, of the graphical properties of {@code Graphics} using this style.
     *
     * @todo Not yet implemented.
     */
    public void setSymbology(Symbology symbology) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the max scale value.
     *
     * @todo Not yet implemented.
     */
    public double getMaxScale() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the max scale value.
     *
     * @todo Not yet implemented.
     */
    public void setMaxScale(double maxScale) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the min scale value.
     *
     * @todo Not yet implemented.
     */
    public double getMinScale() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the min scale value.
     *
     * @todo Not yet implemented.
     */
    public void setMinScale(double minScale) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the <var>z</var> order hint value for this graphic. Graphics with highest
     * <var>z</var> order will be painted on top of graphics with lowest <var>z</var> order.
     * The default value is {@link Double#POSITIVE_INFINITY}.
     */
    public double getZOrderHint() {
        synchronized (getTreeLock()) {
            return zOrder;
        }
    }

    /**
     * Sets the <var>z</var> order hint value for this graphic. Graphics with highest
     * <var>z</var> order will be painted on top of graphics with lowest <var>z</var> order.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#Z_ORDER_HINT_PROPERTY}
     * property change event.
     */
    public void setZOrderHint(final double zOrderHint) {
        if (Double.isNaN(zOrderHint)) {
            throw new IllegalArgumentException(Errors.getResources(getLocale()).getString(
                      ErrorKeys.ILLEGAL_ARGUMENT_$2, "zOrderHint", new Double(zOrderHint)));
        }
        final double oldZOrder;
        synchronized (getTreeLock()) {
            oldZOrder = this.zOrder;
            if (zOrderHint == oldZOrder) {
                return;
            }
            this.zOrder = zOrderHint;
            refresh();
            // TODO: Autoboxing with J2SE 1.5.
            listeners.firePropertyChange(Z_ORDER_HINT_PROPERTY,
                                         new Double(oldZOrder), new Double(zOrderHint));
        }
    }

    /**
     * Determines whether this graphic should be visible when its {@linkplain #getCanvas canvas}
     * is visible. The default value is {@code true}.
     *
     * @return {@code true} if the graphic is visible, {@code false} otherwise.
     */
    public boolean getVisible() {
        return visible;
    }

    /**
     * Sets the visible value. This method may be invoked when the user wants to hide momentarily
     * this graphic.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#VISIBLE_PROPERTY}
     * property change event.
     */
    public void setVisible(final boolean visible) {
        synchronized (getTreeLock()) {
            if (visible == this.visible) {
                return;
            }
            this.visible = visible;
            refresh();
            listeners.firePropertyChange(VISIBLE_PROPERTY, !visible, visible);
        }
    }

    /**
     * Flags this {@code Graphic} object as needing to be redrawn, due to changes to
     * the internal data of the object which affect the rendering of the object.
     * <p>
     * The actual flag set/unset mechanism is implementation-specific. The implementation
     * also choses the manner and timing in which both the flag is checked and the
     * {@code Graphic} object is redrawn.
     * <p>
     * An application would call this method when any geometric information for this
     * {@code Graphic} object has changed; for example, when the underlying {@code Geometry}
     * instance is changed or data in that instance has changed.
     * <p>
     * The default implementation does nothing.
     */
    public void refresh() {
    }

    /**
     * Creates a new {@code Graphic} of the same type as this object. The default implementation
     * invokes {@link #clone}. If the later throws a {@link CloneNotSupportedException}, then the
     * checked exception is wrapped in an unchecked one.
     * <p>
     * By default, {@code AbstractGraphic} are not cloneable. Subclasses need to implements the
     * {@link Cloneable} interface if they support cloning.
     *
     * @return The cloned graphic.
     * @throws IllegalStateException if this graphic is not cloneable.
     */
    public final Graphic cloneGraphic() throws IllegalStateException {
        synchronized (getTreeLock()) {
            try {
                return (Graphic) clone();
            } catch (CloneNotSupportedException exception) {
                throw new IllegalStateException(exception.getLocalizedMessage());
                // TODO: bundle the cause when we will be allowed to compile for J2SE 1.5.
            }
        }
    }

    /**
     * Creates a new {@code Graphic} of the same type as this object. The resulting object should be
     * identical in all respects to the original, except the {@linkplain #getParent parent attribute}
     * which is set to {@code null}.
     * <p>
     * By default, {@code AbstractGraphic} are not cloneable. Subclasses need to implement the
     * {@link Cloneable} interface if they support cloning.
     *
     * @return The cloned graphic.
     * @throws CloneNotSupportedException if this graphic is not cloneable.
     */
    protected Object clone() throws CloneNotSupportedException {
        assert Thread.holdsLock(getTreeLock());
        final AbstractGraphic clone = (AbstractGraphic) super.clone();
        clone.canvas = null;
        clone.parent = null;
        return clone;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        synchronized (getTreeLock()) {
            final Canvas canvas = getCanvas();
            if (canvas != null) {
                canvas.remove(this);
            }
            super.dispose();
        }
    }

    /**
     * Invoked when a property change listener has been {@linkplain #addPropertyChangeListener
     * added} or {@linkplain #removePropertyChangeListener removed}.
     */
    @Override
    void listenersChanged() {
        super.listenersChanged();
        hasScaleListeners = hasListeners(SCALE_PROPERTY);
    }

    /**
     * Check if there are any listeners for a specific property, <strong>excluding</strong>
     * the {@link AbstractCanvas} listener proxy. This is used for avoiding notifications
     * for {@link #SCALE_PROPERTY} and {@link #DISPLAY_BOUNDS_PROPERTY}, which are set by
     * {@link AbstractCanvas} subclasses. We take the trouble to make this optimisation
     * because the two above-cited events are fired everytime the zoom change.
     */
    final boolean hasListeners(final String property) {
        if (listeners.hasListeners(property)) {
            final PropertyChangeListener[] list = listeners.getPropertyChangeListeners();
            for (int i=0; i<list.length; i++) {
                if (list[i] != AbstractCanvas.PROPERTIES_LISTENER) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the logger for all messages to be logged by the Geotools implementation of GO-1. If
     * this object is a {@linkplain Graphic graphic} which is contained in a {@linkplain Canvas
     * canvas}, then the default implementation returns the canvas logger. Otherwise, this method
     * returns a default one.
     */
    protected Logger getLogger() {
        final Canvas canvas = getCanvas();
        if (canvas instanceof DisplayObject) {
            return ((DisplayObject) canvas).getLogger();
        }
        return super.getLogger();
    }

    /**
     * Returns the lock for synchronisation. If this object is contained in a canvas,
     * then this method returns the same lock than the canvas.
     */
    protected final Object getTreeLock() {
        final Canvas canvas = this.canvas;
        return (canvas!=null) ? (Object)canvas : (Object)this;
    }

    /**
     * Returns a string representation of this graphic. This method is for debugging purpose
     * only and may changes in any future version.
     */
    public String toString() {
        return Utilities.getShortClassName(this) + '[' + getName() + ']';
    }
}
