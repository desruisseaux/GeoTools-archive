package org.geotools.renderer3d.impl;

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
     * @param canvasRenderer the 3D renderer used.
     *                       Can be asked for the camera that the navigation gesture listener should modify when gestures happen.
     */
    void setCanvasRenderer( CanvasRenderer canvasRenderer );
}
