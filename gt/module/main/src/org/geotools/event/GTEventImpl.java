/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.event;

import org.geotools.event.GTDelta;
import org.geotools.event.GTEvent;
import java.util.EventObject;


/**
 * Captures changes to Style.
 * 
 * <p>
 * The "delta" acts as a series of bread crumbs allowing you the listener to
 * figure out what changed where. The <b>Type</b> answers that other peskey
 * question: when.
 * </p>
 * 
 * <p>
 * You should be warned that deltas may also be "saved up" for a rainy day,
 * this keeps user interfaces from being flooded with a cascade of events. The
 * best example is a "macro" that makes a series of changes and only reports
 * back a compound change resulting from the batch opperation.
 * </p>
 *
 * @since 2.2.M3
 * @source $URL$
 */
public class GTEventImpl extends EventObject implements GTEvent {
    private static final long serialVersionUID = -5304196462694574579L;
    private GTDelta delta;
    private Type type;

    public GTEventImpl(GTRoot source) {
        this(source,
            new GTDeltaImpl(new GTNoteImpl("", GTDelta.NO_INDEX),
                GTDelta.Kind.CHANGED, source, null));
    }

    public GTEventImpl(GTRoot source, GTDelta delta) {
        this(source, Type.POST_CHANGE, delta);
    }

    public GTEventImpl(GTRoot source, Type type, GTDelta delta) {
        super(source);
        this.type = type;
        this.delta = delta;
    }

    /**
     * Returns a delta, rooted at style, describing the set of changes that
     * happened. Returns <code>null</code> if not applicable to this type of
     * event.
     *
     * @return the style delta, or <code>null</code> if not applicable
     */
    public GTDelta getDelta() {
        return delta;
    }

    /**
     * Returns the type of event being reported.
     *
     * @return one of the event type constants
     *
     * @see #POST_CHANGE
     * @see #PRE_DELETE
     */
    public Type getType() {
        return type;
    }

    public void setDelta(GTDelta delta) {
        this.delta = delta;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
