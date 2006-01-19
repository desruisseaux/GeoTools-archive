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
 * A factory that can not determines all its dependencies at construction time.
 * Because this factory may creates new factories at any time, it must keep all
 * user-supplied hints. The general contract of {@link #hints} said that we can't
 * use this field for creating new factories. But instances of this classe are special cases
 * where we can, because this class retains all user-supplied hints.
 *
 * <p>Extends this class only if you really need to keep all user-supplied hints, because
 * doing so has a cost:</p>
 * <ul>
 *   <li><p>{@link FactoryRegistry} is less likely to reuse existing factory instances, because
 *       any value found in {@link #hints} that do not agree with a user-supplied hint will cause
 *       {@code FactoryRegistry} to discarts this instance, even if the hint was actually
 *       irrelevant to this factory.</p></li>
 *   <li><p>The user-supplied hints may contain references to big objects (for example a coordinate
 *       transform backed by a grid), and some of them may not be relevant to this factory.
 *       Retaining all hints will prevents their garbage collection.<p></li>
 * </ul>
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FactoryUsingVolatileDependencies extends AbstractFactory {
    /**
     * Constructs a factory with a default priority.
     *
     * @param hints The user-supplied hints.
     */
    public FactoryUsingVolatileDependencies(final Hints hints) {
        super();
        this.hints.putAll(hints);
    }

    /**
     * Constructs a factory with the specified priority.
     *
     * @param hints The user-supplied hints.
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY} and {@link #MAXIMUM_PRIORITY} inclusive.
     */
    public FactoryUsingVolatileDependencies(final Hints hints, final int priority) {
        super(priority);
        this.hints.putAll(hints);
    }    
}
