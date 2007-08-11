package org.geotools.renderer3d.terrainblock;

import com.jme.image.Texture;
import org.geotools.renderer3d.field.TextureProvider;
import org.geotools.renderer3d.utils.Pool;
import org.geotools.renderer3d.utils.PoolItemFactory;
import org.geotools.renderer3d.utils.quadtree.NodeDataFactory;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Creates terrain blocks for areas specified by quad tree nodes.
 *
 * @author Hans Häggström
 */
public final class TerrainBlockFactory
        implements NodeDataFactory<TerrainBlock>
{

    //======================================================================
    // Private Fields

    private final int myNumberOfGridsPerSide;

    private final TextureProvider myTextureProvider;
    private final int myTextureSize;

    private final Pool<BufferedImage> myTextureImagePool;
    private final Pool<Texture> myTexturePool;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param numberOfGridsPerSide number of grid cells along the side of a TerrainBlock.
     * @param textureProvider      something that supplies the textures for terrain blocks.
     * @param textureSize          number of pixels per side for the texture.
     */
    public TerrainBlockFactory( final int numberOfGridsPerSide,
                                final TextureProvider textureProvider,
                                final int textureSize )
    {
        myNumberOfGridsPerSide = numberOfGridsPerSide;
        myTextureProvider = textureProvider;
        myTextureSize = textureSize;

        myTextureImagePool = new Pool<BufferedImage>( new PoolItemFactory<BufferedImage>()
        {
            public BufferedImage create()
            {
                return new BufferedImage( myTextureSize, myTextureSize, BufferedImage.TYPE_4BYTE_ABGR );
            }
        } );

        myTexturePool = new Pool<Texture>( new PoolItemFactory<Texture>()
        {
            public Texture create()
            {
                return null;
            }
        } );
    }

    //----------------------------------------------------------------------
    // NodeDataFactory ImplementationK}}

    //======================================================================
    // Public Methods

    public TerrainBlock createNodeDataObject( final QuadTreeNode<TerrainBlock> node )
    {
        System.out.println( "TerrainBlockFactory.createNodeDataObject" );
        System.out.println( "node.getBounds() = " + node.getBounds() );

        final TerrainBlockImpl terrainBlock = new TerrainBlockImpl( node,
                                                                    myNumberOfGridsPerSide,
                                                                    myTexturePool,
                                                                    myTextureImagePool );

        // TODO: First assign the block the texture (and elevation?) of the correct sub-quadrant of the parent node
        // (or an eight of the grandparent and so on, if the parent hasn't rendered yet), and start rendering the
        // texture of the node in a rendering thread.  When it is rendered, create a new texture from the rendered image
        // and assign it to the node.  Same with retrieving the elevation data.
        // Note that data retrieval / rendering can be very slow, as it can be e.g. fetched over the network.

        // Create texture
        final BufferedImage buffer = myTextureImagePool.getItem();
        final Graphics graphics = buffer.getGraphics();
        graphics.setColor( Color.BLUE );
        graphics.fillRect( 0, 0, myTextureSize, myTextureSize );
        graphics.setColor( Color.WHITE );
        graphics.drawRect( 0, 0, myTextureSize - 1, myTextureSize - 1 );

        // TODO: Get texture of parent block, and copy the correct area of it to this terrain block texture
        // TODO: If no parent is available, create a placeholder image

        terrainBlock.setMapImage( buffer );

        // Create texture for terrain block
        myTextureProvider.requestTexture( node.getBounds(), buffer, terrainBlock );

        return terrainBlock;
    }

}
