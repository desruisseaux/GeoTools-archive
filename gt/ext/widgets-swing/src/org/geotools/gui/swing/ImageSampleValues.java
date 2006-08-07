/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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

import java.awt.image.RenderedImage;


/**
 * Display the image sample values as a table.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved to the {@link org.geotools.gui.swing.image} package.
 */
public class ImageSampleValues extends org.geotools.gui.swing.image.ImageSampleValues {
    /**
     * Creates a new table without initial image to display.
     */
    public ImageSampleValues() {
        super();
    }
    
    /**
     * Creates a new table that will display the sample values for the specified image.
     */
    public ImageSampleValues(final RenderedImage image) {
        super(image);
    }
}
