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
package org.geotools.referencing.wkt;

// J2SE dependencies
import java.lang.reflect.Method;

// Geotools dependencies
import org.geotools.util.UnsupportedImplementationException;


/**
 * Adapter for implementations which doesn't extends {@link Formattable}. This includes
 * especially {@link org.geotools.referencing.operation.transform.AffineTransform2D}.
 * This method looks for an appropriate method using reflection.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Adapter extends Formattable {
    /**
     * The argument types for the <code>formatWKT</code> method to search for.
     */
    private static final Class[] ARGUMENTS = {
        Formatter.class
    };

    /**
     * The object to format as WKT.
     */
    private final Object object;

    /**
     * Create an adapter for the specified object.
     */
    public Adapter(final Object object) {
        this.object = object;
    }

    /**
     * Try to format the wrapped object as WKT. If the adapter fails to find a way to format
     * the object as WKT, then an {@link UnsupportedImplementationException} is thrown.
     */
    protected String formatWKT(final Formatter formatter) {
        final Class classe = object.getClass();
        try {
            final Method method = classe.getMethod("formatWKT", ARGUMENTS);
            try {
                // Gets access even if package-private class.
                method.setAccessible(true);
            } catch (SecurityException exception) {
                // Not allowed to make the method accessible.
                // Continue; maybe the class was already public.
            }
            return (String) method.invoke(object, new Object[] {formatter});
        } catch (Exception cause) {
            final UnsupportedImplementationException exception;
            exception = new UnsupportedImplementationException(classe);
            exception.initCause(cause);
            throw exception;
        }
    }
}
