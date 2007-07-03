package org.geotools.renderer3d.terrainblock;

import com.jme.scene.Spatial;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

/**
 * @author Hans Häggström
 */
public class TerrainBlockImpl
        implements TerrainBlock
{

    //======================================================================
    // Private Fields

    private final QuadTreeNode myQuadTreeNode;
    private final int myNumberOfGridsPerSide;

    private Spatial mySpatial = null;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param quadTreeNode
     * @param numberOfGridsPerSide number of grid cells along the side of the TerrainBlock.
     */
    public TerrainBlockImpl( final QuadTreeNode quadTreeNode, final int numberOfGridsPerSide )
    {
        myQuadTreeNode = quadTreeNode;
        myNumberOfGridsPerSide = numberOfGridsPerSide;
    }

    //----------------------------------------------------------------------
    // TerrainBlock Implementation

    public Spatial getSpatial()
    {
        if ( mySpatial == null )
        {
            mySpatial = createSpatial();
        }

        return mySpatial;
    }

    //======================================================================
    // Private Methods

    private Spatial createSpatial()
    {
        final TerrainMesh terrainMesh = new TerrainMesh( myNumberOfGridsPerSide,
                                                         myNumberOfGridsPerSide,
                                                         myQuadTreeNode.getX1(),
                                                         myQuadTreeNode.getY1(),
                                                         myQuadTreeNode.getX2(),
                                                         myQuadTreeNode.getY2(),
                                                         0 );

        terrainMesh.setRandomColors();
        return terrainMesh;
    }

}

