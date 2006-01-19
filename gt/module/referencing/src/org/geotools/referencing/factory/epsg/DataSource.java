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
import java.sql.SQLException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.AbstractFactory;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.AbstractAuthorityFactory;


/**
 * A marker interface for data source to an EPSG database. This sub-interface of J2SE's
 * {@code DataSource} is used as a category for {@link javax.imageio.spi.ServiceRegistry}.
 * EPSG data sources should be registered in the following file:
 *
 * <blockquote><pre>META-INF/services/org.geotools.referencing.factory.epsg.DataSource</pre></blockquote>
 *
 * For an example, see {@link org.geotools.referencing.factory.epsg.AccessDataSource}
 * and its {@code META-INF/services/} registration in the {@code plugin/epsg-access}
 * module. This is a very small module which can be used as a starting point for custom
 * EPSG data sources.
 * <p>
 * <h3>How EPSG factory are found</h3>
 * By default, only one {@link DefaultFactory} is registered and returned by {@link FactoryFinder}.
 * We don't need to register any other implementation for an EPSG factory backed by a SQL database.
 * However, {@code DefaultFactory} alone is not suffisient for querying the database. It needs one
 * more "plugable" information: the <cite>connection</cite> to the EPSG database. This
 * {@code DataSource} interface is there for providing such connection to {@code DefaultFactory}.
 * <p>
 * Some time after {@code DefaultFactory} is registered as an EPSG factory, it looks for a
 * {@code DataSource} using the same plugins mechanism than for factories. In other words,
 * {@code DataSource} is a second level of plugins used internally by {@link DefaultFactory}.
 *
 * @since 2.1
 * @source $URL$
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
     * Opens a connection and creates an EPSG factory for it. This method may returns a
     * sub-class of {@link FactoryUsingSQL} if it wants to use slightly different SQL queries.
     *
     * @param  hints A map of hints, including the low-level factories to use for CRS creation.
     *         This argument should be given unchanged to {@code FactoryUsingSQL} constructor.
     * @return The {@linkplain FactoryUsingSQL EPSG factory} using SQL queries appropriate
     *         for this data source.
     * @throws SQLException if connection to the database failed.
     */
    AbstractAuthorityFactory createFactory(final Hints hints) throws SQLException;
}
