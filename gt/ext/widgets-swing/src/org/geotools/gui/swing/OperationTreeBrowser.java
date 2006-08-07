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

import java.awt.image.renderable.RenderableImage;
import java.awt.image.RenderedImage;


/**
 * Display a chain of images as a tree.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Lionel Flahaut 
 *
 * @deprecated Moved to the {@link org.geotools.gui.swing.image} package.
 */
public class OperationTreeBrowser extends org.geotools.gui.swing.image.OperationTreeBrowser {
    /**
     * Constructs a new browser for the given rendered image.
     *
     * @param source The last image from the rendering chain to browse.
     */
    public OperationTreeBrowser(final RenderedImage source) {
        super(source);
    }

    /**
     * Constructs a new browser for the given renderable image.
     *
     * @param source The last image from the rendering chain to browse.
     */
    public OperationTreeBrowser(final RenderableImage source) {
        super(source);
    }
}
