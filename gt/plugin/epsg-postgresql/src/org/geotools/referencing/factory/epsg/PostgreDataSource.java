/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;
import java.sql.Connection;
import java.sql.SQLException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.resources.Utilities;
import org.geotools.referencing.factory.AbstractAuthorityFactory;

// postgreSQL dependencies
import org.postgresql.jdbc3.Jdbc3SimpleDataSource;


/**
 * Connection to the EPSG database in PostgreSQL database engine using JDBC. The EPSG
 * database can be downloaded from <A HREF="http://www.epsg.org">http://www.epsg.org</A>.
 * It should have been imported into a PostgreSQL database, which doesn't need to be on
 * the local machine.
 * <p>
 * <h3>Connection parameters</h3>
 * The preferred way to specify connection parameters is through the JNDI interface.
 * However, this datasource provides the following alternative as a convenience: if an
 * {@code EPSG-DataSource.properties} file is found in current directory or in the user's home
 * directory, then the following properties are fetch. Note that the default value may change
 * in a future version if a public server become available.
 * <P>
 * <TABLE BORDER="1">
 * <TR>
 *   <TH>Property</TH>
 *   <TH>Type</TH>
 *   <TH>Description</TH>
 *   <TH>Geotools Default</TH>
 * </TR>
 * <TR>
 *   <TD>serverName</TD>
 *   <TD>String</TD>
 *   <TD>PostgreSQL database server host name</TD>
 *   <TD>localhost</TD>
 * </TR>
 * <TR>
 *   <TD>databaseName</TD>
 *   <TD>String</TD>
 *   <TD>PostgreSQL database name</TD>
 *   <TD>EPSG</TD>
 * </TR>
 * <TR>
 *   <TD>portNumber</TD>
 *   <TD>int</TD>
 *   <TD>TCP port which the PostgreSQL database server is listening on</TD>
 *   <TD>5432</TD>
 * </TR>
 * <TR>
 *   <TD>user</TD>
 *   <TD>String</TD>
 *   <TD>User used to make database connections</TD>
 *   <TD>GeoTools</TD>
 * </TR>
 * <TR>
 *   <TD>password</TD>
 *   <TD>String</TD>
 *   <TD>Password used to make database connections</TD>
 *   <TD>GeoTools</TD></TR>
 * </TABLE>
 * <P>
 * The database version is given in the
 * {@linkplain org.opengis.metadata.citation.Citation#getEdition edition attribute}
 * of the {@linkplain org.opengis.referencing.AuthorityFactory#getAuthority authority}.
 * The postgreSQL database should be read only.
 * <P>
 * Just having this class accessible in the classpath, together with the registration in
 * the {@code META-INF/services/} directory, is suffisient to get a working EPSG authority
 * factory backed by this database. Vendors can create a copy of this class, modify it and
 * bundle it with their own distribution if they want to connect their users to an other
 * database.
 *
 * @version $Id$
 * @author Didier Richard
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
public class PostgreDataSource extends Jdbc3SimpleDataSource implements DataSource {
    /**
     * The schema name, or {@code null} if none.
     */
    private final String schema;

    /**
     * Creates a new instance of this data source.
     */
    public PostgreDataSource() {
        File file = new File("EPSG-DataSource.properties");
        if (!file.isFile()) {
            file = new File(System.getProperty("user.home", "."), "EPSG-DataSource.properties");
            if (!file.isFile()) {
                schema = null;
                return;
            }
        }
        final Properties p = new Properties();
        try {
            final InputStream in = new FileInputStream(file);
            p.load(in);
            in.close();
        } catch (IOException exception) {
            Utilities.unexpectedException("org.geotools.referencing.factory", "DataSource",
                                          "<init>", exception);
        }
        int port;
        try {
            port = Integer.parseInt(p.getProperty("portNumber", "5432"));
        } catch (NumberFormatException exception) {
            port = 5432;
            Utilities.unexpectedException("org.geotools.referencing.factory", "DataSource",
                                          "<init>", exception);
        }
        setServerName  (p.getProperty("serverName",   "localhost"));
        setDatabaseName(p.getProperty("databaseName", "EPSG"     ));
        setUser        (p.getProperty("user",         "Geotools" ));
        setPassword    (p.getProperty("password",     "Geotools" ));
        setPortNumber  (port);
        schema = p.getProperty("schema");
    }

    /**
     * Returns the priority for this data source. This priority is set to a lower value than
     * the {@linkplain AccessDataSource}'s one in order to give the priority to any "official"
     * database installed locally by the user, when available.
     */
    public int getPriority() {
        return NORMAL_PRIORITY - 50;
    }

    /**
     * Open a connection and creates an {@linkplain FactoryUsingSQL EPSG factory} for it.
     *
     * @param  hints A map of hints, including the low-level factories to use for CRS creation.
     * @return The EPSG factory using PostgreSQL syntax.
     * @throws SQLException if connection to the database failed.
     */
    public AbstractAuthorityFactory createFactory(final Hints hints) throws SQLException {
        final FactoryUsingAnsiSQL factory = new FactoryUsingAnsiSQL(hints, getConnection());
        if (schema != null) {
            factory.setSchema(schema);
        }
        return factory;
    }
}
