package org.geotools.renderer3d.field;

import org.geotools.renderer3d.utils.BoundingRectangle;
import org.geotools.renderer3d.utils.ParameterChecker;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * The default texture provider, will render the textures in a separate thread and call the texture listener on the
 * swing thread when a texture has been rendered.
 * <p/>
 * TODO: A way to cancel a rendering, by passing a reference to the TextureListener.
 *
 * @author Hans Häggström
 */
public final class TextureProviderImpl
        implements TextureProvider
{

    //======================================================================
    // Private Fields

    private final TextureRenderer myTextureRenderer;
    private final List<TextureJob> myPaintJobs = new LinkedList<TextureJob>();

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param textureRenderer the renderer to use for rendering the textures.
     */
    public TextureProviderImpl( final TextureRenderer textureRenderer )
    {
        ParameterChecker.checkNotNull( textureRenderer, "textureRenderer" );

        myTextureRenderer = textureRenderer;

        // Start a thread that handles texture painting jobs
        final Thread renderThread = new Thread( new Runnable()
        {

            public void run()
            {
                while ( true )
                {
                    final TextureJob paintJob = getNextJob();

                    myTextureRenderer.renderArea( paintJob.getArea(), paintJob.getBuffer() );

                    notifyListener( paintJob );
                }
            }

        } );
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
            myPaintJobs.add( new TextureJob( area, buffer, textureListener ) );
            myPaintJobs.notifyAll();
        }
    }

    //======================================================================
    // Private Methods

    private void notifyListener( final TextureJob paintJob )
    {
        // Notify listener from swing thread
        // TODO: Maybe we could just call the listener directly from this thread?
        SwingUtilities.invokeLater( new Runnable()
        {

            public void run()
            {
                paintJob.getTextureListener().onTextureReady( paintJob.getArea(), paintJob.getBuffer() );
            }

        } );
    }


    /**
     * @return Returns the next paint job.  Blocks until one is available.
     */
    private TextureJob getNextJob()
    {
        TextureJob paintJob = null;

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

}
