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


/**
 * A marker interface for data source to an EPSG database. This sub-interface of J2SE's
 * {@code DataSource} is used as a category for {@link javax.imageio.spi.ServiceRegistry}.
 * EPSG data sources can be registered in the following file:
 *
 * <blockquote><pre>
 * META-INF/services/org.geotools.referencing.factory.epsg.DataSource
 * </pre></blockquote>
 *
 * An example of registered EPSG data source in the EPSG plugin backed by an
 * embedded HSQL database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface DataSource extends javax.sql.DataSource {
    /**
     * Returns {@code true} if this data source is preferred over the default JDBC-ODBC bridge,
     * or {@code false} if it is just a fallback.
     */
    boolean isPreferred();
}
