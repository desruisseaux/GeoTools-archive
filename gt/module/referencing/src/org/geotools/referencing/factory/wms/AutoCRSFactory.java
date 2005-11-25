/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004 Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.wms;

// J2SE dependencies
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.util.SimpleInternationalString;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.factory.AbstractAuthorityFactory;


/**
 * The factory for {@linkplain ProjectedCRS projected CRS} in the {@code AUTO2} space.
 *
 * @since 2.2
 * @version $Id$
 * @author Jody Garnett
 * @author Rueben Schulz
 * @author Martin Desruisseaux
 *
 * @todo Consider renaming this class as {@code Auto2CRSFactory}.
 */
public class AutoCRSFactory extends AbstractAuthorityFactory {
    /**
     * Map of Factlets by integer code (from {@code AUTO:code}).
     *
     * @todo Replace this with full FactorySPI system.
     */
    private final Map factlets = new TreeMap();

    /**
     * Constructs a default factory for the {@code AUTO} authority.
     */
    public AutoCRSFactory() {
        this(null);
    }

    /**
     * Constructs a factory for the {@code AUTO} authority using the specified hints.
     */
    public AutoCRSFactory(final Hints hints) {
        super(hints, NORMAL_PRIORITY);
        add(new Auto42001());
        add(new Auto42002());
        add(new Auto42003());
        add(new Auto42004());
        add(new Auto42005());
    }

    /**
     * Add the specified factlet.
     */
    private void add(final Factlet f) {
        final int code = f.code();
        if (factlets.put(new Integer(code), f) != null) {
            throw new IllegalArgumentException(String.valueOf(code));
        }
    }

    /**
     * Returns the {@link Factlet} for the given code.
     *
     * @param  code The code.
     * @return The fatclet for the specified code.
     * @throws NoSuchAuthorityCodeException if no factlet has been found for the specified code.
     */
    private Factlet findFactlet(final Code code) throws NoSuchAuthorityCodeException {
        if (code.authority.equalsIgnoreCase("AUTO") ||
            code.authority.equalsIgnoreCase("AUTO2"))
        {
            final Integer key = new Integer(code.code);
            final Factlet fac = (Factlet) factlets.get(key);
            if (fac != null) {
                return fac;
            }
        }
        throw noSuchAuthorityCode(code.type, code.toString());
    }

    /**
     * Returns the authority for this factory, which is {@link Citations#AUTO2 AUTO2}.
     */
    public Citation getAuthority() {
        return Citations.AUTO2;
    }

    /**
     * Provides a complete set of the known codes provided by this authority.
     * The codes do not includes the {@code lon0,lat0} part.
     */
    public Set getAuthorityCodes(final Class type) throws FactoryException {
        if (type.isAssignableFrom(ProjectedCRS.class)) {
            final Set set = new LinkedHashSet();
            for (final Iterator it=factlets.keySet().iterator(); it.hasNext();) {
                Integer code = (Integer) it.next();
                set.add("AUTO2:" + code);
            }
            return set;
        } else {
            return Collections.EMPTY_SET;
        }
    }

    /**
     * Returns the CRS name for the given code.
     */
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        final Code c = new Code(code, ProjectedCRS.class);
        return new SimpleInternationalString(findFactlet(c).getName());
    }

    /**
     * Creates an object from the specified code. The default implementation delegates to
     * <code>{@linkplain #createCoordinateReferenceSystem createCoordinateReferenceSystem}(code)</code>.
     */
    public IdentifiedObject createObject(final String code) throws FactoryException {
        return createCoordinateReferenceSystem(code);
    }

    /**
     * Creates a coordinate reference system from the specified code. The default implementation
     * delegates to <code>{@linkplain #createProjectedCRS createProjectedCRS}(code)</code>.
     */
    public CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws FactoryException
    {
        return createProjectedCRS(code);
    }

    /**
     * Creates a projected coordinate reference system from the specified code.
     */
    public ProjectedCRS createProjectedCRS(final String code) throws FactoryException {
        final Code c = new Code(code, ProjectedCRS.class);
        return findFactlet(c).create(c, factories);
    }
}
