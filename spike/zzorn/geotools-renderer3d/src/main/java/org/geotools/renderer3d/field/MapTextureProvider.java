package org.geotools.renderer3d.field;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.renderer3d.utils.BoundingRectangle;
import org.geotools.renderer3d.utils.ParameterChecker;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Hans Häggström
 */
public final class MapTextureProvider
        implements TextureProvider
{

    //======================================================================
    // Private Fields

    private final MapContext myMap;
    private final StreamingRenderer myStreamingRenderer = new StreamingRenderer();
    private final Color myBackgroundColor;
    private final List<PaintJob> myPaintJobs = new LinkedList<PaintJob>();

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param map             the map to render from
     * @param backgroundColor
     */
    public MapTextureProvider( final MapContext map, final Color backgroundColor )
    {
        ParameterChecker.checkNotNull( map, "map" );
        ParameterChecker.checkNotNull( backgroundColor, "backgroundColor" );

        myMap = map;
        myBackgroundColor = backgroundColor;

        myStreamingRenderer.setContext( map );
        myStreamingRenderer.setInteractive( false );
        myStreamingRenderer.setJava2DHints( new RenderingHints( RenderingHints.KEY_ANTIALIASING,
                                                                RenderingHints.VALUE_ANTIALIAS_ON ) );

        // Start a thread that handles texture painting jobs
        final Thread renderThread = new Thread( new MyTextureRenderer() );
        renderThread.setDaemon( true );
        renderThread.start();
    }

    //----------------------------------------------------------------------
    // TextureProvider Implementation

    public void requestTexture( final BoundingRectangle area,
                                final BufferedImage buffer,
                                final TextureListener textureListener )
    {
        synchronized ( myPaintJobs )
        {
            myPaintJobs.add( new PaintJob( area, buffer, textureListener ) );
            myPaintJobs.notifyAll();
        }
    }

    //======================================================================
    // Inner Classes

    private static final class PaintJob
    {

        //======================================================================
        // Non-Private Fields

        final BoundingRectangle area;
        final BufferedImage buffer;
        final TextureListener textureListener;

        //======================================================================
        // Public Methods

        //----------------------------------------------------------------------
        // Constructors

        public PaintJob( final BoundingRectangle area,
                         final BufferedImage buffer,
                         final TextureListener textureListener )
        {
            this.area = area;
            this.buffer = buffer;
            this.textureListener = textureListener;
        }

        //----------------------------------------------------------------------
        // Caononical Methods

        public boolean equals( final Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            final PaintJob paintJob = (PaintJob) o;

            if ( area != null ? !area.equals( paintJob.area ) : paintJob.area != null )
            {
                return false;
            }
            if ( buffer != null ? !buffer.equals( paintJob.buffer ) : paintJob.buffer != null )
            {
                return false;
            }
            if ( textureListener != null ? !textureListener.equals( paintJob.textureListener ) : paintJob.textureListener != null )
            {
                return false;
            }

            return true;
        }


        public int hashCode()
        {
            int result;
            result = ( area != null ? area.hashCode() : 0 );
            result = 31 * result + ( buffer != null ? buffer.hashCode() : 0 );
            result = 31 * result + ( textureListener != null ? textureListener.hashCode() : 0 );
            return result;
        }


        public String toString()
        {
            return "PaintJob{" +
                   "area=" + area +
                   ", buffer=" + buffer +
                   ", textureListener=" + textureListener +
                   '}';
        }

    }

    private class MyTextureRenderer
            implements Runnable
    {

        //======================================================================
        // Public Methods

        //----------------------------------------------------------------------
        // Runnable Implementation

        public void run()
        {
            while ( true )
            {
                final PaintJob paintJob = getNextJob();

                renderJob( paintJob );

                notifyListener( paintJob );
            }
        }

        //======================================================================
        // Private Methods

        private void notifyListener( final PaintJob paintJob )
        {
            // Notify listener from swing thread
            // TODO: Maybe we could just call the listener directly from this thread?
            SwingUtilities.invokeLater( new Runnable()
            {

                public void run()
                {
                    paintJob.textureListener.onTextureReady( paintJob.area, paintJob.buffer );
                }

            } );
        }


        private PaintJob getNextJob()
        {
            PaintJob paintJob = null;

            while ( paintJob == null )
            {
                synchronized ( myPaintJobs )
                {
                    while ( myPaintJobs.isEmpty() )
                    {
                        try
                        {
                            myPaintJobs.wait();
                        }
                        catch ( InterruptedException e )
                        {
                            // Ignore
                        }
                    }

                    if ( !myPaintJobs.isEmpty() )
                    {
                        paintJob = myPaintJobs.get( 0 );
                        myPaintJobs.remove( paintJob );
                    }
                }
            }

            return paintJob;
        }


        private void renderJob( final PaintJob paintJob )
        {
            System.out.println( "MapTextureProvider$MyTextureRenderer.renderJob" );
            System.out.println( "paintJob = " + paintJob );

            final int width = paintJob.buffer.getWidth();
            final int height = paintJob.buffer.getHeight();

            // (or in one of a number of existing rendering threads, to avoid creating too many threads, but still taking advantage of multi-core capabilities).
            // CHECK: Does the MapContext support multi-threaded access?

            final Graphics2D graphics = (Graphics2D) paintJob.buffer.getGraphics();
            graphics.setColor( myBackgroundColor );

            graphics.fillRect( 0, 0, width, height );

            // Create the source and destination areas
            final Rectangle targetArea = new Rectangle( width, height );
            final BoundingRectangle area = paintJob.area;
            final ReferencedEnvelope sourceArea = new ReferencedEnvelope( area.getX1(),
                                                                          area.getX2(),
                                                                          area.getY1(),
                                                                          area.getY2(),
                                                                          myMap.getCoordinateReferenceSystem() );

            // Render
            myStreamingRenderer.paint( graphics, targetArea, sourceArea );

/*
            for ( int i = 0; i < 100; i++ )
            {
                System.out.println( "working" );
            }
*/

/*
            // DEBUG
            try
            {
                Thread.sleep(4000 );
            }
            catch ( InterruptedException e )
            {
            }
*/
        }

    }

}
