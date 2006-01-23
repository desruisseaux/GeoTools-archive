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
 * @source $URL$
 */
public interface GTEvent {
	
    /**
     * Returns a delta, from the root, describing the set of changes that
     * happened.
     * <p>
     * May be <code>null</code> if not applicable, available
     * to this type of event.
     *
     * @return the style delta, or <code>null</code> if not applicable
     */
    public GTDelta getDelta();

    /**
     * Root construct issuing the event.
     * <p>
     * Handy if you are listening to several things at once.
     * </p>
     * @return Root construct issuing event
     */
    public Object getSource();

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
         * Event type indicating an after-the-fact report
         * of creations, deletions, and modifications to one or more
         * constructs expressed as a hierarchical delta as returned by
         * <code>getDelta</code>.
         *
         * @see #getType()
         */
        public static final Type POST_CHANGE = new Type();

        /**
         * Event type indicating a before-the-fact report
         * of the impending deletion of a single construct.
         * <p>
         * The data is being removed, if you were keeping any
         * metadata about this information it is time to throw
         * away.
         * </p>
         * @see #getType()
         */
        public static final Type PRE_DELETE = new Type();
        
        /**
         * Event type indicating a before-the-fact report
         * of the impending closure of a single construct.
         * <p>
         * This is applicable when manging system resources,
         * such as network connections and icons.
         * </p>
         * <p>
         * We are simply tearing dow the data structure because
         * the system is shutting down,  you can keep metadata
         * but be sure to return any resources you were using!
         * </p>
         * @see #getType()
         */
        public static final Type PRE_CLOSE = new Type();

        private Type() {
        }
    }
}
