/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;

import org.geotools.resources.Utilities;


/**
 * A simple international string consisting of a single string for all locales.
 * For such a particular case, this implementation is the more effective than
 * other implementations provided in this package.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SimpleInternationalString extends InternationalString implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3543963804501667578L;

    /**
     * Creates a new instance of international string.
     *
     * @param message The string for all locales.
     */
    public SimpleInternationalString(final String message) {
        defaultValue = message;
        ensureNonNull("message", message);
    }

    /**
     * Returns the same string for all locales. This is the string given to the constructor.
     */
    public String toString(final Locale locale) {
        return defaultValue;
    }

    /**
     * Compares this international string with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final SimpleInternationalString that = (SimpleInternationalString) object;
            return Utilities.equals(this.defaultValue, that.defaultValue);
        }
        return false;
    }

    /**
     * Returns a hash code value for this international text.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ defaultValue.hashCode();
    }

    /**
     * Write the string. This is required since {@link #defaultValue} is not serialized.
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(defaultValue);
    }

    /**
     * Read the string. This is required since {@link #defaultValue} is not serialized.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        defaultValue = in.readUTF();
    }
}
