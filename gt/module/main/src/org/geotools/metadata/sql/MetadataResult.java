/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.metadata.sql;

// J2SE dependencies
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;


/**
 * The result of a query for metadata attributes. This object {@linkplain PreparedStatement
 * prepares a statement} once for ever for a given table. When a particular record in this
 * table is fetched, the {@link ResultSet} is automatically constructed. If many attributes
 * are fetched consecutivly for the same record, then the same {@link ResultSet} is reused.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Automatically close the ResultSet after some delay (e.g. 2 minutes).
 */
final class MetadataResult {
    /**
     * The table name, used for formatting error message.
     */
    private final String tableName;

    /**
     * The statements for a specific table.
     */
    private final PreparedStatement statement;

    /**
     * The results, or <code>null</code> if not yet determined.
     */
    private ResultSet results;

    /**
     * The identifier (usually the primary key) for current results.
     * If the record to fetch doesn't have the same identifier, then
     * the {@link #results} will need to be closed and reconstructed.
     */
    private int identifier;
    
    /**
     * Constructs a metadata result from the specified connection.
     *
     * @param  connection The connection to the database.
     * @param  query The SQL query. The first question mark will be replaced
     *               by the table name.
     * @param  tableName The table name.
     * @throws SQLException if the statement can't be created.
     */
    public MetadataResult(final Connection connection,
                          final String     query,
                          final String     tableName)
            throws SQLException
    {
        this.tableName = tableName;
        final int index = query.indexOf('?');
        if (index < 0) {
            // TODO: localize
            throw new SQLException("Invalid query");
        }
        final StringBuffer buffer = new StringBuffer(query);
        buffer.replace(index, index+1, tableName);
        statement = connection.prepareStatement(buffer.toString());
    }

    /**
     * Returns the result set for the given record.
     *
     * @param  identifier The object identifier, usually the primary key value.
     * @return The result set.
     * @throws SQLException if an SQL operation failed.
     */
    private ResultSet getResultSet(final int identifier) throws SQLException {
        if (results != null) {
            if (identifier == this.identifier) {
                return results;
            }
            if (results.next()) {
                // TODO: Localize
                Logger.getLogger("org.geotools.metadata.sql")
                      .warning("Duplicate identifier: "+identifier);
            }
            results.close();
            results = null; // In case the 'results = ...' below will fails.
        }
        this.identifier = identifier;
        statement.setInt(1, identifier);
        results = statement.executeQuery();
        if (!results.next()) {
            results.close();
            results = null;
            throw new SQLException("Metadata not found: "+identifier+" in table \""+tableName+'"');
            // TODO: localize
        }
        return results;
    }

    /**
     * Returns the attribute value in the given column for the given record.
     *
     * @param  identifier The object identifier, usually the primary key value.
     * @param  columnName The column name of the attribute to search.
     * @return The attribute value.
     * @throws SQLException if an SQL operation failed.
     */
    public Object getObject(final int identifier, final String columnName) throws SQLException {
        return getResultSet(identifier).getObject(columnName);
    }

    /**
     * Returns the attribute value in the given column for the given record.
     *
     * @param  identifier The object identifier, usually the primary key value.
     * @param  columnName The column name of the attribute to search.
     * @return The attribute value.
     * @throws SQLException if an SQL operation failed.
     */
    public int getInt(final int identifier, final String columnName) throws SQLException {
        return getResultSet(identifier).getInt(columnName);
    }

    /**
     * Returns the string value in the first column of the given record.
     * This is used for fetching the name of a code list element.
     *
     * @param  code The object identifier, usually the primary key value.
     * @return The string value found in the first column.
     * @throws SQLException if an SQL operation failed.
     */
    public String getString(final int code) throws SQLException {
        return getResultSet(code).getString(1);
    }

    /**
     * Close this statement and free all resources.
     * After this method has been invoked, this object can't be used anymore.
     *
     * @throws SQLException if an SQL operation failed.
     */
    public void close() throws SQLException {
        if (results != null) {
            results.close();
            results = null;
        }
        statement.close();
    }
}
