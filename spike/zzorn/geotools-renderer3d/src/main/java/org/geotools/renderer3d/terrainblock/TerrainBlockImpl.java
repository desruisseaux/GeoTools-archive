package org.geotools.renderer3d.terrainblock;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import org.geotools.renderer3d.field.TextureListener;
import org.geotools.renderer3d.utils.BoundingRectangle;
import org.geotools.renderer3d.utils.ParameterChecker;
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
    private final Vector3f myCenter;

    private QuadTreeNode<TerrainBlock> myQuadTreeNode;

    private List<Spatial> myChildNodeSpatials = new ArrayList<Spatial>( 4 );

    private TerrainMesh myTerrainMesh = null;
    private Node myTerrain3DNode = null;
    private Image myMapImage = null;

    private boolean myDelted = false;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param quadTreeNode
     * @param numberOfGridsPerSide number of grid cells along the side of the TerrainBlock.
     */
    public TerrainBlockImpl( final QuadTreeNode<TerrainBlock> quadTreeNode, final int numberOfGridsPerSide )
    {
        ParameterChecker.checkNotNull( quadTreeNode, "quadTreeNode" );

        myQuadTreeNode = quadTreeNode;
        myNumberOfGridsPerSide = numberOfGridsPerSide;
        myCenter = new Vector3f( (float) quadTreeNode.getBounds().getCenterX(),
                                 (float) quadTreeNode.getBounds().getCenterY(),
                                 0 ); // TODO: Get ground height at center.

        myQuadTreeNode.addNodeListener( this );
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
        final TerrainMesh terrainMesh = myTerrainMesh;
        return terrainMesh;
    }


    public Vector3f getCenter()
    {
        checkIfDeleted();

        return myCenter;
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


    public void setMapImage( final Image mapImage )
    {
        checkIfDeleted();

        myMapImage = mapImage;

        if ( myTerrainMesh != null )
        {
            myTerrainMesh.setTextureImage( mapImage );
        }
    }

    //======================================================================
    // Private Methods

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
                                                         0 );

        terrainMesh.setTextureImage( myMapImage );

/* // DEBUG
        terrainMesh.setRandomColors();
*/

        return terrainMesh;
    }

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
}

