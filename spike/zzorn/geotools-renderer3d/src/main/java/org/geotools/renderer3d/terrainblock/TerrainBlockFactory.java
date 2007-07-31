package org.geotools.renderer3d.terrainblock;

import org.geotools.renderer3d.utils.quadtree.NodeDataFactory;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

/**
 * Creates terrain blocks for areas specified by quad tree nodes.
 *
 * @author Hans Häggström
 */
public final class TerrainBlockFactory
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
    // NodeDataFactory ImplementationK}}

    //======================================================================
    // Public Methods

    public Object createNodeDataObject( final QuadTreeNode node )
    {
        return new TerrainBlockImpl( node, myNumberOfGridsPerSide );

        //Coverage

        // TODO: First assign the block the texture (and elevation?) of the correct sub-quadrant of the parent node
        // (or an eight of the grandparent and so on, if the parent hasn't rendered yet), and start rendering the
        // texture of the node in a rendering thread.  When it is rendered, create a new texture from the rendered image
        // and assign it to the node.  Same with retrieving the elevation data.
        // Note that data retrieval / rendering can be very slow, as it can be e.g. fetched over the network.
    }

}
