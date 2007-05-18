/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Institut de Recherche pour le Développement
 *    (C) 2006, Geomatys
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.image.io.stream;

// J2SE dependencies
import java.util.Locale;
import java.util.logging.Logger;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.SocketException;
import java.net.InetSocketAddress;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.FileCacheImageInputStream;


/**
 * A service provider for {@link ImageInputStream} from {@link URL} connection.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class UrlInputSpi extends ImageInputStreamSpi {
    /**
     * Maximum number of retries when a connection failed.
     */
    private static final int RETRY = 10;

    /**
     * The proxy.
     *
     * @todo Ce champ devrait pouvoir être spécifié par l'utilisateur.
     */
    private final Proxy proxy = Proxy.NO_PROXY;

    /**
     * Creates a new instance of service provider interface.
     */
    public UrlInputSpi() {
        super("Institut de Recherche pour le Développement",
              "1.0", URL.class);
    }

    /**
     * Returns a brief, human-readable description of this service
     * provider and its associated implementation.
     */
    public String getDescription(final Locale locale) {
        return "Flot d'entré à partir d'un URL, éventuellement via un proxy.";
    }

    /**
     * Returns {@code true} since the input stream requires the use of a cache file.
     */
    @Override
    public boolean needsCacheFile() {
        return true;
    }

    /**
     * Constructs an input stream for an URL.
     */
    public ImageInputStream createInputStreamInstance(final Object  input,
                                                      final boolean useCache,
                                                      final File    cacheDir)
            throws IOException
    {
        final URL url = (URL) input;
        final URLConnection connection = url.openConnection(proxy);
        int retry = RETRY;
        InputStream stream;
        while (true) {
            try {
                stream = connection.getInputStream();
                break;
            } catch (SocketException exception) {
                Logger.getLogger("net.sicade.image.io").warning(exception.getLocalizedMessage());
                if (--retry < 0) {
                    throw exception;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
                // Quelqu'un ne veut pas nous laisser dormir. Retourne au boulot...
            }
            if (true) {
                // L'expérience suggère que les appels à System.gc|runFinalization peuvent aider.
                System.gc();
                final FinalizationStopper stopper = new FinalizationStopper();
                System.runFinalization();
                if (!stopper.cancel()) {
                    Logger.getLogger("net.sicade.image.io").warning("Bloquage de System.runFinalization()");
                }
            }
        }
        return new FileCacheImageInputStream(stream, cacheDir);
    }
}
