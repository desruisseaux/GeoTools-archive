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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opengis.metadata.Identifier;
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
     * Constructs a generic name from an array of local names and the default separator character.
     * If any of the specified names is an {@link InternationalString}, then the
     * <code>{@linkplain InternationalString#toString(Locale) toString}(null)</code>
     * method will be used in order to fetch an unlocalized name. Otherwise, the
     * <code>{@linkplain CharSequence#toString toString}()</code> method will be used.
     *
     * @param names The local names as an array of strings or international strings.
     *              This array must contains at least one element.
     */
    public static GenericName create(final CharSequence[] names) {
        return create(names, org.geotools.util.GenericName.DEFAULT_SEPARATOR);
    }

    /**
     * Constructs a generic name from an array of local names and the specified separator character.
     * If any of the specified names is an {@link InternationalString}, then the
     * <code>{@linkplain InternationalString#toString(Locale) toString}(null)</code>
     * method will be used in order to fetch an unlocalized name. Otherwise, the
     * <code>{@linkplain CharSequence#toString toString}()</code> method will be used.
     *
     * @param names     The local names as an array of strings.
     *                  This array must contains at least one element.
     * @param separator The separator character to use.
     */
    public static GenericName create(final CharSequence[] names, final char separator) {
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
    private static GenericName create(final CharSequence[] names,
                                      final int  length,
                                      final char separator)
    {
        if (length <= 0) {
            throw new IllegalArgumentException(String.valueOf(length));
        }
        if (length == 1) {
            return new LocalName(names[0]);
        }
        return new ScopedName(create(names, length-1, separator), separator, names[length-1]);
    }

    /**
     * Returns the specified name in an array. The {@code value} may be either a {@link String},
     * {@code String[]}, {@link GenericName} or {@code GenericName[]}. This method is used in
     * {@link org.geotools.referencing.DefaultIdentifiedObject} constructors.
     *
     * @param  value The object to cast into an array of generic names.
     * @return The generic names.
     * @throws ClassCastException if {@code value} can't be cast.
     */
    public static GenericName[] toArray(final Object value) throws ClassCastException {
        if (value instanceof CharSequence) {
            return new GenericName[] {create(value.toString())};
        } else if (value instanceof CharSequence[]) {
            final CharSequence[] values = (CharSequence[]) value;
            final GenericName[] names = new GenericName[values.length];
            for (int i=0; i<values.length; i++) {
                names[i] = create( values[i].toString() );
            }
            return names;
        } else if (value instanceof GenericName) {
            return new GenericName[] {(GenericName) value};
        }
        else if( value instanceof Identifier[]){
            final Identifier[] values = (Identifier[]) value;
            final GenericName[] names = new GenericName[ values.length ];
            for( int i=0; i<values.length; i++){
                names[i] = create( values[i].getCode() );
            }
            return names;
        }
        else if( value instanceof GenericName[]){
            return (GenericName[]) value;
        }
        else {
            throw new ClassCastException("Cannot convert "+value.toString()+ " to GenericName[]" );
        }
    }
}
