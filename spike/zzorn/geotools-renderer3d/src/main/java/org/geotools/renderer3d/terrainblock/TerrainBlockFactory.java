package org.geotools.renderer3d.terrainblock;

import org.geotools.renderer3d.utils.quadtree.NodeDataFactory;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

/**
 * Creates terrain blocks for areas specified by quad tree nodes.
 *
 * @author Hans Häggström
 */
public class TerrainBlockFactory
        implements NodeDataFactory
{

    //======================================================================
    // Private Fields

    private final int myNumberOfGridsPerSide;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param numberOfGridsPerSide number of grid cells along the side of a TerrainBlock.
     */
    public TerrainBlockFactory( final int numberOfGridsPerSide )
    {
        myNumberOfGridsPerSide = numberOfGridsPerSide;
    }

    //----------------------------------------------------------------------
    // NodeDataFactory Implementation

    //======================================================================
    // Public Methods

    public Object createNodeDataObject( final QuadTreeNode node )
    {
        return new TerrainBlockImpl( node, myNumberOfGridsPerSide );
    }

}
