package org.geotools.renderer3d.provider.texture.impl;

import org.geotools.renderer3d.provider.texture.TextureRenderer;
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
    private final List<TextureJob> myTextureJobs = new LinkedList<TextureJob>();
    private TextureJob myCurrentJob = null;
    private boolean myCurrentJobWasCanceled = false;

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

                    if ( !myCurrentJobWasCanceled )
                    {
                        notifyListener( paintJob );
                    }
                    myCurrentJobWasCanceled = false;
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
        // DEBUG
        myTextureRenderer.renderArea( area, buffer );
        textureListener.onTextureReady( area, buffer );

/*
        synchronized ( myTextureJobs )
        {
            myTextureJobs.add( new TextureJob( area, buffer, textureListener ) );
            myTextureJobs.notifyAll();
        }
*/
    }

    public void cancelRequest( final TextureListener textureListener )
    {
        synchronized ( myTextureJobs )
        {
            TextureJob textureJobToRemove = null;
            for ( TextureJob textureJob : myTextureJobs )
            {
                if ( textureJob.getTextureListener() == textureListener )
                {
                    textureJobToRemove = textureJob;
                    break;
                }
            }

            if ( textureJobToRemove != null )
            {
                myTextureJobs.remove( textureJobToRemove );
            }
            else if ( myCurrentJob != null && myCurrentJob.getTextureListener() == textureListener )
            {
                myCurrentJobWasCanceled = true;

                // myTextureRenderer.cancelRendering(); // TODO: This could be implemented to speed up things a bit when
                // jobs are canceled, but there's some possibilities that it cancels the next job instead, so left out for now.
            }
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
            synchronized ( myTextureJobs )
            {
                myCurrentJob = null;

                while ( myTextureJobs.isEmpty() )
                {
                    try
                    {
                        myTextureJobs.wait();
                    }
                    catch ( InterruptedException e )
                    {
                        // Ignore
                    }
                }

                if ( !myTextureJobs.isEmpty() )
                {
                    paintJob = myTextureJobs.get( 0 );
                    myTextureJobs.remove( paintJob );
                }

                myCurrentJob = paintJob;
            }
        }

        return paintJob;
    }

}
