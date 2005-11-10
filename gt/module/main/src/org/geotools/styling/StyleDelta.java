package org.geotools.styling;

import java.util.Collections;
import java.util.List;

/**
 * Indicates which style constructs have been changed.
 * <p>
 * Acts as a series of breadcrumbs stored up by a StyleEvent
 * to communicate changes.
 * </p>
 * <p>
 * This delta is constructed a fashion following the outline
 * of the style document, allowing you to skip over entire
 * branches of changes if you are not interested.
 * </p>
 * @author Jody Garnett
 */
public interface StyleDelta {
	/** List indicating no children are present */
    public static final List NO_CHILDREN = Collections.EMPTY_LIST;
    
    /**
     * Kind of Delta, used to indicate change.
     * 
     * @author jgarnett
     * @since 0.9.0
     */
    public class Kind {
        /**
         * Indicates no change.
         * 
         * @see getKind()
         */
        public static final Kind NO_CHANGE = new Kind();

        /**
         * The style constrcut has been added.
         * 
         * @see getKind()
         */
        public static final Kind ADDED = new Kind();

        /**
         * The style construct has been removed.
         * 
         * @see getKind()
         */
        public static final Kind REMOVED = new Kind();

        /**
         * The style construct has been changed.
         * 
         * @see getKind()
         */
        public static final Kind CHANGED = new Kind();
    }
    /**
     * Returns the kind of this delta.
     * <p>
     * Normally, one of <code>ADDED</code>, <code>REMOVED</code> or <code>CHANGED</code>.
     * </p>
     * 
     * @return the kind of this resource delta
     * @see Kind.ADDED
     * @see Kind.REMOVED
     * @see Kind.CHANGED
     */
    public Kind getKind();
    
    /**
     * Affected style construct, getChildern may indicate specific details
     * of the change.
     * <p>
     * Examples of style constructs (in order of abstraction):
     * <ul>
     * <li>StyleLayerDescriptor
     * <li>NamedStyle
     * <li>FeatureTyleStyle
     * <li>Rule
     * <li>Symbolizer
     * <li>Mark
     * </ul>
     * </p>
     * <p>
     * This event system will stop at the Rule level, even though
     * changes may occur at individual Filter Exppressions.
     * </p>
     * @return Style construct
     */
    public Object getAffected();
    
    /**
     * Finds and returns deltas for specificly changed constructs.
     * <p>
     * This code may be considered more accessable then the
     * use of StyleDeltaVisitor.
     * </p>
     *    
     * @kindMask Set of IDelta.Kind
     * @return List of StyleDelta
     */
    public List getChildren();
    
    /**
     * Accepts the given visitor.
     * 
     * @param visitor
     * @throws CoreException
     */
    public void accept( StyleDeltaVisitor visitor );
}