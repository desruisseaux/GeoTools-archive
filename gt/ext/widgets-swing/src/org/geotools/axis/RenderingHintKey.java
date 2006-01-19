/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2000, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
package org.geotools.axis;

// Rendering hints
import java.awt.RenderingHints;


/**
 * Rendering hints for tick's graduation.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class RenderingHintKey extends RenderingHints.Key {
    /**
     * The required base class.
     */
    private final Class type;

    /**
     * Construct a rendering hint key.
     */
    protected RenderingHintKey(final Class type, final int key) {
        super(key);
        this.type = type;
    }

    /**
     * Returns {@code true} if the specified object is a valid value for this key.
     */
    public boolean isCompatibleValue(final Object value) {
        return value!=null && type.isAssignableFrom(value.getClass());
    }
}
