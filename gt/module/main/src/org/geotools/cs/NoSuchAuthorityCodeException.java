/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Thrown when a {@link CoordinateSystemAuthorityFactory}
 * can't find a requested authority code.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see CoordinateSystemAuthorityFactory#createCoordinateSystem
 *
 * @deprecated Replaced by {@link org.opengis.referencing.NoSuchAuthorityCodeException} in the
 *             <code>org.opengis.referencing</code> package.
 */
public class NoSuchAuthorityCodeException extends FactoryException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1493339637440326131L;

    /**
     * Constructs an exception with no message.
     */
    public NoSuchAuthorityCodeException() {
        super();
    }

    /**
     * Constructs an exception with the specified detail message.
     */
    public NoSuchAuthorityCodeException(final String message) {
        super(message);
    }

    /**
     * Constructs an exception with a default detail message.
     *
     * @param classname The short class name of the class to be constructed. E.g. "Ellipsoid".
     * @param code The specified code.
     */
    NoSuchAuthorityCodeException(final String classname, final String code) {
        super(Resources.format(ResourceKeys.ERROR_NO_SUCH_AUTHORITY_CODE_$2, classname, code));
    }
}
