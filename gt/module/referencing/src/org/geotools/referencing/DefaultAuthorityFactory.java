/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing;

// J2SE dependencies
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashSet;

// OpenGIS dependencies
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.resources.Utilities;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.factory.AllAuthoritiesFactory;
import org.geotools.referencing.factory.BufferedAuthorityFactory;


/**
 * The default authority factory to be used by {@link CRS#decode}.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class DefaultAuthorityFactory extends BufferedAuthorityFactory implements CRSAuthorityFactory {
    /**
     * Creates a new authority factory with a {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint
     * if {@code longitudeFirst} is {@code true}.
     */
    DefaultAuthorityFactory(final boolean longitudeFirst) {
        super(new AllAuthoritiesFactory(longitudeFirst ?
                new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE) : null,
                FactoryFinder.getCRSAuthorityFactories()));
    }

    /**
     * Implementation of {@link CRS#getSupportedCodes}. Provided here in order to reduce the
     * amount of class loading when using {@link CRS} for other purpose than CRS decoding.
     */
    static Set/*<String>*/ getSupportedCodes(final String authority) {
    	Set result = Collections.EMPTY_SET;
        boolean isSetCopied = false;
    	for (final Iterator i=FactoryFinder.getCRSAuthorityFactories().iterator(); i.hasNext();) {
            final CRSAuthorityFactory factory = (CRSAuthorityFactory) i.next();
            if (Citations.identifierMatches(factory.getAuthority(), authority)) {
                final Set codes;
                try {
                    codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
                } catch (Exception exception) {
                    /*
                     * Failed to fetch the codes either because of a database connection problem
                     * (FactoryException), or because we are using a simple factory that doesn't
                     * support this operation (UnsupportedOperationException), or any unexpected
                     * reason. No codes from this factory will be added to the set.
                     */
                    Utilities.unexpectedException("org.geotools.referencing", "CRS",
                                                  "getSupportedCodes", exception);
                    continue;
                }
                if (codes!=null && !codes.isEmpty()) {
                    if (result.isEmpty()) {
                        result = codes;
                    } else {
                        if (!isSetCopied) {
                            result = new LinkedHashSet(result);
                            isSetCopied = true;
                        }
                        result.addAll(codes);
                    }
                }
			}
    	}
        return result;
    }
}
