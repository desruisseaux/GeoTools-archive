/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2003, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gui.swing;


/**
 * A simple image viewer.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved to the {@link org.geotools.gui.swing.image} package.
 */
public class ImagePane extends org.geotools.gui.swing.image.ImagePane {
    /**
     * Constructs an initially empty image pane with a default rendered image size.
     */
    public ImagePane() {
        super();
    }

    /**
     * Constructs an initially empty image pane with the specified rendered image size.
     * The {@code renderedSize} argument is the <em>maximum</em> width and height for
     * {@linkplain RenderedImage rendered image}. Images greater than this value will be
     * scaled down for faster rendering.
     */
    public ImagePane(final int renderedSize) {
        super(renderedSize);
    }
}
