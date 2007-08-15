package org.geotools.renderer3d.field;

import org.geotools.renderer3d.utils.BoundingRectangle;
import org.geotools.renderer3d.utils.MathUtils;

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

        // Fill with a gradient that depends on x coordinate.
        for ( int x = 0; x < textureWidth; x++ )
        {
            final Color color = makeCoordinateColor( MathUtils.interpolate( x,
                                                                            0,
                                                                            textureWidth,
                                                                            area.getX1(),
                                                                            area.getX2() ) );
            graphics.setColor( color );
            graphics.drawLine( x, 0, x, textureHeight );
        }

        graphics.setColor( Color.WHITE );
        graphics.drawRect( 0, 0, textureWidth - 1, textureHeight - 1 );
        graphics.drawString( "x:" + area.getCenterX() + ", y:" + area.getCenterY(),
                             textureWidth / 8,
                             textureHeight / 2 );

        textureListener.onTextureReady( area, buffer );
    }

    private Color makeCoordinateColor( double x )
    {
        x = Math.abs( x );
        int i = (int) x;
        return new Color( i % 255, ( i / 10 ) % 255, ( i / 100 ) % 255 );
    }

}
