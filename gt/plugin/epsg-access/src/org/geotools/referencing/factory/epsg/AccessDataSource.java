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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.AbstractAuthorityFactory;


/**
 * Connection to the EPSG database in MS-Access format using JDBC-ODBC bridge. The EPSG
 * database can be downloaded from <A HREF="http://www.epsg.org">http://www.epsg.org</A>.
 * The JDBC-ODBC bridge is a documented feature of Sun's J2SE distribution. See
 * <A HREF="http://java.sun.com/j2se/1.5/docs/guide/jdbc/bridge.html">New data source
 * implementations in the JDBC-ODBC bridge</A>.
 *
 * <P>Just having this class accessible in the classpath, together with the registration in
 * the {@code META-INF/services/} directory, is suffisient to get a working EPSG authority
 * factory backed by this database. Vendors can create a copy of this class, modify it and
 * bundle it with their own distribution if they want to connect their users to an other
 * database (for example a PostgreSQL database reachable on internet).</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AccessDataSource extends sun.jdbc.odbc.ee.DataSource implements DataSource {
    /**
     * Creates a new instance of this data source
     */
    public AccessDataSource() {
        setDatabaseName("EPSG");
    }

    /**
     * Returns the priority for this data source, which is
     * {@link #NORMAL_PRIORITY NORMAL_PRIORITY}.
     */
    public int getPriority() {
        return NORMAL_PRIORITY;
    }

    /**
     * Open a connection and creates an {@linkplain EPSGFactory EPSG factory} for it.
     *
     * @param  factories The low-level factories to use for CRS creation.
     * @return The EPSG factory using MS-Access SQL syntax.
     * @throws SQLException if connection to the database failed.
     */
    public AbstractAuthorityFactory createFactory(final FactoryGroup factories) throws SQLException {
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
        return new EPSGFactory(factories, connection);
    }
}
