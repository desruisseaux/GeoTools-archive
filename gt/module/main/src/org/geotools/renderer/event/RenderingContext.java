/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.renderer.event;


/**
 * Informations relatives to a rendering in progress.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface RenderingContext {
    /**
     * Returns the scale factor, or {@link Float#NaN} if the scale is unknow.
     * The scale factor is usually smaller than 1. For example for a 1:1000 scale,
     * the scale factor will be 0.001. This scale factor takes in account the physical
     * size of the rendering device (e.g. the screen size) if such information is available.
     * Note that this scale can't be more accurate than the
     * {@linkplain java.awt.GraphicsConfiguration#getNormalizingTransform() information supplied
     * by the underlying system}.
     *
     * @return The rendering scale factor as a number between 0 and 1, or {@link Float#NaN}.
     * @see Renderer#getScale
     */
    public float getScale();

    /**
     * Returns <code>true</code> if the map is printed instead of painted on screen.
     * Highlighting managers may choose to ignore highlight when rendering to a printer.
     */
    public boolean isPrinting();
}
