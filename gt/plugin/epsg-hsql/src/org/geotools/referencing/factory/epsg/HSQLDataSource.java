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
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.AbstractAuthorityFactory;

// HSQL dependencies
import org.hsqldb.jdbc.jdbcDataSource;


/**
 * Connection to the EPSG database in HSQL database engine format using JDBC. The EPSG
 * database can be downloaded from <A HREF="http://www.epsg.org">http://www.epsg.org</A>.
 * The SQL scripts (modified for the HSQL syntax as <A HREF="doc-files/HSQL.html">explained
 * here</A>) are bundled into this plugin. The database version is given in the
 * {@linkplain org.opengis.metadata.citation.Citation#getEdition edition attribute}
 * of the {@linkplain org.opengis.referencing.AuthorityFactory#getAuthority authority}.
 * The HSQL database is read only.
 * <P>
 * <H3>Implementation note</H3>
 * The SQL scripts are executed the first time a connection is required. The database
 * is then created as cached tables ({@code HSQL.properties} and {@code HSQL.data} files)
 * in a temporary directory. Future connections to the EPSG database while reuse the cached
 * tables, if available. Otherwise, the scripts will be executed again in order to recreate
 * them.
 * <p>
 * If the EPSG database should be created in a different directory (or already exists in that
 * directory), this directory can be specified in two ways:
 * <p>
 * <ul>
 *   <li>It may be given explicitly as an argument to the {@linkplain #HSQLDataSource(File)
 *       constructor}.</li>
 *   <li>It may be specified as a {@linkplain System#getProperty(String) system property}
 *       nammed {@value #DIRECTORY_KEY}.</li>
 * </ul>
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Didier Richard
 */
public class HSQLDataSource extends jdbcDataSource implements DataSource {
    /**
     * The key for fetching the database directory from {@linkplain System#getProperty(String)
     * system properties}.
     *
     * @since 2.3
     */
    public static final String DIRECTORY_KEY = "EPSG-HSQL.directory";

    /**
     * The database name.
     *
     * @since 2.3
     */
    public static final String DATABASE_NAME = "EPSG";

    /**
     * The directory where the database is stored.
     */
    private File directory;

    /**
     * Creates a new instance of this data source. If the {@value #DIRECTORY_KEY}
     * {@linkplain System#getProperty(String) system property} is defined and contains
     * the name of a directory with a valid {@linkplain File#getParent parent}, then the
     * {@value #DATABASE_NAME} database will be saved in that directory. Otherwise, a
     * temporary directory will be used.
     */
    public HSQLDataSource() {
        this(getDirectory());
    }

    /**
     * Creates a new instance of this data source using the {@value #DATABASE_NAME} database in the
     * specified directory. If {@code directory} is {@code null}, then callers are responsible
     * to invoke {@link #setDatabase} explicitly.
     *
     * @param directory The directory for the {@value #DATABASE_NAME} HSQL database,
     *        or {@code null} if none.
     *
     * @since 2.3
     */
    public HSQLDataSource(final File directory) {
        this.directory = directory;
        if (directory != null) {
            /*
             * Constructs the full path to the HSQL database. Note: we do not use
             * File.toURI() because HSQL doesn't seem to expect an encoded URL
             * (e.g. "%20" instead of spaces).
             */
            final StringBuffer url = new StringBuffer("jdbc:hsqldb:file:");
            final String path = directory.getAbsolutePath().replace(File.separatorChar, '/');
            if (path.length()==0 || path.charAt(0)!='/') {
                url.append('/');
            }
            url.append(path);
            if (url.charAt(url.length()-1) != '/') {
                url.append('/');
            }
            url.append(DATABASE_NAME);
            setDatabase(url.toString());
            this.directory = directory;
        }
        /*
         * If the temporary directory do not exists or can't be created,
         * lets the 'database' attribute unset. If the user do not set it
         * explicitly (for example through JNDI), an exception will be thrown
         * when 'getConnection()' will be invoked.
         */
        setUser("SA"); // System administrator. No password.
    }

    /**
     * Returns the priority for this data source. This priority is set to a lower value than
     * the {@linkplain AccessDataSource}'s one in order to give the priority to the Access-backed
     * database, if presents. Priorities are set that way because:
     * <ul>
     *   <li>The MS-Access format is the primary EPSG database format.</li>
     *   <li>If a user downloads the MS-Access database himself, he probably wants to use it.</li>
     * </ul>
     */
    public int getPriority() {
        return NORMAL_PRIORITY - 30;
    }

