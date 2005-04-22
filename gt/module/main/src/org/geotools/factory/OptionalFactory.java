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
 * A factory that may not be available in all configurations.
 *
 * <p>Such factories often need some resources to download separatly, like a big file or a
 * database. Those resources may not be installed in all configurations. The {@link #isReady}
 * method tells if this factory has found every resources it needs in order to run.</p>
 *
 * <p>{@link FactoryRegistry#getServiceProvider} iterates over all registered factories. If an
 * {@linkplain ServiceRegistry#setOrdering ordering is set}, it is taken in account. If no suitable
 * factory was found before the iterator reachs this optional factory, then {@code FactoryRegistry}
 * invokes {@link #isReady}. If the later returns {@code true}, then this optional factory is
 * processed like any other factories. Otherwise it is ignored.</p>
 *
 * <p>Optional factories are useful when the preferred factory requires resources that are not
 * guaranteed to be installed on the client machine (e.g. the <A HREF="http://www.epsg.org">EPSG
 * database</A>) and some fallback exists (e.g. an EPSG factory based on a WKT file).</p>
 *
 * <p>Ignored factories are not deregistered; they will be queried again every time
 * {@code getServiceProvider(...)} is invoked and the iterator reachs this optional factory.
 * This means that if a resource was not available at startup time but become available later,
 * it may be selected the next time {@code getServiceProvider(...)} is invoked. It will have no
 * impact on previous results of {@code getServiceProvider(...)} however.</p>
 * 
 * <p>{@code OptionalFactory} is not designed for factories with intermittent state (i.e. return
 * value of {@link #isReady} varying in an unpredictable way). While {@code FactoryRegistry} can
 * provides some deterministic behavior when the {@code isReady()} return value become {@code true}
 * as explained in the previous paragraphe, the converse (when the value was {@code true} and become
 * {@code false}) is unsafe. This is because factories returned by previous calls to
 * {@code getServiceProvider(...)} would stop to work in an unexpected way for clients.</p>
 *
 * @todo This interface is like a tiny skeleton of external service API. To complete the picture
 *       we would need a callback mechanism. A listener that that client code can give to the
 *       factory, that it will call when ready. If it is ready it will be called immeditely.
 *       The above advice about alternatives could really be managed by such a factory (especially
 *       if it allowed to notify client code more then once.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public interface OptionalFactory extends Factory {
    /**
     * Returns {@code true} if this factory is ready for use.
     * An optional factory may returns {@code false} for now but returns {@code true} later.
     * However, the converse is not recommended.
     */
    public boolean isReady();
}
