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

import net.sf.jsqlparser.statement.select.ColumnReference;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;

import com.esri.sde.sdk.client.SeConnection;


/**
 * Qualifies a column reference in an order by clause
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.3.x
  */
public class OrderByElementQualifier implements OrderByVisitor {
    /** DOCUMENT ME!  */
    private OrderByElement qualifiedOrderBy;

    /** DOCUMENT ME!  */
    private SeConnection conn;
    
    private Map tableAliases;

    /**
     * Creates a new OrderByElementQualifier object.
     *
     * @param conn DOCUMENT ME!
     */
    private OrderByElementQualifier(SeConnection conn, Map tableAliases) {
        this.conn = conn;
        this.tableAliases = tableAliases;
    }

    /**
     * DOCUMENT ME!
     *
     * @param conn DOCUMENT ME!
     * @param orderBy DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static OrderByElement qualify(SeConnection conn, Map tableAliases,
        OrderByElement orderBy) {
        if (orderBy == null) {
            return null;
        }

        OrderByElementQualifier qualifier = new OrderByElementQualifier(conn, tableAliases);
        orderBy.accept(qualifier);

        return qualifier.qualifiedOrderBy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param orderBy DOCUMENT ME!
     */
    public void visit(OrderByElement orderBy) {
        OrderByElement qualifiedOrderBy = new OrderByElement();
        qualifiedOrderBy.setAsc(orderBy.isAsc());

        ColumnReference colRef = orderBy.getColumnReference();

        ColumnReference qualifiedColRef = ColumnReferenceQualifier.qualify(conn, tableAliases,
                colRef);

        qualifiedOrderBy.setColumnReference(qualifiedColRef);

        this.qualifiedOrderBy = qualifiedOrderBy;
    }
}
