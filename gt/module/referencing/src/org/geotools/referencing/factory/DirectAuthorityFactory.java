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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory;

// J2SE dependencies and extensions
import java.util.Map;
import java.util.Set;
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.referencing.*;
import org.opengis.referencing.crs.*;

// Geotools dependencies
import org.geotools.factory.Hints;


/**
 * The base class for authority factories that create referencing object directly. This is
 * in contrast with other factories like the {@linkplain AuthorityFactoryAdapter adapter}
 * or {@linkplain BufferedAuthorityFactory buffered} ones, which delegates their work to
 * an other factory.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class DirectAuthorityFactory extends AbstractAuthorityFactory {

    // IMPLEMENTATION NOTE:  The reason why this class exist is that we don't want "indirect"
    // factories like BufferedAuthorityFactory to inherit a FactoryGroup field. If this field
    // existed in their super-class, then the super-class constructor could try to initialize
    // it while in fact BufferedAuthorityFactory don't need it.  Experience with Geotools 2.2
    // suggest that it can lead to tricky recursivity problems in FactoryFinder, because most
    // factories registered in META-INF/services are some kind of BufferedAuthorityFactory.

    /**
     * The underlying factories used for objects creation.
     */
    protected final FactoryGroup factories;

    /**
     * Tells if {@link FactoryGroup#hints} has been invoked. It must be invoked exactly once,
     * but can't be invoked in the constructor because it causes a {@link StackOverflowError}
     * in some situations.
     */
    private boolean hintsInitialized;

    /**
     * Constructs an instance using the specified set of factories.
     *
     * @param factories The low-level factories to use.
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     */
    protected DirectAuthorityFactory(final FactoryGroup factories, final int priority) {
        super(priority);
        this.factories = factories;
        ensureNonNull("factories", factories);
    }

    /**
     * Constructs an instance using the specified hints. This constructor recognizes the
     * {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS}, {@link Hints#DATUM_FACTORY DATUM}
     * and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM} {@code FACTORY} hints.
     *
     * @param hints The hints, or {@code null} if none.
     * @param priority The priority for this factory, as a number between
     *        {@link #MINIMUM_PRIORITY MINIMUM_PRIORITY} and
     *        {@link #MAXIMUM_PRIORITY MAXIMUM_PRIORITY} inclusive.
     */
    protected DirectAuthorityFactory(final Hints hints, final int priority) {
        super(priority);
        factories = FactoryGroup.createInstance(hints);
        // Do not copies the user-provided hints to this.hints, because
        // this is up to sub-classes to decide which hints are relevant.
    }

    /**
     * Returns the implementation hints for this factory. The returned map contains values for
     * {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS}, {@link Hints#DATUM_FACTORY DATUM}
     * and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM} {@code FACTORY} hints. Other values
     * may be provided as well, at implementation choice.
     */
    public Map getImplementationHints() {
        synchronized (hints) { // Note: avoid lock on public object.
            if (!hintsInitialized) {
                hintsInitialized = true;
                hints.putAll(factories.getImplementationHints());
            }
        }
        return super.getImplementationHints();
    }
}
