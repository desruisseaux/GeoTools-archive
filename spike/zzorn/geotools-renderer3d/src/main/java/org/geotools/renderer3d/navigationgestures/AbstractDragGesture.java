package org.geotools.renderer3d.navigationgestures;

import com.jme.renderer.Camera;

import java.awt.event.MouseEvent;

/**
 * Common functionality for gestures that involve listening to drags.
 *
 * @author Hans H�ggstr�m
 */
public abstract class AbstractDragGesture
        extends AbstractNavigationGesture
{

    //======================================================================
    // Private Fields

    private final int myButtonDownMask;
    private final int myButton;

    private int myOldX = 0;
    private int myOldY = 0;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // MouseListener Implementation

    public void mousePressed( final MouseEvent e )
    {
        if ( isMouseButtonPressed( e, myButtonDownMask ) )
        {
            myOldX = e.getX();
            myOldY = e.getY();

            getCursorChanger().setCursor( this, null );
        }
    }


    public void mouseReleased( final MouseEvent e )
    {
        if ( e.getButton() == myButton )
        {
            getCursorChanger().resetCursor( this );
        }
    }

    //----------------------------------------------------------------------
    // MouseMotionListener Implementation

    public void mouseDragged( final MouseEvent e )
    {
        if ( isMouseButtonPressed( e, myButtonDownMask ) )
        {
            final Camera camera = getCamera();
            if ( camera != null )
            {
                // Calculate mouse movement
                final int currentX = e.getX();
                final int currentY = e.getY();
                final float deltaX = ( currentX - myOldX ) * getSensitivity();
                final float deltaY = ( currentY - myOldY ) * getSensitivity();

                // Do the drag bussiness logic
                applyDragGesture( camera, deltaX, deltaY );

                // Reset mouse position
                setMousePos( myOldX, myOldY );
            }
        }
    }

    //----------------------------------------------------------------------
    // Other Public Methods

    //======================================================================
    // Protected Methods

    //----------------------------------------------------------------------
    // Protected Constructors

    /**
     * @param sensitivity    a sensitivity value for this gesture.
     * @param button         button whose drags we should listen to.
     * @param buttonDownMask the down mask for the button whose drags we should listen to.
     */
    protected AbstractDragGesture( final float sensitivity, final int button, final int buttonDownMask )
    {
        super( sensitivity );
        myButton = button;
        myButtonDownMask = buttonDownMask;
    }

    //----------------------------------------------------------------------
    // Abstract Protected Methods

    /**
     * Called from the drag handling code.
     *
     * @param camera a reference to the camera
     * @param deltaX the sensitivity scaled movement along the x axis of the mouse since the last call or the start of the gesture.
     * @param deltaY the sensitivity scaled movement along the y axis of the mouse since the last call or the start of the gesture.
     */
    protected abstract void applyDragGesture( Camera camera, float deltaX, float deltaY );

}
