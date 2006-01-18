/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.geometry.array;

// J2SE dependencies
import java.io.Serializable;
import java.util.AbstractList;
import java.util.RandomAccess;

// Geotools dependencies
import org.geotools.geometry.DirectPosition2D;


/**
 * Exposes a {@link PointArray2D} as a list of positions with efficient random access.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see PointArray2D#positions
 */
final class ListAdapter extends AbstractList implements RandomAccess, Serializable {
    /**
     * Serial version for compatibility with previous version.
     */
    private static final long serialVersionUID = -8691631393465315424L;

    /**
     * The array of (<var>x</var>,<var>y</var>) coordinates.
     *
     * @param array The array of (<var>x</var>,<var>y</var>) coordinates
     */
    private final PointArray2D array;

    /**
     * Wraps the specified array in a list.
     */
    public ListAdapter(final PointArray2D array) {
        this.array = array;
    }

    /**
     * Returns this list size.
     */
    public int size() {
        return array.length();
    }

    /**
     * Returns the position at the specified index.
     */
    public Object get(final int index) {
        return array.get(index, null);
    }
}
