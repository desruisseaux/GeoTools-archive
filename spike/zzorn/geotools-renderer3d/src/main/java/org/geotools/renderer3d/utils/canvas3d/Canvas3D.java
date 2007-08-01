package org.geotools.renderer3d.utils.canvas3d;

import com.jme.renderer.Camera;
import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;
import com.jmex.awt.JMECanvas;
import org.geotools.renderer3d.navigationgestures.CameraAccessor;
import org.geotools.renderer3d.navigationgestures.NavigationGesture;
import org.geotools.renderer3d.navigationgestures.PanGesture;
import org.geotools.renderer3d.navigationgestures.RotateGesture;
import org.geotools.renderer3d.utils.ParameterChecker;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

/**
 * A 3D Canvas, showing a 3D object in an AWT Canvas component.
 * <p/>
 * Allows registering Gestures, that can be used to navigate the 3D view (already has default gestures registered).
 *
 * @author Hans Häggström
 */
public final class Canvas3D
{

    //======================================================================
    // Private Fields

    private final Set<NavigationGesture> myNavigationGestures = new HashSet<NavigationGesture>();
    private final CameraAccessor myCameraAccessor = new CameraAccessor()
    {

        public Camera getCamera()
        {
            if ( myCanvasRenderer != null )
            {
                return myCanvasRenderer.getCamera();
            }
            else
            {
                return null;
            }
        }

    };

    private Spatial my3DNode = null;
    private Component myView3D = null;
    private Canvas myCanvas = null;
    private CanvasRenderer myCanvasRenderer = null;

    //======================================================================
    // Private Constants

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    public Canvas3D()
    {
        this( null );
    }


    public Canvas3D( final Spatial a3dNode )
    {
        my3DNode = a3dNode;

        // Add default navigation gestures
        addNavigationGesture( new PanGesture() );
        addNavigationGesture( new RotateGesture() );
    }

    //----------------------------------------------------------------------
    // Other Public Methods

    public void set3DNode( final Spatial a3dNode )
    {
        my3DNode = a3dNode;

        if ( myCanvasRenderer != null )
        {
            myCanvasRenderer.setCanvasRootNode( a3dNode );
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

    private void registerNavigationGestureListener( final NavigationGesture navigationGesture )
    {
        if ( myCanvas != null )
        {
            myCanvas.addMouseMotionListener( navigationGesture );
            myCanvas.addMouseListener( navigationGesture );
            myCanvas.addMouseWheelListener( navigationGesture );
            navigationGesture.setCameraAccessor( myCameraAccessor );
        }
    }


    private void unRegisterNavigationGestureListener( final NavigationGesture navigationGesture )
    {
        if ( myCanvas != null )
        {
            myCanvas.removeMouseMotionListener( navigationGesture );
            myCanvas.removeMouseListener( navigationGesture );
            myCanvas.removeMouseWheelListener( navigationGesture );
            navigationGesture.setCameraAccessor( null );
        }
    }


    private Component createView3D()
    {
        final int width = DEFAULT_WIDTH;
        final int height = DEFAULT_HEIGHT;

        // Create the 3D canvas
        myCanvas = DisplaySystem.getDisplaySystem( "lwjgl" ).createCanvas( width, height );
        myCanvas.setMinimumSize( new Dimension( 0, 0 ) ); // Make sure it is shrinkable
        final JMECanvas jmeCanvas = ( (JMECanvas) myCanvas );

        // Set the renderer that renders the canvas contents
        myCanvasRenderer = new CanvasRenderer( width, height, my3DNode, myCanvas );
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
