/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing;

// J2SE dependencies
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashSet;

// OpenGIS dependencies
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.factory.ManyAuthoritiesFactory;
import org.geotools.referencing.factory.ThreadedAuthorityFactory;


/**
 * The default authority factory to be used by {@link CRS#decode}.
 * <p>
 * This class gathers together a lot of logic in order to capture the following ideas:
 * <ul>
 *   <li>Uses {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER} to swap ordinate order if needed.</li>
 *   <li>Uses {@link ManyAuthoritiesFactory} to access CRSAuthorities in the environment.</li>
 * </ul>
 * 
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Andrea Aime
 */
final class DefaultAuthorityFactory extends ThreadedAuthorityFactory implements CRSAuthorityFactory {
    /**
     * Creates a new authority factory with the specified hints.
     */
    DefaultAuthorityFactory(final Hints hints) {
        super(new ManyAuthoritiesFactory(hints, ReferencingFactoryFinder.getCRSAuthorityFactories(hints)));
    }

    /**
     * Implementation of {@link CRS#getSupportedCodes}. Provided here in order to reduce the
     * amount of class loading when using {@link CRS} for other purpose than CRS decoding.
     */
    static Set/*<String>*/ getSupportedCodes(final String authority) {
        Set result = Collections.EMPTY_SET;
        boolean isSetCopied = false;
        for (final Iterator i=ReferencingFactoryFinder.getCRSAuthorityFactories(null).iterator(); i.hasNext();) {
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
                    CRS.unexpectedException("getSupportedCodes", exception);
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

    /**
     * Implementation of {@link CRS#getSupportedAuthorities}. Provided here in order to reduce the
     * amount of class loading when using {@link CRS} for other purpose than CRS decoding.
     */
    static Set/*<String>*/ getSupportedAuthorities(final boolean returnAliases) {
        final Set authorityFactories = ReferencingFactoryFinder.getCRSAuthorityFactories(null);
        final Set result = new LinkedHashSet();
        for (final Iterator i = authorityFactories.iterator(); i.hasNext();) {
            final CRSAuthorityFactory factory = (CRSAuthorityFactory) i.next();
            final Collection identifiers = factory.getAuthority().getIdentifiers();
            for (final Iterator j = identifiers.iterator(); j.hasNext();) {
                result.add(j.next());
                if (!returnAliases) {
                    break;
                }
            }
        }
        return result;
    }
}
