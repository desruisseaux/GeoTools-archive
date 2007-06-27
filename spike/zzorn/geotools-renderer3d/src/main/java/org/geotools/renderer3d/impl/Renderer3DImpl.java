package org.geotools.renderer3d.impl;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.InputHandler;
import com.jme.input.InputSystem;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.shape.Box;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.awt.JMECanvas;
import com.jmex.awt.SimpleCanvasImpl;
import com.jmex.awt.input.AWTMouseInput;
import org.geotools.map.MapContext;
import org.geotools.renderer3d.Renderer3D;
import org.geotools.renderer3d.utils.quadtree.QuadTree;
import org.geotools.renderer3d.utils.quadtree.QuadTreeImpl;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Hans Häggström
 */
public class Renderer3DImpl
        implements Renderer3D
{

    //======================================================================
    // Private Fields

    private MapContext myMapContext = null;
    private QuadTree myQuadTree;
    private Component myView3D = null;

    //======================================================================
    // Private Constants

    private static final int DEFAULT_START_RADIUS_M = 1000;
    private static final TerrainBlockFactory TERRAIN_BLOCK_FACTORY = new TerrainBlockFactory();

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * Creates a new Renderer3D with 1 km default size for the initial terrain blocks.
     */
    public Renderer3DImpl()
    {
        this( DEFAULT_START_RADIUS_M );
    }


    /**
     * Creates a new Renderer3D.
     *
     * @param startRadius_m the length of each side in the first quad tree nodes created.
     */
    public Renderer3DImpl( final double startRadius_m )
    {
        this( null, startRadius_m );
    }


    /**
     * Creates a new Renderer3D with 1 km default size for the initial terrain blocks.
     *
     * @param mapContextToRender the map context that is used to get the layers to render in the 3D view.
     */
    public Renderer3DImpl( final MapContext mapContextToRender )
    {
        this( mapContextToRender, DEFAULT_START_RADIUS_M );
    }


    /**
     * Creates a new Renderer3D.
     *
     * @param mapContextToRender the map context that is used to get the layers to render in the 3D view.
     * @param startRadius_m      the length of each side in the first quad tree nodes created.
     */
    public Renderer3DImpl( final MapContext mapContextToRender,
                           final double startRadius_m )
    {
        myQuadTree = new QuadTreeImpl( startRadius_m, TERRAIN_BLOCK_FACTORY );
        myMapContext = mapContextToRender;
    }

    //----------------------------------------------------------------------
    // Renderer3D Implementation

    public MapContext getMapContext()
    {
        return myMapContext;
    }


    public void setMapContext( final MapContext mapContext )
    {
        if ( myMapContext != mapContext )
        {
            myMapContext = mapContext;

            // Clear the old quadtree and start building a new one, with the data from the new context.
            myQuadTree = new QuadTreeImpl( DEFAULT_START_RADIUS_M, TERRAIN_BLOCK_FACTORY );
        }
    }


    public Component get3DView()
    {
        if ( myView3D == null )
        {
            myView3D = createView3D();
        }

        return myView3D;
    }

    //======================================================================
    // Private Methods

    private Component createView3D()
    {
        final int width = 800;
        final int height = 600;

        // make the canvas:
        final Canvas canvas = DisplaySystem.getDisplaySystem( "lwjgl" ).createCanvas( width, height );
        canvas.setMinimumSize( new Dimension( 0, 0 ) );

        // add a listener... if window is resized, we can do something about it.
        canvas.addComponentListener( new ComponentAdapter()
        {

            public void componentResized( ComponentEvent ce )
            {
                //doResize();
            }

        } );

        KeyInput.setProvider( KeyInput.INPUT_AWT );

        setupMouse( canvas, false );

        // Important!  Here is where we add the guts to the panel:
        final JMECanvas jmeCanvas = ( (JMECanvas) canvas );
        jmeCanvas.setImplementor( new CanvasRenderer( width, height ) );
        //jmeCanvas.setBackground( Color.GRAY );
        jmeCanvas.setUpdateInput( true );

        // MAKE SURE YOU REPAINT SOMEHOW OR YOU WON'T SEE THE UPDATES...
        new Thread()
        {
            {
                setDaemon( true );
            }

            public void run()
            {
                while ( true )
                {
                    canvas.repaint();
                    try
                    {
                        Thread.sleep( 10 );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                    yield();
                }
            }
        }.start();

        return canvas;
    }

    //======================================================================
    // Inner Classes

    private final class CanvasRenderer
            extends SimpleCanvasImpl
    {

        //======================================================================
        // Private Fields

        private Quaternion rotQuat;
        private float angle = 0;
        private Vector3f axis;
        private Box box;
        private InputHandler input;

        //======================================================================
        // Non-Private Fields

        long startTime = 0;
        long fps = 0;

        //======================================================================
        // Public Methods

        //----------------------------------------------------------------------
        // Constructors

        public CanvasRenderer( int width, int height )
        {
            super( width, height );
        }

        //----------------------------------------------------------------------
        // Other Public Methods

        public void simpleSetup()
        {
            // Normal Scene setup stuff...
            rotQuat = new Quaternion();
            axis = new Vector3f( 1, 1, 0.5f );
            axis.normalizeLocal();

            Vector3f max = new Vector3f( 5, 5, 5 );
            Vector3f min = new Vector3f( -5, -5, -5 );

            box = new Box( "Box", min, max );
            box.setModelBound( new BoundingBox() );
            box.updateModelBound();
            box.setLocalTranslation( new Vector3f( 0, 0, -10 ) );
            box.setRenderQueueMode( Renderer.QUEUE_SKIP );
            rootNode.attachChild( box );

            box.setRandomColors();

            TextureState ts = renderer.createTextureState();
            ts.setEnabled( true );
            ts.setTexture( TextureManager.loadTexture( Renderer3DImpl.class
                    .getClassLoader().getResource(
                    "jmetest/data/images/Monkey.jpg" ),
                                                       Texture.MM_LINEAR, Texture.FM_LINEAR ) );

            rootNode.setRenderState( ts );
            startTime = System.currentTimeMillis() + 5000;

            input = new InputHandler();
            input.addAction( new InputAction()
            {

                public void performAction( InputActionEvent evt )
                {
                    System.out.println( evt.getTriggerName() );
                }

            }, InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_ALL, InputHandler.AXIS_NONE, false );
        }


        public void simpleUpdate()
        {
            input.update( tpf );

            // Code for rotating the box... no surprises here.
            if ( tpf < 1 )
            {
                angle = angle + ( tpf * 25 );
                if ( angle > 360 )
                {
                    angle = 0;
                }
            }
            rotQuat.fromAngleNormalAxis( angle * FastMath.DEG_TO_RAD, axis );
            box.setLocalRotation( rotQuat );

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

    /**
     * Set up a canvas to fire mouse events via the input system.
     *
     * @param glCanvas canvas that should be listened to
     * @param dragOnly true to enable mouse input to jME only when the mouse is dragged
     */
    public static void setupMouse( Canvas glCanvas, boolean dragOnly )
    {
        AWTMouseInput.setProvider( InputSystem.INPUT_SYSTEM_AWT );
        AWTMouseInput awtMouseInput = ( (AWTMouseInput) AWTMouseInput.get() );
        awtMouseInput.setEnabled( !dragOnly );
        awtMouseInput.setDragOnly( dragOnly );
        awtMouseInput.setRelativeDelta( glCanvas );
        glCanvas.addMouseListener( awtMouseInput );
        glCanvas.addMouseWheelListener( awtMouseInput );
        glCanvas.addMouseMotionListener( awtMouseInput );
    }

}
