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

// Geotools dependencies
import org.geotools.factory.AbstractFactory;


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
     * The minimum priority for a data source, which is {@value}.
     * Data sources with lowest priority will be used only if there is no other source available.
     */
    int MINIMUM_PRIORITY = AbstractFactory.MINIMUM_PRIORITY;

    /**
     * The default priority, which is {@value}.
     */
    int NORMAL_PRIORITY = AbstractFactory.NORMAL_PRIORITY;

    /**
     * The maximum priority for a data source, which is {@value}.
     * Data sources with highest priority will be preferred to any other data sources.
     */
    int MAXIMUM_PRIORITY = AbstractFactory.MAXIMUM_PRIORITY;

    /**
     * Returns the priority for this data source, as a number between
     * {@link #MINIMUM_PRIORITY} and {@link #MAXIMUM_PRIORITY} inclusive.
     */
    int getPriority();

    /**
     * Returns {@code true} if the database uses standard SQL syntax, or {@code false} if it
     * uses the MS-Access syntax.
     *
     * @todo We should remove this method and lets DefaultFactory tries different
     *       SELECT instructions. For example we could try a SQL statement like
     *       "SELECT id FROM epsg_sometable WHERE ID=0" (we don't mind if the result
     *       set contains no record) and use {@link FactoryForSQL} if the above didn't
     *       threw a SQLException. An alternative is to define a 'getFactory' method
     *       instead, and lets the DataSource built its own EPSG factory. But in this
     *       case, it will be hard to avoid creating a real subclass of
     *       {@code sun.jdbc.odbc.ee.DataSource}.
     */
    boolean isStandardSQL();
}
