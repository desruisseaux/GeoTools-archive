/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
package org.geotools.renderer.array;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Classe enveloppant une portion seulement d'un tableau {@link CompressedArray}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class SubCompressedArray extends CompressedArray {
    /**
     * Num�ro de s�rie (pour compatibilit� avec des versions ant�rieures).
     */
    private static final long serialVersionUID = 4702506646824251468L;

    /**
     * Plage des donn�es valides du tableau {@link #array}.
     */
    protected final int lower, upper;

    /**
     * Construit un sous-tableau � partir d'un autre tableau compress�.
     *
     * @param  other Tableau source.
     * @param  lower Index de la premi�re coordonn�es <var>x</var> �
     *         prendre en compte dans le tableau <code>other</code>.
     * @param  upper Index suivant celui de la derni�re coordonn�e <var>y</var> �
     *         prendre en compte dans le tableau <code>other</code>. La diff�rence
     *         <code>upper-lower</code> doit obligatoirement �tre paire.
     */
    SubCompressedArray(final CompressedArray other, final int lower, final int upper) {
        super(other, lower);
        this.lower  = lower;
        this.upper  = upper;
        if (upper-lower < 2) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RANGE_$2,
                                               new Integer(lower), new Integer(upper)));
        }
        if (((upper-lower)&1) !=0) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ODD_ARRAY_LENGTH_$1,
                                               new Integer(upper-lower)));
        }
        if (lower < 0) {
            throw new ArrayIndexOutOfBoundsException(lower);
        }
        if (upper > other.array.length) {
            throw new ArrayIndexOutOfBoundsException(upper);
        }
    }

    /**
     * Retourne l'index de la premi�re coordonn�e valide.
     */
    protected final int lower() {
        return lower;
    }

    /**
     * Retourne l'index suivant celui de la derni�re coordonn�e valide.
     */
    protected final int upper() {
        return upper;
    }

    /**
     * Returns an estimation of memory usage in bytes. This method returns the same value
     * than {@link CompressedArray#getMemoryUsage} plus 8 bytes for the internal fields
     * (the {@link #lower} and {@link #upper} fields).
     */
    public long getMemoryUsage() {
        return super.getMemoryUsage() + 8;
    }
}
