package org.geotools.renderer3d.navigationgestures;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

import java.awt.event.MouseEvent;

/**
 * Turn camera using right mouse button drag (change yaw and pitch, pitch locked to avoid rolling to back,
 * there could be a maximum angle to avoid getting lost looking at the sky) (cursor disappears while mouse pressed,
 * relative movement is measured. Similar to first person view games). (A right mouse button click without drag will
 * typically show a context menu for the feature under the mouse).
 *
 * @author Hans Häggström
 */
public final class RotateGesture
        extends AbstractNavigationGesture
{

    //======================================================================
    // Private Fields

    private Quaternion myRotation = new Quaternion( 0, 0, 0, 1 );
    private Quaternion myDirection = new Quaternion( 0, 0, 0, 1 );
    private int myOldX = 0;
    private int myOldY = 0;

    //======================================================================
    // Private Constants

    private static final float SCALE = 0.1f;
    private static final Vector3f Z_AXIS = new Vector3f( 0, 0, 1 );

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // MouseListener Implementation

    public void mousePressed( final MouseEvent e )
    {
        if ( isMouseButtonPressed( e, MouseEvent.BUTTON3_DOWN_MASK ) )
        {
            myOldX = e.getX();
            myOldY = e.getY();
        }
    }

    //----------------------------------------------------------------------
    // MouseMotionListener Implementation


    public void mouseDragged( final MouseEvent e )
    {
        if ( isMouseButtonPressed( e, MouseEvent.BUTTON3_DOWN_MASK ) )
        {
            final Camera camera = getCamera();
            if ( camera != null )
            {
                final int currentX = e.getX();
                final int currentY = e.getY();

                final float deltaX = ( currentX - myOldX ) * SCALE;
                final float deltaY = ( currentY - myOldY ) * SCALE;

                // Get quaternion from camera
                final Vector3f left = camera.getLeft();
                final Vector3f up = camera.getUp();
                final Vector3f forward = camera.getDirection();
                myDirection.fromAxes( left, up, forward );

                // Apply rotation to around current position
                myRotation.fromAngleNormalAxis( deltaY * SCALE, left );
                myRotation.mult( myDirection, myDirection );
                myRotation.fromAngleNormalAxis( -deltaX * SCALE, Z_AXIS );
                myRotation.mult( myDirection, myDirection );

                // Apply new direction to camera
                camera.setAxes( myDirection );

                // TODO: Stabilize the left, up, and forward vectors so that they stay orthogonal despite rotation rounding errors.

                myOldX = currentX;
                myOldY = currentY;
            }
        }
    }

}
