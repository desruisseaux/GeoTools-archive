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
package org.geotools.gui.swing.table;

import java.awt.image.RenderedImage;

/**
 * A table model for image sample values (or pixels).
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved to the {@link org.geotools.gui.swing.image} package.
 */
public class ImageTableModel extends org.geotools.gui.swing.image.ImageTableModel {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -408603520054548181L;

    /**
     * Creates a new table model.
     */
    public ImageTableModel() {
        super();
    }

    /**
     * Creates a new table model for the specified image.
     */
    public ImageTableModel(final RenderedImage image) {
        super(image);
    }
}
