/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.util;

// J2SE dependencies
import java.util.List;
import java.util.ArrayList;

// OpenGIS dependencies
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;


/**
 * A factory for {@link GenericName} objects.
 *
 * @version $Id$
 * @author  Martin Desruisseaux
 */
public final class NameFactory {
    /**
     * Do not allows instantiation of instance of this class.
     */
    private NameFactory() {
    }

    /**
     * Constructs a generic name from a fully qualified name
     * and the default separator character.
     *
     * @param name The fully qualified name.
     */
    public static GenericName create(final String name) {
        return create(name, org.geotools.util.GenericName.DEFAULT_SEPARATOR);
    }

    /**
     * Constructs a generic name from a fully qualified name
     * and the specified separator character.
     *
     * @param name The fully qualified name.
     * @param separator The separator character.
     */
    public static GenericName create(final String name, final char separator) {
        final List names = new ArrayList();
        int lower=0;
        while (true) {
            final int upper = name.indexOf(separator, lower);
            if (upper >= 0) {
                names.add(name.substring(lower, upper));
                lower = upper+1;
            } else {
                names.add(name.substring(lower));
                break;
            }
        }
        return create((String[]) names.toArray(new String[names.size()]), separator);
    }

    /**
     * Constructs a generic name from an array of local names
     * and the default separator character.
     *
     * @param names The local names as an array of strings.
     *              This array must contains at least one element.
     */
    public static GenericName create(final String[] names) {
        return create(names, org.geotools.util.GenericName.DEFAULT_SEPARATOR);
    }

    /**
     * Constructs a generic name from an array of local names
     * and the default separator character.
     *
     * @param names The local names as an array of international strings.
     *              This array must contains at least one element.
     */
    public static GenericName create(final InternationalString[] names) {
        return create(names, org.geotools.util.GenericName.DEFAULT_SEPARATOR);
    }

    /**
     * Constructs a generic name from an array of local names
     * and the specified separator character.
     *
     * @param names     The local names as an array of strings.
     *                  This array must contains at least one element.
     * @param separator The separator character to use.
     */
    public static GenericName create(final String[] names, final char separator) {
        return create(names, names.length, separator);
    }

    /**
     * Constructs a generic name from an array of local names
     * and the specified separator character.
     *
     * @param names     The local names as an array of international strings.
     *                  This array must contains at least one element.
     * @param separator The separator character to use.
     */
    public static GenericName create(final InternationalString[] names, final char separator) {
        return create(names, names.length, separator);
    }

    /**
     * Constructs a generic name from an array of local names
     * and the specified separator character.
     *
     * @param names     The local names as an array of strings.
     * @param length    The valid length of <code>names</code> array.
     * @param separator The separator character to use.
     */
    private static GenericName create(final String[] names, final int length,
                                      final char separator)
    {
        if (length <= 0) {
            throw new IllegalArgumentException(String.valueOf(length));
        }
        if (length == 1) {
            return new LocalName(null, names[0]);
        }
        return new ScopedName(create(names, length-1, separator), separator, names[length-1]);
    }

    /**
     * Constructs a generic name from an array of local names
     * and the specified separator character.
     *
     * @param names     The local names as an array of international strings.
     * @param length    The valid length of <code>names</code> array.
     * @param separator The separator character to use.
     */
    private static GenericName create(final InternationalString[] names,
                                      final int length, final char separator)
    {
        if (length <= 0) {
            throw new IllegalArgumentException(String.valueOf(length));
        }
        if (length == 1) {
            return new LocalName(null, names[0]);
        }
        return new ScopedName(create(names, length-1, separator), separator, names[length-1]);
    }
}
