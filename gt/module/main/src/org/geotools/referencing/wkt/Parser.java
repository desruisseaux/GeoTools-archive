/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.referencing.wkt;

// J2SE dependencies
import java.io.BufferedReader;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.units.NonSI;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.OperationMethod;

// Geotools dependencies
import org.geotools.metadata.citation.Citation;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.IdentifiedObject;
import org.geotools.referencing.Identifier;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.Arguments;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * Parser for
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite> (WKT)</A>. This parser can parse {@linkplain MathTransform math transform}
 * objects as well, which is part of the WKT's <code>FITTED_CS</code> element.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">Well Know Text specification</A>
 * @see <A HREF="http://gdal.velocet.ca/~warmerda/wktproblems.html">OGC WKT Coordinate System Issues</A>
 */
public class Parser extends MathTransformParser {
    /**
     * The mapping between WKT element name and the object class to be created.
     * Will be created by {@link #getClassOf} only when first needed.
     */
    private static Map types;
    
    /**
     * The factory to use for creating {@linkplain Datum datum}.
     */
    protected final DatumFactory datumFactory;

    /**
     * The factory to use for creating {@linkplain CoordinateSystem coordinate systems}.
     */
    protected final CSFactory csFactory;

    /**
     * The factory to use for creating {@linkplain CoordinateReferenceSystem
     * coordinate reference systems}.
     */
    protected final CRSFactory crsFactory;

    /**
     * Set of helper methods working on factories. Will be constructed
     * only the first time it is needed.
     */
    private transient FactoryGroup factories;

    /**
     * The list of {@linkplain AxisDirection axis directions} from their name.
     */
    private final Map directions;
    
    /**
     * Constructs a parser using the default set of symbols and factories.
     */
    public Parser() {
        this(Symbols.DEFAULT);
    }
    
    /**
     * Constructs a parser for the specified set of symbols using default factories.
     *
     * @param symbols The symbols for parsing and formatting numbers.
     */
    public Parser(final Symbols symbols) {
        this(symbols,
             FactoryFinder.getDatumFactory(),
             FactoryFinder.getCSFactory(),
             FactoryFinder.getCRSFactory(),
             FactoryFinder.getMathTransformFactory());
    }
    
    /**
     * Constructs a parser for the specified set of symbols using the specified set of factories.
     *
     * @param symbols   The symbols for parsing and formatting numbers.
     * @param factories The factories to use.
     */
    public Parser(final Symbols symbols, final FactoryGroup factories) {
        this(symbols,
             factories.getDatumFactory(),
             factories.getCSFactory(),
             factories.getCRSFactory(),
             factories.getMathTransformFactory());
        this.factories = factories;
    }
    
    /**
     * Constructs a parser for the specified set of symbols using the specified factories.
     *
     * @param symbols      The symbols for parsing and formatting numbers.
     * @param datumFactory The factory to use for creating {@linkplain Datum datum}.
     * @param csFactory    The factory to use for creating {@linkplain CoordinateSystem
     *                     coordinate systems}.
     * @param crsFactory   The factory to use for creating {@linkplain CoordinateReferenceSystem
     *                     coordinate reference systems}.
     * @param mtFactory    The factory to use for creating {@linkplain MathTransform
     *                     math transform} objects.
     */
    public Parser(final Symbols                symbols,
                  final DatumFactory      datumFactory,
                  final CSFactory            csFactory,
                  final CRSFactory          crsFactory,
                  final MathTransformFactory mtFactory)
    {
        super(symbols, mtFactory);
        this.datumFactory = datumFactory;
        this. csFactory   =    csFactory;
        this.crsFactory   =   crsFactory;
        final AxisDirection[] values = AxisDirection.values();
        directions = new HashMap((int)Math.ceil((values.length+1)/0.75f), 0.75f);
        for (int i=0; i<values.length; i++) {
            directions.put(values[i].name().trim().toUpperCase(), values[i]);
        }
    }

