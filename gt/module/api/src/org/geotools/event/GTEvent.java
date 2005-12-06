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
 * @author Jody Garnett, Refractions Research
 *
 * @since 2.2.M2
 */
public interface GTEvent {
    /**
     * Returns a delta, rooted at style, describing the set of changes that
     * happened. Returns <code>null</code> if not applicable to this type of
     * event.
     *
     * @return the style delta, or <code>null</code> if not applicable
     */
    public GTDelta getDelta();

    /**
     * Style being changed, handy if you are listening to several styles at
     * once.
     *
     * @return Style being changed
     */
    public Object getRoot();

    /**
     * If this change is limited to a single strand, Style being changed, handy
     * if you are listening to several styles at once.
     *
     * @return Style being changed
     */
    public Object getAffected();

    /**
     * Returns the type of event being reported.
     *
     * @return one of the event type constants
     *
     * @see #POST_CHANGE
     * @see #PRE_DELETE
     */
    public Type getType();

    /**
     * Constants used to indicate the type of StyleChangedEvent
     */
    public class Type {
        /**
         * Event type constant (bit mask) indicating an after-the-fact report
         * of creations, deletions, and modifications to one or more style
         * constructs expressed as a hierarchical delta as returned by
         * <code>getDelta</code>.
         *
         * @see #getType()
         * @see #getDelta()
         */
        public static final Type POST_CHANGE = new Type();

        /**
         * Event type constant (bit mask) indicating a before-the-fact report
         * of the impending deletion of a single style construct.
         *
         * @see #getType()
         * @see #getService()
         */
        public static final Type PRE_DELETE = new Type();

        private Type() {
        }
    }
}
