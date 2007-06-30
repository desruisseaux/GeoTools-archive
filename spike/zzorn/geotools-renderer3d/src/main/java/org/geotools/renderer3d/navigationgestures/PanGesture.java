package org.geotools.renderer3d.navigationgestures;

import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

import java.awt.event.MouseEvent;

/**
 * Implements a panning gesture for the 3D renderer.
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
        myOldX = e.getX();
        myOldY = e.getY();
    }

    //----------------------------------------------------------------------
    // MouseMotionListener Implementation


    public void mouseDragged( final MouseEvent e )
    {
        final Camera camera = getCamera();

        if ( camera != null )
        {
            final int currentX = e.getX();
            final int currentY = e.getY();

            final float deltaX = ( currentX - myOldX ) * SCALE;
            final float deltaY = ( currentY - myOldY ) * SCALE;

            final Vector3f newLocation = new Vector3f( camera.getLocation() );
            newLocation.x -= deltaX;
            newLocation.y += deltaY;

            camera.setLocation( newLocation );

            myOldX = currentX;
            myOldY = currentY;
        }
    }

}
