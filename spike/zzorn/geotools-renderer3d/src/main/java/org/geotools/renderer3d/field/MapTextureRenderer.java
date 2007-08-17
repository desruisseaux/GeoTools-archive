package org.geotools.renderer3d.field;

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
        graphics.setColor( myBackgroundColor );

        graphics.fillRect( 0, 0, width, height );

        // Create the source and destination areas
        final Rectangle targetArea = new Rectangle( width, height );
        final ReferencedEnvelope sourceArea = new ReferencedEnvelope( area.getX1(),
                                                                      area.getX2(),
                                                                      area.getY1(),
                                                                      area.getY2(),
                                                                      myMap.getCoordinateReferenceSystem() );

        // Render
        myStreamingRenderer.paint( graphics, targetArea, sourceArea );
    }

}
