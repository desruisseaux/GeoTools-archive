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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing;

// OpenGIS dependencies
import org.opengis.referencing.NoSuchAuthorityCodeException;


/**
 * Base class for all authority factories. An <cite>authority</cite> is an
 * organization that maintains definitions of authority codes. An <cite>authority
 * code</cite> is a compact string defined by an authority to reference a particular
 * spatial reference object. For example the
 * <A HREF="http://www.epsg.org">European Petroleum Survey Group (EPSG)</A> maintains
 * a database of coordinate systems, and other spatial referencing objects, where each
 * object has a code number ID. For example, the EPSG code for a WGS84 Lat/Lon coordinate
 * system is '4326'.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AuthorityFactory extends Factory
        implements org.opengis.referencing.AuthorityFactory
{
    /**
     * Construct a default instance.
     */
    protected AuthorityFactory() {
    }

    /**
     * Creates an exception for an unknow authority code. This convenience method is provided
     * for implementation of <code>createXXX</code> methods.
     *
     * @param  type The GeoAPI interface that was to be created
     *              (e.g. <code>CoordinateReferenceSystem.class</code>).
     * @param  code The unknow authority code.
     * @return An error message initialized with an error message built
     *         from the specified informations.
     *
     * @todo Localize the error message.
     */
    protected NoSuchAuthorityCodeException noSuchAuthorityCode(final Class  type,
                                                               final String code)
    {
        final String authority = getAuthority().getTitle().toString();
        return new NoSuchAuthorityCodeException(
                "No code \""+code+"\" for authority \""+authority+"\".", authority, code);
    }
}
