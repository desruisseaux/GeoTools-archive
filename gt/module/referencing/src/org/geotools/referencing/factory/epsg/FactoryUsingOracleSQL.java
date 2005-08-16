/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.FactoryGroup;


/**
 * An EPSG factory suitable for Oracle SQL syntax.
 *
 * @version $Id$
 * @author John Grange
 *
 * @since 2.1
 *
 * @todo Since this class is constructed through the service provider API rather than directly
 *       instantiated by the user, we need some way to pass the schema information to this class.
 *       one possible approach is to set the schema in preferences. Maybe a better was is to look
 *       for a place in the Oracle {@link javax.sql.DataSource} for that.
 */
public class FactoryUsingOracleSQL extends FactoryUsingAnsiSQL {
    /**
     * The pattern to use for removing <code>" as "</code> elements from the SQL statements.
     */
    private final Pattern pattern = Pattern.compile("\\sAS\\s");

    /**
     * Constructs an authority factory using the specified connection.
     *
     * @param factories  The underlying factories used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     *
     * @deprecated Use {@link #FactoryUsingOracleSQL(Hints,Connection)} instead.
     */
    public FactoryUsingOracleSQL(final FactoryGroup factories,
                                 final Connection  connection)
    {
        this(new Hints(FactoryGroup.HINT_KEY, factories), connection);
    }

    /**
     * Constructs an authority factory using the specified connection.
     *
     * @param hints      The underlying factories used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     *
     * @since 2.2
     */
    public FactoryUsingOracleSQL(final Hints      hints,
                                 final Connection connection)
    {
        super(hints, connection);
    }

    /**
     * Constructs an authority factory using the specified connection to an EPSG database
     * and a database schema. If the database schema is not supplied, or it is null
     * or an empty string, then the tables are assumed to be in the same schema as
     * the user which is being used to connect to the database.  You <strong>MUST</strong>
     * ensure that the connecting user has permissions to select from all the tables in the
     * epsg user schema.
     *
     * @param factories  The underlying factories used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     * @param epsgSchema The database schema in which the epsg tables are stored (optional).
     *
     * @deprecated Use {@link #FactoryUsingOracleSQL(Hints,Connection,String)} instead.
     */
    public FactoryUsingOracleSQL(final FactoryGroup factories,
                                 final Connection  connection,
                                 final String      epsgSchema)
    {
        this(new Hints(FactoryGroup.HINT_KEY, factories), connection, epsgSchema);
    }

    /**
     * Constructs an authority factory using the specified connection to an EPSG database
     * and a database schema. If the database schema is not supplied, or it is null
     * or an empty string, then the tables are assumed to be in the same schema as
     * the user which is being used to connect to the database.  You <strong>MUST</strong>
     * ensure that the connecting user has permissions to select from all the tables in the
     * epsg user schema.
     *
     * @param hints      The underlying factories used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     * @param epsgSchema The database schema in which the epsg tables are stored (optional).
     *
     * @since 2.2
     */
    public FactoryUsingOracleSQL(final Hints      hints,
                                 final Connection connection,
                                 final String     epsgSchema)
    {
        super(hints, connection);
        adaptTableNames(epsgSchema);
    }

    /**
     * Modifies the given SQL string to be suitable for an Oracle databases.
     * This removes {@code " AS "} elements from the SQL statements as
     * these don't work in oracle.
     *
     * @param statement The statement in MS-Access syntax.
     * @return The SQL statement to use, suitable for an Oracle database.
     */
    protected String adaptSQL(final String statement) {
        return pattern.matcher(super.adaptSQL(statement)).replaceAll(" ");
    }

    /**
     * If we have been supplied with a non null {@code epsgSchema},
     * prepend the schema to all the table names.
     *
     * @param epsgSchema The database schema in which the epsg tables are stored (optional).
     */
    private void adaptTableNames(String epsgSchema) {
        if (epsgSchema != null) {
            epsgSchema = epsgSchema.trim();
            if (epsgSchema.length() != 0) {
                for (final Iterator it=map.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry  entry = (Map.Entry) it.next();
                    final String tableName = (String) entry.getValue();
                    /**
                     * Update the map, prepending the schema name to the table name
                     * so long as the value is a table name and not a field. This
                     * algorithm assumes that all old table names start with "epsg_".
                     */
                    if (tableName.startsWith("epsg_")) {
                        entry.setValue(epsgSchema + '.' + tableName);
                    }
                }
            }
        }
    }
}
