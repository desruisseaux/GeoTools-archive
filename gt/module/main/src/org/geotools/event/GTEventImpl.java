/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.event;

import org.geotools.event.GTDelta;
import org.geotools.event.GTEvent;


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
 */
public class GTEventImpl implements GTEvent {
    private GTDelta delta;
    private GTComponent root;
    private Type type;

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
     * Style being changed, handy if you are listening to several styles at
     * once.
     *
     * @return Style being changed
     */

    //    public StyledLayerDescriptor getSLD(){
    //    	return root;
    //    }
    /**
     * If this change is limited to a single strand, Style being changed, handy
     * if you are listening to several styles at once.
     *
     * @return Style being changed
     */
    public Object getAffected() {
        GTDelta here = delta;

        while (here.getChildren().size() == 1) {
            here = (GTDelta) here.getChildren().get(0);
        }

        return here.getAffected();
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

    public Object getRoot() {
        return root;
    }

    public void setRoot(GTComponent root) {
        this.root = root;
    }
}
