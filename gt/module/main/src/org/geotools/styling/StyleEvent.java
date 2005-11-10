package org.geotools.styling;

/**
 * Captures changes to Style.
 * <p>
 * The "delta" acts as a series of bread crumbs allowing you the listener
 * to figure out what changed where. The <b>Type</b> answers that other
 * peskey question: when.
 * </p>
 * <p>
 * You should be warned that deltas may also be "saved up" for a rainy day,
 * this keeps user interfaces from being flooded with a cascade of events.
 * The best example is a "macro" that makes a series of changes and only reports
 * back a compound change resulting from the batch opperation.
 * </p>
 * @author Jody Garnett, Refractions Research
 * @since 2.2.M2
 */
public interface StyleEvent {
	
	/** Constants used to indicate the type of StyleChangedEvent */
    public class Type {
        /**
         * Event type constant (bit mask) indicating an after-the-fact report of creations,
         * deletions, and modifications to one or more style constructs expressed as a
         * hierarchical delta as returned by <code>getDelta</code>.
         * 
         * @see #getType()
         * @see #getDelta()
         */
        public static final Type POST_CHANGE = new Type();

        /**
         * Event type constant (bit mask) indicating a before-the-fact report of the impending
         * deletion of a single style construct.
         * 
         * @see #getType()
         * @see #getService()
         */
        public static final Type PRE_DELETE = new Type();
        
        private Type() {}
    }
    /**
     * Returns a delta, rooted at style, describing the set of changes that happened.
     * Returns <code>null</code> if not applicable to this type of event.
     * 
     * @return the style delta, or <code>null</code> if not applicable
     */
    public StyleDelta getDelta();
    
    /**
     * Style being changed, handy if you are listening to several styles at once.
     * @return Style being changed
     */
    public Style getStyle();
    
    /**
     * If this change is limited to a single strand, Style being changed, handy if you are listening to several styles at once.
     * @return Style being changed
     */    
    public Object getVictim();
    
    /**
     * Returns an object identifying the source of this event.
     * <p>
     * This is used by user interfaces, so they can ignore events generated
     * by their own actions (but I don't have to tell you this).
     * </p>
     * @return an object identifying the source of this event
     * @see java.util.EventObject
     */
    public Object getSource();
    
    /**
     * Returns the type of event being reported.
     * 
     * @return one of the event type constants
     * @see #POST_CHANGE
     * @see #PRE_DELETE
     */
    public Type getType();
}
