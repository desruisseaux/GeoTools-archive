/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.image;

// J2SE dependencies
import java.util.Locale;
import java.io.File;
import java.nio.charset.Charset;
import java.awt.image.RenderedImage;
import javax.swing.JFrame;

// Geotools dependencies
import org.geotools.image.io.PaletteFactory;


/**
 * A set of utilities related to images.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Utilities {
    /**
     * The palette factory.
     */
    private static PaletteFactory factory;

    /**
     * Do not allows instantiation of this class.
     */
    private Utilities() {
    }

    /**
     * Gets the default palette factory.
     */
    public static synchronized PaletteFactory getPaletteFactory() {
        if (factory == null) {
            factory = new PaletteFactory(
            /* parent factory */ null,
            /* class loader   */ Utilities.class,
            /* root directory */ new File("colors"),
            /* extension      */ ".pal",
            /* character set  */ Charset.forName("ISO-8859-1"),
            /* locale         */ Locale.US);
        }
        return factory;
    }

    /**
     * Display the specified image. This method is used mostly for debugging purpose.
     */
    @SuppressWarnings("deprecation")
    public static void show(final RenderedImage image, final String title) {
        final JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new javax.media.jai.widget.ScrollingImagePanel(image, 400, 400));
        frame.pack();
        frame.show();
    }
}
