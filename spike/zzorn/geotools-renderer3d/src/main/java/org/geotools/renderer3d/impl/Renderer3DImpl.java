package org.geotools.renderer3d.impl;

import com.jme.input.InputSystem;
import com.jme.input.KeyInput;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;
import com.jmex.awt.JMECanvas;
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
    private Node myTerrainNode;

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


    public Spatial get3DNode()
    {
        if ( myTerrainNode == null )
        {
            myTerrainNode = createTerrainNode();
        }

        return myTerrainNode;
    }

    //======================================================================
    // Private Methods

    /**
     * Set up a canvas to fire mouse events via the input system.
     *
     * @param glCanvas canvas that should be listened to
     * @param dragOnly true to enable mouse input to jME only when the mouse is dragged
     */
    private static void setupMouse( Canvas glCanvas, boolean dragOnly )
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


    private Node createTerrainNode()
    {
        final Node node = new Node();

        final TerrainBlock terrainBlock = (TerrainBlock) myQuadTree.getRootNode().getNodeData();
        node.attachChild( terrainBlock.getSpatial() );

        return node;
    }


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
        jmeCanvas.setImplementor( new CanvasRenderer( width, height, get3DNode() ) );
        //jmeCanvas.setBackground( Color.GRAY );
        jmeCanvas.setUpdateInput( true );

        // We need to repaint the component to see the updates.
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

}
