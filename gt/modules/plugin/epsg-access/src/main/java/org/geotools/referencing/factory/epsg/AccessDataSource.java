/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.AbstractAuthorityFactory;


/**
 * Connection to the EPSG database in MS-Access format using JDBC-ODBC bridge. The EPSG
 * database can be downloaded from <A HREF="http://www.epsg.org">http://www.epsg.org</A>.
 * The JDBC-ODBC bridge is a documented feature of Sun's J2SE distribution. See
 * <A HREF="http://java.sun.com/j2se/1.5/docs/guide/jdbc/bridge.html">New data source
 * implementations in the JDBC-ODBC bridge</A>.
 * <P>
 * Just having this class accessible in the classpath, together with the registration in
 * the {@code META-INF/services/} directory, is suffisient to get a working EPSG authority
 * factory backed by this database. Vendors can create a copy of this class, modify it and
 * bundle it with their own distribution if they want to connect their users to an other
 * database (for example a PostgreSQL database reachable on internet).
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link FactoryOnAccess}.
 */
public class AccessDataSource extends sun.jdbc.odbc.ee.DataSource implements DataSource {
    /**
     * Creates a new instance of this data source
     */
    public AccessDataSource() {
        setDatabaseName("EPSG");
    }

    /**
     * Returns the priority for this data source. The default implementation returns
     * <code>{@linkplain #NORMAL_PRIORITY NORMAL_PRIORITY} - 10</code>.
     */
    public int getPriority() {
        return NORMAL_PRIORITY;
    }

    /**
     * Open a connection and creates an {@linkplain DirectEpsgFactory EPSG factory} for it.
     *
     * @param  hints A map of hints, including the low-level factories to use for CRS creation.
     * @return The EPSG factory using MS-Access SQL syntax.
     * @throws SQLException if connection to the database failed.
     */
    public AbstractAuthorityFactory createFactory(final Hints hints) throws SQLException {
        final Connection connection;
        try {
            connection = getConnection();
        } catch (NullPointerException exception) {
            /*
             * This try...catch block should NOT be needed. We added it as a workaround because
             * the JDBC-ODBC bridge on Linux throws a NullPointerException when trying to log a
             * warning to the tracer.
             */
            SQLException e = new SQLException("Unexpected exception in JDBC data source.");
            e.initCause(exception);
            throw e;
        }
        return new DirectEpsgFactory(hints, connection);
    }
}
