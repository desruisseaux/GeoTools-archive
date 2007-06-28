package org.geotools.renderer3d.impl;

import com.jme.input.InputHandler;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.scene.Spatial;
import com.jmex.awt.SimpleCanvasImpl;
import org.geotools.renderer3d.utils.ParameterChecker;

/**
 * @author Hans Häggström
 */
public final class CanvasRenderer
        extends SimpleCanvasImpl
{

    //======================================================================
    // Private Fields

    private final Spatial myCanvasRootNode;

    private InputHandler input;
    private long startTime = 0;
    private long fps = 0;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    public CanvasRenderer( int width, int height, Spatial canvasRootNode )
    {
        super( width, height );

        ParameterChecker.checkNotNull( canvasRootNode, "canvasRootNode" );

        myCanvasRootNode = canvasRootNode;
    }

    //----------------------------------------------------------------------
    // Other Public Methods

    @Override
    public void simpleSetup()
    {
        rootNode.attachChild( myCanvasRootNode );

        // Mouse input
        input = new InputHandler();
        input.addAction( new InputAction()
        {

            public void performAction( InputActionEvent evt )
            {
                // DEBUG
                System.out.println( evt.getTriggerName() );
            }

        }, InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_ALL, InputHandler.AXIS_NONE, false );
    }


    public void simpleUpdate()
    {
        input.update( tpf );

        // Frames per second counter
        // DEBUG
        if ( startTime > System.currentTimeMillis() )
        {
            fps++;
        }
        else
        {
            long timeUsed = 5000 + ( startTime - System.currentTimeMillis() );
            startTime = System.currentTimeMillis() + 5000;
            System.out.println( fps + " frames in " + ( timeUsed / 1000f ) + " seconds = "
                                + ( fps / ( timeUsed / 1000f ) ) + " FPS (average)" );
            fps = 0;
        }
    }

}