    /**
     * Parses a coordinate reference system element.
     *
     * @param  text The text to be parsed.
     * @return The coordinate reference system.
     * @throws ParseException if the string can't be parsed.
     */
    public CoordinateReferenceSystem parseCoordinateReferenceSystem(final String text)
            throws ParseException
    {
        final Element element = getTree(text, new ParsePosition(0));
        final CoordinateReferenceSystem crs = parseCoordinateReferenceSystem(element);
        element.close();
        return crs;
    }
    
    /**
     * Parses a coordinate reference system element.
     *
     * @param  parent The parent element.
     * @return The next element as a {@link CoordinateReferenceSystem} object.
     * @throws ParseException if the next element can't be parsed.
     */
    private CoordinateReferenceSystem parseCoordinateReferenceSystem(final Element element)
            throws ParseException
    {
        final Object key = element.peek();
        if (key instanceof Element) {
            final String keyword = ((Element) key).keyword.trim().toUpperCase(symbols.locale);
            if (   "GEOGCS".equals(keyword)) return parseGeoGCS  (element);
            if (   "PROJCS".equals(keyword)) return parseProjCS  (element);
            if (   "GEOCCS".equals(keyword)) return parseGeoCCS  (element);
            if (  "VERT_CS".equals(keyword)) return parseVertCS  (element);
            if ( "LOCAL_CS".equals(keyword)) return parseLocalCS (element);
            if ( "COMPD_CS".equals(keyword)) return parseCompdCS (element);
            if ("FITTED_CS".equals(keyword)) return parseFittedCS(element);
        }
        throw element.parseFailed(null, Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, key));
    }

    /**
     * Parses the next element in the specified <cite>Well Know Text</cite> (WKT) tree.
     *
     * @param  element The element to be parsed.
     * @return The object.
     * @throws ParseException if the element can't be parsed.
     *
     * @todo All sequences of <code>if ("FOO".equals(keyword))</code> in this method
     *       and other methods of this class and subclasses, could be optimized with
     *       a <code>switch</code> statement.
     */
    protected Object parse(final Element element) throws ParseException {
        final Object key = element.peek();
        if (key instanceof Element) {
            final String keyword = ((Element) key).keyword.trim().toUpperCase(symbols.locale);
            if (       "AXIS".equals(keyword)) return parseAxis      (element, SI.METER, true);
            if (     "PRIMEM".equals(keyword)) return parsePrimem    (element, NonSI.DEGREE_ANGLE);
            if (    "TOWGS84".equals(keyword)) return parseToWGS84   (element);
            if (   "SPHEROID".equals(keyword)) return parseSpheroid  (element);
            if ( "VERT_DATUM".equals(keyword)) return parseVertDatum (element);
            if ("LOCAL_DATUM".equals(keyword)) return parseLocalDatum(element);
            if (      "DATUM".equals(keyword)) return parseDatum     (element,
                              org.geotools.referencing.datum.PrimeMeridian.GREENWICH);
        }
        final MathTransform mt = parseMathTransform(element, false);
        if (mt != null) {
            return mt;
        }
        return parseCoordinateReferenceSystem(element);
    }

    /**
     * Parses an <strong>optional</strong> "AUTHORITY" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * AUTHORITY["<name>", "<code>"]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  name The name of the parent object being parsed.
     * @return A properties map with the parent name and the optional autority code.
     * @throws ParseException if the "AUTHORITY" can't be parsed.
     */
    private static Map parseAuthority(final Element parent, final String name)
            throws ParseException
    {
        final Element element = parent.pullOptionalElement("AUTHORITY");
        if (element == null) {
            return Collections.singletonMap(IdentifiedObject.NAME_PROPERTY, name);
        }
        final String auth = element.pullString("name");
        final String code = element.pullString("code");
        element.close();
        final Map     properties = new HashMap(4);
        final Citation authority = Citation.createCitation(auth);
        properties.put(IdentifiedObject.       NAME_PROPERTY, new Identifier(authority, name));
        properties.put(IdentifiedObject.IDENTIFIERS_PROPERTY, new Identifier(authority, code));
        return properties;
    }

    /**
     * Parses an "UNIT" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * UNIT["<name>", <conversion factor> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  unit The contextual unit. Usually {@link SI#METRE} or {@link SI#RADIAN}.
     * @return The "UNIT" element as an {@link Unit} object.
     * @throws ParseException if the "UNIT" can't be parsed.
     *
     * @todo Authority code is currently ignored. We may consider to create a subclass of
     *       {@link Unit} which implements {@link IdentifiedObject} in a future version.
     */
    private static Unit parseUnit(final Element parent, final Unit unit)
            throws ParseException
    {
        final Element element = parent.pullElement("UNIT");
        final String     name = element.pullString("name");
        final double   factor = element.pullDouble("factor");
        final Map  properties = parseAuthority(element, name);
        element.close();
        return (factor!=1) ? unit.multiply(factor) : unit;
    }

    /**
     * Parses an "AXIS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * AXIS["<name>", NORTH | SOUTH | EAST | WEST | UP | DOWN | OTHER]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  unit The contextual unit. Usually {@link NonSI#DEGREE_ANGLE} or {@link SI#METRE}.
     * @param  required <code>true</code> if the axis is mandatory,
     *         or <code>false</code> if it is optional.
     * @return The "AXIS" element as a {@link CoordinateSystemAxis} object, or <code>null</code>
     *         if the axis was not required and there is no axis object.
     * @throws ParseException if the "AXIS" element can't be parsed.
     */
    private CoordinateSystemAxis parseAxis(final Element parent,
                                           final Unit    unit,
                                           final boolean required)
            throws ParseException
    {
        final Element element;
        if (required) {
            element = parent.pullElement("AXIS");
        } else {
            element = parent.pullOptionalElement("AXIS");
            if (element == null) {
                return null;
            }
        }
        final String         name = element.pullString     ("name");
        final Element orientation = element.pullVoidElement("orientation");
        element.close();
        final AxisDirection direction = (AxisDirection) directions.get(
                                        orientation.keyword.trim().toUpperCase());
        if (direction == null) {
            throw element.parseFailed(null,
                  Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, orientation));
        }
        try {
            return createAxis(name, direction, unit);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Creates an axis.
     *
     * @param  name The axis name.
     * @param  direction The axis direction.
     * @param  unit The axis unit.
     * @return The axis.
     * @throws FactoryException if the axis can't be created.
     */
    private CoordinateSystemAxis createAxis(final String        name,
                                            final AxisDirection direction,
                                            final Unit          unit)
            throws FactoryException
    {
        return csFactory.createCoordinateSystemAxis(Collections.singletonMap(
               IdentifiedObject.NAME_PROPERTY, name), name, direction, unit);
    }

    /**
     * Parses a "PRIMEM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PRIMEM["<name>", <longitude> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  angularUnit The contextual unit.
     * @return The "PRIMEM" element as a {@link PrimeMeridian} object.
     * @throws ParseException if the "PRIMEM" element can't be parsed.
     */
    private PrimeMeridian parsePrimem(final Element parent, final Unit angularUnit)
            throws ParseException
    {
        Element   element = parent.pullElement("PRIMEM");
        String       name = element.pullString("name");
        double  longitude = element.pullDouble("longitude");
        Map    properties = parseAuthority(element, name);
        element.close();
        try {
            return datumFactory.createPrimeMeridian(properties, longitude, angularUnit);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses an <strong>optional</strong> "TOWGS84" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * TOWGS84[<dx>, <dy>, <dz>, <ex>, <ey>, <ez>, <ppm>]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "TOWGS84" element as a {@link BursaWolfParameters} object,
     *         or <code>null</code> if no "TOWGS84" has been found.
     * @throws ParseException if the "TOWGS84" can't be parsed.
     */
    private static BursaWolfParameters parseToWGS84(final Element parent)
        throws ParseException 
    {          
        final Element element = parent.pullOptionalElement("TOWGS84");
        if (element == null) {
            return null;
        }
        final BursaWolfParameters info = new BursaWolfParameters(
                org.geotools.referencing.datum.GeodeticDatum.WGS84);
        info.dx  = element.pullDouble("dx");
        info.dy  = element.pullDouble("dy");
        info.dz  = element.pullDouble("dz");
        if (element.peek() != null) {
            info.ex  = element.pullDouble("ex");
            info.ey  = element.pullDouble("ey");
            info.ez  = element.pullDouble("ez");
            info.ppm = element.pullDouble("ppm");
        }
        element.close();
        return info;
    }

    /**
     * Parses a "SPHEROID" element. This element has the following pattern:
     *
     * <blockquote><code>
     * SPHEROID["<name>", <semi-major axis>, <inverse flattening> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "SPHEROID" element as an {@link Ellipsoid} object.
     * @throws ParseException if the "SPHEROID" element can't be parsed.
     */
    private Ellipsoid parseSpheroid(final Element parent) throws ParseException {       
        Element          element = parent.pullElement("SPHEROID");
        String              name = element.pullString("name");
        double     semiMajorAxis = element.pullDouble("semiMajorAxis");
        double inverseFlattening = element.pullDouble("inverseFlattening");
        Map           properties = parseAuthority(element, name);
        element.close();
        if (inverseFlattening == 0) {
            // Inverse flattening null is an OGC convention for a sphere.
            inverseFlattening = Double.POSITIVE_INFINITY;
        }
        try {
            return datumFactory.createFlattenedSphere(properties,
                    semiMajorAxis, inverseFlattening, SI.METER);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "PROJECTION" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PROJECTION["<name>" {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  ellipsoid The ellipsoid, or <code>null</code> if none.
     * @param  linearUnit The linear unit of the parent PROJCS element, or <code>null</code>.
     * @param  angularUnit The angular unit of the parent GEOCS element, or <code>null</code>.
     * @return The "PROJECTION" element as a {@link ParameterValueGroup} object.
     * @throws ParseException if the "PROJECTION" element can't be parsed.
     */
    private ParameterValueGroup parseProjection(final Element   parent,
                                                final Ellipsoid ellipsoid,
                                                final Unit      linearUnit,
                                                final Unit      angularUnit)
        throws ParseException
    {                
        final Element       element = parent.pullElement("PROJECTION");
        final String classification = element.pullString("name");
        final Map        properties = parseAuthority(element, classification);
        element.close();
        /*
         * Set the list of parameters.  NOTE: Parameters are defined in
         * the parent Element (usually a "PROJCS" element), not in this
         * "PROJECTION" element.
         *
         * We will set the semi-major and semi-minor parameters from the
         * ellipsoid first. If those values were explicitly specified in
         * a "PARAMETER" statement, they will overwrite the values inferred
         * from the ellipsoid.
         */
        final ParameterValueGroup parameters;
        try {
            parameters = mtFactory.getDefaultParameters(classification);
        } catch (NoSuchIdentifierException exception) {
            throw element.parseFailed(exception, null);
        }
        Element param = parent;
        try {
            if (ellipsoid != null) {
                final Unit axisUnit = ellipsoid.getAxisUnit();
                parameters.parameter("semi_major").setValue(ellipsoid.getSemiMajorAxis(), axisUnit);
                parameters.parameter("semi_minor").setValue(ellipsoid.getSemiMinorAxis(), axisUnit);
            }
            while ((param=parent.pullOptionalElement("PARAMETER")) != null) {
                final String         paramName  = param.pullString("name");
                final double         paramValue = param.pullDouble("value");
                final ParameterValue parameter  = parameters.parameter(paramName);
                final Unit expected = ((ParameterDescriptor)parameter.getDescriptor()).getUnit();
                if (expected!=null && !Unit.ONE.equals(expected)) {
                    if (linearUnit!=null && SI.METER.isCompatible(expected)) {
                        parameter.setValue(paramValue, linearUnit);
                        continue;
                    }
                    if (angularUnit!=null && SI.RADIAN.isCompatible(expected)) {
                        parameter.setValue(paramValue, angularUnit);
                        continue;
                    }
                }
                parameter.setValue(paramValue);
            }
        } catch (ParameterNotFoundException exception) {
            throw param.parseFailed(exception, Resources.format(
                  ResourceKeys.ERROR_UNEXPECTED_PARAMETER_$1, exception.getParameterName()));
        }
        return parameters;
    }

    /**
     * Parses a "DATUM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * DATUM["<name>", <spheroid> {,<to wgs84>} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  meridian the prime meridian.
     * @return The "DATUM" element as a {@link GeodeticDatum} object.
     * @throws ParseException if the "DATUM" element can't be parsed.
     */
    private GeodeticDatum parseDatum(final Element parent,
                                     final PrimeMeridian meridian)
            throws ParseException
    {
        Element             element = parent.pullElement("DATUM");
        String                 name = element.pullString("name");
        Ellipsoid         ellipsoid = parseSpheroid(element);
        BursaWolfParameters toWGS84 = parseToWGS84(element); // Optional; may be null.
        Map              properties = parseAuthority(element, name);
        element.close();
        if (toWGS84 != null) {
            if (properties.size() == 1) {
                properties = new HashMap(properties);
            }
            properties.put(org.geotools.referencing.datum.GeodeticDatum.BURSA_WOLF_PROPERTY,
                           toWGS84);
        }
        try {
            return datumFactory.createGeodeticDatum(properties, ellipsoid, meridian);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }        

    /**
     * Parses a "VERT_DATUM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * VERT_DATUM["<name>", <datum type> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "VERT_DATUM" element as a {@link VerticalDatum} object.
     * @throws ParseException if the "VERT_DATUM" element can't be parsed.
     */
    private VerticalDatum parseVertDatum(final Element parent) throws ParseException {        
        Element element = parent.pullElement("VERT_DATUM");
        String     name = element.pullString ("name");
        final int datum = element.pullInteger("datum");
        Map  properties = parseAuthority(element, name);
        element.close();
        final VerticalDatumType type = org.geotools.referencing.datum.VerticalDatum
                                       .getVerticalDatumTypeFromLegacyCode(datum);
        if (type == null) {
            throw element.parseFailed(null,
                  Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, new Integer(datum)));
        }
        try {
            return datumFactory.createVerticalDatum(properties, type);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "LOCAL_DATUM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * LOCAL_DATUM["<name>", <datum type> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "LOCAL_DATUM" element as an {@link EngineeringDatum} object.
     * @throws ParseException if the "LOCAL_DATUM" element can't be parsed.
     *
     * @todo The vertical datum type is currently ignored.
     */
    private EngineeringDatum parseLocalDatum(final Element parent) throws ParseException {
        Element element = parent.pullElement("LOCAL_DATUM");
        String     name = element.pullString ("name");
        final int datum = element.pullInteger("datum");
        Map  properties = parseAuthority(element, name);
        element.close();
        try {
            return datumFactory.createEngineeringDatum(properties);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "LOCAL_CS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * LOCAL_CS["<name>", <local datum>, <unit>, <axis>, {,<axis>}* {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "LOCAL_CS" element as an {@link EngineeringCRS} object.
     * @throws ParseException if the "LOCAL_CS" element can't be parsed.
     *
     * @todo The coordinate system used is always a Geotools implementation, since we don't
     *       know which method to invokes in the {@link CSFactory} (is it a cartesian
     *       coordinate system? a spherical one? etc.).
     */
    private EngineeringCRS parseLocalCS(final Element parent) throws ParseException {        
        Element           element = parent.pullElement("LOCAL_CS");
        String               name = element.pullString("name");
        EngineeringDatum    datum = parseLocalDatum(element);
        Unit           linearUnit = parseUnit(element, SI.METER);
        CoordinateSystemAxis axis = parseAxis(element, linearUnit, true);
        List                 list = new ArrayList();
        do {
            list.add(axis);
            axis = parseAxis(element, linearUnit, false);
        } while (axis != null);
        Map properties = parseAuthority(element, name);
        element.close();

        final CoordinateSystem cs;
        cs = new org.geotools.referencing.cs.CoordinateSystem(
                 Collections.singletonMap("name", name),
                 (CoordinateSystemAxis[]) list.toArray(new CoordinateSystemAxis[list.size()]));
        try {
            return crsFactory.createEngineeringCRS(properties, datum, cs);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }        

    /**
     * Parses a "GEOCCS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * GEOCCS["<name>", <datum>, <prime meridian>,  <linear unit>
     *        {,<axis> ,<axis> ,<axis>} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "GEOCCS" element as a {@link GeocentricCRS} object.
     * @throws ParseException if the "GEOCCS" element can't be parsed.
     */
    private GeocentricCRS parseGeoCCS(final Element parent) throws ParseException {        
        final Element        element = parent.pullElement("GEOCCS");
        final String            name = element.pullString("name");
        final Map         properties = parseAuthority(element, name);
        final PrimeMeridian meridian = parsePrimem   (element, NonSI.DEGREE_ANGLE);
        final GeodeticDatum    datum = parseDatum    (element, meridian);
        final Unit        linearUnit = parseUnit     (element, SI.METER);
        CoordinateSystemAxis axis0, axis1, axis2;
        axis0 = parseAxis(element, linearUnit, false);
        try {
            if (axis0 != null) {
                axis1 = parseAxis(element, linearUnit, true);
                axis2 = parseAxis(element, linearUnit, true);
            } else {
                // Those default values are part of WKT specification.
                axis0 = createAxis("X", AxisDirection.OTHER, linearUnit);
                axis1 = createAxis("Y", AxisDirection.EAST,  linearUnit);
                axis2 = createAxis("Z", AxisDirection.NORTH, linearUnit);
            }
            element.close();
            return crsFactory.createGeocentricCRS(properties, datum,
                    csFactory.createCartesianCS(properties, axis0, axis1, axis2));
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }        

    /**
     * Parses an <strong>optional</strong> "VERT_CS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * VERT_CS["<name>", <vert datum>, <linear unit>, {<axis>,} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "VERT_CS" element as a {@link VerticalCRS} object.
     * @throws ParseException if the "VERT_CS" element can't be parsed.
     */
    private VerticalCRS parseVertCS(final Element parent) throws ParseException { 
        final Element element = parent.pullElement("VERT_CS");
        if (element == null) {
            return null;
        }
        String               name = element.pullString("name");
        VerticalDatum       datum = parseVertDatum(element);
        Unit           linearUnit = parseUnit(element, SI.METER);
        CoordinateSystemAxis axis = parseAxis(element, linearUnit, false);
        Map            properties = parseAuthority(element, name);
        element.close();
        try {
            if (axis == null) {
                axis = createAxis("Z", AxisDirection.UP, linearUnit);
            }
            return crsFactory.createVerticalCRS(properties, datum,
                    csFactory.createVerticalCS(Collections.singletonMap("name", name), axis));
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "GEOGCS" element. This element has the following pattern:
     *
     * <blockquote><code>
     * GEOGCS["<name>", <datum>, <prime meridian>, <angular unit>  {,<twin axes>} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "GEOGCS" element as a {@link GeographicCRS} object.
     * @throws ParseException if the "GEOGCS" element can't be parsed.
     */
    private GeographicCRS parseGeoGCS(final Element parent) throws ParseException {
        Element            element = parent.pullElement("GEOGCS");
        String                name = element.pullString("name");
        Map             properties = parseAuthority(element, name);
        Unit           angularUnit = parseUnit     (element, SI.RADIAN);
        PrimeMeridian     meridian = parsePrimem   (element, angularUnit);
        GeodeticDatum        datum = parseDatum    (element, meridian);
        CoordinateSystemAxis axis0 = parseAxis     (element, angularUnit, false);
        CoordinateSystemAxis axis1;
        try {
            if (axis0 != null) {
                axis1 = parseAxis(element, angularUnit, true);
            } else {
                // Those default values are part of WKT specification.
                axis0 = createAxis("Lon", AxisDirection.EAST,  angularUnit);
                axis1 = createAxis("Lat", AxisDirection.NORTH, angularUnit);
            }
            element.close();
            return crsFactory.createGeographicCRS(properties, datum,
                    csFactory.createEllipsoidalCS(properties, axis0, axis1));
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "PROJCS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * PROJCS["<name>", <geographic cs>, <projection>, {<parameter>,}*,
     *        <linear unit> {,<twin axes>}{,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "PROJCS" element as a {@link ProjectedCRS} object.
     * @throws ParseException if the "GEOGCS" element can't be parsed.
     */
    private ProjectedCRS parseProjCS(final Element parent) throws ParseException {
        Element                element = parent.pullElement("PROJCS");
        String                    name = element.pullString("name");
        Map                 properties = parseAuthority(element, name);
        GeographicCRS           geoCRS = parseGeoGCS(element);
        Ellipsoid            ellipsoid = ((GeodeticDatum) geoCRS.getDatum()).getEllipsoid();
        Unit                linearUnit = parseUnit(element, SI.METER);
        Unit               angularUnit = geoCRS.getCoordinateSystem().getAxis(0).getUnit();
        ParameterValueGroup projection = parseProjection(element, ellipsoid, linearUnit, angularUnit);
        CoordinateSystemAxis     axis0 = parseAxis(element, linearUnit, false);
        CoordinateSystemAxis     axis1;
        try {
            if (axis0 != null) {
                axis1 = parseAxis(element, linearUnit, true);
            } else {
                // Those default values are part of WKT specification.
                axis0 = createAxis("X", AxisDirection.EAST,  linearUnit);
                axis1 = createAxis("Y", AxisDirection.NORTH, linearUnit);
            }
            element.close();
            if (factories == null) {
                factories = new FactoryGroup(datumFactory, csFactory, crsFactory, mtFactory);
            }
            return factories.createProjectedCRS(properties, geoCRS, null, projection,
                    csFactory.createCartesianCS(properties, axis0, axis1));
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "COMPD_CS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * COMPD_CS["<name>", <head cs>, <tail cs> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "COMPD_CS" element as a {@link CompoundCRS} object.
     * @throws ParseException if the "COMPD_CS" element can't be parsed.
     */
    private CompoundCRS parseCompdCS(final Element parent) throws ParseException {
        final CoordinateReferenceSystem[] CRS = new CoordinateReferenceSystem[2];
        Element element = parent.pullElement("COMPD_CS");
        String     name = element.pullString("name");
        Map  properties = parseAuthority(element, name);
        CRS[0] = parseCoordinateReferenceSystem(element);
        CRS[1] = parseCoordinateReferenceSystem(element);
        element.close();
        try {
            return crsFactory.createCompoundCRS(properties, CRS);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "FITTED_CS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * FITTED_CS["<name>", <to base>, <base cs>]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "FITTED_CS" element as a {@link CompoundCRS} object.
     * @throws ParseException if the "COMPD_CS" element can't be parsed.
     */
    private DerivedCRS parseFittedCS(final Element parent) throws ParseException {
        Element element = parent.pullElement("FITTED_CS");
        String     name = element.pullString("name");
        Map  properties = parseAuthority(element, name);
        final MathTransform toBase = parseMathTransform(element, true);
        final CoordinateReferenceSystem base = parseCoordinateReferenceSystem(element);
        final OperationMethod method = getOperationMethod();
        element.close();
        /*
         * WKT provides no informations about the underlying CS of a derived CRS.
         * We have to guess some reasonable one with arbitrary units.  We try to
         * construct the one which contains as few information as possible, in
         * order to avoid providing wrong informations.
         */
        final CoordinateSystemAxis[] axis = new CoordinateSystemAxis[toBase.getSourceDimensions()];
        final StringBuffer buffer = new StringBuffer(name);
        buffer.append(" axis ");
        final int start = buffer.length();
        try {
            for (int i=0; i<axis.length; i++) {
                final String number = String.valueOf(i);
                buffer.setLength(start);
                buffer.append(number);
                axis[i] = csFactory.createCoordinateSystemAxis(
                    Collections.singletonMap(IdentifiedObject.NAME_PROPERTY, buffer.toString()),
                    number, AxisDirection.OTHER, Unit.ONE);
            }
            return crsFactory.createDerivedCRS(properties, method, base, toBase.inverse(),
                   new org.geotools.referencing.cs.CoordinateSystem(properties, axis));
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        } catch (NoninvertibleTransformException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Returns the class of the specified WKT element. For example for this method returns
     * <code>{@linkplain ProjectedCRS}.class</code> for element "{@code PROJCS}".
     *
     * @param  element The WKT element name.
     * @return The GeoAPI class of the specified element, or <code>null</code> if unknow.
     */
    public static Class getClassOf(String element) {
        // No need to synchronize.
        element = element.trim().toUpperCase();
        if (types == null) {
            final Map map = new HashMap(25);
            map.put(        "GEOGCS",        GeographicCRS.class);
            map.put(        "PROJCS",         ProjectedCRS.class);
            map.put(        "GEOCCS",        GeocentricCRS.class);
            map.put(       "VERT_CS",          VerticalCRS.class);
            map.put(      "LOCAL_CS",       EngineeringCRS.class);
            map.put(      "COMPD_CS",          CompoundCRS.class);
            map.put(     "FITTED_CS",           DerivedCRS.class);
            map.put(          "AXIS", CoordinateSystemAxis.class);
            map.put(        "PRIMEM",        PrimeMeridian.class);
            map.put(       "TOWGS84",  BursaWolfParameters.class);
            map.put(      "SPHEROID",            Ellipsoid.class);
            map.put(    "VERT_DATUM",        VerticalDatum.class);
            map.put(   "LOCAL_DATUM",     EngineeringDatum.class);
            map.put(         "DATUM",        GeodeticDatum.class);
            map.put(      "PARAM_MT",        MathTransform.class);
            map.put(     "CONCAT_MT",        MathTransform.class);
            map.put(    "INVERSE_MT",        MathTransform.class);
            map.put("PASSTHROUGH_MT",        MathTransform.class);
            types = map; // Set the field only once completed, in order to avoid synchronisation.
        }
        return (Class) types.get(element);
    }

    /**
     * Read WKT strings from the {@linkplain System#in standard input stream} and
     * reformat them to the {@linkplain System#out standard output stream}. The
     * input is read until it reach the end-of-file (<code>[Ctrl-Z]</code> if
     * reading from the keyboard), or until an unparsable WKT has been hit.
     * Optional arguments are:
     *
     * <TABLE CELLPADDING='0' CELLSPACING='0'>
     *   <TR><TD NOWRAP><CODE>-authority</CODE> <VAR>&lt;name&gt;</VAR></TD>
     *       <TD NOWRAP>&nbsp;The authority to prefer when choosing WKT entities names.</TD></TR>
     *   <TR><TD NOWRAP><CODE>-indentation</CODE> <VAR>&lt;value&gt;</VAR></TD>
     *       <TD NOWRAP>&nbsp;Set the indentation (0 for output on a single line)</TD></TR>
     *   <TR><TD NOWRAP><CODE>-encoding</CODE> <VAR>&lt;code&gt;</VAR></TD>
     *       <TD NOWRAP>&nbsp;Set the character encoding</TD></TR>
     *   <TR><TD NOWRAP><CODE>-locale</CODE> <VAR>&lt;language&gt;</VAR></TD>
     *       <TD NOWRAP>&nbsp;Set the language for the output (e.g. "fr" for French)</TD></TR>
     * </TABLE>
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        final Integer indentation = arguments.getOptionalInteger(Formattable.INDENTATION);
        final String    authority = arguments.getOptionalString("-authority");
        args = arguments.getRemainingArguments(0);
        if (indentation != null) {
            Formattable.setIndentation(indentation.intValue());        
        }
        final BufferedReader in = new BufferedReader(Arguments.getReader(System.in));
        try {
            final Parser parser = new Parser();
            if (authority != null) {
                parser.setAuthority(Citation.createCitation(authority));
            }
            parser.reformat(in, arguments.out, arguments.err);
        } catch (Exception exception) {
            exception.printStackTrace(arguments.err);
        }
        // Do not close 'in', since it is the standard input stream.
    }
}
