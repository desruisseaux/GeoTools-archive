package org.geotools.renderer3d.navigationgestures;


import javax.swing.event.MouseInputListener;
import java.awt.event.MouseWheelListener;

/**
 * An interface for handling navigation gestures done to the 3D view.
 *
 * @author Hans Häggström
 */
public interface NavigationGesture
        extends MouseInputListener, MouseWheelListener
{
    /**
     * @param cameraAccessor Can be asked for the camera that the navigation gesture listener should modify when gestures happen.
     */
    void setCameraAccessor( CameraAccessor cameraAccessor );
}
