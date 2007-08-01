package org.geotools.renderer3d.navigationgestures;

import com.jme.renderer.Camera;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;


/**
 * Contains common functionality for navigationGestureListeners.
 * <p/>
 * REFACTOR: Include common features of Pan and Rotate gestures here or into a common superclass for them.
 *
 * @author Hans Häggström
 */
public abstract class AbstractNavigationGesture
        extends MouseInputAdapter
        implements NavigationGesture
{

    //======================================================================
    // Private Fields

    private CameraAccessor myCameraAccessor = null;

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

    public final void setCameraAccessor( final CameraAccessor cameraAccessor )
    {
        myCameraAccessor = cameraAccessor;
    }

    //======================================================================
    // Protected Methods

    /**
     * @return the camera that has been assigned to this navigation gesture listener, or null if no camera has yet been assigned.
     */
    protected Camera getCamera()
    {
        if ( myCameraAccessor != null )
        {
            return myCameraAccessor.getCamera();
        }
        else
        {
            return null;
        }
    }

    protected boolean isMouseButtonPressed( final MouseEvent e, final int buttonDownMask )
    {
        return ( e.getModifiersEx() & buttonDownMask ) != 0;
    }
}
