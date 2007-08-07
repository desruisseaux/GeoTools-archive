package org.geotools.renderer3d.field;

import org.geotools.renderer3d.utils.BoundingRectangle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A TextureProvider implementation for testing purposes.
 * Outputs a texture with the the center coordinates of the requested area.
 *
 * @author Hans Häggström
 */
public final class DummyTextureProvider
        implements TextureProvider
{

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // TextureProvider Implementation

    public void requestTexture( final BoundingRectangle area,
                                final BufferedImage buffer,
                                final TextureListener textureListener )
    {
        final Graphics2D graphics = (Graphics2D) buffer.getGraphics();

        final int textureWidth = buffer.getWidth();
        final int textureHeight = buffer.getHeight();

        graphics.setColor( Color.BLUE );
        graphics.fillRect( 0, 0, textureWidth, textureHeight );
        graphics.setColor( Color.WHITE );
        graphics.drawRect( 0, 0, textureWidth - 1, textureHeight - 1 );
        graphics.drawString( "(" + area.getCenterX() + ", " + area.getCenterY() + ")",
                             textureWidth / 2,
                             textureHeight / 2 );

        textureListener.onTextureReady( area, buffer );
    }

}
