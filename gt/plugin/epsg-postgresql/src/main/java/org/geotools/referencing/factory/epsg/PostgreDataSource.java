/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

// PostgreSQL dependencies
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
 * {@value #CONFIGURATION_FILE} file is found in current directory or in the user's home
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
 *   <TD>{@code serverName}</TD>
 *   <TD>String</TD>
 *   <TD>PostgreSQL database server host name</TD>
 *   <TD>{@code localhost}</TD>
 * </TR>
 * <TR>
 *   <TD>{@code databaseName}</TD>
 *   <TD>String</TD>
 *   <TD>PostgreSQL database name</TD>
 *   <TD>{@code EPSG}</TD>
 * </TR>
 * <TR>
 *   <TD>{@code schema}</TD>
 *   <TD>String</TD>
 *   <TD>The schema for the EPSG tables</TD>
 *   <TD></TD>
 * </TR>
 * <TR>
 *   <TD>{@code portNumber}</TD>
 *   <TD>int</TD>
 *   <TD>TCP port which the PostgreSQL database server is listening on</TD>
 *   <TD>{@code 5432}</TD>
 * </TR>
 * <TR>
 *   <TD>{@code user}</TD>
 *   <TD>String</TD>
 *   <TD>User used to make database connections</TD>
 *   <TD>{@code GeoTools}</TD>
 * </TR>
 * <TR>
 *   <TD>{@code password}</TD>
 *   <TD>String</TD>
 *   <TD>Password used to make database connections</TD>
 *   <TD>{@code GeoTools}</TD></TR>
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
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Didier Richard
 * @author Martin Desruisseaux
 *
 * @tutorial http://docs.codehaus.org/display/GEOTOOLS/How+to+install+the+EPSG+database+in+PostgreSQL
 */
public class PostgreDataSource extends Jdbc3SimpleDataSource implements DataSource {
    /**
     * The user configuration file. This class search first for the first file found in the
     * following directories:
     * <ul>
     *   <li>The current directory</li>
     *   <li>The user's home directory</li>
     * </ul>
     */
    public static final String CONFIGURATION_FILE = "EPSG-DataSource.properties";

    /**
     * The schema name, or {@code null} if none.
     */
    private final String schema;

    /**
     * Creates a new instance of this data source.
     */
    public PostgreDataSource() {
        this("localhost", "EPSG", null, "Geotools", "Geotools");
    }

    /**
     * Creates a new instance of this data source with the specified default parameters.
     * If a {@linkplain #CONFIGURATION_FILE configuration file} has been found, then the
     * user setting will override the arguments supplied to this constructor.
     *
     * @param server   The server name.
     * @param database The database name.
     * @param schema   The schema name, or {@code null} if none.
     * @param user     The user name.
     * @param password The password.
     */
    public PostgreDataSource(final String server,
                             final String database,
                             final String schema,
                             final String user,
                             final String password)
    {
        final Properties p = new Properties();
        try {
            load(p);
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
        setPortNumber  (port);
        setServerName  (p.getProperty("serverName",   server  ));
        setDatabaseName(p.getProperty("databaseName", database));
        setUser        (p.getProperty("user",         user    ));
        setPassword    (p.getProperty("password",     password));
        this.schema =  (p.getProperty("schema",       schema  ));
    }

    /**
     * Loads the {@linkplain #CONFIGURATION_FILE configuration file}.
     *
     * @param  The properties in which to stores the configuration informations.
     * @throws IOException if the configuration file was found, but an error occured while
     *         reading it.
     */
    private static void load(final Properties p) throws IOException {
        File file = new File(CONFIGURATION_FILE);
        if (!file.isFile()) {
            file = new File(System.getProperty("user.home", "."), CONFIGURATION_FILE);
            if (!file.isFile()) {
                return;
            }
        }
        final InputStream in = new FileInputStream(file);
        p.load(in);
        in.close();
        return;
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
