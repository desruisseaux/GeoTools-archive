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

import java.util.Collections;
import java.util.List;


/**
 * Indicates which style constructs have been changed.
 * 
 * <p>
 * Acts as a series of breadcrumbs stored up by a StyleEvent to communicate
 * changes.
 * </p>
 * 
 * <p>
 * This delta is constructed a fashion following the outline of the style
 * document, allowing you to skip over entire branches of changes if you are
 * not interested.
 * </p>
 * <h2>Example Use</h2>
 * <p>
 * The following examples will make use of the following
 * allegorical data structure
 * <pre><code>
 * Root
 *  +--Parent
 *       +-- Child
 *       +-- List
 *             [0]--Element
 * </code></pre>
 * These roles are allegorical in nature any may be played
 * in real life by arrays, beans, collections, etc..
 * (as example StyleLayerDescriptor is a "Root"). 
 * </p>
 * <p>
 * Example 0: An <b>Child</b> is changed.
 * <pre><code>
 * Event.POST_CHANGE
 *    +---Delta1 "" -1 NO_CHANGE(Root,null,)
 *           +---Delta2 "Parent" -1 NO_CHANGE (Parent, null )
 *                  +---Delta3 "Child" -1 CHANGED (Child, oldChild )
 * </code></pre>
 * Location of <i>change</i> is indicated by delta, including
 * name of "Child". The oldChild is provided incase you need to
 * un-listen or something. Parent is not considered to have
 * changed itself (it is still has the same structure).
 * </p> 
 * <p>
 * Example 1: An <b>Element</b> is changed:
 * <pre><code>
 * Event.POST_CHANGE
 *    +---Delta1 "" -1 NO_CHANGE(Root,null,)
 *           +---Delta2 "Parent" -1 NO_CHANGE (Parent, null )
 *                  +---Delta3 "List" 0 CHANGED (Element, null )
 * </code></pre>
 * Location of <i>change</i> is indicated by delta, including position
 * in list. Children in lists are children too, delta intentionally
 * similar to the last.
 * </p>
 * <p>
 * Example 2: A new <b>Element2</b> is added:
 * <pre><code>
 * Event.POST_CHANGE
 *    +---Delta1 "" -1 NO_CHANGE(Root,null,)
 *           +---Delta2 "Parent" -1 CHANGED (Parent, null )
 *                  +---Delta3 "List" 1 ADDED (Element2, null )
 * </code></pre>
 * </p>
 * Adding Element2 results in a change to Parent (it is structured
 * diffrently). Position of the change in the "List" is indicated.
 * <p>
 * Example 3: Swap <b>Element</b> and Element2
 * <pre><code>
 * Event.POST_CHANGE
 *    +---Delta1 "" -1 NO_CHANGE(Root,null,)
 *           +---Delta2 "Parent" -1 CHANGED (Parent, null )
 *                  +---Delta3 "List" 1 NO_CHANGE (Element, null )
 *                  +---Delta4 "List" 0 NO_CHANGE (Element2, null )
 * </code></pre>
 * Moving things around does not change them, but it is a change to the
 * Parent.
 * </p>
 * <p>
 * Example 5: Remove <b>Element2</b>
 * <pre><code>
 * Event.POST_CHANGE
 *    +---Delta1 "" -1 NO_CHANGE(Root,null,)
 *           +---Delta2 "Parent" -1 CHANGED (Parent, null )
 *                  +---Delta3 "List" -1 REMOVED ( null, Element2 )
 *                  +---Delta4 "List" 0 NO_CHANGE (Element, null )
 * </code></pre>
 * Removing also is a considered a change to Parent, note
 * this is a POST_CHANGE event so Element2 is an oldValue.
 * The NO_CHANGE for Element is similar to Example4, it still
 * indicates a change of position.
 * </p>
 * <p>
 * Example 6: Go Fish
 * <pre><code>
 * Event.POST_CHANGE
 *    +---Delta1 "" -1 CHANGED(Root,null,)
 * </code></pre>
 * Something changed somewhere, similar to "touch".
 * </p> 
 * <p>
 * Example 7: Shutting Down
 * <pre><code>
 * Event.PRE_CLOSE
 *    +---Delta1 "" -1 NO_CHANGE(Root,null,)
 * </code></pre>
 * That was easy then...
 * </p>
 * <p>
 * You should be aware that the event system may "collapse"
 * events that duplicate information, especially when
 * considering batch changes resulting from the application
 * of a visitor or transformation.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public interface GTDelta {
	
    /** List indicating no children are present */
    public static final List NO_CHILDREN = Collections.EMPTY_LIST;
    
    /** Index position is not to be considered relevent */
    public static final int NO_INDEX = -1;
    
    /**
     * Returns the kind of this delta.
     * 
     * <p>
     * Normally, one of <code>ADDED</code>, <code>REMOVED</code> or
     * <code>CHANGED</code>.
     * </p>
     *
     * @return the kind of this resource delta
     *
     * @see Kind.ADDED
     * @see Kind.REMOVED
     * @see Kind.CHANGED
     */
    public Kind getKind();

    /**
     * Affected construct, getKind & getChildern
     * indicate specific details of the change.
     * 
     * @return Affected construct
     */
    public Object getValue();

    /**
     * Construct being replaced with a changed, getValue is the replacing value.
     * 
     * @return Affected construct
     */
    public Object getOldValue();
    
    /**
     * Position in a "list" where the change occured, or NO_INDEX.
     * 
     * @return Position in "list" or NO_INDEX.
     */
    public int getPoisition();
    
    /**
     * Name of property being affected.
     * <p>
     * <ul>
     * <li>propertyName: "bean" property name when intergrating third party
     *     Java Beans into a more complex tree structure.
     * <li>List and Arrays: the name of the list, and may be
     *     combined with getPosition to indicate location, NO_INDEX would indicate
     *     the entire "list" has been modified.
     * </ul>
     * </p>
     * @return name of affected Child, or <code>null</code> for root
     */
    public String getName();
    
    /**
     * Finds and returns deltas for specificly changed constructs.
     * <p>
     * This code may be considered more accessable then the use of
     * GTDeltaVisitor.
     * </p>
     *
     * @return List of StyleDelta
     */
    public List getChildren();

    /**
     * Accepts the given visitor.
     *
     * @param visitor
     */
    public void accept(GTDeltaVisitor visitor);

    /**
     * Kind of Delta, used to indicate change.
     *
     * @since 2.2.M3
     */
    public class Kind {
        /**
         * Indicates no change.
         * <p>
         * GTDelta defined values:
         * <ul>
         * <li>getValue(): unchanged value
         * <li>getOldValue(): null
         * <li>getPosition(): index in list, or NO_INDEX
         * </ul>
         * May be considered a touch if it occurs as the last delta,
         * usually just indicates that a child has undergone a change.
         * </p>
         * @see getKind()
         */
        public static final Kind NO_CHANGE = new Kind();

        /**
         * The construct has been added (usually to a list)
         * <p>
         * GTDelta defined values:
         * <ul>
         * <li>getValue(): added value
         * <li>getOldValue(): null
         * <li>getPosition(): index in list, or NO_INDEX
         * </ul>
         * </p>
         * @see getKind()
         */
        public static final Kind ADDED = new Kind();

        /**
         * The construct has been removed (usually from a list)
         * <p>
         * GTDelta defined values:
         * <ul>
         * <li>getValue(): value being removed
         * <li>getOldValue(): null
         * <li>getPosition(): index in list, or NO_INDEX
         * </ul>
         * </p>
         * <p>
         * Since REMOVED deltas are sent before the delete is performed
         * getValue is still valid.
         * </p>
         * 
         * @see getKind()
         */
        public static final Kind REMOVED = new Kind();

        /**
         * The construct has been changed.
         * <p>
         * GTDelta defined values:
         * <ul>
         * <li>getValue(): current value
         * <li>getOldValue(): previous value (being replaced)
         * <li>getPosition(): index in list, or NO_INDEX
         * </ul>
         * </p>
         * @see getKind()
         */
        public static final Kind CHANGED = new Kind();
    }
}