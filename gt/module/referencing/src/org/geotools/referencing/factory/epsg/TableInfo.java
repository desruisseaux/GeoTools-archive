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


/**
 * Information about a specific table. This class also provides some utility methods
 * for the creation of SQL queries. The MS-Access dialect of SQL is assumed (it will
 * be translated into ANSI SQL later by {@link FactoryUsingSQL#adaptSQL} if needed).
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class TableInfo {
    /**
     * The class of object to be created.
     */
    public final Class type;

    /**
     * The table name for SQL queries. May contains a {@code "JOIN"} clause.
     */
    public final String table;

    /**
     * Column name for the code (usually with the {@code "_CODE"} suffix).
     */
    public final String codeColumn;

    /**
     * Column name for the name (usually with the {@code "_NAME"} suffix), or {@code null}.
     */
    public final String nameColumn;

    /**
     * Column type for the type (usually with the {@code "_TYPE"} suffix), or {@code null}.
     */
    public final String typeColumn;

    /**
     * Sub-interfaces of {@link #type} to handle, or {@code null} if none.
     */
    public final Class[] subTypes;

    /**
     * Names of {@link #subTypes} in the database, or {@code null} if none.
     */
    public final String[] typeNames;

    /**
     * Stores information about a specific table.
     */
    TableInfo(final Class type, final String table,
              final String codeColumn, final String nameColumn)
    {
        this(type, table, codeColumn, nameColumn, null, null, null);
    }

    /**
     * Stores information about a specific table.
     */
    TableInfo(final Class type,
              final String table, final String codeColumn, final String nameColumn,
              final String typeColumn, final Class[] subTypes, final String[] typeNames)
    {
        this.type       = type;
        this.table      = table;
        this.codeColumn = codeColumn;
        this.nameColumn = nameColumn;
        this.typeColumn = typeColumn;
        this.subTypes   = subTypes;
        this.typeNames  = typeNames;
    }
}
