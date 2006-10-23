/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
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
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.referencing.wkt.Symbols;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.util.DerivedSet;
import org.geotools.util.SimpleInternationalString;


/**
 * Default implementation for a coordinate reference system authority factory
 * backed by a property file. This gives some of the benificts of using the
 * {@linkplain org.geotools.referencing.factory.epsg.FactoryUsingSQL EPSG database backed
 * authority factory} (for example), in a portable property file.
 * <p>
 * This factory doesn't cache any result. Any call to a {@code createFoo} method will trig a new
 * WKT parsing. For caching, this factory should be wrapped in some buffered factory like
 * {@link BufferedAuthorityFactory}.
 * <p>
 * This authority factory has a low priority. By default,
 * {@link org.geotools.referencing.FactoryFinder} will uses it only if it has been unable
 * to get a connection to a more suitable database like EPSG.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Rueben Schulz
 * @author Martin Desruisseaux
 */
public class PropertyAuthorityFactory extends DirectAuthorityFactory
        implements CRSAuthorityFactory, CSAuthorityFactory, DatumAuthorityFactory
{
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
     * A WKT parser.
     */
    private transient Parser parser;
    
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
        super(factories, MINIMUM_PRIORITY + 10);
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
     * <p>
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
     * {@code "PROJCS"} and {@code "GEOGCS"} at the start of the WKT strings.
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
                filtered = new Codes(definitions, type);
                filteredCodes.put(type, filtered);
            }
            return filtered;
        }
    }

    /**
     * The set of codes for a specific type of CRS. This set filter the codes set in the
     * enclosing {@link PropertyAuthorityFactory} in order to keep only the codes for the
     * specified type. Filtering is performed on the fly. Consequently, this set is cheap
     * if the user just want to check for the existence of a particular code.
     */
    private static final class Codes extends DerivedSet/*<String, String>*/ {
        /**
         * The spatial reference objects type (may be {@code Object.class}).
         */
        private final Class/*<? extends IdentifiedType>*/ type;

        /**
         * The reference to {@link PropertyAuthorityFactory#definitions}.
         */
        private final Map/*<String,String>*/ definitions;

        /**
         * Constructs a set of codes for the specified type.
         */
        public Codes(final Map/*<String,String>*/ definitions,
                     final Class/*<? extends IdentifiedType>*/ type)
        {
            super(definitions.keySet());
            this.definitions = definitions;
            this.type = type;
        }

        /**
         * Returns the code if the associated key is of the expected type, or {@code null}
         * otherwise.
         */
        protected /*String*/ Object baseToDerived(final /*String*/ Object element) {
            final String key = (String) element;
            final String wkt = (String) definitions.get(key);
            final int length = wkt.length();
            int i=0; while (i<length && Character.isJavaIdentifierPart(wkt.charAt(i))) i++;
            Class candidate = Parser.getClassOf(wkt.substring(0,i));
            if (candidate == null) {
                candidate = IdentifiedObject.class;
            }
            return type.isAssignableFrom(candidate) ? key : null;
        }

        /**
         * Transforms a value in this set to a value in the base set.
         */
        protected /*String*/ Object derivedToBase(final /*String*/ Object element) {
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
        final String wkt = definitions.getProperty(trimAuthority(code));
        if (wkt == null) {
            throw noSuchAuthorityCode(IdentifiedObject.class, code);
        }
        return wkt.trim();
    }

    /**
     * Gets a description of the object corresponding to a code.
     *
     * @param  code Value allocated by authority.
     * @return A description of the object, or {@code null} if the object
     *         corresponding to the specified {@code code} has no description.
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
     * Returns the parser.
     */
    private Parser getParser() {
        if (parser == null) {
            parser = new Parser();
        }
        return parser;
    }

    /**
     * Returns an arbitrary object from a code. If the object type is know at compile time, it is
     * recommended to invoke the most precise method instead of this one.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public IdentifiedObject createObject(final String code)
            throws NoSuchAuthorityCodeException, FactoryException
    {
        final String wkt = getWKT(code);
        final Parser parser = getParser();
        try {
            synchronized (parser) {
                parser.code = code;
                return (IdentifiedObject) parser.parseObject(wkt);
            }
        } catch (ParseException exception) {
            throw new FactoryException(exception);
        }
    }

    /**
     * Returns a coordinate reference system from a code. If the object type is know at compile
     * time, it is recommended to invoke the most precise method instead of this one.
     *
     * @param  code Value allocated by authority.
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the object creation failed for some other reason.
     */
    public CoordinateReferenceSystem createCoordinateReferenceSystem(final String code)
            throws NoSuchAuthorityCodeException, FactoryException
    {
        final String wkt = getWKT(code);
        final Parser parser = getParser();
        try {
            synchronized (parser) {
                parser.code = code;
                // parseCoordinateReferenceSystem provides a slightly faster path than parseObject.
                return (CoordinateReferenceSystem) parser.parseCoordinateReferenceSystem(wkt);
            }
        } catch (ParseException exception) {
            throw new FactoryException(exception);
        }
    }

    /**
     * The WKT parser for this authority factory. This parser add automatically the authority
     * code if it was not explicitly specified in the WKT.
     */
    private final class Parser extends org.geotools.referencing.wkt.Parser {
        /**
         * The authority code for the WKT to be parsed.
         */
        String code;

        /**
         * Creates the parser.
         */
        public Parser() {
            super(Symbols.DEFAULT, factories);
        }

        /**
         * Add the authority code to the specified properties, if not already present.
         */
        // @Override
        protected Map alterProperties(Map properties) {
            Object candidate = properties.get(IdentifiedObject.IDENTIFIERS_KEY);
            if (candidate == null && code != null) {
                properties = new HashMap(properties);
                properties.put(IdentifiedObject.IDENTIFIERS_KEY,
                        new NamedIdentifier(authority, trimAuthority(code)));
            }
            return super.alterProperties(properties);
        }
    }
}
