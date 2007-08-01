package org.geotools.renderer3d.navigationgestures;

import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

import java.awt.event.MouseEvent;

/**
 * Implements a panning gesture for the 3D renderer.
 * <p/>
 * Pan / move across map with left mouse button drag (will actually move camera along the left/right,
 * up/down axes of the camera). (A single left mouse button click will select / click on an item on map).
 * The drag has some inertia, so a quick drag will move the camera faster than a slow one.
 * The camera is kept above the ground level at all times though.
 *
 * @author Hans Häggström
 */
public final class PanGesture
        extends AbstractNavigationGesture
{

    //======================================================================
    // Private Fields

    private int myOldX = 0;
    private int myOldY = 0;

    //======================================================================
    // Private Constants

    private static final float SCALE = 0.1f;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // MouseListener Implementation

    public void mousePressed( final MouseEvent e )
    {
        if ( isMouseButtonPressed( e, MouseEvent.BUTTON1_DOWN_MASK ) )
        {
            myOldX = e.getX();
            myOldY = e.getY();
        }
    }

    //----------------------------------------------------------------------
    // MouseMotionListener Implementation

    public void mouseDragged( final MouseEvent e )
    {
        if ( isMouseButtonPressed( e, MouseEvent.BUTTON1_DOWN_MASK ) )
        {
            final Camera camera = getCamera();
            if ( camera != null )
            {
                final int currentX = e.getX();
                final int currentY = e.getY();

                final float deltaX = ( currentX - myOldX ) * SCALE;
                final float deltaY = ( currentY - myOldY ) * SCALE;

                // TODO: Add inertia and acceleration
                // TODO: Add ground collision detection and keep the camera above ground.

                final Vector3f newLocation = new Vector3f( camera.getLocation() );
                newLocation.scaleAdd( deltaX, camera.getLeft(), newLocation );
                newLocation.scaleAdd( deltaY, camera.getUp(), newLocation );

                camera.setLocation( newLocation );

                myOldX = currentX;
                myOldY = currentY;
            }
        }
    }

}
