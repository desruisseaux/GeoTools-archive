package org.geotools.renderer3d.terrainblock;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import org.geotools.renderer3d.field.TextureListener;
import org.geotools.renderer3d.utils.BoundingRectangle;
import org.geotools.renderer3d.utils.ParameterChecker;
import org.geotools.renderer3d.utils.Pool;
import org.geotools.renderer3d.utils.quadtree.NodeListener;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Store the texture image related to the terrain block in this class, create it here (based on parent block one),
 * apply it to the mesh, and pass a reference of it to the texture calculation method.  That way texture memory management
 * will be simplified.
 *
 * @author Hans Häggström
 */
public final class TerrainBlockImpl
        implements TerrainBlock, TextureListener, NodeListener<TerrainBlock>
{

    //======================================================================
    // Private Fields

    private final int myNumberOfGridsPerSide;
    private final Pool<Texture> myTexturePool;
    private final Pool<BufferedImage> myTextureImagePool;

    private Vector3f myCenter;

    private QuadTreeNode<TerrainBlock> myQuadTreeNode;

    private List<Spatial> myChildNodeSpatials = new ArrayList<Spatial>( 4 );

    private TerrainMesh myTerrainMesh = null;
    private Node myTerrain3DNode = null;
    private BufferedImage myMapImage = null;

    private boolean myDelted = false;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param quadTreeNode
     * @param numberOfGridsPerSide number of grid cells along the side of the TerrainBlock.
     * @param texturePool
     * @param textureImagePool
     */
    public TerrainBlockImpl( final QuadTreeNode<TerrainBlock> quadTreeNode,
                             final int numberOfGridsPerSide,
                             final Pool<Texture> texturePool,
                             final Pool<BufferedImage> textureImagePool )
    {
        ParameterChecker.checkNotNull( quadTreeNode, "quadTreeNode" );
        ParameterChecker.checkPositiveNonZeroInteger( numberOfGridsPerSide, "numberOfGridsPerSide" );
        ParameterChecker.checkNotNull( texturePool, "texturePool" );
        ParameterChecker.checkNotNull( textureImagePool, "textureImagePool" );

        myQuadTreeNode = quadTreeNode;
        myNumberOfGridsPerSide = numberOfGridsPerSide;
        myTexturePool = texturePool;
        myTextureImagePool = textureImagePool;

        updateDerivedData();

        myQuadTreeNode.addNodeListener( this );
    }

    //----------------------------------------------------------------------
    // NodeListener Implementation

    public void onDeleted( QuadTreeNode<TerrainBlock> quadTreeNode )
    {
        checkIfDeleted();

        if ( myTerrain3DNode != null )
        {
            myTerrain3DNode.detachAllChildren();
        }

        myQuadTreeNode = null;
        myMapImage = null;
        myTerrainMesh = null;
        myTerrain3DNode = null;

        myDelted = true;
    }


    public void onCollapsed( QuadTreeNode<TerrainBlock> quadTreeNode )
    {
        if ( myTerrain3DNode != null )
        {
            for ( Spatial childNodeSpatial : myChildNodeSpatials )
            {
                myTerrain3DNode.detachChild( childNodeSpatial );
            }
            myChildNodeSpatials.clear();

            myTerrain3DNode.attachChild( getOrCreateTerrainMesh() );
        }
    }


    public void onExpanded( QuadTreeNode<TerrainBlock> quadTreeNode )
    {
        if ( myTerrain3DNode != null )
        {
            if ( myTerrainMesh != null )
            {
                myTerrain3DNode.detachChild( myTerrainMesh );
            }

            attachChildNodeSpatials();
        }
    }

    //----------------------------------------------------------------------
    // TerrainBlock Implementation

    public Spatial getSpatial()
    {
        checkIfDeleted();

        if ( myTerrain3DNode == null )
        {
            myTerrain3DNode = new Node();

            if ( myQuadTreeNode.isExpanded() )
            {
                attachChildNodeSpatials();
            }
            else
            {
                myTerrain3DNode.attachChild( getOrCreateTerrainMesh() );
            }
        }


        return myTerrain3DNode;
    }


    public Vector3f getCenter()
    {
        checkIfDeleted();

        return myCenter;
    }


    public void updateDerivedData()
    {
        final BoundingRectangle bounds = myQuadTreeNode.getBounds();
        myCenter = new Vector3f( (float) bounds.getCenterX(),
                                 (float) bounds.getCenterY(),
                                 0 ); // TODO: Get ground height at center.

        if ( myTerrainMesh != null )
        {
            myTerrainMesh.updateBounds( bounds.getX1(), bounds.getY1(), bounds.getX2(), bounds.getY2() );
            myTerrainMesh.setTextureImage( myMapImage );
            myMapImage = null;
        }

        // Make sure the node is collapsed
        onCollapsed( myQuadTreeNode );
    }

    //----------------------------------------------------------------------
    // TextureListener Implementation

    public void onTextureReady( final BoundingRectangle area, final BufferedImage texture )
    {
        if ( !myDelted )
        {
            setMapImage( texture );
        }
    }

    //----------------------------------------------------------------------
    // Other Public Methods

    public Image getMapImage()
    {
        checkIfDeleted();

        return myMapImage;
    }


    public void setMapImage( final BufferedImage mapImage )
    {
        checkIfDeleted();

        if ( myTerrainMesh != null )
        {
            myTerrainMesh.setTextureImage( mapImage );
        }
        else
        {
            myMapImage = mapImage;
        }
    }

    //======================================================================
    // Private Methods

    private void attachChildNodeSpatials()
    {
        myChildNodeSpatials.clear();
        for ( int i = 0; i < myQuadTreeNode.getNumberOfChildren(); i++ )
        {
            final QuadTreeNode<TerrainBlock> child = myQuadTreeNode.getChild( i );
            if ( child != null )
            {
                final TerrainBlock childBlock = child.getNodeData();
                if ( childBlock != null )
                {
                    final Spatial childSpatial = childBlock.getSpatial();
                    myChildNodeSpatials.add( childSpatial );
                    myTerrain3DNode.attachChild( childSpatial );
                }
            }
        }
    }


    private TerrainMesh getOrCreateTerrainMesh()
    {
        if ( myTerrainMesh == null )
        {
            myTerrainMesh = createTerrainMesh();
        }

        return myTerrainMesh;
    }


    private void checkIfDeleted()
    {
        if ( myDelted )
        {
            throw new IllegalStateException( "Can not access a deleted TerrainBlock anymore." );
        }
    }


    private TerrainMesh createTerrainMesh()
    {
        final TerrainMesh terrainMesh = new TerrainMesh( myNumberOfGridsPerSide,
                                                         myNumberOfGridsPerSide,
                                                         myQuadTreeNode.getBounds().getX1(),
                                                         myQuadTreeNode.getBounds().getY1(),
                                                         myQuadTreeNode.getBounds().getX2(),
                                                         myQuadTreeNode.getBounds().getY2(),
                                                         0,
                                                         myTextureImagePool );

        terrainMesh.setTextureImage( myMapImage );
        myMapImage = null;

/* // DEBUG
        terrainMesh.setRandomColors();
*/

        return terrainMesh;
    }

}

