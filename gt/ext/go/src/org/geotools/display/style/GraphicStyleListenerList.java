/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
 * (C) 2006, Institut de Recherche pour le Développement
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
package org.geotools.display.style;

// OpenGIS dependencies
import java.util.Map;
import java.util.LinkedHashMap;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// OpenGIS dependencies
import org.opengis.go.display.style.GraphicStyle;
import org.opengis.go.display.style.event.GraphicStyleEvent;
import org.opengis.go.display.style.event.GraphicStyleListener;

// Geotools dependencies
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;


/**
 * A list of {@link GraphicStyleListener}s.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GraphicStyleListenerList implements PropertyChangeListener {
    /**
     * The owner of this list.
     */
    private final GraphicStyle source;

    /**
     * The list of listeners.
     */
    private GraphicStyleListener[] listeners;

    /**
     * A list of changes underway.
     */
    private final Map/*<String,ValuePair>*/ changes = new LinkedHashMap/*<String,ValuePair>*/();

    /**
     * {@code >0} if the next property change events should be groupped into a single
     * graphic style event.
     */
    private int groupCount;

    /**
     * Creates a new list of graphic style listener.
     */
    public GraphicStyleListenerList(final GraphicStyle source) {
        this.source = source;
        listeners = new GraphicStyleListener[0];
    }

    /**
     * Creates a new list of graphic style listener initialized to the same values
     * than the specified one, except for the grouping status.
     */
    public GraphicStyleListenerList(final GraphicStyleListenerList clone) {
        this.source    = clone.source;
        this.listeners = clone.listeners; // Safe to share.
    }

    /**
     * Adds the specified listener to the list.
     * Returns {@code true} if the list was empty prior the addition of the specified listener.
     * <p>
     * <strong>Implementation note:</strong> a new array is created on every addition. This is
     * somewhat inefficient, but suffisient if listener addition are very rare compared to change
     * events. This is an easy way to avoid synchronization in the {@link #fire} method.
     */
    public boolean add(final GraphicStyleListener listener) {
        assert Thread.holdsLock(source);
        final int count = listeners.length;
        for (int i=count; --i>=0;) {
            if (listeners[i] == listener) {
                return false;
            }
        }
        listeners = (GraphicStyleListener[]) XArray.resize(listeners, count+1);
        listeners[count] = listener;
        return count == 0;
    }

    /**
     * Removes the specified listener from the list.
     * Returns {@code true} if the list is empty as a result of the listener removal.
     * <p>
     * <strong>Implementation note:</strong> a new array is created on every removal. This is
     * somewhat inefficient, but suffisient if listener removal are very rare compared to change
     * events. This is an easy way to avoid synchronization in the {@link #fire} method.
     */
    public boolean remove(final GraphicStyleListener listener) {
        assert Thread.holdsLock(source);
        for (int i=listeners.length; --i>=0;) {
            if (listeners[i] == listener) {
                listeners = (GraphicStyleListener[]) XArray.remove(listeners, i, 1);
                return listeners.length == 0;
            }
        }
        return false;
    }

    /**
     * Notifies all listeners that a change occured. This method should be invoked outside
     * synchronized block, if possible.
     */
    private static void fire(final GraphicStyleListener[] listeners,
                             final GraphicStyleEvent      event)
    {
        for (int i=0; i<listeners.length; i++) {
            try {
                listeners[i].styleChanged(event);
            } catch (RuntimeException exception) {
                Utilities.unexpectedException("org.geotools.display.style",
                        "GraphicStyleListener", "styleChanged", exception);
                /*
                 * Continues to notify the other listeners, since they may be unrelated
                 * to the faulty one and we don't want to prevent other listeners to work.
                 */
            }
        }
    }

    /**
     * If {@code true}, all subsequent {@linkplain PropertyChangeEvent property change events}
     * will be grouped into a single {@linkplain GraphicStyleEvent graphic style event} until
     * <code>{@linkplain #setGroupChangeEvents setGroupChangeEvents}(false)</code> is invoked.
     */
    public void setGroupChangeEvents(final boolean grouping) {
        final GraphicStyleEvent      event;
        final GraphicStyleListener[] listeners;
        synchronized (source) {
            if (grouping) {
                ++groupCount;
                return;
            }
            if (groupCount == 0) {
                throw new IllegalStateException();
            }
            if (--groupCount != 0 || changes.isEmpty()) {
                return;
            }
            listeners = this.listeners; // Protect from changes.
            event = new DefaultGraphicStyleEvent(source, changes);
            changes.clear();
        }
        fire(listeners, event);
    }

    /**
     * Invoked when a property changed. This method add the modified property to an internal list,
     * and notifies all {@linkplain GraphicStyleListener graphic style listeners} at once when
     * <code>{@linkplain #setGroupChangeEvents setGroupChangeEvents}(false)</code> is invoked.
     */
    public void propertyChange(final PropertyChangeEvent change) {
        final GraphicStyleEvent      event;
        final GraphicStyleListener[] listeners;
        synchronized (source) {
            final String name = change.getPropertyName();
            ValuePair pair = new ValuePair(change.getOldValue());
            final ValuePair previous = (ValuePair) changes.put(name, pair);
            if (previous != null) {
                pair = previous;
                changes.put(name, previous);
            }
            pair.newValue = change.getNewValue();
            if (groupCount != 0) {
                return;
            }
            listeners = this.listeners; // Protect from changes.
            event = new DefaultGraphicStyleEvent(source, changes);
            changes.clear();
        }
        fire(listeners, event);
    }
}