    /**
     * Returns the default directory for the EPSG database. If the {@value #DIRECTORY_KEY}
     * {@linkplain System#getProperty(String) system property} is defined and contains the
     * name of a directory with a valid {@linkplain File#getParent parent}, then the
     * {@value #DATABASE_NAME} database will be saved in that directory. Otherwise,
     * a temporary directory will be used.
     */
    private static File getDirectory() {
        try {
            final String property = System.getProperty(DIRECTORY_KEY);
            if (property != null) {
                final File directory = new File(property);
                /*
                 * Creates the directory if needed (mkdir), but NOT the parent directories (mkdirs)
                 * because a missing parent directory may be a symptom of an installation problem.
                 * For example if 'directory' is a subdirectory in the temporary directory (~/tmp/),
                 * this temporary directory should already exists. If it doesn't, an administrator
                 * should probably looks at this problem.
                 */
                if (directory.isDirectory() || directory.mkdir()) {
                    return directory;
                }
            }
        } catch (SecurityException e) {
            /*
             * Can't fetch the base directory from system properties.
             * Fallback on the default temporary directory.
             */
        }
        File directory = new File(System.getProperty("java.io.tmpdir", "."), "Geotools");
        if (directory.isDirectory() || directory.mkdir()) {
            directory = new File(directory, "Databases/HSQL");
            if (directory.isDirectory() || directory.mkdirs()) {
                return directory;
            }
        }
        return null;
    }

    /**
     * Returns {@code true} if the database contains data. This method returns {@code false}
     * if an empty EPSG database has been automatically created by HSQL and not yet populated.
     */
    private static boolean dataExists(final Connection connection) throws SQLException {
        final ResultSet tables = connection.getMetaData().getTables(
                null, null, "EPSG_%", new String[] {"TABLE"});
        final boolean exists = tables.next();
        tables.close();
        return exists;
    }

    /**
     * Opens a connection to the database. If the cached tables are not available,
     * they will be created now from the SQL scripts bundled in this plugin.
     */
    public Connection getConnection() throws SQLException {
        final String database = getDatabase();
        if (database==null || database.trim().length()==0) {
            /*
             * The 'database' attribute is unset if the constructor has been unable
             * to locate the temporary directory, or to create the subdirectory.
             */
            // TODO: localize
            throw new SQLException("Can't write to the database directory.");
        }
        Connection connection = super.getConnection();
        if (!dataExists(connection)) {
            /*
             * HSQL has created automatically an empty database. We need to populate it.
             * Executes the SQL scripts bundled in the JAR. In theory, each line contains
             * a full SQL statement. For this plugin however, we have compressed "INSERT
             * INTO" statements using Compactor class in this package.
             */
            Logger.getLogger("org.geotools.referencing.factory").config("Creating cached EPSG database."); // TODO: localize
            final Statement statement = connection.createStatement();
            try {
                final BufferedReader in = new BufferedReader(new InputStreamReader(
                        HSQLDataSource.class.getResourceAsStream("EPSG.sql"), "ISO-8859-1"));
                StringBuffer insertStatement = null;
                String line;
                while ((line=in.readLine()) != null) {
                    line = line.trim();
                    final int length = line.length();
                    if (length != 0) {
                        if (line.startsWith("INSERT INTO")) {
                            /*
                             * We are about to insert many rows into a single table.
                             * The row values appear in next lines; the current line
                             * should stop right after the VALUES keyword.
                             */
                            insertStatement = new StringBuffer(line);
                            continue;
                        }
                        if (insertStatement != null) {
                            /*
                             * We are about to insert a row. Prepend the "INSERT INTO"
                             * statement and check if we will have more rows to insert
                             * after this one.
                             */
                            final int values = insertStatement.length();
                            insertStatement.append(line);
                            final boolean hasMore = (line.charAt(length-1) == ',');
                            if (hasMore) {
                                insertStatement.setLength(insertStatement.length()-1);
                            }
                            line = insertStatement.toString();
                            insertStatement.setLength(values);
                            if (!hasMore) {
                                insertStatement = null;
                            }
                        }
                        statement.execute(line);
                    }
                }
                in.close();
                /*
                 * The database has been fully created. Now, make it read-only.
                 */
                if (directory != null) {
                    final File file = new File(directory, DATABASE_NAME + ".properties");
                    final InputStream propertyIn = new FileInputStream(file);
                    final Properties properties  = new Properties();
                    properties.load(propertyIn);
                    propertyIn.close();
                    properties.put("readonly", "true");
                    final OutputStream out = new FileOutputStream(file);
                    properties.store(out, "EPSG database on HSQL");
                    out.close();
                }
            } catch (IOException exception) {
                statement.close();
                SQLException e = new SQLException("Can't read the SQL script."); // TODO: localize
                e.initCause(exception);
                throw e;
            }
            statement.close();
            connection.close();
            connection = super.getConnection();
            assert dataExists(connection);
        }
        return connection;
    }

    /**
     * Opens a connection and creates an {@linkplain FactoryUsingSQL EPSG factory} for it.
     *
     * @param  hints A map of hints, including the low-level factories to use for CRS creation.
     * @return The EPSG factory using HSQLDB SQL syntax.
     * @throws SQLException if connection to the database failed.
     */
    public AbstractAuthorityFactory createFactory(final Hints hints) throws SQLException {
        return new FactoryUsingHSQL(hints, getConnection());
    }
}
