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
package org.geotools.referencing.factory;

// J2SE dependencies
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.text.Format;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.referencing.wkt.Parser;
import org.geotools.referencing.wkt.Symbols;
import org.geotools.util.DerivedSet;
import org.geotools.util.SimpleInternationalString;


/**
 * Default implementation for a coordinate reference system authority factory
 * backed by a property file. This gives some of the benificts of using the
 * {@linkplain org.geotools.referencing.factory.epsg.FactoryUsingSQL EPSG database backed
 * authority factory} (for example), in a portable property file.
 * <br><br>
 * This factory doesn't cache any result. Any call to a {@code createFoo} method will send a new
 * query to the EPSG database. For caching, this factory should be wrapped in some buffered factory
 * like {@link BufferedAuthorityFactory}.
 * <br><br>
 * This authority factory has a low priority. By default,
 * {@link org.geotools.referencing.FactoryFinder} will uses it only if it has been unable
 * to get a connection to a more suitable database like EPSG.
 *
 * @version $Id$
 * @author Jody Garnett
 * @author Rueben Schulz
 * @author Martin Desruisseaux
 */
public class PropertyAuthorityFactory extends AbstractAuthorityFactory {
    /**
     * The authority for this factory.
     */
    private final Citation authority;
    
    /**
     * The properties object for our properties file. Keys are the authority
     * code for a coordinate reference system and the associated value is a 
     * WKT string for the CRS.
     */
    private final Properties definitions = new Properties();

    /**
     * An unmodifiable view of the authority keys. This view is always up to date
     * even if entries are added or removed in the {@linkplain #definitions} map.
     */
    private final Set codes = Collections.unmodifiableSet(definitions.keySet());

    /**
     * Views of {@link #codes} for different types. Views will be constructed only when first
     * needed. View are always up to date even if entries are added or removed in the
     * {@linkplain #definitions} map.
     */
    private transient Map filteredCodes;

    /**
     * A WKT parser. Will be used only if the generic {@link #createObject} method is invoked.
     */
    private transient Format parser;
    
    /**
     * Loads from the specified property file.
     *
     * @param  factories   The underlying factories used for objects creation.
     * @param  authority   The organization or party responsible for definition and maintenance of
     *                     the database.
     * @param  definitions URL to the definition file.
     * @throws IOException if the definitions can't be read.
     */
    public PropertyAuthorityFactory(final FactoryGroup factories,
                                    final Citation     authority,
                                    final URL          definitions)
            throws IOException
    {
        super(factories, MINIMUM_PRIORITY+10);
        this.authority = authority;
        final InputStream in = definitions.openStream();
        this.definitions.load(in);
        in.close();
    }

    /**
     * Returns the organization or party responsible for definition and maintenance of the
     * database.
     */
    public Citation getAuthority() {
        return authority;
    }
    
    /**  
     * Returns the set of authority codes of the given type. The type  
     * argument specify the base class. For example if this factory is 
     * an instance of CRSAuthorityFactory, then:
     * <ul>
     *  <li>{@code CoordinateReferenceSystem.class} asks for all authority codes accepted by
     *      {@link #createGeographicCRS createGeographicCRS},
     *      {@link #createProjectedCRS  createProjectedCRS},
     *      {@link #createVerticalCRS   createVerticalCRS},
     *      {@link #createTemporalCRS   createTemporalCRS}
     *      and their friends.</li>
     *  <li>{@code ProjectedCRS.class} asks only for authority codes accepted by
     *      {@link #createProjectedCRS createProjectedCRS}.</li>
     * </ul>
     *
     * The default implementaiton filters the set of codes based on the
     * "PROJCS" and "GEOGCS" at the start of the WKT strings.
     *
     * @param  type The spatial reference objects type (may be {@code Object.class}).
     * @return The set of authority codes for spatial reference objects of the given type.
     *         If this factory doesn't contains any object of the given type, then this method
     *         returns an empty set.
     * @throws FactoryException if access to the underlying database failed.
     */
    public Set getAuthorityCodes(final Class type) throws FactoryException {
        if (type==null || type.isAssignableFrom(IdentifiedObject.class)) {
            return codes;
        }
        if (filteredCodes == null) {
            filteredCodes = new HashMap();
        }
        synchronized (filteredCodes) {
            Set filtered = (Set) filteredCodes.get(type);
            if (filtered == null) {
                filtered = new Codes(codes, type);
                filteredCodes.put(type, filtered);
            }
            return filtered;
        }
    }

