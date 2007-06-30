package org.geotools.renderer3d.impl;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;
import com.jmex.awt.JMECanvas;
import org.geotools.map.MapContext;
import org.geotools.renderer3d.Renderer3D;
import org.geotools.renderer3d.utils.ParameterChecker;
import org.geotools.renderer3d.utils.quadtree.QuadTree;
import org.geotools.renderer3d.utils.quadtree.QuadTreeImpl;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Hans Häggström
 */
public final class Renderer3DImpl
        implements Renderer3D
{

    //======================================================================
    // Private Fields

    private MapContext myMapContext = null;
    private QuadTree myQuadTree;
    private Component myView3D = null;
    private Node myTerrainNode = null;
    private Set<NavigationGesture> myNavigationGestures = new HashSet<NavigationGesture>();
    private Canvas myCanvas = null;
    private CanvasRenderer myCanvasRenderer = null;

    //======================================================================
    // Private Constants

    private static final int DEFAULT_START_RADIUS_M = 10;
    private static final TerrainBlockFactory TERRAIN_BLOCK_FACTORY = new TerrainBlockFactory();
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

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

        // Add default navigation gestures
        addNavigationGesture( new PanGesture() );
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


    public void addNavigationGesture( NavigationGesture addedNavigationGesture )
    {
        ParameterChecker.checkNotNull( addedNavigationGesture, "addedNavigationGesture" );
        ParameterChecker.checkNotAlreadyContained( addedNavigationGesture,
                                                   myNavigationGestures,
                                                   "myNavigationGestures" );

        myNavigationGestures.add( addedNavigationGesture );

        registerNavigationGestureListener( addedNavigationGesture );
    }


    public void removeNavigationGesture( NavigationGesture removedNavigationGesture )
    {
        ParameterChecker.checkNotNull( removedNavigationGesture, "removedNavigationGesture" );
        ParameterChecker.checkContained( removedNavigationGesture,
                                         myNavigationGestures,
                                         "myNavigationGestures" );

        myNavigationGestures.remove( removedNavigationGesture );

        unRegisterNavigationGestureListener( removedNavigationGesture );
    }


    public void removeAllNavigationGestures()
    {
        myNavigationGestures.clear();

        for ( NavigationGesture navigationGesture : myNavigationGestures )
        {
            unRegisterNavigationGestureListener( navigationGesture );
        }
    }

    //======================================================================
    // Private Methods

    private Node createTerrainNode()
    {
        final Node node = new Node();

        final TerrainBlock terrainBlock = (TerrainBlock) myQuadTree.getRootNode().getNodeData();
        node.attachChild( terrainBlock.getSpatial() );

        return node;
    }


    private Component createView3D()
    {
        final int width = DEFAULT_WIDTH;
        final int height = DEFAULT_HEIGHT;

        // REFACTOR: Package the whole 3D canvas into one class, with a awt canvas as output,
        //           and a Spatial node and gesture listeners as input.  Then this class can concentrate on the map integration.

        // Create the 3D canvas
        myCanvas = DisplaySystem.getDisplaySystem( "lwjgl" ).createCanvas( width, height );
        myCanvas.setMinimumSize( new Dimension( 0, 0 ) ); // Make sure it is shrinkable
        final JMECanvas jmeCanvas = ( (JMECanvas) myCanvas );

        // Set the renderer that renders the canvas contents
        myCanvasRenderer = new CanvasRenderer( width, height, get3DNode(), myCanvas );
        jmeCanvas.setImplementor( myCanvasRenderer );

        // Add navigation gesture listeners to the created 3D canvas
        for ( NavigationGesture navigationGesture : myNavigationGestures )
        {
            registerNavigationGestureListener( navigationGesture );
        }

        // We need to repaint the component to see the updates, so we create a repaint calling thread
        final Thread repaintThread = new Thread( new Repainter( myCanvas ) );
        repaintThread.setDaemon( true ); // Do not keep the JVM alive if only the repaint thread is left running
        repaintThread.start();

        return myCanvas;
    }


    private void registerNavigationGestureListener( final NavigationGesture navigationGesture )
    {
        if ( myCanvas != null )
        {
            myCanvas.addMouseMotionListener( navigationGesture );
            myCanvas.addMouseListener( navigationGesture );
            myCanvas.addMouseWheelListener( navigationGesture );
            navigationGesture.setCanvasRenderer( myCanvasRenderer );
        }
    }


    private void unRegisterNavigationGestureListener( final NavigationGesture navigationGesture )
    {
        if ( myCanvas != null )
        {
            myCanvas.removeMouseMotionListener( navigationGesture );
            myCanvas.removeMouseListener( navigationGesture );
            myCanvas.removeMouseWheelListener( navigationGesture );
            navigationGesture.setCanvasRenderer( null );
        }
    }

    //======================================================================
    // Inner Classes

    private static final class Repainter
            implements Runnable
    {

        //======================================================================
        // Private Fields

        private final Canvas myCanvas;

        //======================================================================
        // Public Methods

        //----------------------------------------------------------------------
        // Constructors

        public Repainter( final Canvas canvas )
        {
            myCanvas = canvas;
        }

        //----------------------------------------------------------------------
        // Runnable Implementation

        public void run()
        {
            while ( true )
            {
                myCanvas.repaint();

                try
                {
                    Thread.sleep( 10 );
                }
                catch ( InterruptedException e )
                {
                    // Ignore
                }
            }
        }

    }

}
