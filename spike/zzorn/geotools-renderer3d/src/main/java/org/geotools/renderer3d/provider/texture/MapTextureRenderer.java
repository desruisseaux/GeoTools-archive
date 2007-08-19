package org.geotools.renderer3d.provider.texture;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.renderer3d.utils.BoundingRectangle;
import org.geotools.renderer3d.utils.ParameterChecker;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * A renderer that can render parts of a GeoTools MapContext to requesed textures.
 *
 * @author Hans Häggström
 */
public final class MapTextureRenderer
        implements TextureRenderer
{

    //======================================================================
    // Private Fields

    private final MapContext myMap;
    private final StreamingRenderer myStreamingRenderer = new StreamingRenderer();
    private final Color myBackgroundColor;
    private final double myScaleX;
    private final double myScaleY;
    private final double myTranslateX;
    private final double myTranslateY;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param map             the map to render from
     * @param backgroundColor a color to fill the underlying map with.
     */
    public MapTextureRenderer( final MapContext map, final Color backgroundColor )
    {
        ParameterChecker.checkNotNull( map, "map" );
        ParameterChecker.checkNotNull( backgroundColor, "backgroundColor" );

        myMap = map;
        myBackgroundColor = backgroundColor;

        myScaleX = 0.001;
        myScaleY = 0.001;
        myTranslateX = -100;
        myTranslateY = 30;


        myStreamingRenderer.setContext( map );
        myStreamingRenderer.setInteractive( false );
        myStreamingRenderer.setJava2DHints( new RenderingHints( RenderingHints.KEY_ANTIALIASING,
                                                                RenderingHints.VALUE_ANTIALIAS_ON ) );
    }

    //----------------------------------------------------------------------
    // TextureRenderer Implementation

    public void renderArea( final BoundingRectangle area, final BufferedImage target )
    {
        final int width = target.getWidth();
        final int height = target.getHeight();

        final Graphics2D graphics = (Graphics2D) target.getGraphics();

        // Clear to color
        graphics.setColor( myBackgroundColor );
        graphics.fillRect( 0, 0, width, height );

        final BoundingRectangle transformedArea = area.transform( myTranslateX, myTranslateY, myScaleX, myScaleY );

        // Create the source and destination areas
        final Rectangle targetArea = new Rectangle( width, height );
        final ReferencedEnvelope sourceArea = new ReferencedEnvelope( transformedArea.getX1(),
                                                                      transformedArea.getX2(),
                                                                      transformedArea.getY1(),
                                                                      transformedArea.getY2(),
                                                                      myMap.getCoordinateReferenceSystem() );

        // Render
        myStreamingRenderer.paint( graphics, targetArea, sourceArea );

/*
        graphics.setColor( Color.BLACK );
        graphics.drawRect( 0,0,width-1, height-1 );
*/

/*
        try
        {
            Thread.sleep( 100);
        }
        catch ( InterruptedException e )
        {
            
        }
*/
    }

}
