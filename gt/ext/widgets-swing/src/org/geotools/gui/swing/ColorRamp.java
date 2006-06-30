/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2000, Institut de Recherche pour le D�veloppement
 * (C) 1999, P�ches et Oc�ans Canada
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.gui.swing;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;


/**
 * A color ramp with a graduation.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved to the {@link org.geotools.gui.swing.image} package.
 */
public class ColorRamp extends org.geotools.gui.swing.image.ColorRamp {
    /**
     * Constructs an initially empty color bar. Colors can be
     * set using one of the {@code setColors(...)} methods.
     */
    public ColorRamp() {
        super();
    }

    /**
     * Constructs a color bar for the specified coverage.
     */
    public ColorRamp(final Coverage coverage) {
        super(coverage);
    }
}
