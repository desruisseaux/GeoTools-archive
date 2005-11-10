package org.geotools.styling;

/**
 * Listens to changes in the Style content.
 * <p>
 * Changes are provided:
 * <ul>
 * <li>Before: deletion
 * <li>After: modification
 * </ul>
 * </p>
 * <p>
 * Since the Style data structure can be vast and complicated
 * a trail of breadcrumbs (a delta) is provided to help find your
 * way to the change.
 * </p> 
 * @author jgarnett
 */
public interface StyleListener {
	
    /**
     * Notifies this listener that some changes are happening, or have already happened.
     * <p>
     * The supplied event gives details. This event object
     * (and the delta within it) is valid only for the duration of the
     * invocation of this method.
     * </p>
     * <p>
     * Note that during style change event notification, further changes
     * to the style may be disallowed.
     * </p>
     * <p>
     * Note that this method is not guaranteed to execute in
     * an user interface thread:
     * <ul>
     * <li>SwingUtilities.invokeLater( Runnable );
     * <li>SWT: Display.getDefault().asyncExec( Runnable );
     * </ul>
     * All you J2EE developers can stop laughing now.
     * </p>
     * 
     * @param event the style change event
     */
    public void changed( StyleEvent event );
}