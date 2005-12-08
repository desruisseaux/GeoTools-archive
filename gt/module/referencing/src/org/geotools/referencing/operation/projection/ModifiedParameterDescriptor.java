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
package org.geotools.referencing.operation.projection;

// J2SE dependencies
import java.util.Iterator;
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;

// Geotools dependencies
import org.geotools.parameter.DefaultParameterDescriptor;


/**
 * A parameter descriptor identical to the supplied one except for the default value.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ModifiedParameterDescriptor extends DefaultParameterDescriptor {
    /**
     * For compatibility with different versions during deserialization.
     */
    private static final long serialVersionUID = -616587615222354457L;

    /**
     * The original parameter descriptor. Used for comparaisons purpose only.
     */
    private final ParameterDescriptor original;

    /**
     * The new default value.
     */
    private final Double defaultValue;

    /**
     * Creates a parameter descriptor wrapping the specified one with the specified
     * default value.
     */
    public ModifiedParameterDescriptor(final ParameterDescriptor original,
                                       final double defaultValue)
    {
        super(original);
        this.original     = original;
        this.defaultValue = new Double(defaultValue);
    }

    /**
     * Returns the default value for the parameter.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns {@code true} if the specified collection contains the specified descriptor. Invoking
     * this method is similar to invoking {@code set.contains(descriptor)}, except that instance of
     * {@link ModifiedParameterDescriptor} are unwrapped to their original descriptor. The drawback
     * is that this method is slower than {@code set.contains(descriptor)}, so it should be invoked
     * only if the former fails.
     */
    public static boolean contains(final Collection set, ParameterDescriptor descriptor) {
        if (descriptor instanceof ModifiedParameterDescriptor) {
            descriptor = ((ModifiedParameterDescriptor) descriptor).original;
        }
        for (final Iterator it=set.iterator(); it.hasNext();) {
            ParameterDescriptor candidate = (ParameterDescriptor) it.next();
            if (candidate instanceof ModifiedParameterDescriptor) {
                candidate = ((ModifiedParameterDescriptor) candidate).original;
            }
            if (descriptor.equals(candidate)) {
                return true;
            }
        }
        assert !set.contains(descriptor);
        return false;
    }
}
