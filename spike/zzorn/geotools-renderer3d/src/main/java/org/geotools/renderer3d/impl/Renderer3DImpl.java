package org.geotools.renderer3d.impl;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import org.geotools.map.MapContext;
import org.geotools.renderer3d.Renderer3D;
import org.geotools.renderer3d.navigationgestures.NavigationGesture;
import org.geotools.renderer3d.utils.canvas3d.Canvas3D;
import org.geotools.renderer3d.utils.quadtree.QuadTree;
import org.geotools.renderer3d.utils.quadtree.QuadTreeImpl;

import java.awt.Component;

/**
 * @author Hans Häggström
 */
public final class Renderer3DImpl
        implements Renderer3D
{

    //======================================================================
    // Private Fields

    private final Canvas3D myCanvas3D = new Canvas3D();

    private MapContext myMapContext = null;
    private QuadTree myQuadTree;
    private Node myTerrainNode = null;

    //======================================================================
    // Private Constants

    private static final int DEFAULT_START_RADIUS_M = 10;
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
        initializeTerrainNodeIfNeeded();

        return myCanvas3D.get3DView();
    }


    public Spatial get3DNode()
    {
        initializeTerrainNodeIfNeeded();

        return myTerrainNode;
    }


    public void addNavigationGesture( final NavigationGesture addedNavigationGesture )
    {
        myCanvas3D.addNavigationGesture( addedNavigationGesture );
    }


    public void removeNavigationGesture( final NavigationGesture removedNavigationGesture )
    {
        myCanvas3D.removeNavigationGesture( removedNavigationGesture );
    }


    public void removeAllNavigationGestures()
    {
        myCanvas3D.removeAllNavigationGestures();
    }

    //======================================================================
    // Private Methods

    private void initializeTerrainNodeIfNeeded()
    {
        if ( myTerrainNode == null )
        {
            myTerrainNode = createTerrainNode();

            myCanvas3D.set3DNode( myTerrainNode );
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

}
