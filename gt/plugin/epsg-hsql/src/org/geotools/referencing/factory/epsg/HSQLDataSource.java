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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// Geotools dependencies
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.AbstractAuthorityFactory;

// HSQL dependencies
import org.hsqldb.jdbc.jdbcDataSource;


/**
 * Connection to the EPSG database in HSQL database engine format using JDBC. The EPSG
 * database can be downloaded from <A HREF="http://www.epsg.org">http://www.epsg.org</A>.
 * It has been transformed into {@code HSQL.properties} and {@code HSQL.data} files in the
 * {@code epsg} directory. The database version is given in the
 * {@linkplain org.opengis.metadata.citation.Citation#getEdition edition attribute}
 * of the {@linkplain org.opengis.referencing.AuthorityFactory#getAuthority authority}.
 * The HSQL database is read only.
 * <P>
 * Just having this class accessible in the classpath, together with the registration in
 * the {@code META-INF/services/} directory, is suffisient to get a working EPSG authority
 * factory backed by this database. Vendors can create a copy of this class, modify it and
 * bundle it with their own distribution if they want to connect their users to an other
 * database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Didier Richard
 */
public class HSQLDataSource extends jdbcDataSource implements DataSource {
    /**
     * Creates a new instance of this data source
     */
    public HSQLDataSource() {
//        setDatabase("jdbc:hsqldb:res:org/geotools/referencing/factory/epsg/HSQL");
        setDatabase("jdbc:hsqldb:file:HSQL");
        setUser    ("Geotools");
        setPassword("Geotools");
    }

    /**
     * Returns the priority for this data source. This priority is set to a lower value than
     * the {@linkplain AccessDataSource}'s one in order to give the priority to the Access-backed
     * database, if presents.
     */
    public int getPriority() {
        return NORMAL_PRIORITY-10;
    }

    /**
     * Open a connection and creates an {@linkplain EPSGFactory EPSG factory} for it.
     *
     * @param  factories The low-level factories to use for CRS creation.
     * @return The EPSG factory using HSQLDB SQL syntax.
     * @throws SQLException if connection to the database failed.
     */
    public AbstractAuthorityFactory createFactory(final FactoryGroup factories) throws SQLException {
        return new Factory(factories, getConnection());
    }

    /**
     * Adapts SQL statements for HSQL. The HSQL database engine doesn't understand
     * the parenthesis in (INNER JOIN ... ON) statements for the "BursaWolfParameters"
     * query. Unfortunatly, those parenthesis are required by MS-Access. We need to
     * removes them programmatically here.
     *
     * @todo Verify if we can get ride of this hack in a future HSQL version.
     */
    private static final class Factory extends FactoryForSQL {
        /**
         * The regular expression pattern for searching the "FROM (" clause.
         * This is the pattern for the opening parenthesis.
         */
        private static final Pattern OPENING_PATTERN =
                Pattern.compile("\\s+FROM\\s*\\(",
                Pattern.CASE_INSENSITIVE);

        /**
         * Constructs the factory for the given connection to the HSQL database.
         */
        public Factory(final FactoryGroup factories, final Connection connection) {
            super(factories, connection);
        }

        /**
         * If the query contains a "FROM (" expression, remove the parenthesis.
         */
        public String adaptSQL(String query) {
            query = super.adaptSQL(query);
            final Matcher matcher = OPENING_PATTERN.matcher(query);
            if (matcher.find()) {
                final int opening = matcher.end()-1;
                final int length  = query.length();
                int closing = opening;
                for (int count=0; ; closing++) {
                    if (closing >= length) {
                        // Should never happen with well formed SQL statement.
                        // If it happen anyway, don't change anything and let
                        // the HSQL driver produces a "syntax error" message.
                        return query;
                    }
                    switch (query.charAt(closing)) {
                        case '(': count++; break;
                        case ')': count--; break;
                        default : continue;
                    }
                    if (count == 0) {
                        break;
                    }
                }
                query = query.substring(0,         opening) +
                        query.substring(opening+1, closing) +
                        query.substring(closing+1);
            }
            return query;
        }
    }
}
