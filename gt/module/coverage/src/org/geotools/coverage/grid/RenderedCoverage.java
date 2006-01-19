/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.coverage.grid;

// Dependencies
import java.awt.image.RenderedImage;
import javax.media.jai.PropertySource;
import org.opengis.coverage.grid.GridCoverage;


/**
 * A grid coverage backed by a {@linkplain RenderedImage rendered image}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public interface RenderedCoverage extends GridCoverage, PropertySource {
    /**
     * Returns grid data as a rendered image.
     */
    RenderedImage getRenderedImage();
}
