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

// Geotools dependencies
import org.geotools.factory.Hints;


/**
 * An EPSG factory suitable for Oracle SQL syntax.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author John Grange
 *
 * @todo Since this class is constructed through the service provider API rather than directly
 *       instantiated by the user, we need some way to pass the schema information to this class.
 *       one possible approach is to set the schema in preferences. Maybe a better was is to look
 *       for a place in the Oracle {@link javax.sql.DataSource} for that.
 *
 * @deprecated Please use {@link OracleDialectEpsgFactory}.
 */
public class FactoryUsingOracleSQL extends OracleDialectEpsgFactory {
    /**
     * Constructs an authority factory using the specified connection.
     *
     * @param userHints  The underlying factories used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     *
     * @since 2.2
     */
    public FactoryUsingOracleSQL(final Hints      userHints,
                                 final Connection connection)
    {
        super(userHints, connection);
    }
}
