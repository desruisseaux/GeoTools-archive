package org.geotools.renderer3d.impl;

import com.jme.renderer.Camera;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseWheelEvent;

/**
 * Contains common functionality for navigationGestureListeners.
 *
 * @author Hans Häggström
 */
public abstract class AbstractNavigationGesture
        extends MouseInputAdapter
        implements NavigationGesture
{

    //======================================================================
    // Private Fields

    private CanvasRenderer myCanvasRenderer = null;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // MouseWheelListener Implementation

    public void mouseWheelMoved( final MouseWheelEvent e )
    {
        // Override if needed
    }

    //----------------------------------------------------------------------
    // NavigationGesture Implementation

    public void setCanvasRenderer( final CanvasRenderer canvasRenderer )
    {
        myCanvasRenderer = canvasRenderer;
    }

    //======================================================================
    // Protected Methods

    /**
     * @return the camera that has been assigned to this navigation gesture listener, or null if no camera has yet been assigned.
     */
    protected Camera getCamera()
    {
        if ( myCanvasRenderer != null )
        {
            return myCanvasRenderer.getCamera();
        }
        else
        {
            return null;
        }
    }

}
