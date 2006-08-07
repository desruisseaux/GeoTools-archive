/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Développement
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
 * A widget for selecting and/or editing a {@link javax.media.jai.KernelJAI} object.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved to the {@link org.geotools.gui.swing.image} package.
 */
public class KernelEditor extends org.geotools.gui.swing.image.KernelEditor {
    /**
     * Constructs a new kernel editor. No kernel will be initially shown. The method
     * {@link #setKernel} must be invoked, or the user must performs a selection in
     * a combo box, in order to make a kernel visible.
     */
    public KernelEditor() {
        super();
    }
}
