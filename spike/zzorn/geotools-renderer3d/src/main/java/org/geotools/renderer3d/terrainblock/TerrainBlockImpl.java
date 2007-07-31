package org.geotools.renderer3d.terrainblock;

import com.jme.scene.Spatial;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

import java.awt.Image;

/**
 * @author Hans H�ggstr�m
 */
public final class TerrainBlockImpl
        implements TerrainBlock
{

    //======================================================================
    // Private Fields

    private final QuadTreeNode myQuadTreeNode;
    private final int myNumberOfGridsPerSide;

    private TerrainMesh myTerrainMesh = null;
    private Image myMapImage = null;

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
        if ( myTerrainMesh == null )
        {
            myTerrainMesh = createTerrainMesh();
        }

        return myTerrainMesh;
    }

    public Image getMapImage()
    {
        return myMapImage;
    }

    public void setMapImage( final Image mapImage )
    {
        myMapImage = mapImage;

        if ( myTerrainMesh != null )
        {
            myTerrainMesh.setTextureImage( mapImage );
        }
    }

    //======================================================================
    // Private Methods

    private TerrainMesh createTerrainMesh()
    {
        final TerrainMesh terrainMesh = new TerrainMesh( myNumberOfGridsPerSide,
                                                         myNumberOfGridsPerSide,
                                                         myQuadTreeNode.getBounds().getX1(),
                                                         myQuadTreeNode.getBounds().getY1(),
                                                         myQuadTreeNode.getBounds().getX2(),
                                                         myQuadTreeNode.getBounds().getY2(),
                                                         0 );

        terrainMesh.setTextureImage( myMapImage );

/* // DEBUG
        terrainMesh.setRandomColors();
*/

        return terrainMesh;
    }

}

