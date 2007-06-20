/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

// Geotools dependencies
import org.geotools.factory.Hints;


/**
 * An EPSG factory for the database generated by SQL scripts rather than the MS-Access one.
 * 
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Rueben Schulz
 * @author Martin Desruisseaux
 * @author Didier Richard
 * @author John Grange
 *
 * @deprecated Please use {@link AnsiDialectEpsgFactory}.
 */
public final class FactoryUsingAnsiSQL extends AnsiDialectEpsgFactory {
    /**
     * Constructs an authority factory using the specified connection.
     *
     * @param userHints  The underlying factories used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     *
     * @since 2.2
     */
    public FactoryUsingAnsiSQL(final Hints      userHints,
                               final Connection connection)
    {
        super(userHints, connection);
    }
}
