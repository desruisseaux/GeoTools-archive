package org.geotools.renderer3d.utils.canvas3d;

import com.jme.renderer.Camera;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jmex.awt.SimpleCanvasImpl;
import org.geotools.renderer3d.utils.ParameterChecker;

import java.awt.Canvas;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * A renderer that renders a 3D object in a 3D Canvas.
 * <p/>
 * REFACTOR: Change to an inner class of Canvas3D?
 *
 * @author Hans Häggström
 */
final class CanvasRenderer
        extends SimpleCanvasImpl
{

    //======================================================================
    // Private Fields

    private final Canvas myCanvas;

    private Spatial myCanvasRootNode;

    private long startTime = 0;
    private long fps = 0;

    private boolean myAspectRatioNeedsCorrecting = true;

    //======================================================================
    // Private Constants

    private static final float DEFAULT_VIEWLD_OF_VIEW_DEGREES = 45;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * Creates a new renderer that renders the specified spatial in a 3D canvas.
     *
     * @param width          initial size of the canvas.  Should be larger than 0.
     * @param height         initial size of the canvas.  Should be larger than 0.
     * @param canvasRootNode the 3D object to render.
     *                       May be null, in which case nothing is rendered (black area)
     * @param canvas         the canvas we are rendering to.  Needed for listening to resize events.
     */
    public CanvasRenderer( final int width,
                           final int height,
                           final Spatial canvasRootNode,
                           final Canvas canvas )
    {
        super( width, height );

        ParameterChecker.checkPositiveNonZeroInteger( width, "width" );
        ParameterChecker.checkPositiveNonZeroInteger( height, "height" );
        ParameterChecker.checkNotNull( canvas, "canvas" );

        myCanvasRootNode = canvasRootNode;
        myCanvas = canvas;

        // When the component is resized, adjust the size of the 3D viewport too.
        myCanvas.addComponentListener( new ComponentAdapter()
        {

            public void componentResized( ComponentEvent ce )
            {
                resizeCanvas( myCanvas.getWidth(), myCanvas.getHeight() );
                myAspectRatioNeedsCorrecting = true;
            }

        } );
    }

    //----------------------------------------------------------------------
    // Other Public Methods

    /**
     * @param canvasRootNode the spatial to render with this CanvasRenderer.
     *                       May be null, in which case nothing is rendered (black area)
     */
    public void setCanvasRootNode( final Spatial canvasRootNode )
    {
        if ( rootNode != null && myCanvasRootNode != null )
        {
            rootNode.detachChild( myCanvasRootNode );
        }

        myCanvasRootNode = canvasRootNode;

        if ( rootNode != null && myCanvasRootNode != null )
        {
            rootNode.attachChild( myCanvasRootNode );
        }
    }


    @Override
    public void simpleSetup()
    {
        if ( myCanvasRootNode != null )
        {
            rootNode.attachChild( myCanvasRootNode );
        }
    }


    @Override
    public void simpleUpdate()
    {
        // Frames per second counter
        // DEBUG: To be removed in production code
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


    public void simpleRender()
    {
        // Setup aspect ratio for camera on the first frame (the camera is not created before the rendering starts)
        if ( myAspectRatioNeedsCorrecting )
        {
            correctCameraAspectRatio();

            myAspectRatioNeedsCorrecting = false;
        }
    }

    //======================================================================
    // Private Methods

    /**
     * Sets the aspect ratio of the camera to the aspect ratio of the viewport size.
     */
    private void correctCameraAspectRatio()
    {
        final Renderer renderer = getRenderer();

        if ( renderer != null )
        {
            // Get size on screen
            final float height = renderer.getHeight();
            final float width = renderer.getWidth();

            // Calculate aspect ratio
            float aspectRatio = 1;
            if ( height > 0 )
            {
                aspectRatio = width / height;
            }

            // Set aspect ratio and field of view to camera
            final Camera camera = getCamera();
            camera.setFrustumPerspective( DEFAULT_VIEWLD_OF_VIEW_DEGREES,
                                          aspectRatio,
                                          camera.getFrustumNear(),
                                          camera.getFrustumFar() );
        }
    }

}
