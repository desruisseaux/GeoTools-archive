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
import java.util.Set;
import java.util.AbstractSet;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A set of EPSG authority codes. This set requires a living connection to the EPSG database.
 * All {@link #iterator} method call creates a new {@link ResultSet} holding the codes. However,
 * call to {@link #contains} map directly to a SQL call.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
final class AuthorityCodes extends AbstractSet {
    /**
     * The type for this code set. This is translated to the most appropriate
     * interface type even if the user supplied an implementation type.
     */
    public final Class type;

    /**
     * A view of this set as a map with object's name as value, or {@code null} if none.
     * Will be created only when first needed.
     */
    private transient java.util.Map asMap;

    /**
     * The SQL command to use for creating the {@code queryAll} statement.
     */
    final String sqlAll;

    /**
     * The SQL command to use for creating the {@code querySingle} statement.
     */
    private final String sqlSingle;

    /**
     * The statement to use for querying all codes.
     * Will be created only when first needed.
     */
    private transient PreparedStatement queryAll;

    /**
     * The statement to use for querying a single code.
     * Will be created only when first needed.
     */
    private transient PreparedStatement querySingle;

    /**
     * The connection to the underlying database. This set should never close
     * this connection. Closing it is {@link FactoryUsingSQL}'s job.
     */
    private final Connection connection;

    /**
     * The collection's size, or -1 if not yet computed.
     * Records will be counted only when first needed.
     */
    private int size = -1;

    /**
     * Creates a new set of authority codes for the specified type.
     *
     * @param  connection The connection to the EPSG database.
     * @param  table      The table to query.
     * @param  type       The type to query.
     * @param  factory    The factory originator.
     */
    public AuthorityCodes(final Connection      connection,
                          final TableInfo       table,
                          final Class           type,
                          final FactoryUsingSQL factory)
    {
        this.connection = connection;
        final StringBuffer buffer = new StringBuffer("SELECT ");
        buffer.append(table.codeColumn);
        if (table.nameColumn != null) {
            buffer.append(", ");
            buffer.append(table.nameColumn);
        }
        buffer.append(" FROM ");
        buffer.append(table.table);
        boolean hasWhere = false;
        Class tableType = table.type;
        if (table.typeColumn != null) {
            // Iterates in reverse order in order to set
            // 'tableType' to the first occurence found.
            for (int i=table.subTypes.length; --i>=0;) {
                final Class candidate = table.subTypes[i];
                if (candidate.isAssignableFrom(type)) {
                    buffer.append(hasWhere ? " OR " : " WHERE (");
                    buffer.append(table.typeColumn);
                    buffer.append(" LIKE '");
                    buffer.append(table.typeNames[i]);
                    buffer.append("%'");
                    hasWhere = true;
                    tableType = candidate;
                }
            }
            if (hasWhere) {
                buffer.append(')');
            }
        }
        this.type = tableType;
        final int length = buffer.length();
        buffer.append(" ORDER BY ");
        buffer.append(table.codeColumn);
//        buffer.trimToSize(); // TODO: Uncomment when we will be allowed to compile for J2SE 1.5.
        sqlAll = factory.adaptSQL(buffer.toString());

        buffer.setLength(length);
        buffer.append(hasWhere ? " AND " : " WHERE ");
        buffer.append(table.codeColumn);
        buffer.append(" = ?");
//        buffer.trimToSize(); // TODO: Uncomment when we will be allowed to compile for J2SE 1.5.
        sqlSingle = factory.adaptSQL(buffer.toString());
    }

    /**
     * Returns all codes.
     */
    private synchronized ResultSet getAll() throws SQLException {
        if (queryAll == null) {
            queryAll = connection.prepareStatement(sqlAll);
        }
        return queryAll.executeQuery();
    }

    /**
     * Returns a single code.
     */
    private synchronized ResultSet getSingle(final Object code) throws SQLException {
        if (querySingle == null) {
            querySingle = connection.prepareStatement(sqlSingle);
        }
        querySingle.setString(1, code.toString());
        final ResultSet results = querySingle.executeQuery();
        return results;
    }

    /**
     * Returns {@code true} if this collection contains no elements.
     * This method fetch at most one row instead of counting all rows.
     */
    public boolean isEmpty() {
        if (size >= 0) { // No need to synchronize
            return size == 0;
        }
        boolean empty = true;
        try {
            final ResultSet results = getAll();
            empty = !results.next();
            results.close();
        } catch (SQLException exception) {
            unexpectedException("isEmpty", exception);
        }
        return empty;
    }

    /**
     * Count the number of elements in the underlying result set.
     */
    public int size() {
        if (size >= 0) { // No need to synchronize
            return size;
        }
        int count = 0;
        try {
            final ResultSet results = getAll();
            while (results.next()) {
                count++;
            }
            results.close();
            size = count; // Stores only on success.
        } catch (SQLException exception) {
            unexpectedException("size", exception);
        }
        return count;
    }

    /**
     * Returns {@code true} if this collection contains the specified element.
     */
    public synchronized boolean contains(final Object code) {
        boolean exists = false;
        if (code != null) try {
            final ResultSet results = getSingle(code);
            exists = results.next();
            results.close();
        } catch (SQLException exception) {
            unexpectedException("contains", exception);
        }
        return exists;
    }

    /**
     * Returns an iterator over the codes. The iterator is backed by a living {@link ResultSet},
     * which will be closed as soon as the iterator reach the last element.
     */
    public java.util.Iterator iterator() {
        try {
            return new Iterator(getAll());
        } catch (SQLException exception) {
            unexpectedException("iterator", exception);
            return Collections.EMPTY_SET.iterator();
        }
    }

    /**
     * Closes the underlying statements. Note: this method is also invoked directly
     * by {@link FactoryUsingSQL#dispose}, which is okay in this particular case since
     * the implementation of this method can be executed an arbitrary amount of times.
     */
    protected synchronized void finalize() throws SQLException {
        if (querySingle != null) {
            querySingle.close();
            querySingle = null;
        }
        if (queryAll != null) {
            queryAll.close();
            queryAll = null;
        }
    }

    /**
     * Invoked when an exception occured. This method just log a warning.
     */
    private static void unexpectedException(final String       method,
                                            final SQLException exception)
    {
        unexpectedException("AuthorityCodes", method, exception);
    }

    /**
     * Invoked when an exception occured. This method just log a warning.
     */
    static void unexpectedException(final String       classe,
                                    final String       method,
                                    final SQLException exception)
    {
        Utilities.unexpectedException("org.geotools.referencing.factory", classe, method, exception);
    }

    /**
     * The iterator over the codes. This inner class must kept a reference toward the enclosing
     * {@link AuthorityCodes} in order to prevent a call to {@link AuthorityCodes#finalize}
     * before the iteration is finished. Concequently, this inner class should not be static
     * even if adding a "static" keyword do not introduces any compilation error.
     */
    private final class Iterator implements java.util.Iterator {
        /** The result set, or {@code null} if there is no more elements. */
        private ResultSet results;

        /** The next code. */
        private transient String next;

        /** Creates a new iterator for the specified result set. */
        Iterator(final ResultSet results) throws SQLException {
            this.results = results;
            toNext();
        }

        /** Moves to the next element. */
        private void toNext() throws SQLException {
            if (results.next()) {
                next = results.getString(1);
            } else {
                results.close();
                results = null;
            }
        }

        /** Returns {@code true} if there is more elements. */
        public boolean hasNext() {
            return results != null;
        }

        /** Returns the next element. */
        public Object next() {
            if (results == null) {
                throw new NoSuchElementException();
            }
            final String current = next;
            try {
                toNext();
            } catch (SQLException exception) {
                results = null;
                unexpectedException("AuthorityCodes.Iterator", "next", exception);
            }
            return current;
        }

        /** Always throws an exception, since this iterator is read-only. */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /** Closes the underlying result set. */
        protected void finalize() throws SQLException {
            if (results != null) {
                results.close();
                results = null;
            }
        }
    }

    /**
     * Returns a view of this set as a map with object's name as value, or {@code null} if none.
     */
    final java.util.Map asMap() {
        if (asMap == null) {
            asMap = new Map();
        }
        return asMap;
    }
    
    /**
     * A view of {@link AuthorityCodes} as a map, with authority codes as key and
     * object names as values.
     */
    private final class Map extends AbstractMap {
        /**
         * Returns the number of key-value mappings in this map.
         */
        public int size() {
            return AuthorityCodes.this.size();
        }

        /**
         * Returns {@code true} if this map contains no key-value mappings.
         */
        public boolean isEmpty() {
            return AuthorityCodes.this.isEmpty();
        }

        /**
         * Returns the description to which this map maps the specified EPSG code.
         */
        public Object get(final Object code) {
            String value = null;
            if (code != null) try {
                final ResultSet results = getSingle(code);
                if (results.next()) {
                    value = results.getString(2);
                }
                results.close();
            } catch (SQLException exception) {
                unexpectedException("get", exception);
            }
            return value;
        }

        /**
         * Returns {@code true} if this map contains a mapping for the specified EPSG code.
         */
        public boolean containsKey(final Object key) {
            return contains(key);
        }

        /**
         * Returns a set view of the keys contained in this map.
         */
        public Set keySet() {
            return AuthorityCodes.this;
        }

        /**
         * Returns a set view of the mappings contained in this map.
         *
         * @todo Not yet implemented.
         */
        public Set entrySet() {
            throw new UnsupportedOperationException();
        }
    }
}
