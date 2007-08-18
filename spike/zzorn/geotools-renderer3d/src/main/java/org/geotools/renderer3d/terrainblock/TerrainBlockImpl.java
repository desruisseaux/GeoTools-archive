package org.geotools.renderer3d.terrainblock;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import org.geotools.renderer3d.provider.texture.impl.TextureListener;
import org.geotools.renderer3d.provider.texture.impl.TextureProvider;
import org.geotools.renderer3d.utils.BoundingRectangle;
import org.geotools.renderer3d.utils.ParameterChecker;
import org.geotools.renderer3d.utils.quadtree.NodeListener;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
        implements TerrainBlock, NodeListener<TerrainBlock>
{

    //======================================================================
    // Private Fields

    private final int myNumberOfGridsPerSide;
    private final BufferedImage myTextureImage;

    private final TextureProvider myTextureProvider;
    private final TextureListener myTextureListener = new TextureListener()
    {

        public void onTextureReady( final BoundingRectangle area, final BufferedImage texture )
        {
            // The texture has been calculated to the image instance we hold in this terrain block,
            // so just apply it to the terrain mesh if it has been created already.
            if ( myTerrainMesh != null )
            {
                myTerrainMesh.setTextureImage( myTextureImage );
            }

            myHasCalculatedTextureImage = true;
        }

    };
    private final int myTextureSize;

    private Vector3f myCenter;

    private QuadTreeNode<TerrainBlock> myQuadTreeNode;

    private List<Spatial> myChildNodeSpatials = new ArrayList<Spatial>( 4 );

    private TerrainMesh myTerrainMesh = null;
    private Node myTerrain3DNode = null;

    private boolean myHasCalculatedTextureImage = false;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param quadTreeNode
     * @param numberOfGridsPerSide number of grid cells along the side of the TerrainBlock.
     * @param textureSize
     * @param textureProvider
     */
    public TerrainBlockImpl( final QuadTreeNode<TerrainBlock> quadTreeNode,
                             final int numberOfGridsPerSide,
                             final int textureSize,
                             final TextureProvider textureProvider )
    {
        ParameterChecker.checkNotNull( quadTreeNode, "quadTreeNode" );
        ParameterChecker.checkPositiveNonZeroInteger( numberOfGridsPerSide, "numberOfGridsPerSide" );
        ParameterChecker.checkPositiveNonZeroInteger( textureSize, "textureSize" );
        ParameterChecker.checkNotNull( textureProvider, "textureProvider" );

        myQuadTreeNode = quadTreeNode;
        myNumberOfGridsPerSide = numberOfGridsPerSide;
        myTextureProvider = textureProvider;
        myTextureSize = textureSize;

        myTextureImage = allocateTextureImage( textureSize );

        updateDerivedData();

        myQuadTreeNode.addNodeListener( this );
    }

    //----------------------------------------------------------------------
    // NodeListener Implementation

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
        return myCenter;
    }


    public void updateDerivedData()
    {
        // Remove previous texture request, if found
        myTextureProvider.cancelRequest( myTextureListener );

        // Update center
        final BoundingRectangle bounds = myQuadTreeNode.getBounds();
        myCenter = new Vector3f( (float) bounds.getCenterX(),
                                 (float) bounds.getCenterY(),
                                 0 ); // TODO: Get ground height at center.

        // Copy a chunk of a previously calculated parent block texture to the texture of this block
        initializeTextureFromParentTexture();

        // Update terrain mesh if present
        if ( myTerrainMesh != null )
        {
            myTerrainMesh.updateBounds( bounds.getX1(), bounds.getY1(), bounds.getX2(), bounds.getY2() );
            myTerrainMesh.setTextureImage( myTextureImage );
        }

        // Make sure the node is collapsed
        onCollapsed( myQuadTreeNode );

        // Request texture for terrain block
        myTextureProvider.requestTexture( bounds, myTextureImage, myTextureListener );
    }


    public BufferedImage getTextureImage()
    {
        return myTextureImage;
    }


    public boolean hasCalculatedTextureImage()
    {
        return myHasCalculatedTextureImage;
    }

    public void clearPicture()
    {
        fillTextureImageWithLoadingGraphics();
    }

    //======================================================================
    // Private Methods

    private void initializeTextureFromParentTexture()
    {
        fillTextureImageWithLoadingGraphics();
        myHasCalculatedTextureImage = false;

/*
        final BoundingRectangle textureBounds = new BoundingRectangleImpl( 0, 0, myTextureSize, myTextureSize );
        initializeTextureFromParentTexture( myQuadTreeNode, textureBounds );
*/
    }


    /**
     * Recursive function.  Gets the texture from a smaller part of the parents parent texture,
     * if parent doesn't have a calculated texture, and so on.
     */
    private void initializeTextureFromParentTexture( final QuadTreeNode<TerrainBlock> node,
                                                     final BoundingRectangle textureBounds )
    {
        final QuadTreeNode<TerrainBlock> parentNode = node.getParent();
        if ( parentNode == null )
        {
            // Not even the root had a calculated texture yet, use placeholder graphics
            fillTextureImageWithLoadingGraphics();
        }
        else
        {
            // Calculate the texture area to get from the parent:
            final int quadrant = parentNode.getIndexOfChild( node );
            if ( quadrant >= 0 )
            {

                final BoundingRectangle parentTextureSourceArea = textureBounds.createSubquadrantBoundingRectangle(
                        quadrant );

                final TerrainBlock parentBlock = parentNode.getNodeData();
                if ( parentBlock.hasCalculatedTextureImage() )
                {
                    // Copy the texture for the section that this terrain block occupies from the parent texture
                    final BufferedImage parentImage = parentBlock.getTextureImage();

                    final Graphics2D graphics = (Graphics2D) myTextureImage.getGraphics();
                    graphics.drawImage( parentImage,
                                        0, 0, myTextureImage.getWidth(), myTextureImage.getHeight(),
                                        (int) parentTextureSourceArea.getX1(),
                                        (int) parentTextureSourceArea.getY1(),
                                        (int) parentTextureSourceArea.getX2(),
                                        (int) parentTextureSourceArea.getY2(),
                                        null );
                }
                else
                {
                    // The parent had no calculated texture, so see if its parent has.
                    initializeTextureFromParentTexture( parentNode, textureBounds );
                }
            }
            else
            {
                fillTextureImageWithLoadingGraphics();
            }
        }
    }


    private void fillTextureImageWithLoadingGraphics()
    {
        final Graphics graphics = myTextureImage.getGraphics();

        // Background color
        graphics.setColor( Color.GRAY );
        graphics.fillRect( 0, 0, myTextureImage.getWidth(), myTextureImage.getHeight() );

        // Text
        graphics.setColor( Color.BLACK );
        graphics.drawString( "Rendering Texture", 0, myTextureImage.getHeight() / 2 );
    }


    private BufferedImage allocateTextureImage( final int textureSize )
    {
        // Try to allocate the texture space, if we are out of memory just use null (a placeholder texture will be used)
        BufferedImage mapImage;
        try
        {
            mapImage = new BufferedImage( textureSize, textureSize, BufferedImage.TYPE_4BYTE_ABGR );
        }
        catch ( OutOfMemoryError e )
        {
            mapImage = null;
        }

        return mapImage;
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

        return myTerrainMesh;
    }


    private TerrainMesh createTerrainMesh()
    {
        final float z = 0;
        final TerrainMesh terrainMesh = new TerrainMesh( myNumberOfGridsPerSide,
                                                         myNumberOfGridsPerSide,
                                                         myQuadTreeNode.getBounds().getX1(),
                                                         myQuadTreeNode.getBounds().getY1(),
                                                         myQuadTreeNode.getBounds().getX2(),
                                                         myQuadTreeNode.getBounds().getY2(),
                                                         z );

        terrainMesh.setTextureImage( myTextureImage );

        return terrainMesh;
    }

}

