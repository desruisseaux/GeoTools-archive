package org.geotools.renderer3d.impl;

import org.geotools.map.MapContext;
import org.geotools.renderer3d.utils.quadtree.QuadTree;
import org.geotools.renderer3d.utils.quadtree.QuadTreeImpl;
import org.geotools.renderer3d.Renderer3D;

import javax.swing.*;

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


    public JComponent get3DView()
    {
        return new JLabel( "No 3D view yet" );
        // IMPLEMENT
    }

}
