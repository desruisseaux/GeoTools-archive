/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.arcsde.data.view;

import java.util.Map;

import org.geotools.arcsde.pool.Session;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import com.esri.sde.sdk.client.SeConnection;

/**
 * Qualifies a column name with the ArcSDE "table.user." prefix as required by
 * the ArcSDE java api to not get confused when using joined tables.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/view/ColumnQualifier.java $
 * @since 2.3.x
 */
class ColumnQualifier {
    /**
     * DOCUMENT ME!
     * 
     * @param conn
     *            DOCUMENT ME!
     * @param column
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static Column qualify(Session conn, Map tableAliases, Column column) {
        Table table = column.getTable();

        String columnName = column.getColumnName();

        Table unaliasedTable = (Table) tableAliases.get(table.getName());

        Table qualifiedTable;

        if (unaliasedTable == null) {
            // not an aliased table, qualify it
            qualifiedTable = TableQualifier.qualify(conn, table);
        } else {
            // AllTableColumns is refering to an aliased table in the FROM
            // clause,
            // replace its table by the original one to get rid of the alias
            qualifiedTable = unaliasedTable;
        }

        Column qualifiedColumn = new Column();

        qualifiedColumn.setColumnName(columnName);
        qualifiedColumn.setTable(qualifiedTable);

        return qualifiedColumn;
    }
}
