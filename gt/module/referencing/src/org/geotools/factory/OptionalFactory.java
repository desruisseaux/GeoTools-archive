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
package org.geotools.factory;

// J2SE dependencies
import javax.imageio.spi.ServiceRegistry; // For javadoc


/**
 * An optional factory that may not be available in all configurations.
 * <p>
 * Such factories often need some external resources. For example the default
 * {@linkplain org.geotools.referencing.factory.epsg.FactoryUsingSQL EPSG factory} need a
 * MS-Access database installed on the client machine. This database is not bundle in
 * Geotools distribution; if the user have not installed it, the factory can't work.
 * <p>
 * This interface is <strong>not</strong> a manager for automatic download of external resources.
 * It is just a way to tell to {@link FactoryFinder} that this factory exists, but can't do its
 * job for whatever reasons (usually a missing resource that the user shall download and install
 * himself), so {@code FactoryFinder} has to choose an other factory. In other words, the
 * {@code OptionalFactory} interface is used as a filter, nothing else. The process is as follows:
 * <p>
 * <ul>
 *   <li>When {@link FactoryRegistry#getServiceProvider} is invoked, it starts to iterate over all
 *       registered factories. If an {@linkplain ServiceRegistry#setOrdering ordering is set}, it
 *       is taken in account for the iteration order.</li>
 *   <li>If no suitable factory was found before the iterator reachs this optional factory, then
 *       {@link #isAvailable} is invoked. If it returns {@code true}, then this optional factory
 *       is processed like any other factories. Otherwise it is ignored.</li>
 * </ul>
 * <p>
 * <strong>NOTE:</strong> {@code OptionalFactory} is not designed for factories with intermittent
 * state (i.e. return value of {@link #isAvailable} varying in an unpredictable way). The behavior
 * is undetermined if the {@code isAvailable()} state changes with time.
 *
 * @author Martin Desruisseaux
 * @source $URL$
 * @version $Id$
 *
 * @see org.geotools.data.DataStoreFactorySpi#isAvailable
 */
public interface OptionalFactory extends Factory {
    /**
     * Returns {@code true} if this factory is ready for use.
     * An optional factory may returns {@code false} for now but returns {@code true} later.
     * However, the converse is not recommended.
     */
    boolean isAvailable();
}
