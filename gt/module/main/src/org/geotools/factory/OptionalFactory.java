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


/**
 * A factory that may not be available in all configurations.
 * <p>
 * Such factories often need some external resources like a big file or a connection
 * to a database. Those resources may not be available at JVM starting time, but become
 * available later.
 * </p>
 * <p>
 * The {@link #isReady} method queries the status of this factory. If an optional factory
 * was aimed to be the primary factory (for example a factory backed by a connection to
 * a database), then {@link FactoryRegistry} will uses fallbacks (for example a factory
 * backed by an ASCII file) as long as the {@code isReady()} method in the primary factory
 * returns {@code false}.
 * </p>
 * Q: Is this wise? If you need to control factory order then a complete plugin system
 * would *know* about start up dependency and not allow an application to get into this
 * mess.
 * A: This really starts to look like you need an API for an ExternalService
 * (or ExternalFactgory), to complete this picture you need a callback mechanism. Something
 * that client code can "give" to the factory, that it will call when ready. If it is ready
 * it will be called immeditely. The above advice about "alternatives" could really be
 * managed by such a factory (especially if it allowed to notify client code more then once.
 * (And yes events would also serve this purpose).
 * </p>
 * @author Martin Desruisseaux
 * @version $Id$
 */
public interface OptionalFactory extends Factory {
    /**
     * Returns {@code true} if this factory is ready for use.
     * An optional factory may returns {@code false} for now but returns {@code true} later.
     */
    public boolean isReady();
}