    /**
     * The set of codes for a specific type of CRS. This set filter the codes set in the
     * enclosing {@link PropertyAuthorityFactory} in order to keep only the codes for the
     * specified type. Filtering is performed on the fly. Concequently, this set is cheap
     * if the user just want to check for the existence of a particular code.
     */
    private static final class Codes extends DerivedSet {
        /**
         * The spatial reference objects type (may be {@code Object.class}).
         */
        private final Class type;

        /**
         * Constructs a set of codes for the specified type.
         */
        public Codes(final Set codes, final Class type) {
            super(codes);
            this.type = type;
        }

        /**
         * Returns the element if it is of the expected type, or {@code null} otherwise.
         */
        protected Object baseToDerived(final Object element) {
            final String wkt = (String) element;
            final int length = wkt.length();
            int i=0; while (i<length && Character.isJavaIdentifierPart(wkt.charAt(i))) i++;
            Class candidate = Parser.getClassOf(wkt.substring(0,i));
            if (candidate == null) {
                candidate = IdentifiedObject.class;
            }
            return type.isAssignableFrom(candidate) ? element : null;
        }

        /**
         * Transforms a value in this set to a value in the base set.
         */
        protected Object derivedToBase(final Object element) {
            return element;
        }
    }

    /**
     * Returns the Well Know Text from a code.
     *
     * @param  code Value allocated by authority.
     * @return The Well Know Text (WKT) for the specified code.
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     */
    public String getWKT(final String code) throws NoSuchAuthorityCodeException {
        ensureNonNull("code", code);
        final String epsg = trimAuthority(code);
        final String wkt = definitions.getProperty(epsg);
        if (wkt == null) {
            throw noSuchAuthorityCode(IdentifiedObject.class, code);
        }
        return wkt.trim();
    }

    /**
     * Gets a description of the object corresponding to a code.
     *
     * @param  code Value allocated by authority.
     * @return A description of the object, or <code>null</code> if the object
     *         corresponding to the specified <code>code</code> has no description.
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the query failed for some other reason.
     */
    public InternationalString getDescriptionText(final String code)
            throws NoSuchAuthorityCodeException, FactoryException
    {
        final String wkt = getWKT(code);
        int start = wkt.indexOf('"');
        if (start >= 0) {
            final int end = wkt.indexOf('"', ++start);
            if (end >= 0) {
                return new SimpleInternationalString(wkt.substring(start, end).trim());
            }
        }
        return null;
    } 

    /**
     * Returns an arbitrary object from a code.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public IdentifiedObject createObject(final String code)
            throws NoSuchAuthorityCodeException, FactoryException
    {
        final String wkt = getWKT(code);
        if (parser == null) {
            parser = new Parser(Symbols.DEFAULT, factories);
        }
        try {
            synchronized (parser) {
                return (IdentifiedObject) parser.parseObject(wkt);
            }
        } catch (ParseException exception) {
            throw new FactoryException(exception);
        }
    }

    /**
     * Returns an arbitrary {@linkplain CoordinateReferenceSystem coordinate reference system}
     * from a code. If the coordinate reference system type is know at compile time, it is
     * recommended to invoke the most precise method instead of this one.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public CoordinateReferenceSystem createCoordinateReferenceSystem(final String code) 
            throws NoSuchAuthorityCodeException, FactoryException 
    {
        return factories.getCRSFactory().createFromWKT(getWKT(code));
    }
}
