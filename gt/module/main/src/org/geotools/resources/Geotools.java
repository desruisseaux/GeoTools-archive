/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// J2SE dependencies
import java.util.logging.Level;

// Geotools dependencies
import org.geotools.util.MonolineFormatter;


/**
 * A central place where to provides system-wide services for Geotools. The 
 * {@link #init} method should be invoked once during some Geotools's class 
 * initialization. Current implementation just setup a custom logger for the
 * <code>"org.geotools"</code> package.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Invoke {@link MonolineFormatter#initGeotools} instead.
 */
public final class Geotools {
    /**
     * Do not allow instanciation of this class.
     */
    private Geotools() {
    }

    /**
     * Performs a system-wide initialization for Geotools.
     *
     * @deprecated Invoke {@link MonolineFormatter#initGeotools()} instead.
     */
    public static void init() {
        init(null);
    }

    /**
     * Performs a system-wide initialization for Geotools.
     * Note: Avoid this method as much as possible,  since it overrides user's level setting for
     *       the <code>base</code> logger. A user trying to configure its logging properties may
     *       find confusing to see his setting ignored.
     *
     * @param level The logging level, or <code>null</code> if no level should be set.
     *
     * @deprecated Invoke {@link MonolineFormatter#initGeotools(Level)} instead.
     */
    public static void init(final Level level) {
        final MonolineFormatter f = MonolineFormatter.init("org.geotools", level);
        // As of new MonolineFormatter.init(...) specification, 'f' should never be null.
        if (f.getSourceFormat() == null) {
            // Set the source format only if the user didn't specified
            // an explicit one in the jre/lib/logging.properties file.
            f.setSourceFormat("class:long");
        }
    }
}
